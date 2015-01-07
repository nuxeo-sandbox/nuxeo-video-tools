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

package org.nuxeo.video.tools.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.video.tools.VideoSlicer;

/**
 * 
 */
@Operation(id = VideoSlicerOp.ID, category = Constants.CAT_CONVERSION, label = "Video: Slice", description = "SLice the input blob starting at <code>start</code>, for <code>duration</code>. A specific converte can be used (for example, use videoSlicerByCopy for very fast cut (because ffmpeg does not re-encode the video) if you know there will be no frame or timestamp issue in the sliced video")
public class VideoSlicerOp {

    public static final String ID = "Video.Slice";

    @Param(name = "start", required = false)
    protected String start;

    @Param(name = "duration", required = false)
    protected String duration;

    @Param(name = "converter", required = false, values = { "videoSlicer" })
    protected String converter;

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob inBlob) {

        Blob result = null;

        VideoSlicer slicer = new VideoSlicer(inBlob);
        if(converter != null && !converter.isEmpty()) {
            slicer.setConverter(converter);
        }
        result = slicer.slice(start, duration);

        return result;
    }

}
