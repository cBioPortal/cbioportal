package org.mskcc.cbio.importer.dmp.importer;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;


import org.mskcc.cbio.importer.dmp.model.DMPsession;

public class DMPclinicaldataimporter {

    private RestTemplate template; //spring rest template
    private DMPsession session; //dmp session

    public DMPclinicaldataimporter()
        throws IOException {

            template = new RestTemplate();
            session = getSession();
            getRawResult(session.getSessionId());

    }

    private DMPsession getSession() 
        throws IOException {

        // RestTemplate restTemplate = new RestTemplate();
        // DMPsession session = restTemplate.getForObject("http://draco.mskcc.org:9770/create_session/Y2Jpb19ydwo=/eDM4I3hGMgo=/0", DMPsession.class);
        
        //Get the session info JSON object and parse to get session Id
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> entity_session = template.getForEntity("http://draco.mskcc.org:9770/create_session/Y2Jpb19ydwo=/eDM4I3hGMgo=/0", String.class);
        String session_body = entity_session.getBody();
        //MediaType contentType = entity_session.getHeaders().getContentType();
        //HttpStatus statusCode = entity_session.getStatusCode();
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getJsonFactory(); 
        JsonParser jp = factory.createJsonParser(session_body);
        JsonNode actualObj = mapper.readTree(jp);

        return new DMPsession(
                actualObj.get("session_id").asText(),
                actualObj.get("time_created").asText(),
                actualObj.get("time_expired").asText()
            );

    }

    private void getRawResult(String _sessionId) {

        ResponseEntity<String> entity_raw_result = 
                template.getForEntity("http://draco.mskcc.org:9770/cbio_retrieve_variants/" + 
                                      _sessionId + 
                                      "/0", String.class);    
        System.out.println(entity_raw_result.getBody());  

    }

}