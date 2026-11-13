package org.cbioportal.application.rest.vcolumnstore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import org.cbioportal.application.rest.availability.StudyUnavailableException;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.domain.cancerstudy.usecase.GetCancerStudyMetadataUseCase;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.web.config.annotation.InternalApi;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Answers whether the caller may read a study, for reverse proxies that authorize requests to other
 * services with a subrequest (nginx {@code auth_request}, ingress-nginx {@code auth-url}).
 *
 * <p>The answer is a bare status: 204 when the study is readable, 401 when the API requires
 * authentication and the request has none, and 403 otherwise. An unknown study, and a study being
 * (re)imported, are also 403: a proxy treats any other status as an error, and a distinct status
 * for unknown studies would reveal which studies exist.
 *
 * <p>Internal rather than public API: it serves reverse proxies in front of other services, not API
 * clients, which read studies through {@code /api/studies/{studyId}}.
 */
@InternalApi
@Tag(name = "Study Access", description = " ")
@RestController
@RequestMapping("/api")
public class StudyAccessController {

  /** The authenticated user, for the proxy to forward to the protected service. */
  public static final String USER_HEADER = "X-Auth-Request-User";

  /** The identifier of the study that was checked. */
  public static final String STUDY_HEADER = "X-Auth-Request-Study";

  private static final AuthenticationTrustResolver TRUST_RESOLVER =
      new AuthenticationTrustResolverImpl();

  private final GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase;
  private final CacheMapUtil cacheMapUtil;

  /** Whether study permissions are enforced, i.e. method security and its evaluator are active. */
  private final boolean permissionsEnforced;

  public StudyAccessController(
      GetCancerStudyMetadataUseCase getCancerStudyMetadataUseCase,
      CacheMapUtil cacheMapUtil,
      Optional<CancerStudyPermissionEvaluator> permissionEvaluator) {
    this.getCancerStudyMetadataUseCase = getCancerStudyMetadataUseCase;
    this.cacheMapUtil = cacheMapUtil;
    this.permissionsEnforced = permissionEvaluator.isPresent();
  }

  @PreAuthorize(
      "hasPermission(#studyId, 'CancerStudyId', T(org.cbioportal.legacy.utils.security.AccessLevel).READ)")
  @RequestMapping(method = RequestMethod.GET, value = "/studies/{studyId}/access")
  @Operation(
      description =
          "Check whether the caller may read a study. Intended for reverse-proxy authorization"
              + " subrequests; the response has no body.")
  @ApiResponse(
      responseCode = "204",
      description = "The caller may read the study",
      headers = {
        @Header(
            name = USER_HEADER,
            description =
                "Name of the authenticated user (Authentication.getName()); absent for anonymous"
                    + " callers"),
        @Header(name = STUDY_HEADER, description = "Identifier of the study that was checked")
      })
  @ApiResponse(
      responseCode = "401",
      description =
          "The API requires authentication and the request has none; never returned with"
              + " authenticate=optional_oauth2",
      content = @Content)
  @ApiResponse(
      responseCode = "403",
      description = "The caller may not read the study, or no such study is available",
      content = @Content)
  public ResponseEntity<Void> checkStudyAccess(
      @Parameter(required = true, description = "Study ID e.g. acc_tcga") @PathVariable
          String studyId) {
    String studyIdentifier =
        getCancerStudyMetadataUseCase
            .findStudyIdentifier(studyId)
            .orElseThrow(() -> new AccessDeniedException("Study access denied"));
    // CancerStudyPermissionEvaluator allows an id that is missing from its study permission map,
    // which is refreshed periodically (cache.cancer_study_permission.ttl_seconds). A study imported
    // since the last refresh is in the database but not yet in the map, so its groups have not been
    // checked: refuse it until the map includes it.
    if (permissionsEnforced
        && !cacheMapUtil.getCancerStudyPermissionMap().containsKey(studyIdentifier)) {
      throw new AccessDeniedException("Study access denied");
    }

    ResponseEntity.HeadersBuilder<?> response =
        ResponseEntity.noContent()
            .cacheControl(CacheControl.noStore())
            .header(STUDY_HEADER, studyIdentifier);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !TRUST_RESOLVER.isAnonymous(authentication)) {
      String userName = authentication.getName();
      // A protected service that trusts the header must never see an authenticated request
      // without a user.
      if (userName == null || userName.isBlank()) {
        throw new AccessDeniedException("Authenticated user has no name");
      }
      response.header(USER_HEADER, userName);
    }
    return response.build();
  }

  /**
   * Denials carry no body, unlike the JSON error of the global handler, since the caller is a proxy
   * that only reads the status.
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Void> handleAccessDenied() {
    return forbidden();
  }

  /**
   * A study being (re)imported is not readable right now. 403 rather than the usual 423, which a
   * proxy would report as a server error.
   */
  @ExceptionHandler(StudyUnavailableException.class)
  public ResponseEntity<Void> handleStudyUnavailable() {
    return forbidden();
  }

  private static ResponseEntity<Void> forbidden() {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).cacheControl(CacheControl.noStore()).build();
  }
}
