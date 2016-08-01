package org.cbioportal.persistence.mybatis;

import java.util.LinkedList;
import java.util.List;
import junit.framework.Assert;

import org.cbioportal.model.CosmicCount;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class CosmicCountMyBatisRepositoryTest {

	@Autowired
    private CosmicCountMyBatisRepository cosmicCountMyBatisRepository;
	
	@Test
	public void getCOSMICCountsByKeywordsEmptyQuery() {
		List<CosmicCount> result = cosmicCountMyBatisRepository.getCOSMICCountsByKeywords(new LinkedList<String>());
		Assert.assertEquals(6, result.size());
	}
	
	@Test
	public void getCOSMICCountsByKeywordsSingleKeyword() {
		List<String> keywords = new LinkedList<>();
		keywords.add("OR4F5 D45 missense");
		List<CosmicCount> result = cosmicCountMyBatisRepository.getCOSMICCountsByKeywords(keywords);
		Assert.assertEquals(1, result.size());
		CosmicCount count = result.get(0);
		Assert.assertEquals("3677745", count.getCosmicMutationId());
		Assert.assertEquals("OR4F5 D45 missense", count.getKeyword());
		Assert.assertEquals((Integer)1, count.getCount());
		Assert.assertEquals("D45A", count.getProteinChange());
	}
	
	@Test
	public void getCOSMICCountsByKeywordsMultipleKeywords() {
		List<String> keywords = new LinkedList<>();
		keywords.add("OR4F5 D45 missense");
		keywords.add("SAMD11 P23 silent");
		keywords.add("NOC2L S146 silent");
		List<CosmicCount> result = cosmicCountMyBatisRepository.getCOSMICCountsByKeywords(keywords);
		Assert.assertEquals(3, result.size());
	}
	
	@Test
	public void getCOSMICCountsByKeywordsDuplicateKeywords() {
		List<String> keywords = new LinkedList<>();
		keywords.add("OR4F5 D45 missense");
		keywords.add("SAMD11 P23 silent");
		keywords.add("SAMD11 P23 silent");
		keywords.add("SAMD11 P23 silent");
		List<CosmicCount> result = cosmicCountMyBatisRepository.getCOSMICCountsByKeywords(keywords);
		Assert.assertEquals(2, result.size());
	}
	
	@Test
	public void getCOSMICCountsByKeywordsReusedKeyword() {
		List<String> keywords = new LinkedList<>();
		keywords.add("NOC2L truncating");
		List<CosmicCount> result = cosmicCountMyBatisRepository.getCOSMICCountsByKeywords(keywords);
		Assert.assertEquals(2, result.size());
	}
}
