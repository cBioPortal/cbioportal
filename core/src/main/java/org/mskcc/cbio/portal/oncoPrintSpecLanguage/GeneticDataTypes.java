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

import org.mskcc.cbio.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;

public enum GeneticDataTypes {
   CopyNumberAlteration(DataTypeCategory.Discrete, "CNA"), 
   Expression(DataTypeCategory.Continuous, "MRNA microRNA"), 
   Mutation( DataTypeCategory.Discrete, ""), 
   Methylation(DataTypeCategory.Continuous, ""),
   RPPA(DataTypeCategory.Continuous, "RPPA, prot");

   private final DataTypeCategory theDataTypeCategory;
   private String[] nicknames = null;

   /**
    * @param theDataTypeCategory
    * @param nicknames nicknames are alternative names that can be used to lookup GeneticDataTypes 
    */
   private GeneticDataTypes(DataTypeCategory theDataTypeCategory, String nicknames) {
       this.theDataTypeCategory = theDataTypeCategory;
       if( !nicknames.equals("")){
           this.nicknames = nicknames.split(" ");
       }else{
           this.nicknames = new String[0];
       }
   }

   public DataTypeCategory getTheDataTypeCategory() {
       return theDataTypeCategory;
   }

   public String[] getNicknames() {
       return nicknames;
   }

};