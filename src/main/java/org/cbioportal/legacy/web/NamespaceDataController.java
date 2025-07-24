package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.service.NamespaceDataService;
import org.cbioportal.legacy.web.config.InternalApiTags;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.NamespaceComparisonFilter;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = InternalApiTags.NAMESPACE_DATA, description = " ")
public class NamespaceDataController {

  private final NamespaceDataService namespaceDataService;

  @Autowired
  public NamespaceDataController(NamespaceDataService namespaceDataService) {
    this.namespaceDataService = namespaceDataService;
  }

  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @PostMapping(
      value = "/namespace-data/fetch",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "Get namespace data for comparison page for the selected samples and values")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = NamespaceData.class))))
  public ResponseEntity<List<NamespaceData>> getNamespaceDataForComparison(
      @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
          @RequestAttribute(required = false, value = "involvedCancerStudies")
          Collection<String> involvedCancerStudies,
      @Parameter(
              hidden =
                  true) // prevent reference to this attribute in the swagger-ui interface. this
          // attribute is needed for the @PreAuthorize tag above.
          @Valid
          @RequestAttribute(required = false, value = "interceptedNamespaceComparisonFilter")
          NamespaceComparisonFilter interceptedNamespaceComparisonFilter,
      @Parameter(
              required = true,
              description = "List of SampleIdentifiers, list of values and a NamespaceAttribute")
          @Valid
          @RequestBody(required = false)
          NamespaceComparisonFilter namespaceComparisonFilter) {

    List<NamespaceData> namespaceDataList;
    List<SampleIdentifier> sampleIdentifiers = namespaceComparisonFilter.getSampleIdentifiers();
    NamespaceAttribute namespaceAttribute = namespaceComparisonFilter.getNamespaceAttribute();
    List<String> values = namespaceComparisonFilter.getValues();
    List<String> studyIds = new ArrayList<>();
    List<String> sampleIds = new ArrayList<>();
    for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
      studyIds.add(sampleIdentifier.getStudyId());
      sampleIds.add(sampleIdentifier.getSampleId());
    }

    namespaceDataList =
        namespaceDataService.fetchNamespaceDataForComparison(
            studyIds, sampleIds, namespaceAttribute, values);

    return new ResponseEntity<>(namespaceDataList, HttpStatus.OK);
  }
}
