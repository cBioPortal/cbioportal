package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.GenericAssayDataCount;
import org.cbioportal.model.GenericAssayDataCountItem;
import org.cbioportal.persistence.helper.StudyViewFilterHelper;
import org.cbioportal.persistence.mybatisclickhouse.config.MyBatisConfig;
import org.cbioportal.web.parameter.GenericAssayDataFilter;
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
public class GenericAssayDataCountsTest extends AbstractTestcontainers {

    private static final String ACC_TCGA = "acc_tcga";
    private static final String STUDY_GENIE_PUB = "study_genie_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getSampleCategoricalGenericAssayDataCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(ACC_TCGA));

        GenericAssayDataFilter genericAssayDataFilter = new GenericAssayDataFilter("1p_status", "armlevel_cna");
        List<GenericAssayDataCountItem> actualCounts = studyViewMapper.getGenericAssayDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            List.of(genericAssayDataFilter)
        );

        List<GenericAssayDataCountItem> expectedCounts = List.of(
            new GenericAssayDataCountItem("1p_status", List.of(
                new GenericAssayDataCount("Loss", 1),
                new GenericAssayDataCount("Gain", 1),
                new GenericAssayDataCount("Unchanged", 1),
                new GenericAssayDataCount("NA", 1)
            ))
        );

        assertThat(actualCounts)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedCounts);
    }

    @Test
    public void getPatientCategoricalGenericAssayDataCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_GENIE_PUB));

        GenericAssayDataFilter genericAssayDataFilter = new GenericAssayDataFilter("DMETS_DX_ADRENAL", "distant_mets");
        List<GenericAssayDataCountItem> actualCounts = studyViewMapper.getGenericAssayDataCounts(
            StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            List.of(genericAssayDataFilter)
        );

        List<GenericAssayDataCountItem> expectedCounts = List.of(
            new GenericAssayDataCountItem("DMETS_DX_ADRENAL", List.of(
                new GenericAssayDataCount("No", 9),
                new GenericAssayDataCount("Yes", 1),
                new GenericAssayDataCount("NA", 14)
            ))
        );

        assertThat(actualCounts)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .isEqualTo(expectedCounts);
    }
}