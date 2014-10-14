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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import org.mskcc.cbio.importer.dmp.util.*;

public class DMPclinicaldataimporter {

    //Tokens for webservice
    private static final String DMP_SERVER_NAME = "http://draco.mskcc.org:9770";
    private static final String DMP_CREATE_SESSION = "create_session";
    private static final String DMP_CBIO_RETRIEVE_VARIANTS = "cbio_retrieve_variants";

    //Spring Rest Template for calling webservice
    private final RestTemplate template; //spring rest template
    
    //Jackson JSON mapper instance
    private final ObjectMapper mapper;
    private final JsonFactory factory;

    private final DMPsession session; //dmp session
    private final String sample_result_json_str; //sample result - includes everything; format - json string

    public DMPclinicaldataimporter()
        throws IOException {

            //init
            template = new RestTemplate();
            mapper = new ObjectMapper();
            factory = mapper.getJsonFactory();
            session = initSession(); 

            //Adjust the structure and order of raw result to fit in json2pojo library
            sample_result_json_str = JSONconverters.convertRaw(getRawResult(session.getSessionId()));
            //TODO: manually attach the cnv_introgenic_variants, therefore we can generate a complete template
            //System.out.println(JSONconverters.attachMissingValues(sample_result_json_str));
            
    }

    private DMPsession initSession() 
        throws IOException {
        
        //Get the session info JSON object and parse to get session Id
        ResponseEntity<String> entity_session = 
                template.getForEntity(
                    DMP_SERVER_NAME + "/" + DMP_CREATE_SESSION + "/" + "Y2Jpb19ydwo=/eDM4I3hGMgo=/0", 
                    String.class
                );
        JsonParser jp = factory.createJsonParser(entity_session.getBody());
        JsonNode actualObj = mapper.readTree(jp);
        
        return new DMPsession(
                actualObj.get("session_id").asText(),
                actualObj.get("time_created").asText(),
                actualObj.get("time_expired").asText()
            );
    }

    private String getRawResult(String _sessionId) 
        throws IOException {
        
        ResponseEntity<String> raw_result_entity = 
                template.getForEntity(
                    DMP_SERVER_NAME + "/" + DMP_CBIO_RETRIEVE_VARIANTS + "/" + _sessionId + "/0", 
                    String.class
                );    
        
        return raw_result_entity.getBody();
    }
    
    private class DMPsession {

            private String SESSION_ID;
            private String TIME_CREATED;
            private String TIME_EXPIRED;

            public DMPsession(String _session_id, String _time_created, String _time_expired) {
                SESSION_ID = _session_id;
                TIME_CREATED = _time_created;
                TIME_EXPIRED = _time_expired;
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
        return sample_result_json_str;
    }
    
}