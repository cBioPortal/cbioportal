package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataEnrichment;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalAttributeService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.Group;
import org.cbioportal.web.parameter.GroupFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.util.ClinicalDataEnrichmentUtil;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ClinicalDataEnrichmentController.class, TestConfig.class})
public class ClinicalDataEnrichmentControllerTest {

    public static final String STUDY_ID1 = "study_id1";
    public static final String STUDY_ID2 = "study_id2";
    public static final String SAMPLE_ID1 = "sample_id1";
    public static final String SAMPLE_ID2 = "sample_id2";
    public static final String SAMPLE_ID3 = "sample_id3";
    public static final String SAMPLE_ID4 = "sample_id4";
    public static final String SAMPLE_ID5 = "sample_id5";
    public static final String PATIENT_ID1 = "patient_id1";
    public static final String PATIENT_ID2 = "patient_id2";
    public static final String PATIENT_ID3 = "patient_id3";
    public static final String PATIENT_ID4 = "patient_id4";
    public static final String PATIENT_ID5 = "patient_id5";

    public static final String CLINICAL_ATTRIBUTE_ID_1 = "attribute_id1";
    public static final String CLINICAL_ATTRIBUTE_ID_2 = "attribute_id2";
    public static final String CLINICAL_ATTRIBUTE_ID_3 = "attribute_id3";
    public static final String CLINICAL_ATTRIBUTE_ID_4 = "attribute_id4";

    @MockBean
    private ClinicalDataEnrichmentUtil clinicalDataEnrichmentUtil;

    @MockBean
    private ClinicalAttributeService clinicalAttributeService;

    @MockBean
    private SampleService sampleService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    public void fetchClinicalDataCounts() throws Exception {

        SampleIdentifier sampleIdentifier1 = new SampleIdentifier();
        sampleIdentifier1.setSampleId(SAMPLE_ID1);
        sampleIdentifier1.setStudyId(STUDY_ID1);
        SampleIdentifier sampleIdentifier2 = new SampleIdentifier();
        sampleIdentifier2.setSampleId(SAMPLE_ID2);
        sampleIdentifier2.setStudyId(STUDY_ID1);
        SampleIdentifier sampleIdentifier3 = new SampleIdentifier();
        sampleIdentifier3.setSampleId(SAMPLE_ID3);
        sampleIdentifier3.setStudyId(STUDY_ID2);
        SampleIdentifier sampleIdentifier4 = new SampleIdentifier();
        sampleIdentifier4.setSampleId(SAMPLE_ID4);
        sampleIdentifier4.setStudyId(STUDY_ID2);
        SampleIdentifier sampleIdentifier5 = new SampleIdentifier();
        sampleIdentifier5.setSampleId(SAMPLE_ID5);
        sampleIdentifier5.setStudyId(STUDY_ID2);

        Sample sample1 = new Sample();
        sample1.setStableId(SAMPLE_ID1);
        sample1.setPatientStableId(PATIENT_ID1);
        sample1.setCancerStudyIdentifier(STUDY_ID1);

        Sample sample2 = new Sample();
        sample2.setStableId(SAMPLE_ID2);
        sample2.setPatientStableId(PATIENT_ID2);
        sample2.setCancerStudyIdentifier(STUDY_ID1);

        Sample sample3 = new Sample();
        sample3.setStableId(SAMPLE_ID3);
        sample3.setPatientStableId(PATIENT_ID3);
        sample3.setCancerStudyIdentifier(STUDY_ID2);

        Sample sample4 = new Sample();
        sample4.setStableId(SAMPLE_ID4);
        sample4.setPatientStableId(PATIENT_ID4);
        sample4.setCancerStudyIdentifier(STUDY_ID2);

        Sample sample5 = new Sample();
        sample5.setStableId(SAMPLE_ID5);
        sample5.setPatientStableId(PATIENT_ID5);
        sample5.setCancerStudyIdentifier(STUDY_ID2);

        ClinicalAttribute attribute1 = new ClinicalAttribute();
        attribute1.setAttrId(CLINICAL_ATTRIBUTE_ID_1);
        attribute1.setDatatype("STRING");
        attribute1.setPatientAttribute(false);

        ClinicalAttribute attribute2 = new ClinicalAttribute();
        attribute2.setAttrId(CLINICAL_ATTRIBUTE_ID_2);
        attribute2.setDatatype("STRING");
        attribute2.setPatientAttribute(true);

        ClinicalAttribute attribute3 = new ClinicalAttribute();
        attribute3.setAttrId(CLINICAL_ATTRIBUTE_ID_3);
        attribute3.setDatatype("NUMBER");
        attribute3.setPatientAttribute(true);

        ClinicalAttribute attribute4 = new ClinicalAttribute();
        attribute4.setAttrId(CLINICAL_ATTRIBUTE_ID_4);
        attribute4.setDatatype("NUMBER");
        attribute4.setPatientAttribute(true);

        GroupFilter groupFilter = new GroupFilter();
        List<Group> groups = new ArrayList<Group>();
        groupFilter.setGroups(groups);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/clinical-data-enrichments/fetch").accept(MediaType.APPLICATION_JSON).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(groupFilter)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()).andExpect(MockMvcResultMatchers
                        .jsonPath("$.message").value("interceptedGroupFilter size must be between 2 and 2147483647"));

