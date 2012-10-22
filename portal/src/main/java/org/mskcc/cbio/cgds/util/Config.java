/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.util;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Reads build.properties file and makes key/value pairs accessible.
 */
public class Config {
    private static Config config = null;
    private Properties properties;

    private Config () {
        try {
           String props = "build.properties";
		   InputStream in = this.getClass().getClassLoader().getResourceAsStream(props);

            if( null == in ){
               System.err.println( "Properties file '" + props + "' could not be found by getResourceAsStream(). Check the CLASSPATH or class loader.\n" +
                     "See http://download.oracle.com/javase/1.5.0/docs/api/java/lang/Class.html#getResourceAsStream%28java.lang.String%29 re proper location of properties file.");

               System.exit(1);
            }
            properties = new Properties ();
            properties.load(in);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Config getInstance() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    /**
     * Determines if users must authenticate or not.
	 *
     * @return true or false.
     */
    public boolean usersMustAuthenticate() {
		return new Boolean(properties.getProperty("authenticate"));
    }

	/**
	 * Determines if users must be authorized to access a 
	 * particular cancer study.
	 *
	 * @return true or false.
	 */
	public boolean usersMustBeAuthorized() {
		return new Boolean(properties.getProperty("authorization"));
	}
}
