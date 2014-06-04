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

import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticDataTypes;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintGeneDisplaySpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ResultDataTypeSpec;

/**
 * 
 * @author Arthur Goldberg
 * 
 */
public class HeatMapLegend {

   static ArrayList<String>            CNAlevels;
   static ArrayList<String>            MutationLevels;
   static ArrayList<String>            ExpressionLevels;
   static ArrayList<String>            RPPALevels;
   static ArrayList<String>            Alterations;
   
   static ArrayList<ArrayList<String>> allColumns;
   static StringBuffer sb;
   
   /**
    * Output the Heat Map html legend, listing only genetic alterations that are
    * selected by the OncoSpec.
    * <p>
    * TODO: unit test
    * @param allPossibleAlterations
    * @return
    */
   static public String outputHeatMapLegend( OncoPrintGeneDisplaySpec allPossibleAlterations) {
      /*
       * Three columns: CNA, Mutation, mRNA regulation. Output column if any of
       * it is selected; within a column, output all possible alternatives.
       */
      CNAlevels = new ArrayList<String>();
      MutationLevels = new ArrayList<String>();
      ExpressionLevels = new ArrayList<String>();
      RPPALevels = new ArrayList<String>();
      Alterations = new ArrayList<String>();
      
      // CNA colors
      if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Amplified)) {
         CNAlevels.add("<td class=amp_2>High-Level Amplification</td>");
      }

      if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HomozygouslyDeleted)) {
         CNAlevels.add("<td class=del_2>Homozygous Deletion</td>");
      }

      if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.Gained)) {
         CNAlevels.add("<td class=gained_2>Low-level Gain</td>");
      }

      if (allPossibleAlterations.satisfy(GeneticDataTypes.CopyNumberAlteration, GeneticTypeLevel.HemizygouslyDeleted)) {
         CNAlevels.add("<td class=hetloss_2>Heterozygous Deletion</td>");
      }

      // mutations
      if (allPossibleAlterations.satisfy(GeneticDataTypes.Mutation, GeneticTypeLevel.Mutated)) {
         MutationLevels.add("<td valign=top><img src=\"images/bullseye.png\"> = Point Mutation</td>");
      }

      // expression
      // over
      ResultDataTypeSpec theResultDataTypeSpec = allPossibleAlterations
               .getResultDataTypeSpec(GeneticDataTypes.Expression);
      if (null != theResultDataTypeSpec && (null != theResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec())) {
         ExpressionLevels.add("<td valign=top><img src=\"images/up2.png\"> = mRNA up-Regulation</td>");
      }

      // under
      theResultDataTypeSpec = allPossibleAlterations.getResultDataTypeSpec(GeneticDataTypes.Expression);
      if (null != theResultDataTypeSpec && (null != theResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec())) {
         ExpressionLevels.add("<td valign=top><img src=\"images/down2.png\"> = mRNA down-Regulation</td>");
      }

      // RPPA
      theResultDataTypeSpec = allPossibleAlterations
               .getResultDataTypeSpec(GeneticDataTypes.RPPA);
      if (null != theResultDataTypeSpec && (null != theResultDataTypeSpec.getCombinedGreaterContinuousDataTypeSpec())) {
         RPPALevels.add("<td valign=top><img src=\"images/up-rppa.png\"> = Protein up-Regulation</td>");
      }

      // under
      theResultDataTypeSpec = allPossibleAlterations.getResultDataTypeSpec(GeneticDataTypes.Expression);
      if (null != theResultDataTypeSpec && (null != theResultDataTypeSpec.getCombinedLesserContinuousDataTypeSpec())) {
         RPPALevels.add("<td valign=top><img src=\"images/down-rppa.png\"> = Protein down-Regulation</td>");
      }

      // column headers
      if (0 < CNAlevels.size()) {
         CNAlevels.add(0, "<th>Putative Copy Number Alteration</th>");
      }
      if (0 < MutationLevels.size()) {
         MutationLevels.add(0, "<th>Point Mutation</th>");
      }
      if (0 < ExpressionLevels.size()) {
         ExpressionLevels.add(0, "<th>Gene Expression</th>");
      }
      if (0 < RPPALevels.size()) {
         RPPALevels.add(0, "<th>Protein Level</th>");
      }
      
      // Alterations
      Alterations.add("<th>Gene Set</th>");
      Alterations.add("<td valign=top><img src=\"images/altered.gif\"> = At least one gene in set is altered in case</td>");

      allColumns = new ArrayList<ArrayList<String>>();
      allColumns.add(CNAlevels);
      allColumns.add(MutationLevels);
      allColumns.add(ExpressionLevels);
      allColumns.add(RPPALevels);
      allColumns.add(Alterations);
            
      sb = new StringBuffer();
      appendLine( sb, "<table>");
      
      boolean[] columnUsed = outputHeadings(sb, allColumns);
      while (levelsRemaining(allColumns)) {
         outputNextRow(sb, allColumns, columnUsed);
      }
      
      appendLine( sb, "</table>");
      return sb.toString();
   }

   static private boolean[] outputHeadings( StringBuffer sb, ArrayList<ArrayList<String>> allColumns) {
      boolean[] rv = new boolean[allColumns.size()];
      int i = 0;
      appendLine( sb, "<TR>");
      for (ArrayList<String> col : allColumns) {
         if (0 < col.size()) {
            rv[i] = true;
            appendLine( sb, col.remove(0));
         }
         i++;
      }
      appendLine( sb, "</TR>");
      return rv;
   }

   static private boolean levelsRemaining(ArrayList<ArrayList<String>> allColumns) {
      for (ArrayList<String> col : allColumns) {
         if (0 < col.size()) {
            return true;
         }
      }
      return false;
   }

   static private void outputNextRow( StringBuffer sb, ArrayList<ArrayList<String>> allColumns, boolean[] columnUsed) {
      int i = 0;
      appendLine( sb, "<TR>");
      for (ArrayList<String> col : allColumns) {
         if (0 < col.size()) {
            appendLine( sb, col.remove(0));
         } else {
            if (columnUsed[i]) {
               appendLine( sb, "<TD></TD>");
            }
         }
         i++;
      }
      appendLine( sb, "</TR>");
   }
   
   static private void appendLine( StringBuffer sb, String s ){
      sb.append(s).append("\n");
   }

}