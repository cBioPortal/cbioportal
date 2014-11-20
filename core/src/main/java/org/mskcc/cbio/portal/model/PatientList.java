/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

// imports
import java.util.ArrayList;

/**
 * Class to store information of patient list.
 */
public class PatientList {
   private String            stableId;
   private int               patientListId;
   private int               cancerStudyId;
   private String            name;
   private String            description;
   private PatientListCategory  patientListCategory;
   private ArrayList<String> patientList;

   public PatientList() {
      super();
   }

   /**
    * A constructor for all the NON NULL fields in a patient_list
    * @param stableId
    * @param patientListId
    * @param cancerStudyId
    * @param name
    */
   public PatientList(String stableId, int patientListId, int cancerStudyId, String name,
                      PatientListCategory patientListCategory) {
      super();
      this.stableId = stableId;
      this.patientListId = patientListId;
      this.cancerStudyId = cancerStudyId;
      this.name = name;
      this.patientListCategory = patientListCategory;
   }

   public String getStableId() {
      return stableId;
   }

   public void setStableId(String stableId) {
      this.stableId = stableId;
   }

   public int getPatientListId() {
      return patientListId;
   }

   public void setPatientListId(int patientListId) {
      this.patientListId = patientListId;
   }

   public int getCancerStudyId() {
      return cancerStudyId;
   }

   public void setCancerStudyId( int cancerStudyId) {
      this.cancerStudyId = cancerStudyId;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public PatientListCategory getPatientListCategory() {
       return patientListCategory;
   }

   public void setPatientListCategory(PatientListCategory patientListCategory) {
       this.patientListCategory = patientListCategory;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public ArrayList<String> getPatientList() {
      return patientList;
   }

   public void setPatientList(ArrayList<String> patientList) {
      this.patientList = patientList;
   }

    @Override
    public String toString() {
        return this.getClass().getName() + "{"
                + "stableId " + this.stableId
                + ", patientListId " + this.patientListId
                + ", patientListId " + this.cancerStudyId
                + ", name " + this.name
                + ", description " + this.description
                + ", PatientListCategory " + this.patientListCategory
                + ", patientList " + this.patientList
                + "}";
    }


    /**
     * Gets list of all patient IDs in the set as one string.
     *
     * @return space-delimited list of patient IDs.
     */
    public String getPatientListAsString() {
        StringBuilder str = new StringBuilder();
        for (String patientId : patientList) {
            str.append(patientId).append(" ");
        }
        return str.toString();
    }
}
