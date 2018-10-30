package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.meta.BaseMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class MolecularProfileMyBatisRepositoryTest {

    @Autowired
    private MolecularProfileMyBatisRepository molecularProfileMyBatisRepository;

    @Test
    public void getAllMolecularProfilesIdProjection() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository.getAllMolecularProfiles("ID", null, null, 
            null, null);

        Assert.assertEquals(9, result.size());
        MolecularProfile molecularProfile = result.get(0);
        Assert.assertEquals((Integer) 8, molecularProfile.getMolecularProfileId());
        Assert.assertEquals("acc_tcga_mutations", molecularProfile.getStableId());
        Assert.assertNull(molecularProfile.getCancerStudy());
    }

    @Test
    public void getAllMolecularProfilesSummaryProjection() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository.getAllMolecularProfiles("SUMMARY", null, null, 
            null, null);

        Assert.assertEquals(9, result.size());
        MolecularProfile molecularProfile = result.get(0);
        Assert.assertEquals((Integer) 2, molecularProfile.getMolecularProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", molecularProfile.getStableId());
        Assert.assertEquals((Integer) 1, molecularProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", molecularProfile.getCancerStudyIdentifier());
        Assert.assertEquals(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION,
                molecularProfile.getMolecularAlterationType());
        Assert.assertEquals("DISCRETE", molecularProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", molecularProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                molecularProfile.getDescription());
        Assert.assertEquals(true, molecularProfile.getShowProfileInAnalysisTab());
        Assert.assertNull(molecularProfile.getCancerStudy());
    }

    @Test
    public void getAllMolecularProfilesDetailedProjection() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository.getAllMolecularProfiles("DETAILED", null, 
            null, null, null);

        Assert.assertEquals(9, result.size());
        MolecularProfile molecularProfile = result.get(0);
        Assert.assertEquals((Integer) 2, molecularProfile.getMolecularProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", molecularProfile.getStableId());
        Assert.assertEquals((Integer) 1, molecularProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", molecularProfile.getCancerStudyIdentifier());
        Assert.assertEquals(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION,
                molecularProfile.getMolecularAlterationType());
        Assert.assertEquals("DISCRETE", molecularProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", molecularProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                        "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                molecularProfile.getDescription());
        Assert.assertEquals(true, molecularProfile.getShowProfileInAnalysisTab());
        CancerStudy cancerStudy = molecularProfile.getCancerStudy();
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
    public void getAllMolecularProfilesSummaryProjection1PageSize() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository.getAllMolecularProfiles("SUMMARY", 1, 0, null, 
            null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getAllMolecularProfilesSummaryProjectionStableIdSort() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository.getAllMolecularProfiles("SUMMARY", null, null,
                "stableId", "ASC");

        Assert.assertEquals(9, result.size());
        Assert.assertEquals("acc_tcga_mutations", result.get(0).getStableId());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(1).getStableId());
        Assert.assertEquals("study_tcga_pub_gsva_scores", result.get(2).getStableId());
        Assert.assertEquals("study_tcga_pub_log2CNA", result.get(3).getStableId());
        Assert.assertEquals("study_tcga_pub_methylation_hm27", result.get(5).getStableId());
        Assert.assertEquals("study_tcga_pub_mrna", result.get(6).getStableId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(7).getStableId());
        Assert.assertEquals("study_tcga_pub_sv", result.get(8).getStableId());
    }

    @Test
    public void getMetaMolecularProfiles() throws Exception {

        BaseMeta result = molecularProfileMyBatisRepository.getMetaMolecularProfiles();

        Assert.assertEquals((Integer) 9, result.getTotalCount());
    }

    @Test
    public void getMolecularProfileNullResult() throws Exception {

        MolecularProfile result = molecularProfileMyBatisRepository.getMolecularProfile("invalid_molecular_profile");

        Assert.assertNull(result);
    }

    @Test
    public void getMolecularProfile() throws Exception {

        MolecularProfile molecularProfile = molecularProfileMyBatisRepository
            .getMolecularProfile("study_tcga_pub_gistic");

        Assert.assertEquals((Integer) 2, molecularProfile.getMolecularProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", molecularProfile.getStableId());
        Assert.assertEquals((Integer) 1, molecularProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", molecularProfile.getCancerStudyIdentifier());
        Assert.assertEquals(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION,
                molecularProfile.getMolecularAlterationType());
        Assert.assertEquals("DISCRETE", molecularProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", molecularProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                        "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                molecularProfile.getDescription());
        Assert.assertEquals(true, molecularProfile.getShowProfileInAnalysisTab());
        CancerStudy cancerStudy = molecularProfile.getCancerStudy();
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
    public void getMolecularProfiles() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository.getMolecularProfiles(Arrays.asList(
            "study_tcga_pub_gistic", "study_tcga_pub_mrna"), "SUMMARY");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("study_tcga_pub_gistic", result.get(0).getStableId());
        Assert.assertEquals("study_tcga_pub_mrna", result.get(1).getStableId());
    }

    @Test
    public void getMetaMolecularProfilesById() throws Exception {

        BaseMeta result = molecularProfileMyBatisRepository.getMetaMolecularProfiles(Arrays.asList(
            "study_tcga_pub_gistic", "study_tcga_pub_mrna"));

        Assert.assertEquals((Integer) 2, result.getTotalCount());
    }


    @Test
    public void getAllMolecularProfilesInStudySummaryProjection() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository
            .getAllMolecularProfilesInStudy("study_tcga_pub", "SUMMARY", null, null, null, null);

        Assert.assertEquals(8, result.size());
        MolecularProfile molecularProfile = result.get(0);
        Assert.assertEquals((Integer) 2, molecularProfile.getMolecularProfileId());
        Assert.assertEquals("study_tcga_pub_gistic", molecularProfile.getStableId());
        Assert.assertEquals((Integer) 1, molecularProfile.getCancerStudyId());
        Assert.assertEquals("study_tcga_pub", molecularProfile.getCancerStudyIdentifier());
        Assert.assertEquals(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION,
                molecularProfile.getMolecularAlterationType());
        Assert.assertEquals("DISCRETE", molecularProfile.getDatatype());
        Assert.assertEquals("Putative copy-number alterations from GISTIC", molecularProfile.getName());
        Assert.assertEquals("Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous " +
                        "deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.",
                molecularProfile.getDescription());
        Assert.assertEquals(true, molecularProfile.getShowProfileInAnalysisTab());
        Assert.assertNull(molecularProfile.getCancerStudy());
    }

    @Test
    public void getMetaMolecularProfilesInStudy() throws Exception {

        BaseMeta result = molecularProfileMyBatisRepository.getMetaMolecularProfilesInStudy("study_tcga_pub");

        Assert.assertEquals((Integer) 8, result.getTotalCount());
    }

    @Test
    public void getMolecularProfilesInStudies() throws Exception {

        List<MolecularProfile> result = molecularProfileMyBatisRepository
            .getMolecularProfilesInStudies(Arrays.asList("study_tcga_pub", "acc_tcga"), "SUMMARY");

        Assert.assertEquals(9, result.size());
        MolecularProfile molecularProfile = result.get(0);
        Assert.assertEquals((Integer) 8, molecularProfile.getMolecularProfileId());
        Assert.assertEquals("acc_tcga_mutations", molecularProfile.getStableId());
        Assert.assertEquals((Integer) 2, molecularProfile.getCancerStudyId());
        Assert.assertEquals("acc_tcga", molecularProfile.getCancerStudyIdentifier());
        Assert.assertEquals(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED,
                molecularProfile.getMolecularAlterationType());
        Assert.assertEquals("MAF", molecularProfile.getDatatype());
        Assert.assertEquals("Mutations", molecularProfile.getName());
        Assert.assertEquals("Mutation data from whole exome sequencing.",
                molecularProfile.getDescription());
        Assert.assertEquals(true, molecularProfile.getShowProfileInAnalysisTab());
        Assert.assertNull(molecularProfile.getCancerStudy());
    }

    @Test
    public void getMetaMolecularProfilesInStudies() throws Exception {

        BaseMeta result = molecularProfileMyBatisRepository.getMetaMolecularProfilesInStudies(
            Arrays.asList("study_tcga_pub", "acc_tcga"));

        Assert.assertEquals((Integer) 9, result.getTotalCount());
    }
}
