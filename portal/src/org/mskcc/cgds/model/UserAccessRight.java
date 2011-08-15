package org.mskcc.cgds.model;

import org.mskcc.cgds.util.EqualsUtil;

/**
 * User rights to access a cancer study. An instance means that the user
 * identified by email has the right to access the cancer study identified by
 * cancerStudyId, which refers to CancerStudy.studyId.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class UserAccessRight{

   private String email;
   private int cancerStudyId;
   
   public UserAccessRight(String email, int cancerStudyId) {
      super();
      this.email = email;
      this.cancerStudyId = cancerStudyId;
   }

   public int getCancerStudyId() {
      return cancerStudyId;
   }

   public void setCancerStudyId(int cancerStudyId) {
      this.cancerStudyId = cancerStudyId;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   @Override
   public boolean equals( Object otherUserAccessRight) {
       if( this == otherUserAccessRight ) return true;
       if ( !(otherUserAccessRight instanceof UserAccessRight) ) return false;
       UserAccessRight that = (UserAccessRight) otherUserAccessRight;
       return
           EqualsUtil.areEqual(this.email, that.email) &&
           this.cancerStudyId == that.cancerStudyId;
   }
}