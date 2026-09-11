package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** The normalized WSI response assembled for one portal patient. */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiHierarchy(
    String referenceSampleId,
    List<WsiSampleGroup> sampleGroups) {}
