package org.mskcc.cgds.test.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.mskcc.cgds.scripts.ComputeZScoreUnit;

public class TestComputeZScoreUnit extends TestCase {
   
   public void testComputeZScoreUnit(){
      
      String Args[] = { "testData/test_all_thresholded.by_genes.txt", "testData/test_PR_GDAC_CANCER.medianexp.txt",
               "testData/data_mRNA_ZbyNorm.txt" };
      try {
         ComputeZScoreUnit.main(Args);
         // compare with correct
         String line;
         Process p = Runtime.getRuntime().exec("diff "+ "testData/correct_data_mRNA_ZbyNorm.txt " + Args[2] );
         BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()) );
         while ((line = input.readLine()) != null) {
            assertEquals ( "", line );
         }
         input.close();
         
      } catch (Exception e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      assertTrue( ComputeZScoreUnit.isNormal("TCGA-A7-A0CG-11A-11D-A011-01") );
      assertFalse( ComputeZScoreUnit.isNormal("TCGA-A7-A0CG-01A-11D-A011-01") );

   }
}
