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