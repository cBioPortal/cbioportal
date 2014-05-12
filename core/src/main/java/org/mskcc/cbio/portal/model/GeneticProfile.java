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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class for genetic profile
 */
public class GeneticProfile {
    private int geneticProfileId;
    private String stableId;
    private int cancerStudyId;
    private GeneticAlterationType geneticAlterationType;
    private String datatype;
    private String profileName;
    private String profileDescription;
    private String targetLine;
    private boolean showProfileInAnalysisTab;

    public GeneticProfile() {
      super();
   }

   public GeneticProfile(String stableId, int cancerStudyId, GeneticAlterationType geneticAlterationType,
						 String datatype, String profileName, String profileDescription, boolean showProfileInAnalysisTab) {
      super();
      this.stableId = stableId;
      this.cancerStudyId = cancerStudyId;
      this.geneticAlterationType = geneticAlterationType;
      this.datatype = datatype;
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

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
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
    
    @Override
    public String toString() {
       return ToStringBuilder.reflectionToString(this);
    }
    
}