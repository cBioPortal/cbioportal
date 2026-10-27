package org.cbioportal.infrastructure.repository.clickhouse.wsi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.cbioportal.domain.wsi.WsiSlideSource;
import org.junit.Test;

public class ClickhouseWsiSlideAccessRepositoryTest {

  private static final String SLIDE_KEY = "2351e12d4955";
  private static final String SEALED_SOURCE = "AAECAwQFBgcICQoLNpOTYnY8HVb-KcPzu1F5HPykr7D0YY_U";

  @Test
  public void returnsTheCapabilityInputsOfAServableRow() {
    WsiSlideSource source =
        repositoryReturning(row()).getSlideSource("study", "patient", SLIDE_KEY);

    assertEquals(new WsiSlideSource(SLIDE_KEY, SEALED_SOURCE, 128, 96), source);
  }

  @Test
  public void returnsNullWhenThereIsNoRow() {
    assertNull(repositoryReturning(null).getSlideSource("study", "patient", SLIDE_KEY));
  }

  @Test
  public void returnsNullForARowThatCannotServeTiles() {
    Map<String, Object> notServable = row();
    notServable.put("can_serve_tiles", false);
    Map<String, Object> noSealedSource = row();
    noSealedSource.put("sealed_source", "");
    Map<String, Object> noThumbnailSize = row();
    noThumbnailSize.put("thumbnail_width", null);

    for (Map<String, Object> row : new Map[] {notServable, noSealedSource, noThumbnailSize}) {
      assertNull(
          row.toString(), repositoryReturning(row).getSlideSource("study", "patient", SLIDE_KEY));
    }
  }

  private static ClickhouseWsiSlideAccessRepository repositoryReturning(Map<String, Object> row) {
    ClickhouseWsiSlideAccessMapper mapper = mock(ClickhouseWsiSlideAccessMapper.class);
    when(mapper.getSlideAccess("study", "patient", SLIDE_KEY)).thenReturn(row);
    return new ClickhouseWsiSlideAccessRepository(mapper);
  }

  private static Map<String, Object> row() {
    Map<String, Object> row = new HashMap<>();
    row.put("can_serve_tiles", true);
    row.put("sealed_source", SEALED_SOURCE);
    row.put("thumbnail_width", 128);
    row.put("thumbnail_height", 96);
    return row;
  }
}
