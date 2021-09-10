package org.cbioportal.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.cbioportal.model.MutationSpectrum;
import org.cbioportal.service.MutationSpectrumService;
import org.cbioportal.web.config.TestConfig;
import org.cbioportal.web.parameter.MutationSpectrumFilter;
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

@RunWith(SpringJUnit4ClassRunner.class)
@WebMvcTest
@ContextConfiguration(classes = {MutationSpectrumController.class, TestConfig.class})
public class MutationSpectrumControllerTest {

    private static final String TEST_MOLECULAR_PROFILE_ID = "test_molecular_profile_id";
    private static final String TEST_SAMPLE_ID_1 = "test_sample_id_1";
    private static final String TEST_SAMPLE_ID_2 = "test_sample_id_2";
    private static final int TEST_C_TO_A_1 = 1;
    private static final int TEST_C_TO_G_1 = 2;
    private static final int TEST_C_TO_T_1 = 3;
    private static final int TEST_T_TO_A_1 = 4;
    private static final int TEST_T_TO_C_1 = 5;
    private static final int TEST_T_TO_G_1 = 6;
    private static final int TEST_C_TO_A_2 = 3;
    private static final int TEST_C_TO_G_2 = 4;
    private static final int TEST_C_TO_T_2 = 5;
    private static final int TEST_T_TO_A_2 = 6;
    private static final int TEST_T_TO_C_2 = 1;
    private static final int TEST_T_TO_G_2 = 2;

    @MockBean
    private MutationSpectrumService mutationSpectrumService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void fetchMutationSpectrums() throws Exception {

        List<MutationSpectrum> mutationSpectrumList = new ArrayList<>();
        MutationSpectrum mutationSpectrum1 = new MutationSpectrum();
        mutationSpectrum1.setMolecularProfileId(TEST_MOLECULAR_PROFILE_ID);
        mutationSpectrum1.setSampleId(TEST_SAMPLE_ID_1);
        mutationSpectrum1.setCtoA(TEST_C_TO_A_1);
        mutationSpectrum1.setCtoG(TEST_C_TO_G_1);
        mutationSpectrum1.setCtoT(TEST_C_TO_T_1);
        mutationSpectrum1.setTtoA(TEST_T_TO_A_1);
        mutationSpectrum1.setTtoC(TEST_T_TO_C_1);
        mutationSpectrum1.setTtoG(TEST_T_TO_G_1);
        mutationSpectrumList.add(mutationSpectrum1);
        MutationSpectrum mutationSpectrum2 = new MutationSpectrum();
        mutationSpectrum2.setMolecularProfileId(TEST_MOLECULAR_PROFILE_ID);
        mutationSpectrum2.setSampleId(TEST_SAMPLE_ID_2);
        mutationSpectrum2.setCtoA(TEST_C_TO_A_2);
        mutationSpectrum2.setCtoG(TEST_C_TO_G_2);
        mutationSpectrum2.setCtoT(TEST_C_TO_T_2);
        mutationSpectrum2.setTtoA(TEST_T_TO_A_2);
        mutationSpectrum2.setTtoC(TEST_T_TO_C_2);
        mutationSpectrum2.setTtoG(TEST_T_TO_G_2);
        mutationSpectrumList.add(mutationSpectrum2);

        Mockito.when(mutationSpectrumService.fetchMutationSpectrums(TEST_MOLECULAR_PROFILE_ID,
            Arrays.asList(TEST_SAMPLE_ID_1, TEST_SAMPLE_ID_2))).thenReturn(mutationSpectrumList);

        MutationSpectrumFilter mutationSpectrumFilter = new MutationSpectrumFilter();
        mutationSpectrumFilter.setSampleIds(Arrays.asList(TEST_SAMPLE_ID_1, TEST_SAMPLE_ID_2));

        mockMvc.perform(MockMvcRequestBuilders.post(
            "/api/molecular-profiles/test_molecular_profile_id/mutation-spectrums/fetch").with(csrf())
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(mutationSpectrumFilter)))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].molecularProfileId").value(TEST_MOLECULAR_PROFILE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sampleId").value(TEST_SAMPLE_ID_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].CtoA").value(TEST_C_TO_A_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].CtoG").value(TEST_C_TO_G_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].CtoT").value(TEST_C_TO_T_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].TtoA").value(TEST_T_TO_A_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].TtoC").value(TEST_T_TO_C_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].TtoG").value(TEST_T_TO_G_1))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].molecularProfileId").value(TEST_MOLECULAR_PROFILE_ID))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sampleId").value(TEST_SAMPLE_ID_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].CtoA").value(TEST_C_TO_A_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].CtoG").value(TEST_C_TO_G_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].CtoT").value(TEST_C_TO_T_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].TtoA").value(TEST_T_TO_A_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].TtoC").value(TEST_T_TO_C_2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].TtoG").value(TEST_T_TO_G_2));
    }
}
