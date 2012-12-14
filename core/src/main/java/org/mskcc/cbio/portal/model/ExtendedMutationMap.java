/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.model;

import java.util.ArrayList;
import java.util.HashMap;
import org.mskcc.cbio.cgds.model.ExtendedMutation;

/**
 * A HashMap of ExtendedMutation Objects, indexed by Gene and Case ID.
 *
 * @author Ethan Cerami.
 */
public class ExtendedMutationMap {
    private static final String DELIMITER = ":";
    private HashMap <String, ArrayList<ExtendedMutation>> mutationCaseMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();
    private HashMap <String, ArrayList<ExtendedMutation>> mutationMap =
            new HashMap<String, ArrayList<ExtendedMutation>>();
    private ArrayList<String> caseList;

    public ExtendedMutationMap (ArrayList<ExtendedMutation> mutationList,
            ArrayList<String> caseList) {
        this.caseList = caseList;
        for (ExtendedMutation mutation:  mutationList) {
            String key = getKey(mutation.getGeneSymbol(), mutation.getCaseId());
            appendToMap(key, mutation, mutationCaseMap);
            appendToMap(mutation.getGeneSymbol(), mutation, mutationMap);
        }
    }

    /**
     * Gets all Extended Mutations, associated with the specified Gene / Case ID combination.
     * @param geneSymbol    Gene Symbol.
     * @param caseId        Case ID.
     * @return ArrayList of ExtendedMutation Objects.
     */
    public ArrayList <ExtendedMutation> getExtendedMutations(String geneSymbol, String caseId) {
        String key = getKey(geneSymbol.toUpperCase(), caseId);
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

    public ArrayList<String> getCaseList() {
        return caseList;
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

    private String getKey(String geneSymbol, String caseId) {
        return geneSymbol + DELIMITER + caseId;
    }
}