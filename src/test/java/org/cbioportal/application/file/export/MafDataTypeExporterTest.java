package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.exporters.MafDataTypeExporter;
import org.cbioportal.application.file.export.services.GeneticProfileService;
import org.cbioportal.application.file.export.services.MafRecordService;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

public class MafDataTypeExporterTest {
    GeneticProfileService geneticProfileService = new GeneticProfileService(null) {
        @Override
        public List<GeneticProfileDatatypeMetadata> getGeneticProfiles(String studyId, String geneticAlterationType, String datatype) {
            GeneticProfileDatatypeMetadata genProf = new GeneticProfileDatatypeMetadata();
            genProf.setCancerStudyIdentifier(studyId);
            genProf.setStableId("MAF_STABLE_ID");
            genProf.setGeneticAlterationType("MUTATION_EXTENDED");
            genProf.setDatatype("MAF");
            return List.of(genProf);
        }
    };

    @Test
    public void testMafDataTypeExport() {
        var factory = new InMemoryFileWriterFactory();

        MafRecordService mafRecordService = new MafRecordService(null) {
            @Override
            public CloseableIterator<MafRecord> getMafRecords(String molecularProfileStableId) {
                MafRecord mafRecord = new MafRecord();
                mafRecord.setHugoSymbol("HUGO");
                mafRecord.setEntrezGeneId("12345");
                mafRecord.setCenter("center1");
                mafRecord.setNcbiBuild("hg38");
                mafRecord.setChromosome("X");
                mafRecord.setStartPosition(1000000L);
                mafRecord.setEndPosition(1000100L);
                mafRecord.setStrand("+");
                mafRecord.setVariantClassification("Missense_Mutation");
                mafRecord.setVariantType("SNP");
                mafRecord.setReferenceAllele("T");
                mafRecord.setTumorSeqAllele1("C");
                mafRecord.setTumorSeqAllele2("A");
                mafRecord.setDbSnpRs("DBSNPRS123");
                mafRecord.setDbSnpValStatus("byFrequency");
                mafRecord.setTumorSampleBarcode("SAMPLE_1");
                mafRecord.setMatchedNormSampleBarcode("SAMPLE_2");
                mafRecord.setMatchNormSeqAllele1("A");
                mafRecord.setMatchNormSeqAllele2("T");
                mafRecord.setTumorValidationAllele1("C");
                mafRecord.setTumorValidationAllele2("G");
                mafRecord.setMatchNormValidationAllele1("A");
                mafRecord.setMatchNormValidationAllele2("T");
                mafRecord.setVerificationStatus("Verified");
                mafRecord.setValidationStatus("Somatic");
                mafRecord.setMutationStatus("Somatic");
                mafRecord.setSequencingPhase("Phase1");
                mafRecord.setSequenceSource("Exome");
                mafRecord.setValidationMethod("Sanger");
                mafRecord.setScore("1.1");
                mafRecord.setBamFile("bam_file");
                mafRecord.setSequencer("Illumina hiseq 2000");
                mafRecord.setHgvspShort("SHRT");
                mafRecord.settAltCount(55);
                mafRecord.settRefCount(33);
                mafRecord.setnAltCount(100);
                mafRecord.setnRefCount(99);

                return new SimpleCloseableIterator<>(List.of(mafRecord));
            }
        };

        MafDataTypeExporter mafDataTypeExporter = new MafDataTypeExporter(geneticProfileService, mafRecordService);

        mafDataTypeExporter.exportData(factory, "TEST_STUDY_ID");

        var fileContents = factory.getFileContents();
        assertEquals(Set.of("meta_mutation_extended_maf_maf_stable_id.txt", "data_mutation_extended_maf_maf_stable_id.txt"), fileContents.keySet());

        assertEquals("cancer_study_identifier: TEST_STUDY_ID\n"
            + "genetic_alteration_type: MUTATION_EXTENDED\n"
            + "datatype: MAF\n"
            + "stable_id: MAF_STABLE_ID\n"
            + "data_filename: data_mutation_extended_maf_maf_stable_id.txt\n", fileContents.get("meta_mutation_extended_maf_maf_stable_id.txt").toString());

        assertEquals("""
            Hugo_Symbol\tEntrez_Gene_Id\tCenter\tNCBI_Build\tChromosome\tStart_Position\tEnd_Position\tStrand\tVariant_Classification\tVariant_Type\tReference_Allele\tTumor_Seq_Allele1\tTumor_Seq_Allele2\tdbSNP_RS\tdbSNP_Val_Status\tTumor_Sample_Barcode\tMatched_Norm_Sample_Barcode\tMatch_Norm_Seq_Allele1\tMatch_Norm_Seq_Allele2\tTumor_Validation_Allele1\tTumor_Validation_Allele2\tMatch_Norm_Validation_Allele1\tMatch_Norm_Validation_Allele2\tVerification_Status\tValidation_Status\tMutation_Status\tSequencing_Phase\tSequence_Source\tValidation_Method\tScore\tBAM_File\tSequencer\tHGVSp_Short\tt_alt_count\tt_ref_count\tn_alt_count\tn_ref_count
            HUGO\t12345\tcenter1\thg38\tX\t1000000\t1000100\t+\tMissense_Mutation\tSNP\tT\tC\tA\tDBSNPRS123\tbyFrequency\tSAMPLE_1\tSAMPLE_2\tA\tT\tC\tG\tA\tT\tVerified\tSomatic\tSomatic\tPhase1\tExome\tSanger\t1.1\tbam_file\tIllumina hiseq 2000\tSHRT\t55\t33\t100\t99
            """, fileContents.get("data_mutation_extended_maf_maf_stable_id.txt").toString());
    }

    @Test
    public void testMafDataTypeExportNoRows() {
        var factory = new InMemoryFileWriterFactory();

        MafRecordService mafRecordService = new MafRecordService(null) {
            @Override
            public CloseableIterator<MafRecord> getMafRecords(String molecularProfileStableId) {
                return new SimpleCloseableIterator<>(emptyList());
            }
        };

        MafDataTypeExporter mafDataTypeExporter = new MafDataTypeExporter(geneticProfileService, mafRecordService);

        mafDataTypeExporter.exportData(factory, "TEST_STUDY_ID");

        var fileContents = factory.getFileContents();
        assertEquals(Set.of("meta_mutation_extended_maf_maf_stable_id.txt", "data_mutation_extended_maf_maf_stable_id.txt"), fileContents.keySet());

        assertEquals("""
            Hugo_Symbol\tEntrez_Gene_Id\tCenter\tNCBI_Build\tChromosome\tStart_Position\tEnd_Position\tStrand\tVariant_Classification\tVariant_Type\tReference_Allele\tTumor_Seq_Allele1\tTumor_Seq_Allele2\tdbSNP_RS\tdbSNP_Val_Status\tTumor_Sample_Barcode\tMatched_Norm_Sample_Barcode\tMatch_Norm_Seq_Allele1\tMatch_Norm_Seq_Allele2\tTumor_Validation_Allele1\tTumor_Validation_Allele2\tMatch_Norm_Validation_Allele1\tMatch_Norm_Validation_Allele2\tVerification_Status\tValidation_Status\tMutation_Status\tSequencing_Phase\tSequence_Source\tValidation_Method\tScore\tBAM_File\tSequencer\tHGVSp_Short\tt_alt_count\tt_ref_count\tn_alt_count\tn_ref_count
            """, fileContents.get("data_mutation_extended_maf_maf_stable_id.txt").toString());
    }
}
