package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenePanelToGeneMixin {

    @JsonIgnore
    private String genePanelId;
}
