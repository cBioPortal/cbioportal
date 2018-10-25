package org.cbioportal.web.parameter;

public class ClinicalDataBinFilter extends ClinicalDataFilter
{
    private Boolean disableLogScale;

    public Boolean getDisableLogScale() {
        return disableLogScale;
    }

    public void setDisableLogScale(Boolean disableLogScale) {
        this.disableLogScale = disableLogScale;
    }
}
