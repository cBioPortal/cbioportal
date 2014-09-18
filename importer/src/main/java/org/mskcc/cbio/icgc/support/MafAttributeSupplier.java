

package org.mskcc.cbio.icgc.support;

import com.google.common.base.Supplier;
import com.google.inject.internal.Lists;
import java.util.List;

/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
public class MafAttributeSupplier implements Supplier<List<String>> {
    /**
     * responsible for supplying an instantiated list of MAF column attributes
     * source: https://wiki.nci.nih.gov/display/TCGA/Mutation+Annotation+Format+(MAF)+Specification
     */
   
    public MafAttributeSupplier() {
        
    }
    @Override
    public List<String> get() {
       return  Lists.newArrayList(
                "Hugo_Symbol",  //1
                "Entrez_Gene_Id", //2
                "Center", //3
                "NCBI_Build", //4
                "Chromosome", //5
                "Start_Position", //6
                "End_Position",    //7
                "Strand",   //8
                "Variant_Classification",  //9
                "Variant_Type",  //10
                "Reference_Allele",  //11
                "Tumor_Seq_Allele1", //12
                "Tumor_Seq_Allele2", //13
                "dbSNP_RS",  //14
                "dbSNP_Val_Status", //15
                "Tumor_Sample_Barcode", //16
                "Matched_Norm_Sample_Barcode", //17
                "Match_Norm_Seq_Allele1", //18
                "Match_Norm_Seq_Allele2",  //19
                "Tumor_Validation_Allele1", //20
                "Tumor_Validation_Allele2", //21
                "Match_Norm_Validation_Allele1",  //22
                "Match_Norm_Validation_Allele2",  //23
                "Verification_Status", //24
                "Validation_Status", //25
                "Mutation_Status",  //26
                "Sequencing_Phase",  //27
                "Sequence_Source", //28
                "Validation_Method", // 29
                "Score",  //30
                "BAM_File", //31
                "Sequencer" , //32
                "Tumor_Sample_UUID", //33
                "Matched_Norm_Sample_UUID",  //34
               "t_alt_count",  //  mskcc Variant allele count (tumor).
               "t_ref_count", // mskcc  Reference allele count (total - tumor)
               "n_alt_count",  //  mskcc Variant allele count (normal).
               "n_ref_count" // Reference allele count (normal)
                );
    }

}
