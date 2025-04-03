package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.CancerStudyMetadata;
import org.cbioportal.application.file.model.ClinicalAttributesMetadata;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class ToMetadataKeyValuesTest {

    @Test
    public void testCancerStudyMetadataMetadata() {
        var cancerStudyMetadata = new CancerStudyMetadata();
        cancerStudyMetadata.setCancerStudyIdentifier("study_id1");
        cancerStudyMetadata.setName("study name");
        cancerStudyMetadata.setAddGlobalCaseList(true);
        cancerStudyMetadata.setDescription("study description");
        cancerStudyMetadata.setCitation("Citation");
        cancerStudyMetadata.setPmid("1234");
        cancerStudyMetadata.setGroups("GROUP1;GROUP2");
        cancerStudyMetadata.setReferenceGenome("hg38");
        cancerStudyMetadata.setTypeOfCancer("toc");

        var expectedMetadata = new LinkedHashMap<String, String>();
        expectedMetadata.put("cancer_study_identifier", "study_id1");
        expectedMetadata.put("type_of_cancer", "toc");
        expectedMetadata.put("name", "study name");
        expectedMetadata.put("description", "study description");
        expectedMetadata.put("citation", "Citation");
        expectedMetadata.put("pmid", "1234");
        expectedMetadata.put("groups", "GROUP1;GROUP2");
        expectedMetadata.put("add_global_case_list", "true");
        expectedMetadata.put("reference_genome", "hg38");
        assertEquals(expectedMetadata, cancerStudyMetadata.toMetadataKeyValues());
    }

    /**
     * Test for the CancerStudyMetadata class when all fields are null.
     * nulls are still included in the metadata map for specific metadata keys.
     */
    @Test
    public void testCancerStudyMetadataMetadataNulls() {
        var cancerStudyMetadata = new CancerStudyMetadata();

        var expectedMetadata = new LinkedHashMap<String, String>();
        expectedMetadata.put("cancer_study_identifier", null);
        expectedMetadata.put("type_of_cancer", null);
        expectedMetadata.put("name", null);
        expectedMetadata.put("description", null);
        expectedMetadata.put("citation", null);
        expectedMetadata.put("pmid", null);
        expectedMetadata.put("groups", null);
        expectedMetadata.put("add_global_case_list", null);
        expectedMetadata.put("reference_genome", null);
        assertEquals(expectedMetadata, cancerStudyMetadata.toMetadataKeyValues());
    }

    @Test
    public void testClinicalAttributesMetadata() {
        var clinicalAttributesMetadata = new ClinicalAttributesMetadata();
        clinicalAttributesMetadata.setCancerStudyIdentifier("study_id1");
        clinicalAttributesMetadata.setGeneticAlterationType("CLINICAL");
        clinicalAttributesMetadata.setDatatype("SAMPLE_ATTRIBUTES");

        var expectedMetadata = new LinkedHashMap<String, String>();
        expectedMetadata.put("cancer_study_identifier", "study_id1");
        expectedMetadata.put("genetic_alteration_type", "CLINICAL");
        expectedMetadata.put("datatype", "SAMPLE_ATTRIBUTES");

        assertEquals(expectedMetadata, clinicalAttributesMetadata.toMetadataKeyValues());
    }

    @Test
    public void testClinicalAttributesMetadataNulls() {
        var clinicalAttributesMetadata = new ClinicalAttributesMetadata();

        var expectedMetadata = new LinkedHashMap<String, String>();
        expectedMetadata.put("cancer_study_identifier", null);
        expectedMetadata.put("genetic_alteration_type", null);
        expectedMetadata.put("datatype", null);

        assertEquals(expectedMetadata, clinicalAttributesMetadata.toMetadataKeyValues());
    }

    @Test
    public void testGeneticProfileDatatypeMetadata() {
        var geneticProfileMetadata = new GeneticProfileDatatypeMetadata();
        geneticProfileMetadata.setGeneticAlterationType("MUTATION_EXTENDED");
        geneticProfileMetadata.setDatatype("MAF");
        geneticProfileMetadata.setStableId("mutations");
        geneticProfileMetadata.setShowProfileInAnalysisTab(true);
        geneticProfileMetadata.setProfileName("profile name");
        geneticProfileMetadata.setProfileDescription("profile description");
        geneticProfileMetadata.setGenePanel("gene_panel");
        geneticProfileMetadata.setPivotThreshold(1.5f);
        geneticProfileMetadata.setPatientLevel(false);
        geneticProfileMetadata.setGenericAssayType("genericAssayType");
        geneticProfileMetadata.setCancerStudyIdentifier("study_id1");
        geneticProfileMetadata.setSortOrder("ASC");

        var expectedMetadata = new LinkedHashMap<String, String>();
        expectedMetadata.put("cancer_study_identifier", "study_id1");
        expectedMetadata.put("genetic_alteration_type", "MUTATION_EXTENDED");
        expectedMetadata.put("datatype", "MAF");
        expectedMetadata.put("stable_id", "mutations");
        expectedMetadata.put("show_profile_in_analysis_tab", "true");
        expectedMetadata.put("profile_name", "profile name");
        expectedMetadata.put("profile_description", "profile description");
        expectedMetadata.put("gene_panel", "gene_panel");
        expectedMetadata.put("pivot_threshold_value", "1.5");
        expectedMetadata.put("value_sort_order", "ASC");
        expectedMetadata.put("patient_level", "false");
        expectedMetadata.put("generic_assay_type", "genericAssayType");

        assertEquals(expectedMetadata, geneticProfileMetadata.toMetadataKeyValues());
    }

    @Test
    public void testGeneticProfileDatatypeMetadataNulls() {
        var geneticProfileMetadata = new GeneticProfileDatatypeMetadata();

        var expectedMetadata = new LinkedHashMap<String, String>();
        expectedMetadata.put("cancer_study_identifier", null);
        expectedMetadata.put("genetic_alteration_type", null);
        expectedMetadata.put("datatype", null);
        expectedMetadata.put("stable_id", null);
        expectedMetadata.put("show_profile_in_analysis_tab", null);
        expectedMetadata.put("profile_name", null);
        expectedMetadata.put("profile_description", null);
        expectedMetadata.put("gene_panel", null);
        expectedMetadata.put("pivot_threshold_value", null);
        expectedMetadata.put("value_sort_order", null);
        expectedMetadata.put("patient_level", null);
        expectedMetadata.put("generic_assay_type", null);

        assertEquals(expectedMetadata, geneticProfileMetadata.toMetadataKeyValues());
    }
}
