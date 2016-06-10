package org.cbioportal.persistence;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Mutation;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@Configurable
public class MutationRepositoryTest {
	
	@Autowired
    private MutationRepository mutationRepository;

	@Test
    public void testGetAllMutations() {
		
		List<String> geneticProfileStableIds = new ArrayList<String>();
		List<String> sampleStableIds = new ArrayList<String>();
		String sampleListStableId = "study_tcga_pub_sequenced";
		List<String> hugoGeneSymbols = new ArrayList<String>();
		List<Mutation> result = mutationRepository.getMutations(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds, sampleListStableId);
    	
		Assert.assertNotNull(result);
		Assert.assertEquals(3, result.size());
    }

	@Test
    public void testGetMutationsForKRAS() {
		
		List<String> geneticProfileStableIds = new ArrayList<String>();
		List<String> sampleStableIds = new ArrayList<String>();
		String sampleListStableId = "study_tcga_pub_sequenced";
		List<String> hugoGeneSymbols = new ArrayList<String>();
		hugoGeneSymbols.add("KRAS");
		List<Mutation> result = mutationRepository.getMutations(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds, sampleListStableId);
    	
		Assert.assertNotNull(result);
		Assert.assertEquals(0, result.size());
    }

	@Test
    public void testGetMutationsForBRCA1() {
		
		List<String> geneticProfileStableIds = new ArrayList<String>();
		List<String> sampleStableIds = new ArrayList<String>();
		String sampleListStableId = "study_tcga_pub_sequenced";
		List<String> hugoGeneSymbols = new ArrayList<String>();
		hugoGeneSymbols.add("BRCA1");
		List<Mutation> result = mutationRepository.getMutations(geneticProfileStableIds, hugoGeneSymbols, sampleStableIds, sampleListStableId);
    	
		Assert.assertNotNull(result);
		Assert.assertEquals(3, result.size());
    }
}
