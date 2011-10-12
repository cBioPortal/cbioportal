
package org.mskcc.cgds.model;

/**
 *
 * @author jj
 */
public class ProteinArrayData {
    private String arrayId;
    private String caseId;
    private double abundance;

    public ProteinArrayData(String arrayId, String caseId, double abundance) {
        this.arrayId = arrayId;
        this.caseId = caseId;
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

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
    
    
}
