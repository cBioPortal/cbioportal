package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.ALWAYS)
public class ResultsPageSettings extends PageSettingsData implements Serializable {

    /**
     * Configuration of clinical tracks
     * Use lowercase instead of camelCase to be compatible with url query param 
     */
    private List<ClinicalTrackConfig> clinicallist = new ArrayList<>();

    public List<ClinicalTrackConfig> getClinicallist() {
        return clinicallist;
    }

    public void setClinicallist(List<ClinicalTrackConfig> clinicallist) {
        this.clinicallist = clinicallist;
    }
}

