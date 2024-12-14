package org.cbioportal.file.export;

import com.clickhouse.client.internal.apache.hc.core5.function.Factory;
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
        LinkedHashMap<String, Factory<MafRecord, String>> mafRow = new LinkedHashMap<>();
        mafRow.put("Hugo_Symbol", MafRecord::hugoSymbol);
        mafRow.put("Entrez_Gene_Id", MafRecord::entrezGeneId);
        mafRow.put("Center", MafRecord::center);
        mafRow.put("NCBI_Build", MafRecord::ncbiBuild);
        mafRow.put("Chromosome", MafRecord::chromosome);
        mafRow.put("Start_Position", (mafRecord -> mafRecord.startPosition() == null ? null : mafRecord.startPosition().toString()));
        mafRow.put("End_Position", mafRecord -> mafRecord.endPosition() == null ? null : mafRecord.endPosition().toString());
        mafRow.put("Strand", MafRecord::strand);
        mafRow.put("Variant_Classification", MafRecord::variantClassification);
        mafRow.put("Variant_Type", MafRecord::variantType);
        mafRow.put("Reference_Allele", MafRecord::referenceAllele);
        mafRow.put("Tumor_Seq_Allele1", MafRecord::tumorSeqAllele1);
        mafRow.put("Tumor_Seq_Allele2", MafRecord::tumorSeqAllele2);
        mafRow.put("dbSNP_RS", MafRecord::dbSnpRs);
        mafRow.put("dbSNP_Val_Status", MafRecord::dbSnpValStatus);
        mafRow.put("Tumor_Sample_Barcode", MafRecord::tumorSampleBarcode);
        mafRow.put("Matched_Norm_Sample_Barcode", MafRecord::matchedNormSampleBarcode);
        mafRow.put("Match_Norm_Seq_Allele1", MafRecord::matchNormSeqAllele1);
        mafRow.put("Match_Norm_Seq_Allele2", MafRecord::matchNormSeqAllele2);
        mafRow.put("Tumor_Validation_Allele1", MafRecord::tumorValidationAllele1);
        mafRow.put("Tumor_Validation_Allele2", MafRecord::tumorValidationAllele2);
        mafRow.put("Match_Norm_Validation_Allele1", MafRecord::matchNormValidationAllele1);
        mafRow.put("Match_Norm_Validation_Allele2", MafRecord::matchNormValidationAllele2);
        mafRow.put("Verification_Status", MafRecord::verificationStatus);
        mafRow.put("Validation_Status", MafRecord::validationStatus);
        mafRow.put("Mutation_Status", MafRecord::mutationStatus);
        mafRow.put("Sequencing_Phase", MafRecord::sequencingPhase);
        mafRow.put("Sequence_Source", MafRecord::sequenceSource);
        mafRow.put("Validation_Method", MafRecord::validationMethod);
        mafRow.put("Score", MafRecord::score);
        mafRow.put("BAM_File", MafRecord::bamFile);
        mafRow.put("Sequencer", MafRecord::sequencer);
        mafRow.put("HGVSc_Short", MafRecord::hgvspShort);
        mafRow.put("t_alt_count", (mafRecord -> mafRecord.tAltCount() == null ? null : mafRecord.tAltCount().toString()));
        mafRow.put("t_ref_count", (mafRecord -> mafRecord.tRefCount() == null ? null : mafRecord.tRefCount().toString()));
        mafRow.put("n_alt_count", (mafRecord -> mafRecord.nAltCount() == null ? null : mafRecord.nAltCount().toString()));
        mafRow.put("n_ref_count", (mafRecord -> mafRecord.nRefCount() == null ? null : mafRecord.nRefCount().toString()));
        writeRow(mafRow.sequencedKeySet());
        while (maf.hasNext()) {
            MafRecord mafRecord = maf.next();
            writeRow(mafRow.sequencedValues().stream().map(factory -> factory.create(mafRecord)).toList());
        }
    }

    private void writeRow(Iterable<String> row) {
        try {
            writer.write(composeRow(row));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
