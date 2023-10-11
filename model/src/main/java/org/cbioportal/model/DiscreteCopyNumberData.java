package org.cbioportal.model;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class DiscreteCopyNumberData extends Alteration implements Serializable {
    @NotNull
    private Integer alteration;

    @JsonRawValue
    @Schema(type = "java.util.Map")
    private String annotationJson;

    public Integer getAlteration() {
        return alteration;
    }

    public void setAlteration(Integer alteration) {
        this.alteration = alteration;
    }
    
    public String getAnnotationJson() {
        return annotationJson;
    }

    public void setAnnotationJson(String annotationJson) {
        this.annotationJson = annotationJson;
    }

}
