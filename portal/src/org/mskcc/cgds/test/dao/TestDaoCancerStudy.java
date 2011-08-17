package org.mskcc.cgds.test.dao;

import java.util.ArrayList;
import java.io.File;

import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.ProgressMonitor;

public class TestDaoCancerStudy extends TestCase{

   public void testDaoCancerStudy() throws Exception {
      ResetDatabase.resetDatabase();
      // load cancers
      ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));

      CancerStudy cancerStudy = new CancerStudy( "GBM", "GBM Description", "gbm", "brca", false );
      DaoCancerStudy.addCancerStudy(cancerStudy);
      assertEquals( 1, cancerStudy.getInternalId() ); //   `CANCER_STUDY_ID` auto_increment counts from 1

      cancerStudy.setName("Breast");
      cancerStudy.setCancerStudyStablId( "breast" );
      cancerStudy.setDescription("Breast Description");
      DaoCancerStudy.addCancerStudy(cancerStudy);
      assertEquals( 2, cancerStudy.getInternalId() ); //

      ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
      assertEquals(2, list.size());

      cancerStudy = list.get(0);
      assertEquals("gbm", cancerStudy.getCancerStudyStableId());
      assertEquals("GBM", cancerStudy.getName());
      assertEquals("GBM Description", cancerStudy.getDescription() );
      assertEquals( 1, cancerStudy.getInternalId());

      cancerStudy = list.get(1);
      assertEquals( 2, cancerStudy.getInternalId());
      assertEquals("Breast Description", cancerStudy.getDescription() );
      assertEquals("Breast", cancerStudy.getName());

      cancerStudy = DaoCancerStudy.getCancerStudyByStableId("gbm");
      assertEquals("gbm", cancerStudy.getCancerStudyStableId());
      assertEquals("GBM", cancerStudy.getName());
      assertEquals("GBM Description", cancerStudy.getDescription() );

      assertEquals( null, DaoCancerStudy.getCancerStudyByStableId("no such study") );
      assertTrue( DaoCancerStudy.doesCancerStudyExistByStableId( cancerStudy.getCancerStudyStableId() ) );
      assertFalse( DaoCancerStudy.doesCancerStudyExistByStableId( "no such study" ) );

      assertTrue( DaoCancerStudy.doesCancerStudyExistByInternalId( cancerStudy.getInternalId() ) );
      assertFalse( DaoCancerStudy.doesCancerStudyExistByInternalId( -1 ) );
      
      DaoCancerStudy.deleteCancerStudy( cancerStudy.getInternalId() );

      list = DaoCancerStudy.getAllCancerStudies();
      assertEquals(1, list.size());
   }

   public void testDaoCancerStudy2() throws Exception {
      ResetDatabase.resetDatabase();
      // load cancers
      ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));

      CancerStudy cancerStudy1 = new CancerStudy( "GBM public study x", "GBM Description", "brca", true );
      DaoCancerStudy.addCancerStudy(cancerStudy1);

      CancerStudy cancerStudy = new CancerStudy( "GBM private study x", "GBM Description 2", "brca", false );
      DaoCancerStudy.addCancerStudy(cancerStudy);

      cancerStudy = new CancerStudy( "Breast", "Breast Description", "brca", false );
      DaoCancerStudy.addCancerStudy(cancerStudy);

      ArrayList<CancerStudy> list = DaoCancerStudy.getAllCancerStudies();
      assertEquals(3, list.size());

      cancerStudy = list.get(0);
      assertEquals(1, cancerStudy.getInternalId() );
      assertEquals("GBM Description", cancerStudy1.getDescription());
      assertEquals("GBM public study x", cancerStudy1.getName());
      assertEquals(true, cancerStudy1.isPublicStudy());

      cancerStudy1 = list.get(1);
      assertEquals(2, cancerStudy1.getInternalId());
      assertEquals("GBM private study x", cancerStudy1.getName());
      assertEquals("GBM Description 2", cancerStudy1.getDescription());
      assertEquals(false, cancerStudy1.isPublicStudy());

      cancerStudy1 = list.get(2);
      assertEquals(3, cancerStudy1.getInternalId());
      assertEquals("Breast", cancerStudy1.getName());
      assertEquals("Breast Description", cancerStudy1.getDescription());
      assertEquals(false, cancerStudy1.isPublicStudy());

      cancerStudy1 = DaoCancerStudy.getCancerStudyByInternalId(1);
      assertEquals(1, cancerStudy1.getInternalId());
      assertEquals("GBM Description", cancerStudy1.getDescription());
      assertEquals("GBM public study x", cancerStudy1.getName());
      assertEquals(true, cancerStudy1.isPublicStudy());

      assertEquals(3, DaoCancerStudy.getCount());
      DaoCancerStudy.deleteCancerStudy(1);
      assertEquals(2, DaoCancerStudy.getCount());
      DaoCancerStudy.deleteCancerStudy(1);
      assertEquals(2, DaoCancerStudy.getCount());
      DaoCancerStudy.deleteAllRecords();
      assertEquals(0, DaoCancerStudy.getCount());
      assertEquals( null, DaoCancerStudy.getCancerStudyByInternalId( CancerStudy.NO_SUCH_STUDY) );
   }

   /*
    * Use carefully; will DELETE ENTIRE dbms
    * Built just to test  ResetDatabase.resetDatabase();
   
   public static void testResetDatabase(){
      CancerStudy cancerStudy = new CancerStudy();
      for( int i=0; i <= ResetDatabase.MAX_RESET_SIZE; i++ ){
         cancerStudy.setName("GBM");
         cancerStudy.setDescription("GBM Description");
         try {
            DaoCancerStudy.addCancerStudy(cancerStudy);
         } catch (DaoException e) {
         }
      }
      try {
         ResetDatabase.resetDatabase();
         fail( "Should have thrown DaoException." );
      } catch (DaoException e) {
         assertTrue( e.getMessage().contains( " studies, and we don't reset a database with more than " + 
                  ResetDatabase.MAX_RESET_SIZE + " records."));
      }
      try {
         ResetDatabase.resetAnySizeDatabase();
      } catch (DaoException e) {
      }      
   }
    */
   
}
