package org.cbioportal.persistence.helper;

import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.GenomicDataFilter;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StudyViewFilterHelperTest {

    // (-5, -1], (-1, 3], (3, 7] -> (-5, 7]
    @Test
    public void testMergeDataFilterNumericalContinuousValues() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue(BigDecimal.valueOf(-5), BigDecimal.valueOf(-1), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(-1), BigDecimal.valueOf(3), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(3), BigDecimal.valueOf(7), null));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal start = mergedDataFilterValues.getFirst().getStart();
        BigDecimal end = mergedDataFilterValues.getFirst().getEnd();
        assertEquals(0, BigDecimal.valueOf(-5).compareTo(start));
        assertEquals(0, BigDecimal.valueOf(7).compareTo(end));
    }

    // (-2.5, -2.25], (-2.25, -2], (-1.75, -1.5], (-1.5, -1.25] -> (-2.5, -2], (-1.75, -1.25]
    @Test
    public void testMergeDataFilterNumericalDiscontinuousValues() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.5), BigDecimal.valueOf(-2.25), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.25), BigDecimal.valueOf(-2), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(-1.75), BigDecimal.valueOf(-1.5), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(-1.5), BigDecimal.valueOf(-1.25), null));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal firstStart = mergedDataFilterValues.getFirst().getStart();
        BigDecimal firstEnd = mergedDataFilterValues.getFirst().getEnd();
        assertEquals(0, BigDecimal.valueOf(-2.5).compareTo(firstStart));
        assertEquals(0, BigDecimal.valueOf(-2).compareTo(firstEnd));

        BigDecimal secondStart = mergedDataFilterValues.get(1).getStart();
        BigDecimal secondEnd = mergedDataFilterValues.get(1).getEnd();
        assertEquals(0, BigDecimal.valueOf(-1.75).compareTo(secondStart));
        assertEquals(0, BigDecimal.valueOf(-1.25).compareTo(secondEnd));
    }

    // (null, -2.25], (-2.25, -2], (-2, null] -> (null, null]
    @Test
    public void testMergeDataFilterNumericalInfiniteValues() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue(null, BigDecimal.valueOf(-2.25), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.25), BigDecimal.valueOf(-2), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2), null, null));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal start = mergedDataFilterValues.getFirst().getStart();
        BigDecimal end = mergedDataFilterValues.getFirst().getEnd();
        assertNull(start);
        assertNull(end);
    }

    // (-2.5, -2.25], (-2.25, -2], "NA" -> "NA", (-2.5, -1.75]
    @Test
    public void testMergeDataFilterNumericalNonNumericalValues() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.5), BigDecimal.valueOf(-2.25), null));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.25), BigDecimal.valueOf(-2), null));
        values.add(new DataFilterValue(null, null, "NA"));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        String value = mergedDataFilterValues.getFirst().getValue();
        BigDecimal start = mergedDataFilterValues.get(1).getStart();
        BigDecimal end = mergedDataFilterValues.get(1).getEnd();
        assertEquals("NA", value);
        assertEquals(0, BigDecimal.valueOf(-2.5).compareTo(start));
        assertEquals(0, BigDecimal.valueOf(-2).compareTo(end));
    }
}
