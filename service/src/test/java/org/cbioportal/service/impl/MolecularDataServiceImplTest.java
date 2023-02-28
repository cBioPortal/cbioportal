package org.cbioportal.service.impl;

import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MolecularDataServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MolecularDataServiceImpl molecularDataService;

    @Mock
    private MolecularDataRepository molecularDataRepository;
    @Mock
    private DiscreteCopyNumberRepository discreteCopyNumberRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService molecularProfileService;
    @Mock
    private SampleListRepository sampleListRepository;

    @Test
    public void getMolecularData() throws Exception {
        
        when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
            .thenReturn(Arrays.asList(SAMPLE_ID1));
        
        MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");
        
        when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfileSamples);

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
        when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);
        
        List<Sample> sampleList = new ArrayList<>();
        Sample sample = new Sample();
        sample.setInternalId(1);
        sample.setStableId(SAMPLE_ID1);
        sampleList.add(sample);
        when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
            .thenReturn(sampleList);

        List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
        GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
        molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
        molecularAlteration.setValues("0.4674,-0.3456");
        molecularAlterationList.add(molecularAlteration);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        when(molecularDataRepository.getGeneMolecularAlterations(MOLECULAR_PROFILE_ID, entrezGeneIds, 
            PROJECTION)).thenReturn(molecularAlterationList);

        List<GeneMolecularData> result = molecularDataService.getMolecularData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            entrezGeneIds, PROJECTION);

        Assert.assertEquals(1, result.size());
        GeneMolecularData molecularData = result.get(0);
        Assert.assertEquals(ENTREZ_GENE_ID_1, molecularData.getEntrezGeneId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, molecularData.getMolecularProfileId());
        Assert.assertEquals(SAMPLE_ID1, molecularData.getSampleId());
        Assert.assertEquals("0.4674", molecularData.getValue());
    }

    @Test
    public void getMetaMolecularData() throws Exception {

        when(sampleListRepository.getAllSampleIdsInSampleList(SAMPLE_LIST_ID))
            .thenReturn(Arrays.asList(SAMPLE_ID1));

        MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");
        
        when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfileSamples);

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setCancerStudyIdentifier(STUDY_ID);
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
        when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        List<Sample> sampleList = new ArrayList<>();
        Sample sample = new Sample();
        sample.setInternalId(1);
        sample.setStableId(SAMPLE_ID1);
        sampleList.add(sample);
        when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
            .thenReturn(sampleList);

        List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
        GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
        molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
        molecularAlteration.setValues("0.4674,-0.3456");
        molecularAlterationList.add(molecularAlteration);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        when(molecularDataRepository.getGeneMolecularAlterations(MOLECULAR_PROFILE_ID, entrezGeneIds, "ID"))
            .thenReturn(molecularAlterationList);

        BaseMeta result = molecularDataService.getMetaMolecularData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            entrezGeneIds);
        
        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getMolecularDataOfAllSamplesOfMolecularProfile() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
        when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");
        
        when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfileSamples);

        List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
        GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
        molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
        molecularAlteration.setValues("0.4674,-0.3456");
        molecularAlterationList.add(molecularAlteration);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        when(molecularDataRepository.getGeneMolecularAlterations(MOLECULAR_PROFILE_ID, entrezGeneIds, 
            PROJECTION)).thenReturn(molecularAlterationList);
        
        List<Integer> internalIds = new ArrayList<>();
        internalIds.add(1);
        internalIds.add(2);
        
        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setInternalId(1);
        sample1.setStableId(SAMPLE_ID1);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setInternalId(2);
        sample2.setStableId("sample_id_2");
        samples.add(sample2);
        when(sampleService.getSamplesByInternalIds(internalIds)).thenReturn(samples);

        List<GeneMolecularData> result = molecularDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, null, 
            entrezGeneIds, PROJECTION);

        Assert.assertEquals(2, result.size());
        GeneMolecularData molecularData1 = result.get(0);
        Assert.assertEquals(ENTREZ_GENE_ID_1, molecularData1.getEntrezGeneId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, molecularData1.getMolecularProfileId());
        Assert.assertEquals(SAMPLE_ID1, molecularData1.getSampleId());
        Assert.assertEquals("0.4674", molecularData1.getValue());
        GeneMolecularData molecularData2 = result.get(1);
        Assert.assertEquals(ENTREZ_GENE_ID_1, molecularData2.getEntrezGeneId());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, molecularData2.getMolecularProfileId());
        Assert.assertEquals("sample_id_2", molecularData2.getSampleId());
        Assert.assertEquals("-0.3456", molecularData2.getValue());
    }

    @Test
    public void fetchMetaMolecularData() throws Exception {

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
        when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);

        MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");
        
        when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfileSamples);

        List<GeneMolecularAlteration> molecularAlterationList = new ArrayList<>();
        GeneMolecularAlteration molecularAlteration = new GeneMolecularAlteration();
        molecularAlteration.setEntrezGeneId(ENTREZ_GENE_ID_1);
        molecularAlteration.setValues("0.4674,-0.3456");
        molecularAlterationList.add(molecularAlteration);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        when(molecularDataRepository.getGeneMolecularAlterations(MOLECULAR_PROFILE_ID, entrezGeneIds, "ID"))
            .thenReturn(molecularAlterationList);

        List<Integer> internalIds = new ArrayList<>();
        internalIds.add(1);
        internalIds.add(2);

        List<Sample> samples = new ArrayList<>();
        Sample sample1 = new Sample();
        sample1.setInternalId(1);
        sample1.setStableId(SAMPLE_ID1);
        samples.add(sample1);
        Sample sample2 = new Sample();
        sample2.setInternalId(2);
        sample2.setStableId("sample_id_2");
        samples.add(sample2);
        when(sampleService.getSamplesByInternalIds(internalIds)).thenReturn(samples);

        BaseMeta result = molecularDataService.fetchMetaMolecularData(MOLECULAR_PROFILE_ID, null, entrezGeneIds);

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }

    @Test
    public void getNumberOfSamplesInMolecularProfile() throws Exception {

        MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");
        
        when(molecularDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID))
            .thenReturn(molecularProfileSamples);
        
        Integer result = molecularDataService.getNumberOfSamplesInMolecularProfile(MOLECULAR_PROFILE_ID);
        
        Assert.assertEquals((Integer) 2, result);
    }

    @Test
    public void getMolecularDataInMultipleMolecularProfilesByGeneQueries() {

        // two record come in ..
        List<GeneMolecularData> unfilteredData = new ArrayList<>();
        GeneMolecularData geneMolecularData1 = new GeneMolecularData();
        geneMolecularData1.setEntrezGeneId(1);
        geneMolecularData1.setMolecularProfileId("profile1");
        geneMolecularData1.setValue("-2");
        geneMolecularData1.setSampleId("sample1");
        GeneMolecularData geneMolecularData2 = new GeneMolecularData();
        geneMolecularData2.setEntrezGeneId(1);
        geneMolecularData2.setMolecularProfileId("profile1");
        geneMolecularData2.setValue("-1");
        geneMolecularData2.setSampleId("sample2");
        unfilteredData.add(geneMolecularData1);
        unfilteredData.add(geneMolecularData2);
        
        MolecularDataServiceImpl spy = spy(molecularDataService);
        doReturn(unfilteredData).when(spy).getMolecularDataInMultipleMolecularProfiles(anyList(), anyList(), anyList(), anyString());

        List<DiscreteCopyNumberData> selectedCnaEvents = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData1 = new DiscreteCopyNumberData();
        discreteCopyNumberData1.setEntrezGeneId(1);
        discreteCopyNumberData1.setMolecularProfileId("profile1");
        discreteCopyNumberData1.setAlteration(-2);
        discreteCopyNumberData1.setSampleId("sample1");
        selectedCnaEvents.add(discreteCopyNumberData1);

        when(discreteCopyNumberRepository.getDiscreteCopyNumbersInMultipleMolecularProfilesByGeneQueries(anyList(), anyList(), anyList(), anyString())).thenReturn(selectedCnaEvents);

        List<GeneMolecularData> filteredData = spy.getMolecularDataInMultipleMolecularProfilesByGeneQueries(Arrays.asList(), Arrays.asList(), Arrays.asList(), "projection");
        
        // one record comes out ...
        // so, test whether record correctly removed from result set
        Assert.assertEquals(1, filteredData.size());
        Assert.assertEquals("sample1", filteredData.get(0).getSampleId());

    }
}