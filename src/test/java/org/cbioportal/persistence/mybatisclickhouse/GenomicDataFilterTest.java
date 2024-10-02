package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.GenomicDataBinFilter;
import org.cbioportal.web.parameter.GenomicDataFilter;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class GenomicDataFilterTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_ACC_TCGA = "acc_tcga";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getCNACounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        GenomicDataFilter genomicDataFilterCNA = new GenomicDataFilter("AKT1", "cna");
        List<GenomicDataCountItem> actualCountsCNA = studyViewMapper.getCNACounts(StudyViewFilterHelper.build(studyViewFilter, null, null), List.of(genomicDataFilterCNA));
        List<GenomicDataCountItem> expectedCountsCNA = List.of(
            new GenomicDataCountItem("AKT1", "cna", List.of(
                new GenomicDataCount("Homozygously deleted", "-2", 2),
                new GenomicDataCount("Heterozygously deleted", "-1", 2),
                new GenomicDataCount("Diploid", "0", 2),
                new GenomicDataCount("Gained", "1", 2),
                new GenomicDataCount("Amplified", "2", 2),
                new GenomicDataCount("NA", "NA", 5)
            )));
        assertThat(actualCountsCNA)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedCountsCNA);

        GenomicDataFilter genomicDataFilterGISTIC = new GenomicDataFilter("AKT1", "gistic");
        List<GenomicDataCountItem> actualCountsGISTIC = studyViewMapper.getCNACounts(StudyViewFilterHelper.build(studyViewFilter, null, null), List.of(genomicDataFilterGISTIC));
        List<GenomicDataCountItem> expectedCountsGISTIC = List.of(
            new GenomicDataCountItem("AKT1", "gistic", List.of(
                new GenomicDataCount("Homozygously deleted", "-2", 2),
                new GenomicDataCount("Heterozygously deleted", "-1", 3),
                new GenomicDataCount("Diploid", "0", 3),
                new GenomicDataCount("Gained", "1", 3),
                new GenomicDataCount("Amplified", "2", 3),
                new GenomicDataCount("NA", "NA", 1)
            )));
        assertThat(actualCountsGISTIC)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedCountsGISTIC);
    }
    
    @Test
    public void getMutationCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
        
        GenomicDataFilter genomicDataFilterMutation = new GenomicDataFilter("AKT1", "cna");
        Map<String, Integer> actualMutationCounts = studyViewMapper.getMutationCounts(StudyViewFilterHelper.build(studyViewFilter, null, null), genomicDataFilterMutation);
        Map<String, Integer> expectedMutationCounts = new HashMap<>();
        expectedMutationCounts.put("mutatedCount", 2);
        expectedMutationCounts.put("notMutatedCount", 2);
        expectedMutationCounts.put("notProfiledCount", 11);
        assertThat(actualMutationCounts)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedMutationCounts);
    }
    
    @Test
    public void getMutationCountsByType() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        GenomicDataFilter genomicDataFilterMutation = new GenomicDataFilter("AKT1", "mutation");
        List<GenomicDataCountItem> actualMutationCountsByType = studyViewMapper.getMutationCountsByType(StudyViewFilterHelper.build(studyViewFilter, null, null), List.of(genomicDataFilterMutation));
        List<GenomicDataCountItem> expectedMutationCountsByType = List.of(
            new GenomicDataCountItem("AKT1", "mutations", List.of(
                new GenomicDataCount("nonsense mutation", "nonsense_mutation", 1, 1),
                new GenomicDataCount("missense mutation", "missense_mutation", 1, 1)
            )));
        assertThat(actualMutationCountsByType)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedMutationCountsByType);
    }

    @Test
    public void getProteinExpressionCounts() {
        // Testing combined study missing samples when one lacks a relevant genomic profile
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));

        GenomicDataBinFilter genomicDataBinFilterRPPA = new GenomicDataBinFilter();
        genomicDataBinFilterRPPA.setHugoGeneSymbol("AKT1");
        genomicDataBinFilterRPPA.setProfileType("rppa");

        List<ClinicalDataCount> actualRPPACounts1 = studyViewMapper.getGenomicDataBinCounts(StudyViewFilterHelper.build(studyViewFilter, null, null), List.of(genomicDataBinFilterRPPA));

        ClinicalDataCount expectedRPPACount1 = new ClinicalDataCount();
        expectedRPPACount1.setAttributeId("AKT1rppa");
        expectedRPPACount1.setValue("0.7360");
        expectedRPPACount1.setCount(1);
        ClinicalDataCount expectedRPPACount2 = new ClinicalDataCount();
        expectedRPPACount2.setAttributeId("AKT1rppa");
        expectedRPPACount2.setValue("-0.8097");
        expectedRPPACount2.setCount(1);
        ClinicalDataCount expectedRPPACount3 = new ClinicalDataCount();
        expectedRPPACount3.setAttributeId("AKT1rppa");
        expectedRPPACount3.setValue("-0.1260");
        expectedRPPACount3.setCount(1);
        ClinicalDataCount expectedRPPACountNA = new ClinicalDataCount();
        expectedRPPACountNA.setAttributeId("AKT1rppa");
        expectedRPPACountNA.setValue("NA");
        expectedRPPACountNA.setCount(16);

        List<ClinicalDataCount> expectedRPPACounts1 = List.of(
            expectedRPPACount1, expectedRPPACount2, expectedRPPACount3, expectedRPPACountNA
        );
        assertThat(actualRPPACounts1)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedRPPACounts1);


        // Testing NA filtering on combined study missing samples when one lacks a relevant genomic profile
        // Make genomic data filter to put in study view filter
        GenomicDataFilter genomicDataFilterRPPA = new GenomicDataFilter("AKT1", "rppa");
        DataFilterValue dataFilterValue = new DataFilterValue();
        dataFilterValue.setValue("NA");
        genomicDataFilterRPPA.setValues(List.of(dataFilterValue));
        studyViewFilter.setGenomicDataFilters(List.of(genomicDataFilterRPPA));

        List<ClinicalDataCount> actualRPPACounts2 = studyViewMapper.getGenomicDataBinCounts(StudyViewFilterHelper.build(studyViewFilter, null, null), List.of(genomicDataBinFilterRPPA));

        ClinicalDataCount expectedRPPACount = new ClinicalDataCount();
        expectedRPPACount.setAttributeId("AKT1rppa");
        expectedRPPACount.setValue("NA");
        expectedRPPACount.setCount(16);

        List<ClinicalDataCount> expectedRPPACounts2 = List.of(
            expectedRPPACount
        );
        assertThat(actualRPPACounts2)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedRPPACounts2);
    }
}
