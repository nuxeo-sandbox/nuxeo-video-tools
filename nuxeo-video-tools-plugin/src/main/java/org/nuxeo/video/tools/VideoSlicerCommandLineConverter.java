/* (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;

public class VideoSlicerCommandLineConverter extends
        BaseVideoToolsCommandLineConverter {

    public static final Log log = LogFactory.getLog(VideoSlicerCommandLineConverter.class);

    // -i #{sourceFilePath} -ss #{start} -t #{duration} -acodec copy -vcodec
    // copy #{outFilePath}
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

        String start = (String) parameters.get("start");
        String duration = (String) parameters.get("duration");
        cmdStringParams.put("start", start);
        cmdStringParams.put("duration", duration);

        String fileName = blobHolder.getBlob().getFilename();
        String suffix = start.replace(":", "") + "-"
                + duration.replace(":", "");
        fileName = BaseVideoToolsCommandLineConverter.addSuffixToFileName(
                fileName, suffix);

        String outFilePath = outDir.getAbsolutePath() + "/" + fileName;
        cmdStringParams.put("outFilePath", outFilePath);

        return cmdStringParams;
    }

}
