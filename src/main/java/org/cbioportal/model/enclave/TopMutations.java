package org.cbioportal.model.enclave;

import java.util.ArrayList;
import java.util.List;

public class TopMutations {
    /* The count of Hugo symbols, on a per patient basis. */
    //public int count;

    public List<MutationCount> data = new ArrayList<>();

    /*  Represents patient mutation records (rows in the MAF file) of any unique Hugo/DNAChange/HGVSp symbol */
    //public int sampleCountErrorUpperBound;

    /* how many patient mutation records (rows in the MAF files) were left out of the response. If all unique values were aggregated, this number will be zero. */
    //public int sumRemainingCount;
}
