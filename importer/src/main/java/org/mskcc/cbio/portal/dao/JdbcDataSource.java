package org.mskcc.cbio.portal.dao;

import java.time.Duration;
import java.util.*;
import org.apache.commons.dbcp2.BasicDataSource;
import org.mskcc.cbio.portal.util.DatabaseProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data source that self-initializes based on cBioPortal configuration.
 */
public class JdbcDataSource extends BasicDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcDataSource.class);
    public static final Duration VALIDATION_QUERY_TIMEOUT = Duration.ofSeconds(10);

    class RequiredPropertyInfo {
        private String key; // the key string in the .properties file
        private String value; // the value for the key set in the properties file
        private String label; // the common descriptive label for the property for the error message

        public RequiredPropertyInfo(String key, String value, String label) {
            this.key = key;
            this.value = value;
            this.label = label;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getLabel() {
            return label;
        }
    }

    public JdbcDataSource() {
        DatabaseProperties dbProperties = DatabaseProperties.getInstance();
        logUsedDeprecatedProperties(dbProperties);
        // extract required property values
        String username = dbProperties.getSpringDbUser();
        String password = dbProperties.getSpringDbPassword();
        String connectionURL = dbProperties.getSpringConnectionURL();
        String driverClassName = dbProperties.getSpringDbDriverClassName();
        // extract optional property values
        String enablePooling = dbProperties.getDbEnablePooling();
        if (stringIsNullOrBlank(enablePooling)) {
            enablePooling = "false"; // default
        }
        // validate required property values are provided
        ArrayList<RequiredPropertyInfo> requiredPropertyInfoList = new ArrayList<>();
        requiredPropertyInfoList.add(new RequiredPropertyInfo("spring.datasource.username", username, "username"));
        requiredPropertyInfoList.add(new RequiredPropertyInfo("spring.datasource.password", password, "password"));
        requiredPropertyInfoList.add(new RequiredPropertyInfo("spring.datasource.url", connectionURL, "connectionURL"));
        requiredPropertyInfoList.add(new RequiredPropertyInfo("spring.datasource.driver-class-name", driverClassName, "driver class name"));
        validateRequiredProperties(requiredPropertyInfoList);
        //  Set up poolable data source
        this.setUsername(username);
        this.setPassword(password);
        this.setUrl(connectionURL);
        this.setDriverClassName(driverClassName);
        // Disable this to avoid caching statements
        this.setPoolPreparedStatements(Boolean.valueOf(enablePooling));
        // these values are from the production cbioportal application context for a jndi data source
        this.setMaxTotal(500);
        this.setMaxIdle(30);
        this.setMaxWait(Duration.ofSeconds(10));
        // Evict idle connections after 30s of idleness, checking every 10s
        this.setMinEvictableIdle(Duration.ofSeconds(30));
        this.setDurationBetweenEvictionRuns(Duration.ofSeconds(10));
        // Test connections before borrow and while idle to catch stale connections
        this.setTestOnBorrow(true);
        this.setTestWhileIdle(true);
        // Avoid connections living so long they go stale on the server side
        this.setMaxConn(Duration.ofMinutes(30));
        // Let DBCP call Connection.isValid(timeout) instead of executing a validation
        // query. ClickHouse JDBC bounds both connection and socket I/O in isValid,
        // including a TLS handshake that can otherwise inherit the long import
        // query socket timeout.
        this.setValidationQueryTimeout(VALIDATION_QUERY_TIMEOUT);
    }

    private void logUsedDeprecatedProperties(DatabaseProperties dbProperties) {
        if (defined(dbProperties.getDbHost()) ||
                defined(dbProperties.getDbUser()) ||
                defined(dbProperties.getDbPassword()) ||
                defined(dbProperties.getDbName()) ||
                defined(dbProperties.getConnectionURL()) ||
                defined(dbProperties.getDbDriverClassName()) ||
                defined(dbProperties.getDbUseSSL())) {
            LOG.warn("\n" +
                    "----------------------------------------------------------------------------------------------------------------\n" +
                    "-- Connection warning:\n" +
                    "-- You seem to be attempting to connect to the database by setting some of these deprecated properties:\n" +
                    "---    db.host\n" +
                    "---    db.user\n" +
                    "---    db.password\n" +
                    "---    db.portal_db_name\n" +
                    "---    db.connection_string\n" +
                    "---    db.driver\n" +
                    "-- Please remove those properties and set these properties instead:\n" +
                    "---    spring.datasource.username\n" +
                    "---    spring.datasource.password\n" +
                    "---    spring.datasource.url\n" +
                    "---    spring.datasource.driver-class-name\n" +
                    "-- For examples and assistance on setting these properties see:\n" +
                    "--     https://github.com/cBioPortal/cbioportal/blob/master/src/main/resources/application.properties.EXAMPLE\n" +
                    "--     https://docs.cbioportal.org/deployment/customization/application.properties-reference/\n" +
                    "----------------------------------------------------------------------------------------------------------------\n");
        }
    }

    private void validateRequiredProperties(List<RequiredPropertyInfo> requiredPropertyInfoList) {
        boolean requiredPropertyIsMissing = false;
        for (RequiredPropertyInfo requiredPropertyInfo : requiredPropertyInfoList) {
            if (stringIsNullOrBlank(requiredPropertyInfo.getValue())) {
                LOG.error(errorMessage(requiredPropertyInfo));
                requiredPropertyIsMissing = true;
            }
        }
        if (requiredPropertyIsMissing) {
            // Throw exception. Because this occurs during the bean wiring step, this will cause the context creation to fail and the application to exit.
            throw new RuntimeException("Datasource cannot be configured because required properties are missing values. See log for details.");
        }
    }

    private String errorMessage(RequiredPropertyInfo requiredPropertyInfo) {
        return String.format(
                "No %s provided for database connection. Please provide a value for '%s' in application.properties or as a jvm command line system property.",
                requiredPropertyInfo.getLabel(),
                requiredPropertyInfo.getKey());
    }

    private boolean defined(String property) {
        return !stringIsNullOrBlank(property);
    }

    private boolean stringIsNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
