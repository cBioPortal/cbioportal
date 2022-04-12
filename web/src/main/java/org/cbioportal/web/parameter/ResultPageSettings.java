package org.cbioportal.web.parameter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ResultPageSettings extends PageSettingsData implements Serializable {
    
    private List<ClinicalTrackConfig> clinicallist = new ArrayList<>();

    public List<ClinicalTrackConfig> getClinicallist() {
        return clinicallist;
    }

    public void setClinicallist(List<ClinicalTrackConfig> clinicallist) {
        this.clinicallist = clinicallist;
    }
}

class ClinicalTrackConfig {
    public String stableId;
    public String sortOrder;
    public boolean gapOn;
}