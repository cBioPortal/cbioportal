package org.cbioportal.application.security;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Architecture test that ensures all REST controller endpoints accessing study-specific data have
 * {@link PreAuthorize} annotations for authorization.
 *
 * <p>Controllers that serve only public/reference data, infrastructure endpoints, or external
 * proxies are listed in {@link #AUTHORIZED_EXCEPTIONS} and must include a justification comment.
 *
 * <p>If this test fails, either:
 *
 * <ul>
 *   <li>Add a {@code @PreAuthorize} annotation to the endpoint, OR
 *   <li>Add the controller to {@link #AUTHORIZED_EXCEPTIONS} (or the specific method to {@link
 *       #METHOD_EXCEPTIONS}) with a comment explaining why it does not need authorization
 * </ul>
 */
@AnalyzeClasses(packages = "org.cbioportal")
public class EndpointAuthorizationArchTest {

  /**
   * Controllers that intentionally do not require {@code @PreAuthorize}. Each entry must have a
   * comment explaining why the exception is justified.
   */
  private static final Set<String> AUTHORIZED_EXCEPTIONS =
      Set.of(
          // Public reference data
          "org.cbioportal.legacy.web.CancerTypeController",
          "org.cbioportal.legacy.web.GeneController",
          "org.cbioportal.legacy.web.GenePanelController",
          "org.cbioportal.legacy.web.GenesetController",
          "org.cbioportal.legacy.web.ReferenceGenomeGeneController",

          // Server infrastructure / health
          "org.cbioportal.legacy.web.InfoController",
          "org.cbioportal.legacy.web.ServerStatusController",
          "org.cbioportal.legacy.web.CacheController",
          "org.cbioportal.legacy.web.CacheStatsController",
          "org.cbioportal.legacy.web.StaticDataTimestampController",
          "org.cbioportal.legacy.web.TestController",

          // UI pages and documentation
          "org.cbioportal.legacy.web.IndexPageController",
          "org.cbioportal.legacy.web.LoginPageController",
          "org.cbioportal.legacy.web.SamlAndBasicLoginController",
          "org.cbioportal.legacy.web.DocRedirectController",
          "org.cbioportal.legacy.web.LegacyApiController",
          "org.cbioportal.application.documentation.ExternalPageController",

          // Explicitly public data
          "org.cbioportal.legacy.web.PublicVirtualStudiesController",

          // Authentication token management (secured by authentication, not study-level authz)
          "org.cbioportal.legacy.web.DataAccessTokenController",
          "org.cbioportal.legacy.web.OAuth2DataAccessTokenController",

          // User session management (secured by authentication, not study-level authz)
          "org.cbioportal.legacy.web.SessionServiceController",

          // External service proxies (no study data)
          "org.cbioportal.application.proxy.ProxyController",
          "org.cbioportal.application.proxy.LegacyProxyController",
          "org.cbioportal.legacy.web.MatchMinerController",

          // URL shortener (utility, no study data)
          "org.cbioportal.legacy.url_shortener.URLShortenerController",

          // TODO: These controllers access study-specific data and SHOULD have @PreAuthorize.
          // They are listed here temporarily to avoid breaking the build.
          // Each should be fixed and removed from this list.
          "org.cbioportal.legacy.web.MutationCountController", // no study IDs in request/response
          "org.cbioportal.legacy.web.MskEntityTranslationController", // study-specific lookups
          "org.cbioportal.legacy.web.GenericAssayController", // @PreAuthorize removed for perf
          "org.cbioportal.application.rest.vcolumnstore.ColumnStoreGenericAssayController", // @PreAuthorize removed for perf
          "org.cbioportal.application.rest.vcolumnstore.ColumnStoreStudyController" // study data
          );

  /**
   * Individual methods that are exempt from requiring {@code @PreAuthorize}. These are endpoints in
   * controllers that otherwise DO have authorization, but specific methods use service-layer
   * filtering instead. Format: "fully.qualified.ClassName.methodName"
   */
  private static final Set<String> METHOD_EXCEPTIONS =
      Set.of(
          // "Get all" endpoints that filter via service layer using AccessLevel/Authentication
          // TODO: Consider migrating these to @PreAuthorize for consistency
          "org.cbioportal.legacy.web.StudyController.getAllStudies",
          "org.cbioportal.legacy.web.StudyController.getTags",
          "org.cbioportal.legacy.web.PatientController.getAllPatients",
          "org.cbioportal.legacy.web.SampleController.getSamplesByKeyword",
          "org.cbioportal.legacy.web.SampleListController.getAllSampleLists",
          "org.cbioportal.legacy.web.ClinicalAttributeController.getAllClinicalAttributes",
          "org.cbioportal.legacy.web.MolecularProfileController.getAllMolecularProfiles",
          "org.cbioportal.application.rest.vcolumnstore.ColumnStoreSampleController.getSamplesByKeyword");

  private static boolean isEndpointMethod(JavaMethod method) {
    return method.isAnnotatedWith(RequestMapping.class)
        || method.isAnnotatedWith(GetMapping.class)
        || method.isAnnotatedWith(PostMapping.class)
        || method.isAnnotatedWith(PutMapping.class)
        || method.isAnnotatedWith(DeleteMapping.class)
        || method.isAnnotatedWith(PatchMapping.class);
  }

  @ArchTest
  static final ArchRule all_study_data_endpoints_must_have_authorization =
      methods()
          .that()
          .areDeclaredInClassesThat()
          .areAnnotatedWith(RestController.class)
          .should(
              new ArchCondition<>("have @PreAuthorize or be in AUTHORIZED_EXCEPTIONS") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                  if (!isEndpointMethod(method)) {
                    return;
                  }
                  String controllerName = method.getOwner().getName();
                  if (AUTHORIZED_EXCEPTIONS.contains(controllerName)) {
                    return;
                  }
                  String methodKey = controllerName + "." + method.getName();
                  if (METHOD_EXCEPTIONS.contains(methodKey)) {
                    return;
                  }
                  if (!method.isAnnotatedWith(PreAuthorize.class)) {
                    events.add(
                        SimpleConditionEvent.violated(
                            method,
                            String.format(
                                "Method %s in %s is a REST endpoint without @PreAuthorize. "
                                    + "Either add @PreAuthorize or add the controller to "
                                    + "AUTHORIZED_EXCEPTIONS or METHOD_EXCEPTIONS in EndpointAuthorizationArchTest "
                                    + "with a justification.",
                                method.getName(), controllerName)));
                  }
                }
              })
          .because(
              "all endpoints accessing study-specific data must have @PreAuthorize "
                  + "for study-level authorization (see AGENTS.md)");
}
