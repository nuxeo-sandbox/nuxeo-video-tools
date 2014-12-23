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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
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

    public static final String CONVERTER_FULL_VIDEO = "videoClosedCaptionsExtractor";

    public static final String CONVERTER_SLICED_VIDEO = "videoPartClosedCaptionsExtractor";

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

    public Blob extractCC() {
        return extractCC(null);
    }

    protected boolean isTextOutFormat(String inFormat) {
        return TEXT_OUTFORMATS.contains(inFormat);
    }

    public Blob extractCC(String theOutFormat) {

        Blob blobCC = null;

        if (blob == null) {
            return null;
        }

        ConversionService conversionService = Framework.getService(ConversionService.class);

        BlobHolder source = new SimpleBlobHolder(blob);
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();

        if (StringUtils.isBlank(theOutFormat)) {
            theOutFormat = DEFAULT_OUTFORMAT;
        }
        parameters.put("outFormat", theOutFormat);

        BlobHolder result = null;
        String converterName = CONVERTER_FULL_VIDEO;
        if (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) {
            converterName = CONVERTER_SLICED_VIDEO;
            parameters.put("startAt", startAt);
            parameters.put("endAt", endAt);
        }
        // In case of problem, whatever the problem, we don't store the
        // ClosedCations
        try {
            result = conversionService.convert(converterName, source,
                    parameters);
        } catch (Exception e) {
            log.error("Error while extracting Closed Captions");
            result = null;
        }

        if (result != null) {
            blobCC = result.getBlob();
            // ccextractor always create a file, even if there is no captions.
            // We must check if the file is empty or not, while handling BOMs of
            // Unicode files.
            // Let's say that less than 5 bytes, we don't have a caption.
            File f = BlobHelper.getFileFromBlob(blobCC);
            if (f.length() < 5) {
                blobCC = null;
                f.delete();
            } else {
                blobCC.setFilename(blob.getFilename() + "." + theOutFormat);
                if (isTextOutFormat(theOutFormat)) {
                    blobCC.setMimeType("text/plain");
                }
            }
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
