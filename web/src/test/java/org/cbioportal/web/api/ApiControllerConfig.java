package org.cbioportal.web.api;

import java.util.List;
import org.cbioportal.persistence.mybatis.MutationMapper;
import org.cbioportal.service.MutationService;
import org.cbioportal.persistence.mybatis.SVMapper;
import org.cbioportal.service.SVService;
import org.cbioportal.web.config.CustomObjectMapper;
import org.mockito.Mockito;
import org.mskcc.cbio.portal.persistence.CancerTypeMapper;
import org.mskcc.cbio.portal.persistence.ClinicalDataMapper;
import org.mskcc.cbio.portal.persistence.ClinicalFieldMapper;
import org.mskcc.cbio.portal.persistence.EntityAttributeMapper;
import org.mskcc.cbio.portal.persistence.EntityMapper;
import org.mskcc.cbio.portal.persistence.GeneAliasMapper;
import org.mskcc.cbio.portal.persistence.GeneMapper;
import org.mskcc.cbio.portal.persistence.GeneticProfileMapper;
import org.mskcc.cbio.portal.persistence.PatientMapper;
import org.mskcc.cbio.portal.persistence.ProfileDataMapper;
import org.mskcc.cbio.portal.persistence.SampleListMapper;
import org.mskcc.cbio.portal.persistence.SampleMapper;
import org.mskcc.cbio.portal.persistence.StudyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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
    public MutationService mutationService() {
        return Mockito.mock(MutationService.class);
    }
    @Bean
    public MutationMapper mutationMapper() {
        return Mockito.mock(MutationMapper.class);
    }
    @Bean
    public SVService svService(){
        return Mockito.mock(SVService.class);
    }
    @Bean
    public SVMapper svMapper(){
        return Mockito.mock(SVMapper.class);
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
    public GeneMapper geneMapper() {
        return Mockito.mock(GeneMapper.class);
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
