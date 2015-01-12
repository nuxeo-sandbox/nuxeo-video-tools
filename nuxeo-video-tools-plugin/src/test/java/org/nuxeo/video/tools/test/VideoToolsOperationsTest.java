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

package org.nuxeo.video.tools.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.video.tools.CCExtractor;
import org.nuxeo.video.tools.VideoConcatDemuxer;
import org.nuxeo.video.tools.VideoConverter;
import org.nuxeo.video.tools.VideoSlicer;
import org.nuxeo.video.tools.operations.ExtractClosedCaptionsOp;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, CoreFeature.class,
        EmbeddedAutomationServerFeature.class })
@Deploy({ "nuxeo-video-tools", "org.nuxeo.ecm.platform.commandline.executor",
        "org.nuxeo.ecm.platform.video.core",
        "org.nuxeo.ecm.platform.video.convert",
        "org.nuxeo.ecm.platform.picture.core" })
public class VideoToolsOperationsTest {

    protected static final Log log = LogFactory.getLog(VideoToolsOperationsTest.class);

    protected DocumentModel parentOfTestDocs;

    protected DocumentModel docForCC, docForMege1, docForMege2, docForMege3,
            docForMege4;

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    @Inject
    EventService eventService;

    protected void doLog(String what) {
        System.out.println(what);
    }

    // Not sure it's the best way to get the current method name, but at least
    // it works
    protected String getCurrentMethodName(RuntimeException e) {
        StackTraceElement currentElement = e.getStackTrace()[0];
        return currentElement.getMethodName();
    }

    protected DocumentModel createVideoDocument(File inFile, boolean inSaveIt) {

        DocumentModel videoDoc = coreSession.createDocumentModel(
                parentOfTestDocs.getPathAsString(), inFile.getName(), "Video");
        videoDoc.setPropertyValue("dc:title", inFile.getName());
        videoDoc.setPropertyValue("file:content", new FileBlob(inFile));
        videoDoc = coreSession.createDocument(videoDoc);
        if (inSaveIt) {
            videoDoc = coreSession.saveDocument(videoDoc);
            coreSession.save();
        }
        return videoDoc;
    }

    protected String fileBlobToString(FileBlob inBlob) throws IOException {

        File f = inBlob.getFile();
        Path p = Paths.get(f.getAbsolutePath());

        return new String(Files.readAllBytes(p));
    }

    @Before
    public void setUp() {

        parentOfTestDocs = coreSession.createDocumentModel("/",
                "test-video-tools", "Folder");
        parentOfTestDocs.setPropertyValue("dc:title", "test-video-tools");
        parentOfTestDocs = coreSession.createDocument(parentOfTestDocs);
        parentOfTestDocs = coreSession.saveDocument(parentOfTestDocs);

        coreSession.save();
    }

    @After
    public void cleanup() {
        coreSession.removeDocument(parentOfTestDocs.getRef());
        coreSession.save();
    }

    @Test
    public void testClosedCaptionsWithBlob() throws Exception {

        doLog(getCurrentMethodName(new RuntimeException()) + "...");

        OperationContext ctx = new OperationContext(coreSession);
        OperationChain chain;
        Blob result;
        String ccFull, ccPart;

        File f = FileUtils.getResourceFileFromContext("files/VideoLan-Example.ts");
        FileBlob fb = new FileBlob(f);

        // ==================== Default value, full CC extraction
        ctx.setInput(fb);
        chain = new OperationChain("testChain");
        chain.add(ExtractClosedCaptionsOp.ID);

        result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);

        ccFull = fileBlobToString((FileBlob) result);
        assertNotNull(ccFull);
        assertTrue(ccFull.indexOf("You should be a detective") > -1);
        

        // ==================== Extract only a part
        ctx.setInput(fb);
        chain = new OperationChain("testChain2");
        chain.add(ExtractClosedCaptionsOp.ID).set("startAt", "00:10").set("endAt", "00:20");

        result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);

        ccPart = fileBlobToString((FileBlob) result);
        assertNotNull(ccPart);
        assertNotEquals(ccFull, ccPart);
        assertTrue(ccFull.length() > ccPart.length());
        assertTrue(ccPart.indexOf("You should be a detective") > -1);

        
        doLog("done");
    }
    
    @Test
    public void testClosedCaptionsWithDoc() throws Exception {

        doLog(getCurrentMethodName(new RuntimeException()) + "...");
        
        OperationContext ctx = new OperationContext(coreSession);
        OperationChain chain;
        Blob result;
        String ccFull;
        
        File f = FileUtils.getResourceFileFromContext("files/VideoLan-Example.ts");
        DocumentModel video = createVideoDocument(f, true);

        ctx.setInput(video);
        chain = new OperationChain("testChain");
        chain.add(ExtractClosedCaptionsOp.ID);

        result = (Blob) automationService.run(ctx, chain);
        assertNotNull(result);

        ccFull = fileBlobToString((FileBlob) result);
        assertNotNull(ccFull);
        assertTrue(ccFull.indexOf("You should be a detective") > -1);

        doLog("done");
        
    }
}
