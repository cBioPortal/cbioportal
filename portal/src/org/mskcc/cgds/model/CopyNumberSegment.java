
package org.mskcc.cgds.model;

/**
 *
 * @author jgao
 */
public class CopyNumberSegment {
    private long segId;
    private String sample;
    private String chr; // 1-22,X/Y,M
    private long start;
    private long end;
    private int numProbes;
    private double segMean;

    public CopyNumberSegment(String sample, String chr, long start, long end, int numProbes, double segMean) {
        this.sample = sample;
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.numProbes = numProbes;
        this.segMean = segMean;
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
