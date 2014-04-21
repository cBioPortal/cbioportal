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

package org.mskcc.cbio.portal.util;

import java.util.ArrayList;

import org.mskcc.cbio.portal.model.GeneticEvent;
import org.mskcc.cbio.portal.model.GeneticEventImpl;
import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.model.ProfileDataSummary;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification;

/**
 * Utility Class to Convert Profile Data to a Matrix of Genetic Events.
 * 
 * @author Ethan Cerami
 */
public class ConvertProfileDataToGeneticEvents {
   // TODO: move this method into ProfileDataSummary

   /**
    * Converts a Profile Data Summary Object into a 2D Matrix of Genetic Event Objects.
    * 
    * @param pSummaryData
    *           Profile Summary Data.
    * @param zScoreThreshold
    *           Z Score Threshold.
    * @param geneList
    *           list of rows of genes to return in the matrix
    * @return 2D Matrix of GeneticEvent Objects.
    */
   public static GeneticEvent[][] convert(ProfileDataSummary pSummaryData, String[] geneList, 
            OncoPrintSpecification theOncoPrintSpecification, double zScoreThreshold, double rppaScoreThreshold) {
      ProfileData pData = pSummaryData.getProfileData();
      
      ArrayList <String> goodGenes = new ArrayList <String>();
      for( String gene : geneList ){
         // TODO: replace with a hash lookup, as pData.getValidGeneList().contains is O(n), so this for-loop is O(n**2)
         if (pData.getGeneList().contains( gene )) {
            goodGenes.add(gene);
         }
      }

      GeneticEvent matrix[][] = new GeneticEvent[goodGenes.size()][pData.getCaseIdList().size()];
      int row = 0;
      for( String currentGene : goodGenes ){

         for (int j = 0; j < pData.getCaseIdList().size(); j++) {
            String currentCaseId = pData.getCaseIdList().get(j);
            String value = pData.getValue(currentGene, currentCaseId);
            ValueParser valueParser = ValueParser.generateValueParser( currentGene, value, zScoreThreshold,
                    rppaScoreThreshold, theOncoPrintSpecification );
            if( null == valueParser){
               System.err.println( "Yikes null valueParser");
            }else{
               GeneticEventImpl event = new GeneticEventImpl(valueParser, currentGene, currentCaseId);
               matrix[row][j] = event;
            }
         }
         row++;
      }
      return matrix;
   }
}
