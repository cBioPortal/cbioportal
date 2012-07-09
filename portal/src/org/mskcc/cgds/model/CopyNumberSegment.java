
package org.mskcc.cgds.model;

/**
 *
 * @author jgao
 */
public class CopyNumberSegment {
    private int segId;
    private String sample;
    private int chromosome; // 1-24
    private long start;
    private long end;
    private int numProbes;
    private double segMean;

    public CopyNumberSegment(String sample, int chromosome, long start, long end, int numProbes, double segMean) {
        this.sample = sample;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.numProbes = numProbes;
        this.segMean = segMean;
    }

    public int getChromosome() {
        return chromosome;
    }

    public void setChromosome(int chromosome) {
        this.chromosome = chromosome;
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

    public int getSegId() {
        return segId;
    }

    public void setSegId(int segId) {
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
