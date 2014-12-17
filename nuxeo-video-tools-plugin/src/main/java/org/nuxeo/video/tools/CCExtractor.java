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

import org.apache.commons.lang.StringUtils;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.runtime.api.Framework;

/**
 * Warning: ccextractor command line must be installed
 *
 * @since 7.1
 */
public class CCExtractor extends AbstractVideoTools {

    public static final String CONVERTER_FULL_VIDEO = "videoToClosedCaptions";

    public static final String CONVERTER_SLICED_VIDEO = "videoToClosedCaptionsSliced";

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

    public Blob extractCC(String theOutFormat) {

        Blob blobCC = null;

        ConversionService conversionService = Framework.getService(ConversionService.class);

        BlobHolder source = new SimpleBlobHolder(blob);
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();

        if (!StringUtils.isBlank(theOutFormat)) {
            parameters.put("outFormat", theOutFormat);
        }

        BlobHolder result = null;
        String converterName = CONVERTER_FULL_VIDEO;
        if (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) {
            converterName = CONVERTER_SLICED_VIDEO;
            parameters.put("startAt", startAt);
            parameters.put("endAt", endAt);
        }
        result = conversionService.convert(converterName, source, parameters);
        if (result != null) {
            blobCC = result.getBlob();
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
