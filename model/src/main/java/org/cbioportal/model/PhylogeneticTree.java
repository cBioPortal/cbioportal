package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class PhylogeneticTree implements Serializable {
    private long internalId;
    private int cancerStudyId;
    private String cancerStudyIdentifier;
    private int patientId;
    private String patientStableId;
    private String ancestorClone;
    private String descendantClone;

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getCancerStudyIdentifier() {
        return cancerStudyIdentifier;
    }

    public void setCancerStudyIdentifier(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    public String getAncestorClone() {
        return ancestorClone;
    }

    public void setAncestorClone(String ancestorClone) {
        this.ancestorClone = ancestorClone;
    }

    public String getDescendantClone() {
        return descendantClone;
    }

    public void setDescendantClone(String descendantClone) {
        this.descendantClone = descendantClone;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientStableId() {
        return patientStableId;
    }

    public void setPatientStableId(String patientStableId) {
        this.patientStableId = patientStableId;
    }

    public long getInternalId() {
        return internalId;
    }

    public void setInternalId(long internalId) {
        this.internalId = internalId;
    }

}
