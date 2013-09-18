
package org.mskcc.cbio.portal.model;

/**
 *
 * @author jgao
 */
public class CopyNumberSegment {
    private long segId;
    private int cancerStudyId;
    private String sample;
    private String chr; // 1-22,X/Y,M
    private long start;
    private long end;
    private int numProbes;
    private double segMean;

    public CopyNumberSegment(int cancerStudyId, String sample, String chr,
            long start, long end, int numProbes, double segMean) {
        this.cancerStudyId = cancerStudyId;
        this.sample = sample;
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.numProbes = numProbes;
        this.segMean = segMean;
    }

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getNumProbes() {
        return numProbes;
    }

    public void setNumProbes(int numProbes) {
        this.numProbes = numProbes;
    }

    public String getCaseId() {
        return sample;
    }

    public void setCaseId(String sample) {
        this.sample = sample;
    }

    public long getSegId() {
        return segId;
    }

    public void setSegId(long segId) {
        this.segId = segId;
    }

    public double getSegMean() {
        return segMean;
    }

    public void setSegMean(double segMean) {
        this.segMean = segMean;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }
}
