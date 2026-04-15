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
import org.cbioportal.legacy.model.DiscreteCopyNumberData;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.service.DiscreteCopyNumberService;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.DiscreteCopyNumberEventType;
import org.cbioportal.legacy.web.parameter.DiscreteCopyNumberFilter;
import org.cbioportal.legacy.web.parameter.DiscreteCopyNumberMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
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

@PublicApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.DISCRETE_COPY_NUMBER_ALTERATIONS, description = " ")
public class DiscreteCopyNumberController {

  @Autowired private DiscreteCopyNumberService discreteCopyNumberService;

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/discrete-copy-number",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get discrete copy number alterations in a molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = DiscreteCopyNumberData.class))))
  public ResponseEntity<List<DiscreteCopyNumberData>> getDiscreteCopyNumbersInMolecularProfile(
      @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_gistic")
          @PathVariable
          String molecularProfileId,
      @Parameter(required = true, description = "Sample List ID e.g. acc_tcga_all") @RequestParam
          String sampleListId,
      @Parameter(description = "Type of the copy number event")
          @RequestParam(defaultValue = "HOMDEL_AND_AMP")
          DiscreteCopyNumberEventType discreteCopyNumberEventType,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection)
      throws MolecularProfileNotFoundException {

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(
          HeaderKeyConstants.TOTAL_COUNT,
          discreteCopyNumberService
              .getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
                  molecularProfileId,
                  sampleListId,
                  null,
                  discreteCopyNumberEventType.getAlterationTypes())
              .getTotalCount()
              .toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(
          discreteCopyNumberService.getDiscreteCopyNumbersInMolecularProfileBySampleListId(
              molecularProfileId,
              sampleListId,
              null,
              discreteCopyNumberEventType.getAlterationTypes(),
              projection.name()),
          HttpStatus.OK);
    }
  }

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/discrete-copy-number/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description = "Fetch discrete copy number alterations in a molecular profile by sample ID")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = DiscreteCopyNumberData.class))))
  public ResponseEntity<List<DiscreteCopyNumberData>> fetchDiscreteCopyNumbersInMolecularProfile(
      @Parameter(required = true, description = "Molecular Profile ID e.g. acc_tcga_gistic")
          @PathVariable
          String molecularProfileId,
      @Parameter(description = "Type of the copy number event")
          @RequestParam(defaultValue = "HOMDEL_AND_AMP")
          DiscreteCopyNumberEventType discreteCopyNumberEventType,
      @Parameter(
              required = true,
              description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
          @Valid
          @RequestBody
          DiscreteCopyNumberFilter discreteCopyNumberFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection)
      throws MolecularProfileNotFoundException {

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      BaseMeta baseMeta;

      if (discreteCopyNumberFilter.getSampleListId() != null) {
        baseMeta =
            discreteCopyNumberService.getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(
                molecularProfileId,
                discreteCopyNumberFilter.getSampleListId(),
                discreteCopyNumberFilter.getEntrezGeneIds(),
                discreteCopyNumberEventType.getAlterationTypes());
      } else {
        baseMeta =
            discreteCopyNumberService.fetchMetaDiscreteCopyNumbersInMolecularProfile(
                molecularProfileId,
                discreteCopyNumberFilter.getSampleIds(),
                discreteCopyNumberFilter.getEntrezGeneIds(),
                discreteCopyNumberEventType.getAlterationTypes());
      }
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    } else {
      List<DiscreteCopyNumberData> discreteCopyNumberDataList;
      if (discreteCopyNumberFilter.getSampleListId() != null) {
        discreteCopyNumberDataList =
            discreteCopyNumberService.getDiscreteCopyNumbersInMolecularProfileBySampleListId(
                molecularProfileId,
                discreteCopyNumberFilter.getSampleListId(),
                discreteCopyNumberFilter.getEntrezGeneIds(),
                discreteCopyNumberEventType.getAlterationTypes(),
                projection.name());
      } else {
        discreteCopyNumberDataList =
            discreteCopyNumberService.fetchDiscreteCopyNumbersInMolecularProfile(
                molecularProfileId,
                discreteCopyNumberFilter.getSampleIds(),
                discreteCopyNumberFilter.getEntrezGeneIds(),
                discreteCopyNumberEventType.getAlterationTypes(),
                projection.name());
      }

      return new ResponseEntity<>(discreteCopyNumberDataList, HttpStatus.OK);
    }
  }

  @PreAuthorize(
      "hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/discrete-copy-number/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      description =
          "Fetch discrete copy number alterations by molecular profile IDs or molecular profile and sample pairs")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = DiscreteCopyNumberData.class))))
  public ResponseEntity<List<DiscreteCopyNumberData>>
      fetchDiscreteCopyNumbersInMultipleMolecularProfiles(
          @Parameter(hidden = true)
              @RequestAttribute(required = false, value = "involvedCancerStudies")
              Collection<String> involvedCancerStudies,
          @Parameter(hidden = true)
              @Valid
              @RequestAttribute(
                  required = false,
                  value = "interceptedDiscreteCopyNumberMultipleStudyFilter")
              DiscreteCopyNumberMultipleStudyFilter
                  interceptedDiscreteCopyNumberMultipleStudyFilter,
          @Parameter(
                  required = true,
                  description =
                      "List of molecular profile IDs and entrez gene IDs or list of molecular"
                          + " profile and sample pairs and entrez gene IDs")
              @Valid
              @RequestBody(required = false)
              DiscreteCopyNumberMultipleStudyFilter discreteCopyNumberMultipleStudyFilter,
          @Parameter(description = "Type of the copy number event")
              @RequestParam(defaultValue = "HOMDEL_AND_AMP")
              DiscreteCopyNumberEventType discreteCopyNumberEventType,
          @Parameter(description = "Level of detail of the response")
              @RequestParam(defaultValue = "SUMMARY")
              Projection projection) {

    List<String> molecularProfileIds;
    List<String> sampleIds = null;
    if (interceptedDiscreteCopyNumberMultipleStudyFilter.getMolecularProfileIds() != null) {
      molecularProfileIds =
          interceptedDiscreteCopyNumberMultipleStudyFilter.getMolecularProfileIds();
    } else {
      molecularProfileIds = new ArrayList<>();
      sampleIds = new ArrayList<>();
      extractMolecularProfileAndSampleIds(
          interceptedDiscreteCopyNumberMultipleStudyFilter, molecularProfileIds, sampleIds);
    }

    if (projection == Projection.META) {
      BaseMeta baseMeta =
          discreteCopyNumberService.getMetaDiscreteCopyNumbersInMultipleMolecularProfiles(
              molecularProfileIds,
              sampleIds,
              interceptedDiscreteCopyNumberMultipleStudyFilter.getEntrezGeneIds(),
              discreteCopyNumberEventType.getAlterationTypes());
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, baseMeta.getTotalCount().toString());
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }

    List<DiscreteCopyNumberData> result =
        discreteCopyNumberService.getDiscreteCopyNumbersInMultipleMolecularProfiles(
            molecularProfileIds,
            sampleIds,
            interceptedDiscreteCopyNumberMultipleStudyFilter.getEntrezGeneIds(),
            discreteCopyNumberEventType.getAlterationTypes(),
            projection.name());
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  private void extractMolecularProfileAndSampleIds(
      DiscreteCopyNumberMultipleStudyFilter discreteCopyNumberMultipleStudyFilter,
      List<String> molecularProfileIds,
      List<String> sampleIds) {

    for (SampleMolecularIdentifier sampleMolecularIdentifier :
        discreteCopyNumberMultipleStudyFilter.getSampleMolecularIdentifiers()) {
      molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
      sampleIds.add(sampleMolecularIdentifier.getSampleId());
    }
  }
}
