/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.model;

/**
 *
 * @author jgao
 */
public class Diagnostic {
    private Long diagnosticId;
    private Integer cancerStudyId;
    private String patientId;
    private Integer date;
    private String type;
    private String side;
    private String target;
    private String result;
    private String status;
    private String imageBaseline;
    private Integer numNewTumors;
    private String notes;

    public Long getDiagnosticId() {
        return diagnosticId;
    }

    public void setDiagnosticId(Long diagosticId) {
        this.diagnosticId = diagosticId;
    }

    public Integer getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(Integer cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageBaseline() {
        return imageBaseline;
    }

    public void setImageBaseline(String imageBaseline) {
        this.imageBaseline = imageBaseline;
    }

    public Integer getNumNewTumors() {
        return numNewTumors;
    }

    public void setNumNewTumors(Integer numNewTumors) {
        this.numNewTumors = numNewTumors;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    
}
