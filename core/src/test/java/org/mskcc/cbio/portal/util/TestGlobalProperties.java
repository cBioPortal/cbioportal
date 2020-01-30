/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoUser;
import org.mskcc.cbio.portal.model.User;

/**
 * JUnit test for DaoUser class.
 */

public class TestGlobalProperties {
    public final String DB_VERSION = "db.version";
    public final String PROPERTIES_FILENAME = "maven.properties";

    @Test
    public void testVersionsMatch() throws Exception {
        InputStream is =
            TestGlobalProperties.class.getClassLoader()
                .getResourceAsStream(PROPERTIES_FILENAME);
        Properties properties = loadProperties(is);
        assertNotNull(properties.getProperty(DB_VERSION));
    }

    private static Properties loadProperties(InputStream is) {
        Properties properties = new Properties();
        try {
            properties.load(is);
            is.close();
        } catch (IOException e) {}
        return properties;
    }
}
