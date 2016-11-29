/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
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

import org.cbioportal.model.GeneticDataSamples;
import org.cbioportal.model.GeneticDataValues;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GeneticDataMyBatisRepository implements GeneticDataRepository {
	@Autowired
    private GeneticDataMapper geneticDataMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public List<GeneticDataValues> getGeneticDataValuesInGeneticProfile(String geneticProfileId, List<Integer> geneticEntityIds, Integer pageSize,
			Integer pageNumber) {
    	//get values csv:
        return geneticDataMapper.getGeneticDataValues(Arrays.asList(geneticProfileId), geneticEntityIds, pageSize, 
                offsetCalculator.calculate(pageSize, pageNumber));
    }

    @Override
    public GeneticDataSamples getGeneticDataSamplesInGeneticProfile(String geneticProfileId, Integer pageSize,
			Integer pageNumber) {
    	//get values csv:
        List<GeneticDataSamples> result = geneticDataMapper.getGeneticDataSamples(Arrays.asList(geneticProfileId), pageSize, 
                offsetCalculator.calculate(pageSize, pageNumber));
        if (result != null && result.size() > 0) {
        	//only one result item expected for this query: //TODO validate if size > 1?
        	return result.get(0);
        }
        return null;
    }

}
