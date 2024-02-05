/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cbioportal.web.config;

import org.cbioportal.persistence.CancerTypeRepository;
import org.cbioportal.persistence.GenericAssayRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.StudyRepository;
import org.cbioportal.persistence.cachemaputil.CacheMapUtil;
import org.cbioportal.service.StaticDataTimestampService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
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
