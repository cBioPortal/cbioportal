/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.model;

import java.io.Serializable;

/**
 *
 * @author jiaojiao
 */
public class CNASegmentData implements Serializable{
    private String sampleStableId;
    private String chromosome;
    private Integer start;
    private Integer end;
    private Integer numProbes;
    private float segmentMean;

    public String getSampleStableId() {
        return sampleStableId;
    }

    public void setSampleStableId(String sampleStableId) {
        this.sampleStableId = sampleStableId;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getNumProbes() {
        return numProbes;
    }

    public void setNumProbes(Integer numProbes) {
        this.numProbes = numProbes;
    }

    public float getSegmentMean() {
        return segmentMean;
    }

    public void setSegmentMean(float segmentMean) {
        this.segmentMean = segmentMean;
    }
}