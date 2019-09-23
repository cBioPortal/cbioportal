package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class MolecularDataMyBatisRepositoryTest {
    
    @Autowired
    private MolecularDataMyBatisRepository molecularDataMyBatisRepository;
    
    @Test
    public void getCommaSeparatedSampleIdsOfMolecularProfile() throws Exception {

        String result = molecularDataMyBatisRepository
            .getCommaSeparatedSampleIdsOfMolecularProfile("study_tcga_pub_gistic");

        Assert.assertEquals("1,2,3,4,5,6,7,8,9,10,11,12,13,14,", result);
    }

    @Test
    public void getCommaSeparatedSampleIdsOfMolecularProfiles() throws Exception {

        List<String> result = molecularDataMyBatisRepository
            .getCommaSeparatedSampleIdsOfMolecularProfiles(Arrays.asList("study_tcga_pub_mrna", "study_tcga_pub_m_na"));

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("1,2,3,4,5,6,7,8,9,10,11,", result.get(0));
        Assert.assertEquals("2,3,6,8,9,10,12,13,", result.get(1));
    }

    @Test
    public void getGeneMolecularAlterations() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<GeneMolecularAlteration> result = molecularDataMyBatisRepository.getGeneMolecularAlterations("study_tcga_pub_gistic",
            entrezGeneIds, "SUMMARY");
        
        Assert.assertEquals(2, result.size());
        GeneMolecularAlteration molecularAlteration1 = result.get(0);
        Assert.assertEquals((Integer) 207, molecularAlteration1.getEntrezGeneId());
        String[] expected = {"-0.4674","-0.6270","-1.2266","-1.2479","-1.2262","0.6962","-0.3338","-0.1264","0.7559","-1.1267","-0.5893",
        "-1.1546","-1.0027","-1.3157",""};
        Assert.assertArrayEquals(expected, molecularAlteration1.getSplitValues());
        GeneMolecularAlteration molecularAlteration2 = result.get(1);
        Assert.assertEquals((Integer) 208, molecularAlteration2.getEntrezGeneId());
        String[] expected2 = {"1.4146","-0.0662","-0.8585","-1.6576","-0.3552","-0.8306","0.8102","0.1146","0.3498","0.0349","0.4927",
                "-0.8665","-0.4754","-0.7221",""};
        Assert.assertArrayEquals(expected2, molecularAlteration2.getSplitValues());
    }

    @Test
    public void getGeneMolecularAlterationsInMultipleMolecularProfiles() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<GeneMolecularAlteration> result = molecularDataMyBatisRepository
            .getGeneMolecularAlterationsInMultipleMolecularProfiles(Arrays.asList("study_tcga_pub_gistic", "study_tcga_pub_mrna"),
            entrezGeneIds, "SUMMARY");
        
        Assert.assertEquals(3, result.size());
        GeneMolecularAlteration molecularAlteration1 = result.get(0);
        Assert.assertEquals((Integer) 207, molecularAlteration1.getEntrezGeneId());
        String[] expected = {"-0.4674","-0.6270","-1.2266","-1.2479","-1.2262","0.6962","-0.3338","-0.1264","0.7559","-1.1267","-0.5893",
        "-1.1546","-1.0027","-1.3157",""};
        Assert.assertArrayEquals(expected, molecularAlteration1.getSplitValues());
        GeneMolecularAlteration molecularAlteration2 = result.get(1);
        Assert.assertEquals((Integer) 208, molecularAlteration2.getEntrezGeneId());
        String[] expected2 = {"1.4146","-0.0662","-0.8585","-1.6576","-0.3552","-0.8306","0.8102","0.1146","0.3498","0.0349","0.4927",
                "-0.8665","-0.4754","-0.7221",""};
        Assert.assertArrayEquals(expected2, molecularAlteration2.getSplitValues());
        GeneMolecularAlteration molecularAlteration3 = result.get(2);
        Assert.assertEquals((Integer) 208, molecularAlteration3.getEntrezGeneId());
        String[] expected3 = {"-0.8097","0.7360","-1.0225","-0.8922","0.7247","0.3537","1.2702","-0.1419",""};
        Assert.assertArrayEquals(expected3, molecularAlteration3.getSplitValues());
    }

    @Test
    public void getGenesetMolecularAlterations() {

        String genesetId1 = "HINATA_NFKB_MATRIX";
        String genesetId2 = "MORF_ATRX";
        List<String> genesetIds = new ArrayList<>();
        genesetIds.add(genesetId1);
        genesetIds.add(genesetId2);

        List<GenesetMolecularAlteration> result = molecularDataMyBatisRepository.getGenesetMolecularAlterations("study_tcga_pub_gsva_scores",
                genesetIds, "SUMMARY");

        //expect 2 items, one for each geneset:
        Assert.assertEquals(2, result.size());
        GenesetMolecularAlteration molecularAlteration1 = result.get(0);
        Assert.assertEquals(genesetId1, molecularAlteration1.getGenesetId());
        String[] expected = {"1.0106","-0.0662","-0.8585","-1.6576","-0.3552","-0.8306","0.8102","0.1106","0.3098","0.0309","0.0927",
                "-0.8665","-0.0750","-0.7221",""};
        Assert.assertArrayEquals(expected, molecularAlteration1.getSplitValues());
        GenesetMolecularAlteration molecularAlteration2 = result.get(1);
        Assert.assertEquals(genesetId2, molecularAlteration2.getGenesetId());
        String[] expected2 = {"-0.0670","-0.6270","-1.2266","-1.2079","-1.2262","0.6962","-0.3338","-0.1260","0.7559","-1.1267","-0.5893",
                "-1.1506","-1.0027","-1.3157",""};
        Assert.assertArrayEquals(expected2, molecularAlteration2.getSplitValues());
    }


}
