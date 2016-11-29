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

package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticEntity.EntityType;
import org.cbioportal.model.meta.BaseMeta;

public interface GeneticDataService {

    BaseMeta getMetaGeneticDataInGeneticProfile(String geneticProfileId);

	List<GeneticData> getAllGeneticDataInGeneticProfile(String geneticProfileId, 
			String projectionName, Integer pageSize, Integer pageNumber);

	List<GeneticData> fetchGeneticDataInGeneticProfile(String geneticProfileId, EntityType geneticEntityType, List<String> geneticEntityIds, 
			String projectionName, Integer pageSize, Integer pageNumber);

	List<GeneticData> fetchGeneticDataInGeneticProfile(String geneticProfileId, EntityType geneticEntityType, List<String> geneticEntityIds, String caseListId,
			String projectionName, Integer pageSize, Integer pageNumber);

	List<GeneticData> fetchGeneticDataInGeneticProfile(String geneticProfileId, EntityType geneticEntityType, List<String> geneticEntityIds, List<String> caseIds, 
			String projectionName, Integer pageSize, Integer pageNumber);
}
