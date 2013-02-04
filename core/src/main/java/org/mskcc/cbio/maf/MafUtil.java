/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.maf;

import java.util.HashMap;

/**
 * Utility Class for Parsing MAF Files.
 *
 * This utility class handles variable columns and column orderings within MAF Files.
 */
public class MafUtil
{
	// standard header column names
	public static final String HUGO_SYMBOL = "Hugo_Symbol";
	public static final String ENTREZ_GENE_ID = "Entrez_Gene_Id";
	public static final String CENTER = "Center";
	public static final String NCBI_BUILD = "NCBI_Build";
	public static final String CHROMOSOME = "Chromosome";
	public static final String START_POSITION = "Start_Position";
	public static final String END_POSITION = "End_Position";
	public static final String STRAND = "Strand";
	public static final String VARIANT_CLASSIFICATION = "Variant_Classification";
	public static final String VARIANT_TYPE = "Variant_Type";
	public static final String REFERENCE_ALLELE = "Reference_Allele";
	public static final String TUMOR_SEQ_ALLELE_1 = "Tumor_Seq_Allele1";
	public static final String TUMOR_SEQ_ALLELE_2 = "Tumor_Seq_Allele2";
	public static final String DBSNP_RS = "dbSNP_RS";
	public static final String DBSNP_VAL_STATUS = "dbSNP_Val_Status";
	public static final String TUMOR_SAMPLE_BARCODE = "Tumor_Sample_Barcode";
	public static final String MATCHED_NORM_SAMPLE_BARCODE = "Matched_Norm_Sample_Barcode";
	public static final String MATCH_NORM_SEQ_ALLELE1 = "Match_Norm_Seq_Allele1";
	public static final String MATCH_NORM_SEQ_ALLELE2 = "Match_Norm_Seq_Allele2";
	public static final String TUMOR_VALIDATION_ALLELE1 = "Tumor_Validation_Allele1";
	public static final String TUMOR_VALIDATION_ALLELE2 = "Tumor_Validation_Allele2";
	public static final String MATCH_NORM_VALIDATION_ALLELE1 = "Match_Norm_Validation_Allele1";
	public static final String MATCH_NORM_VALIDATION_ALLELE2 = "Match_Norm_Validation_Allele2";
	public static final String VERIFICATION_STATUS = "Verification_Status";
	public static final String VALIDATION_STATUS = "Validation_Status";
	public static final String MUTATION_STATUS = "Mutation_Status";
	public static final String SEQUENCING_PHASE = "Sequencing_Phase";
	public static final String SEQUENCE_SOURCE = "Sequence_Source";
	public static final String VALIDATION_METHOD = "Validation_Method";
	public static final String SCORE = "Score";
	public static final String BAM_FILE = "BAM_File";
	public static final String SEQUENCER = "Sequencer";

	// oncotator column names
	public static final String ONCOTATOR_COSMIC_OVERLAPPING = "ONCOTATOR_COSMIC_OVERLAPPING";
	public static final String ONCOTATOR_DBSNP_RS = "ONCOTATOR_DBSNP_RS";
	public static final String ONCOTATOR_DBSNP_VAL_STATUS = "ONCOTATOR_DBSNP_VAL_STATUS";

	public static final String ONCOTATOR_PROTEIN_CHANGE = "ONCOTATOR_PROTEIN_CHANGE";
	public static final String ONCOTATOR_VARIANT_CLASSIFICATION = "ONCOTATOR_VARIANT_CLASSIFICATION";
	public static final String ONCOTATOR_GENE_SYMBOL = "ONCOTATOR_GENE_SYMBOL";
	public static final String ONCOTATOR_REFSEQ_MRNA_ID = "ONCOTATOR_REFSEQ_MRNA_ID";
	public static final String ONCOTATOR_REFSEQ_PROT_ID = "ONCOTATOR_REFSEQ_PROT_ID";
	public static final String ONCOTATOR_UNIPROT_ENTRY_NAME = "ONCOTATOR_UNIPROT_ENTRY_NAME";
	public static final String ONCOTATOR_UNIPROT_ACCESSION = "ONCOTATOR_UNIPROT_ACCESSION";
	public static final String ONCOTATOR_CODON_CHANGE = "ONCOTATOR_CODON_CHANGE";
	public static final String ONCOTATOR_TRANSCRIPT_CHANGE = "ONCOTATOR_TRANSCRIPT_CHANGE";
	public static final String ONCOTATOR_EXON_AFFECTED = "ONCOTATOR_EXON_AFFECTED";

