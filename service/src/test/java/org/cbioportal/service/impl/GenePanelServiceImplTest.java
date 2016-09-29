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
package org.cbioportal.service.impl;

import org.cbioportal.model.GenePanel;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GenePanelWithSamples;
import org.cbioportal.persistence.GenePanelRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;

/**
 *
 * @author heinsz
 */

@RunWith(MockitoJUnitRunner.class)
public class GenePanelServiceImplTest {
    
    @InjectMocks
    private GenePanelServiceImpl genePanelService;

    @Mock
    private GenePanelRepository genePanelRepository;
    
    private GenePanel genePanel1;
    private GenePanel genePanel2;
    private Gene egfr;
    private Gene braf;
    private Gene akt1;
    
    @Test
    public void getGenePanelDataByProfileAndGenes() throws Exception {
        genePanel1 = new GenePanel();
        genePanel1.setStableId("GENEPANEL2");
        genePanel1.setDescription("2 genes tested");
        genePanel1.setInternalId(1);

        genePanel2 = new GenePanel();
        genePanel2.setStableId("GENEPANEL3");
        genePanel2.setDescription("3 genes tested");
        genePanel2.setInternalId(2);

        List<Gene> genes = new ArrayList<>();
        braf  = new Gene();
        braf.setEntrezGeneId(673);
        braf.setHugoGeneSymbol("BRAF");
        braf.setType("protein-coding");
        braf.setCytoband("7q34");
        braf.setLength(4564);
        egfr = new Gene();
        egfr.setEntrezGeneId(1956);
        egfr.setHugoGeneSymbol("EGFR");
        egfr.setType("protein-coding");
        egfr.setCytoband("7p12");
        egfr.setLength(12961);
        akt1 = new Gene();
        akt1.setEntrezGeneId(207);
        akt1.setHugoGeneSymbol("AKT1");
        akt1.setType("protein-coding");
        akt1.setCytoband("12q32.32");
        akt1.setLength(10838);
        genes.add(braf);
        genes.add(egfr);
        genes.add(akt1);

        genePanel1.setGenes(genes);
        
        List<String> queryGenes = new ArrayList<>();
        queryGenes.add("BRAF");
        queryGenes.add("EGFR");
        
        List<GenePanelWithSamples> repositoryResponse = new ArrayList<>();
        GenePanelWithSamples genePanelWithSamples = new GenePanelWithSamples();
        genePanelWithSamples.setGenes(genes);
        repositoryResponse.add(genePanelWithSamples);
        
        Mockito.when(genePanelRepository.getGenePanelsByProfile("PROFILE1")).thenReturn(repositoryResponse);
        List<GenePanelWithSamples> serviceResponse = genePanelService.getGenePanelDataByProfileAndGenes("PROFILE1", queryGenes);
        Assert.assertTrue(serviceResponse.get(0).getGenes().size() == 2);
    }
}