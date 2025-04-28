package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.model.GenericAssayDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseGenericAssayMapperTest {


    private static final String ACC_TCGA = "acc_tcga";
    private static final String STUDY_GENIE_PUB = "study_genie_pub";

    @Autowired
    private ClickhouseGenericAssayMapper mapper;

    @Test
    public void getSampleCategoricalGenericAssayDataCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(ACC_TCGA));

        GenericAssayDataFilter genericAssayDataFilter = new GenericAssayDataFilter("1p_status", "armlevel_cna");
        List<GenericAssayDataCountItem> actualCounts = mapper.getGenericAssayDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
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
        List<GenericAssayDataCountItem> actualCounts = mapper.getGenericAssayDataCounts(
            StudyViewFilterFactory.make(studyViewFilter, null, studyViewFilter.getStudyIds(), null),
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