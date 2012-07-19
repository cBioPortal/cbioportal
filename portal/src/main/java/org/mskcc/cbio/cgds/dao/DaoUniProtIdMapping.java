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
