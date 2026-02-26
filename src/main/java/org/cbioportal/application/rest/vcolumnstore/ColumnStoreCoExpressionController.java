package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import org.cbioportal.domain.coexpression.usecase.FetchCoExpressionsUseCase;
import org.cbioportal.legacy.model.CoExpression;
import org.cbioportal.legacy.service.exception.GeneNotFoundException;
import org.cbioportal.legacy.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.legacy.web.parameter.CoExpressionFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/column-store")
@Validated
public class ColumnStoreCoExpressionController {

  private final FetchCoExpressionsUseCase fetchCoExpressionsUseCase;

  public ColumnStoreCoExpressionController(FetchCoExpressionsUseCase fetchCoExpressionsUseCase) {
    this.fetchCoExpressionsUseCase = fetchCoExpressionsUseCase;
  }

  @Hidden
  @PreAuthorize(
      "hasPermission(#molecularProfileIdA, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ) and hasPermission(#molecularProfileIdB, 'MolecularProfileId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @PostMapping(
      value = "/molecular-profiles/co-expressions/fetch",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<List<CoExpression>> fetchCoExpressions(
      @Parameter(required = true, description = "Molecular Profile ID A") @RequestParam
          String molecularProfileIdA,
      @Parameter(required = true, description = "Molecular Profile ID B") @RequestParam
          String molecularProfileIdB,
      @Parameter(required = true, description = "Co-Expression Filter") @Valid @RequestBody
          CoExpressionFilter coExpressionFilter,
      @Parameter(description = "Threshold") @RequestParam(defaultValue = "0.3") Double threshold)
      throws MolecularProfileNotFoundException, GeneNotFoundException {

    if (coExpressionFilter.getEntrezGeneId() == null) {
      return new ResponseEntity<>(List.of(), HttpStatus.OK);
    }

    List<CoExpression> coExpressionList;
    if (coExpressionFilter.getSampleListId() != null) {
      coExpressionList =
          fetchCoExpressionsUseCase.execute(
              molecularProfileIdA,
              molecularProfileIdB,
              coExpressionFilter.getEntrezGeneId(),
              coExpressionFilter.getSampleListId(),
              threshold);
    } else {
      coExpressionList =
          fetchCoExpressionsUseCase.execute(
              molecularProfileIdA,
              molecularProfileIdB,
              coExpressionFilter.getEntrezGeneId(),
              coExpressionFilter.getSampleIds(),
              threshold);
    }

    return new ResponseEntity<>(coExpressionList, HttpStatus.OK);
  }
}
