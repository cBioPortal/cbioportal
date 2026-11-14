package org.cbioportal.legacy.web.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.google.common.collect.Range;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.cbioportal.legacy.model.DataBin;
import org.junit.Test;

public class LinearDataBinnerTest {

  private static final BigDecimal EIGHTEEN = BigDecimal.valueOf(18);

  private final LinearDataBinner linearDataBinner = new LinearDataBinner();

  // ages 17 to 95: the first generated bin is [15, 20], which the age special case moves to
  // start from 18
  private final List<BigDecimal> ages =
      IntStream.rangeClosed(17, 95).mapToObj(BigDecimal::valueOf).toList();
  private final Range<BigDecimal> boxRange =
      Range.closed(BigDecimal.valueOf(17), BigDecimal.valueOf(95));

  @Test
  public void firstBinStartsAtEighteenForAgeAttributes() {
    for (String attributeId : List.of("AGE", "AGE_AT_DIAGNOSIS", "CURRENT_AGE_DEID")) {
      assertEquals(attributeId, 0, EIGHTEEN.compareTo(firstBinStart(attributeId)));
    }
  }

  @Test
  public void firstBinStartsAtEighteenForAttributesEndingWithAge() {
    for (String attributeId : List.of("DIAGNOSIS_AGE", "CURRENT_AGE", "DX_AGE")) {
      assertEquals(attributeId, 0, EIGHTEEN.compareTo(firstBinStart(attributeId)));
    }
  }

  @Test
  public void firstBinIsNotChangedForOtherAttributes() {
    for (String attributeId : List.of("TUMOR_SIZE", "TUMOR_STAGE")) {
      assertNotEquals(attributeId, 0, EIGHTEEN.compareTo(firstBinStart(attributeId)));
    }
  }

  private BigDecimal firstBinStart(String attributeId) {
    List<DataBin> dataBins =
        linearDataBinner.calculateDataBins(
            true, boxRange, ages, null, null, Optional.of(attributeId));
    return dataBins.get(0).getStart();
  }
}
