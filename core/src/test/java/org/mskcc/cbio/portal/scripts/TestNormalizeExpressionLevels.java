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

import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.util.GlobalProperties;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

public class TestNormalizeExpressionLevels extends TestCase {

    private static String validationFile = initializeValidationFile();
    private static String initializeValidationFile()
    {
        String home = System.getenv(GlobalProperties.HOME_DIR);
        return (home != null) ? 
            home + File.separator + "core/target/test-classes/correct_data_mRNA_ZbyNorm.txt" : null;
    }
    private static String[] args = initializeNormalizeArgsArray();
    private static String[] initializeNormalizeArgsArray()
    {
        String[] args = null;

        String home = System.getenv(GlobalProperties.HOME_DIR);
        if (home != null) {
            args = new String [] { home + File.separator + "core/target/test-classes/test_all_thresholded.by_genes.txt",
                                   home + File.separator + "core/target/test-classes/test_PR_GDAC_CANCER.medianexp.txt",
                                   home + File.separator + "core/target/test-classes/data_mRNA_ZbyNorm.txt",
                                   NormalizeExpressionLevels.TCGA_NORMAL_SUFFIX, "4" };
        }

        return args;
    }
    
   
	// TBD: change this to use getResourceAsStream()

   public void testNormalizeExpressionLevels(){
      
      try {

          DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
          daoGene.addGene(new CanonicalGene(65985, "AACS"));
          daoGene.addGene(new CanonicalGene(63916, "ELMO2"));
          daoGene.addGene(new CanonicalGene(9240, "PNMA1"));
          daoGene.addGene(new CanonicalGene(6205, "RPS11"));
          daoGene.addGene(new CanonicalGene(7157, "TP53"));
          daoGene.addGene(new CanonicalGene(367, "AR"));
         
         NormalizeExpressionLevels.driver(args);
         // compare with correct
         String line;
         Process p = Runtime.getRuntime().exec("diff" + " "  + validationFile + " " + args[2] );
         BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()) );
         while ((line = input.readLine()) != null) {
            assertEquals ( "", line );
         }
         input.close();
         
      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      assertTrue( NormalizeExpressionLevels.isNormal("TCGA-A7-A0CG-11A-11D-A011-01") );
      assertFalse( NormalizeExpressionLevels.isNormal("TCGA-A7-A0CG-01A-11D-A011-01") );
   }
   
   public void testJoin(){
      ArrayList<String> l = new ArrayList<String>();
      l.add("out");
      l.add("of");
      l.add("order");
      assertEquals ( "out-of-order", NormalizeExpressionLevels.join( l, "-" ) );
   }
}
