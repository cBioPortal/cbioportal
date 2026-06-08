package org.cbioportal.legacy.web;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.legacy.service.MutationService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.MutationFilter;
import org.cbioportal.legacy.web.parameter.MutationMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.legacy.web.parameter.sort.MutationSortBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.MUTATIONS, description = " ")
public class MutationController {

  @Autowired private MutationService mutationService;
  @Autowired private ObjectMapper objectMapper;

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/mutations",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get mutations in a molecular profile by Sample List ID")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mutation.class))))
  public ResponseEntity<List<Mutation>> getMutationsInMolecularProfileBySampleListId(
      @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_mutations")
          @PathVariable
          String molecularProfileId,
      @Parameter(required = true, description = "Sample List ID e.g. acc_tcga_all") @RequestParam
          String sampleListId,
      @Parameter(required = true, description = "Entrez Gene ID") @RequestParam(required = true)
          Integer entrezGeneId,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection,
      @Parameter(description = "Page size of the result list")
          @Max(PagingConstants.MAX_PAGE_SIZE)
          @Min(PagingConstants.MIN_PAGE_SIZE)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE)
          Integer pageSize,
      @Parameter(description = "Page number of the result list")
          @Min(PagingConstants.MIN_PAGE_NUMBER)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
          Integer pageNumber,
      @Parameter(description = "Name of the property that the result list is sorted by")
          @RequestParam(required = false)
          MutationSortBy sortBy,
      @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
          Direction direction)
      throws MolecularProfileNotFoundException {

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      MutationMeta mutationMeta =
          mutationService.getMetaMutationsInMolecularProfileBySampleListId(
              molecularProfileId,
              sampleListId,
              entrezGeneId == null ? null : Arrays.asList(entrezGeneId));
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
      responseHeaders.add(
          HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          mutationService.getMutationsInMolecularProfileBySampleListId(
              molecularProfileId,
              sampleListId,
              entrezGeneId == null ? null : Arrays.asList(entrezGeneId),
              false,
              projection.name(),
              pageSize,
              pageNumber,
              sortBy == null ? null : sortBy.getOriginalValue(),
              direction.name()),
          HttpStatus.OK);
    }
  }

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/mutations/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch mutations in a molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mutation.class))))
  public ResponseEntity<List<Mutation>> fetchMutationsInMolecularProfile(
      @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_mutations")
          @PathVariable
          String molecularProfileId,
      @Parameter(
              required = true,
              description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
          @Valid
          @RequestBody
          MutationFilter mutationFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection,
      @Parameter(description = "Page size of the result list")
          @Max(PagingConstants.MAX_PAGE_SIZE)
          @Min(PagingConstants.MIN_PAGE_SIZE)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE)
          Integer pageSize,
      @Parameter(description = "Page number of the result list")
          @Min(PagingConstants.MIN_PAGE_NUMBER)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
          Integer pageNumber,
      @Parameter(description = "Name of the property that the result list is sorted by")
          @RequestParam(required = false)
          MutationSortBy sortBy,
      @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
          Direction direction)
      throws MolecularProfileNotFoundException {

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      MutationMeta mutationMeta;

      if (mutationFilter.getSampleListId() != null) {
        mutationMeta =
            mutationService.getMetaMutationsInMolecularProfileBySampleListId(
                molecularProfileId,
                mutationFilter.getSampleListId(),
                mutationFilter.getEntrezGeneIds());
      } else {
        mutationMeta =
            mutationService.fetchMetaMutationsInMolecularProfile(
                molecularProfileId,
                mutationFilter.getSampleIds(),
                mutationFilter.getEntrezGeneIds());
      }
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
      responseHeaders.add(
          HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    } else {
      List<Mutation> mutations;
      if (mutationFilter.getSampleListId() != null) {
        mutations =
            mutationService.getMutationsInMolecularProfileBySampleListId(
                molecularProfileId,
                mutationFilter.getSampleListId(),
                mutationFilter.getEntrezGeneIds(),
                false,
                projection.name(),
                pageSize,
                pageNumber,
                sortBy == null ? null : sortBy.getOriginalValue(),
                direction.name());
      } else {
        mutations =
            mutationService.fetchMutationsInMolecularProfile(
                molecularProfileId,
                mutationFilter.getSampleIds(),
                mutationFilter.getEntrezGeneIds(),
                false,
                projection.name(),
                pageSize,
                pageNumber,
                sortBy == null ? null : sortBy.getOriginalValue(),
                direction.name());
      }

      return new ResponseEntity<>(mutations, HttpStatus.OK);
    }
  }

  //  @Hidden
  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/mutations/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch mutations in multiple molecular profiles by sample IDs")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = Mutation.class))))
  public ResponseEntity<StreamingResponseBody> fetchMutationsInMultipleMolecularProfiles(
      @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
          @RequestAttribute(required = false, value = "involvedCancerStudies")
          Collection<String> involvedCancerStudies,
      @Parameter(
              hidden =
                  true) // prevent reference to this attribute in the swagger-ui interface. this
          // attribute is needed for the @PreAuthorize tag above.
          @Valid
          @RequestAttribute(required = false, value = "interceptedMutationMultipleStudyFilter")
          MutationMultipleStudyFilter interceptedMutationMultipleStudyFilter,
      @Parameter(
              required = true,
              description =
                  "List of Molecular Profile IDs or List of Molecular Profile ID / Sample ID pairs,"
                      + " and List of Entrez Gene IDs")
          @Valid
          @RequestBody(required = false)
          MutationMultipleStudyFilter mutationMultipleStudyFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection,
      @Parameter(description = "Page size of the result list")
          @Max(PagingConstants.MAX_PAGE_SIZE)
          @Min(PagingConstants.MIN_PAGE_SIZE)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE)
          Integer pageSize,
      @Parameter(description = "Page number of the result list")
          @Min(PagingConstants.MIN_PAGE_NUMBER)
          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER)
          Integer pageNumber,
      @Parameter(description = "Name of the property that the result list is sorted by")
          @RequestParam(required = false)
          MutationSortBy sortBy,
      @Parameter(description = "Direction of the sort") @RequestParam(defaultValue = "ASC")
          Direction direction) {

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      MutationMeta mutationMeta;

      if (interceptedMutationMultipleStudyFilter.getMolecularProfileIds() != null) {
        mutationMeta =
            mutationService.getMetaMutationsInMultipleMolecularProfiles(
                interceptedMutationMultipleStudyFilter.getMolecularProfileIds(),
                null,
                interceptedMutationMultipleStudyFilter.getEntrezGeneIds());
      } else {

        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        extractMolecularProfileAndSampleIds(
            interceptedMutationMultipleStudyFilter, molecularProfileIds, sampleIds);
        mutationMeta =
            mutationService.getMetaMutationsInMultipleMolecularProfiles(
                molecularProfileIds,
                sampleIds,
                interceptedMutationMultipleStudyFilter.getEntrezGeneIds());
      }
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, mutationMeta.getTotalCount().toString());
      responseHeaders.add(
          HeaderKeyConstants.SAMPLE_COUNT, mutationMeta.getSampleCount().toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }

    // Resolve the (profileIds, sampleIds) for the chosen branch, then stream the result as a JSON
    // array so the (potentially very large) mutation result set is never materialized into a list.
    final List<String> molecularProfileIds;
    final List<String> sampleIds;
    if (interceptedMutationMultipleStudyFilter.getMolecularProfileIds() != null) {
      molecularProfileIds = interceptedMutationMultipleStudyFilter.getMolecularProfileIds();
      sampleIds = null;
    } else {
      molecularProfileIds = new ArrayList<>();
      sampleIds = new ArrayList<>();
      extractMolecularProfileAndSampleIds(
          interceptedMutationMultipleStudyFilter, molecularProfileIds, sampleIds);
    }
    final List<Integer> entrezGeneIds = interceptedMutationMultipleStudyFilter.getEntrezGeneIds();
    final String projectionName = projection.name();
    final String sortByValue = sortBy == null ? null : sortBy.getOriginalValue();
    final String directionName = direction.name();

    StreamingResponseBody body =
        outputStream -> {
          try (JsonGenerator generator = objectMapper.getFactory().createGenerator(outputStream)) {
            generator.writeStartArray();
            mutationService.streamMutationsInMultipleMolecularProfiles(
                molecularProfileIds,
                sampleIds,
                entrezGeneIds,
                projectionName,
                pageSize,
                pageNumber,
                sortByValue,
                directionName,
                mutation -> {
                  try {
                    generator.writeObject(mutation);
                  } catch (IOException e) {
                    // ResultHandler/Consumer cannot throw checked exceptions; unwrapped below
                    throw new UncheckedIOException(e);
                  }
                });
            generator.writeEndArray();
          } catch (UncheckedIOException e) {
            throw e.getCause();
          }
        };
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
  }

  private void extractMolecularProfileAndSampleIds(
      MutationMultipleStudyFilter mutationMultipleStudyFilter,
      List<String> molecularProfileIds,
      List<String> sampleIds) {

    for (SampleMolecularIdentifier sampleMolecularIdentifier :
        mutationMultipleStudyFilter.getSampleMolecularIdentifiers()) {

      molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
      sampleIds.add(sampleMolecularIdentifier.getSampleId());
    }
  }
}
