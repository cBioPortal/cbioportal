package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.Gene;

public class MutationEventMixin {

    @JsonIgnore
    private Integer mutationEventId;
}
