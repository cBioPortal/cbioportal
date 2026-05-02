package org.cbioportal.application.rest.response;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Compact matrix projection of generic assay data")
public class GenericAssayDataMatrixDTO {
    @Schema(description = "Ordered list of sample IDs representing the columns of the matrix")
    private List<String> sampleIds;
    
    @Schema(description = "Map of generic assay stable IDs to an array of values, ordered exactly as the sampleIds array")
    private Map<String, List<Object>> entries;

    public List<String> getSampleIds() {
        return sampleIds;
    }

    public void setSampleIds(List<String> sampleIds) {
        this.sampleIds = sampleIds;
    }

    public Map<String, List<Object>> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, List<Object>> entries) {
        this.entries = entries;
    }
    
}
