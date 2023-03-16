package org.cbioportal.webparam.sort;

public enum CopyNumberSegmentSortBy {

    chromosome("chr"),
    start("start"),
    end("end"),
    numberOfProbes("numProbes"),
    segmentMean("segmentMean");

    private String originalValue;

    CopyNumberSegmentSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
}
