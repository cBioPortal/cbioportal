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

import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.service.GeneService;
import org.cbioportal.service.exception.GeneNotFoundException;
import org.cbioportal.service.util.ChromosomeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeneServiceImpl implements GeneService {

    @Autowired
    private GeneRepository geneRepository;
    @Autowired
    private ChromosomeCalculator chromosomeCalculator;

    @Override
    public List<Gene> getAllGenes(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                  String direction) {

        List<Gene> geneList = geneRepository.getAllGenes(projection, pageSize, pageNumber, sortBy, direction);

        geneList.forEach(gene -> chromosomeCalculator.setChromosome(gene));
        return geneList;
    }

    @Override
    public BaseMeta getMetaGenes() {

        return geneRepository.getMetaGenes();
    }

    @Override
    public Gene getGene(String geneId) throws GeneNotFoundException {

        Gene gene;

        if (isInteger(geneId)) {
            gene = geneRepository.getGeneByEntrezGeneId(Integer.valueOf(geneId));
        } else {
            gene = geneRepository.getGeneByHugoGeneSymbol(geneId);
        }

        if (gene == null) {
            throw new GeneNotFoundException(geneId);
        }

        chromosomeCalculator.setChromosome(gene);
        return gene;
    }

    @Override
    public List<String> getAliasesOfGene(String geneId) {

        if (isInteger(geneId)) {
            return geneRepository.getAliasesOfGeneByEntrezGeneId(Integer.valueOf(geneId));
        } else {
            return geneRepository.getAliasesOfGeneByHugoGeneSymbol(geneId);
        }
    }

    @Override
    public List<Gene> fetchGenes(List<String> geneIds, String projection) {

        List<Integer> entrezGeneIds = new ArrayList<>();
        List<String> hugoGeneSymbols = new ArrayList<>();

        splitIdsByType(geneIds, entrezGeneIds, hugoGeneSymbols);

        List<Gene> geneList = geneRepository.fetchGenesByEntrezGeneIds(entrezGeneIds, projection);
        geneList.addAll(geneRepository.fetchGenesByHugoGeneSymbols(hugoGeneSymbols, projection));

        geneList.forEach(gene -> chromosomeCalculator.setChromosome(gene));
        return geneList;
    }

    @Override
    public BaseMeta fetchMetaGenes(List<String> geneIds) {

        List<Integer> entrezGeneIds = new ArrayList<>();
        List<String> hugoGeneSymbols = new ArrayList<>();

        splitIdsByType(geneIds, entrezGeneIds, hugoGeneSymbols);

        BaseMeta baseMeta = new BaseMeta();
        baseMeta.setTotalCount(geneRepository.fetchMetaGenesByEntrezGeneIds(entrezGeneIds).getTotalCount() +
                geneRepository.fetchMetaGenesByHugoGeneSymbols(hugoGeneSymbols).getTotalCount());

        return baseMeta;
    }

    private void splitIdsByType(List<String> geneIds, List<Integer> entrezGeneIds, List<String> hugoGeneSymbols) {

        for (String geneId : geneIds) {
            if (isInteger(geneId)) {
                entrezGeneIds.add(Integer.valueOf(geneId));
            } else {
                hugoGeneSymbols.add(geneId);
            }
        }
    }

    private boolean isInteger(String geneId) {
        return geneId.matches("^-?\\d+$");
    }
}
