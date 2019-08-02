package org.cbioportal.service.util.oncokb;

/**
 * Created by Hongxin Zhang on 2019-07-25.
 */
public enum Oncogenicity {
    YES("Oncogenic"),
    LIKELY("Likely Oncogenic"),
    PREDICTED("Predicted Oncogenic"),
    LIKELY_NEUTRAL("Likely Neutral"),
    INCONCLUSIVE("Inconclusive"),
    UNKNOWN("Unknown")
    ;
    private String oncogenic;

    Oncogenicity(String oncogenic) {
        this.oncogenic = oncogenic;
    }

    public String getOncogenic() {
        return oncogenic;
    }
}
