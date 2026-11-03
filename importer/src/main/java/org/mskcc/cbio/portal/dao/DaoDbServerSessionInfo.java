/*
 * Copyright (c) 2026 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 * Provides database query functions which return information about the session created
 * by the database update process.
 */
public class DaoDbServerSessionInfo {

    /* Singleton pattern : all functionality accessed through a singleton object rather than
     * static class methods. This enables convenient mocking for unit testing.
     */
    private static DaoDbServerSessionInfo daoDbServerSessionInfo;

    /**
     * Gets Global Singleton Instance.
     *
     * @return DaoDbServerSessionInfo Singleton object.
     */
    public static DaoDbServerSessionInfo getInstance() {
        if (daoDbServerSessionInfo == null) {
            // create singleton
            daoDbServerSessionInfo = new DaoDbServerSessionInfo();
        }
        return daoDbServerSessionInfo;
    }

    /**
     * Private constructor to enforce singleton pattern.
     */
    private DaoDbServerSessionInfo() {
    } 

    /**
     * Make a simple query based on a provided expression. The return value must be a single String
     * @param expression : a valid SQL expression which will be evaluated to yield a single string.
     * @return a string from the database server's expression evaluation.
     * @throws DaoException
     */
    private String queryDatabaseServerForString(String expression) throws DaoException {
        Connection connection = null;
        try {
            String query = String.format("SELECT %s AS expression_result", expression);
            connection = JdbcUtil.getDbConnection(DaoDbServerSessionInfo.class);
            try (
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("expression_result");
                } else {
                    String errorMessage = String.format("unable to evaluate expression '%s' on database service", expression);
                    ProgressMonitor.setCurrentMessage(errorMessage);
                    throw new DaoException(errorMessage);
                }
            }
        } catch (SQLException e) {
            String errorMessage = String.format("unable to evaluate expression '%s' on database service", expression);
            ProgressMonitor.setCurrentMessage(errorMessage);
            throw new DaoException(errorMessage, e);
        } finally {
            JdbcUtil.closeAll(DaoDbServerSessionInfo.class, connection, null, null);
        }
    }

    /**
     * Get version of the database server
     * @return the database version (by evaluating 'version()')
     * @throws DaoException
     */
    public String getServerVersion() throws DaoException {
        return queryDatabaseServerForString("version()");
    }

    /**
     * Get the name of the target database in use
     * @return the default / in-use database name (by evaluating 'current_database()')
     * @throws DaoException
     */
    public String getDatabaseInUse() throws DaoException {
        return queryDatabaseServerForString("current_database()");
    }

    /**
     * Get the name of the database user we are connecting as
     * @return the name of the user that the update process is using to connect to the database.
     * @throws DaoException
     */
    public String getDatabaseCurrentUser() throws DaoException {
        return queryDatabaseServerForString("current_user()");
    }

    /**
     * Get the privilege grants for the current user.
     * @return a list of GRANT privilege strings for the current user. Strings may contain multiple privileges listed (comma separated) on a single line. Some privileges may be role names.
     * @throws DaoException
     */
    public List<String> getPrivilegesForCurrentUser() throws DaoException {
        Connection connection = null;
        try {
            String query = "SHOW GRANTS";
            connection = JdbcUtil.getDbConnection(DaoDbServerSessionInfo.class);
            try (
                    PreparedStatement preparedStatement = connection.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                List<String> privileges = new ArrayList<>();
                while (resultSet.next()) {
                    privileges.add(resultSet.getString("Grants"));
                }
                return privileges;
            }
        } catch (SQLException e) {
            String errorMessage = "unable to evaluate expression 'SHOW GRANTS' on database service";
            ProgressMonitor.setCurrentMessage(errorMessage);
            throw new DaoException(errorMessage, e);
        } finally {
            JdbcUtil.closeAll(DaoDbServerSessionInfo.class, connection, null, null);
        }
    }

    /**
     * Get the privilege grants for a role.
     * @return a list of GRANT privilege strings for the passed role. Strings may contain multiple privileges listed (comma separated) on a single line. Some privileges may be role names.
     * @throws DaoException
     */
    public List<String> getPrivilegesForRole(String role) throws DaoException {
        if (role == null || role.strip().length() == 0) {
            throw new DaoException("illegal empty role value");
        }
        Connection connection = null;
        try {
            String query = "SHOW GRANTS FOR ?";
            connection = JdbcUtil.getDbConnection(DaoDbServerSessionInfo.class);
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, role);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<String> privileges = new ArrayList<>();
                    while (resultSet.next()) {
                        privileges.add(resultSet.getString("Grants"));
                    }
                    return privileges;
                }
            }
        } catch (SQLException e) {
            String errorMessage = "unable to evaluate expression 'SHOW GRANTS FOR " + role + "' on database service";
            ProgressMonitor.setCurrentMessage(errorMessage);
            throw new DaoException(errorMessage, e);
        } finally {
            JdbcUtil.closeAll(DaoDbServerSessionInfo.class, connection, null, null);
        }
    }
}
