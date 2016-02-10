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

import java.io.*;
import java.util.Properties;

public class MavenProperties {
    
    private static final String VERSION_LINE = "INSERT INTO info VALUES";
    private static final String RESOURCE_FILENAME =  "maven.properties";
    public static final String HOME_DIR = "PORTAL_HOME";
    public static final String DATABASE_VERSION = "db.version";
    
    private static final Properties properties = initializeMavenProperties();
    
    
    private static Properties initializeMavenProperties()
    {
        return loadProperties(getResourceStream());
    }
    
    private static InputStream getResourceStream()
    {
        String resourceFilename = null;
        InputStream is = null;
        
        is = MavenProperties.class.getClassLoader().getResourceAsStream(MavenProperties.RESOURCE_FILENAME);
  
        return is;
    }
    
    private static Properties loadProperties(InputStream resourceInputStream)
    {
        Properties properties = new Properties();

        try {
            properties.load(resourceInputStream);
            resourceInputStream.close();
        }
        catch (IOException e) {
            System.out.println("Failed to read maven properties");
        }

        return properties;
    }
    
    public static String getDbVersion()
    {
        return properties.getProperty(DATABASE_VERSION);
    }
    
    public static void main(String[] args)
    {
        System.out.println(getDbVersion());
    }
}


