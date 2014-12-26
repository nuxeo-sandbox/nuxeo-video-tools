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
import org.nuxeo.video.tools.VideoConverter;
import org.nuxeo.video.tools.VideoSlicer;

@Operation(id = VideoConverterOp.ID, category = Constants.CAT_CONVERSION, label = "Video: Convert", description = "Use either <code>height</code> <i>or</i> <code>scale</scale>. If both are > 0, the operation uses <code>height</code>")
public class VideoConverterOp {

    public static final String ID = "Video.Convert";

    @Param(name = "height", required = false)
    protected long height;

    @Param(name = "scale", required = false)
    protected double scale;

    @Param(name = "converter", required = false)
    protected String converter;

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob inBlob) {

        Blob result = null;

        VideoConverter vc = new VideoConverter(inBlob);
        if (height > 0) {
            result = vc.convert(height, converter);
        } else {
            result = vc.convert(scale, converter);
        }

        return result;
    }

}
