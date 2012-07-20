package org.mskcc.cbio.cgds.util;

import java.io.File;

import junit.framework.TestCase;

import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.util.CancerStudyReader;
import org.mskcc.cbio.cgds.util.ProgressMonitor;

/**
 * JUnit test for CancerStudyReader class.
 */
public class TestCancerStudyReader extends TestCase {

   public void testCancerStudyReader() throws Exception {
      ResetDatabase.resetDatabase();
      // load cancers
      ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));

      File file = new File("test_data/cancer_study.txt");
      CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy( file );
      
      CancerStudy expectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId( "test_brca" );
      assertEquals(expectedCancerStudy, cancerStudy);
      
      file = new File("test_data/cancer_study_bad.txt");
      try {
         cancerStudy = CancerStudyReader.loadCancerStudy( file );
         fail( "Should have thrown DaoException." );
      } catch (DaoException e) {
         assertTrue( e.getMessage().equals( 
                  "cancerStudy.getTypeOfCancerId() 'brcaXXX' does not refer to a TypeOfCancer."));
      }
   }
}
