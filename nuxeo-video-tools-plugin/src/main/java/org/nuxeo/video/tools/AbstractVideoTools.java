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
import java.nio.file.Files;
import java.nio.file.Paths;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.picture.api.BlobHelper;
import org.nuxeo.runtime.api.Framework;

/**
 * Umbrella class with basic utilities
 *
 * @since 7.1
 */
public abstract class AbstractVideoTools {

    Blob blob;

    public AbstractVideoTools(Blob inBlob) {
        blob = inBlob;
    }

    /**
     * Return the java File holding the blob. If the blob was not backed-up by a
     * File, create a temporary one
     * 
     * @return the java File holding the blob
     * @throws IOException
     *
     * @since 7.1
     */
    public File getBlobFile() throws IOException {

        File f = null;

        try {
            f = BlobHelper.getFileFromBlob(blob);
        } finally {
            // Nothing
        }

        if (f == null) {

            File tempFile = File.createTempFile("NxVT-", "");
            blob.transferTo(tempFile);
            tempFile.deleteOnExit();
            Framework.trackFile(tempFile, this);
        }

        return f;
    }

}