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

    private static final LinkedHashMap<String, Factory<MafRecord, String>> MAF_ROW = new LinkedHashMap<>();
    static {
        MAF_ROW.put("Hugo_Symbol", MafRecord::hugoSymbol);
        MAF_ROW.put("Entrez_Gene_Id", MafRecord::entrezGeneId);
        MAF_ROW.put("Center", MafRecord::center);
        MAF_ROW.put("NCBI_Build", MafRecord::ncbiBuild);
        MAF_ROW.put("Chromosome", MafRecord::chromosome);
        MAF_ROW.put("Start_Position", (mafRecord -> mafRecord.startPosition() == null ? null : mafRecord.startPosition().toString()));
        MAF_ROW.put("End_Position", mafRecord -> mafRecord.endPosition() == null ? null : mafRecord.endPosition().toString());
        MAF_ROW.put("Strand", MafRecord::strand);
        MAF_ROW.put("Variant_Classification", MafRecord::variantClassification);
        MAF_ROW.put("Variant_Type", MafRecord::variantType);
        MAF_ROW.put("Reference_Allele", MafRecord::referenceAllele);
        MAF_ROW.put("Tumor_Seq_Allele1", MafRecord::tumorSeqAllele1);
        MAF_ROW.put("Tumor_Seq_Allele2", MafRecord::tumorSeqAllele2);
        MAF_ROW.put("dbSNP_RS", MafRecord::dbSnpRs);
        MAF_ROW.put("dbSNP_Val_Status", MafRecord::dbSnpValStatus);
        MAF_ROW.put("Tumor_Sample_Barcode", MafRecord::tumorSampleBarcode);
        MAF_ROW.put("Matched_Norm_Sample_Barcode", MafRecord::matchedNormSampleBarcode);
        MAF_ROW.put("Match_Norm_Seq_Allele1", MafRecord::matchNormSeqAllele1);
        MAF_ROW.put("Match_Norm_Seq_Allele2", MafRecord::matchNormSeqAllele2);
        MAF_ROW.put("Tumor_Validation_Allele1", MafRecord::tumorValidationAllele1);
        MAF_ROW.put("Tumor_Validation_Allele2", MafRecord::tumorValidationAllele2);
        MAF_ROW.put("Match_Norm_Validation_Allele1", MafRecord::matchNormValidationAllele1);
        MAF_ROW.put("Match_Norm_Validation_Allele2", MafRecord::matchNormValidationAllele2);
        MAF_ROW.put("Verification_Status", MafRecord::verificationStatus);
        MAF_ROW.put("Validation_Status", MafRecord::validationStatus);
        MAF_ROW.put("Mutation_Status", MafRecord::mutationStatus);
        MAF_ROW.put("Sequencing_Phase", MafRecord::sequencingPhase);
        MAF_ROW.put("Sequence_Source", MafRecord::sequenceSource);
        MAF_ROW.put("Validation_Method", MafRecord::validationMethod);
        MAF_ROW.put("Score", MafRecord::score);
        MAF_ROW.put("BAM_File", MafRecord::bamFile);
        MAF_ROW.put("Sequencer", MafRecord::sequencer);
        MAF_ROW.put("HGVSc_Short", MafRecord::hgvspShort);
        MAF_ROW.put("t_alt_count", (mafRecord -> mafRecord.tAltCount() == null ? null : mafRecord.tAltCount().toString()));
        MAF_ROW.put("t_ref_count", (mafRecord -> mafRecord.tRefCount() == null ? null : mafRecord.tRefCount().toString()));
        MAF_ROW.put("n_alt_count", (mafRecord -> mafRecord.nAltCount() == null ? null : mafRecord.nAltCount().toString()));
        MAF_ROW.put("n_ref_count", (mafRecord -> mafRecord.nRefCount() == null ? null : mafRecord.nRefCount().toString()));
    }

    public MafRecordWriter(Writer writer) {
        this.writer = writer;
    }

    public void write(Iterator<MafRecord> maf) {
        writeRow(MAF_ROW.sequencedKeySet());
        while (maf.hasNext()) {
            MafRecord mafRecord = maf.next();
            writeRow(MAF_ROW.sequencedValues().stream().map(factory -> factory.create(mafRecord)).toList());
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
