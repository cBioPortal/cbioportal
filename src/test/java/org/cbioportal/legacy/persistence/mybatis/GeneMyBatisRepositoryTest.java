/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.mybatis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.cbioportal.legacy.AbstractLegacyTestcontainers;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.config.MyBatisLegacyConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Import({MyBatisLegacyConfig.class, GeneMyBatisRepository.class})
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractLegacyTestcontainers.Initializer.class)
public class GeneMyBatisRepositoryTest {

  @Autowired private GeneMyBatisRepository geneMyBatisRepository;

  @Test
  public void getAllGenesIdProjection() throws Exception {

    List<Gene> result = geneMyBatisRepository.getAllGenes(null, null, "ID", null, null, null, null);

    Assert.assertEquals(23, result.size());
    Gene gene = result.get(0);
    Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
    Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
  }

  @Test
  public void getAllGenesSummaryProjection() throws Exception {

    List<Gene> result =
        geneMyBatisRepository.getAllGenes(
            null, null, "SUMMARY", null, null, "hugoGeneSymbol", "ASC");

    Assert.assertEquals(23, result.size());
    Gene gene = result.get(0);
    Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
    Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
    Assert.assertEquals("protein-coding", gene.getType());
  }

  @Test
  public void getAllGenesDetailedProjection() throws Exception {

    List<Gene> result =
        geneMyBatisRepository.getAllGenes(
            null, null, "DETAILED", null, null, "hugoGeneSymbol", "ASC");

    Assert.assertEquals(23, result.size());
    Gene gene = result.get(0);
    Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
    Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
    Assert.assertEquals("protein-coding", gene.getType());
  }

  @Test
  public void getAllGenesSummaryProjection1PageSize() throws Exception {

    List<Gene> result = geneMyBatisRepository.getAllGenes(null, null, "SUMMARY", 1, 0, null, null);

    Assert.assertEquals(1, result.size());
  }

  @Test
  public void getAllGenesSummaryProjectionHugoGeneSymbolSort() throws Exception {

    List<Gene> result =
        geneMyBatisRepository.getAllGenes(
            null, null, "SUMMARY", null, null, "hugoGeneSymbol", "ASC");

    Assert.assertEquals(23, result.size());
    Assert.assertEquals("AKT1", result.get(0).getHugoGeneSymbol());
    Assert.assertEquals("AKT2", result.get(1).getHugoGeneSymbol());
    Assert.assertEquals("AKT3", result.get(2).getHugoGeneSymbol());
    Assert.assertEquals("ALK", result.get(3).getHugoGeneSymbol());
    Assert.assertEquals("ARAF", result.get(4).getHugoGeneSymbol());
    Assert.assertEquals("ATM", result.get(5).getHugoGeneSymbol());
    Assert.assertEquals("BRAF", result.get(6).getHugoGeneSymbol());
    Assert.assertEquals("SAMD11", result.get(21).getHugoGeneSymbol());
  }

  @Test
  public void getAllGenesWithKeywordSearch() throws Exception {

    List<Gene> result =
        geneMyBatisRepository.getAllGenes("AKT", null, "SUMMARY", null, null, null, null);

    Assert.assertEquals(3, result.size());
    Assert.assertEquals("AKT1", result.get(0).getHugoGeneSymbol());
    Assert.assertEquals("AKT2", result.get(1).getHugoGeneSymbol());
    Assert.assertEquals("AKT3", result.get(2).getHugoGeneSymbol());
  }

  @Test
  public void getAllGenesWithKeywordSearchCaseInsensitive() throws Exception {

    List<Gene> result =
        geneMyBatisRepository.getAllGenes("akt", null, "SUMMARY", null, null, null, null);

    Assert.assertEquals(3, result.size());
    Assert.assertEquals("AKT1", result.get(0).getHugoGeneSymbol());
    Assert.assertEquals("AKT2", result.get(1).getHugoGeneSymbol());
    Assert.assertEquals("AKT3", result.get(2).getHugoGeneSymbol());
  }

  @Test
  public void getAllGenesWithKeywordSearchAlphabeticalOrder() throws Exception {

    List<Gene> result =
        geneMyBatisRepository.getAllGenes("A", null, "SUMMARY", null, null, null, null);

    // Verify results are returned in alphabetical order by hugo gene symbol
    Assert.assertTrue(result.size() > 0);
    for (int i = 0; i < result.size() - 1; i++) {
      String current = result.get(i).getHugoGeneSymbol();
      String next = result.get(i + 1).getHugoGeneSymbol();
      Assert.assertTrue(
          "Expected " + current + " to come before " + next + " alphabetically",
          current.compareTo(next) <= 0);
    }
    // Verify specific ordering of genes starting with 'A'
    Assert.assertEquals("AKT1", result.get(0).getHugoGeneSymbol());
    Assert.assertEquals("AKT2", result.get(1).getHugoGeneSymbol());
    Assert.assertEquals("AKT3", result.get(2).getHugoGeneSymbol());
    Assert.assertEquals("ALK", result.get(3).getHugoGeneSymbol());
    Assert.assertEquals("ARAF", result.get(4).getHugoGeneSymbol());
    Assert.assertEquals("ATM", result.get(5).getHugoGeneSymbol());
  }

