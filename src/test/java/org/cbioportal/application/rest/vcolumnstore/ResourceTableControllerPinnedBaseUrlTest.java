package org.cbioportal.application.rest.vcolumnstore;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.cbioportal.application.rest.util.PortalBaseUrl;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.cbioportal.domain.resource.usecase.GetResourceTableDataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableMetadataUseCase;
import org.cbioportal.domain.resource.usecase.GetResourceTableTabsUseCase;
import org.cbioportal.legacy.web.config.TestConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * With {@code resource.url_template.base_url} set, {@code ${base_url}} is that value whatever host,
 * context path or forwarded headers the request arrived with.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(
    classes = {
      ResourceTableController.class,
      GetResourceTableDataUseCase.class,
      PortalBaseUrl.class,
      TestConfig.class
    })
@TestPropertySource(properties = "resource.url_template.base_url=https://cbioportal.example.org/")
public class ResourceTableControllerPinnedBaseUrlTest {

  @MockitoBean private GetResourceTableTabsUseCase getResourceTableTabsUseCase;
  @MockitoBean private GetResourceTableMetadataUseCase getResourceTableMetadataUseCase;
  @MockitoBean private ResourceDataRepository resourceDataRepository;

  @Autowired private MockMvc mockMvc;

  @Before
  public void rows() {
    Mockito.when(resourceDataRepository.getResourceTableRows(Mockito.any()))
        .thenReturn(ResourceTableControllerUrlTemplateTest.storedRows());
  }

  @Test
  @WithMockUser
  public void expandsTheTemplateAgainstTheConfiguredBaseUrl() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    "http://internal-host:8080/cbioportal/api/resource-table/query/fetch")
                .contextPath("/cbioportal")
                .header("X-Forwarded-Host", "elsewhere.example.org")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(ResourceTableControllerUrlTemplateTest.query()))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].url")
                .value(
                    "https://cbioportal.example.org/wsi/patient/P-0000001?studyId=mskimpact"
                        + "&slideKey=2351e12d4955"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[1].url").value("https://example.com/own.svs"));
  }
}
