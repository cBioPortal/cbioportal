package org.mskcc.portal.test.util;

import junit.framework.TestCase;
import org.mskcc.portal.model.MafRecord;
import org.mskcc.portal.util.MafUtil;

/**
 * JUnit Tests for MAF Util.
 *
*/ 
public class TestMafUtil extends TestCase {
    
    public void testMafUtil1() {
        String headerLine = "BAM_file\tCenter\tChromosome\tEnd_position\tEntrez_Gene_Id\tHugo_Symbol" +
                "\tMatch_Norm_Seq_Allele1\tMatch_Norm_Seq_Allele2\tMatch_Norm_Validation_Allele1" +
                "\tMatch_Norm_Validation_Allele2\tMatched_Norm_Sample_Barcode\tMutation_Status" +
                "\tNCBI_Build\tReference_Allele\tScore\tSequence_Source\tSequencer\tSequencing_Phase" +
                "\tStart_position\tStrand\tTumor_Sample_Barcode\tTumor_Seq_Allele1\tTumor_Seq_Allele2" +
                "\tTumor_Validation_Allele1\tTumor_Validation_Allele2\tValidation_Method" +
                "\tValidation_Status\tVariant_Classification\tVariant_Type\tVerification_Status" +
                "\tdataset\tdbSNP_RS\tdbSNP_Val_Status\tpatient\ttype\tclassification" +
                "\tstart\tend\tgene\tchr\tref_allele\ttum_allele1\ttum_allele2\tnewbase" +
                "\tcontext_orig\tcontext65\tgene_name";
        MafUtil mafUtil = new MafUtil(headerLine);
        assertEquals(1, mafUtil.getCenterIndex());
        assertEquals(2, mafUtil.getChrIndex());
        assertEquals(3, mafUtil.getEndPositionIndex());
        assertEquals(4, mafUtil.getEntrezGeneIdIndex());
        assertEquals(5, mafUtil.getHugoGeneSymbolIndex());
        assertEquals(11, mafUtil.getMutationStatusIndex());
        assertEquals(12, mafUtil.getNcbiIndex());
        assertEquals(13, mafUtil.getReferenceAlleleIndex());
        assertEquals(18, mafUtil.getStartPositionIndex());
        assertEquals(19, mafUtil.getStrandIndex());
        assertEquals(20, mafUtil.getTumorSampleIndex());
        assertEquals(27, mafUtil.getVariantClassificationIndex());
        assertEquals(28, mafUtil.getVariantTypeIndex());
    }
    
