package org.cbioportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


import org.cbioportal.application.rest.response.MutationDTO;
import org.cbioportal.shared.enums.ProjectionType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ColumnStoreMutationControllerE2ETest extends AbstractE2ETest{
    
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();


    private MutationDTO[] callFetchMutationEndPoint(String testDataJson)throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(testDataJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/mutations//fetch?enrichmentType=" + ProjectionType.ID,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        return OBJECT_MAPPER.readValue(response.getBody(), MutationDTO[].class);

    }

    private String loadTestData(String filename) throws Exception {
        return new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("src/e2e/java/org/cbioportal/ColumnStoreMutationControllerE2ETest/" + filename)));
    }
    
    @Test
    void testFetchMutationEndPointWithDataJson() throws Exception {
        // The json has two molecularProfileId and a sampleId and entrezGeneIds to restrict search  
        // Two profiles meet this criteria 

        String testDataJson = loadTestData("mutation_filter.json");
        MutationDTO[] mutation = callFetchMutationEndPoint(testDataJson);
        
        assertNotNull(mutation, "Response should have mutation DTO");
        assertEquals(2, mutation.length, "Two mutations meet the search criteria of profileid: lgg_ucsf_2014_mutations");

    }
    
}
