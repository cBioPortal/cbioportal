package org.cbioportal.domain.wsi;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/** WSI slides associated with a cBioPortal sample, or unmatched pathology when null. */
@JsonInclude(JsonInclude.Include.ALWAYS)
public record WsiSampleGroup(String sampleId, List<WsiPart> parts) {}
