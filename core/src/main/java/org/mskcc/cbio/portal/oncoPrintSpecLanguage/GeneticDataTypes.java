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