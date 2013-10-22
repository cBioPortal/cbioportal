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

package org.mskcc.cbio.portal.tool;

import java.util.HashSet;

import org.mskcc.cbio.portal.model.ProfileData;
import org.mskcc.cbio.portal.util.ValueParser;
import org.mskcc.cbio.portal.model.GeneticAlterationType;

/**
 * Render an "image" datatype, which includes a value and a link to a list of images.
 * Render into a table.
 * <p>
 * An image value is stored in the dbms in `cgds`.`genetic_alteration`.`value` in the RE form VALUE~images, where
 * 'images' means that images are available.
 *  
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class RenderImageDataType{
   
   public static final String FIELD_SEPARATOR = "~"; 
   
   // public static final String StudyTrackerPrefix = "https://devtracker.mskcc.org/devtracker/TCGALookup.do?studyId=11&key=";
   // public static final String ImageIndicator = "images";

   public static String render( String imageDatatype, ProfileData mergedProfile ){
      
      GeneticAlterationType theGeneticAlterationType =
              GeneticAlterationType.getType(imageDatatype);
      if( null != theGeneticAlterationType ){
         StringBuffer sb = new StringBuffer();

         /*
          * Data is sparse -- only output actual results
          * 
          * I. Get genes to display
          * for each gene in the mergedProfile{
          *    for each case in the mergedProfile's case list{
          *       if( data is available ){
          *          Add the gene to list of genes with data
          *       }
          *    }
          * }
          * if( number of genes with data == 0 ){
          *    return null
          * }
          * 
          * II. write header
          * write data description
          * 
          * III. write data
          * for each gene in the mergedProfile{
          *    if( gene does not have data ){
          *       continue
          *    }
          *    write gene
          *    write table:
          *    for each case in the mergedProfile's case list{
          *       if( data is available ){
          *          write row with case, measurement, and, if available, link to images
          *          link has form: https://devtracker.mskcc.org/devtracker/TCGALookup.do?studyId=11&&key=<caseID>
          *       }
          *    }
          * }
          * 
          * IV. write footer
          * write "login required" 
          */
         
         // I. Get genes to display
         HashSet<String> genesWithData = new HashSet<String>();
         String value;
         for( String caseID : mergedProfile.getCaseIdList() ){
            for( String geneID : mergedProfile.getGeneList()){

               value = getValue( caseID, geneID, imageDatatype, mergedProfile );
                  
               if( null == value ){
                  continue;
               }
               if( value.equals(GeneticAlterationType.NAN )){
                  continue;
               }
               genesWithData.add(geneID);
            }
         }
         if( 0 == genesWithData.size() ){
            return null;
         }
         
         // III. write data
         for( String geneID : mergedProfile.getGeneList()){
            if( genesWithData.contains(geneID) ){
               sb.append( "<P><H5>" + geneID + "</H5></P>\n");
               
               sb.append("<TABLE cellspacing='0px'>\n");
               sb.append("<TR>\n");
               sb.append("\t<TH>Cases</TH>\n");
               sb.append("\t<TH>Measurement</TH>\n");
               sb.append("\t<TH>Image(s) *</TH>\n");
               sb.append("</TR>\n");
               
               boolean alternate = true;

               for( String caseID : mergedProfile.getCaseIdList() ){
                  value = getValue( caseID, geneID, imageDatatype, mergedProfile );
                  
                  if( null == value ){
                     continue;
                  }
                  if( value.equals(GeneticAlterationType.NAN )){
                     continue;
                  }
                  
                  String bgcolor = "";
                  String bgheadercolor = "#B9B9FC"; 
                  
                  if( alternate ){
                     bgcolor = "#eeeeee";
                     bgheadercolor = "#dddddd";
                  }
                  alternate = !alternate;
                  
                  //set background color of row
                  sb.append("<tr bgcolor='" + bgcolor + "'>\n"); 

                  //print row header -- case id
                  sb.append("\t<td class='last_mut' style=\"border-bottom:1px solid #AEAEFF; background:"+bgheadercolor+ ";\">");
                  sb.append(caseID);
                  sb.append("</td>\n"); 
                  
                  // parse value
                  String[] fields = value.split( FIELD_SEPARATOR, 2 );
                  String measuredValue = fields[0];
                  String imagesURL = null;
                  if( 1 < fields.length ){
                     imagesURL = fields[1];
                  }
                  
                  sb.append("\t<TD>");
                  if( null != measuredValue ){
                     sb.append(measuredValue);
                  }
                  sb.append("</TD>\n");

                  sb.append("\t<TD>");
                  if( null != imagesURL ){
                     // link has form: https://devtracker.mskcc.org/devtracker/TCGALookup.do?studyId=11&key=<caseID>
                     // <A HREF="url">Image(s)</A>
                     sb.append( "<A HREF=\"" + imagesURL + "\">Image(s)</A><BR>" );
                  }
                  sb.append("</TD>\n");
                  sb.append("</TR>\n");
               }
               sb.append("</TABLE>\n");
               
            }
         }

         // IV. write footer
         sb.append("<p>* Login required.\n");

         // out.println ("<li class=\"ui-state-default ui-corner-top\"><a href='#" + imageDatatype + "'>Text name for " + imageDatatype + "</a></li>");
         return sb.toString();
      }
      return null;
   }
   
   private static String getValue( String caseID, String geneID, String imageType, ProfileData mergedProfile ){
      ValueParser theValueParser = mergedProfile.getValueParsed(geneID, caseID, 0);
      if( null == theValueParser ){
         return null;
      }
      String value = null;
      if( imageType.equals(GeneticAlterationType.PROTEIN_LEVEL.toString() ) ){
         value = theValueParser.getUnparsedProteinLevelValue();
      }
      if( imageType.equals(GeneticAlterationType.PHOSPHORYLATION.toString() ) ){
         value = theValueParser.getUnparsedPhosphorylationValue();
      }
      return value;
   }

}