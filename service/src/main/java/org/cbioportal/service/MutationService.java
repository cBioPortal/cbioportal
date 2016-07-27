/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.persistence.dto.AltCount;
import org.mskcc.cbio.portal.dao.DaoException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MutationService {

    /**
     * Complete implementation to provide mutation data to
     * Web API endpoint /api/geneticprofiledata.
     * @param geneticProfileStableIds find only mutations from these genetic profiles. Unrecognized profile ids are silently ignored.
     * @param hugoGeneSymbols find only mutations in these genes. Unrecognized gene ids are silently ignored.
     * @param sampleStableIds find only mutations in specified samples. Empty list returns all unless restricted by sampleListStableId.
     * @param sampleListStableId find only mutations in a sample in any of the identified sample lists. If sampleStableIds was provided, this argument is ignored. If empty, return all.
     */
    List<Mutation> getMutationsDetailed(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols,
                                        List<String> sampleStableIds, String sampleListStableId);

    Map<String,List> getMutationMatrix(List<String> sampleStableIds, String mutationGeneticProfileStableId,
                                       String mrnaGeneticProfileStableId, String cnaGeneticProfileStableId,
                                       String drugType) throws IOException, DaoException;

    Map<String, Integer> getMutationCount(String mutationGeneticProfileStableId, List<String> sampleStableIds);

    List<Map<String,Object>> getSmg(String mutationGeneticProfileStableId, List<String> sampleStableIds) throws DaoException;

    /**
     * Provides mutation data to
     * Web API endpoint /api/mutationsforprofilesandgenes
     * @param geneticProfileStableIds profileIds from which to retrieve mutations
     * @param hugoGeneSymbols only retrieve mutations involving a gene in this list
     * @param sampleSetIds a list of sample set identifiers. If provided, mutations are filtered to only those which involve a sample in one of the identified sample sets.
     * @param sampleIdsKeys a list of sample set lookup keys. If provided, mutations are filtered to only those which involve a sample in one of the identified sample sets. If sampleSetIds is not empty, this argument is ignored.
     * @param providedSampleIds an explicit list of samples. If provided, and if sampleSetIds and sampleIdsKeys are empty, then only mutations involving these samples are returned. If all three parameters are empty, then mutations are not filtered by sample.
     */
    List<?> getMutationsForProfilesAndGenes(List<String> geneticProfileStableIds, List<String> hugoGeneSymbols, List<String> providedSampleIds, List<String> sampleSetIds, List<String> sampleIdsKeys);

    /**
     * Provides mutation data to
     * Web API endpoint /api/mutationsforstudiesandgenes
     * @param cancerStudies studies from which to retrieve mutations
     * @param hugoGeneSymbols only retrieve mutations involving a gene in this list
     * @param dataPriority use 1 to retrieve events from the default mutation map, 2 to retrieve events from the default cna map. Any other value will retrieve events from the default combination (mutation + cna) map.
     */
    List<?> getMutationsForStudiesAndGenes(List<String> hugoGeneSymbols, int dataPriority, List<String> cancerStudies);
}
