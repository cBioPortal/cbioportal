/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import java.util.*;

/**
 * Class to store information of patient list.
 */
public class SampleList {
   private String            stableId;
   private int               sampleListId;
   private int               cancerStudyId;
   private String            name;
   private String            description;
   private SampleListCategory  sampleListCategory;
   private ArrayList<String> sampleList;

   public SampleList() {
      super();
   }

   /**
    * A constructor for all the NON NULL fields in a sample_list
    * @param stableId
    * @param sampleListId
    * @param cancerStudyId
    * @param name
    */
   public SampleList(String stableId, int sampleListId, int cancerStudyId, String name,
                      SampleListCategory sampleListCategory) {
      super();
      this.stableId = stableId;
      this.sampleListId = sampleListId;
      this.cancerStudyId = cancerStudyId;
      this.name = name;
      this.sampleListCategory = sampleListCategory;
   }

   public String getStableId() {
      return stableId;
   }

   public void setStableId(String stableId) {
      this.stableId = stableId;
   }

   public int getSampleListId() {
      return sampleListId;
   }

   public void setSampleListId(int sampleListId) {
      this.sampleListId = sampleListId;
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

   public SampleListCategory getSampleListCategory() {
       return sampleListCategory;
   }

   public void setSampleListCategory(SampleListCategory sampleListCategory) {
       this.sampleListCategory = sampleListCategory;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public ArrayList<String> getSampleList() {
      return sampleList;
   }

   public void setSampleList(ArrayList<String> sampleList) {
      this.sampleList = sampleList;
   }

    @Override
    public String toString() {
        return this.getClass().getName() + "{"
                + "stableId " + this.stableId
                + ", sampleListId " + this.sampleListId
                + ", sampleListId " + this.cancerStudyId
                + ", name " + this.name
                + ", description " + this.description
                + ", SampleListCategory " + this.sampleListCategory
                + ", sampleList " + this.sampleList
                + "}";
    }


    /**
     * Gets list of all patient IDs in the set as one string.
     *
     * @return space-delimited list of patient IDs.
     */
    public String getSampleListAsString() {
        StringBuilder str = new StringBuilder();
        for (String patientId : sampleList) {
            str.append(patientId).append(" ");
        }
        return str.toString();
    }
}
