package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.TemporalRelation;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
import org.cbioportal.web.parameter.DataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.filter.AndedPatientTreatmentFilters;
import org.cbioportal.web.parameter.filter.AndedSampleTreatmentFilters;
import org.cbioportal.web.parameter.filter.OredPatientTreatmentFilters;
import org.cbioportal.web.parameter.filter.OredSampleTreatmentFilters;
import org.cbioportal.web.parameter.filter.PatientTreatmentFilter;
import org.cbioportal.web.parameter.filter.SampleTreatmentFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class StudyViewMapperTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_ACC_TCGA = "acc_tcga";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getFilteredSamples() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(Arrays.asList(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
        var filteredSamples = studyViewMapper.getFilteredSamples(StudyViewFilterHelper.build(studyViewFilter, null));
        assertEquals(19, filteredSamples.size());
    }

    @Test
    public void getMutatedGenes() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
        var alterationCountByGenes = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
        assertEquals(3, alterationCountByGenes.size());

        var testBrca1AlterationCount = alterationCountByGenes.stream().filter(a -> Objects.equals(a.getHugoGeneSymbol(), "BRCA1")).findFirst();
        assert (testBrca1AlterationCount.isPresent());
        assertEquals(Integer.valueOf(5), testBrca1AlterationCount.get().getTotalCount());
    }

    @Test
    public void getMutatedGenesWithAlterationFilter() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        // Create AlterationFilter
        AlterationFilter alterationFilter = new AlterationFilter();
        Map<MutationEventType, Boolean> mutationEventTypeFilterMap = new HashMap<>();
        mutationEventTypeFilterMap.put(MutationEventType.nonsense_mutation, Boolean.TRUE);
        mutationEventTypeFilterMap.put(MutationEventType.other, Boolean.FALSE);
        alterationFilter.setMutationEventTypes(mutationEventTypeFilterMap);

        var alterationCountByGenes = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null),
            AlterationFilterHelper.build(alterationFilter));
        assertEquals(2, alterationCountByGenes.size());

        AlterationFilter onlyMutationStatusFilter = new AlterationFilter();
        onlyMutationStatusFilter.setMutationEventTypes(new HashMap<>());
        onlyMutationStatusFilter.setIncludeGermline(false);
        onlyMutationStatusFilter.setIncludeSomatic(false);
        onlyMutationStatusFilter.setIncludeUnknownStatus(true);

        var alterationCountByGenes1 = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null),
            AlterationFilterHelper.build(onlyMutationStatusFilter));
        assertEquals(1, alterationCountByGenes1.size());

        AlterationFilter mutationTypeAndStatusFilter = new AlterationFilter();
        mutationTypeAndStatusFilter.setMutationEventTypes(mutationEventTypeFilterMap);
        mutationTypeAndStatusFilter.setMutationEventTypes(new HashMap<>());
        mutationTypeAndStatusFilter.setIncludeGermline(false);
        mutationTypeAndStatusFilter.setIncludeSomatic(false);
        mutationTypeAndStatusFilter.setIncludeUnknownStatus(true);

        var alterationCountByGenes2 = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null),
            AlterationFilterHelper.build(onlyMutationStatusFilter));
        assertEquals(1, alterationCountByGenes2.size());
    }

    @Test
    public void getTotalProfiledCountsByGene() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        var totalProfiledCountsMap = studyViewMapper.getTotalProfiledCounts(StudyViewFilterHelper.build(studyViewFilter, null),
            "MUTATION_EXTENDED");

        assertEquals(3, totalProfiledCountsMap.size());

        var akt2TotalProfiledCounts = totalProfiledCountsMap.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT2")).findFirst();
        assertTrue(akt2TotalProfiledCounts.isPresent());
        assertEquals(4, akt2TotalProfiledCounts.get().getNumberOfProfiledCases().intValue());
    }

    @Test
    public void getClinicalEventTypeCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        var clinicalEventTypeCounts = studyViewMapper.getClinicalEventTypeCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        assertEquals(4, clinicalEventTypeCounts.size());

        var clinicalEventTypeCountOptional = clinicalEventTypeCounts.stream().filter(ce -> ce.getEventType().equals("Treatment"))
            .findFirst();

        assertTrue(clinicalEventTypeCountOptional.isPresent());
        assertEquals(1, clinicalEventTypeCountOptional.get().getCount().intValue());

        DataFilter dataFilter = new DataFilter();
        DataFilterValue dataFilterValue = new DataFilterValue();
        dataFilterValue.setValue("Treatment");
        dataFilter.setValues(List.of(dataFilterValue));
        studyViewFilter.setClinicalEventFilters(List.of(dataFilter));

        clinicalEventTypeCounts = studyViewMapper.getClinicalEventTypeCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        assertEquals(3, clinicalEventTypeCounts.size());

        clinicalEventTypeCountOptional = clinicalEventTypeCounts.stream().filter(ce -> ce.getEventType().equals("status"))
            .findFirst();

        assertFalse(clinicalEventTypeCountOptional.isPresent());
    }

    @Test
    public void getPatientTreatmentReportCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));


        var patientTreatmentCounts = studyViewMapper.getPatientTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        var patientTreatments = studyViewMapper.getPatientTreatments(StudyViewFilterHelper.build(studyViewFilter, null));

        assertEquals(1, patientTreatmentCounts);
        assertEquals("madeupanib", patientTreatments.getFirst().treatment());

        PatientTreatmentFilter filter = new PatientTreatmentFilter();
        filter.setTreatment("madeupanib");

        OredPatientTreatmentFilters oredPatientTreatmentFilters = new OredPatientTreatmentFilters();
        oredPatientTreatmentFilters.setFilters(List.of(filter));

        AndedPatientTreatmentFilters andedPatientTreatmentFilters = new AndedPatientTreatmentFilters();
        andedPatientTreatmentFilters.setFilters(List.of(oredPatientTreatmentFilters));
        studyViewFilter.setPatientTreatmentFilters(andedPatientTreatmentFilters);

        patientTreatmentCounts = studyViewMapper.getPatientTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        patientTreatments = studyViewMapper.getPatientTreatments(StudyViewFilterHelper.build(studyViewFilter, null));

        assertEquals(1, patientTreatmentCounts);
        assertEquals("madeupanib", patientTreatments.getFirst().treatment());

    }

    @Test
    public void getSampleTreatmentCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));


        var totalSampleTreatmentCount = studyViewMapper.getTotalSampleTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        var sampleTreatmentCounts = studyViewMapper.getSampleTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        assertEquals(1, totalSampleTreatmentCount);
        assertEquals("madeupanib", sampleTreatmentCounts.getFirst().treatment());
        assertEquals(1, sampleTreatmentCounts.getFirst().postSampleCount());
        assertEquals(0, sampleTreatmentCounts.getFirst().preSampleCount());

        SampleTreatmentFilter filter = new SampleTreatmentFilter();
        filter.setTreatment("madeupanib");
        filter.setTime(TemporalRelation.Pre);

        OredSampleTreatmentFilters oredSampleTreatmentFilters = new OredSampleTreatmentFilters();
        oredSampleTreatmentFilters.setFilters(List.of(filter));

        AndedSampleTreatmentFilters andedSampleTreatmentFilters = new AndedSampleTreatmentFilters();
        andedSampleTreatmentFilters.setFilters(List.of(oredSampleTreatmentFilters));
        studyViewFilter.setSampleTreatmentFilters(andedSampleTreatmentFilters);

        totalSampleTreatmentCount = studyViewMapper.getTotalSampleTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        sampleTreatmentCounts = studyViewMapper.getSampleTreatmentCounts(StudyViewFilterHelper.build(studyViewFilter, null));

        assertEquals(0, totalSampleTreatmentCount);
        assertEquals(0, sampleTreatmentCounts.size());

    }
}