/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.cbioportal.model.SV;
import org.cbioportal.persistence.SVRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author jake
 */
@RunWith(MockitoJUnitRunner.class)
public class SVServiceImplTest {
    
    @InjectMocks
    private SVServiceImpl svService;
    
    @Mock
    private SVRepository svRepository;
    
    @Test
    public void getSV() throws Exception{
        ArrayList<Integer> testGeneticProfileStableIds = new ArrayList<>();
        testGeneticProfileStableIds.add(0);
        ArrayList<String> testHugoGeneSymbols = new ArrayList<>();
        testHugoGeneSymbols.add("test_hugo_gene_symbol");
        ArrayList<String> testSampleStableIds = new ArrayList<>();
        testSampleStableIds.add("test_sample_stable_id");
        String testSampleList = "test_sample_list";
        
        List<SV> expectedSVList = new ArrayList<>();
        SV expectedSV = new SV();
        expectedSVList.add(expectedSV);
        
        Mockito.when(svRepository.getSVs(testGeneticProfileStableIds, testHugoGeneSymbols, testSampleStableIds));
        
        List<SV> resultSVList = svService.getSVs(testGeneticProfileStableIds, testHugoGeneSymbols, testSampleStableIds);
        
        Assert.assertEquals(expectedSVList, resultSVList);
    }
}
