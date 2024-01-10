package org.cbioportal.service.impl;

import org.cbioportal.model.GeneMolecularData;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MrnaPercentile;
import org.cbioportal.service.MolecularDataService;
import org.cbioportal.service.MolecularProfileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MrnaPercentileServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private MrnaPercentileServiceImpl mrnaPercentileService;

    @Mock
    private MolecularDataService molecularDataService;
    @Mock
    private MolecularProfileService molecularProfileService;

    @Test
    public void fetchMrnaPercentile() throws Exception {

        List<GeneMolecularData> molecularDataList = new ArrayList<>();
        GeneMolecularData molecularData1 = new GeneMolecularData();
        molecularData1.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData1.setEntrezGeneId(ENTREZ_GENE_ID_1);
        molecularData1.setSampleId(SAMPLE_ID1);
        molecularData1.setValue("0.3456");
        molecularDataList.add(molecularData1);
        GeneMolecularData molecularData2 = new GeneMolecularData();
        molecularData2.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData2.setEntrezGeneId(ENTREZ_GENE_ID_1);
        molecularData2.setSampleId("sample_id_2");
        molecularData2.setValue("0.2456");
        molecularDataList.add(molecularData2);
        GeneMolecularData molecularData3 = new GeneMolecularData();
        molecularData3.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData3.setEntrezGeneId(ENTREZ_GENE_ID_1);
        molecularData3.setSampleId("sample_id_3");
        molecularData3.setValue("0.2457");
        molecularDataList.add(molecularData3);
        GeneMolecularData molecularData4 = new GeneMolecularData();
        molecularData4.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData4.setEntrezGeneId(2);
        molecularData4.setSampleId(SAMPLE_ID1);
        molecularData4.setValue("NA");
        molecularDataList.add(molecularData4);
        GeneMolecularData molecularData5 = new GeneMolecularData();
        molecularData5.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData5.setEntrezGeneId(2);
        molecularData5.setSampleId("sample_id_2");
        molecularData5.setValue("0.1456");
        molecularDataList.add(molecularData5);
        GeneMolecularData molecularData6 = new GeneMolecularData();
        molecularData6.setMolecularProfileId(MOLECULAR_PROFILE_ID);
        molecularData6.setEntrezGeneId(2);
        molecularData6.setSampleId("sample_id_3");
        molecularData6.setValue("-0.1234");
        molecularDataList.add(molecularData6);

        MolecularProfile molecularProfile = new MolecularProfile();
        molecularProfile.setMolecularAlterationType(MolecularProfile.MolecularAlterationType.MRNA_EXPRESSION);
        Mockito.when(molecularProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(molecularProfile);
        
        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        entrezGeneIds.add(2);

        Mockito.when(molecularDataService.fetchMolecularData(MOLECULAR_PROFILE_ID, null, entrezGeneIds, 
            "SUMMARY")).thenReturn(molecularDataList);
        
        List<MrnaPercentile> result = mrnaPercentileService.fetchMrnaPercentile(MOLECULAR_PROFILE_ID, "sample_id_2", 
            entrezGeneIds);

        Assert.assertEquals(2, result.size());
        MrnaPercentile mrnaPercentile1 = result.get(0);
        Assert.assertEquals(MOLECULAR_PROFILE_ID, mrnaPercentile1.getMolecularProfileId());
        Assert.assertEquals("sample_id_2", mrnaPercentile1.getSampleId());
        Assert.assertEquals(ENTREZ_GENE_ID_1, mrnaPercentile1.getEntrezGeneId());
        Assert.assertEquals(new BigDecimal("0.2456"), mrnaPercentile1.getzScore());
        Assert.assertEquals(new BigDecimal("33.33"), mrnaPercentile1.getPercentile());
        MrnaPercentile mrnaPercentile2 = result.get(1);
        Assert.assertEquals(MOLECULAR_PROFILE_ID, mrnaPercentile2.getMolecularProfileId());
        Assert.assertEquals("sample_id_2", mrnaPercentile2.getSampleId());
        Assert.assertEquals((Integer) 2, mrnaPercentile2.getEntrezGeneId());
        Assert.assertEquals(new BigDecimal("0.1456"), mrnaPercentile2.getzScore());
        Assert.assertEquals(new BigDecimal("100.00"), mrnaPercentile2.getPercentile());
    }
}