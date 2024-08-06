package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.persistence.enums.ClinicalAttributeDataSource;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
import org.cbioportal.web.parameter.CategorizedClinicalDataCountFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class StudyViewMapperClinicalDataCountTest extends AbstractTestcontainers {
    private static final String STUDY_ACC_TCGA = "acc_tcga";
    private static final String STUDY_GENIE_PUB = "study_genie_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;
    
    @Test
    public void getMutationCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        var mutationsCounts = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, null),
            List.of("mutation_count"),
            Collections.emptyList()
        );
        
        assertEquals(6, mutationsCounts.size());
        assertEquals(1, findClinicaDataCount(mutationsCounts, "11"));
        assertEquals(1, findClinicaDataCount(mutationsCounts, "6"));
        assertEquals(2, findClinicaDataCount(mutationsCounts, "4"));
        assertEquals(4, findClinicaDataCount(mutationsCounts, "2"));
        assertEquals(2, findClinicaDataCount(mutationsCounts, "1"));
        // 1 empty string + 1 'NAN' + 11 samples with no data
        assertEquals(13, findClinicaDataCount(mutationsCounts, "NA"));
    }
    
    @Test
    public void getCenterCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        var categoricalClinicalDataCounts = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, null),
            List.of("center"),
            Collections.emptyList()
        );

        assertEquals(7, categoricalClinicalDataCounts.size());
        assertEquals(3, findClinicaDataCount(categoricalClinicalDataCounts, "msk"));
        assertEquals(2, findClinicaDataCount(categoricalClinicalDataCounts, "dfci"));
        assertEquals(2, findClinicaDataCount(categoricalClinicalDataCounts, "chop"));
        assertEquals(1, findClinicaDataCount(categoricalClinicalDataCounts, "mda"));
        assertEquals(1, findClinicaDataCount(categoricalClinicalDataCounts, "ohsu"));
        assertEquals(1, findClinicaDataCount(categoricalClinicalDataCounts, "ucsf"));
        // 1 empty string + 1 'NA' + 11 samples with no data
        assertEquals(13, findClinicaDataCount(categoricalClinicalDataCounts, "NA"));
    }

    @Test
    public void getDeadCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        var categoricalClinicalDataCounts = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, null),
            List.of("dead"),
            Collections.emptyList()
        );

        assertEquals(6, categoricalClinicalDataCounts.size());
        assertEquals(3, findClinicaDataCount(categoricalClinicalDataCounts, "True"));
        assertEquals(4, findClinicaDataCount(categoricalClinicalDataCounts, "False"));
        assertEquals(1, findClinicaDataCount(categoricalClinicalDataCounts, "Not Released"));
        assertEquals(1, findClinicaDataCount(categoricalClinicalDataCounts, "Not Collected"));
        assertEquals(1, findClinicaDataCount(categoricalClinicalDataCounts, "Unknown"));
        // 1 empty string + 1 'N/A' + 11 samples with no data
        assertEquals(13, findClinicaDataCount(categoricalClinicalDataCounts, "NA"));
    }

    @Test
    public void getMutationAndCenterCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        var combinedClinicalDataCounts = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, null),
            List.of("mutation_count", "center"),
            Collections.emptyList()
        );

        assertEquals(13, combinedClinicalDataCounts.size());
    }

    @Test
    public void getAgeCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        var ageCounts = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, null),
            List.of("age"),
            Collections.emptyList()
        );

        assertAgeCounts(ageCounts);
        
        // 1 empty string + 1 'NAN' + 1 'N/A' + 1 patient without data
        assertEquals(4, findClinicaDataCount(ageCounts, "NA"));
    }

    @Test
    public void getAgeCountsForMultipleStudies() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB, STUDY_ACC_TCGA));

        var ageCounts = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, null),
            List.of("age"),
            Collections.emptyList()
        );

        // everything should be exactly the same as single study (STUDY_GENIE_PUB) filter
        // except NA counts
        assertAgeCounts(ageCounts);

        // TODO this fails because of a known issue 
        //  (https://github.com/cBioPortal/rfc80-team/issues/39)
        // 1 empty string + 1 'NAN' + 1 'N/A' + 1 GENIE_PUB patient without data + 4 ACC_TCGA data without data
        // assertEquals(8, findClinicaDataCount(ageCounts, "NA"));
    }
    
    private void assertAgeCounts(List<ClinicalDataCount> ageCounts) {
        assertEquals(14, ageCounts.size());
        
        assertEquals(3, findClinicaDataCount(ageCounts, "<18"));
        assertEquals(1, findClinicaDataCount(ageCounts, "18"));
        assertEquals(1, findClinicaDataCount(ageCounts, "22"));
        assertEquals(2, findClinicaDataCount(ageCounts, "42"));
        assertEquals(1, findClinicaDataCount(ageCounts, "66"));
        assertEquals(1, findClinicaDataCount(ageCounts, "66"));
        assertEquals(1, findClinicaDataCount(ageCounts, "68"));
        assertEquals(1, findClinicaDataCount(ageCounts, "77"));
        assertEquals(1, findClinicaDataCount(ageCounts, "78"));
        assertEquals(1, findClinicaDataCount(ageCounts, "79"));
        assertEquals(2, findClinicaDataCount(ageCounts, "80"));
        assertEquals(2, findClinicaDataCount(ageCounts, "82"));
        assertEquals(1, findClinicaDataCount(ageCounts, "89"));
        assertEquals(2, findClinicaDataCount(ageCounts, ">89"));
    }
    
    @Test
    public void getMutationCountsFilteredByAge() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));
        
        // filter patients with age between 20 and 70
        // (there are 5 patients within this range, which are 307..311)
        ClinicalDataFilter filter = buildClinicalDataFilter("age", 20, 70);
        studyViewFilter.setClinicalDataFilters(List.of(filter));
        
        Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributeDataSourceListMap = new HashMap<>();
        var clinicalAttr = new ClinicalAttribute();
        clinicalAttr.setAttrId("age");
        clinicalAttr.setPatientAttribute(true);
        clinicalAttr.setDatatype("NUMBER");
        clinicalAttributeDataSourceListMap.put(ClinicalAttributeDataSource.PATIENT, List.of(clinicalAttr));
        clinicalAttributeDataSourceListMap.put(ClinicalAttributeDataSource.SAMPLE, List.of());
        var mutationCountsFiltered = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, clinicalAttributeDataSourceListMap, null, null),
            List.of("mutation_count"),
            Collections.emptyList()
        );

        assertEquals(3, mutationCountsFiltered.size());
        assertEquals(2, findClinicaDataCount(mutationCountsFiltered, "2"));
        assertEquals(2, findClinicaDataCount(mutationCountsFiltered, "1"));
        assertEquals(1, findClinicaDataCount(mutationCountsFiltered, "NA"));
    }

    @Test
    public void getMutationCountsFilteredByAgeWithOpenStartValues() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        // filter patients with age less than 20
        // (there are 4 patients within this range, which are 301, 302, 303, and 306)
        ClinicalDataFilter filter = buildClinicalDataFilter("age", null, 20);
        studyViewFilter.setClinicalDataFilters(List.of(filter));

        Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributeDataSourceListMap = new HashMap<>();
        var clinicalAttr = new ClinicalAttribute();
        clinicalAttr.setAttrId("age");
        clinicalAttr.setPatientAttribute(true);
        clinicalAttr.setDatatype("NUMBER");
        clinicalAttributeDataSourceListMap.put(ClinicalAttributeDataSource.PATIENT, List.of(clinicalAttr));
        clinicalAttributeDataSourceListMap.put(ClinicalAttributeDataSource.SAMPLE, List.of());
        var mutationCountsFiltered = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, clinicalAttributeDataSourceListMap, null, null),
            List.of("mutation_count"),
            Collections.emptyList()
        );

        assertEquals(4, mutationCountsFiltered.size());
        assertEquals(1, findClinicaDataCount(mutationCountsFiltered, "11")); // patient 301
        assertEquals(1, findClinicaDataCount(mutationCountsFiltered, "6")); // patient 302
        assertEquals(1, findClinicaDataCount(mutationCountsFiltered, "4")); // patient 303
        assertEquals(1, findClinicaDataCount(mutationCountsFiltered, "2")); // patient 306

        // no patients/samples with NA
        assertEquals(0, findClinicaDataCount(mutationCountsFiltered, "NA")); 
    }

    @Test
    public void getMutationCountsFilteredByAgeWithOpenEndValues() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        // filter patients with age greater than 80
        // (there are 4 patients within this range, which are 317, 318, 319, 304, and 305)
        ClinicalDataFilter filter = buildClinicalDataFilter("age", 80, null);
        studyViewFilter.setClinicalDataFilters(List.of(filter));

        Map<ClinicalAttributeDataSource, List<ClinicalAttribute>> clinicalAttributeDataSourceListMap = new HashMap<>();
        var clinicalAttr = new ClinicalAttribute();
        clinicalAttr.setAttrId("age");
        clinicalAttr.setPatientAttribute(true);
        clinicalAttr.setDatatype("NUMBER");
        clinicalAttributeDataSourceListMap.put(ClinicalAttributeDataSource.PATIENT, List.of(clinicalAttr));
        clinicalAttributeDataSourceListMap.put(ClinicalAttributeDataSource.SAMPLE, List.of());
        var mutationCountsFiltered = studyViewMapper.getClinicalDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, clinicalAttributeDataSourceListMap, null, null),
            List.of("mutation_count"),
            Collections.emptyList()
        );

        assertEquals(3, mutationCountsFiltered.size());
        assertEquals(1, findClinicaDataCount(mutationCountsFiltered, "4")); // patient 304
        assertEquals(1, findClinicaDataCount(mutationCountsFiltered, "2")); // patient 305

        // patients/samples with NA data: 317, 318, and 319
        assertEquals(3, findClinicaDataCount(mutationCountsFiltered, "NA"));
    }
    
    private ClinicalDataFilter buildClinicalDataFilter(String attributeId, Integer start, Integer end) {
        DataFilterValue value = new DataFilterValue();
        if (start != null) {
            value.setStart(BigDecimal.valueOf(start));
        }
        if (end != null) {
            value.setEnd(BigDecimal.valueOf(end));
        }

        ClinicalDataFilter filter = new ClinicalDataFilter();
        filter.setAttributeId(attributeId);
        filter.setValues(List.of(value));
        
        return filter;
    }
    
    private int findClinicaDataCount(List<ClinicalDataCount> counts, String attrValue) {
        var count = counts.stream().filter(c -> c.getValue().equals(attrValue)).findAny().orElse(null);

        return count == null ? 0 : count.getCount();
    }
}
