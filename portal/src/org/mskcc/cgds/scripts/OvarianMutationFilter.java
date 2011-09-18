package org.mskcc.cgds.scripts;

import org.mskcc.cgds.model.ExtendedMutation;

public class OvarianMutationFilter {

    /**
     * Indicates whether the specified mutation should be accepted as input to the CGDS Database.
     *
     * @param mutation  Extended Mutation.
     * @return true or false.
     */
    public static boolean acceptMutation (ExtendedMutation mutation) {

        String validationStatus = mutation.getValidationStatus();
        String mutationType = mutation.getMutationType();
        String mutationStatus = mutation.getMutationStatus();
        String center = mutation.getSequencingCenter();

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

        //  For Germline Mutations:  Only Import BRCA1/BRCA2
        //  Allow non-validated mutations from the Broad.
        //  Only Allow Validated mutations from Baylor and Wash U.
        if (mutationStatus.equalsIgnoreCase("Germline")) {
            if (mutationType.equalsIgnoreCase("Missense_Mutation")) {
                return false;
            }
            long entrezGeneId = mutation.getEntrezGeneId();
            if (entrezGeneId == 672 || entrezGeneId == 675) {
                if (center.equalsIgnoreCase("broad.mit.edu")) {
                    return true;
                } else {
                    if (validationStatus.equalsIgnoreCase("Germline")  
                            || validationStatus.equalsIgnoreCase("Valid")) {
                        //  Filter out these germline mutations (p.S694) from Baylor, because
                        //  we have strong reason to believe they are annotated incorrectly,
                        //  and are in fact silent mutations.
                        //  (Email from Mike McLellan (March 14, 2010).
                        if (entrezGeneId == 672 && center.equalsIgnoreCase("hgsc.bcm.edu")
                                && mutation.getAminoAcidChange().equalsIgnoreCase("p.S694")) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }
            }
        }

        //  For Somatic Mutations:
        //  Allow non-validated mutations from the Broad.
        //  Only Allow Validated mutations from Baylor and Wash U.
        if (mutationStatus.equalsIgnoreCase("Somatic") || mutationStatus.equalsIgnoreCase("Valid")) {
            if (center.equalsIgnoreCase("broad.mit.edu")) {
                return true;
            } else {
                if (validationStatus.equalsIgnoreCase("Valid")) {
                    return true;
                }
            }
        }
        return false;
    }
}
