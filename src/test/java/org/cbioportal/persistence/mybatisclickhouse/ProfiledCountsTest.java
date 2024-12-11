package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ProfiledCountsTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_GENIE_PUB = "study_genie_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getTotalProfiledCountsByGene() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        // Testing profiled counts on samples with gene panel data and WES for one study
        var totalProfiledCountsForMutationsMap = studyViewMapper.getTotalProfiledCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "MUTATION_EXTENDED", List.of());
        var totalProfiledCountsForCnaMap = studyViewMapper.getTotalProfiledCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "COPY_NUMBER_ALTERATION", List.of());
        var sampleProfiledCountsForMutationsWithoutPanelDataMap = studyViewMapper.getSampleProfileCountWithoutPanelData(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "MUTATION_EXTENDED");
        var sampleProfiledCountsForCnaWithoutPanelDataMap = studyViewMapper.getSampleProfileCountWithoutPanelData(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "COPY_NUMBER_ALTERATION");

        // Assert the count of genes with profiled cases for mutations
        assertEquals(5, totalProfiledCountsForMutationsMap.size());
        // Assert the count of genes with profiled cases for CNA
        assertEquals(5, totalProfiledCountsForCnaMap.size());
        // Assert the profiled counts for mutations without panel data (WES)
        assertEquals(6, sampleProfiledCountsForMutationsWithoutPanelDataMap);
        // Assert the profiled counts for CNA without panel data (WES)
        assertEquals(11, sampleProfiledCountsForCnaWithoutPanelDataMap);

        // Assert the profiled counts for AKT2 mutations
        // AKT2 is on testpanel2 in STUDY_TCGA_PUB
        var akt2TotalProfiledCountsForMutations = totalProfiledCountsForMutationsMap.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT2")).findFirst();
        assertTrue(akt2TotalProfiledCountsForMutations.isPresent());
        assertEquals(4, akt2TotalProfiledCountsForMutations.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for BRCA1 mutations
        // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
        var brca1TotalProfiledCountsForMutations = totalProfiledCountsForMutationsMap.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA1")).findFirst();
        assertTrue(brca1TotalProfiledCountsForMutations.isPresent());
        assertEquals(1, brca1TotalProfiledCountsForMutations.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for AKT1 mutations
        // AKT1 is on both testpanel1 and testpanel2 in STUDY_TCGA_PUB
        var akt1TotalProfiledCountsForMutations = totalProfiledCountsForMutationsMap.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT1")).findFirst();
        assertTrue(akt1TotalProfiledCountsForMutations.isPresent());
        assertEquals(5, akt1TotalProfiledCountsForMutations.get().getNumberOfProfiledCases().intValue());

        // Assert the profiled counts for AKT2 CNA
        // AKT2 is on testpanel2 in STUDY_TCGA_PUB
        var akt2TotalProfiledCountsForCna = totalProfiledCountsForCnaMap.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT2")).findFirst();
        assertTrue(akt2TotalProfiledCountsForCna.isPresent());
        assertEquals(6, akt2TotalProfiledCountsForCna.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for BRCA1 CNA
        // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
        var brca1TotalProfiledCountsForCna = totalProfiledCountsForCnaMap.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA1")).findFirst();
        assertTrue(brca1TotalProfiledCountsForCna.isPresent());
        assertEquals(2, brca1TotalProfiledCountsForCna.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for AKT1 CNA
        // AKT1 is on both testpanel1 and testpanel2 in STUDY_TCGA_PUB
        var akt1TotalProfiledCountsForCna = totalProfiledCountsForCnaMap.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT1")).findFirst();
        assertTrue(akt1TotalProfiledCountsForCna.isPresent());
        assertEquals(8, akt1TotalProfiledCountsForCna.get().getNumberOfProfiledCases().intValue());

        // Testing profiled counts on combined studies
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_GENIE_PUB));

        // Testing profiled counts on samples with gene panel data and WES for a combined study
        var totalProfiledCountsForMutationsMap1 = studyViewMapper.getTotalProfiledCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "MUTATION_EXTENDED", List.of());
        var totalProfiledCountsForCnaMap1 = studyViewMapper.getTotalProfiledCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "COPY_NUMBER_ALTERATION", List.of());
        var sampleProfiledCountsForMutationsWithoutPanelDataMap1 = studyViewMapper.getSampleProfileCountWithoutPanelData(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "MUTATION_EXTENDED");
        var sampleProfiledCountsForCnaWithoutPanelDataMap1 = studyViewMapper.getSampleProfileCountWithoutPanelData(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            "COPY_NUMBER_ALTERATION");

        // Assert the count of genes with profiled cases for mutations in a combined study
        assertEquals(8, totalProfiledCountsForMutationsMap1.size());
        // Assert the count of genes with profiled cases for CNA in a combined study
        assertEquals(8, totalProfiledCountsForCnaMap1.size());
        // Assert the profiled counts for mutations without panel data (WES) in a combined study
        assertEquals(8, sampleProfiledCountsForMutationsWithoutPanelDataMap1);
        // Assert the profiled counts for CNA without panel data (WES) in a combined study
        assertEquals(12, sampleProfiledCountsForCnaWithoutPanelDataMap1);

        // Assert the profiled counts for BRCA1 mutations
        // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
        var brca1TotalProfiledCountsForMutations1 = totalProfiledCountsForMutationsMap1.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA1")).findFirst();
        assertTrue(brca1TotalProfiledCountsForMutations1.isPresent());
        assertEquals(1, brca1TotalProfiledCountsForMutations1.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for BRCA2 mutations
        // BRCA2 is on testpanel3 and testpanel4 in STUDY_GENIE_PUB
        var brca2TotalProfiledCountsForMutations1 = totalProfiledCountsForMutationsMap1.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA2")).findFirst();
        assertTrue(brca2TotalProfiledCountsForMutations1.isPresent());
        assertEquals(2, brca2TotalProfiledCountsForMutations1.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for AKT2 mutations
        // AKT2 is on testpanel2 in STUDY_TCGA_PUB and testpanel4 in STUDY_GENIE_PUB
        var akt2TotalProfiledCountsForMutations1 = totalProfiledCountsForMutationsMap1.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT2")).findFirst();
        assertTrue(akt2TotalProfiledCountsForMutations1.isPresent());
        assertEquals(4, akt2TotalProfiledCountsForMutations1.get().getNumberOfProfiledCases().intValue());

        // Assert the profiled counts for BRCA1 CNA
        // BRCA1 is on testpanel1 in STUDY_TCGA_PUB
        var brca1TotalProfiledCountsForCna1 = totalProfiledCountsForCnaMap1.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA1")).findFirst();
        assertTrue(brca1TotalProfiledCountsForCna1.isPresent());
        assertEquals(2, brca1TotalProfiledCountsForCna1.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for BRCA2 CNA
        // BRCA2 is on testpanel3 and testpanel4 in STUDY_GENIE_PUB
        var brca2TotalProfiledCountsForCna1 = totalProfiledCountsForCnaMap1.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA2")).findFirst();
        assertTrue(brca2TotalProfiledCountsForCna1.isPresent());
        assertEquals(3, brca2TotalProfiledCountsForCna1.get().getNumberOfProfiledCases().intValue());
        // Assert the profiled counts for AKT2 CNA
        // AKT2 is on testpanel2 in STUDY_TCGA_PUB and testpanel4 in STUDY_GENIE_PUB
        var akt2TotalProfiledCountsForCna1 = totalProfiledCountsForCnaMap1.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT2")).findFirst();
        assertTrue(akt2TotalProfiledCountsForCna1.isPresent());
        assertEquals(7, akt2TotalProfiledCountsForCna1.get().getNumberOfProfiledCases().intValue());
    }
}
