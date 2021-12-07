package org.cbioportal.web.util.appliers;

import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelFilter;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GenePanelFilterApplierTest {
    @Mock
    GenePanelService genePanelService;
    
    @InjectMocks
    GenePanelFilterApplier subject;

    @Test
    public void shouldNotFilterAnySamples() {
        GenePanelFilter genePanelFilter = new GenePanelFilter();
        genePanelFilter.setGenePanel("gene_panel_1");
        genePanelFilter.setMolecularProfileIds(Collections.singletonList("study_1_cna"));
        
        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(Collections.singletonList(genePanelFilter));

        Mockito.when(genePanelService.fetchGenePanelDataByMolecularProfileIds(new HashSet<>(Collections.singletonList("study_1_cna"))))
            .thenReturn(Collections.singletonList(genePanelData("gene_panel_1", "sample_1", "study_1")));
        
        List<SampleIdentifier> toFilter = Collections.singletonList(sampleIdentifier("sample_1", "study_1"));

        List<SampleIdentifier> actual = subject.filter(toFilter, filter);
        
        Assert.assertEquals(toFilter, actual);
    }

    @Test
    public void shouldFilterSampleWithMismatchedSampleId() {
        GenePanelFilter genePanelFilter = new GenePanelFilter();
        genePanelFilter.setGenePanel("gene_panel_1");
        genePanelFilter.setMolecularProfileIds(Collections.singletonList("study_1_cna"));

        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(Collections.singletonList(genePanelFilter));

        Mockito.when(genePanelService.fetchGenePanelDataByMolecularProfileIds(new HashSet<>(Collections.singletonList("study_1_cna"))))
            .thenReturn(Collections.singletonList(genePanelData("gene_panel_1", "sample_1", "study_1")));

        List<SampleIdentifier> toFilter = Collections.singletonList(sampleIdentifier("sample_2", "study_1"));

        List<SampleIdentifier> actual = subject.filter(toFilter, filter);
        List<SampleIdentifier> expected = new ArrayList<>();

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldFilterSampleWithMismatchedStudyId() {
        GenePanelFilter genePanelFilter = new GenePanelFilter();
        genePanelFilter.setGenePanel("gene_panel_1");
        genePanelFilter.setMolecularProfileIds(Collections.singletonList("study_1_cna"));

        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(Collections.singletonList(genePanelFilter));

        Mockito.when(genePanelService.fetchGenePanelDataByMolecularProfileIds(new HashSet<>(Collections.singletonList("study_1_cna"))))
            .thenReturn(Collections.singletonList(genePanelData("gene_panel_1", "sample_1", "study_1")));

        List<SampleIdentifier> toFilter = Collections.singletonList(sampleIdentifier("sample_1", "study_2"));

        List<SampleIdentifier> actual = subject.filter(toFilter, filter);
        List<SampleIdentifier> expected = new ArrayList<>();

        Assert.assertEquals(expected, actual);
    }
    
    private SampleIdentifier sampleIdentifier(String sampleId, String studyId) {
        SampleIdentifier sampleIdentifier = new SampleIdentifier();
        sampleIdentifier.setSampleId(sampleId);
        sampleIdentifier.setStudyId(studyId);
        return sampleIdentifier;
    }
    
    private GenePanelData genePanelData(String genePanelId, String sampleId, String studyId) {
        GenePanelData genePanelData = new GenePanelData();
        genePanelData.setGenePanelId(genePanelId);
        genePanelData.setSampleId(sampleId);
        genePanelData.setStudyId(studyId);
        return genePanelData;
    }

    @Test
    public void shouldNotApplyNullFilter() {
        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(null);

        boolean actual = subject.shouldApplyFilter(filter);
        boolean expected = false;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldNotApplyEmptyFilter() {
        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(new ArrayList<>());

        boolean actual = subject.shouldApplyFilter(filter);
        boolean expected = false;

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldApplyFilter() {
        StudyViewFilter filter = new StudyViewFilter();
        filter.setGenePanelFilters(Collections.singletonList(new GenePanelFilter()));

        boolean actual = subject.shouldApplyFilter(filter);
        boolean expected = true;

        Assert.assertEquals(expected, actual);
    }
}