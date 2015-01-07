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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.runtime.api.Framework;

public class VideoSlicer extends BaseVideoTools {

    public static final String CONVERTER_SLICE_DEFAULT = "videoSlicer";

    public static final String CONVERTER_SLICE_BY_COPY = "videoSlicerByCopy";

    protected DecimalFormat s_msFormat = new DecimalFormat("#.###");
    
    protected String converterName = CONVERTER_SLICE_DEFAULT;

    public VideoSlicer(Blob inBlob) {
        super(inBlob);
    }

    public Blob slice(String inStart, String inDuration) {

        Blob sliced = null;

        if (blob == null) {
            return null;
        }

        ConversionService conversionService = Framework.getService(ConversionService.class);

        BlobHolder source = new SimpleBlobHolder(blob);
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();

        parameters.put("start", inStart);
        parameters.put("duration", inDuration);

        BlobHolder result = conversionService.convert(converterName, source,
                parameters);
        if (result != null) {
            sliced = result.getBlob();
            String fileName = VideoToolsUtilities.addSuffixToFileName(
                    blob.getFilename(), "-" + inStart.replaceAll(":", "") + "-"
                            + inDuration.replaceAll(":", ""));
            sliced.setFilename(fileName);
            // The command-line does not change the format
            sliced.setMimeType(blob.getMimeType());
        }

        return sliced;
    }

    public BlobList slice(int inCount) {

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
    
    public void setConverter(String inConverter) {
        converterName = inConverter;
    }
    
    public String getConverter() {
        return converterName;
    }

}
