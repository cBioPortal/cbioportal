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
import java.util.*;
import org.mskcc.cbio.portal.model.GenesetHierarchyLeaf;

public class DaoGenesetHierarchyLeaf {

    private DaoGenesetHierarchyLeaf() {}

    /**
     * Add gene set hierarchy object to geneset_hierarchy_leaf table in database.
     * @throws DaoException
     */
    public static void addGenesetHierarchyLeaf(
        GenesetHierarchyLeaf genesetHierarchyLeaf
    )
        throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Open connection to database
            connection =
                JdbcUtil.getDbConnection(DaoGenesetHierarchyLeaf.class);

            // Prepare SQL statement
            preparedStatement =
                connection.prepareStatement(
                    "INSERT INTO geneset_hierarchy_leaf " +
                    "(`NODE_ID`, `GENESET_ID`) VALUES(?,?)"
                );

            // Fill in statement
            preparedStatement.setInt(1, genesetHierarchyLeaf.getNodeId());
            preparedStatement.setInt(2, genesetHierarchyLeaf.getGenesetId());

            // Execute statement
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(
                DaoGenesetHierarchyLeaf.class,
                connection,
                preparedStatement,
                resultSet
            );
        }
    }

    public static List<GenesetHierarchyLeaf> getGenesetHierarchyLeafsByGenesetId(
        int genesetId
    )
        throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // Open connection to database
            connection =
                JdbcUtil.getDbConnection(DaoGenesetHierarchyLeaf.class);

            // Prepare SQL statement
            preparedStatement =
                connection.prepareStatement(
                    "SELECT * FROM geneset_hierarchy_leaf WHERE GENESET_ID = ?"
                );
            preparedStatement.setInt(1, genesetId);

            // Execute statement
            resultSet = preparedStatement.executeQuery();

            List<GenesetHierarchyLeaf> genesetHierarchyLeafs = new ArrayList<GenesetHierarchyLeaf>();

            while (resultSet.next()) {
                GenesetHierarchyLeaf genesetHierarchyLeaf = new GenesetHierarchyLeaf();
                genesetHierarchyLeaf.setNodeId(resultSet.getInt("NODE_ID"));
                genesetHierarchyLeaf.setGenesetId(
                    resultSet.getInt("GENESET_ID")
                );
                genesetHierarchyLeafs.add(genesetHierarchyLeaf);
            }

            return genesetHierarchyLeafs;
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            JdbcUtil.closeAll(
                DaoGenesetHierarchyLeaf.class,
                connection,
                preparedStatement,
                resultSet
            );
        }
    }
}
