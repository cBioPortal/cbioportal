package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.*;
import org.cbioportal.model.meta.MutationMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class MutationMyBatisRepositoryTest {

    @Autowired
    private MutationMyBatisRepository mutationMyBatisRepository;
    
    @Test
    public void getMutationsInMolecularProfileBySampleListIdIdProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, null, "ID", null, null, null, null);

        Assert.assertEquals(8, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutation.getSampleId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
    }
    
    @Test
    public void getMutationsInMolecularProfileBySampleListIdSummaryProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(8, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutation.getSampleId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("GRCh37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getOncotatorRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) (-1), mutation.getTumorAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
        Assert.assertEquals("Putative_Driver", mutation.getDriverFilter());
        Assert.assertEquals("Pathogenic", mutation.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 1", mutation.getDriverTiersFilter());
        Assert.assertEquals("Highly Actionable", mutation.getDriverTiersFilterAnnotation());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdAndEntrezGeneIdsSummaryProjection() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", entrezGeneIds, null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(3, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutation.getSampleId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("GRCh37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getOncotatorRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) (-1), mutation.getTumorAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
        Assert.assertEquals("Putative_Driver", mutation.getDriverFilter());
        Assert.assertEquals("Pathogenic", mutation.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 1", mutation.getDriverTiersFilter());
        Assert.assertEquals("Highly Actionable", mutation.getDriverTiersFilterAnnotation());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdDetailedProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, null, "DETAILED", null, null, null, null);

        Assert.assertEquals(3, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutation.getSampleId());
        Assert.assertEquals((Integer) 207, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("NA", mutation.getFunctionalImpactScore());
        Assert.assertEquals(new BigDecimal("0.0"), mutation.getFisValue());
        Assert.assertEquals("getma.org/?cm=var&var=hg19,17,41244748,G,A&fts=all", mutation.getLinkXvar());
        Assert.assertEquals("NA", mutation.getLinkPdb());
        Assert.assertEquals("NA", mutation.getLinkMsa());
        Assert.assertEquals("GRCh37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getOncotatorRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) (-1), mutation.getTumorAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
        Assert.assertEquals("Putative_Driver", mutation.getDriverFilter());
        Assert.assertEquals("Pathogenic", mutation.getDriverFilterAnnotation());
        Assert.assertEquals("Tier 1", mutation.getDriverTiersFilter());
        Assert.assertEquals("Highly Actionable", mutation.getDriverTiersFilterAnnotation());
        Gene gene = mutation.getGene();
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("protein-coding", gene.getType());
        Assert.assertEquals("14q32.33", gene.getCytoband());
        Assert.assertEquals((Integer) 10838, gene.getLength());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdSummaryProjection1PageSize() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, null, "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getMutationsInMolecularProfileBySampleListIdSummaryProjectionProteinChangeSort() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null, null, "SUMMARY", null, null, "proteinChange", "ASC");

        Assert.assertEquals(8, result.size());
        Assert.assertEquals("C27_splice", result.get(0).getProteinChange());
        Assert.assertEquals("C27_splice", result.get(1).getProteinChange());
        Assert.assertEquals("C27_splice", result.get(2).getProteinChange());
        Assert.assertEquals("C61G", result.get(3).getProteinChange());
    }

    @Test
    public void getMetaMutationsInMolecularProfileBySampleListId() throws Exception {

        MutationMeta result = mutationMyBatisRepository.getMetaMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", null);

        Assert.assertEquals((Integer) 8, result.getTotalCount());
        Assert.assertEquals((Integer) 7, result.getSampleCount());
    }

    @Test
    public void getMetaMutationsInMolecularProfileBySampleListIdAndEntrezGeneIds() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(207);
        entrezGeneIds.add(208);
        
        MutationMeta result = mutationMyBatisRepository.getMetaMutationsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all", entrezGeneIds);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
        Assert.assertEquals((Integer) 3, result.getSampleCount());
    }

    @Test
    public void getMutationsInMultipleMolecularProfiles() throws Exception {
        
        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileIds.add("acc_tcga_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");
        
        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-B0SO-01");
        sampleIds.add("TCGA-A1-A0SH-01");
        
        List<Mutation> result = mutationMyBatisRepository.getMutationsInMultipleMolecularProfiles(molecularProfileIds, 
            sampleIds, null, "SUMMARY", null, null, null, null);
        
        Assert.assertEquals(3, result.size());
        Mutation mutation1 = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation1.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SH-01", mutation1.getSampleId());
        Mutation mutation2 = result.get(1);
        Assert.assertEquals("study_tcga_pub_mutations", mutation2.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SH-01", mutation2.getSampleId());
        Mutation mutation3 = result.get(2);
        Assert.assertEquals("acc_tcga_mutations", mutation3.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-B0SO-01", mutation3.getSampleId());
    }

    @Test
    public void getMetaMutationsInMultipleMolecularProfiles() throws Exception {

        List<String> molecularProfileIds = new ArrayList<>();
        molecularProfileIds.add("acc_tcga_mutations");
        molecularProfileIds.add("study_tcga_pub_mutations");

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-B0SO-01");
        sampleIds.add("TCGA-A1-A0SH-01");
        
        MutationMeta result = mutationMyBatisRepository.getMetaMutationsInMultipleMolecularProfiles(molecularProfileIds,
            sampleIds, null);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
        Assert.assertEquals((Integer) 2, result.getSampleCount());
    }

    @Test
    public void fetchMutationsInMolecularProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SO-01");
        
        List<Mutation> result = mutationMyBatisRepository.fetchMutationsInMolecularProfile("study_tcga_pub_mutations", 
            sampleIds, null, null, "SUMMARY", null, null, null, null);
        
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(0).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(0).getSampleId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(1).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(1).getSampleId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(2).getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SO-01", result.get(2).getSampleId());
    }

    @Test
    public void fetchMetaMutationsInMolecularProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SO-01");

        MutationMeta result = mutationMyBatisRepository.fetchMetaMutationsInMolecularProfile("study_tcga_pub_mutations",
            sampleIds, null);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
        Assert.assertEquals((Integer) 2, result.getSampleCount());
    }

    @Test
    public void getSampleCountByEntrezGeneIds() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(672);
        
        List<MutationCountByGene> result = mutationMyBatisRepository.getSampleCountByEntrezGeneIdsAndSampleIds(
            "study_tcga_pub_mutations", null, entrezGeneIds);
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals((Integer) 4, result.get(0).getCount());
    }
    
    @Test
    public void getMutationCountsInMolecularProfileBySampleListId() throws Exception {
        
        List<MutationCount> result = mutationMyBatisRepository.getMutationCountsInMolecularProfileBySampleListId(
            "study_tcga_pub_mutations", "study_tcga_pub_all");

        Assert.assertEquals(7, result.size());
        MutationCount mutationCount = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutationCount.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SB-01", mutationCount.getSampleId());
        Assert.assertEquals((Integer) 1, mutationCount.getMutationCount());
    }

    @Test
    public void fetchMutationCountsInMolecularProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SO-01");

        List<MutationCount> result = mutationMyBatisRepository.fetchMutationCountsInMolecularProfile(
            "study_tcga_pub_mutations", sampleIds);

        Assert.assertEquals(2, result.size());
        MutationCount mutationCount = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutationCount.getMolecularProfileId());
        Assert.assertEquals("TCGA-A1-A0SH-01", mutationCount.getSampleId());
        Assert.assertEquals((Integer) 2, mutationCount.getMutationCount());
    }

    @Test
    public void getMutationCountByPosition() throws Exception {
        
        MutationCountByPosition result = mutationMyBatisRepository.getMutationCountByPosition(672, 61, 936);
        
        Assert.assertEquals((Integer) 672, result.getEntrezGeneId());
        Assert.assertEquals((Integer) 61, result.getProteinPosStart());
        Assert.assertEquals((Integer) 936, result.getProteinPosEnd());
        Assert.assertEquals((Integer) 3, result.getCount());
    }
}
