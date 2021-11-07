/*
 * Copyright (c) 2016 - 2018 Memorial Sloan Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

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
package org.mskcc.cbio.portal.auth_integration_test;

import org.cbioportal.service.CosmicCountService;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.persistence.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class LegacyApiControllerConfig {

    @Bean
    public CosmicCountService cosmicCountService() {
        return Mockito.mock(CosmicCountService.class);
    }
    @Bean
    public CosmicCountMapperLegacy cosmicCountMapper() {
        return Mockito.mock(CosmicCountMapperLegacy.class);
    }
    @Bean
    public MutationMapperLegacy mutationMapper() {
        return Mockito.mock(MutationMapperLegacy.class);
    }
    @Bean
    public CancerTypeMapperLegacy cancerTypeMapper() {
        return Mockito.mock(CancerTypeMapperLegacy.class);
    }
    @Bean
    public ClinicalDataMapperLegacy clinicalDataMapper() {
        return Mockito.mock(ClinicalDataMapperLegacy.class);
    }
    @Bean
    public ClinicalFieldMapper clinicalFieldMapper() {
        return Mockito.mock(ClinicalFieldMapper.class);
    }
    @Bean
    public GeneAliasMapper geneAliasMapper() {
        return Mockito.mock(GeneAliasMapper.class);
    }
    @Bean
    public GeneMapperLegacy geneMapperLegacy() {
        return Mockito.mock(GeneMapperLegacy.class);
    }
    @Bean
    public GeneticProfileMapperLegacy geneticProfileMapper() {
        return Mockito.mock(GeneticProfileMapperLegacy.class);
    }
    @Bean
    public PatientMapperLegacy patientMapper() {
        return Mockito.mock(PatientMapperLegacy.class);
    }
    @Bean
    public ProfileDataMapper profileDataMapper() {
        return Mockito.mock(ProfileDataMapper.class);
    }
    @Bean
    public SampleListMapperLegacy sampleListMapper() {
        return Mockito.mock(SampleListMapperLegacy.class);
    }
    @Bean
    public SampleMapperLegacy sampleMapper() {
        return Mockito.mock(SampleMapperLegacy.class);
    }
    @Bean
    public StudyMapperLegacy studyMapper() {
        return Mockito.mock(StudyMapperLegacy.class);
    }
}
