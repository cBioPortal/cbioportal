package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.SNPCount;
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
public class MutationalSignatureMyBatisRepositoryTest {

	@Autowired
    private MutationalSignatureMyBatisRepository mutationalSignatureMyBatisRepository;
	
	@Test
	public void getSNPCountsByGeneticProfile() {
		List<SNPCount> snpCounts = mutationalSignatureMyBatisRepository.getSNPCounts("study_tcga_pub_mutations");
		
		Assert.assertEquals(8, snpCounts.size());
	}
	
	@Test
	public void getSNPCountsForSpecificSamples() {
		List<String> sampleIds1 = new LinkedList<>();
		sampleIds1.add("TCGA-A1-A0SH-01");
		List<SNPCount> snpCounts = mutationalSignatureMyBatisRepository.getSNPCounts("study_tcga_pub_mutations", sampleIds1);
		Assert.assertEquals(2, snpCounts.size());
		
		List<String> sampleIds2 = new LinkedList<>();
		sampleIds2.add("TCGA-A1-A0SO-01");
		snpCounts = mutationalSignatureMyBatisRepository.getSNPCounts("study_tcga_pub_mutations", sampleIds2);
		Assert.assertEquals(1, snpCounts.size());
		Assert.assertTrue(snpCounts.get(0).getSampleId().equals("TCGA-A1-A0SO-01"));
		Assert.assertTrue(snpCounts.get(0).getReferenceAllele().equals("C"));
		Assert.assertTrue(snpCounts.get(0).getTumorAllele().equals("T"));
		Assert.assertTrue(snpCounts.get(0).getCount() == 1);
		
		List<String> sampleIds3 = new LinkedList<>();
		sampleIds3.add("TCGA-A1-A0SI-01");
		snpCounts = mutationalSignatureMyBatisRepository.getSNPCounts("study_tcga_pub_mutations", sampleIds3);
		Assert.assertEquals(1, snpCounts.size());
		Assert.assertTrue(snpCounts.get(0).getSampleId().equals("TCGA-A1-A0SI-01"));
		Assert.assertTrue(snpCounts.get(0).getReferenceAllele().equals("G"));
		Assert.assertTrue(snpCounts.get(0).getTumorAllele().equals("A"));
		Assert.assertTrue(snpCounts.get(0).getCount() == 2);
		
		List<String> sampleIds4 = new LinkedList<>();
		sampleIds4.add("TCGA-A1-A0SP-01");
		sampleIds4.add("TCGA-A1-A0SB-01");
		sampleIds4.add("TCGA-A1-A0SD-01");
		sampleIds4.add("TCGA-A1-A0SE-01");
		snpCounts = mutationalSignatureMyBatisRepository.getSNPCounts("study_tcga_pub_mutations", sampleIds4);
		Assert.assertEquals(4, snpCounts.size());
	}
	
	@Test
	public void getSNPCountsEmptySampleList() {
		List<String> sampleIds = new LinkedList<>();
		List<SNPCount> snpCounts = mutationalSignatureMyBatisRepository.getSNPCounts("study_tcga_pub_mutations", sampleIds);
		
		Assert.assertEquals(8, snpCounts.size());
	}
}
