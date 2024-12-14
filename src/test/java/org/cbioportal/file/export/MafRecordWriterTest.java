package org.cbioportal.file.export;

import org.cbioportal.file.model.MafRecord;
import org.junit.Test;

import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MafRecordWriterTest {

    StringWriter output = new StringWriter();
    MafRecordWriter writer = new MafRecordWriter(output);

    @Test
    public void testMafRecordWriter() {
        writer.write(List.of(
            new MafRecord(
                "HUGO",
                "12345", 
                "center1", 
                "hg38", 
                "X", 
                1000000L,
                1000100L,
                "+",
                "Missense_Mutation",
                "SNP",
                "T",
                "C",
                "A",
                "DBSNPRS123",
                "byFrequency",
                "SAMPLE_1",
                "SAMPLE_2",
                "A",
                "T",
                "C",
                "G",
                "A",
                "T",
                "Verified",
                "Somatic",
                "Somatic",
                "Phase1",
                "Exome",
                "Sanger",
                "1.1",
                "bam_file",
                "Illumina hiseq 2000",
                "SHRT",
                55,
                33,
                100,
                99
            )
        ).iterator());
        
        assertEquals("""
            Hugo_Symbol\tEntrez_Gene_Id\tCenter\tNCBI_Build\tChromosome\tStart_Position\tEnd_Position\tStrand\tVariant_Classification\tVariant_Type\tReference_Allele\tTumor_Seq_Allele1\tTumor_Seq_Allele2\tdbSNP_RS\tdbSNP_Val_Status\tTumor_Sample_Barcode\tMatched_Norm_Sample_Barcode\tMatch_Norm_Seq_Allele1\tMatch_Norm_Seq_Allele2\tTumor_Validation_Allele1\tTumor_Validation_Allele2\tMatch_Norm_Validation_Allele1\tMatch_Norm_Validation_Allele2\tVerification_Status\tValidation_Status\tMutation_Status\tSequencing_Phase\tSequence_Source\tValidation_Method\tScore\tBAM_File\tSequencer\tHGVSc_Short\tt_alt_count\tt_ref_count\tn_alt_count\tn_ref_count
            HUGO\t12345\tcenter1\thg38\tX\t1000000\t1000100\t+\tMissense_Mutation\tSNP\tT\tC\tA\tDBSNPRS123\tbyFrequency\tSAMPLE_1\tSAMPLE_2\tA\tT\tC\tG\tA\tT\tVerified\tSomatic\tSomatic\tPhase1\tExome\tSanger\t1.1\tbam_file\tIllumina hiseq 2000\tSHRT\t55\t33\t100\t99
            """, output.toString());
    }

    @Test
    public void testEmptyMafRecords() {
        List<MafRecord> emptyMafRecords = List.of();
        writer.write(emptyMafRecords.iterator());

        assertEquals("""
            Hugo_Symbol\tEntrez_Gene_Id\tCenter\tNCBI_Build\tChromosome\tStart_Position\tEnd_Position\tStrand\tVariant_Classification\tVariant_Type\tReference_Allele\tTumor_Seq_Allele1\tTumor_Seq_Allele2\tdbSNP_RS\tdbSNP_Val_Status\tTumor_Sample_Barcode\tMatched_Norm_Sample_Barcode\tMatch_Norm_Seq_Allele1\tMatch_Norm_Seq_Allele2\tTumor_Validation_Allele1\tTumor_Validation_Allele2\tMatch_Norm_Validation_Allele1\tMatch_Norm_Validation_Allele2\tVerification_Status\tValidation_Status\tMutation_Status\tSequencing_Phase\tSequence_Source\tValidation_Method\tScore\tBAM_File\tSequencer\tHGVSc_Short\tt_alt_count\tt_ref_count\tn_alt_count\tn_ref_count
            """, output.toString());
    }

}
