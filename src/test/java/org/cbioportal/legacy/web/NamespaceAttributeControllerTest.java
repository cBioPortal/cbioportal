package org.cbioportal.legacy.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.service.NamespaceAttributeService;
import org.cbioportal.legacy.web.config.TestConfig;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {NamespaceAttributeController.class, TestConfig.class})
public class NamespaceAttributeControllerTest {

    private static final String TEST_OUTER_KEY_1 = "test_outer_key_1";
    private static final String TEST_INNER_KEY_1 = "test_inner_key_1";
    private static final String TEST_OUTER_KEY_2 = "test_outer_key_2";
    private static final String TEST_INNER_KEY_2 = "test_inner_key_2";

    @MockBean
    private NamespaceAttributeService namespaceAttributeService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchNamespaceAttributes() throws Exception {

        List<NamespaceAttribute> namespaceAttributes = createExampleNamespaceAttributes();

        Mockito.when(namespaceAttributeService.fetchNamespaceAttributes(Mockito.anyList()))
            .thenReturn(namespaceAttributes);

        List<String> studyIds = new ArrayList<>();
        studyIds.add("study_id_1");
        studyIds.add("study_id_2");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/namespace-attributes/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(studyIds)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].outerKey").value(TEST_OUTER_KEY_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].innerKey").value(TEST_INNER_KEY_1));
    }

    @Test
    @WithMockUser
    public void fetchNamespaceAttributesEmptyBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/namespace-attributes/fetch").with(csrf())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private List<NamespaceAttribute> createExampleNamespaceAttributes() {

        List<NamespaceAttribute> namespaceAttributes = new ArrayList<>();
        NamespaceAttribute namespaceAttribute1 = new NamespaceAttribute();
        namespaceAttribute1.setOuterKey(TEST_OUTER_KEY_1);
        namespaceAttribute1.setInnerKey(TEST_INNER_KEY_1);
        namespaceAttributes.add(namespaceAttribute1);
        NamespaceAttribute namespaceAttribute2 = new NamespaceAttribute();
        namespaceAttribute2.setOuterKey(TEST_OUTER_KEY_2);
        namespaceAttribute2.setInnerKey(TEST_INNER_KEY_2);
        namespaceAttributes.add(namespaceAttribute2);
        return namespaceAttributes;
    }
}
