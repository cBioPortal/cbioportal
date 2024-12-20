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
public class MutationDataCountsTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getMutationCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        GenomicDataFilter genomicDataFilterMutation = new GenomicDataFilter("AKT1", "cna");
        Map<String, Integer> actualMutationCounts = studyViewMapper.getMutationCounts(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()), genomicDataFilterMutation);
        Map<String, Integer> expectedMutationCounts = new HashMap<>();
        expectedMutationCounts.put("mutatedCount", 2);
        expectedMutationCounts.put("notMutatedCount", 8);
        expectedMutationCounts.put("notProfiledCount", 5);
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
        List<GenomicDataCountItem> actualMutationCountsByType = studyViewMapper.getMutationCountsByType(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()), List.of(genomicDataFilterMutation));
        List<GenomicDataCountItem> expectedMutationCountsByType = List.of(
            new GenomicDataCountItem("AKT1", "mutations", List.of(
                new GenomicDataCount("nonsense mutation", "nonsense_mutation", 2, 1),
                new GenomicDataCount("missense mutation", "missense_mutation", 1, 1)
            )));
        assertThat(actualMutationCountsByType)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedMutationCountsByType);
    }
}
