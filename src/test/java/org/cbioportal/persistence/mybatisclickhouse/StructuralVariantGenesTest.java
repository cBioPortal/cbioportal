package org.cbioportal.persistence.mybatisclickhouse;

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

import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Import(MyBatisConfig.class)
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractTestcontainers.Initializer.class)
public class StructuralVariantGenesTest extends AbstractTestcontainers {

    private static final String STUDY_TCGA_PUB = "study_tcga_pub";
    private static final String STUDY_ACC_TCGA = "acc_tcga";

    @Autowired
    private StudyViewMapper studyViewMapper;

    @Test
    public void getStructuralVariantGenes() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();
        studyViewFilter.setStudyIds(List.of(STUDY_TCGA_PUB, STUDY_ACC_TCGA));
        var alterationCountByGenes = studyViewMapper.getStructuralVariantGenes(StudyViewFilterHelper.build(studyViewFilter, null, null, studyViewFilter.getStudyIds()),
            AlterationFilterHelper.build(studyViewFilter.getAlterationFilter()));
        assertEquals(8, alterationCountByGenes.size());
        
        // Test sv count for eml4 which is in one study
        var testeml4AlterationCount = alterationCountByGenes.stream().filter(a -> Objects.equals(a.getHugoGeneSymbol(), "eml4"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
        assertEquals(1, testeml4AlterationCount);

        // Test sv count for ncoa4 which is in both studies
        var testncoa4AlterationCount = alterationCountByGenes.stream().filter(a -> Objects.equals(a.getHugoGeneSymbol(), "ncoa4"))
            .mapToInt(c -> c.getTotalCount().intValue())
            .sum();
        assertEquals(3, testncoa4AlterationCount);
    }
}
