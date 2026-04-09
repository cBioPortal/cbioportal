package org.cbioportal.legacy.web.parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.legacy.web.config.CustomObjectMapper;
import org.junit.Test;

public class ResultsPageSettingsDeserializationTest {

  private final ObjectMapper objectMapper = new CustomObjectMapper();

  @Test
  public void shouldDeserializeClinicalTrackGapModeFromResultsViewSettings() throws Exception {
    String payload =
        """
        {
          "page": "results_view",
          "origin": ["study_es_0"],
          "clinicallist": [
            {
              "stableId": "SUBTYPE",
              "sortOrder": "ASC",
              "gapOn": true,
              "gapMode": "HIDE_GAPS"
            }
          ]
        }
        """;

    PageSettingsData pageSettingsData = objectMapper.readValue(payload, PageSettingsData.class);

    assertTrue(pageSettingsData instanceof ResultsPageSettings);

    ResultsPageSettings resultsPageSettings = (ResultsPageSettings) pageSettingsData;
    assertEquals(1, resultsPageSettings.getClinicallist().size());

    ClinicalTrackConfig clinicalTrackConfig = resultsPageSettings.getClinicallist().getFirst();
    assertEquals("SUBTYPE", clinicalTrackConfig.getStableId());
    assertEquals("ASC", clinicalTrackConfig.getSortOrder());
    assertEquals(Boolean.TRUE, clinicalTrackConfig.getGapOn());
    assertEquals("HIDE_GAPS", clinicalTrackConfig.getGapMode());
  }
}
