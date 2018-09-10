/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Class for genetic profile
 */
public class GeneticProfile implements Serializable {
    private int geneticProfileId;
    private String stableId;
    private int cancerStudyId;
    private GeneticAlterationType geneticAlterationType;
    private String datatype;
    private String profileName;
    private String profileDescription;
    private String targetLine;
    private boolean showProfileInAnalysisTab;
    private Properties otherMetadataFields;

    public GeneticProfile() {
      super();
   }

   public GeneticProfile(String stableId, int cancerStudyId, GeneticAlterationType geneticAlterationType,
						 String datatype, String profileName, String profileDescription, boolean showProfileInAnalysisTab) {
      this();
      this.stableId = stableId;
      this.cancerStudyId = cancerStudyId;
      this.geneticAlterationType = geneticAlterationType;
      this.datatype = datatype;
      this.profileName = profileName;
      this.profileDescription = profileDescription;
      this.showProfileInAnalysisTab = showProfileInAnalysisTab;
   }


   /**
    * Constructs a new genetic profile object with the same attributes as the one given as an argument.
    *
    * @param template  the object to copy
    */
   public GeneticProfile(GeneticProfile template) {
       this(
               template.getStableId(),
               template.getCancerStudyId(),
               template.getGeneticAlterationType(),
               template.getDatatype(),
               template.getProfileName(),
               template.getProfileDescription(),
               template.showProfileInAnalysisTab());
       this.setGeneticProfileId(template.geneticProfileId);
       this.setTargetLine(template.getTargetLine());
       this.setOtherMetadataFields(template.getAllOtherMetadataFields());
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

    /**
     * Stores metadata fields only recognized in particular data file types.
     *
     * @param fields  a properties instance holding the keys and values
     */
    public void setOtherMetadataFields(Properties fields) {
        this.otherMetadataFields = fields;
    }

    /**
     * Returns all file-specific metadata fields as a Properties object.
     *
     * @return  a properties instance holding the keys and values or null
     */
    public Properties getAllOtherMetadataFields() {
        return this.otherMetadataFields;
    }

    /**
     * Retrieves metadata fields specific to certain data file types.
     *
     * @param fieldname  the name of the field to retrieve
     * @return  the value of the field or null
     */
    public String getOtherMetaDataField(String fieldname) {
        if (otherMetadataFields == null) {
            return null;
        } else {
            return otherMetadataFields.getProperty(fieldname);
        }
    }

    @Override
    public String toString() {
       return ToStringBuilder.reflectionToString(this);
    }

}
