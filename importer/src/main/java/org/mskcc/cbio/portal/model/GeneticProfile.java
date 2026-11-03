/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.mskcc.cbio.portal.model.shared.GeneticAlterationType;

/**
 * Class for genetic profile
 */
public class GeneticProfile implements Serializable {
    private int geneticProfileId;
    private String stableId;
    private int cancerStudyId;
    private GeneticAlterationType geneticAlterationType;
    private String genericAssayType;
    private String datatype;
    private String profileName;
    private String profileDescription;
    private String targetLine;
    private boolean showProfileInAnalysisTab;
    private Properties otherMetadataFields;
    private Float pivotThreshold;
    private String sortOrder;
    private boolean patientLevel;

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
    
    public GeneticProfile(String stableId, int cancerStudyId, GeneticAlterationType geneticAlterationType,
    String datatype, String profileName, String profileDescription, boolean showProfileInAnalysisTab, float pivotThreshold, String sortOrder) {
        this(stableId, cancerStudyId, geneticAlterationType, datatype, profileName, profileDescription, showProfileInAnalysisTab);
        this.pivotThreshold = pivotThreshold;
        this.sortOrder = sortOrder;
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
        this.setPivotThreshold(template.getPivotThreshold());
        this.setSortOrder(template.getSortOrder());
        this.setPatientLevel(template.getPatientLevel());
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

    public Float getPivotThreshold() {
        return this.pivotThreshold;
    }

    public void setPivotThreshold(Float pivotThreshold) {
        this.pivotThreshold = pivotThreshold;
    }

    public String getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getGenericAssayType() {
        return genericAssayType;
    }

    public void setGenericAssayType(String genericAssayType) {
        this.genericAssayType = genericAssayType;
    }

    public boolean getPatientLevel() {
        return patientLevel;
    }

    public void setPatientLevel(boolean patientLevel) {
        this.patientLevel = patientLevel;
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
