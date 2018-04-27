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

import org.cbioportal.model.ReferenceGenome;
import org.cbioportal.model.ReferenceGenomeGene;
import org.cbioportal.model.Gene;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class ReferenceGenomeGeneMyBatisRepositoryTest {

    @Autowired
    private ReferenceGenomeGeneMyBatisRepository refGeneMyBatisRepository;

    @Autowired
    private GeneMyBatisRepository geneMyBatisRepository;
    
    @Test
    public void getAllGenesByGenomeName() throws Exception {

        List<ReferenceGenomeGene> result = refGeneMyBatisRepository.getAllGenesByGenomeName(ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);
        
        ReferenceGenomeGene refGene = result.get(0);
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
    }
    

    @Test
    public void getReferenceGenomeGene() throws Exception {
        String genomeName = ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME;
        ReferenceGenomeGene refGene = refGeneMyBatisRepository.getReferenceGenomeGene(207, genomeName);
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("14q32.33", refGene.getCytoband());
    }
    
    @Test
    public void getReferenceGenomeGeneByEntityId() throws Exception {
        ReferenceGenomeGene refGene = refGeneMyBatisRepository.getReferenceGenomeGeneByEntityId((Integer)2, "hg38");
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 208, gene.getEntrezGeneId());
        Assert.assertEquals("AKT2", gene.getHugoGeneSymbol());
        Assert.assertEquals("19q13.2", refGene.getCytoband());
    }
    

    @Test
    public void getGenesByGenomeName() throws Exception {

        List<Integer> geneIds = new ArrayList<>();
        geneIds.add((Integer)207);
        geneIds.add((Integer)208);

        List<ReferenceGenomeGene> result = refGeneMyBatisRepository.getGenesByGenomeName(geneIds, ReferenceGenome.HOMO_SAPIENS_DEFAULT_GENOME_NAME);

        Assert.assertEquals(2, result.size());
        ReferenceGenomeGene refGene = result.get(0);
        Gene gene = geneMyBatisRepository.getGeneByEntrezGeneId(refGene.getEntrezGeneId());
        Assert.assertEquals((Integer) 207, gene.getEntrezGeneId());
        Assert.assertEquals("AKT1", gene.getHugoGeneSymbol());
        Assert.assertEquals("14q32.33", refGene.getCytoband());
    }
    
}

