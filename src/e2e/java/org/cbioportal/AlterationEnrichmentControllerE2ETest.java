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
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=SAMPLE",
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
            "http://localhost:" + port + "/api/column-store/alteration-enrichments/fetch?enrichmentType=SAMPLE",
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

        AlterationEnrichment TP53I13Enrichment = Arrays.stream(enrichments)
            .filter(enrichment -> "TP53I13".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);

        assertNotNull(TP53I13Enrichment, "TP53I13 enrichment should be present in response");
        
        int totalProfiledSamplesForTP53I13 = TP53I13Enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();

        assertEquals(26, totalProfiledSamplesForTP53I13, "TP53I13 should have 26 total profiled samples across all groups");

        // Find TP53 gene and verify total profiled samples across all groups is 104 (since TP53 is in IMPACT)
        AlterationEnrichment tp53Enrichment = Arrays.stream(enrichments)
            .filter(enrichment -> "TP53".equals(enrichment.getHugoGeneSymbol()))
            .findFirst()
            .orElse(null);

        assertNotNull(tp53Enrichment, "TP53 enrichment should be present in response");

        int totalProfiledSamplesForTP53 = tp53Enrichment.getCounts().stream()
            .mapToInt(count -> count.getProfiledCount())
            .sum();

        assertEquals(33, totalProfiledSamplesForTP53, "TP53 should have 104 total profiled samples across all groups because it is in IMPACT");

    }

//    @Test
//    void testFetchAlterationEnrichmentsPatientLevel() {
//        MolecularProfileCaseIdentifier entity1 = new MolecularProfileCaseIdentifier();
//        entity1.setCaseId("TCGA-A1-A0SB");
//        entity1.setMolecularProfileId("acc_tcga_mutations");
//        
//        MolecularProfileCaseIdentifier entity2 = new MolecularProfileCaseIdentifier();
//        entity2.setCaseId("TCGA-A1-A0SD");
//        entity2.setMolecularProfileId("acc_tcga_mutations");
//
//        MolecularProfileCasesGroupFilter casesGroup1 = new MolecularProfileCasesGroupFilter();
//        casesGroup1.setName("group1");
//        casesGroup1.setMolecularProfileCaseIdentifiers(Arrays.asList(entity1));
//
//        MolecularProfileCasesGroupFilter casesGroup2 = new MolecularProfileCasesGroupFilter();
//        casesGroup2.setName("group2");
//        casesGroup2.setMolecularProfileCaseIdentifiers(Arrays.asList(entity2));
//
//        MolecularProfileCasesGroupAndAlterationTypeFilter filter = new MolecularProfileCasesGroupAndAlterationTypeFilter();
//        filter.setMolecularProfileCasesGroupFilter(Arrays.asList(casesGroup1, casesGroup2));
//
//        AlterationFilter alterationFilter = new AlterationFilter();
//        Map<MutationEventType, Boolean> mutationTypes = new HashMap<>();
//        mutationTypes.put(MutationEventType.missense_mutation, true);
//        
//        Map<CNA, Boolean> cnaTypes = new HashMap<>();
//        cnaTypes.put(CNA.AMP, true);
//        
//        alterationFilter.setMutationEventTypes(mutationTypes);
//        alterationFilter.setCopyNumberAlterationEventTypes(cnaTypes);
//        filter.setAlterationEventTypes(alterationFilter);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<MolecularProfileCasesGroupAndAlterationTypeFilter> requestEntity = new HttpEntity<>(filter, headers);
//
//        ResponseEntity<AlterationEnrichment[]> response = restTemplate.exchange(
//            "http://localhost:" + port + "/api/alteration-enrichments/fetch?enrichmentType=" + EnrichmentType.PATIENT,
//            HttpMethod.POST,
//            requestEntity,
//            AlterationEnrichment[].class
//        );
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        
//        List<AlterationEnrichment> enrichments = Arrays.asList(response.getBody());
//        assertTrue(enrichments.size() >= 0);
//    }
//
//    @Test
//    void testFetchAlterationEnrichmentsWithMutationsOnly() {
//        MolecularProfileCaseIdentifier entity1 = new MolecularProfileCaseIdentifier();
//        entity1.setCaseId("TCGA-A1-A0SB-01");
//        entity1.setMolecularProfileId("acc_tcga_mutations");
//        
//        MolecularProfileCaseIdentifier entity2 = new MolecularProfileCaseIdentifier();
//        entity2.setCaseId("TCGA-A1-A0SD-01");
//        entity2.setMolecularProfileId("acc_tcga_mutations");
//
//        MolecularProfileCasesGroupFilter casesGroup1 = new MolecularProfileCasesGroupFilter();
//        casesGroup1.setName("mutations_group1");
//        casesGroup1.setMolecularProfileCaseIdentifiers(Arrays.asList(entity1));
//
//        MolecularProfileCasesGroupFilter casesGroup2 = new MolecularProfileCasesGroupFilter();
//        casesGroup2.setName("mutations_group2");
//        casesGroup2.setMolecularProfileCaseIdentifiers(Arrays.asList(entity2));
//
//        MolecularProfileCasesGroupAndAlterationTypeFilter filter = new MolecularProfileCasesGroupAndAlterationTypeFilter();
//        filter.setMolecularProfileCasesGroupFilter(Arrays.asList(casesGroup1, casesGroup2));
//
//        AlterationFilter alterationFilter = new AlterationFilter();
//        Map<MutationEventType, Boolean> mutationTypes = new HashMap<>();
//        mutationTypes.put(MutationEventType.missense_mutation, true);
//        mutationTypes.put(MutationEventType.feature_truncation, true);
//        
//        Map<CNA, Boolean> cnaTypes = new HashMap<>();
//        cnaTypes.put(CNA.AMP, false);
//        cnaTypes.put(CNA.HOMDEL, false);
//        
//        alterationFilter.setMutationEventTypes(mutationTypes);
//        alterationFilter.setCopyNumberAlterationEventTypes(cnaTypes);
//        filter.setAlterationEventTypes(alterationFilter);
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<MolecularProfileCasesGroupAndAlterationTypeFilter> requestEntity = new HttpEntity<>(filter, headers);
//
//        ResponseEntity<AlterationEnrichment[]> response = restTemplate.exchange(
//            "http://localhost:" + port + "/api/alteration-enrichments/fetch?enrichmentType=" + EnrichmentType.SAMPLE,
//            HttpMethod.POST,
//            requestEntity,
//            AlterationEnrichment[].class
//        );
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        
//        List<AlterationEnrichment> enrichments = Arrays.asList(response.getBody());
//        assertTrue(enrichments.size() >= 0);
//    }
}