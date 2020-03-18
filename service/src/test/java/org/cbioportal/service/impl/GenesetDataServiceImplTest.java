package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileSamples;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GenesetDataServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private GenesetDataServiceImpl genesetDataService;

    @Mock
    private MolecularDataRepository geneticDataRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService geneticProfileService;

    /**
     * This is executed n times, for each of the n test methods below:
     * @throws Exception 
     * @throws DaoException
     */
    @Before 
    public void setUp() throws Exception {
        
        MolecularProfileSamples molecularProfileSamples = new MolecularProfileSamples();
        molecularProfileSamples.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularProfileSamples.setCommaSeparatedSampleIds("1,2,");
        
        //stub for samples
        Mockito.when(geneticDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfileSamples);

        List<Sample> sampleList1 = new ArrayList<>();
        Sample sample = new Sample();
        sample.setInternalId(1);
        sample.setStableId(SAMPLE_ID1);
        sampleList1.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
            .thenReturn(sampleList1);
        List<Sample> sampleListAll = new ArrayList<>(sampleList1);
        sample = new Sample();
        sample.setInternalId(2);
        sample.setStableId(SAMPLE_ID2);
        sampleListAll.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID"))
            .thenReturn(sampleListAll);

        //stub for genetic profile
        MolecularProfile geneticProfile = new MolecularProfile();
        geneticProfile.setCancerStudyIdentifier(STUDY_ID);
        Mockito.when(geneticProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(geneticProfile);

        //stub for repository data
        List<GenesetMolecularAlteration> genesetGeneticAlterationList = new ArrayList<>();
        GenesetMolecularAlteration genesetGeneticAlteration = new GenesetMolecularAlteration();
        genesetGeneticAlteration.setGenesetId(GENESET_ID1);
        genesetGeneticAlteration.setValues("0.2,0.499");
        genesetGeneticAlterationList.add(genesetGeneticAlteration);
        genesetGeneticAlteration = new GenesetMolecularAlteration();
        genesetGeneticAlteration.setGenesetId(GENESET_ID2);
        genesetGeneticAlteration.setValues("0.89,-0.509");
        genesetGeneticAlterationList.add(genesetGeneticAlteration);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        Mockito.when(geneticDataRepository.getGenesetMolecularAlterations(MOLECULAR_PROFILE_ID, Arrays.asList(GENESET_ID1, GENESET_ID2), "SUMMARY"))
            .thenReturn(genesetGeneticAlterationList);
    }


    @Test
    public void fetchGenesetData() throws Exception {

        List<GenesetMolecularData> result = genesetDataService.fetchGenesetData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
                Arrays.asList(GENESET_ID1, GENESET_ID2));

        //what we expect: 2 samples x 2 geneset items = 4 GenesetData items:
        //SAMPLE_1:
        //   geneset1 value: 0.2
        //   geneset2 value: 0.89
        //SAMPLE_2:
        //   geneset1 value: 0.499
        //   geneset2 value: -0.509
        Assert.assertEquals(4, result.size());
        GenesetMolecularData item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getGenesetId(), GENESET_ID1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        GenesetMolecularData item2 = result.get(1);
        Assert.assertEquals(item2.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item2.getGenesetId(), GENESET_ID2);
        Assert.assertEquals(item2.getValue(), "0.89");
        Assert.assertEquals(item2.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        GenesetMolecularData item4 = result.get(3);
        Assert.assertEquals(item4.getSampleId(), SAMPLE_ID2);
        Assert.assertEquals(item4.getGenesetId(), GENESET_ID2);
        Assert.assertEquals(item4.getValue(), "-0.509");
        Assert.assertEquals(item4.getMolecularProfileId(), MOLECULAR_PROFILE_ID);

        //check when selecting only 1 sample:
        result = genesetDataService.fetchGenesetData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
                Arrays.asList(GENESET_ID1, GENESET_ID2));
        Assert.assertEquals(2, result.size());
        item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getGenesetId(), GENESET_ID1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
    }
}
