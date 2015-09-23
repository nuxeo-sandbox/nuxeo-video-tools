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
import java.nio.file.Files;

import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 */
public class VideoWatermarker extends BaseVideoTools {

    protected static final String COMMAND_WATERMARK_WITH_PICTURE = "videoWatermarkWithPicture";

    public VideoWatermarker(Blob inBlob) {
        super(inBlob);
    }

    /* The command line is:
     * ffmpeg -y -i #{sourceFilePath}  #{pictureFilePath} -filter_complex "overlay:#{pos_x}:#{pos_y}" #{outFilePath}
     * 
     */
    public Blob watermarkWithPicture(String inFinalFileName, Blob inWatermark, String x, String y) throws IOException,
            CommandNotAvailable, ClientException {

        FileBlob result = null;
        String originalMimeType;
        
        String imgPath = inWatermark.getFile().getAbsolutePath();

        originalMimeType = blob.getMimeType();

        // Prepare parameters
        if (inFinalFileName == null || inFinalFileName.isEmpty()) {
            inFinalFileName = VideoToolsUtilities.addSuffixToFileName(
                    blob.getFilename(), "-WM");
        }

        // Run the command line
        CmdParameters params = new CmdParameters();
        
        File sourceFile = getBlobFile();
        params.addNamedParameter("sourceFilePath", sourceFile.getAbsolutePath());
        
        params.addNamedParameter("pictureFilePath", imgPath);
        params.addNamedParameter("pos_x", x);
        params.addNamedParameter("pos_y", y);
        
        // The temp destination file
        String outFilePath = getTempDirectoryPath() + "/" + "VW-"
                + java.util.UUID.randomUUID().toString().replace("-", "") + "-"
                + inFinalFileName;
        params.addNamedParameter("outFilePath", outFilePath);

        CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
        ExecResult clResult = cles.execCommand(COMMAND_WATERMARK_WITH_PICTURE,
                params);

        // Get the result, and first, handle errors.
        if (clResult.getError() != null) {
            throw new ClientException("Failed to execute the command <"
                    + COMMAND_WATERMARK_WITH_PICTURE + ">", clResult.getError());
        }

        if (!clResult.isSuccessful()) {
            throw new ClientException("Failed to execute the command <"
                    + COMMAND_WATERMARK_WITH_PICTURE + ">. Final command [ "
                    + clResult.getCommandLine() + " ] returned with error "
                    + clResult.getReturnCode());
        }

        // Build the Blob
        File resultFile = new File(outFilePath);
        result = new FileBlob(resultFile);
        result.setFilename(inFinalFileName);
        result.setMimeType(originalMimeType);

        return result;
    }

}
