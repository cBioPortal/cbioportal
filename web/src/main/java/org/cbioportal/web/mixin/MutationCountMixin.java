package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;

public class MutationCountMixin {

    @JsonIgnore
    private Integer geneticProfileId;
    @JsonIgnore
    private Integer sampleId;
}
