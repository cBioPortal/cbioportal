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
 * Encapsulates Clinical Data.
 *
 * @author Gideon Dresdner <dresdnerg@cbio.mskcc.org>
 */
public class ClinicalData {
    private int cancerStudyId;
    private String stableId;
    private String attrId;
    private String attrVal;

    /**
     * Constructor
     */
    public ClinicalData() {
        this(-1, "", "", "");
    }
    
    public ClinicalData(ClinicalData other) {
        this(other.getCancerStudyId(), other.getStableId(), other.getAttrId(), other.getAttrVal());
    }

    /**
     * Constructor
     *
     * @param cancerStudyId     database id of cancer study
     * @param stableId          stable id of the patient or sample
     * @param attrId            database id of the attribute
     * @param attrVal           value of the clinical attribute given above
     */
    public ClinicalData(int cancerStudyId,
                        String stableId,
                        String attrId,
                        String attrVal) {

        this.cancerStudyId = cancerStudyId;
        this.stableId = stableId;
        this.attrId = attrId;
        this.attrVal = attrVal;
    }

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getStableId() {
        return stableId;
    }

    public void setStableId(String stableId) {
        this.stableId = stableId;
    }

    public String getAttrId() {
        return attrId;
    }

    public void setAttrId(String attrId) {
        this.attrId = attrId;
    }

    public String getAttrVal() {
        return attrVal;
    }

    public void setAttrVal(String attrVal) {
        this.attrVal = attrVal;
    }

    public String toString() {
        return String.format("ClinicalData[cancerStudyId=%d, %s, %s, %s]", cancerStudyId, stableId, attrId, attrVal);
    }
}
