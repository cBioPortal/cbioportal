package org.cbioportal.file.export;

import org.cbioportal.file.model.MafRecord;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static org.cbioportal.file.export.TSVUtil.composeRow;

/**
 * Writes MAF records to a writer
 */
public class MafRecordWriter {
    private final Writer writer;

    public MafRecordWriter(Writer writer) {
        this.writer = writer;
    }

    public void write(Iterator<MafRecord> maf) {
        int line = 0;
        while (maf.hasNext()) {
            MafRecord mafRecord = maf.next();
            LinkedHashMap<String, String> mafRow = new LinkedHashMap<>();
            mafRow.put("Hugo_Symbol", mafRecord.hugoSymbol());
            mafRow.put("Entrez_Gene_Id", mafRecord.entrezGeneId());
            mafRow.put("Center", mafRecord.center());
            mafRow.put("NCBI_Build", mafRecord.ncbiBuild());
            mafRow.put("Chromosome", mafRecord.chromosome());
            mafRow.put("Start_Position", mafRecord.startPosition().toString());
            mafRow.put("End_Position", mafRecord.endPosition().toString());
            mafRow.put("Strand", mafRecord.strand());
            mafRow.put("Variant_Classification", mafRecord.variantClassification());
            mafRow.put("Variant_Type", mafRecord.variantType()); 
            mafRow.put("Reference_Allele", mafRecord.referenceAllele());
            mafRow.put("Tumor_Seq_Allele1", mafRecord.tumorSeqAllele1());
            mafRow.put("Tumor_Seq_Allele2", mafRecord.tumorSeqAllele2());
            mafRow.put("dbSNP_RS", mafRecord.dbSnpRs());
            mafRow.put("dbSNP_Val_Status", mafRecord.dbSnpValStatus());
            mafRow.put("Tumor_Sample_Barcode", mafRecord.tumorSampleBarcode());
            mafRow.put("Matched_Norm_Sample_Barcode", mafRecord.matchedNormSampleBarcode());
            mafRow.put("Match_Norm_Seq_Allele1", mafRecord.matchNormSeqAllele1());
            mafRow.put("Match_Norm_Seq_Allele2", mafRecord.matchNormSeqAllele2());
            mafRow.put("Tumor_Validation_Allele1", mafRecord.tumorValidationAllele1());
            mafRow.put("Tumor_Validation_Allele2", mafRecord.tumorValidationAllele2());
            mafRow.put("Match_Norm_Validation_Allele1", mafRecord.matchNormValidationAllele1());
            mafRow.put("Match_Norm_Validation_Allele2", mafRecord.matchNormValidationAllele2());
            mafRow.put("Verification_Status", mafRecord.verificationStatus());
            mafRow.put("Validation_Status", mafRecord.validationStatus());
            mafRow.put("Mutation_Status", mafRecord.mutationStatus());
            mafRow.put("Sequencing_Phase", mafRecord.sequencingPhase());
            mafRow.put("Sequence_Source", mafRecord.sequenceSource());
            mafRow.put("Validation_Method", mafRecord.validationMethod());
            mafRow.put("Score", mafRecord.score());
            mafRow.put("BAM_File", mafRecord.bamFile());
            mafRow.put("Sequencer", mafRecord.sequencer());
            mafRow.put("HGVSc_Short", mafRecord.hgvspShort());
            mafRow.put("t_alt_count", mafRecord.tAltCount().toString());
            mafRow.put("t_ref_count", mafRecord.tRefCount().toString());
            mafRow.put("n_alt_count", mafRecord.nAltCount().toString());
            mafRow.put("n_ref_count", mafRecord.nRefCount().toString());
            if (line == 0) {
                writeRow(mafRow.sequencedKeySet());
            }
            writeRow(mafRow.sequencedValues());
            line++;
        }
    }

    private void writeRow(Iterable<String> row) {
        try {
            writer.write(composeRow(row));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
