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
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.video.tools.VideoConcatDemuxer;
import org.nuxeo.video.tools.VideoSlicer;

/**
 * 
 */
@Operation(id = VideoConcatDemuxerOp.ID, category = Constants.CAT_CONVERSION, label = "Video: Concat (ffmpeg demuxer)", description = "")
public class VideoConcatDemuxerOp {

    public static final String ID = "Video.ConcatWithFfmpegDemuxer";

    @Param(name = "resultFileName", required = false)
    protected String resultFileName;

    @OperationMethod
    public Blob run(BlobList inBlobs) throws ClientException, IOException, CommandNotAvailable {

        Blob result = null;

        VideoConcatDemuxer vc = new VideoConcatDemuxer();
        vc.addBlobs(inBlobs);
        result = vc.concat(resultFileName);

        return result;
    }
    
    @OperationMethod
    public Blob run(DocumentModelList inDocs) throws ClientException, IOException, CommandNotAvailable {

        Blob result = null;

        VideoConcatDemuxer vc = new VideoConcatDemuxer();
        for(DocumentModel doc : inDocs) {
            vc.addBlob((Blob) doc.getPropertyValue("file:content"));
        }
        
        result = vc.concat(resultFileName);

        return result;
    }

}
