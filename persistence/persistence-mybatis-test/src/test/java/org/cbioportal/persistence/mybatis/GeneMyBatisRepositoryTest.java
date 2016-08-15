/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.Gene;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author jiaojiao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class GeneMyBatisRepositoryTest {
    @Autowired
    private GeneMyBatisRepository geneMyBatisRepository;

    @Test
    public void getEmptyGeneList() {
                List hugoSymbols = new ArrayList<>();

		List<Gene> result = geneMyBatisRepository.getGeneListByHugoSymbols(hugoSymbols);

		Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSingleGene() {
                List hugoSymbols = new ArrayList<>(Arrays.asList("AKT1"));

		List<Gene> result = geneMyBatisRepository.getGeneListByHugoSymbols(hugoSymbols);

		Assert.assertEquals(1, result.size());
    }
    
    @Test
    public void getGeneList() {
                List hugoSymbols = new ArrayList<>(Arrays.asList("AKT1", "AKT2", "AKT3"));
                
		List<Gene> result = geneMyBatisRepository.getGeneListByHugoSymbols(hugoSymbols);

		Assert.assertEquals(3, result.size());
    }
}
