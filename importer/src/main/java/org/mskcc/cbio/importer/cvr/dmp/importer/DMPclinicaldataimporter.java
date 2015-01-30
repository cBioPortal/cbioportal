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

package org.mskcc.cbio.importer.cvr.dmp.importer;


import org.mskcc.cbio.importer.cvr.dmp.util.JSONconverters;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import org.mskcc.cbio.importer.fetcher.internal.DmpDarwinFetcherImpl;
import org.mskcc.cbio.portal.util.GlobalProperties;

public class DMPclinicaldataimporter {
    
    private static final String HOME_DIR = "PORTAL_HOME";
    private static final String PORTAL_PROPERTIES_FILENAME = "importer.properties";

    private static final String DMP_SERVER_NAME = "dmp.server_name";
    private static final String DMP_CBIO_USERNAME = "dmp.user_name";
    private static final String DMP_CBIO_PASSWORD = "dmp.password";
    private static final String DMP_CREATE_SESSION = "dmp.tokens.create_session";
    private static final String DMP_CBIO_RETRIEVE_VARIANTS = "dmp.tokens.retrieve_variants";
    private static final String DMP_CBIO_RETRIEVE_SEGMENT_DATA = "dmp.tokens.retrieve_segment_data";
    private static final String DMP_CBIO_CONSUME_SAMPLE = "dmp.tokens.consume_sample";

    private static Properties properties = new Properties();
    private final RestTemplate template = new RestTemplate(); //spring rest template
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = mapper.getJsonFactory();
    private static final Log LOG = LogFactory.getLog(DmpDarwinFetcherImpl.class);
    
    private String resultJsonStr = ""; //sample result - includes everything; format - json string

    /**
     * 
     * Create instance to retrieve sample meta data and segment data
     * 
     * @throws IOException
     * 
     */
    public DMPclinicaldataimporter()
        throws IOException {
        
        properties = loadProperties(getResourcesStream());
        DMPsession _session = new DMPsession(); 

        //Retrieves meta data 
        ResponseEntity<String> rawResultEntity = 
            template.getForEntity(
                properties.getProperty(DMP_SERVER_NAME) + 
                properties.getProperty(DMP_CBIO_RETRIEVE_VARIANTS) + "/" + _session.getSessionId() + "/0", 
                String.class
            ); 
        String rawResultJsonStr = rawResultEntity.getBody();
        resultJsonStr = 
                JSONconverters.convertRaw(rawResultJsonStr); //Adjust the structure and order of raw result to fit in json2pojo library

        //Retrieves segment data
        JsonParser jp = factory.createJsonParser(rawResultEntity.getBody());
        JsonNode rawResultObj = mapper.readTree(jp);
        Iterator<String> sampleIdsItr = rawResultObj.get("results").getFieldNames();

        while(sampleIdsItr.hasNext()) {
            String sampleId = sampleIdsItr.next();
            ResponseEntity<String> rawSegDataResultEntity = 
            template.getForEntity(
                properties.getProperty(DMP_SERVER_NAME) + 
                properties.getProperty(DMP_CBIO_RETRIEVE_SEGMENT_DATA) + "/" + _session.getSessionId() + "/" + sampleId, 
                String.class
            ); 
            String _sampleSegmentDataJsonStr = JSONconverters.convertSegDataJson(rawSegDataResultEntity.getBody());
            resultJsonStr = JSONconverters.mergeSampleSegmentData(resultJsonStr, _sampleSegmentDataJsonStr); //Map segment data to sample meta data
        }

    }

    /**
     * 
     * Create instance to flag consumed samples
     * 
     * @param sampleIds an array list of id of samples which are consumed 
     * @throws IOException
     * 
     */
    public DMPclinicaldataimporter(List<String> sampleIds) 
        throws IOException {
        
        properties = loadProperties(getResourcesStream());
        DMPsession _session = new DMPsession(); 
        
        for(String sampleId : sampleIds) {
            ResponseEntity<String> rawConsumedMarkingResultEntity = 
                template.getForEntity(
                    properties.getProperty(DMP_SERVER_NAME) + "/" + 
                    properties.getProperty(DMP_CBIO_CONSUME_SAMPLE) + "/" + _session.getSessionId() + "/" + sampleId,
                    String.class
                );
            JsonParser jp = factory.createJsonParser(rawConsumedMarkingResultEntity.getBody());
            JsonNode rawConsumedMarkingResultObj = mapper.readTree(jp);
            if (!rawConsumedMarkingResultObj.findPath("error").isMissingNode()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(sampleId + " got error: " + rawConsumedMarkingResultObj.get("error").asText());
                }
            } else if (rawConsumedMarkingResultObj.get("affectedRows").asText().equals("1")) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(sampleId + " successfully updated. ");
                }
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("non sense feedback from DMP. ");
                }
            }
        }

    }
    
    private class DMPsession {

        private final String SESSION_ID;
        private final String TIME_CREATED;
        private final String TIME_EXPIRED;
        
        public DMPsession() 
            throws IOException{
            
            ResponseEntity<String> entitySession = 
                template.getForEntity(
                    properties.getProperty(DMP_SERVER_NAME) + "/" + 
                    properties.getProperty(DMP_CREATE_SESSION) + "/" + 
                    properties.getProperty(DMP_CBIO_USERNAME) + "/"  + 
                    properties.getProperty(DMP_CBIO_PASSWORD) + "/0", 
                    String.class
                );
            JsonParser jp = factory.createJsonParser(entitySession.getBody());
            JsonNode sessionObj = mapper.readTree(jp);
        
            this.SESSION_ID = sessionObj.get("session_id").asText();
            this.TIME_CREATED = sessionObj.get("time_created").asText();
            this.TIME_EXPIRED = sessionObj.get("time_expired").asText();
            
        }

        public String getSessionId() {
            return SESSION_ID;
        }
        
        public String getTimeCreated() {
            return TIME_CREATED;
        }
        
        public String getTimeExpired() {
            return TIME_EXPIRED;
        }

    }

    public String getResult() { 
        return resultJsonStr;
    }
    
    private InputStream getResourcesStream() {
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                 resourceFilename =
                    home + File.separator + PORTAL_PROPERTIES_FILENAME;
                if (LOG.isInfoEnabled()) {
                    LOG.info("Attempting to read properties file: " + resourceFilename);
                }
                resourceFIS = new FileInputStream(resourceFilename);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Successfully read properties file");
                }
            }
        }
        catch (FileNotFoundException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to read properties file: " + resourceFilename);
            }
        }

        if (resourceFIS == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Attempting to read properties file from classpath");
            }
            resourceFIS = GlobalProperties.class.getClassLoader().
                getResourceAsStream(PORTAL_PROPERTIES_FILENAME);
            if (LOG.isInfoEnabled()) {
                LOG.info("Successfully read properties file");
            }
        }
        
        return resourceFIS;
    }
    
    private static Properties loadProperties(InputStream resourceInputStream) {

        Properties _properties = new Properties();
        try {
            _properties.load(resourceInputStream);
            resourceInputStream.close();
        }
        catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error loading properties file: " + e.getMessage());
            }
        }

        return _properties;
    }
    
}