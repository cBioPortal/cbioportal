package org.mskcc.cgds.model;

import java.util.ArrayList;
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
        HashMap<String, GeneticProfile> defaultProfileSet = new HashMap<String, GeneticProfile>();
        conditionallyAddProfileToSet(getDefaultCnaProfile(), defaultProfileSet);
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
        if (geneticAlterationType.equals(GeneticAlterationType.COPY_NUMBER_ALTERATION)) {
            addCopyNumberProfile(geneticProfile);
        } else if (geneticAlterationType.equals(GeneticAlterationType.MUTATION_EXTENDED)) {
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
        String name = copyNumberProfile.getProfileName();
        if (name.contains(GISTIC)) {
            addGisticProfile(copyNumberProfile);
        } else if (name.contains(RAE)) {
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