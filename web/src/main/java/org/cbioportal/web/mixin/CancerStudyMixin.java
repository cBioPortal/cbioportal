package org.cbioportal.web.mixin;

import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.web.mixin.summary.CancerStudySummaryMixin;

public class CancerStudyMixin extends CancerStudySummaryMixin {

    private TypeOfCancer typeOfCancer;
    private Integer sampleCount;
}
