package org.mskcc.cbio.portal.util;

import org.junit.Assert;
import org.junit.Test;
import org.mskcc.cbio.portal.model.StructuralVariant;

/**
 * @author ochoaa
 */
public class TestStructuralVariantUtil {
    private StructuralVariantUtil structuralVariantUtil = new StructuralVariantUtil("", null);

    @Test
    public void testValidStructuralVariantRecordWithCompleteGeneTranscriptInfo() {
        // confirm that record meets minimum requirements for import
        // record has site 1 entrez id and missing site 1 hugo symbol
        // and missing site 2 entrez id but present site 2 hugo symbol
        StructuralVariant record = new StructuralVariant();
        record.setSampleId("SMP0001");
        record.setSvStatus("SOMATIC");
        record.setSite1EntrezGeneId(654684L);
        record.setSite1HugoSymbol("NA");
        record.setSite1EnsemblTranscriptId("ENST29O3403");
        record.setSite1Region("exon");
        record.setSite1RegionNumber(5);
        record.setSite2EntrezGeneId(-1L);
        record.setSite2HugoSymbol("ALK1");
        record.setSite2EnsemblTranscriptId("ENST2843757657");
        record.setSite2Region("exon");
        record.setSite2RegionNumber(7);
        Assert.assertTrue(structuralVariantUtil.hasRequiredStructuralVariantFields(record));
    }

    @Test
    public void testValidStructuralVariantRecordMissingGeneInfo() {
        // confirm that record does meet minimum requirements for import
        // record is missing site 1 gene info (renders record invalid)
        // and missing site 2 entrez id but present site 2 hugo symbol
        StructuralVariant record = new StructuralVariant();
        record.setSampleId("SMP0001");
        record.setSvStatus("SOMATIC");
        record.setSite1EntrezGeneId(Long.MIN_VALUE);
        record.setSite1HugoSymbol("NA");
        record.setSite1EnsemblTranscriptId("ENST29O3403");
        record.setSite1Region("exon");
        record.setSite1RegionNumber(5);
        record.setSite2EntrezGeneId(Long.MIN_VALUE);
        record.setSite2HugoSymbol("ALK1");
        record.setSite2EnsemblTranscriptId("ENST2843757657");
        record.setSite2Region("exon");
        record.setSite2RegionNumber(7);
        Assert.assertTrue(structuralVariantUtil.hasRequiredStructuralVariantFields(record));
    }

    @Test
    public void testValidStructuralVariantRecordMissingSampleId() {
        StructuralVariant record = new StructuralVariant();
        record.setSvStatus("SOMATIC");
        record.setSite1EntrezGeneId(654684L);
        record.setSite1HugoSymbol("NA");
        record.setSite1EnsemblTranscriptId("NA");
        record.setSite1Region("NA");
        record.setSite1RegionNumber(-1);
        record.setSite2EntrezGeneId(Long.MIN_VALUE);
        record.setSite2HugoSymbol("ALK1");
        record.setSite2EnsemblTranscriptId("NA");
        record.setSite2Region("NA");
        record.setSite2RegionNumber(-1);
        Assert.assertFalse(structuralVariantUtil.hasRequiredStructuralVariantFields(record));
    }

    @Test
    public void testValidStructuralVariantRecordMissingSvStatus() {
        StructuralVariant record = new StructuralVariant();
        record.setSampleId("SMP0001");
        record.setSite1EntrezGeneId(654684L);
        record.setSite1HugoSymbol("NA");
        record.setSite1EnsemblTranscriptId("NA");
        record.setSite1Region("NA");
        record.setSite1RegionNumber(-1);
        record.setSite2EntrezGeneId(Long.MIN_VALUE);
        record.setSite2HugoSymbol("ALK1");
        record.setSite2EnsemblTranscriptId("NA");
        record.setSite2Region("NA");
        record.setSite2RegionNumber(-1);
        Assert.assertFalse(structuralVariantUtil.hasRequiredStructuralVariantFields(record));
    }
}
