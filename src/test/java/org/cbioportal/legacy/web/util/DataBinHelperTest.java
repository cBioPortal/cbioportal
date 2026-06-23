package org.cbioportal.legacy.web.util;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.DataBin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DataBinHelperTest {

  private DataBinHelper dataBinHelper;

  @Before
  public void setUp() throws Exception {
    dataBinHelper = new DataBinHelper();
  }

  @Test
  public void rangeSmallerThanAnchor() {
    List<BigDecimal> sortedNumericalValues =
        Arrays.asList(new BigDecimal(-62), new BigDecimal(-38));
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(0));
    assertEquals(decList(-60, -50, -40), boundaries);
  }

  @Test
  public void rangeLargerThanAnchor() {
    List<BigDecimal> sortedNumericalValues =
        Arrays.asList(new BigDecimal(-62), new BigDecimal(-38));
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(-70));
    assertEquals(decList(-60, -50, -40), boundaries);
  }

  @Test
  public void rangeSpansAnchor() {
    List<BigDecimal> sortedNumericalValues =
        Arrays.asList(new BigDecimal(-62), new BigDecimal(-38));
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(-20));
    assertEquals(decList(-60, -50, -40), boundaries);
  }

  @Test
  public void rangeSmallerThanAnchorPositiveRange() {
    List<BigDecimal> sortedNumericalValues = Arrays.asList(new BigDecimal(38), new BigDecimal(62));
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(100));
    assertEquals(decList(40, 50, 60), boundaries);
  }

  @Test
  public void rangeLargerThanAnchorPositiveRange() {
    List<BigDecimal> sortedNumericalValues = Arrays.asList(new BigDecimal(38), new BigDecimal(62));
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(0));
    assertEquals(decList(40, 50, 60), boundaries);
  }

  @Test
  public void rangeSpansAnchorPositiveRange() {
    List<BigDecimal> sortedNumericalValues = Arrays.asList(new BigDecimal(38), new BigDecimal(62));
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(40));
    assertEquals(decList(40, 50, 60), boundaries);
  }

  @Test
  public void singleValueNoBoundaries() {
    List<BigDecimal> sortedNumericalValues = Arrays.asList(new BigDecimal(38));
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(40));
    assertEquals(decList(), boundaries);
  }

  @Test
  public void emptyNumericalValues() {
    List<BigDecimal> sortedNumericalValues = new ArrayList<>();
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10), new BigDecimal(40));
    assertNull(boundaries);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullCustomBinsArg() {
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(null, new BigDecimal(10), new BigDecimal(40));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullBinSizeArg() {
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(Collections.emptyList(), null, new BigDecimal(40));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullAnchorPointArg() {
    List<BigDecimal> boundaries =
        dataBinHelper.generateBins(Collections.emptyList(), new BigDecimal(10), null);
  }

  @Test
  public void testMedianEvenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6);
    assertEquals(new BigDecimal("3.5"), dataBinHelper.calcMedian(values));
  }

  @Test
  public void testMedianUnevenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7);
    assertEquals(new BigDecimal("4"), dataBinHelper.calcMedian(values));
  }

  @Test
  public void testMedianNullSeries() {
    assertNull(dataBinHelper.calcMedian(null));
  }

  @Test
  public void testMedianEmptySeries() {
    assertNull(dataBinHelper.calcMedian(new ArrayList<>()));
  }

  @Test
  public void testQ1EvenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6);
    assertEquals(new BigDecimal("2"), dataBinHelper.calcQ1(values));
  }

  @Test
  public void testQ1UnevenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    assertEquals(new BigDecimal("3"), dataBinHelper.calcQ1(values));
  }

  @Test
  public void testQ1NullSeries() {
    assertNull(dataBinHelper.calcQ1(null));
  }

  @Test
  public void testQ1EmptySeries() {
    assertNull(dataBinHelper.calcQ1(new ArrayList<>()));
  }

  @Test
  public void testQ3EvenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6);
    assertEquals(new BigDecimal("5"), dataBinHelper.calcQ3(values));
  }

  @Test
  public void testQ3UnevenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(new BigDecimal("7.5"), dataBinHelper.calcQ3(values));
  }

  @Test
  public void testQ3NullSeries() {
    assertNull(dataBinHelper.calcQ3(null));
  }

  @Test
  public void testQ3EmptySeries() {
    assertNull(dataBinHelper.calcQ3(new ArrayList<>()));
  }

  @Test
  public void testValueCloseToQ1EvenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6);
    assertEquals(new BigDecimal("2"), dataBinHelper.valueCloseToQ1(values));
  }

  @Test
  public void testValueCloseToQ1UnevenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(new BigDecimal("3"), dataBinHelper.valueCloseToQ1(values));
  }

  @Test
  public void testValueCloseToQ1NullSeries() {
    assertNull(dataBinHelper.valueCloseToQ1(null));
  }

  @Test
  public void testValueCloseToQ1EmptySeries() {
    assertNull(dataBinHelper.valueCloseToQ1(new ArrayList<>()));
  }

  @Test
  public void testValueCloseToQ3EvenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6);
    assertEquals(new BigDecimal("5"), dataBinHelper.valueCloseToQ3(values));
  }

  @Test
  public void testValueCloseToQ3UnevenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    assertEquals(new BigDecimal("7"), dataBinHelper.valueCloseToQ3(values));
  }

  @Test
  public void testValueCloseToQ3NullSeries() {
    assertNull(dataBinHelper.valueCloseToQ3(null));
  }

  @Test
  public void testValueCloseToQ3EmptySeries() {
    assertNull(dataBinHelper.valueCloseToQ3(new ArrayList<>()));
  }

  @Test
  public void testCalcQuartileBoundariesUniqueSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    final List<BigDecimal> expected =
        Arrays.asList(new BigDecimal("2.5"), new BigDecimal("5"), new BigDecimal("7.5"));
    final List<BigDecimal> observed = dataBinHelper.calcQuartileBoundaries(values);
    assertEquals(3, observed.size());
    assertEquals(0, observed.get(0).compareTo(expected.get(0)));
    assertEquals(0, observed.get(1).compareTo(expected.get(1)));
    assertEquals(0, observed.get(2).compareTo(expected.get(2)));
  }

  @Test
  public void testCalcQuartileBoundariesIdenticalSeries() {
    List<BigDecimal> values = decList(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    final List<BigDecimal> observed = dataBinHelper.calcQuartileBoundaries(values);
    assertEquals(1, observed.size());
    assertEquals(0, observed.get(0).compareTo(new BigDecimal("1")));
  }

  @Test
  public void testCalcQuartileBoundariesIdenitcalUpperValues() {
    List<BigDecimal> values = decList(1, 1, 1, 10, 10, 10, 10, 10, 10);
    final List<BigDecimal> expected = Arrays.asList(new BigDecimal("1"), new BigDecimal("10"));
    final List<BigDecimal> observed = dataBinHelper.calcQuartileBoundaries(values);
    assertEquals(2, observed.size());
    assertEquals(0, observed.get(0).compareTo(expected.get(0)));
    assertEquals(0, observed.get(1).compareTo(expected.get(1)));
  }

  @Test
  public void testCalcQuartileBoundariesIdenticalLowerValues() {
    List<BigDecimal> values = decList(1, 1, 1, 1, 1, 1, 10, 10, 10);
    final List<BigDecimal> expected = Arrays.asList(new BigDecimal("1"), new BigDecimal("10"));
    final List<BigDecimal> observed = dataBinHelper.calcQuartileBoundaries(values);
    assertEquals(2, observed.size());
    assertEquals(0, observed.get(0).compareTo(expected.get(0)));
    assertEquals(0, observed.get(1).compareTo(expected.get(1)));
  }

  @Test
  public void testCalcQuartileBoundariesFractions() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8);
    final List<BigDecimal> expected =
        Arrays.asList(new BigDecimal("2.5"), new BigDecimal("4.5"), new BigDecimal("6.5"));
    final List<BigDecimal> observed = dataBinHelper.calcQuartileBoundaries(values);
    assertEquals(3, observed.size());
    assertEquals(0, observed.get(0).compareTo(expected.get(0)));
    assertEquals(0, observed.get(1).compareTo(expected.get(1)));
    assertEquals(0, observed.get(2).compareTo(expected.get(2)));
  }

  @Test
  public void testCalcInterquartileRangeApproximation() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8);
    assertEquals(
        new BigDecimal("3"),
        dataBinHelper.calcInterquartileRangeApproximation(values).lowerEndpoint());
    assertEquals(
        new BigDecimal("7"),
        dataBinHelper.calcInterquartileRangeApproximation(values).upperEndpoint());
  }

  @Test
  public void testCalsIqrUnevenSeries() {
    List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    assertEquals(
        new BigDecimal("3"),
        dataBinHelper.calcInterquartileRangeApproximation(values).lowerEndpoint());
    assertEquals(
        new BigDecimal("9"),
        dataBinHelper.calcInterquartileRangeApproximation(values).upperEndpoint());
  }

  private List<BigDecimal> decList(int... numbers) {
    return Arrays.stream(numbers).mapToObj(BigDecimal::new).collect(Collectors.toList());
  }

  // ── calcCounts tests ──────────────────────────────────────────────────────

  /** Helper: builds a DataBin with the given start/end (both inclusive). */
  private DataBin bin(Integer start, Integer end) {
    DataBin bin = new DataBin();
    bin.setStart(start == null ? null : new BigDecimal(start));
    bin.setEnd(end == null ? null : new BigDecimal(end));
    bin.setCount(0);
    return bin;
  }

  @Test
  public void calcCounts_emptyDataBins_doesNothing() {
    List<DataBin> bins = Collections.emptyList();
    List<BigDecimal> values = decList(1, 2, 3);
    DataBinHelper.calcCounts(bins, values);
    // Guard clause should return early without throwing or modifying values
    assertEquals("values must be unchanged when bins is empty", 3, values.size());
  }

  @Test
  public void calcCounts_emptyValues_doesNothing() {
    DataBin b = bin(0, 10);
    DataBinHelper.calcCounts(List.of(b), Collections.emptyList());
    assertEquals(0, b.getCount().intValue());
  }

  @Test
  public void calcCounts_valueInsideBin_counted() {
    // [0, 10] — value 5 should be counted
    DataBin b = bin(0, 10);
    DataBinHelper.calcCounts(List.of(b), decList(5));
    assertEquals(1, b.getCount().intValue());
  }

  @Test
  public void calcCounts_valueOnOpenLowerBoundary_notCounted() {
    // Normal bins use OPEN lower bound (exclusive start): (0, 10]
    // so value exactly at start=0 must NOT be counted.
    DataBin b = bin(0, 10);
    DataBinHelper.calcCounts(List.of(b), decList(0));
    assertEquals(0, b.getCount().intValue());
  }

  @Test
  public void calcCounts_valueJustAboveLowerBoundary_counted() {
    // (0, 10] — value 1 (strictly above open lower bound) must be counted.
    DataBin b = bin(0, 10);
    DataBinHelper.calcCounts(List.of(b), decList(1));
    assertEquals(1, b.getCount().intValue());
  }

  @Test
  public void calcCounts_valueOnClosedUpperBoundary_counted() {
    // [0, 10] — value exactly at upper boundary
    DataBin b = bin(0, 10);
    DataBinHelper.calcCounts(List.of(b), decList(10));
    assertEquals(1, b.getCount().intValue());
  }

  @Test
  public void calcCounts_valueOutsideAllBins_notCounted() {
    DataBin b = bin(0, 10);
    DataBinHelper.calcCounts(List.of(b), decList(99));
    assertEquals(0, b.getCount().intValue());
  }

  @Test
  public void calcCounts_adjacentBins_valueOnBoundary_countedOnce() {
    // [0,5] and [5,10] — value 5 must go to exactly one bin
    DataBin b1 = bin(0, 5);
    DataBin b2 = bin(5, 10);
    DataBinHelper.calcCounts(List.of(b1, b2), decList(5));
    int total = b1.getCount() + b2.getCount();
    assertEquals("value on shared boundary must be counted exactly once", 1, total);
  }

  @Test
  public void calcCounts_multipleValuesSpreadAcrossBins() {
    // three bins: [0,10], [10,20], [20,30]
    DataBin b1 = bin(0, 10);
    DataBin b2 = bin(10, 20);
    DataBin b3 = bin(20, 30);
    List<DataBin> bins = List.of(b1, b2, b3);
    DataBinHelper.calcCounts(bins, decList(5, 10, 15, 20, 25));
    int total = b1.getCount() + b2.getCount() + b3.getCount();
    assertEquals("all 5 values should be counted across the three bins", 5, total);
  }

  @Test
  public void calcCounts_discretePointBin_startEqualsEnd() {
    // A bin where start == end acts as a discrete point
    DataBin b = bin(7, 7);
    DataBinHelper.calcCounts(List.of(b), decList(7));
    assertEquals(1, b.getCount().intValue());
  }

  @Test
  public void calcCounts_discretePointBin_nonMatchingValue_notCounted() {
    DataBin b = bin(7, 7);
    DataBinHelper.calcCounts(List.of(b), decList(6, 8));
    assertEquals(0, b.getCount().intValue());
  }

  @Test
  public void calcCounts_openEndedUpperBin_countsValuesAboveStart() {
    // Bin with no upper bound and specialValue=">=" creates [100, +∞) — inclusive start.
    DataBin b = bin(100, null);
    b.setSpecialValue(">=");
    DataBinHelper.calcCounts(List.of(b), decList(100, 200, 999));
    assertEquals(3, b.getCount().intValue());
  }

  @Test
  public void calcCounts_openEndedUpperBin_noSpecialValue_excludesStart() {
    // Without specialValue, bin(100, null) creates (100, +∞) — exclusive start.
    DataBin b = bin(100, null);
    DataBinHelper.calcCounts(List.of(b), decList(100, 200, 999));
    assertEquals(2, b.getCount().intValue()); // 100 excluded, 200 and 999 included
  }

  @Test
  public void calcCounts_openEndedLowerBin_countsValuesBelowEnd() {
    // Bin with no lower bound (e.g. "< 10")
    DataBin b = bin(null, 10);
    DataBinHelper.calcCounts(List.of(b), decList(-99, 0, 5));
    assertEquals(3, b.getCount().intValue());
  }
}
