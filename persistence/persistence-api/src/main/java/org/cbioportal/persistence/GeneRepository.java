/*
 * Copyright (c) 2019 Memorial Sloan Kettering Cancer Center.
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
package org.cbioportal.persistence;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneAlias;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface GeneRepository {

    @Cacheable("StaticRepositoryCacheOne")
    List<Gene> getAllGenes(String keyword, String alias, String projection, Integer pageSize, Integer pageNumber, String sortBy, 
                           String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaGenes(String keyword, String alias);
    
    @Cacheable("StaticRepositoryCacheOne")
    Gene getGeneByGeneticEntityId(Integer geneticEntityId);

    @Cacheable("StaticRepositoryCacheOne")
    Gene getGeneByEntrezGeneId(Integer entrezGeneId);

    @Cacheable("StaticRepositoryCacheOne")
    Gene getGeneByHugoGeneSymbol(String hugoGeneSymbol);

    @Cacheable("StaticRepositoryCacheOne")
    List<String> getAliasesOfGeneByEntrezGeneId(Integer entrezGeneId);

    @Cacheable("StaticRepositoryCacheOne")
    List<String> getAliasesOfGeneByHugoGeneSymbol(String hugoGeneSymbol);

    // not cached because this is called only a single time, during @PostConstruct method of GeneServiceImpl
    List<GeneAlias> getAllAliases();

    @Cacheable("GeneralRepositoryCache")
    List<Gene> fetchGenesByEntrezGeneIds(List<Integer> entrezGeneIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    List<Gene> fetchGenesByHugoGeneSymbols(List<String> hugoGeneSymbols, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaGenesByEntrezGeneIds(List<Integer> entrezGeneIds);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaGenesByHugoGeneSymbols(List<String> hugoGeneSymbols);
}
