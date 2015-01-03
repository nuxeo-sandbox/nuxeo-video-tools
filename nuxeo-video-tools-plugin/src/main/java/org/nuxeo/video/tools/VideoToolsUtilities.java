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
import java.io.IOException;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.picture.api.BlobHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Misc. utility methods
 * 
 *
 * @since 7.1
 */
public class VideoToolsUtilities {

    /**
     * Return the java File holding the blob. If the blob was not backed-up by a
     * File, create a temporary one
     * 
     * @return the java File holding the blob
     * @throws IOException
     *
     * @since 7.1
     */
    public static File getBlobFile(Blob inBlob) throws IOException {

        File f = null;

        try {
            f = BlobHelper.getFileFromBlob(inBlob);
        } finally {
            // Nothing
        }

        if (f == null) {

            File tempFile = File.createTempFile("NxVTu-", "");
            inBlob.transferTo(tempFile);
            tempFile.deleteOnExit();
            Framework.trackFile(tempFile, inBlob);
        }

        return f;
    }
    
    /**
     * Build a filename, inserting the suffix between the file extension.
     * <p>
     * If the fileName has no extension, the suffix is just added.
     * <p>
     * (if the suffix is null or empty, nothing happens)
     * 
     * @param inFileName
     * @param inSuffix
     * @return
     *
     * @since 7.1
     */
    public static String addSuffixToFileName(String inFileName, String inSuffix) {
        if (inFileName == null || inFileName.isEmpty() || inSuffix == null
                || inSuffix.isEmpty()) {
            return inFileName;
        }

        int dotIndex = inFileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return inFileName + inSuffix;
        }

        return inFileName.substring(0, dotIndex) + inSuffix
                + inFileName.substring(dotIndex);
    }
}
