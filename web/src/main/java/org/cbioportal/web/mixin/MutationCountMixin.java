package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;

public class MutationCountMixin {

    @JsonUnwrapped
    private GeneticProfile geneticProfile;

    @JsonUnwrapped
    private Sample sample;

    private Integer mutationCount;
}
