
package org.mskcc.cgds.model;

/**
 *
 * @author jj
 */
public class ProteinArrayData {
    private String arrayId;
    private String caseId;
    private int cancerStudyId;
    private double abundance;

    public ProteinArrayData(String arrayId, String caseId, int cancerStudyId, double abundance) {
        this.arrayId = arrayId;
        this.caseId = caseId;
        this.cancerStudyId = cancerStudyId;
        this.abundance = abundance;
    }

    public double getAbundance() {
        return abundance;
    }

    public void setAbundance(double abundance) {
        this.abundance = abundance;
    }

    public String getArrayId() {
        return arrayId;
    }

    public void setArrayId(String arrayId) {
        this.arrayId = arrayId;
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
    
    
}
