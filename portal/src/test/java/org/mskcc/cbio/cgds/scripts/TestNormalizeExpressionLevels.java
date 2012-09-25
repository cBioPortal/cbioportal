/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.scripts;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;

import org.mskcc.cbio.cgds.scripts.NormalizeExpressionLevels;

public class TestNormalizeExpressionLevels extends TestCase {
   
	// TBD: change this to use getResourceAsStream()
	String Args[] = { "target/test-classes/test_all_thresholded.by_genes.txt",
					  "target/test-classes/test_PR_GDAC_CANCER.medianexp.txt",
					  "target/test-classes/data_mRNA_ZbyNorm.txt", "4" };
   public void testNormalizeExpressionLevels(){
      
      try {
         
         NormalizeExpressionLevels.main(Args);
         // compare with correct
         String line;
         Process p = Runtime.getRuntime().exec("diff "+ "target/test-classes/correct_data_mRNA_ZbyNorm.txt " + Args[2] );
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
   
   public void testReadCopyNumberFile(){
      
      HashMap<String,ArrayList<String[]>> map = 
            NormalizeExpressionLevels.readCopyNumberFile(Args[0]);
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
      assertEquals ( "out-of-order", NormalizeExpressionLevels.join( l, "-" ) );
   }
}
