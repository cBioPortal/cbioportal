package org.mskcc.cgds.model;

/**
 * Class for micro rna data
 */
public class MicroRna extends Gene{
    private String microRnaId;

    public MicroRna(String microRnaId) {
        this.microRnaId = microRnaId;
    }

    public String getStandardSymbol() {
        return microRnaId;
    }

    public String getMicroRnaId() {
        return microRnaId;
    }
}
