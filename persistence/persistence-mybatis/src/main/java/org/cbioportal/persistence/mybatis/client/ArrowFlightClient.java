package org.cbioportal.persistence.mybatis.client;

import org.apache.arrow.flight.CallHeaders;
import org.apache.arrow.flight.FlightCallHeaders;
import org.apache.arrow.flight.HeaderCallOption;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ArrowFlightClient {
    private static String host;
    @Value("${dremio.host}")
    public void setHost(String property) { host = property; }
    
    private static int port;
    @Value("${dremio.port}")
    public void setPort(int property) { port = property; }
    
    private static String userName;
    @Value("${dremio.user}")
    public void setUserName(String property) { userName = property; }
    
    private static String userPassword;
    @Value("${dremio.password}")
    public void setPassword(String property) { userPassword = property; }
    
    private static AdhocFlightClient client = null;
    private static final BufferAllocator BUFFER_ALLOCATOR = new RootAllocator(Integer.MAX_VALUE);
    private static final Map<String, String> sessionPropertiesMap = new HashMap<>();
    private static final Log log = LogFactory.getLog(ArrowFlightClient.class);
    /**
     * Private Constructor to enforce Singleton Pattern.
     */
    private ArrowFlightClient() {
    }

    public static AdhocFlightClient getClient() throws Exception {
        if (client == null) {
            client = AdhocFlightClient.getBasicClient(BUFFER_ALLOCATOR,
                host, port,
                userName, userPassword,
                null,
                createClientProperties(sessionPropertiesMap),
                null);;
        }
        log.info("client setup successfully!" + "\n");
        // run query to test connection
        // client.runQuery("select * from cBioPortal.\"cbioportal-dremio\".\"expression-data-parquet\" limit 100", createClientProperties(sessionPropertiesMap), null, true);
        return client;
    }

    private static HeaderCallOption createClientProperties(Map<String, String> clientProperties) {
        final CallHeaders callHeaders = new FlightCallHeaders();
        clientProperties.forEach(callHeaders::insert);
        return new HeaderCallOption(callHeaders);
    }
}