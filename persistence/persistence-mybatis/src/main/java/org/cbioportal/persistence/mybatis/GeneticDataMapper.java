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

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.GeneticData;
import org.cbioportal.model.GeneticDataSamples;
import org.cbioportal.model.GeneticDataValues;
import org.cbioportal.model.meta.BaseMeta;

public interface GeneticDataMapper {

	List<GeneticData> getGeneticData(@Param("geneticProfileIds") List<String> geneticProfileIds,
                            @Param("projection") String projection,
                            @Param("limit") Integer limit,
                            @Param("offset") Integer offset);
	
	List<GeneticDataValues> getGeneticDataValues(@Param("geneticProfileIds") List<String> geneticProfileIds,
			@Param("geneticEntityIds") List<Integer> geneticEntityIds,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);

	
	List<GeneticDataSamples> getGeneticDataSamples(@Param("geneticProfileIds") List<String> geneticProfileIds,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);


	BaseMeta getMetaGeneticData(@Param("geneticProfileIds") List<String> geneticProfileIds);

}
