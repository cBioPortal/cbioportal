/*
 * Copyright (c) 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 * The importer refuses to load data unless the database's schema version equals the version it
 * expects. That expected version must be db.version from the root pom.xml, the one the web app
 * checks too. When the importer became a module of the cbioportal build it silently fell back to
 * "0", so every import aborted; this keeps that from happening again.
 */
public class TestGlobalPropertiesDbVersion {

    private static final Pattern DB_VERSION =
            Pattern.compile("<db\\.version>\\s*([^<]+?)\\s*</db\\.version>");

    @Test
    public void expectedDbVersionIsTheRootPomVersion() throws Exception {
        assertNull("a db.version system property would bypass the check",
                System.getProperty(GlobalProperties.DB_VERSION));

        // The importer's tests run with importer/ as their working directory.
        String rootPom = Files.readString(Path.of("..", "pom.xml"), StandardCharsets.UTF_8);
        Matcher matcher = DB_VERSION.matcher(rootPom);
        assertTrue("db.version not found in the root pom.xml", matcher.find());

        assertEquals(matcher.group(1), GlobalProperties.getDbVersion());
    }
}
