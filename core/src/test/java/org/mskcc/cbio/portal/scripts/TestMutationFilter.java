/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * JUnit tests for MutationFilter class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestMutationFilter {
	
	@Before
	public void setUp() throws DaoException {
	      // load genes
	      loadGene( "FOO", 3L  );
	      loadGene( "BAR", 234L  );
	      loadGene( "BIG", 234234L  );
	}
   
   
   @Test
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
            assertTrue(myMutationFilter.acceptMutation(anExtendedMutation, null));
        } else {
            assertFalse(myMutationFilter.acceptMutation(anExtendedMutation, null));
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
