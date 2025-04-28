package org.cbioportal.infrastructure.repository.clickhouse.clinical_event;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.domain.studyview.StudyViewFilterFactory;
import org.cbioportal.infrastructure.repository.clickhouse.AbstractTestcontainers;
import org.cbioportal.infrastructure.repository.clickhouse.config.MyBatisConfig;
import org.cbioportal.legacy.web.parameter.DataFilter;
import org.cbioportal.legacy.web.parameter.DataFilterValue;
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

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class ClickhouseClinicalEventMapperTest {
    private static final String STUDY_TCGA_PUB = "study_tcga_pub";

    @Autowired
    private ClickhouseClinicalEventMapper mapper;

    @Test
    public void getClinicalEventTypeCounts() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        StudyViewFilterContext studyViewFilterContext = StudyViewFilterFactory.make(studyViewFilter, null,
            studyViewFilter.getStudyIds(), null);

        var clinicalEventTypeCounts = mapper.getClinicalEventTypeCounts(studyViewFilterContext);

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

        clinicalEventTypeCounts = mapper.getClinicalEventTypeCounts(
            StudyViewFilterFactory.make(studyViewFilter, null,
                studyViewFilter.getStudyIds(), null));

        assertEquals(3, clinicalEventTypeCounts.size());

        clinicalEventTypeCountOptional = clinicalEventTypeCounts.stream().filter(ce -> ce.getEventType().equals("status"))
            .findFirst();

        assertFalse(clinicalEventTypeCountOptional.isPresent());
    }
}