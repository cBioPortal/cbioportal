/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.mybatis;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.cbioportal.legacy.AbstractLegacyTestcontainers;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.Geneset;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.PersistenceConstants;
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
@Import({MyBatisLegacyConfig.class, GenesetMyBatisRepository.class})
@DataJpaTest
@DirtiesContext
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = AbstractLegacyTestcontainers.Initializer.class)
public class GenesetMyBatisRepositoryTest {

  @Autowired private GenesetMyBatisRepository genesetMyBatisRepository;

  @Test
  public void getAllGenesets() {
    // String projection, Integer pageSize, Integer pageNumber
    List<Geneset> result =
        genesetMyBatisRepository.getAllGenesets(PersistenceConstants.SUMMARY_PROJECTION, 10, 0);
    Assert.assertEquals(2, result.size());
    // expect: ordered ASC by geneset id:
    Geneset geneset = result.get(0);
    Assert.assertEquals("HINATA_NFKB_MATRIX", geneset.getGenesetId());
    Assert.assertEquals("https://hinata_link", geneset.getRefLink());
    geneset = result.get(1);
    Assert.assertEquals("MORF_ATRX", geneset.getGenesetId());
    Assert.assertEquals("Morf description", geneset.getDescription());
  }

  @Test
  public void getMetaGenesets() {

    BaseMeta result = genesetMyBatisRepository.getMetaGenesets();
    Assert.assertEquals(2, result.getTotalCount().intValue());
  }

  @Test
  public void getGeneset() {

    Geneset geneset = genesetMyBatisRepository.getGeneset("HINATA_NFKB_MATRIX");
    Assert.assertEquals("https://hinata_link", geneset.getRefLink());
  }

  @Test
  public void fetchGenesets() {

    List<Geneset> result = genesetMyBatisRepository.fetchGenesets(Arrays.asList("DUMMY"));
    Assert.assertEquals(0, result.size());
    result = genesetMyBatisRepository.fetchGenesets(Arrays.asList("MORF_ATRX"));
    Assert.assertEquals(1, result.size());
    result =
        genesetMyBatisRepository.fetchGenesets(Arrays.asList("HINATA_NFKB_MATRIX", "MORF_ATRX"));
    Assert.assertEquals(2, result.size());

    // test summary and ID projections:
    result =
        genesetMyBatisRepository.fetchGenesets(
            Arrays.asList("MORF_ATRX", "HINATA_NFKB_MATRIX", "DUMMY"));
    Assert.assertEquals(2, result.size());
    Geneset geneset = result.get(0);
    // data is sorted ASC on geneset ID, so HINATA_NFKB_MATRIX is first:
    Assert.assertEquals("HINATA_NFKB_MATRIX", geneset.getGenesetId());
  }

  @Test
  public void getGenesByGenesetId() {
    // String genesetId
    List<Gene> genes = genesetMyBatisRepository.getGenesByGenesetId("HINATA_NFKB_MATRIX");
    genes = sortedGenesResult(genes);
    Assert.assertEquals(2, genes.size());
    Gene gene = genes.get(0);
    Assert.assertEquals(369, gene.getEntrezGeneId().intValue());
    gene = genes.get(1);
    Assert.assertEquals(472, gene.getEntrezGeneId().intValue());

    genes = genesetMyBatisRepository.getGenesByGenesetId("MORF_ATRX");
    genes = sortedGenesResult(genes);
    Assert.assertEquals(3, genes.size());
    gene = genes.get(0);
    Assert.assertEquals(1, 207, gene.getEntrezGeneId().intValue());
    gene = genes.get(2);
    Assert.assertEquals(10000, gene.getEntrezGeneId().intValue());
  }

  private List<Gene> sortedGenesResult(List<Gene> result) {
    return result.stream().sorted(Comparator.comparing(Gene::getHugoGeneSymbol)).toList();
  }
}
