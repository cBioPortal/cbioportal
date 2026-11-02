package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.cbioportal.domain.resource.ResourceTableMetadataResult;
import org.cbioportal.domain.resource.ResourceTableQuery;
import org.cbioportal.domain.resource.ResourceTableRow;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.usecase.GetResourceTableDataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableMetadataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableTabsUseCase;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@RequestMapping("/api")
@Tag(name = "Resource Table", description = "Server-side paginated resource table endpoints")
public class ResourceTableController {

  private final GetResourceTableTabsUseCase getTabsUseCase;
  private final GetResourceTableDataUseCase getDataUseCase;
  private final GetResourceTableMetadataUseCase getMetadataUseCase;

  public ResourceTableController(
      GetResourceTableTabsUseCase getTabsUseCase,
      GetResourceTableDataUseCase getDataUseCase,
      GetResourceTableMetadataUseCase getMetadataUseCase) {
    this.getTabsUseCase = getTabsUseCase;
    this.getDataUseCase = getDataUseCase;
    this.getMetadataUseCase = getMetadataUseCase;
  }

  @Hidden
  // Authorizes off the request body rather than the "involvedCancerStudies" request attribute.
  // InvolvedCancerStudyExtractorInterceptor skips this whole controller package -- column-store
  // endpoints authorize themselves -- so that attribute is never populated here and would always
  // be null, which hasPermission() denies. Safe navigation keeps a missing body a denial rather
  // than a 500. studyIds() is a record accessor, so it is called rather than read as a property.
  @PreAuthorize(
      "hasPermission(#request?.studyIds(), 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/resource-table/tabs/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Fetch resource table tab summaries")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = ResourceTableTab.class)))
  public ResponseEntity<List<ResourceTableTab>> fetchResourceTableTabs(
      @Valid @RequestBody(required = false) ResourceTabsRequest request) {
    List<ResourceTableTab> result = getTabsUseCase.execute(request);
    return ResponseEntity.ok(result);
  }

  @Hidden
  // See fetchResourceTableTabs above for why this authorizes off the body.
  @PreAuthorize(
      "hasPermission(#query?.studyIds(), 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/resource-table/query/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Fetch one page of resource table rows")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content =
          @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceTableRow.class))))
  public ResponseEntity<List<ResourceTableRow>> fetchResourceTableData(
      @Valid @RequestBody(required = false) ResourceTableQuery query) {
    return ResponseEntity.ok(getDataUseCase.execute(query));
  }

  @Hidden
  // See fetchResourceTableTabs above for why this authorizes off the body.
  @PreAuthorize(
      "hasPermission(#query?.studyIds(), 'Collection<CancerStudyId>', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(
      value = "/resource-table/metadata/fetch",
      method = RequestMethod.POST,
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Fetch the resource table's columns, filter options and counts",
      description =
          "Everything that does not change as the user pages. Fetch once per study, resource,"
              + " cohort, search and filter combination and reuse it across pages; pageNumber and"
              + " pageSize in the request are ignored.")
  @ApiResponse(
      responseCode = "200",
      description = "OK",
      content = @Content(schema = @Schema(implementation = ResourceTableMetadataResult.class)))
  public ResponseEntity<ResourceTableMetadataResult> fetchResourceTableMetadata(
      @Valid @RequestBody(required = false) ResourceTableQuery query) {
    return ResponseEntity.ok(getMetadataUseCase.execute(query));
  }
}
