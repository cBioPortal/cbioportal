//web services -- actual pulling

package org.mskcc.cbio.importer.dmp.importer;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;

import org.mskcc.cbio.importer.dmp.model.DMPsession;

public class DMPclinicaldataimporter {

    public DMPclinicaldataimporter() {
        
        RestTemplate restTemplate = new RestTemplate();
//        DMPsession session = restTemplate.getForObject("http://draco.mskcc.org:9770/create_session/Y2Jpb19ydwo=/eDM4I3hGMgo=/0", DMPsession.class);
        DMPsession session = restTemplate.getForObject("http://graph.facebook.com/pivotalsoftware", DMPsession.class);
        
        // System.out.println("session_id: " + session.getSessionId());
        // System.out.println("time_created: " + session.getTimeCreated());
        // System.out.println("time_expired: " + session.getTimeExpired());
        System.out.println("Name: " + session.getName());
        System.out.println("About:   " + session.getAbout());
        System.out.println("Phone:   " + session.getPhone());
        System.out.println("Website: " + session.getWebsite());
        System.out.println("can_post: " + session.getCanPost());
        System.out.println("description: " + session.getDescription());


   //  	RestTemplate template = new RestTemplate();
   //      ResponseEntity<String> entity = template.getForEntity("http://draco.mskcc.org:9770/create_session/Y2Jpb19ydwo=/eDM4I3hGMgo=/0", String.class);
 		// String body = entity.getBody();
 		// MediaType contentType = entity.getHeaders().getContentType();
 		// HttpStatus statusCode = entity.getStatusCode();
 		// System.out.println("body: " + body);
 		// System.out.println("content type: " + contentType);
 		// System.out.println("status code: " + statusCode);

    }

}