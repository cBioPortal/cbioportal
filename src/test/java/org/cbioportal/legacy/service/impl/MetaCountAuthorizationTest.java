package org.cbioportal.legacy.service.impl;

import java.io.Serializable;
import java.util.Arrays;
import org.cbioportal.legacy.model.Patient;
import org.cbioportal.legacy.model.meta.BaseMeta;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.cbioportal.legacy.service.PatientService;
import org.cbioportal.legacy.service.StudyService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Checks, with real method security and a real Spring proxy, that a META count covers only what the
 * caller may read: the same patients that the list method lets through.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MetaCountAuthorizationTest.TestConfig.class)
@TestPropertySource(properties = "authenticate=true")
public class MetaCountAuthorizationTest {

  @Configuration
  @EnableMethodSecurity
  static class TestConfig {
    @Bean
    PatientRepository patientRepository() {
      return Mockito.mock(PatientRepository.class);
    }

    @Bean
    StudyService studyService() {
      return Mockito.mock(StudyService.class);
    }

    @Bean
    PatientService patientService() {
      return new PatientServiceImpl();
    }

    // The caller may read study_a only
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
      DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
      handler.setPermissionEvaluator(
          new PermissionEvaluator() {
            @Override
            public boolean hasPermission(
                Authentication authentication, Object targetDomainObject, Object permission) {
              return targetDomainObject instanceof Patient patient
                  && "study_a".equals(patient.getCancerStudyIdentifier());
            }

            @Override
            public boolean hasPermission(
                Authentication authentication,
                Serializable targetId,
                String targetType,
                Object permission) {
              return false;
            }
          });
      return handler;
    }
  }

  @Autowired private PatientService patientService;
  @Autowired private PatientRepository patientRepository;

  @Before
  public void setUp() {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken("user", "password", "ROLE_USER"));
    Mockito.when(patientRepository.getAllPatients(null, "ID", null, null, null, null))
        .thenReturn(Arrays.asList(patient("study_a"), patient("study_a"), patient("study_b")));
    Mockito.when(patientRepository.getMetaPatients(null)).thenReturn(metaWithCount(3));
  }

  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void metaCountExcludesPatientsOfStudiesTheCallerMayNotRead() {
    BaseMeta result = patientService.getMetaPatients(null);

    Assert.assertEquals((Integer) 2, result.getTotalCount());
    Mockito.verify(patientRepository, Mockito.never()).getMetaPatients(Mockito.any());
  }

  private static Patient patient(String studyId) {
    Patient patient = new Patient();
    patient.setCancerStudyIdentifier(studyId);
    return patient;
  }

  private static BaseMeta metaWithCount(int count) {
    BaseMeta baseMeta = new BaseMeta();
    baseMeta.setTotalCount(count);
    return baseMeta;
  }
}
