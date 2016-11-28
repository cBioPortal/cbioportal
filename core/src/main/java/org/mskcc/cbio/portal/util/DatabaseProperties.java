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

//import org.mskcc.cbio.portal.util.GlobalProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Stores db props (name, id, pw, host) from portal.properties
 * and makes them accessible.
 */
@Configuration
public class DatabaseProperties {    
    @Value("${db.host}")
    private String dbHost;
    
    @Value("${db.user}")
    private String dbUser;
    
    @Value("${db.password}")
    private String dbPassword;
    
    @Value("${db.portal_db_name}")
    private String dbName;
    
    @Value("${db.encryptedKey}")
    private String dbEncryptedKey;
    
    @Value("${db.driver}")
    private String dbDriverClassName;

    // No production keys stored in filesystem or code: digest the key; put it in properties; load it into dbms on startup
    private static DatabaseProperties dbProperties;
    @Autowired
    private void setDatabaseProperties() {
        dbProperties.setDbHost(dbHost);
        dbProperties.setDbName(dbName);
        dbProperties.setDbUser(dbUser);
        dbProperties.setDbUser(dbUser);
        dbProperties.setDbPassword(dbPassword);
        dbProperties.setDbEncryptedKey(dbEncryptedKey);
        dbProperties.setDbDriverClassName(dbDriverClassName);
    }

    public static DatabaseProperties getInstance() {
//        if (dbProperties == null) {
//            dbProperties = new DatabaseProperties();
//            //  Get DB Properties from portal.properties.
//            dbProperties.setDbHost(GlobalProperties.getProperty("db.host"));
//            dbProperties.setDbName(GlobalProperties.getProperty("db.portal_db_name"));
//            dbProperties.setDbUser(GlobalProperties.getProperty("db.user"));
//            dbProperties.setDbPassword(GlobalProperties.getProperty("db.password"));
//            dbProperties.setDbEncryptedKey(GlobalProperties.getProperty("db.encryptedKey"));
//            dbProperties.setDbDriverClassName(GlobalProperties.getProperty("db.driver"));
//        }
        return dbProperties;
    }

    public String getDbEncryptedKey() {
      return dbEncryptedKey;
   }

   public void setDbEncryptedKey(String dbEncryptedKey) {
      this.dbEncryptedKey = dbEncryptedKey;
   }

   private DatabaseProperties() {
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbDriverClassName() {
        return dbDriverClassName;
    }

    public void setDbDriverClassName(String dbDriverClassName) {
        this.dbDriverClassName = dbDriverClassName;
    }
}
