package org.mskcc.cgds.test.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

import org.mskcc.cgds.scripts.ComputeZScoreUnit;

public class TestComputeZScoreUnit extends TestCase {
   
   String Args[] = { "test_data/test_all_thresholded.by_genes.txt", "test_data/test_PR_GDAC_CANCER.medianexp.txt",
   "test_data/data_mRNA_ZbyNorm.txt", "4" };
   public void testComputeZScoreUnit(){
      
      try {
         
         ComputeZScoreUnit.main(Args);
         // compare with correct
         String line;
         Process p = Runtime.getRuntime().exec("diff "+ "test_data/correct_data_mRNA_ZbyNorm.txt " + Args[2] );
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
   
   public void testReadCopyNumberFile(){
      
      HashMap<String,ArrayList<String[]>> map = 
            ComputeZScoreUnit.readCopyNumberFile(Args[0]);
      String SampleAndValue[] = map.get("A2M").get(0);
      assertTrue( SampleAndValue[1].equals("1") );
      SampleAndValue = map.get("ELMO2").get(0);
      assertTrue( SampleAndValue[1].equals("-1") );

   }
   
   public void testJoin(){
      ArrayList<String> l = new ArrayList<String>();
      l.add("out");
      l.add("of");
      l.add("order");
      assertEquals ( "out-of-order", ComputeZScoreUnit.join( l, "-" ) );
   }
}
