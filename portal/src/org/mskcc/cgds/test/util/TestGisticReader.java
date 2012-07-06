package org.mskcc.cgds.test.util;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.util.GisticReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;
import java.io.IOException;

public class TestGisticReader extends TestCase {

    public void testGisticReader() throws DaoException, IOException {
    File metadata = new File("./test_data/testCancerStudy.txt");
    File gisticFile = new File("test_data/test-gistic-amp.txt");
    File gisticTable_file = new File("test_data/test-gistic-table-amp.txt");
    ProgressMonitor pm = new ProgressMonitor();

    int cancerStudyId;
    cancerStudyId = GisticReader.getCancerStudyInternalId(metadata);

    GisticReader reader = new GisticReader();
    reader.parse_Table(gisticTable_file, pm);


    }
}
