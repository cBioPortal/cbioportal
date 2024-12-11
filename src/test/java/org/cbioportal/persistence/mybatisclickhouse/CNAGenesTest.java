package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.persistence.helper.AlterationFilterHelper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class CNAGenesTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getCnaGenes() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
        var alterationCountByGenes = studyViewMapper.getCnaGenes(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
        assertEquals(3, alterationCountByGenes.size());

        // Test cna count for akt1
        var testAKT1AlterationCount = alterationCountByGenes.stream().filter(a -> Objects.equals(a.getHugoGeneSymbol(), "AKT1"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
        assertEquals(3, testAKT1AlterationCount);
    }

    @Test
    public void getCnaGenesWithAlterationFilter() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        // Create AlterationFilter
        AlterationFilter alterationFilter = new AlterationFilter();
        Map<CNA, Boolean> cnaEventTypeFilterMap = new HashMap<>();
        cnaEventTypeFilterMap.put(CNA.HOMDEL, false);
        cnaEventTypeFilterMap.put(CNA.AMP, true);
        alterationFilter.setCopyNumberAlterationEventTypes(cnaEventTypeFilterMap);
        
        var alterationCountByGenes = studyViewMapper.getCnaGenes(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            AlterationFilterHelper.build(alterationFilter));
        assertEquals(2, alterationCountByGenes.size());

        // Test cna count for akt1 filtering for AMP
        var testAKT1AlterationCount = alterationCountByGenes.stream().filter(a -> Objects.equals(a.getHugoGeneSymbol(), "AKT1"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
        assertEquals(2, testAKT1AlterationCount);
    }
}
