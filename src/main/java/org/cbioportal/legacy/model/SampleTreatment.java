package org.cbioportal.legacy.model;

import java.util.List;

public record SampleTreatment(
    String treatment,
    int preSampleCount,
    int postSampleCount,
    List<ClinicalEventSample> preSamples,
    List<ClinicalEventSample> postSamples) {}
