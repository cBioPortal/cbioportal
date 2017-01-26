/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Categorized Set of Genetic Profiles.
 *
 * @author Ethan Cerami
 */
public class CategorizedGeneticProfileSet {
    private static final String GISTIC = "GISTIC";
    private static final String RAE = "RAE";

    private ArrayList<GeneticProfile> mutationProfileList = new ArrayList<GeneticProfile>();
    private ArrayList<GeneticProfile> gisticProfileList = new ArrayList<GeneticProfile>();
    private ArrayList<GeneticProfile> raeProfileList = new ArrayList<GeneticProfile>();
    private ArrayList<GeneticProfile> otherCnaProfileList = new ArrayList<GeneticProfile>();

    /**
     * Constructor.
     *
     * @param geneticProfileList All genetic profiles associated with a cancer study.
     */
    public CategorizedGeneticProfileSet(ArrayList<GeneticProfile> geneticProfileList) {
        for (GeneticProfile currentProfile : geneticProfileList) {
            addGeneticProfile(currentProfile);
        }
    }

    /**
     * Gets the Default Copy Number Profile.
     *
     * An individual cancer study may have multiple copy number profiles associated with it.
     * It is sometimes useful, however to have a single "default" copy number profile.
     *
     * This default copy number profile is determined by the following order of precedence rules:
     *
     * - Any GISTIC profile.
     * - Any RAE profile.
     * - Any other CNA profile.
     *
     * @return GeneticProfile Object.
     */
    public GeneticProfile getDefaultCnaProfile() {
        ArrayList<ArrayList<GeneticProfile>> orderOfPredenceList
                = new ArrayList<ArrayList<GeneticProfile>>();
        orderOfPredenceList.add(gisticProfileList);
        orderOfPredenceList.add(raeProfileList);
        // Now prioritize the ones that are shown in the analysis tab
        Collections.sort(otherCnaProfileList, new Comparator<GeneticProfile>() {
            @Override
            public int compare(GeneticProfile geneticProfileA, GeneticProfile geneticProfileB) {
                int a = geneticProfileA.showProfileInAnalysisTab() ? 1 : 0;
                int b = geneticProfileB.showProfileInAnalysisTab() ? 1 : 0;

                return b-a;
            }
        });
        orderOfPredenceList.add(otherCnaProfileList);
        return getFirstPriorityProfile(orderOfPredenceList);
    }

    /**
     * Gets the Default mutation profile.
     *
     * An individual cancer study may have multiple mutation profiles associated with it.
     * It is sometimes useful, however to have a single "default" mutation profile.
     *
     * The default mutation profile is defined as the first mutation profile associated
     * with the associated cancer study.
     *
     * @return GeneticProfile Object.
     */
    public GeneticProfile getDefaultMutationProfile() {
        if (mutationProfileList.size() > 0) {
            return mutationProfileList.get(0);
        } else {
            return null;
        }
    }

    /**
     * Gets a hashMap of the default mutation and copy number profiles.
     *
     * @return HashMap of Genetic Profiles, indexed by their stable IDs.
     */
    public HashMap<String, GeneticProfile> getDefaultMutationAndCopyNumberMap() {
        return getDefaultGeneticProfileMap(true, true);
    }

    /**
     * Gets a hashMap of the default mutation profiles.
     *
     * @return HashMap of Genetic Profiles, indexed by their stable IDs.
     */
    public HashMap<String, GeneticProfile> getDefaultMutationMap() {
        return getDefaultGeneticProfileMap(false, true);
    }

    /**
     * Gets a hashMap of the default copy number profiles.
     *
     * @return HashMap of Genetic Profiles, indexed by their stable IDs.
     */
    public HashMap<String, GeneticProfile> getDefaultCopyNumberMap() {
        return getDefaultGeneticProfileMap(true, false);
    }

    /**
     * Gets a hashMap of the default mutation and copy number profiles.
     * Provides functionality to exclude one of the profile types.
     *
     * @return HashMap of Genetic Profiles, indexed by their stable IDs.
     */
    private HashMap<String, GeneticProfile> getDefaultGeneticProfileMap(boolean includeCNA, boolean includeMutation) {
        HashMap<String, GeneticProfile> defaultProfileSet = new HashMap<String, GeneticProfile>();

        if(includeCNA)
            conditionallyAddProfileToSet(getDefaultCnaProfile(), defaultProfileSet);

        if(includeMutation)
            conditionallyAddProfileToSet(getDefaultMutationProfile(), defaultProfileSet);

        return defaultProfileSet;
    }

    /**
     * Gets number of default mutation and copy number profiles.
     *
     * @return number of default genetic profiles.
     */
    public int getNumDefaultMutationAndCopyNumberProfiles() {
        return getDefaultMutationAndCopyNumberMap().size();
    }

    private void addGeneticProfile(GeneticProfile geneticProfile) {
        GeneticAlterationType geneticAlterationType = geneticProfile.getGeneticAlterationType();
        if (geneticAlterationType == GeneticAlterationType.COPY_NUMBER_ALTERATION) {
            addCopyNumberProfile(geneticProfile);
        } else if (geneticAlterationType == GeneticAlterationType.MUTATION_EXTENDED) {
            addMutationProfile(geneticProfile);
        }
    }

    private void conditionallyAddProfileToSet(GeneticProfile geneticProfile,
            HashMap<String, GeneticProfile> defaultProfileSet) {
        if (geneticProfile != null) {
            defaultProfileSet.put(geneticProfile.getStableId(), geneticProfile);
        }
    }

    private void addMutationProfile(GeneticProfile mutationProfile) {
        mutationProfileList.add(mutationProfile);
    }

    private void addCopyNumberProfile(GeneticProfile copyNumberProfile) {
    	//Using stableId as this is currently the more reliable than the name for example,
    	//since stableId is strictly validated before loading into DB:
        String stableId = copyNumberProfile.getStableId();
        if (stableId.toLowerCase().endsWith(GISTIC.toLowerCase())) {
            addGisticProfile(copyNumberProfile);
        } else if (stableId.toLowerCase().endsWith(RAE.toLowerCase())) {
            addRaeProfile(copyNumberProfile);
        } else {
            addOtherCnaProfile(copyNumberProfile);
        }
    }

    private void addGisticProfile(GeneticProfile gisticProfile) {
        gisticProfileList.add(gisticProfile);
    }

    private void addRaeProfile(GeneticProfile raeProfile) {
        raeProfileList.add(raeProfile);
    }

    private void addOtherCnaProfile(GeneticProfile otherCnaProfile) {
        otherCnaProfileList.add(otherCnaProfile);
    }

    private GeneticProfile getFirstPriorityProfile(ArrayList<ArrayList<GeneticProfile>>
            orderOfPredenceList) {
        for (ArrayList<GeneticProfile> currentList:  orderOfPredenceList) {
            if (currentList.size() > 0) {
                return currentList.get(0);
            }
        }
        return null;
    }
}
