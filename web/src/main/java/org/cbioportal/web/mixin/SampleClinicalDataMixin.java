package org.cbioportal.web.mixin;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.Sample;
import org.cbioportal.web.mixin.summary.ClinicalDataSummaryMixin;

public class SampleClinicalDataMixin extends ClinicalDataSummaryMixin {

    private Sample sample;
    private ClinicalAttribute clinicalAttribute;

}
