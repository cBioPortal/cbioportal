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

import org.cbioportal.model.*;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import java.util.*;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author heinsz
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class GenePanelMyBatisRepositoryTest {
    String genePanelStableId = "mybatisTestPanel";
    
    @Autowired
    private GenePanelMyBatisRepository genePanelMyBatisRepository;
    
    @Test
    public void getGenePanelsByProfiles() {
        List<String> genes = new ArrayList<>();
        List<GenePanelWithSamples> result = genePanelMyBatisRepository.getGenePanelsByProfile("study_tcga_pub_gistic");
        Assert.assertEquals(2, result.size());
        Assert.assertEquals(3, result.get(0).getGenes().size());
    }
    
    @Test
    public void getGenePanelByStableId() {
        GenePanel result = genePanelMyBatisRepository.getGenePanelByStableId("TESTPANEL1").get(0);
        Assert.assertEquals("TESTPANEL1", result.getStableId());
    }
    
    @Test
    public void getGenePanels() {
        List<GenePanel> result = genePanelMyBatisRepository.getGenePanels();
        Assert.assertEquals(2, result.size());
    }
    
    @Test
    public void getGeneticProfileByStableId() {
        GeneticProfile result = genePanelMyBatisRepository.getGeneticProfileByStableId("study_tcga_pub_gistic");
        Assert.assertEquals((Integer) 2, result.getGeneticProfileId());
    }
    
    @Test
    public void getGeneByEntrezGeneId() {
        Gene result = genePanelMyBatisRepository.getGeneByEntrezGeneId(208);
        Assert.assertEquals("AKT2", result.getHugoGeneSymbol());
    }
    
    @Test
    public void getGeneByHugoSymbol() {
        Gene result = genePanelMyBatisRepository.getGeneByHugoSymbol("AKT1");
        Assert.assertEquals((Integer) 207, result.getEntrezGeneId());
    }
    
    @Test
    public void getGeneByAlias() {
        Gene result = genePanelMyBatisRepository.getGeneByAlias("BRCA1 alias");
        Assert.assertEquals((Integer) 675, result.getEntrezGeneId());
    }    
    
    @Test
    public void sampleProfileMappingExistsByProfile() {
        boolean result = genePanelMyBatisRepository.sampleProfileMappingExistsByProfile(2);
        Assert.assertTrue(result);
    }            
}