        Group group1 = new Group();
        group1.setName("1");
        List<SampleIdentifier> sampleIdentifiers1 = new ArrayList<SampleIdentifier>();
        group1.setSampleIdentifiers(sampleIdentifiers1);
        groups.add(group1);

        // "groups[0].sampleIdentifiers size must be between 1 and 10000000"
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/clinical-data-enrichments/fetch").accept(MediaType.APPLICATION_JSON).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(groupFilter)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        // "groups size must be between 2 and 2147483647"
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/clinical-data-enrichments/fetch").accept(MediaType.APPLICATION_JSON).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(groupFilter)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Group group2 = new Group();
        group2.setName("1");
        List<SampleIdentifier> sampleIdentifiers2 = new ArrayList<SampleIdentifier>();
        group2.setSampleIdentifiers(sampleIdentifiers2);
        groups.add(group2);

        group1.setSampleIdentifiers(Arrays.asList(sampleIdentifier1, sampleIdentifier2));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/clinical-data-enrichments/fetch").accept(MediaType.APPLICATION_JSON).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(groupFilter)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("interceptedGroupFilter size must be between 1 and 10000000"));

        group2.setSampleIdentifiers(new ArrayList<SampleIdentifier>(
                Arrays.asList(sampleIdentifier3, sampleIdentifier4, sampleIdentifier5)));

        // when all are invalid samples
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/clinical-data-enrichments/fetch").accept(MediaType.APPLICATION_JSON).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(groupFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));

        Mockito.when(sampleService.fetchSamples(Mockito.anyList(), Mockito.anyList(),
                Mockito.anyString())).thenReturn(Arrays.asList(sample1, sample2, sample3, sample4, sample5));

        List<ClinicalAttribute> attributes = Arrays.asList(attribute1, attribute3);

        Mockito.when(clinicalAttributeService.fetchClinicalAttributes(Arrays.asList(STUDY_ID2, STUDY_ID1), "SUMMARY"))
                .thenReturn(attributes);

        List<List<Sample>> groupedSamples = new ArrayList<List<Sample>>();
        groupedSamples.add(Arrays.asList(sample1, sample2));
        groupedSamples.add(Arrays.asList(sample3, sample4, sample5));

        // [{"score":4.999999999999999,"method":"Chi-squared
        // Test","pValue":0.08208499862670093,"qValue":null},{"clinicalAttribute":{"attrId":"attribute_id2","displayName":null,"description":null,"datatype":"STRING","patientAttribute":true,"priority":null,"cancerStudyId":null,"cancerStudyIdentifier":null},"score":4.999999999999999,"method":"Chi-squared
        // Test","pValue":0.08208499862670093,"qValue":null}]

        // [{"score":0.08771942638231253,"method":"Kruskal Wallis
        // Test","pValue":0.7670968826920188,"qValue":null},{"clinicalAttribute":{"attrId":"attribute_id4","displayName":null,"description":null,"datatype":"NUMBER","patientAttribute":true,"priority":null,"cancerStudyId":null,"cancerStudyIdentifier":null},"score":0.7894737138614459,"method":"Kruskal
        // Wallis Test","pValue":0.3742593665040995,"qValue":null}]

        ClinicalDataEnrichment clinicalDataEnrichment1 = new ClinicalDataEnrichment();
        clinicalDataEnrichment1.setClinicalAttribute(attribute1);
        clinicalDataEnrichment1.setMethod("Chi-squared Test");
        clinicalDataEnrichment1.setpValue(new BigDecimal("0.08208499862670093"));
        clinicalDataEnrichment1.setScore(new BigDecimal("4.999999999999999"));

        ClinicalDataEnrichment clinicalDataEnrichment = new ClinicalDataEnrichment();
        clinicalDataEnrichment.setClinicalAttribute(attribute3);
        clinicalDataEnrichment.setMethod("Kruskal Wallis Test");
        clinicalDataEnrichment.setpValue(new BigDecimal("0.7670968826920188"));
        clinicalDataEnrichment.setScore(new BigDecimal("0.08771942638231253"));

        Mockito.when(clinicalDataEnrichmentUtil.createEnrichmentsForCategoricalData(attributes, groupedSamples))
                .thenReturn(Arrays.asList(clinicalDataEnrichment1));

        Mockito.when(clinicalDataEnrichmentUtil.createEnrichmentsForNumericData(attributes, groupedSamples))
                .thenReturn(Arrays.asList(clinicalDataEnrichment));

        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/clinical-data-enrichments/fetch").accept(MediaType.APPLICATION_JSON).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(groupFilter)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].clinicalAttribute.clinicalAttributeId")
                        .value(clinicalDataEnrichment1.getClinicalAttribute().getAttrId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].score").value(clinicalDataEnrichment1.getScore()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].pValue").value(clinicalDataEnrichment1.getpValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].method").value(clinicalDataEnrichment1.getMethod()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].clinicalAttribute.clinicalAttributeId")
                        .value(clinicalDataEnrichment.getClinicalAttribute().getAttrId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].score").value(clinicalDataEnrichment.getScore()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].pValue").value(clinicalDataEnrichment.getpValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].method").value(clinicalDataEnrichment.getMethod()));

    }
}
