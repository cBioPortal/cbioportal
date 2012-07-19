package org.mskcc.cgds.model;

// imports
import java.util.ArrayList;

/**
 * Class to store information of case list.
 */
public class CaseList {
   private String            stableId;
   private int               caseListId;
   private int               cancerStudyId;
   private String            name;
   private String            description;
   private CaseListCategory  caseListCategory;
   private ArrayList<String> caseList;

   public CaseList() {
      super();
   }

   /**
    * A constructor for all the NON NULL fields in a case_list
    * @param stableId
    * @param caseListId
    * @param cancerStudyId
    * @param name
    */
   public CaseList(String stableId, int caseListId, int cancerStudyId, String name,
                   CaseListCategory caseListCategory) {
      super();
      this.stableId = stableId;
      this.caseListId = caseListId;
      this.cancerStudyId = cancerStudyId;
      this.name = name;
      this.caseListCategory = caseListCategory;
   }

   public String getStableId() {
      return stableId;
   }

   public void setStableId(String stableId) {
      this.stableId = stableId;
   }

   public int getCaseListId() {
      return caseListId;
   }

   public void setCaseListId(int caseListId) {
      this.caseListId = caseListId;
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

   public CaseListCategory getCaseListCategory() {
       return caseListCategory;
   }

   public void setCaseListCategory(CaseListCategory caseListCategory) {
       this.caseListCategory = caseListCategory;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public ArrayList<String> getCaseList() {
      return caseList;
   }

   public void setCaseList(ArrayList<String> caseList) {
      this.caseList = caseList;
   }

    /**
     * Gets list of all case IDs in the set as one string.
     *
     * @return space-delimited list of case IDs.
     */
    public String getCaseListAsString() {
        StringBuilder str = new StringBuilder();
        for (String caseId : caseList) {
            str.append(caseId).append(" ");
        }
        return str.toString();
    }
}
