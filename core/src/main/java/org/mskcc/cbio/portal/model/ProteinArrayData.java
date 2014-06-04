/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/


package org.mskcc.cbio.portal.model;

/**
 * class for protein array data
 * @author jj
 */
public class ProteinArrayData {
    private String arrayId;
    private int cancerStudyId;
    private String caseId;
    private double abundance;

    public ProteinArrayData(int cancerStudyId, String arrayId, String caseId, double abundance) {
        this.cancerStudyId = cancerStudyId;
        this.arrayId = arrayId;
        this.caseId = caseId;
        this.abundance = abundance;
    }

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
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
