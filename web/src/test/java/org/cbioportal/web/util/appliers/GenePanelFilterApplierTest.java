package org.cbioportal.web.util.appliers;

import org.cbioportal.model.GenePanelFilter;
import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.model.StudyViewGenePanel;
import org.cbioportal.service.StudyViewFilterService;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenePanelFilterApplierTest {
    @Mock
    StudyViewFilterService filterService;
    
    @InjectMocks
    GenePanelFilterApplier subject;


    @Test
    public void shouldSayToApplyFilter() {
        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(Collections.singletonList(new GenePanelFilter()));

        Assert.assertTrue(subject.shouldApplyFilter(filter));
    }

    @Test
    public void shouldSayNoToApplyFilter() {
        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(null);

        Assert.assertFalse(subject.shouldApplyFilter(filter));
        
        filter.setGenePanelFilters(new ArrayList<>());
        Assert.assertFalse(subject.shouldApplyFilter(filter));
    }

    @Test
    public void shouldIncludeSampleWithMatchingGenePanel() {
        StudyViewFilter filter = new StudyViewFilter();
        GenePanelFilter jean = new GenePanelFilter();
        jean.setGenePanelId("gp1");
        jean.setMolecularProfileSuffix("mutations");
        filter.setGenePanelFilters(Collections.singletonList(jean));

        StudyViewGenePanel panel = new StudyViewGenePanel();
        panel.setMolecularProfileId("study1_mutations");
        panel.setGenePanelId("gp1");

        SampleIdentifier sample = new SampleIdentifier();
        sample.setSampleId("sample1");
        sample.setStudyId("study1");

        Mockito
            .when(filterService.getSampleIdentifiersForPanels(Collections.singletonList(panel)))
            .thenReturn(new HashSet<>(Collections.singletonList(sample)));

        List<SampleIdentifier> actual = subject.filter(Collections.singletonList(sample), filter);
        List<SampleIdentifier> expected = Collections.singletonList(sample);
        
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldExcludeSampleWithNoMatchingGenePanel() {
        StudyViewFilter filter = new StudyViewFilter();
        GenePanelFilter jean = new GenePanelFilter();
        jean.setGenePanelId("gp1");
        jean.setMolecularProfileSuffix("mutations");
        filter.setGenePanelFilters(Collections.singletonList(jean));

        StudyViewGenePanel panel = new StudyViewGenePanel();
        panel.setMolecularProfileId("study1_mutations");
        panel.setGenePanelId("gp1");

        SampleIdentifier sample = new SampleIdentifier();
        sample.setSampleId("sample1");
        sample.setStudyId("study1");

        Mockito
            .when(filterService.getSampleIdentifiersForPanels(Collections.singletonList(panel)))
            .thenReturn(new HashSet<>(Collections.emptyList()));

        List<SampleIdentifier> actual = subject.filter(Collections.singletonList(sample), filter);
        List<SampleIdentifier> expected = Collections.emptyList();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldSupportMultiStudyQueries() {
        StudyViewFilter filter = new StudyViewFilter();
        GenePanelFilter jean = new GenePanelFilter();
        jean.setGenePanelId("gp1");
        jean.setMolecularProfileSuffix("mutations");
        filter.setGenePanelFilters(Collections.singletonList(jean));

        StudyViewGenePanel panel = new StudyViewGenePanel();
        panel.setMolecularProfileId("study1_mutations");
        panel.setGenePanelId("gp1");

        SampleIdentifier sample1 = new SampleIdentifier();
        sample1.setSampleId("sample1");
        sample1.setStudyId("study1");

        SampleIdentifier sample2 = new SampleIdentifier();
        sample2.setSampleId("sample2");
        sample2.setStudyId("study1");

        Mockito
            .when(filterService.getSampleIdentifiersForPanels(Collections.singletonList(panel)))
            .thenReturn(new HashSet<>(Collections.singletonList(sample1)));

        List<SampleIdentifier> actual = subject.filter(Arrays.asList(sample1, sample2), filter);
        List<SampleIdentifier> expected = Collections.singletonList(sample1);

        Assert.assertEquals(expected, actual);
    }
}