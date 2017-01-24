package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.Gene;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationSampleCountByGene;
import org.cbioportal.model.MutationSampleCountByKeyword;
import org.cbioportal.model.meta.MutationMeta;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class MutationMyBatisRepositoryTest {
    
    @Autowired
    private MutationMyBatisRepository mutationMyBatisRepository;
    
    @Test
    public void getMutationsInGeneticProfileIdProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInGeneticProfile("study_tcga_pub_mutations", 
            "TCGA-A1-A0SH-01", "ID", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getGeneticProfileStableId());
        Assert.assertEquals("TCGA-A1-A0SH-01", mutation.getSampleStableId());
        Assert.assertEquals((Integer) 672, mutation.getEntrezGeneId());
    }
    
    @Test
    public void getMutationsInGeneticProfileSummaryProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInGeneticProfile("study_tcga_pub_mutations",
            "TCGA-A1-A0SH-01", "SUMMARY", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getGeneticProfileStableId());
        Assert.assertEquals("TCGA-A1-A0SH-01", mutation.getSampleStableId());
        Assert.assertEquals((Integer) 672, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getOncotatorRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) 1, mutation.getTumorAltCount());
        Assert.assertEquals((Integer) 0, mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
    }

    @Test
    public void getMutationsInGeneticProfileDetailedProjection() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInGeneticProfile("study_tcga_pub_mutations",
            "TCGA-A1-A0SH-01", "DETAILED", null, null, null, null);

        Assert.assertEquals(2, result.size());
        Mutation mutation = result.get(0);
        Assert.assertEquals("study_tcga_pub_mutations", mutation.getGeneticProfileStableId());
        Assert.assertEquals("TCGA-A1-A0SH-01", mutation.getSampleStableId());
        Assert.assertEquals((Integer) 672, mutation.getEntrezGeneId());
        Assert.assertEquals("cyclases/Protein", mutation.getAminoAcidChange());
        Assert.assertEquals("genome.wustl.edu", mutation.getCenter());
        Assert.assertEquals((Long) 41244748L, mutation.getEndPosition());
        Assert.assertEquals("BRCA1 truncating", mutation.getKeyword());
        Assert.assertEquals("Germline", mutation.getMutationStatus());
        Assert.assertEquals("Nonsense_Mutation", mutation.getMutationType());
        Assert.assertEquals("37", mutation.getNcbiBuild());
        Assert.assertEquals((Integer) (-1), mutation.getNormalAltCount());
        Assert.assertEquals((Integer) (-1), mutation.getNormalRefCount());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosEnd());
        Assert.assertEquals((Integer) 934, mutation.getOncotatorProteinPosStart());
        Assert.assertEquals("NM_007294", mutation.getOncotatorRefseqMrnaId());
        Assert.assertEquals("Q934*", mutation.getProteinChange());
        Assert.assertEquals("G", mutation.getReferenceAllele());
        Assert.assertEquals((Long) 41244748L, mutation.getStartPosition());
        Assert.assertEquals((Integer) 1, mutation.getTumorAltCount());
        Assert.assertEquals((Integer) 0, mutation.getTumorRefCount());
        Assert.assertEquals("A", mutation.getTumorSeqAllele());
        Assert.assertEquals("Unknown", mutation.getValidationStatus());
        Assert.assertEquals("SNP", mutation.getVariantType());
        Gene gene = mutation.getGene();
        Assert.assertEquals((Integer) 672, gene.getEntrezGeneId());
        Assert.assertEquals("BRCA1", gene.getHugoGeneSymbol());
        Assert.assertEquals("protein-coding", gene.getType());
        Assert.assertEquals("17q21", gene.getCytoband());
        Assert.assertEquals((Integer) 8426, gene.getLength());
    }

    @Test
    public void getMutationsInGeneticProfileSummaryProjection1PageSize() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInGeneticProfile("study_tcga_pub_mutations",
            "TCGA-A1-A0SH-01", "SUMMARY", 1, 0, null, null);

        Assert.assertEquals(1, result.size());
    }

    @Test
    public void getMutationsInGeneticProfileSummaryProjectionProteinChangeSort() throws Exception {

        List<Mutation> result =  mutationMyBatisRepository.getMutationsInGeneticProfile("study_tcga_pub_mutations",
            "TCGA-A1-A0SH-01", "SUMMARY", null, null, "proteinChange", "ASC");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("C61G", result.get(0).getProteinChange());
        Assert.assertEquals("Q934*", result.get(1).getProteinChange());
    }

    @Test
    public void getMetaMutationsInGeneticProfile() throws Exception {

        MutationMeta result = mutationMyBatisRepository.getMetaMutationsInGeneticProfile("study_tcga_pub_mutations", 
            "TCGA-A1-A0SH-01");

        Assert.assertEquals((Integer) 2, result.getTotalCount());
        Assert.assertEquals((Integer) 1, result.getSampleCount());
    }

    @Test
    public void fetchMutationsInGeneticProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SO-01");
        
        List<Mutation> result = mutationMyBatisRepository.fetchMutationsInGeneticProfile("study_tcga_pub_mutations", 
            sampleIds, "SUMMARY", null, null, null, null);
        
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(0).getGeneticProfileStableId());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(0).getSampleStableId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(1).getGeneticProfileStableId());
        Assert.assertEquals("TCGA-A1-A0SH-01", result.get(1).getSampleStableId());
        Assert.assertEquals("study_tcga_pub_mutations", result.get(2).getGeneticProfileStableId());
        Assert.assertEquals("TCGA-A1-A0SO-01", result.get(2).getSampleStableId());
    }

    @Test
    public void fetchMetaMutationsInGeneticProfile() throws Exception {

        List<String> sampleIds = new ArrayList<>();
        sampleIds.add("TCGA-A1-A0SH-01");
        sampleIds.add("TCGA-A1-A0SO-01");

        MutationMeta result = mutationMyBatisRepository.fetchMetaMutationsInGeneticProfile("study_tcga_pub_mutations",
            sampleIds);

        Assert.assertEquals((Integer) 3, result.getTotalCount());
        Assert.assertEquals((Integer) 2, result.getSampleCount());
    }

    @Test
    public void getSampleCountByEntrezGeneIds() throws Exception {

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(672);
        
        List<MutationSampleCountByGene> result = mutationMyBatisRepository.getSampleCountByEntrezGeneIds(
            "study_tcga_pub_mutations", entrezGeneIds);
        
        Assert.assertEquals(1, result.size());
        Assert.assertEquals((Integer) 4, result.get(0).getSampleCount());
    }

    @Test
    public void getSampleCountByKeywords() throws Exception {

        List<String> keywords = new ArrayList<>();
        keywords.add("BRCA1 C61 missense");

        List<MutationSampleCountByKeyword> result = mutationMyBatisRepository.getSampleCountByKeywords(
            "study_tcga_pub_mutations", keywords);

        Assert.assertEquals(1, result.size());
        Assert.assertEquals((Integer) 2, result.get(0).getSampleCount());
    }
}