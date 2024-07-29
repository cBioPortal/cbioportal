package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.cbioportal.model.TypeOfCancer;

import java.util.Date;

public class GenericAssayDataCountMixin {
    @JsonIgnore
    private Integer profileType;

    @JsonIgnore
    private Integer stableId;
    
}
