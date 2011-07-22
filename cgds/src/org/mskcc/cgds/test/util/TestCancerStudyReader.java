package org.mskcc.cgds.test.util;

import java.io.File;

import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.CancerStudyReader;

public class TestCancerStudyReader extends TestCase {

   public void testCancerStudyReader() throws Exception {
      ResetDatabase.resetDatabase();
      // load cancers
      String[] args = { "testData/cancers.txt" };
      ImportTypesOfCancers.main( args );
      
      File file = new File("testData/cancer_study.txt");
      CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy( file );
      
      CancerStudy expectedCancerStudy = DaoCancerStudy.getCancerStudyByIdentifier( "test_brca" ); 
      assertEquals(expectedCancerStudy, cancerStudy);
      
      file = new File("testData/cancer_study_bad.txt");
      try {
         cancerStudy = CancerStudyReader.loadCancerStudy( file );
         fail( "Should have thrown DaoException." );
      } catch (DaoException e) {
         assertTrue( e.getMessage().equals( 
                  "cancerStudy.getTypeOfCancerId() 'brcaXXX' does not refer to a TypeOfCancer."));
      }
   }
}
