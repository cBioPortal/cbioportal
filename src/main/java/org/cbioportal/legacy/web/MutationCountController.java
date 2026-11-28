package org.cbioportal.legacy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.model.MutationCountByPosition;
import org.cbioportal.legacy.service.MutationService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.utils.security.AccessLevel;
import org.cbioportal.legacy.utils.security.PortalSecurityConfig;
import org.cbioportal.legacy.web.config.InternalApiTags;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.MutationPositionIdentifier;
import org.cbioportal.legacy.web.parameter.PagingConstants;
import org.cbioportal.legacy.web.parameter.Projection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@Validated
@Tag(name = InternalApiTags.MUTATIONS_COUNT, description = " ")
public class MutationCountController {

  public static final int MUTATION_MAX_PAGE_SIZE = 10000000;

  @Value("${authenticate:false}")
  private String authenticate;

  @Autowired private MutationService mutationService;

  @Autowired(required = false)
  private StudyService studyService;

  @RequestMapping(
      value = "/api/mutation-counts-by-position/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Fetch mutation counts in all studies by gene and position")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(
              array =
                  @ArraySchema(schema = @Schema(implementation = MutationCountByPosition.class))))
  public ResponseEntity<List<MutationCountByPosition>> fetchMutationCountsByPosition(
      @Parameter(required = true, description = "List of Mutation Position Identifiers")
          @Size(min = 1, max = MUTATION_MAX_PAGE_SIZE)
          @RequestBody
          List<MutationPositionIdentifier> mutationPositionIdentifiers) {

    List<Integer> entrezGeneIds = new ArrayList<>();
    List<Integer> proteinPosStarts = new ArrayList<>();
    List<Integer> proteinPosEnds = new ArrayList<>();
    for (MutationPositionIdentifier mutationPositionIdentifier : mutationPositionIdentifiers) {

      entrezGeneIds.add(mutationPositionIdentifier.getEntrezGeneId());
      proteinPosStarts.add(mutationPositionIdentifier.getProteinPosStart());
      proteinPosEnds.add(mutationPositionIdentifier.getProteinPosEnd());
    }

    if (PortalSecurityConfig.userAuthorizationEnabled(authenticate) && studyService != null) {
      List<String> studyIds =
          studyService
              .getAllStudies(
                  null,
                  Projection.SUMMARY.name(),
                  PagingConstants.MAX_PAGE_SIZE,
                  0,
                  null,
                  Direction.ASC.name(),
                  null,
                  AccessLevel.READ)
              .stream()
              .map(CancerStudy::getCancerStudyIdentifier)
              .collect(Collectors.toList());
      return new ResponseEntity<>(
          mutationService.fetchMutationCountsByPosition(
              entrezGeneIds, proteinPosStarts, proteinPosEnds, studyIds),
          HttpStatus.OK);
    }

    return new ResponseEntity<>(
        mutationService.fetchMutationCountsByPosition(
            entrezGeneIds, proteinPosStarts, proteinPosEnds),
        HttpStatus.OK);
  }
}
