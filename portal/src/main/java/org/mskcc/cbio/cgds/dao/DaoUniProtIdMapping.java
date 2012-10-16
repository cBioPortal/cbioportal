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

package org.mskcc.cbio.cgds.dao;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Data access object for uniprot_id_mapping table.
 */
public final class DaoUniProtIdMapping {
    public static int addUniProtIdMapping(final int entrezGeneId, final String uniProtId) throws DaoException {
        checkNotNull(uniProtId);
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection();
            preparedStatement = connection.prepareStatement("insert into uniprot_id_mapping (`entrez_gene_id`,`uniprot_id`) values (?, ?)");
            preparedStatement.setInt(1, entrezGeneId);
            preparedStatement.setString(2, uniProtId);
            return preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(connection, preparedStatement, resultSet);
        }
    }

    public static List<String> getUniProtIds(final int entrezGeneId) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection();
            preparedStatement = connection.prepareStatement("select uniprot_id from uniprot_id_mapping where entrez_gene_id = ?");
            preparedStatement.setInt(1, entrezGeneId);
            resultSet = preparedStatement.executeQuery();
            List<String> uniProtIds = new ArrayList<String>();
            while (resultSet.next()) {
                uniProtIds.add(resultSet.getString(1));
            }
            return ImmutableList.copyOf(uniProtIds);
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(connection, preparedStatement, resultSet);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection();
            preparedStatement = connection.prepareStatement("truncate table uniprot_id_mapping");
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(connection, preparedStatement, resultSet);
        }
    }
}
