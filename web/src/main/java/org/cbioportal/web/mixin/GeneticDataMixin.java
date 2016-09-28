package org.cbioportal.web.mixin;

import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.web.mixin.summary.GeneticDataSummaryMixin;

public class GeneticDataMixin extends GeneticDataSummaryMixin {

    private GeneticProfile geneticProfile;
    private Gene gene;
    private Sample sample;
}
