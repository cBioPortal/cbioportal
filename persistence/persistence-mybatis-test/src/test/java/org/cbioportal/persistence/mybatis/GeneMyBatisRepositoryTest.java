/*
 * Copyright (c) 2016 Memorial Sloan Kettering Cancer Center.
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
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.Gene;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author jiaojiao
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class GeneMyBatisRepositoryTest {
    @Autowired
    private GeneMyBatisRepository geneMyBatisRepository;

    @Test
    public void getEmptyGeneList() {
                List hugo_gene_symbol = new ArrayList<>();
		List<Gene> result = geneMyBatisRepository.getGeneListByHugoSymbols(hugo_gene_symbol);
		Assert.assertEquals(0, result.size());
    }
    
    @Test
    public void getSingleGene() {
                List hugo_gene_symbol = new ArrayList<>(Arrays.asList("AKT1"));
		List<Gene> result = geneMyBatisRepository.getGeneListByHugoSymbols(hugo_gene_symbol);
		Assert.assertEquals(1, result.size());
    }
    
    @Test
    public void getGeneList() {
                List hugo_gene_symbol = new ArrayList<>(Arrays.asList("AKT1", "AKT2", "AKT3"));
		List<Gene> result = geneMyBatisRepository.getGeneListByHugoSymbols(hugo_gene_symbol);
		Assert.assertEquals(3, result.size());
    }
}
