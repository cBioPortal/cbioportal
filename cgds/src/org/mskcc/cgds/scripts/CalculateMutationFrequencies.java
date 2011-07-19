package org.mskcc.cgds.scripts;

import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.CaseList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Collections;
import java.text.DecimalFormat;

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
        DaoMutation daoMutation = DaoMutation.getInstance();
        ArrayList<ExtendedMutation> mutationList = daoMutation.getAllMutations();
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

        pMonitor.setCurrentMessage("Here are the top 10 most frequency mutated genes:");
        DecimalFormat formatter = new DecimalFormat("#,###,###.###");

        for (int i=0; i< 10; i++) {
            CanonicalGene gene = geneList.get(i);
            System.out.println (gene.getHugoGeneSymbol() + "\t"
                    + formatter.format(gene.getSomaticMutationFrequency()));
        }

        pMonitor.setCurrentMessage("Storing results to database.");
        for (int i=0; i<geneList.size(); i++) {
            CanonicalGene gene = geneList.get(i);
            daoMutationFrequency.addGene(gene.getEntrezGeneId(), gene.getSomaticMutationFrequency(),
                    caseSet.getCancerStudyId());
        }

    }
}

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
