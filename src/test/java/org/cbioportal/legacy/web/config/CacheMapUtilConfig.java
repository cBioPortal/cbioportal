/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.web.config;

import org.cbioportal.legacy.persistence.CancerTypeRepository;
import org.cbioportal.legacy.persistence.GenericAssayRepository;
import org.cbioportal.legacy.persistence.MolecularProfileRepository;
import org.cbioportal.legacy.persistence.PatientRepository;
import org.cbioportal.legacy.persistence.SampleListRepository;
import org.cbioportal.legacy.persistence.StudyRepository;
import org.cbioportal.legacy.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.legacy.service.StaticDataTimestampService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author ochoaa
 */
@TestConfiguration
public class CacheMapUtilConfig {
  @Bean
  public CacheMapUtil cacheMapUtil() {
    return Mockito.mock(CacheMapUtil.class);
  }

  @Bean
  public PatientRepository patientRepository() {
    return Mockito.mock(PatientRepository.class);
  }

  @Bean
  public CancerTypeRepository cancerTypeRepository() {
    return Mockito.mock(CancerTypeRepository.class);
  }

  @Bean
  public StudyRepository studyRepository() {
    return Mockito.mock(StudyRepository.class);
  }

  @Bean
  public MolecularProfileRepository molecularProfileRepository() {
    return Mockito.mock(MolecularProfileRepository.class);
  }

  @Bean
  public SampleListRepository sampleListRepository() {
    return Mockito.mock(SampleListRepository.class);
  }

  @Bean
  public GenericAssayRepository genericAssayRepository() {
    return Mockito.mock(GenericAssayRepository.class);
  }

  @Bean
  public StaticDataTimestampService staticDataTimestampService() {
    return Mockito.mock(StaticDataTimestampService.class);
  }
}
