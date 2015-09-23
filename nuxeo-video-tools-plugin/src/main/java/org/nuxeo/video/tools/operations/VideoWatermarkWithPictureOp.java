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

import java.io.IOException;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.video.tools.VideoConverter;
import org.nuxeo.video.tools.VideoSlicer;
import org.nuxeo.video.tools.VideoWatermarker;

/**
 * Uses a video converter (declared in an XML extension) to transcode the video
 * using a new height. Use either <code>height</code> <i>or</i>
 * <code>scale</scale>. If both are > 0, the operation uses <code>height</code>.
 * If the height is <= 0, then the video is just transcoded (not resized).
 */
@Operation(id = VideoWatermarkWithPictureOp.ID, category = Constants.CAT_CONVERSION, label = "Video: Watermark with Picture", description = "")
public class VideoWatermarkWithPictureOp {

    public static final String ID = "Video.WatermarkWithPicture";

    @Param(name = "pictureDoc", required = true)
    protected DocumentModel pictureDoc;

    @Param(name = "x", required = false)
    protected String x;

    @Param(name = "y", required = false)
    protected String y;

    @Param(name = "resultFileName", required = false)
    protected String resultFileName;

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob inBlob) throws ClientException, IOException, CommandNotAvailable {

        Blob result = null;

        VideoWatermarker vw = new VideoWatermarker(inBlob);
        
        Blob picture = (Blob) pictureDoc.getPropertyValue("file:content");
        
        result = vw.watermarkWithPicture(resultFileName, picture, x, y);

        return result;
    }

}