    public void testMafUtil2() {
        String headerLine = "Hugo_Symbol\tEntrez_Gene_Id\tCenter\tNCBI_Build\tChromosome\tStart_position" +
                "\tEnd_position\tStrand\tVariant_Classification\tVariant_Type\tReference_Allele" +
                "\tTumor_Seq_Allele1\tTumor_Seq_Allele2\tdbSNP_RS\tdbSNP_Val_Status\tTumor_Sample_Barcode" +
                "\tMatched_Norm_Sample_Barcode\tMatch_Norm_Seq_Allele1\tMatch_Norm_Seq_Allele2" +
                "\tTumor_Validation_Allele1\tTumor_Validation_Allele2\tMatch_Norm_Validation_Allele1" +
                "\tMatch_Norm_Validation_Allele2\tVerification_Status\tValidation_Status\tMutation_Status" +
                "\tSequencing_Phase\tSequence_Source\tValidation_Method\tScore\tBAM_file\tSequencer" +
                "\tchromosome_name\tstart\tstop\treference\tvariant\ttype\tgene_name\ttranscript_name" +
                "\ttranscript_species\ttranscript_source\ttranscript_version\tstrand\ttranscript_status" +
                "\ttrv_type\tc_position\tamino_acid_change\tucsc_cons\tdomain\tall_domains" +
                "\tdeletion_substructures\ttranscript_error comment\tMA:variant\tMA:GE.rank\tMA:CNA" +
                "\tMA:OV.variant.samples\tMA:OV.gene.samples\tMA:mapping.issue\tMA:FImpact" +
                "\tMA:FI.score\tMA:Func.region\tMA:bindsite.protein\tMA:bindsite.DNA/RNA" +
                "\tMA:bindsite.sm.mol\tMA:CancerGenes\tMA:TS\tMA:OG\tMA:COSMIC.mutations" +
                "\tMA:COSMIC.cancers\tMA:Uniprot.regions\tMA:TS.interacts\tMA:OG.interacts" +
                "\tMA:Pfam.domain\tMA:link.var\tMA:link.MSA\tMA:link.PDB";
        MafUtil mafUtil = new MafUtil(headerLine);
        assertEquals(0, mafUtil.getHugoGeneSymbolIndex());
        assertEquals(1, mafUtil.getEntrezGeneIdIndex());
        assertEquals(2, mafUtil.getCenterIndex());
        assertEquals(3, mafUtil.getNcbiIndex());
        assertEquals(4, mafUtil.getChrIndex());
        assertEquals(5, mafUtil.getStartPositionIndex());
        assertEquals(6, mafUtil.getEndPositionIndex());
        assertEquals(7, mafUtil.getStrandIndex());
        assertEquals(8, mafUtil.getVariantClassificationIndex());
        assertEquals(9, mafUtil.getVariantTypeIndex());
        assertEquals(10, mafUtil.getReferenceAlleleIndex());
        assertEquals(15, mafUtil.getTumorSampleIndex());
        assertEquals(25, mafUtil.getMutationStatusIndex());
        
        String dataLine = "AGL\t178\tgenome.wustl.edu\t36\t1\t100122272\t100122272\t+\tMissense_Mutation\tSNP" +
                "\tG\tG\tA\tnovel\tUnknown\tTCGA-13-1405-01A-01W-0494-09\tTCGA-13-1405-10A-01W-0495-09" +
                "\tG\tG\tG\tA\tG\tG\tUnknown\tValid\tSomatic\t4\tCapture\t454_PCR_WGA\t1\tdbGAP\tIllumina GAIIx" +
                "\t1\t100122272\t100122272\tG\tA\tSNP\tAGL\tNM_000028\thuman\tgenbank\t54_36p\t1\tvalidated" +
                "\tmissense\tc.2317\tp.E773K\t1" +
                "\tNULL\tsuperfamily_(Trans)glycosidases,HMMPfam_GDE_C,superfamily_Six-hairpin glycosidases" +
                "\t-\tno_errors\t1,100122272,G,A\t0.5081\t0\t1\t1\t\tlow\t1.24\t\t\t\t\tEntrez Query :: Stability" +
                "\t\t\t\t\t\t\t\t\thttp://mutationassessor.org?cm=var&var=1,100122272,G,A&fts=all" +
                "\thttp://mutationassessor.org/?cm=msa&ty=f&p=GDE_HUMAN&rb=601&re=800&var=E773K\t";
        MafRecord record = mafUtil.parseRecord(dataLine);
        assertEquals("AGL", record.getHugoGeneSymbol());
        assertEquals(178L, record.getEntrezGeneId());
        assertEquals("genome.wustl.edu", record.getCenter());
        assertEquals("36", record.getNcbiBuild());
        assertEquals("1", record.getChr());
        assertEquals(100122272, record.getStartPosition());
        assertEquals(100122272, record.getEndPosition());
        assertEquals("+", record.getStrand());
        assertEquals("Missense_Mutation", record.getVariantClassification());
        assertEquals("SNP", record.getVariantType());
        assertEquals("G", record.getReferenceAllele());
        assertEquals("G", record.getTumorSeqAllele1());
        assertEquals("A", record.getTumorSeqAllele2());
        assertEquals("novel", record.getDbSNP_RS());
        assertEquals("TCGA-13-1405-01A-01W-0494-09", record.getTumorSampleID());
        assertEquals("Valid", record.getValidationStatus());
        assertEquals("Somatic", record.getMutationStatus());
    }
}
