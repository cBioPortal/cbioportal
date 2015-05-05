package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutSig;
import org.mskcc.cgds.dao.DaoOncotatorCache;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.MutSig;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.portal.oncotator.OncotatorRecord;

/**
 * Tests the DaoOncotator Cache.
 */
public class TestDaoOncotatorCache extends TestCase {

    /**
     * Tests DaoOncotator.
     *
     */
    public void testDaoOncotator() throws DaoException {
        ResetDatabase.resetDatabase();

        OncotatorRecord record = new OncotatorRecord("cbio_123");
        record.setCosmicOverlappingMutations("cosmic");
        record.setExonAffected(2);
        record.setGene("TP53");
        record.setGenomeChange("genomic");
        record.setProteinChange("protein");
        record.setVariantClassification("missense");
        record.setDbSnpRs("db_snp");
        DaoOncotatorCache cache = DaoOncotatorCache.getInstance();
        int numRecords = cache.put(record);
        assertEquals(1, numRecords);
        
        record = cache.get("cbio_123");
        assertEquals("cbio_123", record.getKey());
        assertEquals("cosmic", record.getCosmicOverlappingMutations());
        assertEquals("TP53", record.getGene());
        assertEquals("genomic", record.getGenomeChange());
        assertEquals("protein", record.getProteinChange());
        assertEquals("missense", record.getVariantClassification());
        assertEquals("db_snp", record.getDbSnpRs());
    }

}


