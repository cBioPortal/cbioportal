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

package org.mskcc.cbio.portal.model;

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

    @Override
    public String toString() {
        return this.getClass().getName() + "{"
                + "stableId " + this.stableId
                + ", caseListId " + this.caseListId
                + ", caseLIstId " + this.cancerStudyId
                + ", name " + this.name
                + ", description " + this.description
                + ", CaseListCategory " + this.caseListCategory
                + ", caseList " + this.caseList
                + "}";
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
