package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class GeneticProfileMyBatisRepositoryTest {

    @Autowired
    private GeneticProfileMyBatisRepository geneticProfileMyBatisRepository;

    @Test
    public void getAllGeneticProfilesIdProjection() throws Exception {

        List<GeneticProfile> result = geneticProfileMyBatisRepository.getAllGeneticProfiles("ID", null, null, null,
                null);

        Assert.assertEquals(6, result.size());
        GeneticProfile geneticProfile = result.get(0);
        Assert.assertEquals((Integer) 2, geneticProfile.getGeneticProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", geneticProfile.getStableId());
        Assert.assertNull(geneticProfile.getCancerStudy());
    }

    @Test
    public void getAllGeneticProfilesSummaryProjection() throws Exception {

        List<GeneticProfile> result = geneticProfileMyBatisRepository.getAllGeneticProfiles("SUMMARY", null, null, null,
                null);

        Assert.assertEquals(6, result.size());
        GeneticProfile geneticProfile = result.get(0);
        Assert.assertEquals((Integer) 2, geneticProfile.getGeneticProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", geneticProfile.getStableId());
        Assert.assertEquals((Integer) 1, geneticProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", geneticProfile.getCancerStudyIdentifier());
        Assert.assertEquals(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        Assert.assertEquals("DISCRETE", geneticProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                geneticProfile.getDescription());
        Assert.assertEquals(true, geneticProfile.getShowProfileInAnalysisTab());
        Assert.assertNull(geneticProfile.getCancerStudy());
    }

    @Test
    public void getAllGeneticProfilesDetailedProjection() throws Exception {

        List<GeneticProfile> result = geneticProfileMyBatisRepository.getAllGeneticProfiles("DETAILED", null, null,
                null, null);

        Assert.assertEquals(6, result.size());
        GeneticProfile geneticProfile = result.get(0);
        Assert.assertEquals((Integer) 2, geneticProfile.getGeneticProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", geneticProfile.getStableId());
        Assert.assertEquals((Integer) 1, geneticProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", geneticProfile.getCancerStudyIdentifier());
        Assert.assertEquals(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        Assert.assertEquals("DISCRETE", geneticProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                        "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                geneticProfile.getDescription());
        Assert.assertEquals(true, geneticProfile.getShowProfileInAnalysisTab());
        CancerStudy cancerStudy = geneticProfile.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getAllGeneticProfilesSummaryProjection1PageSize() throws Exception {

        List<GeneticProfile> result = geneticProfileMyBatisRepository.getAllGeneticProfiles("SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllGeneticProfilesSummaryProjectionStableIdSort() throws Exception {

        List<GeneticProfile> result = geneticProfileMyBatisRepository.getAllGeneticProfiles("SUMMARY", null, null,
                "stableId", "ASC");

        Assert.assertEquals(6, result.size());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(0).getStableId());
        Assert.assertEquals("study_tcga_pub_log2CNA", result.get(1).getStableId());
        Assert.assertEquals("study_tcga_pub_methylation_hm27", result.get(2).getStableId());
        Assert.assertEquals("study_tcga_pub_mrna", result.get(3).getStableId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(4).getStableId());
        Assert.assertEquals("study_tcga_pub_sv", result.get(5).getStableId());
    }

    @Test
    public void getMetaGeneticProfiles() throws Exception {

        BaseMeta result = geneticProfileMyBatisRepository.getMetaGeneticProfiles();

        Assert.assertEquals((Integer) 6, result.getTotalCount());
    }

    @Test
    public void getGeneticProfileNullResult() throws Exception {

        GeneticProfile result = geneticProfileMyBatisRepository.getGeneticProfile("invalid_genetic_profile");

        Assert.assertNull(result);
    }

    @Test
    public void getGeneticProfile() throws Exception {

        GeneticProfile geneticProfile = geneticProfileMyBatisRepository.getGeneticProfile("study_tcga_pub_gistic");

        Assert.assertEquals((Integer) 2, geneticProfile.getGeneticProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", geneticProfile.getStableId());
        Assert.assertEquals((Integer) 1, geneticProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", geneticProfile.getCancerStudyIdentifier());
        Assert.assertEquals(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        Assert.assertEquals("DISCRETE", geneticProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                        "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                geneticProfile.getDescription());
        Assert.assertEquals(true, geneticProfile.getShowProfileInAnalysisTab());
        CancerStudy cancerStudy = geneticProfile.getCancerStudy();
        Assert.assertEquals((Integer) 1, cancerStudy.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", cancerStudy.getCancerStudyIdentifier());
        Assert.assertEquals("brca", cancerStudy.getTypeOfCancerId());
        Assert.assertEquals("Breast Invasive Carcinoma (TCGA, Nature 2012)", cancerStudy.getName());
        Assert.assertEquals("BRCA (TCGA)", cancerStudy.getShortName());
        Assert.assertEquals("<a href=\\\"http://cancergenome.nih.gov/\\\">The Cancer Genome Atlas (TCGA)</a> Breast" +
                " Invasive Carcinoma project. 825 cases.<br><i>Nature 2012.</i> <a href=\\\"http://tcga-data.nci." +
                "nih.gov/tcga/\\\">Raw data via the TCGA Data Portal</a>.", cancerStudy.getDescription());
        Assert.assertEquals(true, cancerStudy.getPublicStudy());
        Assert.assertEquals("23000897", cancerStudy.getPmid());
        Assert.assertEquals("TCGA, Nature 2012", cancerStudy.getCitation());
        Assert.assertEquals("SU2C-PI3K;PUBLIC;GDAC", cancerStudy.getGroups());
        Assert.assertEquals((Integer)0 , cancerStudy.getStatus());
    }

    @Test
    public void getAllGeneticProfilesInStudySummaryProjection() throws Exception {

        List<GeneticProfile> result = geneticProfileMyBatisRepository.getAllGeneticProfilesInStudy("study_tcga_pub",
                "SUMMARY", null, null, null, null);

        Assert.assertEquals(6, result.size());
        GeneticProfile geneticProfile = result.get(0);
        Assert.assertEquals((Integer) 2, geneticProfile.getGeneticProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", geneticProfile.getStableId());
        Assert.assertEquals((Integer) 1, geneticProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", geneticProfile.getCancerStudyIdentifier());
        Assert.assertEquals(GeneticProfile.GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());
        Assert.assertEquals("DISCRETE", geneticProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", geneticProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                        "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                geneticProfile.getDescription());
        Assert.assertEquals(true, geneticProfile.getShowProfileInAnalysisTab());
        Assert.assertNull(geneticProfile.getCancerStudy());
    }

    @Test
    public void getMetaGeneticProfilesInStudy() throws Exception {

        BaseMeta result = geneticProfileMyBatisRepository.getMetaGeneticProfilesInStudy("study_tcga_pub");

        Assert.assertEquals((Integer) 6, result.getTotalCount());
    }
}