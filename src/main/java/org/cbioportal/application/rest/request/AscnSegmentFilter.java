package org.cbioportal.application.rest.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;

@Schema(name = "AscnSegmentFilter", description = "Selects the samples to fetch ASCN segments for")
public record AscnSegmentFilter(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "List of sample identifiers")
        @NotEmpty
        @Size(max = PagingConstants.MAX_PAGE_SIZE)
        List<@NotNull SampleIdentifier> sampleIdentifiers) {

  @AssertTrue(message = "each sample identifier needs a studyId and a sampleId")
  private boolean isSampleIdentifiersComplete() {
    return sampleIdentifiers == null
        || sampleIdentifiers.stream()
            .allMatch(id -> id != null && id.getStudyId() != null && id.getSampleId() != null);
  }

  @JsonIgnore
  public List<String> studyIds() {
    return sampleIdentifiers == null
        ? null
        : sampleIdentifiers.stream().map(SampleIdentifier::getStudyId).distinct().toList();
  }
}
