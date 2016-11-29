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

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Gene;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface GeneMapper {

    List<Gene> getGenes(@Param("projection") String projection,
                        @Param("limit") Integer limit,
                        @Param("offset") Integer offset,
                        @Param("sortBy") String sortBy,
                        @Param("direction") String direction);

    BaseMeta getMetaGenes();

    Gene getGeneByEntrezGeneId(@Param("entrezGeneId") Integer entrezGeneId,
                               @Param("projection") String projection);

    Gene getGeneByHugoGeneSymbol(@Param("hugoGeneSymbol") String hugoGeneSymbol,
                                 @Param("projection") String projection);
    
    Gene getGeneByGeneticEntityId(@Param("geneticEntityId") Integer geneticEntityId,
            @Param("projection") String projection);

    List<String> getAliasesOfGeneByEntrezGeneId(@Param("entrezGeneId") Integer entrezGeneId);

    List<String> getAliasesOfGeneByHugoGeneSymbol(@Param("hugoGeneSymbol") String hugoGeneSymbol);

    List<Gene> getGenesByEntrezGeneIds(@Param("entrezGeneIds") List<Integer> entrezGeneIds,
                                       @Param("projection") String projection);

    List<Gene> getGenesByHugoGeneSymbols(@Param("hugoGeneSymbols") List<String> hugoGeneSymbols,
                                         @Param("projection") String projection);

    BaseMeta getMetaGenesByEntrezGeneIds(@Param("entrezGeneIds") List<Integer> entrezGeneIds);

    BaseMeta getMetaGenesByHugoGeneSymbols(@Param("hugoGeneSymbols") List<String> hugoGeneSymbols);
}
