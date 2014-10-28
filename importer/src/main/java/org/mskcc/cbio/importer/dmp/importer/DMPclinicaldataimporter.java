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

package org.mskcc.cbio.importer.dmp.importer;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import org.mskcc.cbio.importer.dmp.util.*;

public class DMPclinicaldataimporter {

    private static final String DMP_SERVER_NAME = "http://draco.mskcc.org:9770";
    private static final String DMP_CREATE_SESSION = "create_session";
    private static final String DMP_CBIO_RETRIEVE_VARIANTS = "cbio_retrieve_variants";
    private static final String DMP_CBIO_RETRIEVE_SEGMENT_DATA = "get_seg_data";
    private static final String DMP_CBIO_CONSUME_SAMPLE = "cbio_consume_sample";
    private static final String DMP_CBIO_USERNAME = "Y2Jpb19ydwo=";
    private static final String DMP_CBIO_PASSWORD = "eDM4I3hGMgo=";

    private final RestTemplate template = new RestTemplate(); //spring rest template
    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory factory = mapper.getJsonFactory();
    
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

            DMPsession _session = new DMPsession(); 
            
            //Retrieves meta data 
            ResponseEntity<String> rawResultEntity = 
                template.getForEntity(
                    DMP_SERVER_NAME + "/" + DMP_CBIO_RETRIEVE_VARIANTS + "/" + _session.getSessionId() + "/0", 
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
                    DMP_SERVER_NAME + "/" + DMP_CBIO_RETRIEVE_SEGMENT_DATA + "/" + _session.getSessionId() + "/" + sampleId, 
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
    public DMPclinicaldataimporter(ArrayList<String> sampleIds) 
        throws IOException {
        
        DMPsession _session = new DMPsession(); 
        for(String sampleId : sampleIds) {
            template.getForEntity(
                    DMP_SERVER_NAME + "/" + DMP_CBIO_CONSUME_SAMPLE + "/" + sampleId + "/" + _session.getSessionId(),
                    String.class
            );
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
                    DMP_SERVER_NAME + "/" + DMP_CREATE_SESSION + "/" + DMP_CBIO_USERNAME + "/"  + DMP_CBIO_PASSWORD + "/0", 
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
    
}