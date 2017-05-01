package org.cbioportal.service.impl;

import org.cbioportal.model.GeneGeneticData;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.GeneticDataService;
import org.cbioportal.service.GeneticProfileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MrnaPercentileServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MrnaPercentileServiceImpl mrnaPercentileService;

    @Mock
    private GeneticDataService geneticDataService;
    @Mock
    private GeneticProfileService geneticProfileService;

    @Test
    public void fetchMrnaPercentile() throws Exception {

        List<GeneGeneticData> geneticDataList = new ArrayList<>();
        GeneGeneticData geneticData1 = new GeneGeneticData();
        geneticData1.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData1.setEntrezGeneId(ENTREZ_GENE_ID);
        geneticData1.setSampleId(SAMPLE_ID1);
        geneticData1.setValue("0.3456");
        geneticDataList.add(geneticData1);
        GeneGeneticData geneticData2 = new GeneGeneticData();
        geneticData2.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData2.setEntrezGeneId(ENTREZ_GENE_ID);
        geneticData2.setSampleId("sample_id_2");
        geneticData2.setValue("0.2456");
        geneticDataList.add(geneticData2);
        GeneGeneticData geneticData3 = new GeneGeneticData();
        geneticData3.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData3.setEntrezGeneId(ENTREZ_GENE_ID);
        geneticData3.setSampleId("sample_id_3");
        geneticData3.setValue("0.2457");
        geneticDataList.add(geneticData3);
        GeneGeneticData geneticData4 = new GeneGeneticData();
        geneticData4.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData4.setEntrezGeneId(2);
        geneticData4.setSampleId(SAMPLE_ID1);
        geneticData4.setValue("NA");
        geneticDataList.add(geneticData4);
        GeneGeneticData geneticData5 = new GeneGeneticData();
        geneticData5.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData5.setEntrezGeneId(2);
        geneticData5.setSampleId("sample_id_2");
        geneticData5.setValue("0.1456");
        geneticDataList.add(geneticData5);
        GeneGeneticData geneticData6 = new GeneGeneticData();
        geneticData6.setGeneticProfileId(GENETIC_PROFILE_ID);
        geneticData6.setEntrezGeneId(2);
        geneticData6.setSampleId("sample_id_3");
        geneticData6.setValue("-0.1234");
        geneticDataList.add(geneticData6);

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setGeneticAlterationType(GeneticProfile.GeneticAlterationType.MRNA_EXPRESSION);
        Mockito.when(geneticProfileService.getGeneticProfile(GENETIC_PROFILE_ID)).thenReturn(geneticProfile);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID);
        entrezGeneIds.add(2);

        Mockito.when(geneticDataService.fetchGeneticData(GENETIC_PROFILE_ID, null, entrezGeneIds, 
            "SUMMARY")).thenReturn(geneticDataList);
        
        List<MrnaPercentile> result = mrnaPercentileService.fetchMrnaPercentile(GENETIC_PROFILE_ID, "sample_id_2", 
            entrezGeneIds);

        Assert.assertEquals(2, result.size());
        MrnaPercentile mrnaPercentile1 = result.get(0);
        Assert.assertEquals(GENETIC_PROFILE_ID, mrnaPercentile1.getGeneticProfileId());
        Assert.assertEquals("sample_id_2", mrnaPercentile1.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID, mrnaPercentile1.getEntrezGeneId());
        Assert.assertEquals(new BigDecimal("0.2456"), mrnaPercentile1.getzScore());
        Assert.assertEquals(new BigDecimal("33.33"), mrnaPercentile1.getPercentile());
        MrnaPercentile mrnaPercentile2 = result.get(1);
        Assert.assertEquals(GENETIC_PROFILE_ID, mrnaPercentile2.getGeneticProfileId());
        Assert.assertEquals("sample_id_2", mrnaPercentile2.getSampleId());
        Assert.assertEquals((Integer) 2, mrnaPercentile2.getEntrezGeneId());
        Assert.assertEquals(new BigDecimal("0.1456"), mrnaPercentile2.getzScore());
        Assert.assertEquals(new BigDecimal("100.00"), mrnaPercentile2.getPercentile());
    }
}