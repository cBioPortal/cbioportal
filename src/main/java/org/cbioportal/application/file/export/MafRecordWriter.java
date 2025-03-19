package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.MafRecord;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.function.Function;

import static org.cbioportal.application.file.export.TSVUtil.composeRow;

/**
 * Writes MAF records to a writer
 */
public class MafRecordWriter {
    private final Writer writer;

    private static final LinkedHashMap<String, Function<MafRecord, String>> MAF_ROW = new LinkedHashMap<>();
    static {
        MAF_ROW.put("Hugo_Symbol", MafRecord::getHugoSymbol);
        MAF_ROW.put("Entrez_Gene_Id", MafRecord::getEntrezGeneId);
        MAF_ROW.put("Center", MafRecord::getCenter);
        MAF_ROW.put("NCBI_Build", MafRecord::getNcbiBuild);
        MAF_ROW.put("Chromosome", MafRecord::getChromosome);
        MAF_ROW.put("Start_Position", (mafRecord -> mafRecord.getStartPosition() == null ? null : mafRecord.getStartPosition().toString()));
        MAF_ROW.put("End_Position", mafRecord -> mafRecord.getEndPosition() == null ? null : mafRecord.getEndPosition().toString());
        MAF_ROW.put("Strand", MafRecord::getStrand);
        MAF_ROW.put("Variant_Classification", MafRecord::getVariantClassification);
        MAF_ROW.put("Variant_Type", MafRecord::getVariantType);
        MAF_ROW.put("Reference_Allele", MafRecord::getReferenceAllele);
        MAF_ROW.put("Tumor_Seq_Allele1", MafRecord::getTumorSeqAllele1);
        MAF_ROW.put("Tumor_Seq_Allele2", MafRecord::getTumorSeqAllele2);
        MAF_ROW.put("dbSNP_RS", MafRecord::getDbSnpRs);
        MAF_ROW.put("dbSNP_Val_Status", MafRecord::getDbSnpValStatus);
        MAF_ROW.put("Tumor_Sample_Barcode", MafRecord::getTumorSampleBarcode);
        MAF_ROW.put("Matched_Norm_Sample_Barcode", MafRecord::getMatchedNormSampleBarcode);
        MAF_ROW.put("Match_Norm_Seq_Allele1", MafRecord::getMatchNormSeqAllele1);
        MAF_ROW.put("Match_Norm_Seq_Allele2", MafRecord::getMatchNormSeqAllele2);
        MAF_ROW.put("Tumor_Validation_Allele1", MafRecord::getTumorValidationAllele1);
        MAF_ROW.put("Tumor_Validation_Allele2", MafRecord::getTumorValidationAllele2);
        MAF_ROW.put("Match_Norm_Validation_Allele1", MafRecord::getMatchNormValidationAllele1);
        MAF_ROW.put("Match_Norm_Validation_Allele2", MafRecord::getMatchNormValidationAllele2);
        MAF_ROW.put("Verification_Status", MafRecord::getVerificationStatus);
        MAF_ROW.put("Validation_Status", MafRecord::getValidationStatus);
        MAF_ROW.put("Mutation_Status", MafRecord::getMutationStatus);
        MAF_ROW.put("Sequencing_Phase", MafRecord::getSequencingPhase);
        MAF_ROW.put("Sequence_Source", MafRecord::getSequenceSource);
        MAF_ROW.put("Validation_Method", MafRecord::getValidationMethod);
        MAF_ROW.put("Score", MafRecord::getScore);
        MAF_ROW.put("BAM_File", MafRecord::getBamFile);
        MAF_ROW.put("Sequencer", MafRecord::getSequencer);
        MAF_ROW.put("HGVSp_Short", MafRecord::getHgvspShort);
        MAF_ROW.put("t_alt_count", (mafRecord -> mafRecord.gettAltCount() == null ? null : mafRecord.gettAltCount().toString()));
        MAF_ROW.put("t_ref_count", (mafRecord -> mafRecord.gettRefCount() == null ? null : mafRecord.gettRefCount().toString()));
        MAF_ROW.put("n_alt_count", (mafRecord -> mafRecord.getnAltCount() == null ? null : mafRecord.getnAltCount().toString()));
        MAF_ROW.put("n_ref_count", (mafRecord -> mafRecord.getnRefCount() == null ? null : mafRecord.getnRefCount().toString()));
    }

    public MafRecordWriter(Writer writer) {
        this.writer = writer;
    }

    public void write(Iterator<MafRecord> maf) {
        writeRow(MAF_ROW.sequencedKeySet());
        while (maf.hasNext()) {
            MafRecord mafRecord = maf.next();
            writeRow(MAF_ROW.sequencedValues().stream().map(factory -> factory.apply(mafRecord)).toList());
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
