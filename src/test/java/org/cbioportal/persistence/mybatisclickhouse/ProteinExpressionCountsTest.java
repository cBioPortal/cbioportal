package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.ClinicalDataCount;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ProteinExpressionCountsTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_ACC_TCGA = "acc_tcga";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getProteinExpressionCounts() {
        // Testing combined study missing samples when one lacks a relevant genomic profile
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));

        GenomicDataBinFilter genomicDataBinFilterRPPA = new GenomicDataBinFilter();
        genomicDataBinFilterRPPA.setHugoGeneSymbol("AKT1");
        genomicDataBinFilterRPPA.setProfileType("rppa");

        List<ClinicalDataCount> actualRPPACounts1 = studyViewMapper.getGenomicDataBinCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()), List.of(genomicDataBinFilterRPPA));

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

        List<ClinicalDataCount> actualRPPACounts2 = studyViewMapper.getGenomicDataBinCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()), List.of(genomicDataBinFilterRPPA));

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
