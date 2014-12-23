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

import static org.nuxeo.ecm.core.api.CoreSession.ALLOW_VERSION_WRITE;
import static org.nuxeo.video.tools.VideoToolsConstants.*;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.video.Video;
import org.nuxeo.ecm.platform.video.VideoDocument;
import org.nuxeo.runtime.api.Framework;

public class CCExtractorWork extends AbstractWork {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CCExtractorWork.class);

    public static final String CATEGORY_VIDEO_CLOSED_CAPTIONS_EXTRACTOR = "videoClosedCaptionsExtractor";

    public static final String VIDEO_EXTRACT_CLOSED_CAPTIONS_DONE_EVENT = "videoClosedCaptionsExtractionDone";

    protected static String computeIdPrefix(String repositoryName, String docId) {
        return repositoryName + ':' + docId + ":closedCaptionsExtraction:";
    }

    public CCExtractorWork(String repositoryName, String docId) {
        super(computeIdPrefix(repositoryName, docId));
        setDocument(repositoryName, docId);
    }

    @Override
    public String getCategory() {
        return CATEGORY_VIDEO_CLOSED_CAPTIONS_EXTRACTOR;
    }

    @Override
    public String getTitle() {
        return "Video Closed Captions Extraction";
    }

    @Override
    public void work() {
        setStatus("Extracting Closed Captions");
        setProgress(Progress.PROGRESS_INDETERMINATE);

        Video originalVideo = null;
        try {
            initSession();
            originalVideo = getVideoToConvert();
            commitOrRollbackTransaction();
        } finally {
            cleanUp(true, null);
        }

        Blob result = null;
        if (originalVideo != null) {
            CCExtractor cce = new CCExtractor(originalVideo.getBlob());
            result = cce.extractCC("ttxt");
        }
        saveDocument(result);
    }

    protected void saveDocument(Blob inClosedCaptions) {

        if (inClosedCaptions == null) {
            setStatus("No Closed Captions, ot no video file");
        } else {
            setStatus("Saving Closed Captions");
        }
        startTransaction();
        initSession();
        DocumentModel doc = session.getDocument(new IdRef(docId));
        if (inClosedCaptions != null) {
            doc.setPropertyValue(CLOSED_CAPTIONS_BLOB_XPATH,
                    (Serializable) inClosedCaptions);
            doc.setPropertyValue(CLOSED_CAPTIONS_FILENAME_XPATH,
                    inClosedCaptions.getFilename());
        } else {
            doc.setPropertyValue(CLOSED_CAPTIONS_BLOB_XPATH, null);
            doc.setPropertyValue(CLOSED_CAPTIONS_FILENAME_XPATH, null);
        }

        // It may happen the async. job is done while, in the meantime, the user
        // created a version
        if (doc.isVersion()) {
            doc.putContextData(ALLOW_VERSION_WRITE, Boolean.TRUE);
        }
        session.saveDocument(doc);
        fireClosedCaptionsExtractionDoneEvent(doc);
        setStatus("Done");

    }

    protected Video getVideoToConvert() throws ClientException {
        DocumentModel doc = session.getDocument(new IdRef(docId));
        VideoDocument videoDocument = doc.getAdapter(VideoDocument.class);
        Video video = videoDocument.getVideo();
        if (video == null) {
            log.warn("No video file avilable for: " + doc);
        }
        return video;
    }

    protected void fireClosedCaptionsExtractionDoneEvent(DocumentModel doc) {
        WorkManager workManager = Framework.getLocalService(WorkManager.class);
        List<String> workIds = workManager.listWorkIds(
                CATEGORY_VIDEO_CLOSED_CAPTIONS_EXTRACTOR, null);
        String idPrefix = computeIdPrefix(repositoryName, docId);
        int worksCount = 0;
        for (String workId : workIds) {
            if (workId.startsWith(idPrefix)) {
                if (++worksCount > 1) {
                    // another work scheduled
                    return;
                }
            }
        }

        DocumentEventContext ctx = new DocumentEventContext(session,
                session.getPrincipal(), doc);
        Event event = ctx.newEvent(VIDEO_EXTRACT_CLOSED_CAPTIONS_DONE_EVENT);
        Framework.getLocalService(EventService.class).fireEvent(event);
    }

}
