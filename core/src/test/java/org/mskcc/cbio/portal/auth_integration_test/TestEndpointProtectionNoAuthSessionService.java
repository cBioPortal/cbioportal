package org.mskcc.cbio.portal.auth_integration_test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(
        initializers = TestEndpointProtectionNoAuthSessionService.TestApplicationContextInitializer.class,
        locations = {
                "classpath*:applicationContext-dao-test.xml",
                "classpath*:applicationContext-web-test.xml",
                "classpath*:applicationContext-security.xml",
                "classpath*:applicationContext-core.xml",
                "classpath*:applicationContext-business.xml"
})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ActiveProfiles("noauthsessionservice")
public class TestEndpointProtectionNoAuthSessionService {

    public static class TestApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            System.setProperty("authenticate", "noauthsessionservice");
        }
    }

    @Autowired
    private WebApplicationContext webAppContext;
    private MockMvc mockMvc;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webAppContext)
                .alwaysDo(print())
                .apply(springSecurity())
                .build();
    }

    @Test
    public void studiesEndpointSuccess() throws Exception {
        mockMvc.perform(get("/studies"))
                .andExpect(status().isOk());
    }

}