	public static final String ONCOTATOR_PROTEIN_CHANGE_BE = "ONCOTATOR_PROTEIN_CHANGE_BEST_EFFECT";
	public static final String ONCOTATOR_VARIANT_CLASSIFICATION_BE = "ONCOTATOR_VARIANT_CLASSIFICATION_BEST_EFFECT";
	public static final String ONCOTATOR_GENE_SYMBOL_BE = "ONCOTATOR_GENE_SYMBOL_BEST_EFFECT";
	public static final String ONCOTATOR_REFSEQ_MRNA_ID_BE = "ONCOTATOR_REFSEQ_MRNA_ID_BEST_EFFECT";
	public static final String ONCOTATOR_REFSEQ_PROT_ID_BE = "ONCOTATOR_REFSEQ_PROT_ID_BEST_EFFECT";
	public static final String ONCOTATOR_UNIPROT_ENTRY_NAME_BE = "ONCOTATOR_UNIPROT_ENTRY_NAME_BEST_EFFECT";
	public static final String ONCOTATOR_UNIPROT_ACCESSION_BE = "ONCOTATOR_UNIPROT_ACCESSION_BEST_EFFECT";
	public static final String ONCOTATOR_CODON_CHANGE_BE = "ONCOTATOR_CODON_CHANGE_BEST_EFFECT";
	public static final String ONCOTATOR_TRANSCRIPT_CHANGE_BE = "ONCOTATOR_TRANSCRIPT_CHANGE_BEST_EFFECT";
	public static final String ONCOTATOR_EXON_AFFECTED_BE = "ONCOTATOR_EXON_AFFECTED_BEST_EFFECT";

	// mutation assessor column names
	public static final String MA_FIMPACT = "MA:FImpact";
	public static final String MA_LINK_VAR = "MA:link.var";
	public static final String MA_LINK_MSA = "MA:link.MSA";
	public static final String MA_LINK_PDB = "MA:link.PDB";
	public static final String MA_PROTEIN_CHANGE = "MA:protein.change";


    // standard MAF column indices
	private int chrIndex = -1; // CHR
    private int ncbiIndex = -1; // NCBI_BUILD
    private int startPositionIndex = -1; // START_POSITION
    private int endPositionIndex = -1; // END_POSITION
    private int hugoGeneSymbolIndex = -1; 
    private int entrezGeneIdIndex = -1; // ENTREZ_GENE_ID
    private int referenceAlleleIndex = -1; // REFERENCE_ALLELE
    private int variantClassificationIndex = -1; // MUTATION_TYPE 
    private int variantTypeIndex = -1; // VARIANT_TYPE
    private int centerIndex = -1; // CENTER
    private int strandIndex = -1; // STRAND
    private int tumorSeqAllele1Index = -1; // TUMOR_SEQ_ALLELE1
    private int tumorSeqAllele2Index = -1; // TUMOR_SEQ_ALLELE1
    private int dbSNPIndex = -1; // DB_SNP_RS
    private int tumorSampleIndex = -1;
    private int mutationStatusIndex = -1; // MUTATION_STATUS
    private int validationStatusIndex = -1; // VALIDATION_STATUS
    private int sequencerIndex = -1; // SEQUENCER
    private int dbSnpValStatusIndex = -1; // DB_SNP_VAL_STATUS
    private int matchedNormSampleBarcodeIndex = -1; // MATCHED_NORM_SAMPLE_BARCODE
    private int matchNormSeqAllele1Index = -1; // MATCH_NORM_SEQ_ALLELE1
    private int matchNormSeqAllele2Index = -1; // MATCH_NORM_SEQ_ALLELE2
    private int tumorValidationAllele1Index = -1; // TUMOR_VALIDATION_ALLELE1
    private int tumorValidationAllele2Index = -1; // TUMOR_VALIDATION_ALLELE2
    private int matchNormValidationAllele1Index = -1; // MATCH_NORM_VALIDATION_ALLELE1
    private int matchNormValidationAllele2Index = -1; // MATCH_NORM_VALIDATION_ALLELE2
    private int verificationStatusIndex = -1; // VERIFICATION_STATUS
    private int sequencingPhaseIndex = -1; // SEQUENCING_PHASE
    private int sequenceSourceIndex = -1; // SEQUENCE_SOURCE
    private int validationMethodIndex = -1; // VALIDATION_METHOD
    private int scoreIndex = -1; // SCORE
    private int bamFileIndex = -1; // BAM_FILE

	// Allele Frequency Columns
	private int tumorAltCountIndex = -1; // TUMOR_ALT_COUNT
    private int tumorRefCountIndex = -1; // TUMOR_REF_COUNT
    private int normalAltCountIndex = -1; // NORMAL_ALT_COUNT
    private int normalRefCountIndex = -1; // NORMAL_REF_COUNT
    private int tTotCovIndex  = -1;
    private int tVarCovIndex  = -1;
    private int nTotCovIndex  = -1;
    private int nVarCovIndex  = -1;
    private int tumorDepthIndex = -1;
    private int tumorVafIndex = -1;
    private int normalDepthIndex = -1;
    private int normalVafIndex = -1;

