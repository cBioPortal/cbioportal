/*
 * Copyright (c) 2017 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

/*
 * @author Sander Tan
 */

package org.mskcc.cbio.portal.dao;

import java.sql.*;
import org.mskcc.cbio.portal.model.*;

public class DaoGeneticProfileLink {

    private DaoGeneticProfileLink() {}

    /**
     * Set genetic profile link in `genetic_profile_link` table in database.
     * @throws DaoException
     */
    public static void addGeneticProfileLink(
        GeneticProfileLink geneticProfileLink
    )
        throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Open connection to database
            connection = JdbcUtil.getDbConnection(DaoGeneticProfileLink.class);

            // Prepare SQL statement
            preparedStatement =
                connection.prepareStatement(
                    "INSERT INTO genetic_profile_link " +
                    "(REFERRING_GENETIC_PROFILE_ID, REFERRED_GENETIC_PROFILE_ID, REFERENCE_TYPE) VALUES(?,?,?)"
                );

            // Fill in statement
            preparedStatement.setInt(
                1,
                geneticProfileLink.getReferringGeneticProfileId()
            );
            preparedStatement.setInt(
                2,
                geneticProfileLink.getReferredGeneticProfileId()
            );
            preparedStatement.setString(
                3,
                geneticProfileLink.getReferenceType()
            );

            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(
                DaoGeneticProfileLink.class,
                connection,
                preparedStatement,
                resultSet
            );
        }
    }
}
