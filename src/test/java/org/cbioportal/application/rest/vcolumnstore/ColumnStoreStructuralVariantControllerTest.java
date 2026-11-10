package org.cbioportal.application.rest.vcolumnstore;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.cbioportal.domain.structural_variant.usecase.FetchStructuralVariantsUseCase;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.utils.Encoder;
import org.cbioportal.legacy.web.config.TestConfig;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.cbioportal.legacy.web.parameter.StructuralVariantFilter;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {ColumnStoreStructuralVariantController.class, TestConfig.class})
public class ColumnStoreStructuralVariantControllerTest {

  private static final String URL = "/api/structural-variant/fetch";

  @MockitoBean private FetchStructuralVariantsUseCase fetchStructuralVariantsUseCase;

  @Autowired private MockMvc mockMvc;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @WithMockUser
  public void fetchKeepsTheLegacyResponseShape() throws Exception {
    when(fetchStructuralVariantsUseCase.execute(any())).thenReturn(List.of(variant()));

    StructuralVariantFilter filter = new StructuralVariantFilter();
    filter.setMolecularProfileIds(List.of("prof"));
    filter.setEntrezGeneIds(List.of(673));

    mockMvc
        .perform(post(filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value("prof"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value("s1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].patientId").value("p1"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].studyId").value("study"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].site1HugoSymbol").value("KIAA1549"))
        .andExpect(MockMvcResultMatchers.jsonPath("$[0].site2EntrezGeneId").value(673))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniqueSampleKey")
                .value(Encoder.calculateBase64("s1", "study")))
        .andExpect(
            MockMvcResultMatchers.jsonPath("$[0].uniquePatientKey")
                .value(Encoder.calculateBase64("p1", "study")));

    verify(fetchStructuralVariantsUseCase).execute(any(StructuralVariantFilter.class));
  }

  @Test
  @WithMockUser
  public void fetchAcceptsSampleMolecularIdentifiers() throws Exception {
    when(fetchStructuralVariantsUseCase.execute(any())).thenReturn(List.of());

    SampleMolecularIdentifier identifier = new SampleMolecularIdentifier();
    identifier.setMolecularProfileId("prof");
    identifier.setSampleId("s1");
    StructuralVariantFilter filter = new StructuralVariantFilter();
    filter.setSampleMolecularIdentifiers(List.of(identifier));

    mockMvc
        .perform(post(filter))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));
  }

  @Test
  @WithMockUser
  public void filterWithBothOrNeitherIdentifierKindIsRejected() throws Exception {
    mockMvc
        .perform(post(new StructuralVariantFilter()))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());

    StructuralVariantFilter both = new StructuralVariantFilter();
    both.setMolecularProfileIds(List.of("prof"));
    both.setSampleMolecularIdentifiers(List.of(new SampleMolecularIdentifier()));
    mockMvc.perform(post(both)).andExpect(MockMvcResultMatchers.status().isBadRequest());

    verifyNoInteractions(fetchStructuralVariantsUseCase);
  }

  private RequestBuilder post(StructuralVariantFilter filter) throws Exception {
    return MockMvcRequestBuilders.post(URL)
        .with(csrf())
        .accept(MediaType.APPLICATION_JSON)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(filter));
  }

  private StructuralVariant variant() {
    StructuralVariant variant = new StructuralVariant();
    variant.setMolecularProfileId("prof");
    variant.setSampleId("s1");
    variant.setPatientId("p1");
    variant.setStudyId("study");
    variant.setSite1EntrezGeneId(57670);
    variant.setSite1HugoSymbol("KIAA1549");
    variant.setSite2EntrezGeneId(673);
    variant.setSite2HugoSymbol("BRAF");
    return variant;
  }
}
