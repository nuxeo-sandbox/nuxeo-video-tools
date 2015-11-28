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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.video.tools.CCExtractor;
import org.nuxeo.video.tools.VideoConcatDemuxer;
import org.nuxeo.video.tools.VideoConverter;
import org.nuxeo.video.tools.VideoSlicer;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.automation.test.EmbeddedAutomationServerFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class, CoreFeature.class,
        EmbeddedAutomationServerFeature.class })
@Deploy({ "nuxeo-video-tools", "org.nuxeo.ecm.platform.commandline.executor",
        "org.nuxeo.ecm.platform.video.core",
        "org.nuxeo.ecm.platform.video.convert",
        "org.nuxeo.ecm.platform.picture.core" })
public class VideoToolsTest {

    protected static final Log log = LogFactory.getLog(VideoToolsTest.class);

    protected static final String VIDEO_WITH_CC = "files/VideoLan-Example.ts";
        
    protected static Boolean ffmpegLooksOk = null;

    @Before
    public void setUp() {
    }

    @After
    public void cleanup() {

    }
    
    public boolean ffmpegLooksOk() throws IOException {

        if(ffmpegLooksOk == null) {
            ffmpegLooksOk = false;
           
            Blob result = null;
            File f1 = FileUtils.getResourceFileFromContext("files/small.mp4");
            FileBlob fb1 = new FileBlob(f1);
            
            VideoConverter vc = new VideoConverter(fb1);
            result = vc.convert(0, "convertToWebM");
            if(result != null) {
                ffmpegLooksOk = true;
            }
        }
        
        return ffmpegLooksOk;
        
    }

    protected String fileBlobToString(FileBlob inBlob) throws IOException {

        File f = inBlob.getFile();
        Path p = Paths.get(f.getAbsolutePath());

        return new String(Files.readAllBytes(p));
    }

    @Test
    public void testExtractCC() throws Exception {
        
        Assume.assumeTrue("ccextractor is not available, skipping test", CCExtractor.ccextractorIsAvailable());

        File f = FileUtils.getResourceFileFromContext(VIDEO_WITH_CC);
        FileBlob fb = new FileBlob(f);

        CCExtractor cce = new CCExtractor(fb);
        Blob result = cce.extractCC();
        assertNotNull(result);

        // It should be a FileBlob
        assertTrue(result instanceof FileBlob);

        String cc = fileBlobToString((FileBlob) result);
        assertNotNull(cc);
        assertNotEquals("", cc);

    }

    @Test
    public void testExtractCC_sliced() throws Exception {
        
        Assume.assumeTrue("ccextractor is not available, skipping test", CCExtractor.ccextractorIsAvailable());

        File f = FileUtils.getResourceFileFromContext(VIDEO_WITH_CC);
        FileBlob fb = new FileBlob(f);

        CCExtractor cce = new CCExtractor(fb, "00:10", "00:20");
        Blob result = cce.extractCC();
        assertNotNull(result);

        // It should be a FileBlob
        assertTrue(result instanceof FileBlob);

        String cc = fileBlobToString((FileBlob) result);
        assertNotNull(cc);
        assertNotEquals("", cc);

    }

    @Test
    public void testSlice() throws Exception {
        
        Assume.assumeTrue("ffmpeg is not installed or not configured to handle mp4 and others. Cannot test testSlice", ffmpegLooksOk);

        File f1 = FileUtils.getResourceFileFromContext("files/SuggestionWidget-0000-10.mp4");
        FileBlob fb1 = new FileBlob(f1);
        
        VideoInfo vi;
        
        // Default slicer
        VideoSlicer vs = new VideoSlicer(fb1);
        Blob result = vs.slice("0", "3");
        assertNotNull(result);
        vi = VideoHelper.getVideoInfo(result);
        double dFinal = vi.getDuration();
        assertEquals(3.0, dFinal, 0.0);

        
        // slicer-by-copy
        vs.setCommandLineName("videoSlicerByCopy");
        result = vs.slice("0", "3");
        assertNotNull(result);
        vi = VideoHelper.getVideoInfo(result);
        dFinal = vi.getDuration();
        assertEquals(3.0, dFinal, 0.0);
        
    }

    @Ignore
    @Test
    public void testSliceInParts() throws Exception {
        
        Assume.assumeTrue("ffmpeg is not installed or not configured to handle mp4 and others. Cannot test testSliceInParts", ffmpegLooksOk);
        
        File f1 = FileUtils.getResourceFileFromContext("files/SuggestionWidget-TestSlice.mp4");
        FileBlob fb1 = new FileBlob(f1);
        double originalDuration = VideoHelper.getVideoInfo(fb1).getDuration();
                
        VideoSlicer vs = new VideoSlicer(fb1);
        BlobList sliced = vs.slice("3");
        assertNotNull(sliced);
        // The SuggestionWidget-TestSlice.mp4 duration is 10 seconds => we should have 4 parts
        assertEquals(4, sliced.size());
        // Check the total duration is ok
        double dFinal = 0;
        for(Blob b : sliced) {
            dFinal += VideoHelper.getVideoInfo(b).getDuration();
        }
        assertEquals(originalDuration, dFinal, 0);
        
    }

