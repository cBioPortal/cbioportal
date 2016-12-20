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

import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.GenesetRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GenesetMyBatisRepository implements GenesetRepository {

    @Autowired
    GenesetMapper genesetMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

	@Override
	public List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber, String sortBy,
			String direction) {
		
		return genesetMapper.getGenesets(projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), sortBy,
                direction);
	}

	@Override
	public BaseMeta getMetaGenesets() {

		return genesetMapper.getMetaGenesets();
	}

	@Override
	public Geneset getGenesetByGenesetId(String genesetId) {
		
		return genesetMapper.getGenesetByGenesetId(genesetId, PersistenceConstants.DETAILED_PROJECTION);
	}

	@Override
	public Geneset getGenesetByGeneticEntityId(Integer entityId) {
		
		return genesetMapper.getGenesetByGeneticEntityId(entityId, PersistenceConstants.DETAILED_PROJECTION);
	}
	
	@Override
	public List<Geneset> fetchGenesetsByGenesetIds(List<String> genesetIds, String projection) {
		
		return genesetMapper.getGenesetsByGenesetIds(genesetIds, projection);
	}

	@Override
	public BaseMeta fetchMetaGenesetsByGenesetIds(List<String> genesetIds) {
		// TODO Auto-generated method stub
		return null;
	}
}
