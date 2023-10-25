package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.model.GenesetMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.service.GenesetDataService;
import org.cbioportal.service.GenesetService;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GenesetCorrelationServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private GenesetCorrelationServiceImpl genesetCorrelationService;

    @Mock
    private GenesetDataService genesetDataService;
    @Mock
    private MolecularDataService geneticDataService;
    @Mock
    private GenesetService genesetService;
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

        //stub for geneset gene list:
        List<Gene> geneList = new ArrayList<>();
        Gene gene = new Gene();
        gene.setEntrezGeneId(1);
        geneList.add(gene);
        gene = new Gene();
        gene.setEntrezGeneId(2);
        geneList.add(gene);
        Mockito.when(genesetService.getGenesByGenesetId(GENESET_ID1))
            .thenReturn(geneList);

        //stub for geneset data list:
        List<GenesetMolecularData> genesetDataList1 = new ArrayList<GenesetMolecularData>();
        genesetDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID1, GENESET_ID1, "0.2"));
        genesetDataList1.add(getSimpleFlatGenesetDataItem(SAMPLE_ID2, GENESET_ID1, "0.499"));
        Mockito.when(genesetDataService.fetchGenesetData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
                Arrays.asList(GENESET_ID1)))
            .thenReturn(genesetDataList1);
        //simulate 1 empty sample:
        Mockito.when(genesetDataService.fetchGenesetData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
                Arrays.asList(GENESET_ID1)))
            .thenReturn(genesetDataList1);
        
        //dummy stubs (normally these will return different profiles, but for the test this is enough:
        MolecularProfile geneticProfile = new MolecularProfile();
        geneticProfile.setStableId(MOLECULAR_PROFILE_ID);
        Mockito.when(geneticProfileService.getMolecularProfilesReferredBy(MOLECULAR_PROFILE_ID))
            .thenReturn(Arrays.asList(geneticProfile));
        MolecularProfile zscoreGeneticProfile = new MolecularProfile();
        zscoreGeneticProfile.setStableId(MOLECULAR_PROFILE_ID);
        zscoreGeneticProfile.setDatatype("Z-SCORE");
        Mockito.when(geneticProfileService.getMolecularProfilesReferringTo(MOLECULAR_PROFILE_ID))
            .thenReturn(Arrays.asList(zscoreGeneticProfile));
        
        //stub for gene data list, one gene at a time:
        List<GeneMolecularData> geneDataList1 = new ArrayList<GeneMolecularData>();
        geneDataList1.add(getSimpleFlatGeneDataItem(SAMPLE_ID1, 1, "0.2"));
        geneDataList1.add(getSimpleFlatGeneDataItem(SAMPLE_ID2, 1, "0.350"));
        Mockito.when(geneticDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
                Arrays.asList(1), "SUMMARY"))
            .thenReturn(geneDataList1);
        //simulate 1 empty sample:
        Mockito.when(geneticDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
                Arrays.asList(1), "SUMMARY"))
            .thenReturn(geneDataList1);

        List<GeneMolecularData> geneDataList2 = new ArrayList<GeneMolecularData>();
        geneDataList2.add(getSimpleFlatGeneDataItem(SAMPLE_ID1, 2, "0.89"));
        geneDataList2.add(getSimpleFlatGeneDataItem(SAMPLE_ID2, 2, "-0.509"));
        Mockito.when(geneticDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
                Arrays.asList(2), "SUMMARY"))
            .thenReturn(geneDataList2);
        //simulate 1 empty sample:
        Mockito.when(geneticDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3),
                Arrays.asList(2), "SUMMARY"))
            .thenReturn(geneDataList2);
    }


    private GenesetMolecularData getSimpleFlatGenesetDataItem(String sampleStableId, String genesetId, String value){
    
        GenesetMolecularData item = new GenesetMolecularData();
        item.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        item.setGenesetId(genesetId);
        item.setSampleId(sampleStableId);
        item.setValue(value);
        return item;
    }
    
    private GeneMolecularData getSimpleFlatGeneDataItem(String sampleStableId, int entrezGeneId, String value){
        
        GeneMolecularData item = new GeneMolecularData();
        item.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        item.setEntrezGeneId(entrezGeneId);
        item.setSampleId(sampleStableId);
        item.setValue(value);
        return item;
    }
    
    @Test
    public void fetchCorrelatedGenes() throws Exception {

        List<GenesetCorrelation> result = genesetCorrelationService.fetchCorrelatedGenes(GENESET_ID1, MOLECULAR_PROFILE_ID,
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), 0.3);

        //what we expect: gene 1 has good correlation with geneset 1, while gene 2 is anti-correlated. So 
        //we expect only gene 1 to return, with correlation close to 1.0 (it is artificially high in this example)

        Assert.assertEquals(1, result.size());
        Assert.assertEquals(1, result.get(0).getEntrezGeneId().intValue());
        Assert.assertEquals((Double) 1.0, result.get(0).getCorrelationValue());

        result = genesetCorrelationService.fetchCorrelatedGenes(GENESET_ID1, MOLECULAR_PROFILE_ID,
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), -1.0);

        //now we expect both genes to return, since correlation threshold is at -1.0 (just a dummy threshold for testing):
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(1, result.get(0).getEntrezGeneId().intValue());
        Assert.assertEquals((Double) 1.0, result.get(0).getCorrelationValue());
        Assert.assertEquals(2, result.get(1).getEntrezGeneId().intValue());
        Assert.assertEquals((Double) (-1.0), result.get(1).getCorrelationValue());

        //test when 1 of the samples does not have data:
        result = genesetCorrelationService.fetchCorrelatedGenes(GENESET_ID1, MOLECULAR_PROFILE_ID,
                Arrays.asList(SAMPLE_ID1, SAMPLE_ID2, SAMPLE_ID3), -1.0);
        Assert.assertEquals(2, result.size());
    }
}
