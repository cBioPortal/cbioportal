/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.scripts;

import java.text.DecimalFormat;
import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Calculates Mutation Frequencies.
 */
public class CalculateMutationFrequencies {

    public static void main(String[] args) throws Exception {
        HashMap<CanonicalGene, HashSet<Integer>> mutationMap = new HashMap<CanonicalGene, HashSet<Integer>>();
        ProgressMonitor.setConsoleModeAndParseShowProgress(args);

        if (args.length < 2) {
            System.out.println(
                "You must specify a stable case set id and stable cancer study id."
            );
            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
            return;
        }

        String sampleSetName = args[0];
        DaoSampleList daoSampleList = new DaoSampleList();
        SampleList sampleSet = daoSampleList.getSampleListByStableId(
            sampleSetName
        );
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(
            args[1]
        );
        HashSet<Integer> masterSampleSet = new HashSet<Integer>();
        // NOTE - as of 12/12/14, patient lists contain sample ids
        masterSampleSet.addAll(
            InternalIdUtil.getInternalNonNormalSampleIds(
                cancerStudy.getInternalId(),
                sampleSet.getSampleList()
            )
        );

        if (sampleSet == null) {
            System.out.println(
                "Patient set id:  " +
                sampleSetName +
                " does not exist in database."
            );
            return;
        }

        ProgressMonitor.setCurrentMessage(
            "Using patient set:  " + sampleSet.getName()
        );
        ProgressMonitor.setCurrentMessage(
            "Number of patients:  " + sampleSet.getSampleList().size()
        );

        //  Delete all Existing Mutation Frequency Records
        ProgressMonitor.setCurrentMessage(
            "Deleting all existing mutation frequency records"
        );

        //  Get all mutations
        ArrayList<ExtendedMutation> mutationList = DaoMutation.getAllMutations();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        ProgressMonitor.setCurrentMessage("Getting All Mutations...");
        //  Iterate through all mutation records
        for (ExtendedMutation mutation : mutationList) {
            long entrezGeneId = mutation.getEntrezGeneId();
            CanonicalGene gene = daoGene.getGene(entrezGeneId);

            //  Only count the mutation if it's in the selected case set,
            //  and it's somatic.
            if (masterSampleSet.contains(mutation.getSampleId())) {
                if (
                    mutation.getMutationStatus().equalsIgnoreCase("SOMATIC") ||
                    mutation.getMutationStatus().equalsIgnoreCase("UNKNOWN")
                ) {
                    //  Store in mutation map
                    if (mutationMap.containsKey(gene)) {
                        HashSet<Integer> mutationCaseSet = mutationMap.get(
                            gene
                        );
                        mutationCaseSet.add(mutation.getSampleId());
                    } else {
                        HashSet<Integer> mutationCaseSet = new HashSet<Integer>();
                        mutationCaseSet.add(mutation.getSampleId());
                        mutationMap.put(gene, mutationCaseSet);
                    }
                }
            }
        }

        ArrayList<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
        for (CanonicalGene gene : mutationMap.keySet()) {
            HashSet<Integer> mutationCaseSet = mutationMap.get(gene);
            gene.setSomaticMutationFrequency(
                mutationCaseSet.size() /
                (double) sampleSet.getSampleList().size()
            );
            geneList.add(gene);
        }
        Collections.sort(geneList, new SingleGeneComparator());

        ProgressMonitor.setCurrentMessage("Here are all genes mutated > 4%:");
        DecimalFormat formatter = new DecimalFormat("#,###,###.###");

        for (int i = 0; i < geneList.size(); i++) {
            CanonicalGene gene = geneList.get(i);
            if (gene.getSomaticMutationFrequency() > .01) {
                System.out.println(
                    gene.getHugoGeneSymbolAllCaps() +
                    "\t" +
                    formatter.format(gene.getSomaticMutationFrequency())
                );
            }
        }

        ProgressMonitor.setCurrentMessage("Storing results to database.");
        for (int i = 0; i < geneList.size(); i++) {
            CanonicalGene gene = geneList.get(i);
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
        if (
            gene1.getSomaticMutationFrequency() >
            gene2.getSomaticMutationFrequency()
        ) {
            return -1;
        } else {
            return +1;
        }
    }
}
