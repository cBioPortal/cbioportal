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

package org.mskcc.cbio.portal.model;

/**
 * A TypeOfCancer is a clinical cancer type, such as Glioblastoma, Ovarian, etc.
 * Eventually, we'll have ontology problems with this, but initially the dbms will
 * be loaded from a file with a static table of types.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Arman Aksoy
 */
public class TypeOfCancer {

   private String name;
   private String typeOfCancerId;
   private String clinicalTrialKeywords = ""; // Separated by commas
   private String dedicatedColor = "white";
   private String shortName = "";

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

   public String getClinicalTrialKeywords() {
      return clinicalTrialKeywords;
   }

   public void setClinicalTrialKeywords(String clinicalTrialKeywords) {
      this.clinicalTrialKeywords = clinicalTrialKeywords;
   }

    public String getDedicatedColor() {
        return dedicatedColor;
    }

    public void setDedicatedColor(String dedicatedColor) {
        this.dedicatedColor = dedicatedColor;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
