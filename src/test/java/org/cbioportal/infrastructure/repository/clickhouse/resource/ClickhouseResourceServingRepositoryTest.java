package org.cbioportal.infrastructure.repository.clickhouse.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.cbioportal.domain.resource.ResourceServing;
import org.junit.Test;

public class ClickhouseResourceServingRepositoryTest {

  private static final String KEY = "2351e12d4955";
  private static final String SEALED_SOURCE = "AAECAwQFBgcICQoLNpOTYnY8HVb-KcPzu1F5HPykr7D0YY_U";

  @Test
  public void returnsTheServingDataOfACompleteRow() {
    assertEquals(
        new ResourceServing("WSI_SAMPLE", KEY, SEALED_SOURCE, 128, 96),
        repositoryReturning(row()).getServing("study", "WSI_SAMPLE", KEY, "patient"));
  }

  @Test
  public void returnsNullWhenThereIsNoRow() {
    assertNull(repositoryReturning(null).getServing("study", "WSI_SAMPLE", KEY, "patient"));
  }

  @Test
  public void returnsNullForAnIncompleteRow() {
    Map<String, Object> noSealedSource = row();
    noSealedSource.put("sealed_source", "");
    Map<String, Object> noThumbnailSize = row();
    noThumbnailSize.put("thumbnail_height", null);

    for (Map<String, Object> row : new Map[] {noSealedSource, noThumbnailSize}) {
      assertNull(
          row.toString(),
          repositoryReturning(row).getServing("study", "WSI_SAMPLE", KEY, "patient"));
    }
  }

  private static ClickhouseResourceServingRepository repositoryReturning(Map<String, Object> row) {
    ClickhouseResourceServingMapper mapper = mock(ClickhouseResourceServingMapper.class);
    when(mapper.getServing("study", "WSI_SAMPLE", KEY, "patient")).thenReturn(row);
    return new ClickhouseResourceServingRepository(mapper);
  }

  private static Map<String, Object> row() {
    Map<String, Object> row = new HashMap<>();
    row.put("sealed_source", SEALED_SOURCE);
    row.put("thumbnail_width", 128);
    row.put("thumbnail_height", 96);
    return row;
  }
}
