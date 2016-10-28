package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.persistence.dto.AltCount;
import org.cbioportal.persistence.dto.KeywordSampleCount;
import org.cbioportal.persistence.dto.MutatedGeneSampleCount;
import org.cbioportal.persistence.dto.SignificantlyMutatedGene;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class MutationMyBatisRepositoryTest {

	@Autowired
    private MutationMyBatisRepository mutationMyBatisRepository;

	@Test
    public void getMutationsDetailedAllNull() {

		List<Mutation> result = mutationMyBatisRepository.getMutationsDetailed(null, null, null, null);

		Assert.assertEquals(9, result.size());
    }

	@Test
    public void getMutationsDetailedWithSampleListStableId() {

		List<Mutation> result = mutationMyBatisRepository.getMutationsDetailed(null, null, null,
				"study_tcga_pub_sequenced");

		Assert.assertEquals(5, result.size());
    }

	@Test
    public void getMutationsDetailedWithHugoGeneSymbol() {

		List<String> hugoGeneSymbols = new ArrayList<String>();
		hugoGeneSymbols.add("BRCA1");
		hugoGeneSymbols.add("AKT1");
		List<Mutation> result = mutationMyBatisRepository.getMutationsDetailed(null, hugoGeneSymbols, null, null);

		Assert.assertEquals(8, result.size());
    }

	@Test
	public void getMutationsDetailedWithSampleStableIds() {

		List<String> sampleStableIds = new ArrayList<String>();
		sampleStableIds.add("TCGA-A1-A0SB-01");
		sampleStableIds.add("TCGA-A1-A0SD-01");
		sampleStableIds.add("TCGA-A1-A0SH-01");
		List<Mutation> result = mutationMyBatisRepository.getMutationsDetailed(null, null, sampleStableIds, null);

		Assert.assertEquals(4, result.size());
	}

	@Test
	public void getMutationsDetailedWithGeneticProfileStableIds() {

		List<String> geneticProfileStableIds = new ArrayList<String>();
		geneticProfileStableIds.add("study_tcga_pub_mutations");
		List<Mutation> result = mutationMyBatisRepository.getMutationsDetailed(geneticProfileStableIds, null, null,
				null);

		Assert.assertEquals(9, result.size());
	}

	@Test
	public void getMutationsDetailedWithAll() {

		List<String> geneticProfileStableIds = new ArrayList<String>();
		geneticProfileStableIds.add("study_tcga_pub_mutations");

		List<String> sampleStableIds = new ArrayList<String>();
		sampleStableIds.add("TCGA-A1-A0SB-01");
		sampleStableIds.add("TCGA-A1-A0SD-01");
		sampleStableIds.add("TCGA-A1-A0SH-01");

		List<String> hugoGeneSymbols = new ArrayList<String>();
		hugoGeneSymbols.add("BRCA1");
		hugoGeneSymbols.add("AKT2");

		List<Mutation> result = mutationMyBatisRepository.getMutationsDetailed(geneticProfileStableIds, hugoGeneSymbols,
				sampleStableIds, "study_tcga_pub_sequenced");

		Assert.assertEquals(2, result.size());
	}

	@Test
	public void getMutationsAllNull() throws Exception {

		List<Mutation> result = mutationMyBatisRepository.getMutations(null, (List<Integer>) null, null);
		Assert.assertEquals(9, result.size());
	}

	@Test
	public void getMutationsWithSampleIds() throws Exception {

		List<Integer> sampleIds = new ArrayList<Integer>();
		sampleIds.add(6);
		sampleIds.add(12);

		List<Mutation> result = mutationMyBatisRepository.getMutations(sampleIds, (List<Integer>) null, null);
		Assert.assertEquals(3, result.size());
	}

	@Test
	public void getMutationsWithEntrezGeneIds() throws Exception {

		List<Integer> entrezGeneIds = new ArrayList<Integer>();
		entrezGeneIds.add(672);

		List<Mutation> result = mutationMyBatisRepository.getMutations(null, entrezGeneIds, null);
		Assert.assertEquals(6, result.size());
	}

	@Test
	public void getMutationsWithGeneticProfileId() throws Exception {

		List<Mutation> result = mutationMyBatisRepository.getMutations(null, (List<Integer>) null, 7);
		Assert.assertEquals(0, result.size());
	}

	@Test
	public void getMutationsWithAll() throws Exception {

		List<Integer> sampleIds = new ArrayList<Integer>();
		sampleIds.add(6);
		sampleIds.add(12);
		sampleIds.add(3);

		List<Integer> entrezGeneIds = new ArrayList<Integer>();
		entrezGeneIds.add(208);

		List<Mutation> result = mutationMyBatisRepository.getMutations(sampleIds, entrezGeneIds, 6);
		Assert.assertEquals(1, result.size());
	}

	@Test
	public void getSimplifiedMutations() throws Exception {

		List<Integer> sampleIds = new ArrayList<Integer>();
		sampleIds.add(6);
		sampleIds.add(12);
		List<Integer> entrezGeneIds = new ArrayList<Integer>();
		entrezGeneIds.add(672);

		List<Mutation> result = mutationMyBatisRepository.getSimplifiedMutations(sampleIds, entrezGeneIds, 6);
		Assert.assertEquals(3, result.size());
		Assert.assertNull(result.get(0).getMutationEvent());
	}

	@Test
	public void hasAlleleFrequencyDataTrue() throws Exception {

		Boolean result = mutationMyBatisRepository.hasAlleleFrequencyData(6, 6);
		Assert.assertTrue(result);
	}

	@Test
	public void hasAlleleFrequencyDataFalse() throws Exception {

		Boolean result = mutationMyBatisRepository.hasAlleleFrequencyData(6, 1);
		Assert.assertFalse(result);
	}

	@Test
	public void getSignificantlyMutatedGenesAllNull() throws Exception {

		List<SignificantlyMutatedGene> result = mutationMyBatisRepository
				.getSignificantlyMutatedGenes(6, null, null, 0, 0, false);

		Assert.assertEquals(3, result.size());
	}

	@Test
	public void getSignificantlyMutatedGenesWithSampleIds() throws Exception {

		List<Integer> sampleIds = new ArrayList<Integer>();
		sampleIds.add(6);
		sampleIds.add(12);

		List<SignificantlyMutatedGene> result = mutationMyBatisRepository
				.getSignificantlyMutatedGenes(6, null, sampleIds, 0, 0, false);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void getSignificantlyMutatedGenesWithEntrezGeneIds() throws Exception {

		List<Integer> entrezGeneIds = new ArrayList<Integer>();
		entrezGeneIds.add(672);

		List<SignificantlyMutatedGene> result = mutationMyBatisRepository
				.getSignificantlyMutatedGenes(6, entrezGeneIds, null, 0, 0, false);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void getSignificantlyMutatedGenesWithThresholdRecurrence() throws Exception {

		List<SignificantlyMutatedGene> result = mutationMyBatisRepository
				.getSignificantlyMutatedGenes(6, null, null, 2, 0, false);

		Assert.assertEquals(2, result.size());
	}

	@Test
	public void getSignificantlyMutatedGenesWithThresholdNumGenes() throws Exception {

		List<SignificantlyMutatedGene> result = mutationMyBatisRepository
				.getSignificantlyMutatedGenes(6, null, null, 0, 1, false);

		Assert.assertEquals(1, result.size());
	}

	@Test
	public void getSignificantlyMutatedGenesAll() throws Exception {

		List<Integer> sampleIds = new ArrayList<Integer>();
		sampleIds.add(6);
		sampleIds.add(12);

		List<Integer> entrezGeneIds = new ArrayList<Integer>();
		entrezGeneIds.add(672);

		List<SignificantlyMutatedGene> result = mutationMyBatisRepository
				.getSignificantlyMutatedGenes(6, entrezGeneIds, sampleIds, 2, 1, false);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals("6,6,12", result.get(0).getConcatenatedSampleIds());
		Assert.assertEquals((Integer) 3, result.get(0).getCount());
	}

	@Test
	public void countMutationEventsWithoutSampleIds() throws Exception {

		List<MutationCount> result = mutationMyBatisRepository.countMutationEvents(6, null);

		Assert.assertEquals(7, result.size());
		Assert.assertEquals((Integer) 1, result.get(0).getMutationCount());
		Assert.assertEquals((Integer) 2, result.get(3).getMutationCount());
	}

	@Test
	public void countMutationEventsWithSampleIds() throws Exception {

		List<Integer> sampleIds = new ArrayList<Integer>();
		sampleIds.add(6);
		sampleIds.add(12);

		List<MutationCount> result = mutationMyBatisRepository.countMutationEvents(6, sampleIds);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals((Integer) 2, result.get(0).getMutationCount());
		Assert.assertEquals((Integer) 1, result.get(1).getMutationCount());
	}

	@Test
	public void countSamplesWithMutatedGenesWithoutEntrezGeneIds() throws Exception {

		List<MutatedGeneSampleCount> result = mutationMyBatisRepository.countSamplesWithMutatedGenes(6, null);

		Assert.assertEquals(3, result.size());
		Assert.assertEquals((Integer) 208, result.get(0).getEntrezGeneId());
		Assert.assertEquals((Integer) 1, result.get(0).getCount());
		Assert.assertEquals((Integer) 207, result.get(1).getEntrezGeneId());
		Assert.assertEquals((Integer) 2, result.get(1).getCount());
		Assert.assertEquals((Integer) 672, result.get(2).getEntrezGeneId());
		Assert.assertEquals((Integer) 4, result.get(2).getCount());
	}

	@Test
	public void countSamplesWithMutatedGenesWithEntrezGeneIds() throws Exception {

		List<Integer> entrezGeneIds = new ArrayList<Integer>();
		entrezGeneIds.add(672);

		List<MutatedGeneSampleCount> result = mutationMyBatisRepository.countSamplesWithMutatedGenes(6, entrezGeneIds);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals((Integer) 672, result.get(0).getEntrezGeneId());
		Assert.assertEquals((Integer) 4, result.get(0).getCount());
	}

	@Test
	public void countSamplesWithKeywordsWithoutKeywords() throws Exception {

		List<KeywordSampleCount> result = mutationMyBatisRepository.countSamplesWithKeywords(6, null);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals("BRCA1 C61 missense", result.get(0).getKeyword());
		Assert.assertEquals((Integer) 2, result.get(0).getCount());
		Assert.assertEquals("BRCA1 truncating", result.get(1).getKeyword());
		Assert.assertEquals((Integer) 6, result.get(1).getCount());
	}

	@Test
	public void countSamplesWithKeywordsWithKeywords() throws Exception {

		List<String> keywords = new ArrayList<String>();
		keywords.add("BRCA1 truncating");

		List<KeywordSampleCount> result = mutationMyBatisRepository.countSamplesWithKeywords(6, keywords);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals("BRCA1 truncating", result.get(0).getKeyword());
		Assert.assertEquals((Integer) 6, result.get(0).getCount());
	}

	@Test
	public void getGenesOfMutationsWithoutMutationEventIds() throws Exception {

		List<Integer> result = mutationMyBatisRepository.getGenesOfMutations(null);

		Assert.assertEquals(3, result.size());
		Assert.assertEquals((Integer) 208, result.get(0));
		Assert.assertEquals((Integer) 207, result.get(1));
		Assert.assertEquals((Integer) 672, result.get(2));
	}

	@Test
	public void getGenesOfMutationsWithMutationEventIds() throws Exception {

		List<Integer> mutationEventIds = new ArrayList<Integer>();
		mutationEventIds.add(2038);
		mutationEventIds.add(2039);

		List<Integer> result = mutationMyBatisRepository.getGenesOfMutations(mutationEventIds);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals((Integer) 672, result.get(0));
	}

	@Test
	public void getKeywordsOfMutationsWithoutMutationEventIds() throws Exception {

		List<String> result = mutationMyBatisRepository.getKeywordsOfMutations(null);

		Assert.assertEquals(2, result.size());
		Assert.assertEquals("BRCA1 C61 missense", result.get(0));
		Assert.assertEquals("BRCA1 truncating", result.get(1));
	}

	@Test
	public void getKeywordsOfMutationsWithMutationEventIds() throws Exception {

		List<Integer> mutationEventIds = new ArrayList<Integer>();
		mutationEventIds.add(2038);
		mutationEventIds.add(2039);

		List<String> result = mutationMyBatisRepository.getKeywordsOfMutations(mutationEventIds);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals("BRCA1 truncating", result.get(0));
	}

	@Test
	public void getMutationsCountsWithTypeCount() throws Exception {

		List<AltCount> result = mutationMyBatisRepository.getMutationsCounts("count", "BRCA1", null, null, null,
				true);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals((Integer) 6, result.get(0).getCount());
	}

	@Test
	public void getMutationsCountsWithTypeFrequency() throws Exception {

		List<AltCount> result = mutationMyBatisRepository.getMutationsCounts("frequency", "BRCA1", null, null, null,
				true);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals((Double) 6.0, result.get(0).getFrequency());
	}

	@Test
	public void getMutationsCountsWithStartEnd() throws Exception {

		List<AltCount> result = mutationMyBatisRepository.getMutationsCounts("count", "BRCA1", 50, 100, null,
				true);

		Assert.assertEquals(1, result.size());
		Assert.assertEquals((Integer) 1, result.get(0).getCount());
	}

	@Test
	public void getMutationsCountsWithCancerStudyIds() throws Exception {

		List<String> cancerStudyIds = new ArrayList<String>();
		cancerStudyIds.add("nonexistent_study");

		List<AltCount> result = mutationMyBatisRepository.getMutationsCounts("count", "BRCA1", null, null,
				cancerStudyIds, true);

		Assert.assertEquals(0, result.size());
	}
}
