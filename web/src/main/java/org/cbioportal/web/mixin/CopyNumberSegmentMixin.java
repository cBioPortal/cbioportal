package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class CopyNumberSegmentMixin {

    @JsonIgnore
    private Integer segId;
}