    // default Oncotator column indices
	private int oncoCosmicOverlappingIndex = -1;
	private int oncoDbSnpRsIndex = -1;
	private int oncoDbSnpValStatusIndex = -1;
	private int oncoProteinChangeIndex = -1;
    private int oncoVariantClassificationIndex = -1;
	private int oncoGeneSymbolIndex = -1;
	private int oncoRefseqMrnaIdIndex = -1;
	private int oncoRefseqProtIdIndex = -1;
	private int oncoExonAffectedIndex = -1;
	private int oncoTranscriptChangeIndex = -1;
	private int oncoUniprotNameIndex = -1;
	private int oncoUniprotAccessionIndex = -1;
	private int oncoCodonChangeIndex = -1;
	private int oncoProteinChangeBeIndex = -1;
	private int oncoGeneSymbolBeIndex = -1;
	private int oncoRefseqMrnaIdBeIndex = -1;
	private int oncoRefseqProtIdBeIndex = -1;
	private int oncoVariantClassificationBeIndex = -1;
	private int oncoUniprotNameBeIndex = -1;
	private int oncoUniprotAccessionBeIndex = -1;
	private int oncoCodonChangeBeIndex = -1;
	private int oncoTranscriptChangeBeIndex = -1;
	private int oncoExonAffectedBeIndex = -1;

	// Mutation Assessor column indices
	private int maFImpactIndex = -1; // MA:FImpact
	private int maLinkVarIndex = -1; // MA:link.var
	private int maLinkMsaIndex = -1; // MA:link.MSA
	private int maLinkPdbIndex = -1; // MA:link.PDB
	private int maProteinChangeIndex = -1; // MA:protein.change

	// number of headers in the header line
    private int headerCount;

	// mapping for all column names (both standard and custom columns)
	private HashMap<String, Integer> columnIndexMap;



