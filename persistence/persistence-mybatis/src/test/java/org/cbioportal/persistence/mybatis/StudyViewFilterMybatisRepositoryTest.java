package org.cbioportal.persistence.mybatis;

import junit.framework.TestCase;
import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;
import org.cbioportal.persistence.StudyViewFilterRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class StudyViewFilterMybatisRepositoryTest extends TestCase {
    
    @Autowired
    private StudyViewFilterRepository subject;

    @Test
    public void shouldFilterByGenePanel() {
        StudyViewGenePanel panel = new StudyViewGenePanel();
        panel.setGenePanelId("TESTPANEL1");
        panel.setMolecularProfileId("study_tcga_pub_gistic");

        Set<SampleIdentifier> actual = subject.getSampleIdentifiersForPanels(Arrays.asList(panel));
        
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setStudyId("study_tcga_pub");
        sampleIdentifier.setSampleId("TCGA-A1-A0SB-01");
        Set<SampleIdentifier> expected = new HashSet<>(Arrays.asList(sampleIdentifier));
        assertEquals(expected, actual);
    }

    @Test
    public void shouldFilterEntirely() {
        StudyViewGenePanel panel = new StudyViewGenePanel();
        panel.setGenePanelId("fake panel");
        panel.setMolecularProfileId("asdf");

        Set<SampleIdentifier> actual = subject.getSampleIdentifiersForPanels(Arrays.asList(panel));
        
        Set<SampleIdentifier> expected = new HashSet<>();
        assertEquals(expected, actual);
    }

    @Test
    public void shouldNotFilterEmptyList() {
        Set<SampleIdentifier> actual = subject.getSampleIdentifiersForPanels(new ArrayList<>());

        SampleIdentifier siA = new SampleIdentifier();
        siA.setStudyId("study_tcga_pub");
        siA.setSampleId("TCGA-A1-A0SD-01");
        SampleIdentifier siB = new SampleIdentifier();
        siB.setStudyId("study_tcga_pub");
        siB.setSampleId("TCGA-A1-A0SB-01");
        Set<SampleIdentifier> expected = new HashSet<>(Arrays.asList(siA, siB));
        assertEquals(expected, actual);
    }
}