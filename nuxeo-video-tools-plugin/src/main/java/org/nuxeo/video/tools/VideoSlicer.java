/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.video.tools;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.runtime.api.Framework;

public class VideoSlicer extends BaseVideoTools {

    public static final String COMMAND_SLICER_DEFAULT = "videoSlicer";

    public static final String COMMAND_SLICER_BY_COPY = "videoSlicerByCopy";

    protected DecimalFormat s_msFormat = new DecimalFormat("#.###");

    protected String commandLineName = COMMAND_SLICER_DEFAULT;

    public VideoSlicer(Blob inBlob) {
        super(inBlob);
    }

    public Blob slice(String inStart, String inDuration) throws IOException, CommandNotAvailable {

        Blob sliced = null;

        if (blob == null) {
            return null;
        }

        CmdParameters params = new CmdParameters();

        // Get the final name, adding start/duration to the original name
        String finalFileName = VideoToolsUtilities.addSuffixToFileName(
                blob.getFilename(), "-" + inStart.replaceAll(":", "") + "-"
                        + inDuration.replaceAll(":", ""));

        // The source video
        File sourceFile = getBlobFile();
        params.addNamedParameter("sourceFilePath", sourceFile.getAbsolutePath());

        // The temp destination file
        String outFilePath = getTempDirectoryPath() + "/" + "VS-"
                + java.util.UUID.randomUUID().toString().replace("-", "") + "-"
                + finalFileName;
        params.addNamedParameter("outFilePath", outFilePath);

        params.addNamedParameter("start", inStart);
        params.addNamedParameter("duration", inDuration);
        
        CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
        ExecResult clResult = cles.execCommand(commandLineName, params);
        
        // Get the result, and first, handle errors.
        if (clResult.getError() != null) {
            throw new ClientException("Failed to execute the command <"
                    + commandLineName + ">", clResult.getError());
        }

        if (!clResult.isSuccessful()) {
            throw new ClientException("Failed to execute the command <"
                    + commandLineName + ">. Final command [ "
                    + clResult.getCommandLine() + " ] returned with error "
                    + clResult.getReturnCode());
        }

        // Build the Blob
        File resultFile = new File(outFilePath);
        sliced = new FileBlob(resultFile);
        sliced.setFilename(finalFileName);
        sliced.setMimeType(blob.getMimeType());
        Framework.trackFile(resultFile, sliced);

        return sliced;
    }

    public BlobList slice(int inCount) throws IOException, CommandNotAvailable {

        BlobList parts = new BlobList();

        if (inCount < 2) {
            parts.add(blob);
        } else {

            VideoInfo vi = VideoHelper.getVideoInfo(blob);

            double start = 0.0;
            double duration = vi.getDuration() / (double) inCount;
            // ROund to 3 digits for milliseconds
            duration = (double) Math.round(duration * 1000) / 1000;
            String durationStr;// = s_msFormat.format(duration);
            // realign the duration
            // duration = Double.valueOf(durationStr);
            double totalSlicedDuration = duration * (double) inCount;
            while (totalSlicedDuration > vi.getDuration()) {
                duration -= 0.001;
                duration = (double) Math.round(duration * 1000) / 1000;
                totalSlicedDuration = duration * (double) inCount;
            }
            durationStr = String.valueOf(duration);

            // First one
            Blob oneSlice = this.slice("" + start, durationStr);
            parts.add(oneSlice);
            // Others
            for (int i = 1; i < inCount; i++) {
                start += duration;
                if (i == (inCount - 1)) {
                    double remaining = vi.getDuration() - totalSlicedDuration;
                    if (remaining > 0) {
                        duration += remaining + 0.05;
                        duration = (double) Math.round(duration * 1000) / 1000;
                        durationStr = String.valueOf(duration);
                    }
                }
                oneSlice = this.slice("" + start, durationStr);
                parts.add(oneSlice);
            }
        }

        return parts.size() == 0 ? null : parts;

    }

    public void setCommandLineName(String inCommandLineName) {
        commandLineName = inCommandLineName;
    }

    public String getCommandLineName() {
        return commandLineName;
    }

}
