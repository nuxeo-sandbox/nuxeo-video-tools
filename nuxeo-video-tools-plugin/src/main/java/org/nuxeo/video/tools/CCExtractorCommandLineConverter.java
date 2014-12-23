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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;

public class CCExtractorCommandLineConverter extends
        BaseVideoToolsCommandLineConverter {

    public static final Log log = LogFactory.getLog(CCExtractorCommandLineConverter.class);

    // #{sourceFilePath} -out=#{outFormat} -o #{outFileName}
    @Override
    protected Map<String, String> getCmdStringParameters(BlobHolder blobHolder,
            Map<String, Serializable> parameters) throws ConversionException {

        Map<String, String> cmdStringParams = new HashMap<String, String>();

        String baseDir = getTmpDirectory(parameters);
        Path tmpPath = new Path(baseDir).append("NxVT_"
                + java.util.UUID.randomUUID().toString());

        File outDir = new File(tmpPath.toString());
        boolean dirCreated = outDir.mkdir();
        if (!dirCreated) {
            throw new ConversionException(
                    "Unable to create tmp dir for transformer output");
        }

        cmdStringParams.put("outDirPath", outDir.getAbsolutePath());

        // outFormat is a required parameter
        String outFormat = (String) parameters.get("outFormat");
        cmdStringParams.put("outFormat", outFormat);

        String startAt = (String) parameters.get("startAt");
        String endAt = (String) parameters.get("endAt");
        if (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) {
            cmdStringParams.put("startAt", startAt);
            cmdStringParams.put("endAt", endAt);
        }

        String fileName = blobHolder.getBlob().getFilename();
        int pos = fileName.lastIndexOf('.');
        if (pos > -1) {
            fileName = fileName.substring(0, pos);
        }
        String outFilePath = outDir.getAbsolutePath() + "/" + fileName + "."
                + outFormat;
        cmdStringParams.put("outFilePath", outFilePath);

        return cmdStringParams;
    }
}
