/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.model;

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
    private int chromosome;
    private String cytoband;
    private int peakStart;
    private int peakEnd;
    private ArrayList<CanonicalGene> genes_in_ROI;
    private double qValue;
    private boolean amp;

    /**
     * Constructor.
     * @param cancerStudyId     database key
     * @param chromosome        chromosome locus (eg. chromosome 17)
     * @param peakStart         start of wide peak
     * @param peakEnd           end of wide peak
     * @param qValue            q-value for the ROI
     * @param genes_in_ROI      genes in the ROI
     * @param amp_del           region is amplified or deleted. To set use Gistic.AMPLIFIED or Gistic.DELETED
     */
    public Gistic(int cancerStudyId, int chromosome, String cytoband, int peakStart, int peakEnd,
                  double qValue, ArrayList<CanonicalGene> genes_in_ROI, boolean amp_del) {

        this.gisticID = NO_SUCH_GISTIC;
        this.cancerStudyId = cancerStudyId;
        this.chromosome = chromosome;
        this.cytoband = cytoband;
        this.peakStart = peakStart;
        this.peakEnd = peakEnd;
        this.qValue = qValue;
        this.genes_in_ROI = genes_in_ROI;
        this.amp = amp_del;
    }

    // todo: refactor this to be a method : Gistic.getDummyGistic();  What is the syntax for factory methods?
    /**
     * Dummy gistic for passing between different methods that can only
     * set disjoint sets of fields (see GisticReader.java)
     */
    public Gistic() {
        this.gisticID = NO_SUCH_GISTIC;
        this.cancerStudyId = -1;
        this.chromosome = -1;
        this.cytoband = "";
        this.peakStart = -1;
        this.peakEnd = -1;
        this.qValue = -1f;
        this.genes_in_ROI = new ArrayList<CanonicalGene>();
        this.amp = false;
    }

    /**
     * Calculates the size of the ROI
     * @return size of the peak in ROI
     */
    public int peakSize() {
        return this.peakEnd - this.peakStart;
    }
    
    @Override public String toString() {
        
        return String.format("cancerStudyId=%d, " +
                "chromosome: %d, " +
                "cytoband: %s, " +
                "peakStart: %d, " +
                "peakEnd: %d, " +
                "qValue: %s, " +
                "genes_in_ROI: %s, " +
                "amp: %s", this.cancerStudyId,
                this.chromosome,
                this.cytoband,
                this.peakStart,
                this.peakEnd,
                Double.toString(this.qValue),
                this.genes_in_ROI,
                this.amp);
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
     * Sets the chromosome of a gistic
     * @param chromosome
     */
    public void setChromosome(int chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * Sets the cytoband of a gistic
     * @param cytoband
     */
    public void setCytoband (String cytoband) {
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
     * Adds a gene to the genes in the ROI.
     * N.B. All this method does is wrap the add function.
     * It does not fancy. Specifically,
     * it does *not* check for duplicates.
     * @param gene
     */
    public void addGene(CanonicalGene gene) {
        this.genes_in_ROI.add(gene);
    }

    /**
     * Sets the q-value of the ROI
     * @param qValue
     */
    public void setqValue(double qValue) {
        this.qValue = qValue;
    }

    /**
     * Sets whether the ROI is a deletion or amplification
     * Use Gistic.AMPLIFIED and Gistic.DELETED
     * @param amp
     */
    public void setAmp(boolean amp) {
        this.amp = amp;
    }

    /**
     * Returns the chromosome location in chromosome-arm-band notation,
     * called chromosome notation (e.g. 1q12)
     * @return chromosome locus of ROI
     */
    public int getChromosome() {
        return chromosome;
    }

    /**
     * Returns the cytoband
     * @return cytoband of the ROI
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
    public double getqValue() {
        return qValue;
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
    public boolean getAmp() {
        return amp;
    }
}
