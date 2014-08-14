/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;

import java.util.*;
import java.text.DecimalFormat;

/**
 * Calculates Mutation Frequencies.
 */
public class CalculateMutationFrequencies {

    public static void main(String[] args) throws Exception {
        HashMap <CanonicalGene, HashSet<Integer>> mutationMap = new HashMap<CanonicalGene, HashSet<Integer>>();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        if (args.length != 2) {
            System.out.println ("You must specify a stable case set id and stable cancer study id.");
            return;
        }

        String patientSetName = args[0];
        DaoPatientList daoPatientList = new DaoPatientList();
        PatientList patientSet = daoPatientList.getPatientListByStableId(patientSetName);
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(args[1]);
        HashSet <Integer> masterSampleSet = new HashSet<Integer>();
        masterSampleSet.addAll(InternalIdUtil.getInternalNonNormalSampleIdsFromPatientIds(cancerStudy.getInternalId(), patientSet.getPatientList()));

        if (patientSet == null) {
            System.out.println ("Patient set id:  " + patientSetName + " does not exist in database.");
            return;
        }
        pMonitor.setCurrentMessage("Using patient set:  " + patientSet.getName());
        pMonitor.setCurrentMessage("Number of patients:  " + patientSet.getPatientList().size());

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
            if (masterSampleSet.contains(mutation.getSampleId())) {
                if (mutation.getMutationStatus().equalsIgnoreCase("SOMATIC")
                        || mutation.getMutationStatus().equalsIgnoreCase("UNKNOWN")) {
                    //  Store in mutation map
                    if (mutationMap.containsKey(gene)) {
                        HashSet <Integer> mutationCaseSet = mutationMap.get(gene);
                        mutationCaseSet.add(mutation.getSampleId());
                    } else {
                        HashSet <Integer> mutationCaseSet = new HashSet <Integer>();
                        mutationCaseSet.add(mutation.getSampleId());
                        mutationMap.put(gene, mutationCaseSet);
                    }
                }
            }
        }

        ArrayList <CanonicalGene> geneList = new ArrayList <CanonicalGene>();
        for (CanonicalGene gene:  mutationMap.keySet()) {
            HashSet <Integer> mutationCaseSet = mutationMap.get(gene);
            gene.setSomaticMutationFrequency(mutationCaseSet.size() / (double) patientSet.getPatientList().size());
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
                    patientSet.getCancerStudyId());
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
