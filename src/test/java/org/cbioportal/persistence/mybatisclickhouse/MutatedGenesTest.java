package org.cbioportal.persistence.mybatisclickhouse;

import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.MutationEventType;
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
public class MutatedGenesTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getMutatedGenes() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));
        var alterationCountByGenes = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
        assertEquals(3, alterationCountByGenes.size());

        var testBrca1AlterationCount = alterationCountByGenes.stream().filter(a -> Objects.equals(a.getHugoGeneSymbol(), "BRCA1")).findFirst();
        assert (testBrca1AlterationCount.isPresent());
        assertEquals(Integer.valueOf(5), testBrca1AlterationCount.get().getTotalCount());
    }

    @Test
    public void getMutatedGenesWithAlterationFilter() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB));

        // Create AlterationFilter
        AlterationFilter alterationFilter = new AlterationFilter();
        Map<MutationEventType, Boolean> mutationEventTypeFilterMap = new HashMap<>();
        mutationEventTypeFilterMap.put(MutationEventType.nonsense_mutation, Boolean.TRUE);
        mutationEventTypeFilterMap.put(MutationEventType.other, Boolean.FALSE);
        alterationFilter.setMutationEventTypes(mutationEventTypeFilterMap);

        var alterationCountByGenes = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            AlterationFilterHelper.build(alterationFilter));
        assertEquals(2, alterationCountByGenes.size());

        AlterationFilter onlyMutationStatusFilter = new AlterationFilter();
        onlyMutationStatusFilter.setMutationEventTypes(new HashMap<>());
        onlyMutationStatusFilter.setIncludeGermline(false);
        onlyMutationStatusFilter.setIncludeSomatic(false);
        onlyMutationStatusFilter.setIncludeUnknownStatus(true);

        var alterationCountByGenes1 = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            AlterationFilterHelper.build(onlyMutationStatusFilter));
        assertEquals(1, alterationCountByGenes1.size());

        AlterationFilter mutationTypeAndStatusFilter = new AlterationFilter();
        mutationTypeAndStatusFilter.setMutationEventTypes(mutationEventTypeFilterMap);
        mutationTypeAndStatusFilter.setMutationEventTypes(new HashMap<>());
        mutationTypeAndStatusFilter.setIncludeGermline(false);
        mutationTypeAndStatusFilter.setIncludeSomatic(false);
        mutationTypeAndStatusFilter.setIncludeUnknownStatus(true);

        var alterationCountByGenes2 = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            AlterationFilterHelper.build(onlyMutationStatusFilter));
        assertEquals(1, alterationCountByGenes2.size());

        // Testing custom driver filter
        AlterationFilter onlyDriverFilter = new AlterationFilter();
        onlyDriverFilter.setIncludeDriver(true);
        onlyDriverFilter.setIncludeVUS(false);
        onlyDriverFilter.setIncludeUnknownOncogenicity(false);

        var alterationCountByGenes3 = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null, null),
            AlterationFilterHelper.build(onlyDriverFilter));
        assertEquals(2, alterationCountByGenes3.size());

        var akt1AlteredCounts3 = alterationCountByGenes3.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT1"))
            .mapToInt(c -> c.getNumberOfAlteredCases().intValue())
            .sum();
        assertEquals(1, akt1AlteredCounts3);
        var akt2AlteredCounts3 = alterationCountByGenes3.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT2"))
            .mapToInt(c -> c.getNumberOfAlteredCases().intValue())
            .sum();
        assertEquals(0, akt2AlteredCounts3);
        var brca1AlteredCounts3 = alterationCountByGenes3.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA1"))
            .mapToInt(c -> c.getNumberOfAlteredCases().intValue())
            .sum();
        assertEquals(3, brca1AlteredCounts3);

        AlterationFilter onlyVUSFilter = new AlterationFilter();
        onlyVUSFilter.setIncludeDriver(false);
        onlyVUSFilter.setIncludeVUS(true);
        onlyVUSFilter.setIncludeUnknownOncogenicity(false);

        var alterationCountByGenes4 = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null, null),
            AlterationFilterHelper.build(onlyVUSFilter));
        assertEquals(3, alterationCountByGenes4.size());

        var akt1AlteredCounts4 = alterationCountByGenes4.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT1"))
            .mapToInt(c -> c.getNumberOfAlteredCases().intValue())
            .sum();
        assertEquals(1, akt1AlteredCounts4);
        var akt2AlteredCounts4 = alterationCountByGenes4.stream().filter(c -> c.getHugoGeneSymbol().equals("AKT2"))
            .mapToInt(c -> c.getNumberOfAlteredCases().intValue())
            .sum();
        assertEquals(1, akt2AlteredCounts4);
        var brca1AlteredCounts4 = alterationCountByGenes4.stream().filter(c -> c.getHugoGeneSymbol().equals("BRCA1"))
            .mapToInt(c -> c.getNumberOfAlteredCases().intValue())
            .sum();
        assertEquals(2, brca1AlteredCounts4);

        AlterationFilter onlyUnknownOncogenicityFilter = new AlterationFilter();
        onlyUnknownOncogenicityFilter.setIncludeDriver(false);
        onlyUnknownOncogenicityFilter.setIncludeVUS(false);
        onlyUnknownOncogenicityFilter.setIncludeUnknownOncogenicity(true);

        var alterationCountByGenes5 = studyViewMapper.getMutatedGenes(StudyViewFilterHelper.build(studyViewFilter, null, null),
            AlterationFilterHelper.build(onlyUnknownOncogenicityFilter));
        assertEquals(0, alterationCountByGenes5.size());
    }
}
