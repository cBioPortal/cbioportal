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
    
    private List<BigDecimal> decList(int... numbers) {
        return Arrays.stream(numbers).mapToObj(BigDecimal::new).collect(Collectors.toList());
    }
}