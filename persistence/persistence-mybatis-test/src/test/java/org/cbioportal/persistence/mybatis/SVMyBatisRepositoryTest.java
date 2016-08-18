/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.SV;
import org.cbioportal.persistence.SVRepository;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

/**
 *
 * @author jake
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@Configurable
public class SVMyBatisRepositoryTest {
    @Autowired
    SVRepository svRepository;
    
    @Test
    public void testGetAllSV(){
        List<String> geneticProfileStableIds = new ArrayList<>();
        List<String> sampleStableIds = new ArrayList<>();
        sampleStableIds.add("mskimpact_sv");
        List<String> hugoGeneSymbols = new ArrayList<>();
        List<SV> result = svRepository.getSV(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }
    
    @Test
    public void testGetSVForERBB2(){
        List<String> geneticProfileStableIds = new ArrayList<>();
        List<String> sampleStableIds = new ArrayList<>();
        String sampleStableId = "mskimpact_sv";
        sampleStableIds.add(sampleStableId);
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("ERBB2");
        List<SV> result = svRepository.getSV(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }
    
    @Test
    public void testGetSVForKRAS(){
        List<String> geneticProfileStableIds = new ArrayList<>();
        List<String> sampleStableIds = new ArrayList<>();
        String sampleStableId = "mskimpact_sv";
        sampleStableIds.add(sampleStableId);
        List<String> hugoGeneSymbols = new ArrayList<>();
        hugoGeneSymbols.add("KRAS");
        List<SV> result = svRepository.getSV(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds);
        
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.size());
    }
}