    @Test
    public void testConcatDemuxer() throws Exception {
        
        Assume.assumeTrue("ffmpeg is not installed or not configured to handle mp4 and others. Cannot test testConcatDemuxer", ffmpegLooksOk);

        File f1 = FileUtils.getResourceFileFromContext("files/SuggestionWidget-0000-10.mp4");
        File f2 = FileUtils.getResourceFileFromContext("files/SuggestionWidget-0010-08.mp4");
        File f3 = FileUtils.getResourceFileFromContext("files/SuggestionWidget-0020-09.mp4");
        File f4 = FileUtils.getResourceFileFromContext("files/SuggestionWidget-0030-10.mp4");

        FileBlob fb1 = new FileBlob(f1);
        FileBlob fb2 = new FileBlob(f2);
        FileBlob fb3 = new FileBlob(f3);
        FileBlob fb4 = new FileBlob(f4);

        VideoInfo vi;
        vi = VideoHelper.getVideoInfo(fb1);
        double d1 = vi.getDuration();
        vi = VideoHelper.getVideoInfo(fb2);
        double d2 = vi.getDuration();
        vi = VideoHelper.getVideoInfo(fb3);
        double d3 = vi.getDuration();
        vi = VideoHelper.getVideoInfo(fb4);
        double d4 = vi.getDuration();

        VideoConcatDemuxer vc = new VideoConcatDemuxer();
        vc.addBlob(fb1);
        vc.addBlob(fb2);
        vc.addBlob(fb3);
        vc.addBlob(fb4);

        Blob result = vc.concat();
        assertNotNull(result);
        vi = VideoHelper.getVideoInfo(result);
        double dFinal = vi.getDuration();

        assertEquals(dFinal, d1 + d2 + d3 + d4, 0.0);

    }

    @Test
    public void testConvert() throws Exception {
        
        Assume.assumeTrue("ffmpeg is not installed or not configured to handle mp4 and others. Cannot test testConvert", ffmpegLooksOk);

        VideoConverter vc;
        Blob result;

        File f1 = FileUtils.getResourceFileFromContext("files/SuggestionWidget-0000-10.mp4");
        FileBlob fb1 = new FileBlob(f1);

        VideoInfo vi;
        vi = VideoHelper.getVideoInfo(fb1);
        long originalH = vi.getHeight();
        double originalD = vi.getDuration();

        // ------------------------------
        vc = new VideoConverter(fb1);
        result = vc.convert(200, "convertToWebM");
        assertNotNull(result);
        assertEquals("video/webm", result.getMimeType());
        vi = VideoHelper.getVideoInfo(result);
        assertEquals(200, vi.getHeight());
        assertEquals(originalD, vi.getDuration(), 0.0);

        // ------------------------------
        vc = new VideoConverter(fb1);
        result = vc.convert(0, "convertToWebM");
        assertNotNull(result);
        assertEquals("video/webm", result.getMimeType());
        vi = VideoHelper.getVideoInfo(result);
        assertEquals(originalH, vi.getHeight());
        assertEquals(originalD, vi.getDuration(), 0.0);

        // ------------------------------
        vc = new VideoConverter(fb1);
        result = vc.convert(0.5, "convertToWebM");
        assertNotNull(result);
        assertEquals("video/webm", result.getMimeType());
        vi = VideoHelper.getVideoInfo(result);
        long expectedHeight = (long) (originalH * 0.5);
        assertEquals(expectedHeight, vi.getHeight());
        assertEquals(originalD, vi.getDuration(), 0.0);

        // ------------------------------
        // Just reduce size
        vc = new VideoConverter(fb1);
        result = vc.convert(200, "convertToMP4");
        assertNotNull(result);
        assertEquals("video/mp4", result.getMimeType());
        vi = VideoHelper.getVideoInfo(result);
        assertEquals(200, vi.getHeight());
        assertEquals(originalD, vi.getDuration(), 0.0);

        // ------------------------------
        vc = new VideoConverter(fb1);
        result = vc.convert(200, "convertToAVI");
        assertNotNull(result);
        assertEquals("video/x-msvideo", result.getMimeType());
        vi = VideoHelper.getVideoInfo(result);
        assertEquals(200, vi.getHeight());
        assertEquals(originalD, vi.getDuration(), 0.0);

    }
    
    @Ignore("Conversion with AVI not working on test machine (2015-11-26)")
    @Test
    public void testConvertAVI() throws Exception {
        
        Assume.assumeTrue("ffmpeg is not installed or not configured to handle mp4 and others. Cannot test testConvertAVI", ffmpegLooksOk);
          
        File f = FileUtils.getResourceFileFromContext("files/Test-AC3-v2.0.avi");
        FileBlob fb = new FileBlob(f);
        
        VideoInfo vi = VideoHelper.getVideoInfo(fb);
        double originalD = vi.getDuration();
        
        VideoConverter vc = new VideoConverter(fb);
        Blob result = vc.convert(200, "convertToMP4");
        
        assertNotNull(result);
        assertEquals("video/mp4", result.getMimeType());
        vi = VideoHelper.getVideoInfo(result);
        assertEquals(200, vi.getHeight());
        // Duration may not be the exact same in this conversion
        assertEquals(originalD, vi.getDuration(), 0.3);
        
    }
}
