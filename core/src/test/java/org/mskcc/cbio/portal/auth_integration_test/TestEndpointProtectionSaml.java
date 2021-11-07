package org.mskcc.cbio.portal.auth_integration_test;

import org.cbioportal.security.spring.CancerStudyPermissionEvaluator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(
        initializers = TestEndpointProtectionSaml.TestApplicationContextInitializer.class,
        locations = {
                "classpath*:applicationContext-security.xml",
                "classpath*:applicationContext-dao-test.xml",
                "classpath*:applicationContext-web-test.xml",
                "classpath*:applicationContext-core.xml",
                "classpath*:applicationContext-business.xml"
        }
)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("saml")
public class TestEndpointProtectionSaml {

    public static class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            System.setProperty("authenticate", "saml");
            System.setProperty("saml.sp.metadata.entityid", "cbioportal");
            System.setProperty("saml.idp.metadata.location", Paths.get("classpath:/client-tailored-saml-idp-metadata.xml").toString() );
            System.setProperty("saml.idp.metadata.entityid", "https://localhost/auth/realms/cbio");
            System.setProperty("saml.sp.metadata.entitybaseurl", "#{null}");
            System.setProperty("saml.keystore.location", Paths.get("classpath:/samlKeystore.jks").toString());
            System.setProperty("saml.keystore.password", "P@ssword1");
            System.setProperty("saml.keystore.private-key.key", "secure-key");
            System.setProperty("saml.keystore.private-key.password", "P@ssword1");
            System.setProperty("saml.keystore.default-key", "secure-key");
            System.setProperty("saml.idp.comm.binding.settings", "defaultBinding");
            System.setProperty("saml.idp.comm.binding.type", "");
            System.setProperty("saml.idp.metadata.attribute.email", "email");
            System.setProperty("saml.custom.userservice.class", "org.cbioportal.security.spring.authentication.keycloak.SAMLUserDetailsServiceImpl");
            System.setProperty("saml.logout.local", "false");
            System.setProperty("saml.logout.url", "/");
            System.setProperty("saml.sp.metadata.wantAssertionSigned", "true");
            System.setProperty("google.analytics.tracking.code.api", "");
            System.setProperty("google.analytics.application.client.id", "");
        }
    }

    @Autowired
    private WebApplicationContext webAppContext;
    private MockMvc mockMvc;

    @Autowired(required = false)
    private DefaultMethodSecurityExpressionHandler expressionHandler;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .alwaysDo(print())
                .apply(springSecurity())
                .build();

        CancerStudyPermissionEvaluator pe = spy(CancerStudyPermissionEvaluator.class);
        expressionHandler.setPermissionEvaluator(pe);
        doReturn(true).when(pe).hasPermission(any(), eq("allowed_study"), eq("CancerStudyId"), eq("read"));
        doReturn(false).when(pe).hasPermission(any(), eq("denied_study"), eq("CancerStudyId"), eq("read"));
    }

    @Test
    public void studiesEndpointFail() throws Exception {
        mockMvc.perform(get("/studies"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:80/saml/discovery?entityID=cbioportal&returnIDParam=idp"));
    }

    @Test
    @WithMockUser
    public void studiesEndpointSuccess() throws Exception {
        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    public void fetchStudiesEndpointPreAutorizeDeny() throws Exception {
        String studyIds = "[ \"allowed_study\", \"denied_study\" ]";
        mockMvc.perform(post("/studies/fetch")
                .contentType(MediaType.APPLICATION_JSON).content(studyIds)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
    @Test
    @WithMockUser
    public void fetchStudiesEndpointPreAutorizeSuccess() throws Exception {
        String studyIds = "[ \"allowed_study\" ]";
        mockMvc.perform(post("/studies/fetch")
                .contentType(MediaType.APPLICATION_JSON).content(studyIds)
                .with(csrf()))
                .andExpect(status().isOk());
    }

}
