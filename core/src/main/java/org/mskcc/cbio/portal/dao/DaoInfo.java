/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.dao;

import java.lang.StringBuilder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mskcc.cbio.portal.util.GlobalProperties;

public class DaoInfo {
    private static String version;

    /**
     * Set database schema version in `info` table in database.
     * @throws DaoException
     */
    public static synchronized void setVersion() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        version = "-1";
        try {
            con = JdbcUtil.getDbConnection(DaoInfo.class);
            pstmt = con.prepareStatement("SELECT * FROM info");
            rs = pstmt.executeQuery();
            if (rs.next()) {
                version = rs.getString("DB_SCHEMA_VERSION");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            JdbcUtil.closeAll(DaoInfo.class, con, pstmt, rs);
        }
    }

    /**
     * Get database schema version in `info` table in database.
     * @throws DaoException
     */
    public static String getVersion() {
        return version;
    }

    /**
     * Check database schema version in `info` table in database.
     * @throws DaoException
     */
    public static boolean checkVersion(StringBuilder logMessageBuilder) {
        setVersion();
        String expectedVersion = GlobalProperties.getDbVersion();
        String foundVersion = getVersion();
        logMessageBuilder.append(
            "Checked DB schema version: (expected: " +
            expectedVersion +
            ") (found: " +
            foundVersion +
            ")"
        );
        return foundVersion.equals(expectedVersion);
    }

    /**
     * Set gene set version in `info` table in database.
     * @throws DaoException
     */
    public static void setGenesetVersion(String genesetVersion)
        throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            // Open connection to database
            connection = JdbcUtil.getDbConnection(DaoInfo.class);

            // Prepare SQL statement
            preparedStatement =
                connection.prepareStatement(
                    "UPDATE info set GENESET_VERSION = ?"
                );

            preparedStatement.setString(1, genesetVersion);
            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(
                DaoInfo.class,
                connection,
                preparedStatement,
                resultSet
            );
        }
    }

    /**
     * Get gene set version from `info` table in database.
     * @throws DaoException
     */
    public static String getGenesetVersion() throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            // Open connection to database
            connection = JdbcUtil.getDbConnection(DaoInfo.class);

            // Prepare SQL statement
            preparedStatement =
                connection.prepareStatement("SELECT * FROM info");

            // Execute statement
            resultSet = preparedStatement.executeQuery();
            String genesetVersion = new String();

            // Extract version from result
            if (resultSet.next()) {
                genesetVersion = resultSet.getString("GENESET_VERSION");
            }
            return genesetVersion;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(
                DaoInfo.class,
                connection,
                preparedStatement,
                resultSet
            );
        }
    }

    /**
     * Clears (resets to NULL) the gene set version in `info` table in database.
     * @throws DaoException
     */
    public static void clearVersion() throws DaoException {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = JdbcUtil.getDbConnection(DaoInfo.class);

            pstmt =
                con.prepareStatement("UPDATE info set GENESET_VERSION = NULL");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(DaoInfo.class, con, pstmt, rs);
        }
    }
}
