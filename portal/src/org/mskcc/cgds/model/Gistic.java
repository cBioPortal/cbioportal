package org.mskcc.cgds.model;

import java.util.ArrayList;

/**
 * This represents Gistic information for a particular ROI (region of interest)
 *
 * @author Gideon Dresdner
 */

public class Gistic {

    public static final int NO_SUCH_GISTIC = -1;
    public static final boolean AMPLIFIED = true;       // ROI is an amplified region
    public static final boolean DELETED = false;        // ROI is a deleted region

    private int gisticID;
    private int cancerStudyId;
    private String cytoband;
    private int peakStart;
    private int peakEnd;
    private ArrayList<CanonicalGene> genes_in_ROI;
    private String qValue;
    private String res_qValue;
    private boolean ampDel;

    /**
     * Constructor.
     * @param cancerStudyId     database key
     * @param cytoband          cytoband locus (eg. 1p13)
     * @param peakStart         start of wide peak
     * @param peakEnd           end of wide peak
     * @param qValue            q-value for the ROI
     * @param res_qValue        residual q-value for the ROI
     * @param genes_in_ROI      genes in the ROI
     * @param amp_del           region is amplified or deleted. To set use Gistic.AMPLIFIED or Gistic.DELETED
     */

    public Gistic(int cancerStudyId, String cytoband, int peakStart, int peakEnd,
                  String qValue, String res_qValue, ArrayList<CanonicalGene> genes_in_ROI, boolean amp_del) {

        this.gisticID = NO_SUCH_GISTIC;
        this.cancerStudyId = cancerStudyId;
        this.cytoband = cytoband;
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
        this.qValue = qValue;
        this.res_qValue = res_qValue;
        this.genes_in_ROI = genes_in_ROI;
        this.ampDel = amp_del;
    }

    /**
     * Calculates the size of the ROI
     * @return size of the peak in ROI
     */

    public int peakSize() {
        return this.peakEnd - this.peakStart;
    }

    /**
     * Sets the internal ID associated with this record
     * @param internalId
     */

    public void setInternalId(int internalId) {
        this.gisticID = internalId;
    }

    /**
     * Sets the Cancer Study Id of a gistic
     * @param cancerStudyId
     */
    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    /**
     * Sets the Cytoband of a gistic
     * @param cytoband
     */
    public void setCytoband(String cytoband) {
        this.cytoband = cytoband;
    }

    /**
     * Sets the start of the peak of the ROI
     * @param peakStart
     */
    public void setPeakStart(int peakStart) {
        this.peakStart = peakStart;
    }

    /**
     * Sets the end of the peak of the ROI
     * @param peakEnd
     */
    public void setPeakEnd(int peakEnd) {
        this.peakEnd = peakEnd;
    }

    /**
     * Sets the genes in the ROI
     * @param genes_in_ROI
     */
    public void setGenes_in_ROI(ArrayList<CanonicalGene> genes_in_ROI) {
        this.genes_in_ROI = genes_in_ROI;
    }

    /**
     * Sets the q-value of the ROI
     * @param qValue
     */
    public void setqValue(String qValue) {
        this.qValue = qValue;
    }

    /**
     * Sets the residue q-value of the ROI
     * @param res_qValue
     */
    public void setRes_qValue(String res_qValue) {
        this.res_qValue = res_qValue;
    }

    /**
     * Sets whether the ROI is a deletion or amplification
     * Use Gistic.AMPLIFIED and Gistic.DELETED
     * @param ampDel
     */
    public void setAmpDel(boolean ampDel) {
        this.ampDel = ampDel;
    }

    /**
     * Returns the cytoband location in cytoband-arm-band notation,
     * called cytoband notation (e.g. 1q12)
     * @return cytoband locus of ROI
     */

    public String getCytoband() {
        return cytoband;
    }

    /**
     * Returns the start of the peak
     * @return start of peak of ROI
     */

    public int getPeakStart() {
        return peakStart;
    }

    /**
     * Returns the end of the peak
     * @return end of peak of ROI
     */

    public int getPeakEnd() {
        return peakEnd;
    }

    /**
     * Returns a list of the genes in the ROI
     * @return List of genes in the wide peak of the ROI
     */

    public ArrayList<CanonicalGene> getGenes_in_ROI() {
        return genes_in_ROI;
    }

    /**
     * Returns the q-value of the wide peak
     * @return q-value of wide peak
     */

    public String getqValue() {
        return qValue;
    }

    /**
     * Returns the residual q-value of the wide peak.
     * @return residual q-value of wide peak
     */

    public String getRes_qValue() {
        return res_qValue;
    }

    /**
     * Returns the internal CancerStudy ID of the study associated with this ROI
     * @return Returns the internal CancerStudy ID of the ROI
     */

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    /**
     * Returns the internal ID associated with this ROI
     * @return internal ID associated with this ROI
     */

    public int getInternalId() {
        return gisticID;
    }

    /**
     * Returns whether or not the wide peak of ROI is a region of amplification or deletion
     * @return whether or not ROI is amplified or deleted, use Gistic.AMPLIFIED or Gistic.DELETED
     */

    public boolean getAmpDel() {
        return ampDel;
    }
}