  @Test
  public void getMetaGenes() throws Exception {

    BaseMeta result = geneMyBatisRepository.getMetaGenes(null, null);

    Assert.assertEquals((Integer) 23, result.getTotalCount());
  }

  @Test
  public void getGeneByEntrezGeneIdNullResult() throws Exception {

    Gene result = geneMyBatisRepository.getGeneByEntrezGeneId(999);

    Assert.assertNull(result);
  }

  @Test
  public void getGeneByEntrezGeneId() throws Exception {

    Gene result = geneMyBatisRepository.getGeneByEntrezGeneId(207);

    Assert.assertEquals((Integer) 207, result.getEntrezGeneId());
    Assert.assertEquals("AKT1", result.getHugoGeneSymbol());
    Assert.assertEquals("protein-coding", result.getType());
  }

  @Test
  public void getGeneByHugoGeneSymbolNullResult() throws Exception {

    Gene result = geneMyBatisRepository.getGeneByHugoGeneSymbol("invalid_gene");

    Assert.assertNull(result);
  }

  @Test
  public void getGeneByHugoGeneSymbol() throws Exception {

    Gene result = geneMyBatisRepository.getGeneByHugoGeneSymbol("AKT1");

    Assert.assertEquals((Integer) 207, result.getEntrezGeneId());
    Assert.assertEquals("AKT1", result.getHugoGeneSymbol());
    Assert.assertEquals("protein-coding", result.getType());
  }

  @Test
  public void getAliasesOfGeneByEntrezGeneIdEmptyList() throws Exception {

    List<String> result = geneMyBatisRepository.getAliasesOfGeneByEntrezGeneId(208);

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void getAliasesOfGeneByEntrezGeneId() throws Exception {

    List<String> result = geneMyBatisRepository.getAliasesOfGeneByEntrezGeneId(207);
    result.sort(null);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals("AKT alias", result.get(0));
    Assert.assertEquals("AKT alias2", result.get(1));
  }

  @Test
  public void getAliasesOfGeneByHugoGeneSymbolEmptyList() throws Exception {

    List<String> result = geneMyBatisRepository.getAliasesOfGeneByHugoGeneSymbol("AKT2");

    Assert.assertEquals(0, result.size());
  }

  @Test
  public void getAliasesOfGeneByHugoGeneSymbol() throws Exception {

    List<String> result = geneMyBatisRepository.getAliasesOfGeneByHugoGeneSymbol("AKT1");
    result.sort(null);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals("AKT alias", result.get(0));
    Assert.assertEquals("AKT alias2", result.get(1));
  }

  @Test
  public void fetchGenesByEntrezGeneIds() throws Exception {

    List<Integer> entrezGeneIds = new ArrayList<>();
    entrezGeneIds.add(207);
    entrezGeneIds.add(208);

    List<Gene> result = geneMyBatisRepository.fetchGenesByEntrezGeneIds(entrezGeneIds, "SUMMARY");
    result = sortedResult(result);

    Assert.assertEquals(2, result.size());
    Gene gene = result.get(0);
    Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
    Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
    Assert.assertEquals("protein-coding", gene.getType());
  }

  @Test
  public void fetchGenesByHugoGeneSymbols() throws Exception {

    List<String> hugoGeneSymbols = new ArrayList<>();
    hugoGeneSymbols.add("AKT1");
    hugoGeneSymbols.add("AKT2");

    List<Gene> result =
        geneMyBatisRepository.fetchGenesByHugoGeneSymbols(hugoGeneSymbols, "SUMMARY");
    result = sortedResult(result);

    Assert.assertEquals(2, result.size());
    Gene gene = result.get(0);
    Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
    Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
    Assert.assertEquals("protein-coding", gene.getType());
  }

  @Test
  public void fetchMetaGenesByEntrezGeneIds() throws Exception {

    List<Integer> entrezGeneIds = new ArrayList<>();
    entrezGeneIds.add(207);
    entrezGeneIds.add(208);

    BaseMeta result = geneMyBatisRepository.fetchMetaGenesByEntrezGeneIds(entrezGeneIds);

    Assert.assertEquals((Integer) 2, result.getTotalCount());
  }

  @Test
  public void fetchMetaGenesByHugoGeneSymbol() throws Exception {

    List<String> hugoGeneSymbols = new ArrayList<>();
    hugoGeneSymbols.add("AKT1");
    hugoGeneSymbols.add("AKT2");

    BaseMeta result = geneMyBatisRepository.fetchMetaGenesByHugoGeneSymbols(hugoGeneSymbols);

    Assert.assertEquals((Integer) 2, result.getTotalCount());
  }

  private List<Gene> sortedResult(List<Gene> result) {
    return result.stream().sorted(Comparator.comparing(Gene::getHugoGeneSymbol)).toList();
  }
}
