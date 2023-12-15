package org.cbioportal.web.parameter.sort;

public enum SignificantCopyNumberRegionSortBy {

    chromosome("chromosome"),
    cytoband("cytoband"),
    widePeakStart("widePeakStart"),
    widePeakEnd("widePeakEnd"),
    qValue("qValue"),
    amp("amp");

    private String originalValue;

    SignificantCopyNumberRegionSortBy(String originalValue) {
        this.originalValue = originalValue;
    }

    public String getOriginalValue() {
        return originalValue;
    }
    
}
