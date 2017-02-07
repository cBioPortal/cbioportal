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

import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GeneRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GeneMyBatisRepository implements GeneRepository {

    @Autowired
    private GeneMapper geneMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<Gene> getAllGenes(String alias, String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                  String direction) {

        return geneMapper.getGenes(alias, projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), 
            sortBy, direction);
    }

    @Override
    public BaseMeta getMetaGenes(String alias) {

        return geneMapper.getMetaGenes(alias);
    }

    @Override
    public Gene getGeneByEntrezGeneId(Integer entrezGeneId) {

        return geneMapper.getGeneByEntrezGeneId(entrezGeneId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public Gene getGeneByHugoGeneSymbol(String hugoGeneSymbol) {

        return geneMapper.getGeneByHugoGeneSymbol(hugoGeneSymbol, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<String> getAliasesOfGeneByEntrezGeneId(Integer entrezGeneId) {

        return geneMapper.getAliasesOfGeneByEntrezGeneId(entrezGeneId);
    }

    @Override
    public List<String> getAliasesOfGeneByHugoGeneSymbol(String hugoGeneSymbol) {

        return geneMapper.getAliasesOfGeneByHugoGeneSymbol(hugoGeneSymbol);
    }


    @Override
    public List<Gene> fetchGenesByEntrezGeneIds(List<Integer> entrezGeneIds, String projection) {

        return geneMapper.getGenesByEntrezGeneIds(entrezGeneIds, projection);
    }

    @Override
    public List<Gene> fetchGenesByHugoGeneSymbols(List<String> hugoGeneSymbols, String projection) {

        return geneMapper.getGenesByHugoGeneSymbols(hugoGeneSymbols, projection);
    }

    @Override
    public BaseMeta fetchMetaGenesByEntrezGeneIds(List<Integer> entrezGeneIds) {

        return geneMapper.getMetaGenesByEntrezGeneIds(entrezGeneIds);
    }

    @Override
    public BaseMeta fetchMetaGenesByHugoGeneSymbols(List<String> hugoGeneSymbols) {

        return geneMapper.getMetaGenesByHugoGeneSymbols(hugoGeneSymbols);
    }

	@Override
	public List<Gene> getGenesByGenesetId(String genesetId) {
		
		return geneMapper.getGenesByGenesetId(genesetId, "SUMMARY");
	}
}
