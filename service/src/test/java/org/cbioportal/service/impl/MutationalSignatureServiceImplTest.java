package org.cbioportal.service.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationalSignature;
import org.cbioportal.model.SNPCount;
import org.cbioportal.persistence.GeneticProfileRepository;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

@RunWith(MockitoJUnitRunner.class)
public class MutationalSignatureServiceImplTest {
	@InjectMocks
    private MutationalSignatureServiceImpl mutationalSignatureService;
	
	@Mock
    private GeneticProfileRepository geneticProfileRepository;
    @Test
    public void noContextSignaturesTest() throws Exception {
	    List<SNPCount> testSNPCounts = new LinkedList<>();
	    testSNPCounts.add(new SNPCount("sample1", "A", "T", 15));
	    testSNPCounts.add(new SNPCount("sample1", "C", "T", 5));
	    testSNPCounts.add(new SNPCount("sample1", "G", "T", 20));
	    testSNPCounts.add(new SNPCount("sample1", "C", "T", 40));
	    testSNPCounts.add(new SNPCount("sample1", "T", "C", 1));
	    
	    List<String> sampleIds = new LinkedList<>();
	    sampleIds.add("sample1");
	    
	    List<MutationalSignature> signatures = MutationalSignatureServiceImpl.MutationalSignatureFactory.NoContextSignatures(testSNPCounts, sampleIds);
	    Assert.assertTrue(signatures.size() == 1);
	    MutationalSignature signature = signatures.get(0);
	    Assert.assertTrue(signature.getSample().equals("sample1"));
	    Assert.assertTrue(Arrays.equals(signature.getMutationTypes(), new String[]{"C>A", "C>G", "C>T", "T>A", "T>C", "T>G"}));
	    Assert.assertTrue(Arrays.equals(signature.getCounts(), new int[]{20, 0, 45, 15, 1, 0}));
	    
	    Assert.assertTrue(MutationalSignatureServiceImpl.MutationalSignatureFactory.NoContextSignatures(new LinkedList<SNPCount>(), sampleIds).size() == sampleIds.size());
    }
    
    @Test
    public void noProfileExists() throws Exception {
	    Mockito.when(geneticProfileRepository.getAllGeneticProfilesInStudy(org.mockito.Matchers.anyString(),org.mockito.Matchers.anyString(),org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyInt(), org.mockito.Matchers.anyString(), org.mockito.Matchers.anyString())).thenReturn(new LinkedList<GeneticProfile>());
		    
	    Assert.assertTrue(mutationalSignatureService.getMutationalSignatures("NONEXISTENT-STUDY").size() == 0);
	    Assert.assertTrue(mutationalSignatureService.getMutationalSignaturesBySampleIds("NONEXISTENT-STUDY", new LinkedList<String>()).size() == 0);
    }
}
