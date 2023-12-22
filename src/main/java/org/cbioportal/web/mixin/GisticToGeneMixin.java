package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GisticToGeneMixin {

    @JsonIgnore
    private Long gisticRoiId;
}
