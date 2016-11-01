/*
 * Copyright (c) 2016 Memorial Sloan Kettering Cancer Center.
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
package org.cbioportal.web.api;

import java.util.List;
import org.cbioportal.persistence.mybatis.CosmicCountMapper;
import org.cbioportal.persistence.mybatis.MutationMapper;
import org.cbioportal.service.CosmicCountService;
import org.cbioportal.service.MutationService;
import org.cbioportal.web.config.CustomObjectMapper;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.persistence.CancerTypeMapper;
import org.mskcc.cbio.portal.persistence.ClinicalDataMapper;
import org.mskcc.cbio.portal.persistence.ClinicalFieldMapper;
import org.mskcc.cbio.portal.persistence.EntityAttributeMapper;
import org.mskcc.cbio.portal.persistence.EntityMapper;
import org.mskcc.cbio.portal.persistence.GeneAliasMapper;
import org.mskcc.cbio.portal.persistence.GeneticProfileMapper;
import org.mskcc.cbio.portal.persistence.PatientMapper;
import org.mskcc.cbio.portal.persistence.ProfileDataMapper;
import org.mskcc.cbio.portal.persistence.SampleListMapper;
import org.mskcc.cbio.portal.persistence.SampleMapper;
import org.mskcc.cbio.portal.persistence.StudyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.mskcc.cbio.portal.persistence.GeneMapperLegacy;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"org.mskcc.cbio.portal.web.api", "org.mskcc.cbio.portal.persistence", "org.mskcc.cbio.portal.service"})
public class ApiControllerConfig extends WebMvcConfigurerAdapter {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        mappingJackson2HttpMessageConverter.setObjectMapper(new CustomObjectMapper());
        converters.add(mappingJackson2HttpMessageConverter);
    }
    @Bean
    public CosmicCountService cosmicCountService() {
	return Mockito.mock(CosmicCountService.class);
    }
    @Bean
    public CosmicCountMapper cosmicCountMapper() {
	return Mockito.mock(CosmicCountMapper.class);
    }
    @Bean
    public MutationService mutationService() {
        return Mockito.mock(MutationService.class);
    }
    @Bean
    public MutationMapper mutationMapper() {
        return Mockito.mock(MutationMapper.class);
    }
    @Bean
    public CancerTypeMapper cancerTypeMapper() {
        return Mockito.mock(CancerTypeMapper.class);
    }
    @Bean
    public ClinicalDataMapper clinicalDataMapper() {
        return Mockito.mock(ClinicalDataMapper.class);
    }
    @Bean
    public ClinicalFieldMapper clinicalFieldMapper() {
        return Mockito.mock(ClinicalFieldMapper.class);
    }
    @Bean
    public EntityAttributeMapper entityAttributeMapper() {
        return Mockito.mock(EntityAttributeMapper.class);
    }
    @Bean
    public EntityMapper entityMapper() {
        return Mockito.mock(EntityMapper.class);
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
    public GeneticProfileMapper geneticProfileMapper() {
        return Mockito.mock(GeneticProfileMapper.class);
    }
    @Bean
    public PatientMapper patientMapper() {
        return Mockito.mock(PatientMapper.class);
    }
    @Bean
    public ProfileDataMapper profileDataMapper() {
        return Mockito.mock(ProfileDataMapper.class);
    }
    @Bean
    public SampleListMapper sampleListMapper() {
        return Mockito.mock(SampleListMapper.class);
    }
    @Bean
    public SampleMapper sampleMapper() {
        return Mockito.mock(SampleMapper.class);
    }
    @Bean
    public StudyMapper studyMapper() {
        return Mockito.mock(StudyMapper.class);
    }
}