package org.mskcc.cbio.portal.test.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.tool.RenderImageDataType;
import org.mskcc.portal.util.ProfileMerger;
import org.mskcc.portal.util.WebFileConnect;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.model.GeneticAlterationType;

public class TestRenderImageDataType extends TestCase {
   
   public void testRender() throws IOException {
      
      ArrayList<ProfileData> profileList = new ArrayList<ProfileData>();

      GeneticProfile profile0 =
              new GeneticProfile("ovarian_cna", 1,
              GeneticAlterationType.COPY_NUMBER_ALTERATION,
                      "Ovarian CNA", "Ovarian CNA", true);
      String matrix0[][] = WebFileConnect.retrieveMatrix(new File("test_data/cna_sample.txt"));
      ProfileData data0 = new ProfileData(profile0, matrix0);
      profileList.add(data0);

      GeneticProfile profile1 = new
              GeneticProfile
              ("ovarian_protein", 1, GeneticAlterationType.PROTEIN_LEVEL,
              "Ovarian Protein", "Ovarian Protein", true);
      String matrix1[][] = WebFileConnect.retrieveMatrix(
               new File("test_data/protein_sample.txt"));
      ProfileData data1 = new ProfileData(profile1, matrix1);
      profileList.add(data1);

      ProfileMerger merger = new ProfileMerger(profileList);
      ProfileData mergedProfile = merger.getMergedProfile();

      assertEquals(null, RenderImageDataType.render( "PHOSPHORYLATION", mergedProfile ));
      
      BufferedReader in = new BufferedReader(new FileReader( new File("test_data/protein_sample_table.html") ));
      
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
