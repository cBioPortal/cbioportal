package org.mskcc.cbio.cgds.model;

/**
 * A TypeOfCancer is a clinical cancer type, such as Glioblastoma, Ovarian, etc.
 * Eventually, we'll have ontology problems with this, but initially the dbms will
 * be loaded from a file with a static table of types.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class TypeOfCancer {

   private String name;
   private String typeOfCancerId;

   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getTypeOfCancerId() {
      return typeOfCancerId;
   }
   public void setTypeOfCancerId(String typeOfCancerId) {
      this.typeOfCancerId = typeOfCancerId;
   }

}
