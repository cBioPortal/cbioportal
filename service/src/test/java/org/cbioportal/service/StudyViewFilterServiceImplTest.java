package org.cbioportal.service;

import junit.framework.TestCase;
import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;
import org.cbioportal.persistence.StudyViewFilterRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(MockitoJUnitRunner.class)
public class StudyViewFilterServiceImplTest extends TestCase {

    @InjectMocks
    StudyViewFilterServiceImpl subject;
    
    @Mock
    StudyViewFilterRepository repository;

    @Test
    public void shouldGetSampleIds() {
        Set<SampleIdentifier> ids = IntStream.range(1, 3).boxed()
            .map(i -> {
                SampleIdentifier id = new SampleIdentifier();
                id.setSampleId("s_" + i);
                id.setStudyId("s1");
                return id;
            })
            .collect(Collectors.toSet());
        Mockito.when(repository.getSampleIdentifiersForPanels(Mockito.anyList()))
            .thenReturn(ids);

        StudyViewGenePanel panel = new StudyViewGenePanel();
        panel.setGenePanelId("mutations");
        panel.setMolecularProfileId("s1_mutations");

        Set<SampleIdentifier> actual = subject.getSampleIdentifiersForPanels(Arrays.asList(panel));
        
        assertEquals(ids, actual);
    }
}