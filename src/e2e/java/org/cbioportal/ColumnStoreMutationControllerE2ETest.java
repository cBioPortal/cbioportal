package org.cbioportal;


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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
public class ColumnStoreMutationControllerE2ETest extends AbstractE2ETest{
    
    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();


    private MutationDTO[] callFetchMutationEndPoint(String testDataJson, ProjectionType projectionType)throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(testDataJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/mutations/fetch?projection=" 
                    + projectionType.name(),
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
    void testFetchMutationEndPointWithDataJson_IdProjection() throws Exception {
        // The json has two molecularProfileId and a sampleId and entrezGeneIds to restrict search  
        // Two profiles meet this criteria 

        String testDataJson = loadTestData("mutation_filter.json");
        MutationDTO[] mutationResultID = callFetchMutationEndPoint(testDataJson, ProjectionType.ID);
       
        assertNotNull(mutationResultID, "Response should have mutation DTO");
        assertEquals(2, mutationResultID.length, "Two mutations meet the search criteria of the json file");


        //Compare the fields that should be equal
        //MolecularProfile id should be identical
        assertEquals(mutationResultID[0].molecularProfileId(), mutationResultID[1].molecularProfileId(), "molecularProfile IDs should match");
        //study id should be identical; 
        assertEquals(mutationResultID[0].studyId(), mutationResultID[1].studyId(), "study IDs should match");


    }
    @Test
    void testFetchMutationEndPointWithDataJson_SummaryProjection() throws Exception {
        // The json has two molecularProfileId and a sampleId and entrezGeneIds to restrict search  
        // Two profiles meet this criteria 

        String testDataJson = loadTestData("mutation_filter.json");
        MutationDTO[] mutationResultSummary = callFetchMutationEndPoint(testDataJson,ProjectionType.SUMMARY);

        assertNotNull(mutationResultSummary, "Response should have mutation DTO");
        assertEquals(2, mutationResultSummary.length, "SUMMARY projection should not add or remove records");


        //Compare the fields that should be equal
        //MolecularProfile id should be identical
        assertEquals(mutationResultSummary[0].molecularProfileId(), mutationResultSummary[1].molecularProfileId(), "molecularProfile IDs should match");

        //study id should be identical; 
        assertEquals(mutationResultSummary[0].studyId(), mutationResultSummary[1].studyId(), "study IDs should match");

        // Testing different projection expose different fields 
        // SUMMARY projection should not have gene present or any AlleleSpecificCopyNumber. AlleleSpecificCopyNumber is null for this mutation profile
        for (MutationDTO mutationDTO : mutationResultSummary) {
            assertNull(mutationDTO.gene(), "Response should not have gene present");
            assertNull(mutationDTO.alleleSpecificCopyNumber(), "Response should not have AlleleSpecificCopyNumber present");
        }

    }

    @Test
    void testFetchMutationEndPointWithDataJson_DetailedProjection() throws Exception {
        // The json has two molecularProfileId and a sampleId and entrezGeneIds to restrict search  
        // Two profiles meet this criteria 

        String testDataJson = loadTestData("mutation_filter.json");
        MutationDTO[] mutationResultDetailed = callFetchMutationEndPoint(testDataJson,ProjectionType.DETAILED);

        assertNotNull(mutationResultDetailed, "Response should have mutation DTO");
        assertEquals(2, mutationResultDetailed.length, "DETAILED projection should not add or remove records");


        //Compare the fields that should be equal 

        //MolecularProfile id should be identical
        assertEquals(mutationResultDetailed[0].molecularProfileId(), mutationResultDetailed[1].molecularProfileId(), "molecularProfile IDs should match");
        //study id should be identical; 
        assertEquals(mutationResultDetailed[0].studyId(), mutationResultDetailed[1].studyId(), "study IDs should match");

        // Testing different projection expose different fields 
        // Detailed projection should have gene present or any AlleleSpecificCopyNumber. AlleleSpecificCopyNumber is null for this mutation profile
        for (MutationDTO mutationDTO : mutationResultDetailed) {
            assertNotNull(mutationDTO.gene(), "Response should have gene present");
            assertNull(mutationDTO.alleleSpecificCopyNumber(), "Response should not have AlleleSpecificCopyNumber present ");
        }
    }
}
