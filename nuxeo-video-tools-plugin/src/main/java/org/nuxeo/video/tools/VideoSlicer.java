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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.runtime.api.Framework;

public class VideoSlicer extends BaseVideoTools {

    public static final String CONVERTER_NAME = "videoSlicer";

    protected String start;

    protected String duration;

    public VideoSlicer(Blob inBlob, String inStart, String inDuration) {
        super(inBlob);

        start = inStart;
        duration = inDuration;
    }

    public Blob slice() {

        Blob sliced = null;

        if (blob == null) {
            return null;
        }

        ConversionService conversionService = Framework.getService(ConversionService.class);

        BlobHolder source = new SimpleBlobHolder(blob);
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();

        parameters.put("start", start);
        parameters.put("duration", duration);

        BlobHolder result = conversionService.convert(CONVERTER_NAME, source,
                parameters);
        if (result != null) {
            sliced = result.getBlob();
            String fileName = VideoToolsUtilities.addSuffixToFileName(
                    blob.getFilename(), "-" + start.replaceAll(":", "") + "-"
                            + duration.replaceAll(":", ""));
            sliced.setFilename(fileName);
            // The command-line does not change the format
            sliced.setMimeType(blob.getMimeType());
        }

        return sliced;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

}
