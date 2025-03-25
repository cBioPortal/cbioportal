package org.cbioportal.legacy.web.mixin;

import com.fasterxml.jackson.annotation.*;

public class StructuralVariantMixin {

    @JsonProperty("namespaceColumns")
    private Object annotationJson;
}