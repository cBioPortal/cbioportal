package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.application.rest.mapper.NumericGeneMolecularDataMapper;
import org.cbioportal.application.rest.response.NumericGeneMolecularDataDTO;
import org.cbioportal.domain.genomic_data.usecase.FetchGeneMolecularDataUseCase;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.config.PublicApiTags;
import org.cbioportal.legacy.web.config.annotation.PublicApi;
import org.cbioportal.legacy.web.parameter.HeaderKeyConstants;
import org.cbioportal.legacy.web.parameter.MolecularDataFilter;
import org.cbioportal.legacy.web.parameter.MolecularDataMultipleStudyFilter;
import org.cbioportal.legacy.web.parameter.Projection;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@PublicApi
@RestController
@RequestMapping("/api")
@Validated
@Tag(name = PublicApiTags.MOLECULAR_DATA, description = " ")
public class ColumnStoreMolecularDataController {

  private final FetchGeneMolecularDataUseCase fetchGeneMolecularDataUseCase;

  public ColumnStoreMolecularDataController(
      FetchGeneMolecularDataUseCase fetchGeneMolecularDataUseCase) {
    this.fetchGeneMolecularDataUseCase = fetchGeneMolecularDataUseCase;
  }

  @PreAuthorize(
      "hasPermission(#molecularProfileId, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-profiles/{molecularProfileId}/molecular-data/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch molecular data in a molecular profile")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(
                      schema = @Schema(implementation = NumericGeneMolecularDataDTO.class))))
  public ResponseEntity<List<NumericGeneMolecularDataDTO>> fetchAllMolecularDataInMolecularProfile(
      @Parameter(
              required = true,
              description = "Molecular Profile ID e.g. acc_tcga_rna_seq_v2_mrna")
          @PathVariable
          String molecularProfileId,
      @Parameter(
              required = true,
              description = "List of Sample IDs/Sample List ID and Entrez Gene IDs")
          @Valid
          @RequestBody
          MolecularDataFilter molecularDataFilter,
      @Parameter(description = "Level of detail of the response")
          @RequestParam(defaultValue = "SUMMARY")
          Projection projection)
      throws MolecularProfileNotFoundException {

    boolean includeGene = projection == Projection.DETAILED;
    List<GeneMolecularData> molecularData;
    if (molecularDataFilter.getSampleListId() != null) {
      molecularData =
          fetchGeneMolecularDataUseCase.executeBySampleList(
              molecularProfileId,
              molecularDataFilter.getSampleListId(),
              molecularDataFilter.getEntrezGeneIds(),
              includeGene);
    } else {
      molecularData =
          fetchGeneMolecularDataUseCase.executeBySampleIds(
              molecularProfileId,
              molecularDataFilter.getSampleIds(),
              molecularDataFilter.getEntrezGeneIds(),
              includeGene);
    }
    List<NumericGeneMolecularDataDTO> result =
        NumericGeneMolecularDataMapper.INSTANCE.toDtos(filterNonNumberMolecularData(molecularData));

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  // Not subject to InvolvedCancerStudyExtractorInterceptor (column-store controllers do their own
  // authorization), so the study check is done by the permission evaluator on the request body.
  @PreAuthorize(
      "hasPermission(#molecularDataMultipleStudyFilter, 'MolecularDataMultipleStudyFilter', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/molecular-data/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch molecular data")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(
                      schema = @Schema(implementation = NumericGeneMolecularDataDTO.class))))
  public ResponseEntity<List<NumericGeneMolecularDataDTO>>
      fetchMolecularDataInMultipleMolecularProfiles(
          @Parameter(
                  required = true,
                  description =
                      "List of Molecular Profile ID and Sample ID pairs or List of Molecular"
                          + "Profile IDs and Entrez Gene IDs")
              @Valid
              @RequestBody
              MolecularDataMultipleStudyFilter molecularDataMultipleStudyFilter,
          @Parameter(description = "Level of detail of the response")
              @RequestParam(defaultValue = "SUMMARY")
              Projection projection) {

    boolean includeGene = projection == Projection.DETAILED;
    List<GeneMolecularData> molecularData;
    if (molecularDataMultipleStudyFilter.getMolecularProfileIds() != null) {
      molecularData =
          fetchGeneMolecularDataUseCase.executeInMultipleProfiles(
              molecularDataMultipleStudyFilter.getMolecularProfileIds(),
              null,
              molecularDataMultipleStudyFilter.getEntrezGeneIds(),
              includeGene);
    } else {
      List<String> molecularProfileIds = new ArrayList<>();
      List<String> sampleIds = new ArrayList<>();
      for (SampleMolecularIdentifier identifier :
          molecularDataMultipleStudyFilter.getSampleMolecularIdentifiers()) {
        molecularProfileIds.add(identifier.getMolecularProfileId());
        sampleIds.add(identifier.getSampleId());
      }
      molecularData =
          fetchGeneMolecularDataUseCase.executeInMultipleProfiles(
              molecularProfileIds,
              sampleIds,
              molecularDataMultipleStudyFilter.getEntrezGeneIds(),
              includeGene);
    }

    List<NumericGeneMolecularDataDTO> result =
        NumericGeneMolecularDataMapper.INSTANCE.toDtos(filterNonNumberMolecularData(molecularData));

    if (projection == Projection.META) {
      HttpHeaders responseHeaders = new HttpHeaders();
      responseHeaders.add(HeaderKeyConstants.TOTAL_COUNT, String.valueOf(result.size()));
      return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  private List<GeneMolecularData> filterNonNumberMolecularData(
      List<GeneMolecularData> geneMolecularDataList) {
    return geneMolecularDataList.stream()
        .filter(g -> NumberUtils.isCreatable(g.getValue()))
        .toList();
  }
}
