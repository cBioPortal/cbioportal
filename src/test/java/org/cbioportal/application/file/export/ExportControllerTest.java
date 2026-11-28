package org.cbioportal.application.file.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.cbioportal.application.file.export.services.ExportService;
import org.cbioportal.application.file.export.services.VirtualStudyExportDecoratorService;
import org.cbioportal.application.security.CancerStudyPermissionEvaluator;
import org.cbioportal.legacy.model.CancerStudy;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.service.VirtualStudyService;
import org.cbioportal.legacy.web.parameter.VirtualStudy;
import org.cbioportal.legacy.web.parameter.VirtualStudyData;
import org.cbioportal.legacy.web.parameter.VirtualStudySamples;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Checks, with real method security and the real {@link CancerStudyPermissionEvaluator}, which
 * study exports the endpoint authorizes. A virtual study id is not a key of the study permission
 * map, so access depends on its member studies.
 */
@SpringJUnitConfig(ExportControllerTest.TestConfig.class)
class ExportControllerTest {

  private static final String READABLE_STUDY = "readable_study";
  private static final String OTHER_READABLE_STUDY = "other_readable_study";
  private static final String UNREADABLE_STUDY = "unreadable_study";
  private static final String VIRTUAL_STUDY = "virtual_study";

  @Configuration
  @EnableMethodSecurity(proxyTargetClass = true)
  static class TestConfig {
    @Bean
    VirtualStudyService virtualStudyService() {
      return mock(VirtualStudyService.class);
    }

    @Bean
    ExportService exportService() {
      return mock(ExportService.class);
    }

    // The bean name the endpoint's @PreAuthorize expression refers to, as in ExportConfig
    @Bean
    VirtualStudyExportDecoratorService virtualStudyAwareExportService(
        VirtualStudyService virtualStudyService, ExportService exportService) {
      return new VirtualStudyExportDecoratorService(virtualStudyService, exportService);
    }

    @Bean
    ExportController exportController(VirtualStudyExportDecoratorService exportService) {
      return new ExportController(exportService);
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
      CacheMapUtil cacheMapUtil = mock(CacheMapUtil.class);
      when(cacheMapUtil.getCancerStudyPermissionMap())
          .thenReturn(
              Map.of(
                  READABLE_STUDY, study(READABLE_STUDY),
                  OTHER_READABLE_STUDY, study(OTHER_READABLE_STUDY),
                  UNREADABLE_STUDY, study(UNREADABLE_STUDY)));
      DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
      handler.setPermissionEvaluator(
          new CancerStudyPermissionEvaluator("cbioportal", "true", "", cacheMapUtil));
      return handler;
    }
  }

  @Autowired private ExportController controller;
  @Autowired private VirtualStudyService virtualStudyService;
  @Autowired private ExportService exportService;

  @BeforeEach
  void setUp() {
    reset(virtualStudyService, exportService);
    when(virtualStudyService.getVirtualStudyByIdIfExists(anyString())).thenReturn(Optional.empty());
    when(exportService.isStudyExportable(anyString())).thenReturn(true);
    SecurityContextHolder.getContext()
        .setAuthentication(
            new TestingAuthenticationToken(
                "user",
                null,
                "cbioportal:" + READABLE_STUDY.toUpperCase(),
                "cbioportal:" + OTHER_READABLE_STUDY.toUpperCase()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void exportsAVirtualStudyWhoseMemberStudiesAreAllReadable() {
    givenVirtualStudy(READABLE_STUDY, OTHER_READABLE_STUDY);
    assertEquals(HttpStatus.OK, controller.downloadStudyData(VIRTUAL_STUDY).getStatusCode());
  }

  @Test
  void deniesAVirtualStudyWithAnUnreadableMemberStudy() {
    givenVirtualStudy(READABLE_STUDY, UNREADABLE_STUDY);
    assertThrows(AccessDeniedException.class, () -> controller.downloadStudyData(VIRTUAL_STUDY));
  }

  @Test
  void deniesAVirtualStudyWithAnUnknownMemberStudy() {
    givenVirtualStudy(READABLE_STUDY, "unknown_member_study");
    assertThrows(AccessDeniedException.class, () -> controller.downloadStudyData(VIRTUAL_STUDY));
  }

  @Test
  void deniesAVirtualStudyWithoutMemberStudies() {
    givenVirtualStudy();
    assertThrows(AccessDeniedException.class, () -> controller.downloadStudyData(VIRTUAL_STUDY));
  }

  @Test
  void deniesAnIdThatIsNeitherAStudyNorAVirtualStudy() {
    assertThrows(AccessDeniedException.class, () -> controller.downloadStudyData("unknown_study"));
  }

  @Test
  void exportsAReadableStudy() {
    assertEquals(HttpStatus.OK, controller.downloadStudyData(READABLE_STUDY).getStatusCode());
  }

  @Test
  void deniesAnUnreadableStudy() {
    assertThrows(AccessDeniedException.class, () -> controller.downloadStudyData(UNREADABLE_STUDY));
  }

  private void givenVirtualStudy(String... memberStudyIds) {
    VirtualStudyData data = new VirtualStudyData();
    data.setStudies(
        Arrays.stream(memberStudyIds)
            .map(
                id -> {
                  VirtualStudySamples member = new VirtualStudySamples();
                  member.setId(id);
                  return member;
                })
            .collect(Collectors.toSet()));
    VirtualStudy virtualStudy = new VirtualStudy();
    virtualStudy.setData(data);
    when(virtualStudyService.getVirtualStudyByIdIfExists(VIRTUAL_STUDY))
        .thenReturn(Optional.of(virtualStudy));
  }

  private static CancerStudy study(String id) {
    CancerStudy study = new CancerStudy();
    study.setCancerStudyIdentifier(id);
    study.setGroups("");
    return study;
  }
}
