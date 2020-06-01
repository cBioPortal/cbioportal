package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberCount;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DiscreteCopyNumberServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private DiscreteCopyNumberServiceImpl discreteCopyNumberService;
    
    @Mock
    private DiscreteCopyNumberRepository discreteCopyNumberRepository;
    @Mock
    private MolecularDataService molecularDataService;
    @Mock
    private MolecularProfileService molecularProfileService;
    
    @Test
    public void getDiscreteCopyNumbersInMolecularProfileBySampleListIdHomdelOrAmp() throws Exception {

        createMolecularProfile();

        List<DiscreteCopyNumberData> expectedDiscreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        expectedDiscreteCopyNumberDataList.add(discreteCopyNumberData);
        
        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);
        
        Mockito.when(discreteCopyNumberRepository.getDiscreteCopyNumbersInMolecularProfileBySampleListId(
            MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes, PROJECTION))
            .thenReturn(expectedDiscreteCopyNumberDataList);
        
        List<DiscreteCopyNumberData> result = discreteCopyNumberService
            .getDiscreteCopyNumbersInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
                Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes, PROJECTION);

        Assert.assertEquals(expectedDiscreteCopyNumberDataList, result);
    }

    @Test
    public void getDiscreteCopyNumbersInMolecularProfileBySampleListIdNonHomdelOrAmp() throws Exception {

        createMolecularProfile();
        
        List<GeneMolecularData> expectedMolecularDataList = new ArrayList<>();
        GeneMolecularData molecularData = new GeneMolecularData();
        molecularData.setValue("-1");
        molecularData.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData.setSampleId(SAMPLE_ID1);
        molecularData.setEntrezGeneId(ENTREZ_GENE_ID_1);
        Gene gene = new Gene(); 
        molecularData.setGene(gene);
        expectedMolecularDataList.add(molecularData);
        
        Mockito.when(molecularDataService.getMolecularData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1), PROJECTION)).thenReturn(expectedMolecularDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);
        
        List<DiscreteCopyNumberData> result = discreteCopyNumberService
            .getDiscreteCopyNumbersInMolecularProfileBySampleListId(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
                Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes, PROJECTION);
        
        Assert.assertEquals(1, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals((Integer) (-1), discreteCopyNumberData.getAlteration());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, discreteCopyNumberData.getMolecularProfileId());
        Assert.assertEquals(SAMPLE_ID1, discreteCopyNumberData.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID_1, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals(gene, discreteCopyNumberData.getGene());
    }

    @Test
    public void getMetaDiscreteCopyNumbersInMolecularProfileBySampleListIdHomdelOrAmp() throws Exception {

        createMolecularProfile();

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(discreteCopyNumberRepository.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
            MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes))
            .thenReturn(expectedBaseMeta);

        BaseMeta result = discreteCopyNumberService.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
            MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes);
        
        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getMetaDiscreteCopyNumbersInMolecularProfileBySampleListIdNonHomdelOrAmp() throws Exception {

        createMolecularProfile();

        List<GeneMolecularData> expectedMolecularDataList = new ArrayList<>();
        GeneMolecularData molecularData = new GeneMolecularData();
        molecularData.setValue("-1");
        expectedMolecularDataList.add(molecularData);

        Mockito.when(molecularDataService.getMolecularData(MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, 
            Arrays.asList(ENTREZ_GENE_ID_1), "ID")).thenReturn(expectedMolecularDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);

        BaseMeta result = discreteCopyNumberService.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
            MOLECULAR_PROFILE_ID, SAMPLE_LIST_ID, Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes);
        
        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void fetchDiscreteCopyNumbersInMolecularProfileHomdelOrAmp() throws Exception {

        createMolecularProfile();

        List<DiscreteCopyNumberData> expectedDiscreteCopyNumberDataList = new ArrayList<>();
        DiscreteCopyNumberData discreteCopyNumberData = new DiscreteCopyNumberData();
        expectedDiscreteCopyNumberDataList.add(discreteCopyNumberData);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);

        Mockito.when(discreteCopyNumberRepository.fetchDiscreteCopyNumbersInMolecularProfile(MOLECULAR_PROFILE_ID, 
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes, PROJECTION))
            .thenReturn(expectedDiscreteCopyNumberDataList);

        List<DiscreteCopyNumberData> result = discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(
            MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes, PROJECTION);

        Assert.assertEquals(expectedDiscreteCopyNumberDataList, result);
    }

    @Test
    public void fetchDiscreteCopyNumbersInMolecularProfileNonHomdelOrAmp() throws Exception {

        createMolecularProfile();

        List<GeneMolecularData> expectedMolecularDataList = new ArrayList<>();
        GeneMolecularData molecularData = new GeneMolecularData();
        molecularData.setValue("-1");
        molecularData.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData.setSampleId(SAMPLE_ID1);
        molecularData.setEntrezGeneId(ENTREZ_GENE_ID_1);
        Gene gene = new Gene();
        molecularData.setGene(gene);
        expectedMolecularDataList.add(molecularData);

        Mockito.when(molecularDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1), 
            Arrays.asList(ENTREZ_GENE_ID_1), PROJECTION)).thenReturn(expectedMolecularDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);

        List<DiscreteCopyNumberData> result = discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(
            MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes, PROJECTION);

        Assert.assertEquals(1, result.size());
        DiscreteCopyNumberData discreteCopyNumberData = result.get(0);
        Assert.assertEquals((Integer) (-1), discreteCopyNumberData.getAlteration());
        Assert.assertEquals(MOLECULAR_PROFILE_ID, discreteCopyNumberData.getMolecularProfileId());
        Assert.assertEquals(SAMPLE_ID1, discreteCopyNumberData.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID_1, discreteCopyNumberData.getEntrezGeneId());
        Assert.assertEquals(gene, discreteCopyNumberData.getGene());
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInMolecularProfileHomdelOrAmp() throws Exception {

        createMolecularProfile();

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-2);

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(discreteCopyNumberRepository.fetchMetaDiscreteCopyNumbersInMolecularProfile(MOLECULAR_PROFILE_ID,
            Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes)).thenReturn(expectedBaseMeta);

        BaseMeta result = discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInMolecularProfile(
            MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes);

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void fetchMetaDiscreteCopyNumbersInMolecularProfileNonHomdelOrAmp() throws Exception {

        createMolecularProfile();

        List<GeneMolecularData> expectedMolecularDataList = new ArrayList<>();
        GeneMolecularData molecularData = new GeneMolecularData();
        molecularData.setValue("-1");
        expectedMolecularDataList.add(molecularData);

        Mockito.when(molecularDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1), 
            Arrays.asList(ENTREZ_GENE_ID_1), "ID")).thenReturn(expectedMolecularDataList);

        List<Integer> alterationTypes = new ArrayList<>();
        alterationTypes.add(-1);

        BaseMeta result = discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInMolecularProfile(
            MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1), Arrays.asList(ENTREZ_GENE_ID_1), alterationTypes);

        Assert.assertEquals((Integer) 1, result.getTotalCount());
    }

    @Test
    public void getSampleCountByGeneAndAlteration() throws Exception {

        createMolecularProfile();
        
        List<CopyNumberCountByGene> expectedCopyNumberSampleCountByGeneList = new ArrayList<>();
        expectedCopyNumberSampleCountByGeneList.add(new CopyNumberCountByGene());

        Mockito.when(discreteCopyNumberRepository.getSampleCountByGeneAndAlterationAndSampleIds(MOLECULAR_PROFILE_ID, 
            null, Arrays.asList(ENTREZ_GENE_ID_1), Arrays.asList(-2)))
            .thenReturn(expectedCopyNumberSampleCountByGeneList);
        
        List<CopyNumberCountByGene> result = discreteCopyNumberService
            .getSampleCountByGeneAndAlterationAndSampleIds(MOLECULAR_PROFILE_ID, null, Arrays.asList(ENTREZ_GENE_ID_1), 
                Arrays.asList(-2));
        
        Assert.assertEquals(expectedCopyNumberSampleCountByGeneList, result);
    }

    @Test
    public void getPatientCountInMultipleMolecularProfiles() throws Exception {
        
        List<CopyNumberCountByGene> expectedCopyNumberSampleCountByGeneList = new ArrayList<>();
        expectedCopyNumberSampleCountByGeneList.add(new CopyNumberCountByGene());

        Mockito.when(discreteCopyNumberRepository.getPatientCountInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID), 
            null, Arrays.asList(ENTREZ_GENE_ID_1), Arrays.asList(-2)))
            .thenReturn(expectedCopyNumberSampleCountByGeneList);
        
        List<CopyNumberCountByGene> result = discreteCopyNumberService
            .getPatientCountInMultipleMolecularProfiles(Arrays.asList(MOLECULAR_PROFILE_ID), null, Arrays.asList(ENTREZ_GENE_ID_1), 
                Arrays.asList(-2), false, false);
        
        Assert.assertEquals(expectedCopyNumberSampleCountByGeneList, result);
    }

    @Test
    public void fetchCopyNumberCounts() throws Exception {

        List<CopyNumberCountByGene> copyNumberSampleCountByGeneList = new ArrayList<>();
        CopyNumberCountByGene copyNumberSampleCountByGene = new CopyNumberCountByGene();
        copyNumberSampleCountByGene.setEntrezGeneId(ENTREZ_GENE_ID_1);
        copyNumberSampleCountByGene.setAlteration(-2);
        copyNumberSampleCountByGene.setNumberOfAlteredCases(1);
        copyNumberSampleCountByGeneList.add(copyNumberSampleCountByGene);

        createMolecularProfile();

        Mockito.when(molecularDataService.getNumberOfSamplesInMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(2);

        Mockito.when(discreteCopyNumberService.getSampleCountByGeneAndAlterationAndSampleIds(MOLECULAR_PROFILE_ID, null,
            Arrays.asList(ENTREZ_GENE_ID_1), Arrays.asList(-2))).thenReturn(copyNumberSampleCountByGeneList);

        List<CopyNumberCount> result = discreteCopyNumberService.fetchCopyNumberCounts(MOLECULAR_PROFILE_ID,
            Arrays.asList(ENTREZ_GENE_ID_1), Arrays.asList(-2));

        Assert.assertEquals(1, result.size());
        CopyNumberCount copyNumberCount = result.get(0);
        Assert.assertEquals(MOLECULAR_PROFILE_ID, copyNumberCount.getMolecularProfileId());
        Assert.assertEquals(ENTREZ_GENE_ID_1, copyNumberCount.getEntrezGeneId());
        Assert.assertEquals((Integer) (-2), copyNumberCount.getAlteration());
        Assert.assertEquals((Integer) 2, copyNumberCount.getNumberOfSamples());
        Assert.assertEquals((Integer) 1, copyNumberCount.getNumberOfSamplesWithAlterationInGene());
    }

    private void createMolecularProfile() throws MolecularProfileNotFoundException {
        
        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION);
        molecularProfile.setDatatype("DISCRETE");
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);
    }
}
