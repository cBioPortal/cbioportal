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
package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.Geneset;
import org.cbioportal.model.meta.BaseMeta;

public interface GenesetRepository {

    List<Geneset> getAllGenesets(String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction);

    BaseMeta getMetaGenesets();

    Geneset getGenesetByGenesetId(String genesetId);
    
    Geneset getGenesetByGeneticEntityId(Integer entityId);

    List<Geneset> fetchGenesetsByGenesetIds(List<String> genesetIds, String projection);

    BaseMeta fetchMetaGenesetsByGenesetIds(List<String> genesetIds);
    
}
