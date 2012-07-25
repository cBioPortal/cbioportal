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
	  // TBD: change this to use getResourceAsStream()
      ImportTypesOfCancers.load(new ProgressMonitor(), new File("target/test-classes/cancers.txt"));
	  // TBD: change this to use getResourceAsStream()
      File file = new File("target/test-classes/cancer_study.txt");
      CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy( file );
      
      CancerStudy expectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId( "test_brca" );
      assertEquals(expectedCancerStudy, cancerStudy);
      // TBD: change this to use getResourceAsStream()
      file = new File("target/test-classes/cancer_study_bad.txt");
      try {
         cancerStudy = CancerStudyReader.loadCancerStudy( file );
         fail( "Should have thrown DaoException." );
      } catch (DaoException e) {
         assertTrue( e.getMessage().equals( 
                  "cancerStudy.getTypeOfCancerId() 'brcaXXX' does not refer to a TypeOfCancer."));
      }
   }
}
