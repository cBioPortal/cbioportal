package org.mskcc.cgds.test.scripts;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.scripts.MutationFilter;
import org.mskcc.cgds.scripts.ResetDatabase;

public class TestMutationFilter extends TestCase {
   
   protected void setUp(){
      try {
         ResetDatabase.resetDatabase();
      } catch (DaoException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      /*
       * test_germline_white_list_file.txt contains
         FOO -3-
         BAR -234-
         BIG -234234-

       * testData/test_somatic_white_list_file1.txt contains
         ONE 1
         FOUR 4
         FOO 3
         TWELVE 12

       * testData/test_somatic_white_list_file2.txt contains
         FOUR 4
         FOO 3
         SEVENSEVENSEVEN 777         

       */
   }
   
   public void testBadWhiteLists( ){
      try {
         new MutationFilter( 
                  false,
                  "no_such_file",
                  "no_such_file_either"
         );
         Assert.fail( "Should throw IllegalArgumentException");
      } catch (IllegalArgumentException e) {
         assertEquals( "Gene list 'no_such_file' not found.", e.getMessage() );
      }
   }
   
   public void testNoWhitelists( ){

      MutationFilter myMutationFilter = new MutationFilter( );

      alwaysRejectTheseMutations( myMutationFilter );      
      
      // accept all of these, because a MutationFilter without whitelists
      // accepts all mutations other than Silent, LOH, Intron and Wildtype mutations
      // not valid && somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Unknown",        // validationStatus,
               "Unknown",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // valid but not somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Valid",        // validationStatus,
               "Unknown",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // not valid but somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Unknown",        // validationStatus,
               "Somatic",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // valid && somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Valid",        // validationStatus,
               "Somatic",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // valid && somatic
      // testing safeStringTest()
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "vALid_as_hell",        // validationStatus,
               "SOMatic_for_sure",        // mutationStatus,
               "Unknown"         // mutationType
            );

   }

   public void testAcceptMutation_germline_white_list() throws DaoException {
      
      // load genes
      loadGene( "FOO", 3L  );
      loadGene( "BAR", 234L  );
      loadGene( "BIG", 234234L  );
                 
      // create MutationFilter
      MutationFilter myMutationFilter = new MutationFilter( 
               false,
               "testData/test_germline_white_list_file.txt"
         );

      alwaysRejectTheseMutations( myMutationFilter );      
      tryGermlineMutations( myMutationFilter );      
   }
   
   private void tryGermlineMutations( MutationFilter myMutationFilter ){

      // a germline on whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            true, 
            3L, 
            "Unknown",        // validationStatus,
            "GERMLINE",        // mutationStatus,
            "Unknown"         // mutationType
         );

      // a germline on whitelist, but also missense
      nowTestAcceptMutation( 
            myMutationFilter,
            false, 
            3L, 
            "Unknown",        // validationStatus,
            "GERMLINE",        // mutationStatus,
            "Missense_Mutation"         // mutationType
         );

      // a germline NOT on whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            false, 
            9999L, 
            "Unknown",        // validationStatus,
            "GERMLINE",        // mutationStatus,
            "Unknown"         // mutationType
         );

   }

   public void testAcceptMutation_germline_and_somatic_white_lists() throws DaoException {
      
      // load genes
      loadGene( "FOO", 3L  );
      loadGene( "BAR", 234L  );
      loadGene( "BIG", 234234L  );
      
      //                "testData/test_somatic_white_list_file1.txt",
      loadGene( "ONE", 1L  );
      loadGene( "FOUR", 4L  );
      loadGene( "FOO", 3L  );
      
      //                "testData/test_somatic_white_list_file2.txt"
      loadGene( "TWELVE", 12L  );
      loadGene( "FOUR", 4L  );
      loadGene( "FOO", 3L  );
      loadGene( "SEVENSEVENSEVEN", 777L  );
      
      // create MutationFilter
      MutationFilter myMutationFilter = new MutationFilter( 
               false,
               "testData/test_germline_white_list_file.txt",
               "testData/test_somatic_white_list_file1.txt",
               "testData/test_somatic_white_list_file2.txt"
         );

      alwaysRejectTheseMutations( myMutationFilter );      
      tryGermlineMutations( myMutationFilter );

      // somatic mutation on first whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            true, 
            4L, 
            "Unknown",        // validationStatus,
            "somatic",        // mutationStatus,
            "Unknown"         // mutationType
         );

      // somatic mutation on 2nd whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            true, 
            12L, 
            "Unknown",        // validationStatus,
            "somatic",        // mutationStatus,
            "Unknown"         // mutationType
         );

      // somatic mutation on both whitelists
      nowTestAcceptMutation( 
            myMutationFilter,
            true, 
            4L, 
            "Unknown",        // validationStatus,
            "somatic",        // mutationStatus,
            "Unknown"         // mutationType
         );

      // somatic mutation on neither whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            false, 
            1234L, 
            "Unknown",        // validationStatus,
            "somatic",        // mutationStatus,
            "Unknown"         // mutationType
         );
   
      // Unknown mutation on both whitelists
      nowTestAcceptMutation( 
            myMutationFilter,
            true, 
            4L, 
            "Unknown",        // validationStatus,
            "Unknown",        // mutationStatus,
            "Unknown"         // mutationType
         );

      // Unknown mutation on neither whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            false, 
            1234L, 
            "Unknown",        // validationStatus,
            "Unknown",        // mutationStatus,
            "Unknown"         // mutationType
         );
      
      Assert.assertEquals(9, myMutationFilter.getRejects() );
      Assert.assertEquals(5, myMutationFilter.getAccepts() );
      Assert.assertEquals(1, myMutationFilter.getGermlineWhitelistAccepts() );
      Assert.assertEquals(3, myMutationFilter.getSomaticWhitelistAccepts() );
      Assert.assertEquals(1, myMutationFilter.getUnknownAccepts() );
   
   }

   private void nowTestAcceptMutation( 
            MutationFilter myMutationFilter,
            boolean expectedResult, 
            long entrezGeneId, 
            String validationStatus, 
            String mutationStatus,
            String mutationType
            ){
      ExtendedMutation anExtendedMutation = new ExtendedMutation(
         entrezGeneId,           // entrezGeneId,
         validationStatus,       // validationStatus,
         mutationStatus,         // mutationStatus,
         mutationType            // mutationType
      );
      if( expectedResult ){
         assertTrue( myMutationFilter.acceptMutation( anExtendedMutation ) );         
      }else{
         assertFalse( myMutationFilter.acceptMutation( anExtendedMutation ) );         
      }
   }
   
   private void alwaysRejectTheseMutations(MutationFilter myMutationFilter){

      // System.out.println( myMutationFilter.toString() );
      // always reject an empty mutation
      ExtendedMutation anEmptyExtendedMutation = new ExtendedMutation();
      assertFalse( myMutationFilter.acceptMutation( anEmptyExtendedMutation ) );
      
      // REJECT: Silent, LOH, Intron and Wildtype mutations
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "Unknown",
               "Silent"
            );
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "Unknown",
               "Intron"
            );
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "LOH",
               "Unknown"
            );
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "Wildtype",
               "Unknown"
            );
      
   }
   
   private void loadGene( String GeneSymbol, long GeneID ) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene( GeneID, GeneSymbol ));
    }
}