/*
 * Copyright (c) 2015 - 2022 Memorial Sloan Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import java.util.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;

/**
 * This represents a cancer study, with a set of cases and some data sets.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class CancerStudy {
    /**
     * NO_SUCH_STUDY Internal ID has not been assigned yet.
     */
    public static final int NO_SUCH_STUDY = -1;

    private int studyID; // assigned by dbms auto increment
    private String name;
    private String description;
    private String cancerStudyIdentifier;
    private String typeOfCancerId;  // required
    private boolean publicStudy;  // if true, a public study, otherwise private
    private String pmid;
    private String citation;
    private Set<String> groups;
    private Date importDate;
    private String referenceGenome;
    

    /**
     * Constructor.
     * @param name                  Name of Cancer Study.
     * @param description           Description of Cancer Study.
     * @param cancerStudyIdentifier Cancer Study Stable Identifier.
     * @param typeOfCancerId        Type of Cancer.
     * @param publicStudy           Flag to indicate if this is a public study.
     */
    public CancerStudy(String name, String description, String cancerStudyIdentifier,
            String typeOfCancerId, boolean publicStudy) {
        super();
        this.studyID = CancerStudy.NO_SUCH_STUDY;
        this.name = name;
        this.description = description;
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.typeOfCancerId = typeOfCancerId;
        this.publicStudy = publicStudy;
    }

    /**
     * Indicates that this is a public study.
     * @return true or false.
     */
    public boolean isPublicStudy() {
        return publicStudy;
    }

    /**
     * Marks this study as public or private.
     * @param publicFlag Public Flag.
     */
    public void setPublicStudy(boolean publicFlag) {
        this.publicStudy = publicFlag;
    }

    /**
     * Gets the Cancer Study Stable Identifier.
     * @return cancer study stable identifier.
     */
    public String getCancerStudyStableId() {
        return cancerStudyIdentifier;
    }

    /**
     * Gets the Type of Cancer.
     * @return type of cancer.
     */
    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    /**
     * Sets the Cancer Study Stable Identifier.
     * @param cancerStudyIdentifier Cancer Study Stable Identifier.
     */
    public void setCancerStudyStablId(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    /**
     * Gets the Internal ID associated with this record.
     * @return internal integer ID.
     */
    public int getInternalId() {
        return studyID;
    }

    /**
     * Sets the Internal ID associated with this record.
     * @param studyId internal integer ID.
     */
    public void setInternalId(int studyId) {
        this.studyID = studyId;
    }

    /**
     * Gets the Cancer Study Name.
     * @return cancer study name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the Cancer Study Name.
     * @param name cancer study name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the Cancer Study Description.
     * @return cancer study description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the Cancer Study Description.
     * @param description cancer study description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }
    
    public String getReferenceGenome() { return referenceGenome; }
    
    public void setReferenceGenome(String referenceGenome) {
        this.referenceGenome = referenceGenome;
    }

    public Set<String> getFreshGroups() throws DaoException
    {
        return DaoCancerStudy.getFreshGroups(studyID);
    }

    public Set<String> getGroups() {
        if (groups==null) {
            return Collections.emptySet();
        }
        return groups;
    }

    /**
     * Determines the set of groups by splitting the given groups string
     * on ';' and transforming all group names to their UPPER CASE 
     * representation (this last one is in compliance to what is expected 
     * in CancerStudyPermissionEvaluator.hasPermission() ).
     * 
     * @param groups semi-colon (;) delimited groups
     */
    public void setGroupsInUpperCase(String groups) {
        if (groups==null) {
            this.groups = null;
            return;
        }
        
        this.groups = new HashSet<String>(Arrays.asList(groups.toUpperCase().split(";")));
    }

    /**
     * Equals.
     * @param otherCancerStudy Other Cancer Study.
     * @return true of false.
     */
    @Override
    public boolean equals(Object otherCancerStudy) {
        if (this == otherCancerStudy) {
            return true;
        }
        
        if (!(otherCancerStudy instanceof CancerStudy)) {
            return false;
        }
        
        CancerStudy that = (CancerStudy) otherCancerStudy;
        return
                EqualsUtil.areEqual(this.publicStudy, that.publicStudy) &&
                        EqualsUtil.areEqual(this.cancerStudyIdentifier,
                                that.cancerStudyIdentifier) &&
                        EqualsUtil.areEqual(this.description, that.description) &&
                        EqualsUtil.areEqual(this.name, that.name) &&
                        EqualsUtil.areEqual(this.typeOfCancerId, that.typeOfCancerId) &&
                        EqualsUtil.areEqual(this.studyID, that.studyID);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.studyID;
        hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 11 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 11 * hash + (this.cancerStudyIdentifier != null ? this.cancerStudyIdentifier.hashCode() : 0);
        hash = 11 * hash + (this.typeOfCancerId != null ? this.typeOfCancerId.hashCode() : 0);
        hash = 11 * hash + (this.publicStudy ? 1 : 0);
        return hash;
    }

    /**
     * toString() Override.
     * @return string summary of cancer study.
     */
    @Override
    public String toString() {
        return "CancerStudy [studyID=" + studyID + ", name=" + name + ", description="
                + description + ", cancerStudyIdentifier=" + cancerStudyIdentifier
                + ", typeOfCancerId=" + typeOfCancerId + ", publicStudy=" + publicStudy + "]";
    }

    public boolean hasMutSigData() throws DaoException {
        return !DaoMutSig.hasMutsig(this);
    }

    public boolean hasGisticData() throws DaoException {
        return DaoGistic.hasGistic(this);
    }
    
    public boolean hasSurvivalData() throws DaoException {
        Set<String> attrs = DaoClinicalData.getDistinctParameters(studyID);
        return attrs.contains(ClinicalAttribute.OS_STATUS) ||
                    attrs.contains(ClinicalAttribute.DFS_STATUS);
    }

    public String getTypeOfCancer() throws DaoException {
        return DaoTypeOfCancer.getTypeOfCancerById(this.typeOfCancerId).getName();
    }

    /**
     * @return the importDate
     */
    public Date getImportDate() {
        return importDate;
    }

    /**
     * @param importDate the importDate to set
     */
    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }
}
