/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.persistence.mybatis;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {GeneMyBatisRepository.class, TestConfig.class})
public class GeneMyBatisRepositoryTest {

    @Autowired
    private GeneMyBatisRepository geneMyBatisRepository;

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

        List<Gene> result = geneMyBatisRepository.getAllGenes(null, null, "SUMMARY", null, null, null, null);

        Assert.assertEquals(23, result.size());
        Gene gene = result.get(0);
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("protein-coding", gene.getType());
    }

    @Test
    public void getAllGenesDetailedProjection() throws Exception {

        List<Gene> result = geneMyBatisRepository.getAllGenes(null, null, "DETAILED", null, null, null, null);

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

        List<Gene> result = geneMyBatisRepository.getAllGenes(null, null, "SUMMARY", null, null, "hugoGeneSymbol", "ASC");

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

        List<Gene> result = geneMyBatisRepository.fetchGenesByHugoGeneSymbols(hugoGeneSymbols, "SUMMARY");

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
}
