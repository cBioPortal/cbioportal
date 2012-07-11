package org.mskcc.cgds.test.util;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.Gistic;
import org.mskcc.cgds.util.GisticReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestGisticReader extends TestCase {

    public void testGisticReader() throws DaoException, IOException, Exception {
    File metadata = new File("./test_data/testCancerStudy.txt");
    File gisticFile = new File("test_data/test-gistic-amp.txt");
    File gisticTable_file = new File("test_data/test-gistic-table-amp.txt");
    ProgressMonitor pm = new ProgressMonitor();
    Gistic dummyGistic = new Gistic();

    GisticReader reader = new GisticReader();

    // just make sure they run
    ArrayList<Gistic> table_reads = reader.parse_Table(gisticTable_file);
    
    ArrayList<Gistic> nontable_reads = reader.parse_NonTabular(gisticFile);
    
    assertTrue(reader.parseAmpDel(gisticTable_file) == Gistic.AMPLIFIED);
    assertTrue(reader.parseAmpDel(gisticFile) == Gistic.AMPLIFIED);
    
//    ArrayList<Gistic> merged = reader.mergeGistics(table_reads, nontable_reads, Gistic.AMPLIFIED);

//    assertTrue(merged.size() == 14);        // the number of gistics in the test files
//
//        for (Gistic g : merged) {
//            assertTrue(g.getqValue() != dummyGistic.getqValue()) ;
//            assertTrue(g.getRes_qValue() != dummyGistic.getRes_qValue()) ;
//            assertTrue(g.getChromosome() != dummyGistic.getChromosome());
//            assertTrue(g.getPeakStart() != dummyGistic.getPeakStart());
//            assertTrue(g.getPeakEnd() != dummyGistic.getPeakEnd());
//            assertTrue(g.getAmpDel() == Gistic.AMPLIFIED);
//        }

    int cancerStudyId = reader.getCancerStudyInternalId(metadata);
//    reader.loadGistic(cancerStudyId, gisticTable_file, gisticFile);
    }
}
