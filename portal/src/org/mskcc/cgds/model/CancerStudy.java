package org.mskcc.cgds.model;

import org.mskcc.cgds.util.EqualsUtil;

/**
 * 
 * This represents a cancer study, with a set of cases and some data sets.
 * Remember: A CancerStudy that hasn't been loaded into or read from the dbms will not have a studyID!
 * 
 * Was CancerType, before July 2011.
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class CancerStudy {

   public static final int NO_SUCH_STUDY = -1;

   private int studyID; // assigned by dbms auto increment
   private String name;
   private String description;
   private String cancerStudyIdentifier;  // for backward compatibility with cancer_type_id; may be null
   private String typeOfCancerId;  // required
   private boolean publicStudy;  // if true, a public study, otherwise private
   
   public CancerStudy( String name, String description, String typeOfCancerId, boolean publicStudy) {
      super();
      this.studyID = CancerStudy.NO_SUCH_STUDY;
      this.name = name;
      this.description = description;
      this.typeOfCancerId = typeOfCancerId;
      this.publicStudy = publicStudy;
   }
   
   public CancerStudy(String name, String description, String cancerStudyIdentifier, String typeOfCancerId,
            boolean publicStudy) {
      super();
      this.studyID = CancerStudy.NO_SUCH_STUDY;
      this.name = name;
      this.description = description;
      this.cancerStudyIdentifier = cancerStudyIdentifier;
      this.typeOfCancerId = typeOfCancerId;
      this.publicStudy = publicStudy;
   }

   public boolean isPublicStudy() {
      return publicStudy;
   }

   public void setPublicStudy() {
      this.publicStudy = true;
   }

   public void setPrivateStudy() {
      this.publicStudy = false;
   }

   public void setWhetherPublicStudy( boolean publicQ ) {
      this.publicStudy = publicQ;
   }

   public String getCancerStudyStableId() {
      return cancerStudyIdentifier;
   }

   public String getTypeOfCancerId() {
      return typeOfCancerId;
   }

   public void setTypeOfCancerId(String typeOfCancerId) {
      this.typeOfCancerId = typeOfCancerId;
   }

   public void setCancerStudyStablId(String cancerStudyIdentifier) {
      this.cancerStudyIdentifier = cancerStudyIdentifier;
   }

   public int getInternalId() {
      return studyID;
   }

   public void setInternalId(int studyId) {
      this.studyID = studyId;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   @Override
   public boolean equals( Object otherCancerStudy) {
       if( this == otherCancerStudy ) return true;
       if ( !(otherCancerStudy instanceof CancerStudy) ) return false;
       CancerStudy that = (CancerStudy) otherCancerStudy;
       return
           EqualsUtil.areEqual(this.publicStudy, that.publicStudy) &&
           EqualsUtil.areEqual(this.cancerStudyIdentifier, that.cancerStudyIdentifier) &&
           EqualsUtil.areEqual(this.description, that.description) &&
           EqualsUtil.areEqual(this.name, that.name) &&
           EqualsUtil.areEqual(this.typeOfCancerId, that.typeOfCancerId) &&
           EqualsUtil.areEqual(this.studyID, that.studyID);
   }

   @Override
   public String toString() {
      return "CancerStudy [studyID=" + studyID + ", name=" + name + ", description=" + description
               + ", cancerStudyIdentifier=" + cancerStudyIdentifier + ", typeOfCancerId=" + typeOfCancerId
               + ", publicStudy=" + publicStudy + "]";
   }

}