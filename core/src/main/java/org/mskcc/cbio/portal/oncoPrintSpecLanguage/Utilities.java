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

package org.mskcc.cbio.portal.oncoPrintSpecLanguage;

import java.util.ArrayList;

/**
 * use an ArrayList as a temp buffer; ugly, but works
 * @author Arthur Goldberg
 */
public class Utilities {
   // static is OK, as a new parser is instantiated for each user / input
   // TODO: HIGH: UNIT TEST in OncoSpec
   // TODO: find another way to save syntax errors!

   /*
   static ArrayList<String> errorMessages  = new ArrayList<String>();

   static public void clearErrorMessageList() {
      errorMessages.clear();
   }

   public static ArrayList<String> getErrorMessages() {
      return errorMessages;
   }
    */

   /**
    * So that users do not need to complete fullDataTypeSpecs with a semicolon, append a ';' at the end of each line that contains a ':' that isn't
    * followed by a ';'.  assume a new line at the end of the input string.
    * <p>
    * @param input
    * @return the new string
    */
   public static String appendSemis( String input ){
      // TODO: use system new line, as provided by  String NL = System.getProperty("line.separator");

      // append \n to final line.
      String withNL = input+"\n";
      // # add missing ;s at end of line where needed, 
      String appendedSemis = withNL.replaceAll( ":([^;\n]*)\n", ":$1;\n");
      // then remove added \n
      return appendedSemis.substring(0, appendedSemis.length() - 1 );
   }


}