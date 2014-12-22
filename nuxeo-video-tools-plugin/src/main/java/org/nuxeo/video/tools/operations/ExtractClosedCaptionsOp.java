/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     thibaud
 */

package org.nuxeo.video.tools.operations;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.video.tools.CCExtractor;

/**
 * 
 */
@Operation(id = ExtractClosedCaptionsOp.ID, category = Constants.CAT_CONVERSION, label = "Video: Extract Closed Captions", description = "Return a Blob containng the closed captions using <code>ccextractor</code> (see its documentation about <code>outFormat</code>). Its <code>startAt</code>/<code>endAt</code> are empty, the whole movie is handled. If the input is a document, you can use <code>xpath</code> (ignored if the input is a blob)")
public class ExtractClosedCaptionsOp {

    public static final String ID = "Video.ExtractClosedCaptions";

    @Param(name = "outFormat", required = false)
    protected String outFormat;

    @Param(name = "startAt", required = false)
    protected String startAt;

    @Param(name = "endAt", required = false)
    protected String endAt;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod
    public Blob run(DocumentModel inDoc) {

        return run((Blob) inDoc.getPropertyValue("file:content"));

    }

    @OperationMethod
    public Blob run(Blob inBlob) {

        Blob result = null;

        CCExtractor cce = new CCExtractor(inBlob, startAt, endAt);
        result = cce.extractCC(outFormat);

        return result;
    }

}