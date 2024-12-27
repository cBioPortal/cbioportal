package org.cbioportal.test.integration;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.Sample;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.FederatedViewService;
import org.cbioportal.web.FederatedViewController;
import org.cbioportal.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringJUnit4ClassRunner.class)
//@WebMvcTest
//@AutoConfigureMockMvc // load the whole Spring Boot application context
@ContextConfiguration(classes = {
    FederatedViewController.class,
    FederatedViewService.class,
    ClinicalDataService.class,
    TestConfig.class
})
public class FederatedApiIntegrationTest {
}
