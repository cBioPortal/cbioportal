package org.cbioportal.legacy.service.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FisherExactTestCalculatorTest {

  private final FisherExactTestCalculator calculator = new FisherExactTestCalculator();

  @Test
  public void aurkaPValueShouldBeInvariantWhenGroupsAreSwapped() {
    assertGroupOrderInvariant(299, 1, 298, 2, 1.0, 1e-12);
  }

  @Test
  public void abl1PValueShouldBeInvariantWhenGroupsAreSwapped() {
    assertGroupOrderInvariant(296, 4, 298, 2, 0.6859270045884647, 1e-12);
  }

  @Test
  public void egfrPValueShouldBeInvariantWhenGroupsAreSwapped() {
    assertGroupOrderInvariant(265, 35, 285, 15, 0.00458281202627164, 1e-12);
  }

  private void assertGroupOrderInvariant(
      int a, int b, int c, int d, double expected, double delta) {

    double pValue = calculator.getTwoTailedPValue(a, b, c, d);
    double swappedPValue = calculator.getTwoTailedPValue(c, d, a, b);

    assertEquals(expected, pValue, delta);
    assertEquals(expected, swappedPValue, delta);
    assertEquals(pValue, swappedPValue, delta);

    assertTrue(pValue <= 1.0);
    assertTrue(swappedPValue <= 1.0);
  }
}
