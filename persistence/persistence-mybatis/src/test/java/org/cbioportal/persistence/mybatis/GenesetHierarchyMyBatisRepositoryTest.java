package org.cbioportal.persistence.mybatis;

import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {GenesetHierarchyMyBatisRepository.class, TestConfig.class})
public class GenesetHierarchyMyBatisRepositoryTest {
    
    @Autowired
    private GenesetHierarchyMyBatisRepository genesetHierarchyMyBatisRepository;
    
    /**
     * Test data is as follows:
     *  Root node ->  Sub node A -> Parent node 1 -> MORF_ATRX
     *     "              "             "         -> HINATA_NFKB_MATRIX
     *     "              "      -> Parent node 2 -> HINATA_NFKB_MATRIX
     *  Root node ->  Sub node B -> x (dead branch)
     */

    @Test
    public void getGenesetHierarchySuperNodes() {

        List<GenesetHierarchyInfo> result = genesetHierarchyMyBatisRepository.getGenesetHierarchySuperNodes(
                Arrays.asList("MORF_ATRX","HINATA_NFKB_MATRIX"));
        //Expect Root node, Sub node A, Sub node B
        Assert.assertEquals(3, result.size());
        //ordered by parentNodeName, nodeName, so Root node and then Sub node A and B:
        Assert.assertEquals("Root node", result.get(0).getNodeName());
        Assert.assertEquals(null, result.get(0).getParentNodeName());
        Assert.assertEquals("Sub node A", result.get(1).getNodeName());
        Assert.assertEquals("Root node", result.get(1).getParentNodeName());
        Assert.assertEquals("Sub node B", result.get(2).getNodeName());
        Assert.assertEquals("Root node", result.get(2).getParentNodeName());
    }
    
    @Test
    public void getGenesetHierarchyParents() {

        List<GenesetHierarchyInfo> result = genesetHierarchyMyBatisRepository.getGenesetHierarchyParents(
                Arrays.asList("MORF_ATRX","HINATA_NFKB_MATRIX"));
        //Expect parent node 1, parent node 2
        Assert.assertEquals(2, result.size());
        //ordered by parentNodeName, nodeName, so parent node 1 and then 2:
        Assert.assertEquals("Parent node 1", result.get(0).getNodeName());
        Assert.assertEquals("Sub node A", result.get(0).getParentNodeName());
        Assert.assertEquals("Parent node 2", result.get(1).getNodeName());
        Assert.assertEquals("Sub node A", result.get(1).getParentNodeName());
        
        result = genesetHierarchyMyBatisRepository.getGenesetHierarchyParents(
                Arrays.asList("MORF_ATRX"));
        //Expect parent node 1
        Assert.assertEquals(1, result.size());
    }
    
    @Test
    public void getGenesetHierarchyGenesets() {
        
        List<GenesetHierarchyInfo> nodes = genesetHierarchyMyBatisRepository.getGenesetHierarchyParents(
                Arrays.asList("MORF_ATRX","HINATA_NFKB_MATRIX"));
        //Expect parent node 1, parent node 2
        Assert.assertEquals(2, nodes.size());
        //ordered by parentNodeName, nodeName, so parent node 1 and then 2:
        Assert.assertEquals("Parent node 1", nodes.get(0).getNodeName());
        List<Geneset> result = genesetHierarchyMyBatisRepository.getGenesetHierarchyGenesets(nodes.get(0).getNodeId());
        //Expect MORF_ATRX and HINATA_NFKB_MATRIX (but ordered by name, so HINATA first)
        Assert.assertEquals(2, result.size());
        Geneset geneset = result.get(0);
        Assert.assertEquals("HINATA_NFKB_MATRIX", geneset.getGenesetId());
        Assert.assertEquals("https://hinata_link", geneset.getRefLink());
        geneset = result.get(1);
        Assert.assertEquals("MORF_ATRX", geneset.getGenesetId());
        Assert.assertEquals("Morf description", geneset.getDescription());
        
        Assert.assertEquals("Parent node 2", nodes.get(1).getNodeName());
        result = genesetHierarchyMyBatisRepository.getGenesetHierarchyGenesets(nodes.get(1).getNodeId());
        //Expect only HINATA_NFKB_MATRIX
        Assert.assertEquals(1, result.size());
        geneset = result.get(0);
        Assert.assertEquals("HINATA_NFKB_MATRIX", geneset.getGenesetId());
        Assert.assertEquals("https://hinata_link", geneset.getRefLink());
    }
}