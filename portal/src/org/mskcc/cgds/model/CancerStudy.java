package org.mskcc.cgds.model;

import org.mskcc.cgds.util.EqualsUtil;

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

    /**
     * Equals.
     * @param otherCancerStudy Other Cancer Study.
     * @return true of false.
     */
    public boolean equals(Object otherCancerStudy) {
        if (this == otherCancerStudy) return true;
        if (!(otherCancerStudy instanceof CancerStudy)) return false;
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

    /**
     * toString() Override.
     * @return string summary of cancer study.
     */
    public String toString() {
        return "CancerStudy [studyID=" + studyID + ", name=" + name + ", description="
                + description + ", cancerStudyIdentifier=" + cancerStudyIdentifier
                + ", typeOfCancerId=" + typeOfCancerId + ", publicStudy=" + publicStudy + "]";
    }
}