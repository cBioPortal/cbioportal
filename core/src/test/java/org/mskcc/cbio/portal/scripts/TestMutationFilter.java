/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.portal.scripts;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.portal.scripts.MutationFilter;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

/**
 * JUnit tests for MutationFilter class.
 */
public class TestMutationFilter extends TestCase {
   
   protected void setUp(){
      try {
         ResetDatabase.resetDatabase();
      } catch (DaoException e) {
         e.printStackTrace();
      }
   }
   
   public void testBadWhiteLists( ){
      try {
         new MutationFilter("no_such_file");
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

   public void testAcceptMutationGermlineWhiteList() throws DaoException {
      
      // load genes
      loadGene( "FOO", 3L  );
      loadGene( "BAR", 234L  );
      loadGene( "BIG", 234234L  );
                 
      // create MutationFilter
	  // TBD: change this to use getResourceAsStream()
      MutationFilter myMutationFilter = new MutationFilter("target/test-classes/test_germline_white_list_file.txt");

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

    private void nowTestAcceptMutation(
            MutationFilter myMutationFilter,
            boolean expectedResult,
            long entrezGeneId,
            String validationStatus,
            String mutationStatus,
            String mutationType
    ) {
        CanonicalGene gene = new CanonicalGene(entrezGeneId, "XXX");
        ExtendedMutation anExtendedMutation = new ExtendedMutation(
                gene,                   // gene,
                validationStatus,       // validationStatus,
                mutationStatus,         // mutationStatus,
                mutationType            // mutationType
        );
        if (expectedResult) {
            assertTrue(myMutationFilter.acceptMutation(anExtendedMutation));
        } else {
            assertFalse(myMutationFilter.acceptMutation(anExtendedMutation));
        }
    }
   
   private void alwaysRejectTheseMutations(MutationFilter myMutationFilter){

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
   
   private void loadGene( String geneSymbol, long geneID ) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene( geneID, geneSymbol ));
    }
}