package org.mskcc.cgds.model;

import org.apache.commons.lang.builder.ToStringBuilder;

public class GeneticProfile {
    private int geneticProfileId;
    private String stableId;
    private int cancerStudyId;
    private GeneticAlterationType geneticAlterationType;
    private String profileName;
    private String profileDescription;
    private String targetLine;
    private boolean showProfileInAnalysisTab;

    public GeneticProfile() {
      super();
   }

   public GeneticProfile(String stableId, int cancerStudyId, GeneticAlterationType geneticAlterationType,
            String profileName, String profileDescription, boolean showProfileInAnalysisTab) {
      super();
      this.stableId = stableId;
      this.cancerStudyId = cancerStudyId;
      this.geneticAlterationType = geneticAlterationType;
      this.profileName = profileName;
      this.profileDescription = profileDescription;
      this.showProfileInAnalysisTab = showProfileInAnalysisTab;
   }

   public int getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(int geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public GeneticAlterationType getGeneticAlterationType() {
        return geneticAlterationType;
    }

    public void setGeneticAlterationType(GeneticAlterationType geneticAlterationType) {
        this.geneticAlterationType = geneticAlterationType;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileDescription() {
        return profileDescription;
    }

    public void setProfileDescription(String profileDescription) {
        this.profileDescription = profileDescription;
    }

    public String getTargetLine() {
        return targetLine;
    }

    public void setTargetLine(String targetLine) {
        this.targetLine = targetLine;
    }

    public boolean showProfileInAnalysisTab() {
        return showProfileInAnalysisTab;
    }

    public void setShowProfileInAnalysisTab(boolean showProfileInAnalysisTab) {
        this.showProfileInAnalysisTab = showProfileInAnalysisTab;
    }
    
    public String toString() {
       return ToStringBuilder.reflectionToString(this);
    }
    
}