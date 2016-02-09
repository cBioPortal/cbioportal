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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.servlet.QueryBuilder;

public class DBVersionUtil {
    
    private static final String VERSION_LINE = "INSERT INTO info VALUES";
    private static final String CGDS_SQL_FILENAME = "db" + File.separator + "cgds.sql";
    public static final String HOME_DIR = "PORTAL_HOME";
    
    private static final String dbVersion = readDbVersion();
    
    
    private static String readDbVersion()
    {
        return processSqlFile(getResourceStream());
    }
    
    private static InputStream getResourceStream()
    {
        String resourceFilename = null;
        InputStream is = null;

        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                 resourceFilename =
                    home + File.separator + DBVersionUtil.CGDS_SQL_FILENAME;
                is = new FileInputStream(resourceFilename);
            }
        }
        catch (FileNotFoundException e) {
        }
        
        if (is == null) {
            is = DBVersionUtil.class.getClassLoader().getResourceAsStream(DBVersionUtil.CGDS_SQL_FILENAME);
        }
         
        return is;
    }
    
    private static String processSqlFile(InputStream is)
    {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
        String line = null;
        String version = "0";
        try
        {
            while((line = bufferedReader.readLine()) != null)
            {
                if(line.contains(DBVersionUtil.VERSION_LINE))
                {
                    version = line.substring(line.indexOf("(\"") + 2, line.indexOf("\")"));
                }
            }
        } catch (IOException e)
        {
            System.out.println("Failed to process cgds.sql");
        } 
        
        return version;
    }
    
    public static String getDbVersion()
    {
        return DBVersionUtil.dbVersion;
    }
    
    public static void main(String[] args)
    {
        System.out.println(getDbVersion());
    }
}


