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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.nuxeo.ecm.platform.picture.api.BlobHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Warning: ccextractor command line (http://ccextractor.sourceforge.net) must
 * be installed
 * 
 * The closed captions are extracted in a text file (depending on the requests
 * output format)
 *
 * @since 7.1
 */
public class CCExtractor extends BaseVideoTools {

    private static final Log log = LogFactory.getLog(CCExtractor.class);

    public static final String COMMAND_FULL_VIDEO = "videoClosedCaptionsExtractor";

    public static final String COMMAND_SLICED_VIDEO = "videoPartClosedCaptionsExtractor";

    public static final String DEFAULT_OUTFORMAT = "ttxt";

    public static final List<String> TEXT_OUTFORMATS = Collections.unmodifiableList(Arrays.asList(
            "srt", "txt", "ttxt"));

    String startAt;

    String endAt;

    public CCExtractor(Blob inBlob) {
        super(inBlob);
    }

    public CCExtractor(Blob inBlob, String inStartAt, String inEndAt) {
        super(inBlob);

        setStartAt(inStartAt);
        setEndAt(inEndAt);
    }

    public Blob extractCC() throws CommandNotAvailable, IOException {
        return extractCC(null);
    }

    protected boolean isTextOutFormat(String inFormat) {
        return TEXT_OUTFORMATS.contains(inFormat);
    }

    public Blob extractCC(String theOutFormat) throws CommandNotAvailable,
            IOException {

        Blob blobCC = null;

        if (blob == null) {
            return null;
        }

        CmdParameters params = new CmdParameters();

        File sourceFile = getBlobFile();
        params.addNamedParameter("sourceFilePath", sourceFile.getAbsolutePath());

        if (StringUtils.isBlank(theOutFormat)) {
            theOutFormat = DEFAULT_OUTFORMAT;
        }
        params.addNamedParameter("outFormat", theOutFormat);

        String commandLineName = COMMAND_FULL_VIDEO;
        if (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) {
            commandLineName = COMMAND_SLICED_VIDEO;
            params.addNamedParameter("startAt", startAt);
            params.addNamedParameter("endAt", endAt);
        }

        String baseDir = System.getProperty("java.io.tmpdir");
        String fileName = blob.getFilename();
        int pos = fileName.lastIndexOf('.');
        if (pos > -1) {
            fileName = fileName.substring(0, pos);
        }

        String outFilePath = baseDir + "CCE-"
                + java.util.UUID.randomUUID().toString().replace("-", "") + "-"
                + fileName + "." + theOutFormat;
        params.addNamedParameter("outFilePath", outFilePath);

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
        // ccextractor always create a file, even if there is no captions.
        // We must check if the file is empty or not, while handling BOMs of
        // Unicode files.
        // Let's say that less than 5 bytes, we don't have a caption.
        if (resultFile.exists()) {
            if (resultFile.length() > 5) {
                blobCC = new FileBlob(resultFile);
                blobCC.setFilename(blob.getFilename() + "." + theOutFormat);
                if (isTextOutFormat(theOutFormat)) {
                    blobCC.setMimeType("text/plain");
                }
                Framework.trackFile(resultFile, blobCC);
            } else {
                Framework.trackFile(resultFile, this);
            }
            resultFile.deleteOnExit();
        }

        return blobCC;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }
}
