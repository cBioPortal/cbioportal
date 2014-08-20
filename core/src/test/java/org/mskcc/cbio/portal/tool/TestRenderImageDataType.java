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

package org.mskcc.cbio.portal.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.util.ProfileMerger;
import org.mskcc.cbio.io.WebFileConnect;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.GeneticAlterationType;

public class TestRenderImageDataType extends TestCase {
   
   public void testRender() throws IOException {
      
      ArrayList<ProfileData> profileList = new ArrayList<ProfileData>();

      GeneticProfile profile0 =
              new GeneticProfile("ovarian_cna", 1,
								 GeneticAlterationType.COPY_NUMBER_ALTERATION, "DISCRETE",
								 "Ovarian CNA", "Ovarian CNA", true);
	  // TBD: change this to use getResourceAsStream()
      String matrix0[][] = WebFileConnect.retrieveMatrix(new File("target/test-classes/cna_sample.txt"));
      ProfileData data0 = new ProfileData(profile0, matrix0);
      profileList.add(data0);

      GeneticProfile profile1 =
		  new GeneticProfile("ovarian_protein", 1, GeneticAlterationType.PROTEIN_LEVEL, "Z-SCORE",
							 "Ovarian Protein", "Ovarian Protein", true);
	  // TBD: change this to use getResourceAsStream()
      String matrix1[][] = WebFileConnect.retrieveMatrix(new File("target/test-classes/protein_sample.txt"));
      ProfileData data1 = new ProfileData(profile1, matrix1);
      profileList.add(data1);

      ProfileMerger merger = new ProfileMerger(profileList);
      ProfileData mergedProfile = merger.getMergedProfile();

      assertEquals(null, RenderImageDataType.render( "PHOSPHORYLATION", mergedProfile ));
      
	  // TBD: change this to use getResourceAsStream()
      BufferedReader in = new BufferedReader(new FileReader( new File("target/test-classes/protein_sample_table.html") ));
      
      String generatedTable = RenderImageDataType.render( "PROTEIN_LEVEL", mergedProfile );
      String generatedTableRows[] = generatedTable.split( "\n" ); 
//System.out.println( "generated table is\n" + generatedTable );
      String str;
      int i=0;
      while ((str = in.readLine()) != null) {
         assertEquals( str, generatedTableRows[i++] );
      }
      in.close();
   }
}
