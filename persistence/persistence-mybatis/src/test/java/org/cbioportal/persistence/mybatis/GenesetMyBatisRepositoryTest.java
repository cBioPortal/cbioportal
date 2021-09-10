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

import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.Gene;
import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.config.TestConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {GenesetMyBatisRepository.class, TestConfig.class})
public class GenesetMyBatisRepositoryTest {

    @Autowired
    private GenesetMyBatisRepository genesetMyBatisRepository;

    @Test
    public void getAllGenesets() {
        //String projection, Integer pageSize, Integer pageNumber
        List<Geneset> result = genesetMyBatisRepository.getAllGenesets(PersistenceConstants.SUMMARY_PROJECTION, 10, 0);
        Assert.assertEquals(2, result.size());
        //expect: ordered ASC by geneset id:
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
        result = genesetMyBatisRepository.fetchGenesets(Arrays.asList("HINATA_NFKB_MATRIX","MORF_ATRX"));
        Assert.assertEquals(2, result.size());

        //test summary and ID projections:
        result = genesetMyBatisRepository.fetchGenesets(Arrays.asList("MORF_ATRX", "HINATA_NFKB_MATRIX","DUMMY"));
        Assert.assertEquals(2, result.size());
        Geneset geneset = result.get(0);
        //data is sorted ASC on geneset ID, so HINATA_NFKB_MATRIX is first:
        Assert.assertEquals("HINATA_NFKB_MATRIX", geneset.getGenesetId());
    }

    @Test
    public void getGenesByGenesetId() {
        //String genesetId
        List<Gene> genes = genesetMyBatisRepository.getGenesByGenesetId("HINATA_NFKB_MATRIX");
        Assert.assertEquals(2, genes.size());
        Gene gene = genes.get(0);
        Assert.assertEquals(369, gene.getEntrezGeneId().intValue());
        gene = genes.get(1);
        Assert.assertEquals(472, gene.getEntrezGeneId().intValue());
        
        genes = genesetMyBatisRepository.getGenesByGenesetId("MORF_ATRX");
        Assert.assertEquals(3, genes.size());
        gene = genes.get(0);
        Assert.assertEquals(1,207, gene.getEntrezGeneId().intValue());
        gene = genes.get(2);
        Assert.assertEquals(10000, gene.getEntrezGeneId().intValue());
    }
}