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

package org.cbioportal.service.impl;

import java.util.*;
import org.apache.log4j.Logger;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.web_api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of getMutationsForStudiesAndGenes, which supplies mutations for Web API endpoint /api/mutationsforstudiesandgenes.
 */
@Service
public class MutationsForStudiesAndGenes {
    private static final Logger logger = Logger.getLogger(MutationsForStudiesAndGenes.class);
    private AccessControl accessControl; // access to cancer studies
    @Autowired
    private MutationDataUtils mutationDataUtils;

    public void initHelpers() {
        accessControl = SpringUtil.getAccessControl();
    }

    /**
     * entrypoint to service.
     * Filters requested list of cancerStudies according to user authorties,
     * then aggregates mutations from default genetic profiles (selected by
     * data_priority argument) and filtered to those involving a gene in the
     * list supplied in hugoGeneSymbols.
     */
    public List<?> get(List<String> hugoGeneSymbols, int dataTypePriority, List<String> cancerStudies) {
        if (accessControl == null) {
            initHelpers();
        }
        ArrayList<?> result = new ArrayList<Object>();
        if (dataTypePriority != 1 && dataTypePriority != 2) {
            dataTypePriority = 0;
        }
        HashSet<String> cancerStudySet = new HashSet<String>(cancerStudies);
        try {
            List<CancerStudy> cancerStudiesList = accessControl.getCancerStudies();
            for (CancerStudy cancerStudy : cancerStudiesList) {
                if (!cancerStudySet.contains(cancerStudy.getCancerStudyStableId())) {
                    continue;
                }
                String cancerStudyId = cancerStudy.getCancerStudyStableId();
                if (cancerStudyId.equalsIgnoreCase("all")) {
                    continue;
                }
                //  Get all Genetic Profiles Associated with this Cancer Study ID.
                ArrayList<GeneticProfile> geneticProfileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);
                //  Get all Patient Lists Associated with this Cancer Study ID.
                ArrayList<SampleList> sampleSetList = GetSampleLists.getSampleLists(cancerStudyId);
                //  Get the default patient set
                AnnotatedSampleSets annotatedSampleSets = new AnnotatedSampleSets(sampleSetList, dataTypePriority);
                SampleList defaultSampleSet = annotatedSampleSets.getDefaultSampleList();
                if (defaultSampleSet == null) {
                    continue;
                }
                List<String> sampleList = defaultSampleSet.getSampleList();
                //  Get the default genomic profiles
                CategorizedGeneticProfileSet categorizedGeneticProfileSet = new CategorizedGeneticProfileSet(geneticProfileList);
                // TODO: move AnnotatedSampleSets and CategorizedGeneticProfileSet from core to service module once these dependencies move to service:
                //      ./core/src/main/java/org/mskcc/cbio/portal/servlet/CrossCancerJSON.java
                //      ./core/src/main/java/org/mskcc/cbio/portal/servlet/LinkOut.java
                HashMap<String, GeneticProfile> defaultGeneticProfileSet = null;
                switch (dataTypePriority) {
                    case 2:
                        defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultCopyNumberMap();
                        break;
                    case 1:
                        defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultMutationMap();
                        break;
                    case 0:
                    default:
                        defaultGeneticProfileSet = categorizedGeneticProfileSet.getDefaultMutationAndCopyNumberMap();
                }
                for (GeneticProfile profile : defaultGeneticProfileSet.values()) {
                    if(!profile.getGeneticAlterationType().equals(GeneticAlterationType.MUTATION_EXTENDED)) {
                        continue;
                    }
                    // add mutation data for each genetic profile
                    result.addAll(mutationDataUtils.getMutationData(profile.getStableId(), hugoGeneSymbols, sampleList));
                }
            }
        } catch (DaoException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }
}