	/**
     * Constructor.
     *
     * @param headerLine    Header Line.
     */
    public MafUtil(String headerLine)
    {
        // init column index map
	    this.columnIndexMap = new HashMap<String, Integer>();

        // split header names
    	String parts[] = headerLine.split("\t");

    	// update header count
        this.headerCount = parts.length;

        // find required header indices
        for (int i=0; i<parts.length; i++)
        {
            String header = parts[i];

	        // put the index to the map
	        this.columnIndexMap.put(header.toLowerCase(), i);

	        // determine standard & default column indices
            if (header.equalsIgnoreCase(CHROMOSOME)) {
                chrIndex = i;
            } else if(header.equalsIgnoreCase(NCBI_BUILD)) {
                ncbiIndex = i;
            } else if(header.equalsIgnoreCase(START_POSITION)) {
                startPositionIndex = i;
            } else if(header.equalsIgnoreCase(END_POSITION)) {
                endPositionIndex = i;
            } else if(header.equalsIgnoreCase(HUGO_SYMBOL)) {
                hugoGeneSymbolIndex = i;
            } else if(header.equalsIgnoreCase(ENTREZ_GENE_ID)) {
                entrezGeneIdIndex = i;
            } else if(header.equalsIgnoreCase(REFERENCE_ALLELE)) {
                referenceAlleleIndex = i;
            } else if(header.equalsIgnoreCase(VARIANT_CLASSIFICATION)) {
                variantClassificationIndex = i;
            } else if(header.equalsIgnoreCase(VARIANT_TYPE)) {
                variantTypeIndex = i;
            } else if(header.equalsIgnoreCase(CENTER)) {
                centerIndex = i;
            } else if(header.equalsIgnoreCase(STRAND)) {
                strandIndex = i;
            } else if(header.equalsIgnoreCase(TUMOR_SEQ_ALLELE_1)) {
                tumorSeqAllele1Index = i;
            } else if(header.equalsIgnoreCase(TUMOR_SEQ_ALLELE_2)) {
                tumorSeqAllele2Index = i;
            } else if(header.equalsIgnoreCase(DBSNP_RS)) {
                dbSNPIndex = i;
            } else if(header.equalsIgnoreCase(TUMOR_SAMPLE_BARCODE)) {
                tumorSampleIndex = i;
            } else if(header.equalsIgnoreCase(MUTATION_STATUS)) {
                mutationStatusIndex = i;
            } else if(header.equalsIgnoreCase(VALIDATION_STATUS)) {
                validationStatusIndex = i;
            } else if(header.equalsIgnoreCase(SEQUENCER)) {
	            sequencerIndex = i;
	        } else if(header.equalsIgnoreCase(DBSNP_VAL_STATUS)) {
	        	dbSnpValStatusIndex = i;
	        } else if(header.equalsIgnoreCase(MATCHED_NORM_SAMPLE_BARCODE)) {
	        	matchedNormSampleBarcodeIndex = i;
	        } else if(header.equalsIgnoreCase(MATCH_NORM_SEQ_ALLELE1)) {
	        	matchNormSeqAllele1Index = i;
	        } else if(header.equalsIgnoreCase(MATCH_NORM_SEQ_ALLELE2)) {
	        	matchNormSeqAllele2Index = i;
	        } else if(header.equalsIgnoreCase(TUMOR_VALIDATION_ALLELE1)) {
	        	tumorValidationAllele1Index = i;
	        } else if(header.equalsIgnoreCase(TUMOR_VALIDATION_ALLELE2)) {
	        	tumorValidationAllele2Index = i;
	        } else if(header.equalsIgnoreCase(MATCH_NORM_VALIDATION_ALLELE1)) {
	        	matchNormValidationAllele1Index = i;
	        } else if(header.equalsIgnoreCase(MATCH_NORM_VALIDATION_ALLELE2)) {
	        	matchNormValidationAllele2Index = i;
	        } else if(header.equalsIgnoreCase(VERIFICATION_STATUS)) {
	        	verificationStatusIndex = i;
	        } else if(header.equalsIgnoreCase(SEQUENCING_PHASE)) {
	        	sequencingPhaseIndex = i;
	        } else if(header.equalsIgnoreCase(SEQUENCE_SOURCE)) {
	        	sequenceSourceIndex = i;
	        } else if(header.equalsIgnoreCase(VALIDATION_METHOD)) {
	        	validationMethodIndex = i;
	        } else if(header.equalsIgnoreCase(SCORE)) {
	        	scoreIndex = i;
	        } else if(header.equalsIgnoreCase(BAM_FILE)) {
	        	bamFileIndex = i;
	        } else if(header.equalsIgnoreCase(ONCOTATOR_COSMIC_OVERLAPPING)) {
	        	oncoCosmicOverlappingIndex = i;
	        } else if(header.equalsIgnoreCase(ONCOTATOR_DBSNP_RS)) {
	        	oncoDbSnpRsIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_DBSNP_VAL_STATUS)) {
	            oncoDbSnpValStatusIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_PROTEIN_CHANGE)) {
	            oncoProteinChangeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_VARIANT_CLASSIFICATION)) {
	            oncoVariantClassificationIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_GENE_SYMBOL)) {
	            oncoGeneSymbolIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_REFSEQ_MRNA_ID)) {
	            oncoRefseqMrnaIdIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_REFSEQ_PROT_ID)) {
	            oncoRefseqProtIdIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_UNIPROT_ENTRY_NAME)) {
	            oncoUniprotNameIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_UNIPROT_ACCESSION)) {
	            oncoUniprotAccessionIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_CODON_CHANGE)) {
	            oncoCodonChangeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_TRANSCRIPT_CHANGE)) {
	            oncoTranscriptChangeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_EXON_AFFECTED)) {
	            oncoExonAffectedIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_PROTEIN_CHANGE_BE)) {
	            oncoProteinChangeBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_VARIANT_CLASSIFICATION_BE)) {
	            oncoVariantClassificationBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_GENE_SYMBOL_BE)) {
	            oncoGeneSymbolBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_REFSEQ_MRNA_ID_BE)) {
	            oncoRefseqMrnaIdBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_REFSEQ_PROT_ID_BE)) {
	            oncoRefseqProtIdBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_UNIPROT_ENTRY_NAME_BE)) {
	            oncoUniprotNameBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_UNIPROT_ACCESSION_BE)) {
	            oncoUniprotAccessionBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_CODON_CHANGE_BE)) {
	            oncoCodonChangeBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_TRANSCRIPT_CHANGE_BE)) {
	            oncoTranscriptChangeBeIndex = i;
            } else if(header.equalsIgnoreCase(ONCOTATOR_EXON_AFFECTED_BE)) {
	            oncoExonAffectedBeIndex = i;
            } else if(header.equalsIgnoreCase(MA_FIMPACT)) {
				maFImpactIndex = i;
            } else if(header.equalsIgnoreCase(MA_LINK_VAR)) {
	            maLinkVarIndex = i;
            } else if(header.equalsIgnoreCase(MA_LINK_MSA)) {
	            maLinkMsaIndex = i;
            } else if(header.equalsIgnoreCase(MA_LINK_PDB)) {
	            maLinkPdbIndex = i;
            } else if(header.equalsIgnoreCase(MA_PROTEIN_CHANGE)) {
	            maProteinChangeIndex = i;
            }
            // TODO allele freq columns may have different headers
	        else if( header.equalsIgnoreCase("t_ref_count")||header.equalsIgnoreCase("i_t_ref_count")||header.equalsIgnoreCase("AD_Ref")) {
	        	tumorRefCountIndex = i;
            } else if(header.equalsIgnoreCase("t_alt_count")||header.equalsIgnoreCase("i_t_alt_count")||header.equalsIgnoreCase("AD_Alt")) {
                tumorAltCountIndex = i;
            } else if(header.equalsIgnoreCase("n_ref_count")||header.equalsIgnoreCase("Norm_AD_Ref")) {
                normalRefCountIndex= i;
            } else if(header.equalsIgnoreCase("n_alt_count")||header.equalsIgnoreCase("Norm_AD_Alt")) {
                normalAltCountIndex = i;
            } else if(header.equalsIgnoreCase("TTotCov")) {
                tTotCovIndex = i;
            } else if(header.equalsIgnoreCase("TVarCov")) {
                tVarCovIndex = i;
            } else if(header.equalsIgnoreCase("NTotCov")) {
                nTotCovIndex = i;
            } else if(header.equalsIgnoreCase("NVarCov")) {
                nVarCovIndex = i;
            } else if(header.equalsIgnoreCase("tumor_depth")) {
                tumorDepthIndex = i;
            } else if(header.equalsIgnoreCase("tumor_vaf")) {
                tumorVafIndex = i;
            } else if(header.equalsIgnoreCase("normal_depth")) {
                normalDepthIndex = i;
            } else if(header.equalsIgnoreCase("normal_vaf")) {
                normalVafIndex = i;
            }
        }
    }

    public MafRecord parseRecord(String line)
    {
        String parts[] = line.split("\t", -1);

        MafRecord record = new MafRecord();

	    // standard MAF cols
        record.setCenter(getPartString(centerIndex, parts));
        record.setChr(getPartString(chrIndex, parts));
        record.setStartPosition(getPartLong(startPositionIndex, parts));
        record.setEndPosition(getPartLong(endPositionIndex, parts));
        record.setEntrezGeneId(getPartLong(entrezGeneIdIndex, parts));
        record.setHugoGeneSymbol(getPartString(hugoGeneSymbolIndex, parts));
        record.setNcbiBuild(getPartString(ncbiIndex, parts));
        record.setReferenceAllele(getPartString(referenceAlleleIndex, parts));
        record.setStrand(getPartString(strandIndex, parts));
        record.setDbSNP_RS(getPartString(dbSNPIndex, parts));
        record.setTumorSampleID(getPartString(tumorSampleIndex, parts));
        record.setTumorSeqAllele1(getPartString(tumorSeqAllele1Index, parts));
        record.setTumorSeqAllele2(getPartString(tumorSeqAllele2Index, parts));
        record.setVariantClassification(getPartString(variantClassificationIndex, parts));
        record.setVariantType(getPartString(variantTypeIndex, parts));
        record.setMutationStatus(getPartString(mutationStatusIndex, parts));
        record.setValidationStatus(getPartString(validationStatusIndex, parts));
        record.setSequencer(getPartString(sequencerIndex, parts));
        record.setDbSnpValStatus(getPartString(dbSnpValStatusIndex, parts));
        record.setMatchedNormSampleBarcode(getPartString(matchedNormSampleBarcodeIndex, parts));
        record.setMatchNormSeqAllele1(getPartString(matchNormSeqAllele1Index, parts));
        record.setMatchNormSeqAllele2(getPartString(matchNormSeqAllele2Index, parts));
        record.setTumorValidationAllele1(getPartString(tumorValidationAllele1Index, parts));
        record.setTumorValidationAllele2(getPartString(tumorValidationAllele2Index, parts));
        record.setMatchNormValidationAllele1(getPartString(matchNormValidationAllele1Index, parts));
        record.setMatchNormValidationAllele2(getPartString(matchNormValidationAllele2Index, parts));
        record.setVerificationStatus(getPartString(verificationStatusIndex, parts));
        record.setSequencingPhase(getPartString(sequencingPhaseIndex, parts));
        record.setSequenceSource(getPartString(sequenceSourceIndex, parts));
        record.setValidationMethod(getPartString(validationMethodIndex, parts));
        record.setScore(getPartString(scoreIndex, parts));
        record.setBamFile(getPartString(bamFileIndex, parts));

	    // allele frequency (count) columns
	    record.setTumorAltCount(getPartInt(tumorAltCountIndex, parts));
        record.setTumorRefCount(getPartInt(tumorRefCountIndex, parts));
        record.setNormalAltCount(getPartInt(normalAltCountIndex, parts));
        record.setNormalRefCount(getPartInt(normalRefCountIndex, parts));
        record.setTTotCov(getPartInt(tTotCovIndex, parts));
        record.setTVarCov(getPartInt(tVarCovIndex, parts));
        record.setNTotCov(getPartInt(nTotCovIndex, parts));
        record.setNVarCov(getPartInt(nVarCovIndex, parts));
        record.setTumorDepth(getPartInt(tumorDepthIndex, parts));
        record.setTumorVaf(getPartPercentage(tumorVafIndex, parts));
        record.setNormalDepth(getPartInt(normalDepthIndex, parts));
        record.setNormalVaf(getPartPercentage(normalVafIndex, parts));

        // Mutation Assessor columns
	    record.setMaFuncImpact(getPartString(maFImpactIndex, parts));
	    record.setMaLinkVar(getPartString(maLinkVarIndex, parts));
	    record.setMaLinkMsa(getPartString(maLinkMsaIndex, parts));
	    record.setMaLinkPdb(getPartString(maLinkPdbIndex, parts));
	    record.setMaProteinChange(getPartString(maProteinChangeIndex, parts));

	    // Oncotator columns
	    record.setOncotatorCosmicOverlapping(getPartString(oncoCosmicOverlappingIndex, parts));
	    record.setOncotatorDbSnpRs(getPartString(oncoDbSnpRsIndex, parts));
	    record.setOncotatorDbSnpValStatus(getPartString(oncoDbSnpValStatusIndex, parts));

	    record.setOncotatorProteinChange(getPartString(oncoProteinChangeIndex, parts));
        record.setOncotatorVariantClassification(getPartString(oncoVariantClassificationIndex, parts));
	    record.setOncotatorGeneSymbol(getPartString(oncoGeneSymbolIndex, parts));
	    record.setOncotatorRefseqMrnaId(getPartString(oncoRefseqMrnaIdIndex, parts));
	    record.setOncotatorRefseqProtId(getPartString(oncoRefseqProtIdIndex, parts));
	    record.setOncotatorUniprotName(getPartString(oncoUniprotNameIndex, parts));
	    record.setOncotatorUniprotAccession(getPartString(oncoUniprotAccessionIndex, parts));
	    record.setOncotatorCodonChange(getPartString(oncoCodonChangeIndex, parts));
	    record.setOncotatorTranscriptChange(getPartString(oncoTranscriptChangeIndex, parts));
	    record.setOncotatorExonAffected(getPartInt(oncoExonAffectedIndex, parts));

	    record.setOncotatorProteinChangeBestEffect(getPartString(oncoProteinChangeBeIndex, parts));
	    record.setOncotatorVariantClassificationBestEffect(getPartString(oncoVariantClassificationBeIndex, parts));
	    record.setOncotatorGeneSymbolBestEffect(getPartString(oncoGeneSymbolBeIndex, parts));
	    record.setOncotatorRefseqMrnaIdBestEffect(getPartString(oncoRefseqMrnaIdBeIndex, parts));
	    record.setOncotatorRefseqProtIdBestEffect(getPartString(oncoRefseqProtIdBeIndex, parts));
	    record.setOncotatorUniprotNameBestEffect(getPartString(oncoUniprotNameBeIndex, parts));
	    record.setOncotatorUniprotAccessionBestEffect(getPartString(oncoUniprotAccessionBeIndex, parts));
	    record.setOncotatorCodonChangeBestEffect(getPartString(oncoCodonChangeBeIndex, parts));
	    record.setOncotatorTranscriptChangeBestEffect(getPartString(oncoTranscriptChangeBeIndex, parts));
	    record.setOncotatorExonAffectedBestEffect(getPartInt(oncoExonAffectedBeIndex, parts));

        return record;
    }

    private String getPartString(int index, String[] parts)
    {
        try
        {
	        if (parts[index].length() == 0)
	        {
		        return MafRecord.NA_STRING;
	        }
	        else
	        {
		        return parts[index];
	        }
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return MafRecord.NA_STRING;
        }
    }

    private Long getPartLong(int index, String[] parts) {
        try {
            String part = parts[index];
            return Long.parseLong(part);
        } catch (ArrayIndexOutOfBoundsException e) {
            return MafRecord.NA_LONG;
        } catch (NumberFormatException e) {
            return MafRecord.NA_LONG;
        }
    }

    private Integer getPartInt(int index, String[] parts)
    {
        try {
            String part = parts[index];
            return (int)(Float.parseFloat(part));
        } catch (ArrayIndexOutOfBoundsException e) {
            return MafRecord.NA_INT;
        } catch (NumberFormatException e) {
            return MafRecord.NA_INT;
        }
    }

    private Float getPartPercentage(int index, String[] parts)
    {
        try {
            float result = MafRecord.NA_FLOAT;
            String part = parts[index];
            if (part.contains("%")) {
                result = Float.parseFloat(part.replace("%", "")) / Float.parseFloat("100");
            } else {
                result = Float.parseFloat(part);
            }
            return result;
        } catch (ArrayIndexOutOfBoundsException e) {
            return MafRecord.NA_FLOAT;
        } catch (NumberFormatException e) {
            return MafRecord.NA_FLOAT;
        }
    }

	public String adjustDataLine(String dataLine)
	{
		String line = dataLine;
		String[] parts = line.split("\t", -1);

		// diff should be zero if (# of headers == # of data cols)
		int diff = this.getHeaderCount() - parts.length;

		// number of header columns are more than number of data columns
		if (diff > 0)
		{
			// append appropriate number of tabs
			for (int i = 0; i < diff; i++)
			{
				line += "\t";
			}
		}
		// number of data columns are more than number of header columns
		else if (diff < 0)
		{
			line = "";

			// just truncate the data (discard the trailing columns)
			for (int i = 0; i < this.getHeaderCount(); i++)
			{
				line += parts[i];

				if (i < this.getHeaderCount() - 1)
				{
					line += "\t";
				}
			}
		}

		return line;
	}

    public int getChrIndex() {
        return chrIndex;
    }

    public int getNcbiIndex() {
        return ncbiIndex;
    }

    public int getStartPositionIndex() {
        return startPositionIndex;
    }

    public int getEndPositionIndex() {
        return endPositionIndex;
    }

    public int getHugoGeneSymbolIndex() {
        return hugoGeneSymbolIndex;
    }

    public int getEntrezGeneIdIndex() {
        return entrezGeneIdIndex;
    }

    public int getReferenceAlleleIndex() {
        return referenceAlleleIndex;
    }

    public int getVariantClassificationIndex() {
        return variantClassificationIndex;
    }

    public int getVariantTypeIndex() {
        return variantTypeIndex;
    }

    public int getCenterIndex() {
        return centerIndex;
    }

    public int getStrandIndex() {
        return strandIndex;
    }

    public int getTumorSeqAllele1Index() {
        return tumorSeqAllele1Index;
    }

    public int getTumorSeqAllele2Index() {
        return tumorSeqAllele2Index;
    }

    public int getDbSNPIndex() {
        return dbSNPIndex;
    }

    public int getTumorSampleIndex() {
        return tumorSampleIndex;
    }

    public int getMutationStatusIndex() {
        return mutationStatusIndex;
    }

	public int getValidationStatusIndex() {
		return validationStatusIndex;
	}

	public int getSequencerIndex() {
		return sequencerIndex;
	}

	public int getDbSnpValStatusIndex() {
		return dbSnpValStatusIndex;
	}

	public int getMatchedNormSampleBarcodeIndex() {
		return matchedNormSampleBarcodeIndex;
	}

	public int getMatchNormSeqAllele1Index() {
		return matchNormSeqAllele1Index;
	}

	public int getMatchNormSeqAllele2Index() {
		return matchNormSeqAllele2Index;
	}

	public int getTumorValidationAllele1Index() {
		return tumorValidationAllele1Index;
	}

	public int getTumorValidationAllele2Index() {
		return tumorValidationAllele2Index;
	}

	public int getMatchNormValidationAllele1Index() {
		return matchNormValidationAllele1Index;
	}

	public int getMatchNormValidationAllele2Index() {
		return matchNormValidationAllele2Index;
	}

	public int getVerificationStatusIndex() {
		return verificationStatusIndex;
	}

	public int getSequencingPhaseIndex() {
		return sequencingPhaseIndex;
	}

	public int getSequenceSourceIndex() {
		return sequenceSourceIndex;
	}

	public int getValidationMethodIndex() {
		return validationMethodIndex;
	}

	public int getScoreIndex() {
		return scoreIndex;
	}

	public int getBamFileIndex() {
		return bamFileIndex;
	}

	public int getTumorAltCountIndex() {
		return tumorAltCountIndex;
	}

	public int getTumorRefCountIndex() {
		return tumorRefCountIndex;
	}

	public int getNormalAltCountIndex() {
		return normalAltCountIndex;
	}

	public int getNormalRefCountIndex() {
		return normalRefCountIndex;
	}

    public int getTumorTotCovIndex() {
        return tTotCovIndex;
    }

    public int getTumorVarCovIndex() {
        return tVarCovIndex;
    }

	public int getOncoProteinChangeIndex() {
		return oncoProteinChangeIndex;
	}

	public int getOncoVariantClassificationIndex() {
		return oncoVariantClassificationIndex;
	}

	public int getOncoCosmicOverlappingIndex() {
		return oncoCosmicOverlappingIndex;
	}

	public int getOncoDbSnpRsIndex() {
		return oncoDbSnpRsIndex;
	}

	public int getOncoDbSnpValStatusIndex() {
		return oncoDbSnpValStatusIndex;
	}

	public int getOncoGeneSymbolIndex() {
		return oncoGeneSymbolIndex;
	}

	public int getMaFImpactIndex()
	{
		return maFImpactIndex;
	}

	public int getMaLinkVarIndex()
	{
		return maLinkVarIndex;
	}

	public int getMaLinkMsaIndex()
	{
		return maLinkMsaIndex;
	}

	public int getMaLinkPdbIndex()
	{
		return maLinkPdbIndex;
	}

	public int getMaProteinChangeIndex()
	{
		return maProteinChangeIndex;
	}

	public int getOncoRefseqMrnaIdIndex()
	{
		return oncoRefseqMrnaIdIndex;
	}

	public int getOncoExonAffectedIndex()
	{
		return oncoExonAffectedIndex;
	}

	public int getOncoTranscriptChangeIndex()
	{
		return oncoTranscriptChangeIndex;
	}

	public int getOncoUniprotNameIndex()
	{
		return oncoUniprotNameIndex;
	}

	public int getOncoCodonChangeIndex()
	{
		return oncoCodonChangeIndex;
	}

	public int getOncoRefseqProtIdIndex()
	{
		return oncoRefseqProtIdIndex;
	}

	public void setOncoRefseqProtIdIndex(int oncoRefseqProtIdIndex)
	{
		this.oncoRefseqProtIdIndex = oncoRefseqProtIdIndex;
	}

	public int getOncoUniprotAccessionIndex()
	{
		return oncoUniprotAccessionIndex;
	}

	public void setOncoUniprotAccessionIndex(int oncoUniprotAccessionIndex)
	{
		this.oncoUniprotAccessionIndex = oncoUniprotAccessionIndex;
	}

	public int getOncoProteinChangeBeIndex()
	{
		return oncoProteinChangeBeIndex;
	}

	public int getOncoGeneSymbolBeIndex()
	{
		return oncoGeneSymbolBeIndex;
	}

	public int getOncoRefseqMrnaIdBeIndex()
	{
		return oncoRefseqMrnaIdBeIndex;
	}

	public int getOncoVariantClassificationBeIndex()
	{
		return oncoVariantClassificationBeIndex;
	}

	public int getOncoUniprotNameBeIndex()
	{
		return oncoUniprotNameBeIndex;
	}

	public int getOncoCodonChangeBeIndex()
	{
		return oncoCodonChangeBeIndex;
	}

	public int getOncoTranscriptChangeBeIndex()
	{
		return oncoTranscriptChangeBeIndex;
	}

	public int getOncoExonAffectedBeIndex()
	{
		return oncoExonAffectedBeIndex;
	}

	public int getOncoRefseqProtIdBeIndex()
	{
		return oncoRefseqProtIdBeIndex;
	}

	public int getOncoUniprotAccessionBeIndex()
	{
		return oncoUniprotAccessionBeIndex;
	}

	public int getColumnIndex(String colName)
	{
		Integer index = this.columnIndexMap.get(colName.toLowerCase());

		if (index == null)
		{
			index = -1;
		}

		return index;
	}

    public int getHeaderCount() {
		return headerCount;
	}


	// Static Utility Methods

	/**
	 * Generates a key for the given MAF record. The generated key
	 * is in the form of :
	 *   [chromosome]_[startPosition]_[endPosition]_[referenceAllele]_[tumorAllele]
	 *
	 * This method returns null, if tumor allele cannot be determined for the
	 * given record.
	 *
	 * @param record    MAF record representing a single line in a MAF
	 * @return          key for the given record
	 */
	public static String generateKey(MafRecord record)
	{
		String chr = record.getChr();
		Long start = record.getStartPosition();
		Long end = record.getEndPosition();
		String refAllele = record.getReferenceAllele();
		String tumAllele = null;

		// determine tumor allele: take the one that is different from
		// the reference allele
		if (!refAllele.equalsIgnoreCase(record.getTumorSeqAllele1()))
		{
			tumAllele = record.getTumorSeqAllele1();
		}
		else if (!refAllele.equalsIgnoreCase(record.getTumorSeqAllele2()))
		{
			tumAllele = record.getTumorSeqAllele2();
		}

		String key = null;

		// update key if tumor allele is valid
		if (tumAllele != null)
		{
			key = chr + "_" + start + "_" + end + "_" + refAllele + "_" + tumAllele;
		}

		return key;
	}
}