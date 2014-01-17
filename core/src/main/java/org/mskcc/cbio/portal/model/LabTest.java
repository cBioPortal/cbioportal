/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

/**
 *
 * @author jgao
 */
public class LabTest {
    private long labTestId;
    private int cancerStudyId;
    private String caseId;
    private int date;
    private String test;
    private double result;
    private String unit;

    public long getLabTestId() {
        return labTestId;
    }

    public void setLabTestId(long labTestId) {
        this.labTestId = labTestId;
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

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public double getResult() {
        return result;
    }

    public void setResult(double result) {
        this.result = result;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    
}
