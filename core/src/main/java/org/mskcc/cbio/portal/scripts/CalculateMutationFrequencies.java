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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.CaseList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;
import java.text.DecimalFormat;

/**
 * Calculates Mutation Frequencies.
 */
public class CalculateMutationFrequencies {

    public static void main(String[] args) throws Exception {
        HashMap <CanonicalGene, HashSet<String>> mutationMap = new HashMap<CanonicalGene, HashSet<String>>();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        if (args.length == 0) {
            System.out.println ("You must specify a stable case set id.");
            System.exit(-1);
        }

        String caseSetName = args[0];
        DaoCaseList daoCaseList = new DaoCaseList();
        CaseList caseSet = daoCaseList.getCaseListByStableId(caseSetName);
        HashSet <String> masterCaseSet = new HashSet<String>();
        masterCaseSet.addAll(caseSet.getCaseList());

        if (caseSet == null) {
            System.out.println ("Case set id:  " + caseSetName + " does not exist in database.");
            System.exit(-1);
        }
        pMonitor.setCurrentMessage("Using case set:  " + caseSet.getName());
        pMonitor.setCurrentMessage("Number of cases:  " + caseSet.getCaseList().size());

        //  Delete all Existing Mutation Frequency Records
        pMonitor.setCurrentMessage("Deleting all existing mutation frequency records");
        DaoMutationFrequency daoMutationFrequency = new DaoMutationFrequency();
        daoMutationFrequency.deleteAllRecords();

        //  Get all mutations
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getAllMutations();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        pMonitor.setCurrentMessage("Getting All Mutations...");
        //  Iterate through all mutation records
        for (ExtendedMutation mutation: mutationList) {
            long entrezGeneId = mutation.getEntrezGeneId();
            CanonicalGene gene = daoGene.getGene(entrezGeneId);

            //  Only count the mutation if it's in the selected case set,
            //  and it's somatic.
            if (masterCaseSet.contains(mutation.getCaseId())) {
                if (mutation.getMutationStatus().equalsIgnoreCase("SOMATIC")
                        || mutation.getMutationStatus().equalsIgnoreCase("UNKNOWN")) {
                    //  Store in mutation map
                    if (mutationMap.containsKey(gene)) {
                        HashSet <String> mutationCaseSet = mutationMap.get(gene);
                        mutationCaseSet.add(mutation.getCaseId());
                    } else {
                        HashSet <String> mutationCaseSet = new HashSet <String>();
                        mutationCaseSet.add(mutation.getCaseId());
                        mutationMap.put(gene, mutationCaseSet);
                    }
                }
            }
        }

        ArrayList <CanonicalGene> geneList = new ArrayList <CanonicalGene>();
        for (CanonicalGene gene:  mutationMap.keySet()) {
            HashSet <String> mutationCaseSet = mutationMap.get(gene);
            gene.setSomaticMutationFrequency(mutationCaseSet.size() / (double) caseSet.getCaseList().size());
            geneList.add(gene);
        }
        Collections.sort(geneList, new SingleGeneComparator());

        pMonitor.setCurrentMessage("Here are all genes mutated > 4%:");
        DecimalFormat formatter = new DecimalFormat("#,###,###.###");

        for (int i=0; i< geneList.size(); i++) {
            CanonicalGene gene = geneList.get(i);
            if (gene.getSomaticMutationFrequency() > .01) {
                System.out.println (gene.getHugoGeneSymbolAllCaps() + "\t"
                        + formatter.format(gene.getSomaticMutationFrequency()));
            }
        }

        pMonitor.setCurrentMessage("Storing results to database.");
        for (int i=0; i<geneList.size(); i++) {
            CanonicalGene gene = geneList.get(i);
            daoMutationFrequency.addGene(gene.getEntrezGeneId(), gene.getSomaticMutationFrequency(),
                    caseSet.getCancerStudyId());
        }

    }
}

/**
 * Comparison Class for Canonical Genes.
 */
class SingleGeneComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        CanonicalGene gene1 = (CanonicalGene) o1;
        CanonicalGene gene2 = (CanonicalGene) o2;
        if (gene1.getSomaticMutationFrequency() > gene2.getSomaticMutationFrequency()) {
            return -1;
        } else {
            return +1;
        }
    }
}
