package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ReferenceGenome;
import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.model.Gene;
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
public class ReferenceGenomeGeneMyBatisRepositoryTest {

    @Autowired
    private ReferenceGenomeGeneMyBatisRepository refGeneMyBatisRepository;

    @Autowired
    private GeneMyBatisRepository geneMyBatisRepository;
    
    @Test
    public void getAllGenesByGenomeName() throws Exception {
        List<ReferenceGenomeGene> result = refGeneMyBatisRepository.getAllGenesByGenomeName(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        ReferenceGenomeGene refGene = result.get(0);
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
    }
    
    @Test
    public void getReferenceGenomeGene() throws Exception {
        String genomeName = ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME;
        ReferenceGenomeGene refGene = refGeneMyBatisRepository.getReferenceGenomeGene(207, genomeName);
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("14q32.33", refGene.getCytoband());
    }
    
    @Test
    public void getReferenceGenomeGeneByEntityId() throws Exception {
        ReferenceGenomeGene refGene = refGeneMyBatisRepository.getReferenceGenomeGeneByEntityId((Integer)2, "hg38");
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 208, gene.getEntrezGeneId());
        Assert.assertEquals("AKT2", gene.getHugoGeneSymbol());
        Assert.assertEquals("19q13.2", refGene.getCytoband());
    }
    

    @Test
    public void getGenesByGenomeName() throws Exception {
        List<Integer> geneIds = new ArrayList<>();
        geneIds.add((Integer)207);
        geneIds.add((Integer)208);
        List<ReferenceGenomeGene> result = refGeneMyBatisRepository.getGenesByGenomeName(geneIds, ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        Assert.assertEquals(2, result.size());
        ReferenceGenomeGene refGene = result.get(0);
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("14q32.33", refGene.getCytoband());
    }
}

