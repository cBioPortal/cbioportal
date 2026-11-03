/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import java.util.*;

/**
 * A HashMap of ExtendedMutation Objects, indexed by Gene and Sample ID.
 *
 * @author Ethan Cerami.
 */
public class ExtendedMutationMap {
    private static final String DELIMITER = ":";
    private HashMap <String, ArrayList<ExtendedMutation>> mutationCaseMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();
    private HashMap <String, ArrayList<ExtendedMutation>> mutationMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();
    private ArrayList<Integer> sampleList;

    public ExtendedMutationMap (ArrayList<ExtendedMutation> mutationList,
            ArrayList<Integer> sampleList) {
        this.sampleList = sampleList;
        for (ExtendedMutation mutation:  mutationList) {
            String key = getKey(mutation.getGeneSymbol(), mutation.getSampleId());
            appendToMap(key, mutation, mutationCaseMap);
            appendToMap(mutation.getGeneSymbol(), mutation, mutationMap);
        }
    }

    /**
     * Gets all Extended Mutations, associated with the specified Gene / Sample ID combination.
     * @param geneSymbol    Gene Symbol.
     * @param sampleId        Case ID.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public ArrayList <ExtendedMutation> getExtendedMutations(String geneSymbol, Integer sampleId) {
        String key = getKey(geneSymbol.toUpperCase(), sampleId);
        return mutationCaseMap.get(key);
    }

    /**
     * Gets all Extended Mutations, associated with the specified Gene.
     * @param geneSymbol    Gene Symbol.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public ArrayList <ExtendedMutation> getExtendedMutations(String geneSymbol) {
        return mutationMap.get(geneSymbol.toUpperCase());
    }

    public int getNumExtendedMutations(String geneSymbol) {
        ArrayList<ExtendedMutation> mutationList = mutationMap.get(geneSymbol.toUpperCase());
        if (mutationList == null) {
            return 0;
        } else {
            return mutationList.size();
        }
    }

    public int getNumGenesWithExtendedMutations() {
        return mutationMap.keySet().size();
    }

    public ArrayList<Integer> getSampleList() {
        return sampleList;
    }

    private void appendToMap(String key, ExtendedMutation mutation,
            HashMap <String, ArrayList<ExtendedMutation>> map) {
        if (map.containsKey(key)) {
            ArrayList<ExtendedMutation> currentList = map.get(key);
            currentList.add(mutation);
        } else {
            ArrayList <ExtendedMutation> currentList = new ArrayList <ExtendedMutation> ();
            currentList.add(mutation);
            map.put(key, currentList);
        }
    }

    private String getKey(String geneSymbol, Integer sampleId) {
        return geneSymbol + DELIMITER + sampleId;
    }
}
