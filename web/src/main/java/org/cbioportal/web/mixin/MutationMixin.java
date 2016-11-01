package org.cbioportal.web.mixin;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.MutationEvent;
import org.cbioportal.model.Sample;
import org.cbioportal.web.mixin.summary.MutationSummaryMixin;

public class MutationMixin extends MutationSummaryMixin {

    private MutationEvent mutationEvent;
    private GeneticProfile geneticProfile;
    private Sample sample;
    private Gene gene;
}
