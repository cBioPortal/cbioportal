/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.dao;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;

/**
 * Data access object for uniprot_id_mapping table.
 */
public final class DaoUniProtIdMapping {
    public static int addUniProtIdMapping(final String uniprotAcc, final String uniProtId, final Integer entrezGeneId) throws DaoException {
        checkNotNull(uniProtId);
        if (!MySQLbulkLoader.isBulkLoad()) {
                throw new DaoException("You have to turn on MySQLbulkLoader in order to insert uniprot ID mapping");
            } else {

                    // use this code if bulk loading
                    // write to the temp file maintained by the MySQLbulkLoader
                    MySQLbulkLoader.getMySQLbulkLoader("uniprot_id_mapping").insertRecord(
                            uniprotAcc,
                            uniProtId,
                            entrezGeneId==null?null:entrezGeneId.toString()
                            );
                    return 1;
            }
    }
    
    public static Set<String> getAllUniprotAccessions() throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection(DaoUniProtIdMapping.class);
            preparedStatement = connection.prepareStatement("select uniprot_acc from uniprot_id_mapping");
            resultSet = preparedStatement.executeQuery();
            Set<String> uniProtAccs = new HashSet<String>();
            while (resultSet.next()) {
                uniProtAccs.add(resultSet.getString(1));
            }
            return uniProtAccs;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoUniProtIdMapping.class, connection, preparedStatement, resultSet);
        }
    }

    public static List<String> mapFromEntrezGeneIdToUniprotAccession(final int entrezGeneId) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection(DaoUniProtIdMapping.class);
            preparedStatement = connection.prepareStatement("select uniprot_acc from uniprot_id_mapping where entrez_gene_id = ?");
            preparedStatement.setInt(1, entrezGeneId);
            resultSet = preparedStatement.executeQuery();
            List<String> uniProtAccs = new ArrayList<String>();
            while (resultSet.next()) {
                uniProtAccs.add(resultSet.getString(1));
            }
            return ImmutableList.copyOf(uniProtAccs);
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoUniProtIdMapping.class, connection, preparedStatement, resultSet);
        }
    }

    public static String mapFromUniprotAccessionToUniprotId(final String uniprotAcc) throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection(DaoUniProtIdMapping.class);
            preparedStatement = connection.prepareStatement("select UNIPROT_ID from uniprot_id_mapping where UNIPROT_ACC = ?");
            preparedStatement.setString(1, uniprotAcc);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoUniProtIdMapping.class, connection, preparedStatement, resultSet);
        }
    }

    public static void deleteAllRecords() throws DaoException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = JdbcUtil.getDbConnection(DaoUniProtIdMapping.class);
            preparedStatement = connection.prepareStatement("truncate table uniprot_id_mapping");
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            throw new DaoException(e);
        }
        finally {
            JdbcUtil.closeAll(DaoUniProtIdMapping.class, connection, preparedStatement, resultSet);
        }
    }
}
