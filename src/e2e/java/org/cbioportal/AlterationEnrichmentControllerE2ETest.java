package org.cbioportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.AlterationFilter;
import org.cbioportal.legacy.model.CNA;
import org.cbioportal.legacy.model.EnrichmentType;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.cbioportal.legacy.model.MutationEventType;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupAndAlterationTypeFilter;
import org.cbioportal.legacy.web.parameter.MolecularProfileCasesGroupFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class AlterationEnrichmentControllerE2ETest extends AbstractE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;
    
    @Test
    void testFetchAlterationEnrichmentsWithDataJson() throws Exception {
        String testDataJson = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("src/e2e/java/org/cbioportal/data.json")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(testDataJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=" + EnrichmentType.SAMPLE,
            HttpMethod.POST,
            requestEntity,
            String.class
        );
        
        // this combination comparison session has two studies, one WES and the other IMPACT
        // 104 samples total, 92 of which are WES.  14 samples should be profiled for only IMPACT genes
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Parse the JSON response to check SPSB1 profiled samples count
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        AlterationEnrichment[] enrichments = objectMapper.readValue(response.getBody(), AlterationEnrichment[].class);
        
        AlterationEnrichment spsb1Enrichment = Arrays.stream(enrichments)
            .filter(enrichment -> "SPSB1".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(spsb1Enrichment, "SPSB1 enrichment should be present in response");
        
        System.out.println("SPSB1 Enrichment:\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spsb1Enrichment));
        
        
        int totalProfiledSamplesForSPSB1 = spsb1Enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();
        
        assertEquals(92, totalProfiledSamplesForSPSB1, "SPSB1 should have 92 total profiled samples across all groups");
        
        // Find TP53 gene and verify total profiled samples across all groups is 104 (since TP53 is in IMPACT)
        AlterationEnrichment tp53Enrichment = Arrays.stream(enrichments)
            .filter(enrichment -> "TP53".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");
        
        int totalProfiledSamplesForTP53 = tp53Enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();
        
        assertEquals(104, totalProfiledSamplesForTP53, "TP53 should have 104 total profiled samples across all groups because it is in IMPACT");

    }


    @Test
    void testFetchAlterationEnrichmentsWithMultiPanel() throws Exception {
        String testDataJson = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("src/e2e/java/org/cbioportal/multi_panel.json")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(testDataJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=" + EnrichmentType.SAMPLE,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        // this comparison session is of 33 samples (from a single study) which are covered by 2 different panels
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Parse the JSON response to check SPSB1 profiled samples count
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        AlterationEnrichment[] enrichments = objectMapper.readValue(response.getBody(), AlterationEnrichment[].class);

        // Assert that all genes have exactly four groups
        assertTrue(Arrays.stream(enrichments).allMatch(enrichment -> enrichment.getCounts().size() == 4), 
                "All genes should have exactly 4 groups");
        
        // Assert that each gene has at least one group with an alteration
        assertTrue(Arrays.stream(enrichments).allMatch(enrichment -> 
                enrichment.getCounts().stream().anyMatch(count -> count.getAlteredCount() > 0)), 
                "Each gene should have at least one group with an alteration");

        AlterationEnrichment TP53I13Enrichment = Arrays.stream(enrichments)
            .filter(enrichment -> "TP53I13".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);

        assertNotNull(TP53I13Enrichment, "TP53I13 enrichment should be present in response");
        
        int totalProfiledSamplesForTP53I13 = TP53I13Enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();

        // of 33 samples, 26 are covered by WES panel for mutation and so only those will be profiled for 
        // genes which are not covered by panel
        assertEquals(26, totalProfiledSamplesForTP53I13, "TP53I13 should have 26 total profiled samples across all groups");

        // Find TP53 gene and verify total profiled samples across all groups is 33 (since TP53 is in the targetted panel)
        AlterationEnrichment tp53Enrichment = Arrays.stream(enrichments)
            .filter(enrichment -> "TP53".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);

        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");

        int totalProfiledSamplesForTP53 = tp53Enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();

        assertEquals(33, totalProfiledSamplesForTP53, "TP53 should have 33 total profiled samples across all groups because it is in IMPACT");
        
    }


    @Test
    void testFetchAlterationFilteringByAlterationType() throws Exception {
        String testDataJson = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("src/e2e/java/org/cbioportal/multi_panel.json")));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(testDataJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=" + EnrichmentType.SAMPLE,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Parse the JSON response
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        AlterationEnrichment[] enrichments = objectMapper.readValue(response.getBody(), AlterationEnrichment[].class);
        
        // Find TP53 gene and verify total profiled samples across all groups is 33 (since TP53 is in the targetted panel)
        AlterationEnrichment tp53Enrichment = Arrays.stream(enrichments)
            .filter(enrichment -> "TP53".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);

        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");

        int totalAlteredSamples = tp53Enrichment.getCounts().stream()
            .mapToInt(count -> count.getAlteredCount())
            .sum();

        assertEquals(24, totalAlteredSamples, "TP53 should have 24 total altered samples across all groups");
    }

    @Test
    void testFetchAlterationEnrichmentsExcludingMissenseMutations() throws Exception {
        String testDataJson = new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("src/e2e/java/org/cbioportal/multi_panel.json")));

        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        
        // First, execute without filter to get baseline
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> originalRequestEntity = new HttpEntity<>(testDataJson, headers);

        ResponseEntity<String> originalResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=" + EnrichmentType.SAMPLE,
            HttpMethod.POST,
            originalRequestEntity,
            String.class
        );

        assertEquals(HttpStatus.OK, originalResponse.getStatusCode());
        assertNotNull(originalResponse.getBody());

        // Parse the original response to get baseline altered samples count
        AlterationEnrichment[] originalEnrichments = objectMapper.readValue(originalResponse.getBody(), AlterationEnrichment[].class);
        
        AlterationEnrichment originalTp53Enrichment = Arrays.stream(originalEnrichments)
            .filter(enrichment -> "TP53".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);

        assertNotNull(originalTp53Enrichment, "TP53 enrichment should be present in original response");

        int originalTotalAlteredSamples = originalTp53Enrichment.getCounts().stream()
            .mapToInt(count -> count.getAlteredCount())
            .sum();

        // Now execute with missense mutations excluded
        com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(testDataJson);
        
        // Get the alterationEventTypes node
        com.fasterxml.jackson.databind.node.ObjectNode alterationEventTypes = 
            (com.fasterxml.jackson.databind.node.ObjectNode) rootNode.get("alterationEventTypes");
        com.fasterxml.jackson.databind.node.ObjectNode mutationEventTypes = 
            (com.fasterxml.jackson.databind.node.ObjectNode) alterationEventTypes.get("mutationEventTypes");
        
        // Set missense mutation types to false to exclude them
        mutationEventTypes.put("missense", false);
        mutationEventTypes.put("missense_mutation", false);
        mutationEventTypes.put("missense_variant", false);
        
        String modifiedTestDataJson = objectMapper.writeValueAsString(rootNode);
        HttpEntity<String> filteredRequestEntity = new HttpEntity<>(modifiedTestDataJson, headers);

        ResponseEntity<String> filteredResponse = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=" + EnrichmentType.SAMPLE,
            HttpMethod.POST,
            filteredRequestEntity,
            String.class
        );

        assertEquals(HttpStatus.OK, filteredResponse.getStatusCode());
        assertNotNull(filteredResponse.getBody());

        // Parse the filtered response
        AlterationEnrichment[] filteredEnrichments = objectMapper.readValue(filteredResponse.getBody(), AlterationEnrichment[].class);
        
        AlterationEnrichment filteredTp53Enrichment = Arrays.stream(filteredEnrichments)
            .filter(enrichment -> "TP53".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);

        assertNotNull(filteredTp53Enrichment, "TP53 enrichment should be present in filtered response");

        int filteredTotalAlteredSamples = filteredTp53Enrichment.getCounts().stream()
            .mapToInt(count -> count.getAlteredCount())
            .sum();

        // Compare the results - filtered should have fewer altered samples
        assertTrue(filteredTotalAlteredSamples < originalTotalAlteredSamples, 
            "TP53 should have fewer altered samples when missense mutations are excluded. Original: " + 
            originalTotalAlteredSamples + ", Filtered: " + filteredTotalAlteredSamples);
            
        assertEquals(12, filteredTotalAlteredSamples, 
            "TP53 should have 12 altered samples when missense mutations are excluded");

        // Assert that there are fewer genes in the filtered response
        // Some genes may have been filtered out because they have no alterations in any group after excluding missense mutations
        assertTrue(filteredEnrichments.length < originalEnrichments.length,
            "Filtered response should have fewer genes than original. Original: " + originalEnrichments.length + 
            ", Filtered: " + filteredEnrichments.length + " (genes with only missense mutations should be excluded)");
    }
    
    
    
}