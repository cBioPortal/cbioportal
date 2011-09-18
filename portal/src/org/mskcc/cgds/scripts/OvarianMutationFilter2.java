package org.mskcc.cgds.scripts;

import org.mskcc.cgds.model.ExtendedMutation;

import java.util.HashSet;

// todo: discard
public class OvarianMutationFilter2 {

    /**
     * Indicates whether the specified mutation should be accepted as input to the CGDS Database.
     *
     * @param mutation  Extended Mutation.
     * @return true or false.
     */
    public static boolean acceptMutation (ExtendedMutation mutation) {
        HashSet <Long> significantlyMutatedGeneSet = new HashSet<Long>();
        significantlyMutatedGeneSet.add(7157L);     //  TP53
        significantlyMutatedGeneSet.add(672L);      //  BRCA1
        significantlyMutatedGeneSet.add(5925L);     //  RB1
        significantlyMutatedGeneSet.add(114788L);   //  CSMD3
        significantlyMutatedGeneSet.add(4763L);     //  NF1
        significantlyMutatedGeneSet.add(51755L);    //  CDK12
        significantlyMutatedGeneSet.add(120114L);   //  FAT3
        significantlyMutatedGeneSet.add(2559L);     //  GABRA6
        significantlyMutatedGeneSet.add(675L);      //  BRCA2
        significantlyMutatedGeneSet.add(2736L);     //  GLI2

        String validationStatus = mutation.getValidationStatus();
        String mutationType = mutation.getMutationType();
        String mutationStatus = mutation.getMutationStatus();
        String center = mutation.getSequencingCenter();

        boolean returnValue = false;

        // Only allow validated mutations or mutations within Significantly Mutated Genes
        if ((mutation.getValidationStatus() != null 
                && validationStatus.equalsIgnoreCase("valid"))
                || significantlyMutatedGeneSet.contains(mutation.getEntrezGeneId())) {

            //  Do *not* input Silent Mutations
            if (mutationType.equalsIgnoreCase("Silent")) {
                return false;
            }

            //  Do *not* input Intronic Mutations
            if (mutationType.equalsIgnoreCase("Intron")) {
                return false;
            }

            //  Do *not* input LOH Mutations
            if (mutationStatus.equalsIgnoreCase("LOH")) {
                return false;
            }

            // Do *not* input mutations which have been validated as Wildtype
            if (mutationStatus.equalsIgnoreCase("Wildtype")) {
                return false;
            }

            //  For Germline Mutations:  Only Import BRCA1/BRCA2
            if (mutationStatus.equalsIgnoreCase("Germline")) {
                if (mutationType.equalsIgnoreCase("Missense_Mutation")) {
                    return false;
                }
                long entrezGeneId = mutation.getEntrezGeneId();
                if (entrezGeneId == 672 || entrezGeneId == 675) {
                    return true;
                } else {
                    return false;
                }
            }

            //  Allow everything else in
            return true;
        } else {
            return false;
        }
    }
}
