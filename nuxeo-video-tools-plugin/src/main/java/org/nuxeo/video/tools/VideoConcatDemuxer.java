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
 * See https://trac.ffmpeg.org/wiki/Concatenate
 * 
 * Syntax is: ffmpeg -f concat -i mylist.txt -c copy output
 * <p>
 * With mylist.txt: <code>
 * file '/path/to/file1'
 * file '/path/to/file2'
 * file '/path/to/file3'
 * </code>
 * <p>
 * Quote from ffmpeg doc:
 * <p>
 * <quote> The timestamps in the files are adjusted so that the first file
 * starts at 0 and each next file starts where the previous one finishes. Note
 * that it is done globally and may cause gaps if all streams do not have
 * exactly the same length.
 * 
 * All files must have the same streams (same codecs, same time base, etc.).
 * 
 * The duration of each file is used to adjust the timestamps of the next file:
 * if the duration is incorrect (because it was computed using the bit-rate or
 * because the file is truncated, for example), it can cause artifacts. The
 * duration directive can be used to override the duration stored in each file.
 * 
 * [snip]
 * 
 * The concat demuxer can support variable frame rate, but it currently requires
 * that all files have the same time base for the corresponding files. </quote>
 * 
 *
 * @since TODO
 */
public class VideoConcatDemuxer {

    protected static final String COMMAND_CONCAT_VIDEOS_DEMUXER = "concatVideos-demuxer";

    protected BlobList blobs = new BlobList();

    public VideoConcatDemuxer() {

    }

    public void addBlob(Blob inBlob) {
        if (inBlob != null) {
            blobs.add(inBlob);
        }
    }

    public void addBlobs(BlobList inBlobs) {
        for (Blob b : inBlobs) {
            addBlob(b);
        }
    }

    public Blob concat() throws IOException, CommandNotAvailable,
            ClientException {
        return concat(null);
    }

    // -f concat -i #{listFilePath} -c copy #{outFilePath}
    public Blob concat(String inFinalFileName) throws IOException,
            CommandNotAvailable, ClientException {

        FileBlob result = null;
        String originalMimeType;

        if (blobs.size() == 0) {
            return null;
        }

        originalMimeType = blobs.get(0).getMimeType();

        // Prepare parameters
        if (inFinalFileName == null || inFinalFileName.isEmpty()) {
            inFinalFileName = VideoToolsUtilities.addSuffixToFileName(
                    blobs.get(0).getFilename(), "-concat");
        }

        String list = "";
        for (Blob b : blobs) {
            File f = VideoToolsUtilities.getBlobFile(b);
            list += "file '" + f.getAbsolutePath() + "'\n";
        }

        File tempFile = File.createTempFile("NxVTcv-", ".txt");
        Files.write(tempFile.toPath(), list.getBytes());
        tempFile.deleteOnExit();
        Framework.trackFile(tempFile, this);

        String outputFilePath = tempFile.getParent() + "/" + inFinalFileName;

        // Run the command line
        CmdParameters params = new CmdParameters();
        params.addNamedParameter("listFilePath", tempFile.getAbsolutePath());
        params.addNamedParameter("outFilePath", outputFilePath);

        CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
        ExecResult clResult = cles.execCommand(COMMAND_CONCAT_VIDEOS_DEMUXER,
                params);

        // Get the result, and first, handle errors.
        if (clResult.getError() != null) {
            throw new ClientException("Failed to execute the command <"
                    + COMMAND_CONCAT_VIDEOS_DEMUXER + ">", clResult.getError());
        }

        if (!clResult.isSuccessful()) {
            throw new ClientException("Failed to execute the command <"
                    + COMMAND_CONCAT_VIDEOS_DEMUXER + ">. Final command [ "
                    + clResult.getCommandLine() + " ] returned with error "
                    + clResult.getReturnCode());
        }

        // Build the Blob
        File resultFile = new File(outputFilePath);
        result = new FileBlob(resultFile);
        result.setFilename(inFinalFileName);
        result.setMimeType(originalMimeType);

        return result;
    }

}
