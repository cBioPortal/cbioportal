package org.cbioportal.web.util;

import static org.junit.Assert.*;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(0));
        assertEquals(decList(-60, -50, -40), boundaries);
    }

    @Test
    public void rangeLargerThanAnchor() {
        List<BigDecimal> sortedNumericalValues =
            Arrays.asList(new BigDecimal(-62), new BigDecimal(-38));
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(-70));
        assertEquals(decList(-60, -50, -40), boundaries);
    }

    @Test
    public void rangeSpansAnchor() {
        List<BigDecimal> sortedNumericalValues =
            Arrays.asList(new BigDecimal(-62), new BigDecimal(-38));
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(-20));
        assertEquals(decList(-60, -50, -40), boundaries);
    }

    @Test
    public void rangeSmallerThanAnchorPositiveRange() {
        List<BigDecimal> sortedNumericalValues =
            Arrays.asList(new BigDecimal(38), new BigDecimal(62));
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(100));
        assertEquals(decList(40, 50, 60), boundaries);
    }

    @Test
    public void rangeLargerThanAnchorPositiveRange() {
        List<BigDecimal> sortedNumericalValues =
            Arrays.asList(new BigDecimal(38), new BigDecimal(62));
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(0));
        assertEquals(decList(40, 50, 60), boundaries);
    }

    @Test
    public void rangeSpansAnchorPositiveRange() {
        List<BigDecimal> sortedNumericalValues =
            Arrays.asList(new BigDecimal(38), new BigDecimal(62));
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(40));
        assertEquals(decList(40, 50, 60), boundaries);
    }

    @Test
    public void singleValueNoBoundaries() {
        List<BigDecimal> sortedNumericalValues =
            Arrays.asList(new BigDecimal(38));
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(40));
        assertEquals(decList(), boundaries);
    }

    @Test
    public void emptyNumericalValues() {
        List<BigDecimal> sortedNumericalValues = new ArrayList<>();
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(sortedNumericalValues, new BigDecimal(10),
                new BigDecimal(40));
        assertNull(boundaries);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullCustomBinsArg() {
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(null, new BigDecimal(10),
                new BigDecimal(40));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullBinSizeArg() {
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(Collections.emptyList(), null,
                new BigDecimal(40));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullAnchorPointArg() {
        List<BigDecimal> boundaries =
            dataBinHelper.generateBins(Collections.emptyList(), new BigDecimal(10),
               null);
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
        final List<BigDecimal> expected = Arrays.asList(new BigDecimal("2.5"), new BigDecimal("5"), new BigDecimal("7.5"));
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
        final List<BigDecimal> expected = Arrays.asList(new BigDecimal("2.5"), new BigDecimal("4.5"), new BigDecimal("6.5"));
        final List<BigDecimal> observed = dataBinHelper.calcQuartileBoundaries(values);
        assertEquals(3, observed.size());
        assertEquals(0, observed.get(0).compareTo(expected.get(0)));
        assertEquals(0, observed.get(1).compareTo(expected.get(1)));
        assertEquals(0, observed.get(2).compareTo(expected.get(2)));
    }

    @Test
    public void testCalcInterquartileRangeApproximation() {
        List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(new BigDecimal("3"), dataBinHelper.calcInterquartileRangeApproximation(values).lowerEndpoint());
        assertEquals(new BigDecimal("7"), dataBinHelper.calcInterquartileRangeApproximation(values).upperEndpoint());
    }

    @Test
    public void testCalsIqrUnevenSeries() {
        List<BigDecimal> values = decList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
        assertEquals(new BigDecimal("3"), dataBinHelper.calcInterquartileRangeApproximation(values).lowerEndpoint());
        assertEquals(new BigDecimal("9"), dataBinHelper.calcInterquartileRangeApproximation(values).upperEndpoint());
    }
    
    private List<BigDecimal> decList(int... numbers) {
        return Arrays.stream(numbers).mapToObj(BigDecimal::new).collect(Collectors.toList());
    }
}