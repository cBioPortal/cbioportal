/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

/**
 *
 * @author jgao
 */
public class Treatment {
    private long treatmentId;
    private int cancerStudyId;
    private String caseId;
    private int startDate;
    private int stopDate;
    private String type;
    private String agent;
    private double dose;
    private String unit;
    private String schedule;

    public long getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(long treatmentId) {
        this.treatmentId = treatmentId;
    }

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public int getStartDate() {
        return startDate;
    }

    public void setStartDate(int startDate) {
        this.startDate = startDate;
    }

    public int getStopDate() {
        return stopDate;
    }

    public void setStopDate(int stopDate) {
        this.stopDate = stopDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public double getDose() {
        return dose;
    }

    public void setDose(double dose) {
        this.dose = dose;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
    
}