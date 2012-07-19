package org.mskcc.cbio.cgds.test.util;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoGistic;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Gistic;
import org.mskcc.cgds.util.GisticReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class TestGisticReader extends TestCase {

    public void testGisticReader() throws DaoException, IOException, Exception {
//    File metadata = new File("./test_data/testCancerStudy.txt");
//    File gisticFile = new File("test_data/test-gistic-amp.txt");
//    File gisticTable_file = new File("test_data/test-gistic-table-amp.txt");
//    ProgressMonitor pm = new ProgressMonitor();
//    Gistic dummyGistic = new Gistic();
//
//    GisticReader reader = new GisticReader();
//
//    ArrayList<Gistic> table_reads = reader.parse_Table(gisticTable_file);
//    ArrayList<Gistic> nontable_reads = reader.parse_NonTabular(gisticFile);
//    assertTrue(reader.parseAmpDel(gisticTable_file) == Gistic.AMPLIFIED);
//    assertTrue(reader.parseAmpDel(gisticFile) == Gistic.AMPLIFIED);
//
//    // mergeGistic
//    CanonicalGene madeup_gene1 = new CanonicalGene(1l, "DIO2");
//    CanonicalGene madeup_gene2 = new CanonicalGene(2l, "NIKI");
//
//    ArrayList<CanonicalGene> testgenes;
//    testgenes = new ArrayList<CanonicalGene>();
//    testgenes.add(madeup_gene1);
//    testgenes.add(madeup_gene2);
//
//    Gistic testgistic1 = new Gistic(1, -1, 1, 2, 0.05d, 0.05d, testgenes, Gistic.AMPLIFIED);
//    Gistic testgistic2 = new Gistic(1, 1, -1, 2, 0.05d, 0.05d, testgenes, Gistic.AMPLIFIED);
//
//    testgistic1 = reader.mergeGistics(testgistic1, testgistic2);
//    assertTrue(testgistic1.getChromosome() == 1);
//
//    // database test
//    DaoGistic.addGistic(testgistic1);
//    DaoGistic.getGisticByROI(testgistic1.getChromosome(), testgistic1.getPeakStart(), testgistic2.getPeakEnd());
//    DaoGistic.deleteGistic(testgistic1.getInternalId());
//
//
//    ArrayList<Gistic> merged = reader.mergeGisticLists(table_reads, nontable_reads, Gistic.AMPLIFIED);

    }
}
