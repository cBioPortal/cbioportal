package org.cbioportal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.EnrichmentType;
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

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

    private AlterationEnrichment[] callEnrichmentEndpoint(String testDataJson) throws Exception {
        return callEnrichmentEndpoint(testDataJson, EnrichmentType.SAMPLE);
    }

    private AlterationEnrichment[] callEnrichmentEndpoint(String testDataJson, EnrichmentType enrichmentType) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(testDataJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=" + enrichmentType,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return OBJECT_MAPPER.readValue(response.getBody(), AlterationEnrichment[].class);
    }

    private AlterationEnrichment findGeneEnrichment(AlterationEnrichment[] enrichments, String geneSymbol) {
        return Arrays.stream(enrichments)
            .filter(enrichment -> geneSymbol.equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);
    }

    private int getTotalProfiledSamples(AlterationEnrichment enrichment) {
        return enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();
    }

    private int getTotalAlteredSamples(AlterationEnrichment enrichment) {
        return enrichment.getCounts().stream()
            .mapToInt(count -> count.getAlteredCount())
            .sum();
    }

    private String loadTestData(String filename) throws Exception {
        return new String(java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("src/e2e/java/org/cbioportal/AlterationEnrichmentControllerE2ETest/" + filename)));
    }
    
    @Test
    void testFetchAlterationEnrichmentsWithDataJson() throws Exception {
        // this combination comparison session has two studies, one WES and the other IMPACT
        // 104 samples total, 92 of which are belong to WES study.  14 samples should be profiled for only IMPACT genes
        // NOTE that of 92, only 91 are profiled 
        AlterationEnrichment[] enrichments = callEnrichmentEndpoint(loadTestData("data.json"));
        
        AlterationEnrichment spsb1Enrichment = findGeneEnrichment(enrichments, "SPSB1");
        assertNotNull(spsb1Enrichment, "SPSB1 enrichment should be present in response");
        assertEquals(91, getTotalProfiledSamples(spsb1Enrichment), "SPSB1 should have 91 total profiled samples across all groups");
        
        AlterationEnrichment tp53Enrichment = findGeneEnrichment(enrichments, "TP53");
        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");
        assertEquals(104, getTotalProfiledSamples(tp53Enrichment), "TP53 should have 104 total profiled samples across all groups because it is in IMPACT");
    }

    @Test
    void testFetchAlterationEnrichmentsWithDataJson() throws Exception {
        // this combination comparison session has two studies, one WES and the other IMPACT
        // 104 samples total, 92 of which are belong to WES study.  14 samples should be profiled for only IMPACT genes
        // NOTE that of 92, only 91 are profiled 
        AlterationEnrichment[] enrichments = callEnrichmentEndpoint(loadTestData("data.json"));

        AlterationEnrichment spsb1Enrichment = findGeneEnrichment(enrichments, "SPSB1");
        assertNotNull(spsb1Enrichment, "SPSB1 enrichment should be present in response");
        assertEquals(91, getTotalProfiledSamples(spsb1Enrichment), "SPSB1 should have 91 total profiled samples across all groups");

        AlterationEnrichment tp53Enrichment = findGeneEnrichment(enrichments, "TP53");
        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");
        assertEquals(104, getTotalProfiledSamples(tp53Enrichment), "TP53 should have 104 total profiled samples across all groups because it is in IMPACT");
    }


    @Test
    void testFetchAlterationEnrichmentsWithMultiPanel() throws Exception {
        // this comparison session is of 33 samples (from a single study) which are covered by 2 different panels
        AlterationEnrichment[] enrichments = callEnrichmentEndpoint(loadTestData("multi_panel.json"));

        assertTrue(Arrays.stream(enrichments).allMatch(enrichment -> enrichment.getCounts().size() == 4), 
                "All genes should have exactly 4 groups");
        assertTrue(Arrays.stream(enrichments).allMatch(enrichment -> 
                enrichment.getCounts().stream().anyMatch(count -> count.getAlteredCount() > 0)), 
                "Each gene should have at least one group with an alteration");

        AlterationEnrichment tp53i13Enrichment = findGeneEnrichment(enrichments, "TP53I13");
        assertNotNull(tp53i13Enrichment, "TP53I13 enrichment should be present in response");
        // of 33 samples, 26 are covered by WES panel for mutation and so only those will be profiled for 
        // genes which are not covered by panel
        assertEquals(26, getTotalProfiledSamples(tp53i13Enrichment), "TP53I13 should have 26 total profiled samples across all groups");

        AlterationEnrichment tp53Enrichment = findGeneEnrichment(enrichments, "TP53");
        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");
        assertEquals(33, getTotalProfiledSamples(tp53Enrichment), "TP53 should have 33 total profiled samples across all groups because it is in IMPACT");
    }


    @Test
    void testFetchAlterationFilteringByAlterationType() throws Exception {
        AlterationEnrichment[] enrichments = callEnrichmentEndpoint(loadTestData("multi_panel.json"));
        
        AlterationEnrichment tp53Enrichment = findGeneEnrichment(enrichments, "TP53");
        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");
        assertEquals(24, getTotalAlteredSamples(tp53Enrichment), "TP53 should have 24 total altered samples across all groups");
    }

    @Test
    void testFetchAlterationEnrichmentsExcludingMissenseMutations() throws Exception {
        String testDataJson = loadTestData("multi_panel.json");
        
        // Get baseline results without filter
        AlterationEnrichment[] originalEnrichments = callEnrichmentEndpoint(testDataJson);
        AlterationEnrichment originalTp53Enrichment = findGeneEnrichment(originalEnrichments, "TP53");
        assertNotNull(originalTp53Enrichment, "TP53 enrichment should be present in original response");
        int originalTotalAlteredSamples = getTotalAlteredSamples(originalTp53Enrichment);

        // Modify JSON to exclude missense mutations
        com.fasterxml.jackson.databind.JsonNode rootNode = OBJECT_MAPPER.readTree(testDataJson);
        com.fasterxml.jackson.databind.node.ObjectNode mutationEventTypes = 
            (com.fasterxml.jackson.databind.node.ObjectNode) rootNode.get("alterationEventTypes").get("mutationEventTypes");
        mutationEventTypes.put("missense", false);
        mutationEventTypes.put("missense_mutation", false);
        mutationEventTypes.put("missense_variant", false);
        
        // Get filtered results
        AlterationEnrichment[] filteredEnrichments = callEnrichmentEndpoint(OBJECT_MAPPER.writeValueAsString(rootNode));
        AlterationEnrichment filteredTp53Enrichment = findGeneEnrichment(filteredEnrichments, "TP53");
        assertNotNull(filteredTp53Enrichment, "TP53 enrichment should be present in filtered response");
        int filteredTotalAlteredSamples = getTotalAlteredSamples(filteredTp53Enrichment);

        // Verify filtering worked
        assertTrue(filteredTotalAlteredSamples < originalTotalAlteredSamples, 
            "TP53 should have fewer altered samples when missense mutations are excluded. Original: " + 
            originalTotalAlteredSamples + ", Filtered: " + filteredTotalAlteredSamples);
        assertEquals(12, filteredTotalAlteredSamples, 
            "TP53 should have 12 altered samples when missense mutations are excluded");
        assertTrue(filteredEnrichments.length < originalEnrichments.length,
            "Filtered response should have fewer genes than original. Original: " + originalEnrichments.length + 
            ", Filtered: " + filteredEnrichments.length + " (genes with only missense mutations should be excluded)");
    }

    @Test
    void testFetchAlterationEnrichmentsPatientVSample() throws Exception {
        
        // from https://www.cbioportal.org/comparison/alterations?comparisonId=6184fd03f8f71021ce56e3ff
        
        String testDataJsonSample = loadTestData("sample.json");
        String testDataJsonPatient = loadTestData("patient.json");

        // Get F8 enrichment with SAMPLE enrichment type
        AlterationEnrichment[] sampleEnrichments = callEnrichmentEndpoint(testDataJsonSample, EnrichmentType.SAMPLE);
        AlterationEnrichment f8SampleEnrichment = findGeneEnrichment(sampleEnrichments, "F8");
        
        // Get F8 enrichment with PATIENT enrichment type
        AlterationEnrichment[] patientEnrichments = callEnrichmentEndpoint(testDataJsonPatient, EnrichmentType.PATIENT);
        AlterationEnrichment f8PatientEnrichment = findGeneEnrichment(patientEnrichments, "F8");
        
        // Both should be present
        assertNotNull(f8SampleEnrichment, "F8 enrichment should be present in SAMPLE response");
        assertNotNull(f8PatientEnrichment, "F8 enrichment should be present in PATIENT response");
        
        // Get counts for comparison
        int sampleProfiledCount = getTotalProfiledSamples(f8SampleEnrichment);
        int patientProfiledCount = getTotalProfiledSamples(f8PatientEnrichment);
        int sampleAlteredCount = getTotalAlteredSamples(f8SampleEnrichment);
        int patientAlteredCount = getTotalAlteredSamples(f8PatientEnrichment);
        
        // Verify that SAMPLE and PATIENT enrichment types produce different results
        assertEquals(sampleAlteredCount, 31);
        assertEquals(patientAlteredCount, 14);
        
        assertEquals(sampleProfiledCount,447);

        assertEquals(patientProfiledCount,100);
        

    }
    
    
}