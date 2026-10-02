package org.cbioportal.application.rest.vcolumnstore;

import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.cbioportal.application.rest.error.GlobalExceptionHandler;
import org.cbioportal.domain.mutation.GenomicSimilarityResult;
import org.cbioportal.domain.mutation.PatientSimilarityScore;
import org.cbioportal.domain.mutation.usecase.GetPatientGenomicSimilarityUseCase;
import org.cbioportal.legacy.service.exception.PatientNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PatientGenomicSimilarityControllerTest {

  private static final String URL = "/api/studies/brca_tcga/patient-genomic-similarity";

  private GetPatientGenomicSimilarityUseCase useCase;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    useCase = mock(GetPatientGenomicSimilarityUseCase.class);
    mockMvc =
        MockMvcBuilders.standaloneSetup(new PatientGenomicSimilarityController(useCase))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  void returnsTheSimilarPatients() throws Exception {
    when(useCase.execute("brca_tcga", "brca_tcga_mutations", "P1", List.of("TP53", "KRAS"), 5))
        .thenReturn(
            new GenomicSimilarityResult(
                List.of("KRAS", "TP53"),
                List.of(new PatientSimilarityScore("P2", 0.5, List.of("TP53")))));

    postRequest(
            """
            {"molecularProfileId": "brca_tcga_mutations", "referencePatientId": "P1",
             "hugoGeneSymbols": ["TP53", "KRAS"], "topN": 5}""")
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.referencePatientMutatedGenes", contains("KRAS", "TP53")))
        .andExpect(jsonPath("$.similarPatients[0].patientId").value("P2"))
        .andExpect(jsonPath("$.similarPatients[0].similarityScore").value(0.5))
        .andExpect(jsonPath("$.similarPatients[0].commonMutatedGenes", contains("TP53")));
  }

  @Test
  void returnsTenPatientsByDefault() throws Exception {
    postRequest(
            """
            {"molecularProfileId": "brca_tcga_mutations", "referencePatientId": "P1",
             "hugoGeneSymbols": ["TP53"]}""")
        .andExpect(status().isOk());

    verify(useCase).execute("brca_tcga", "brca_tcga_mutations", "P1", List.of("TP53"), 10);
  }

  @Test
  void rejectsAnEmptyGeneList() throws Exception {
    postRequest(
            """
            {"molecularProfileId": "brca_tcga_mutations", "referencePatientId": "P1",
             "hugoGeneSymbols": []}""")
        .andExpect(status().isBadRequest());

    verifyNoInteractions(useCase);
  }

  @Test
  void rejectsABlankGeneSymbol() throws Exception {
    postRequest(
            """
            {"molecularProfileId": "brca_tcga_mutations", "referencePatientId": "P1",
             "hugoGeneSymbols": ["TP53", " "]}""")
        .andExpect(status().isBadRequest());

    verifyNoInteractions(useCase);
  }

  @Test
  void rejectsTopNAboveTheMaximum() throws Exception {
    postRequest(
            """
            {"molecularProfileId": "brca_tcga_mutations", "referencePatientId": "P1",
             "hugoGeneSymbols": ["TP53"], "topN": 501}""")
        .andExpect(status().isBadRequest());

    verifyNoInteractions(useCase);
  }

  @Test
  void returnsNotFoundForAnUnknownReferencePatient() throws Exception {
    when(useCase.execute("brca_tcga", "brca_tcga_mutations", "P9", List.of("TP53"), 10))
        .thenThrow(new PatientNotFoundException("brca_tcga", "P9"));

    postRequest(
            """
            {"molecularProfileId": "brca_tcga_mutations", "referencePatientId": "P9",
             "hugoGeneSymbols": ["TP53"]}""")
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Patient not found in study brca_tcga: P9"));
  }

  private ResultActions postRequest(String body) throws Exception {
    return mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(body));
  }
}
