package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.GenomicDataCountItem;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
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
public class GenomicDataCountsTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getCNACounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        GenomicDataFilter genomicDataFilterCNA = new GenomicDataFilter("AKT1", "cna");
        List<GenomicDataCountItem> actualCountsCNA = studyViewMapper.getCNACounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()), List.of(genomicDataFilterCNA));
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
        List<GenomicDataCountItem> actualCountsGISTIC = studyViewMapper.getCNACounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()), List.of(genomicDataFilterGISTIC));
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
}
