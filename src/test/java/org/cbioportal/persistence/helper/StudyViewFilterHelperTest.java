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
        values.add(new DataFilterValue(BigDecimal.valueOf(-5), BigDecimal.valueOf(-1)));
        values.add(new DataFilterValue(BigDecimal.valueOf(-1), BigDecimal.valueOf(3)));
        values.add(new DataFilterValue(BigDecimal.valueOf(3), BigDecimal.valueOf(7)));
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
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.5), BigDecimal.valueOf(-2.25)));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.25), BigDecimal.valueOf(-2)));
        values.add(new DataFilterValue(BigDecimal.valueOf(-1.75), BigDecimal.valueOf(-1.5)));
        values.add(new DataFilterValue(BigDecimal.valueOf(-1.5), BigDecimal.valueOf(-1.25)));
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
        values.add(new DataFilterValue(null, BigDecimal.valueOf(-2.25)));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.25), BigDecimal.valueOf(-2)));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2), null));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal start = mergedDataFilterValues.getFirst().getStart();
        BigDecimal end = mergedDataFilterValues.getFirst().getEnd();
        assertNull(start);
        assertNull(end);
    }

    // (-2.5, -2.25], (-2.25, -2], "NA" -> (-2.5, -1.75], "NA"
    // This test also ensures the non-numerical values gets moved to the end
    @Test
    public void testMergeDataFilterNumericalNonNumericalValues() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.5), BigDecimal.valueOf(-2.25)));
        values.add(new DataFilterValue(BigDecimal.valueOf(-2.25), BigDecimal.valueOf(-2)));
        values.add(new DataFilterValue("NA"));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal start = mergedDataFilterValues.getFirst().getStart();
        BigDecimal end = mergedDataFilterValues.getFirst().getEnd();
        String value = mergedDataFilterValues.get(1).getValue();
        assertEquals(0, BigDecimal.valueOf(-2.5).compareTo(start));
        assertEquals(0, BigDecimal.valueOf(-2).compareTo(end));
        assertEquals("NA", value);
    }

    // "NA" -> "NA"
    @Test
    public void testMergeDataFilterNonNumericalOnlyValues() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue("NA"));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        String value = mergedDataFilterValues.getFirst().getValue();
        assertEquals("NA", value);
    }

    // invalid null / empty -> unprocessed null / empty
    @Test
    public void testMergeDataFilterEmptyValidation() {
        List<GenomicDataFilter> mergedUninstantiatedFilters = StudyViewFilterHelper.mergeDataFilters(null);
        assertNull(mergedUninstantiatedFilters);

        List<GenomicDataFilter> mergedEmptyFilters = StudyViewFilterHelper.mergeDataFilters(new ArrayList<>());
        assertEquals(0, mergedEmptyFilters.size());
        
        List<GenomicDataFilter> uninstantiatedDataFilters = new ArrayList<>();
        uninstantiatedDataFilters.add(null);
        List<GenomicDataFilter> mergedUninstantiatedDataFilters = StudyViewFilterHelper.mergeDataFilters(uninstantiatedDataFilters);
        assertNull(mergedUninstantiatedDataFilters.getFirst());

        List<GenomicDataFilter> uninstantiatedValueFilters = new ArrayList<>();
        uninstantiatedValueFilters.add(new GenomicDataFilter(null, null, null));
        List<GenomicDataFilter> mergedUninstantiatedValueFilters = StudyViewFilterHelper.mergeDataFilters(uninstantiatedValueFilters);
        assertNull(mergedUninstantiatedValueFilters.getFirst().getValues());

        List<GenomicDataFilter> emptyValueFilters = new ArrayList<>();
        emptyValueFilters.add(new GenomicDataFilter(null, null, new ArrayList<>()));
        List<GenomicDataFilter> mergedEmptyValueFilters = StudyViewFilterHelper.mergeDataFilters(emptyValueFilters);
        assertEquals(0, mergedEmptyValueFilters.getFirst().getValues().size());
    }

    // invalid (2, 3, "NA"] -> unprocessed (2, 3, "NA"]
    @Test
    public void testMergeDataFilterNonNumericalValidation() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> invalidValues = new ArrayList<>();
        DataFilterValue invalidValue = new DataFilterValue("NA");
        invalidValue.setStart(BigDecimal.valueOf(2));
        invalidValue.setEnd(BigDecimal.valueOf(3));
        invalidValues.add(invalidValue);
        genomicDataFilters.add(new GenomicDataFilter(null, null, invalidValues));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal start = mergedDataFilterValues.getFirst().getStart();
        BigDecimal end = mergedDataFilterValues.getFirst().getEnd();
        String value = mergedDataFilterValues.getFirst().getValue();
        assertEquals(0, BigDecimal.valueOf(2).compareTo(start));
        assertEquals(0, BigDecimal.valueOf(3).compareTo(end));
        assertEquals("NA", value);
    }

    // invalid (42, 6] -> unprocessed (42, 6]
    @Test
    public void testMergeDataFilterRangeValidation() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> invalidValues = new ArrayList<>();
        invalidValues.add(new DataFilterValue(BigDecimal.valueOf(42), BigDecimal.valueOf(6)));
        genomicDataFilters.add(new GenomicDataFilter(null, null, invalidValues));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal start = mergedDataFilterValues.getFirst().getStart();
        BigDecimal end = mergedDataFilterValues.getFirst().getEnd();
        assertEquals(0, BigDecimal.valueOf(42).compareTo(start));
        assertEquals(0, BigDecimal.valueOf(6).compareTo(end));
    }

    // invalid (3, 5], (1, 3] -> unprocessed (3, 5], (1, 3]
    @Test
    public void testMergeDataFilterContinuousValidation() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue(BigDecimal.valueOf(3), BigDecimal.valueOf(5)));
        values.add(new DataFilterValue(BigDecimal.valueOf(1), BigDecimal.valueOf(3)));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal firstStart = mergedDataFilterValues.getFirst().getStart();
        BigDecimal firstEnd = mergedDataFilterValues.getFirst().getEnd();
        assertEquals(0, BigDecimal.valueOf(3).compareTo(firstStart));
        assertEquals(0, BigDecimal.valueOf(5).compareTo(firstEnd));
        BigDecimal secondStart = mergedDataFilterValues.get(1).getStart();
        BigDecimal secondEnd = mergedDataFilterValues.get(1).getEnd();
        assertEquals(0, BigDecimal.valueOf(1).compareTo(secondStart));
        assertEquals(0, BigDecimal.valueOf(3).compareTo(secondEnd));
    }

    // invalid (3, 5], (2, 4] -> unprocessed (3, 5], (2, 4]
    @Test
    public void testMergeDataFilterNoOverlapValidation() {
        List<GenomicDataFilter> genomicDataFilters = new ArrayList<>();
        List<DataFilterValue> values = new ArrayList<>();
        values.add(new DataFilterValue(BigDecimal.valueOf(3), BigDecimal.valueOf(5)));
        values.add(new DataFilterValue(BigDecimal.valueOf(2), BigDecimal.valueOf(4)));
        genomicDataFilters.add(new GenomicDataFilter(null, null, values));

        List<GenomicDataFilter> mergedGenomicDataFilters = StudyViewFilterHelper.mergeDataFilters(genomicDataFilters);
        List<DataFilterValue> mergedDataFilterValues = mergedGenomicDataFilters.getFirst().getValues();
        BigDecimal firstStart = mergedDataFilterValues.getFirst().getStart();
        BigDecimal firstEnd = mergedDataFilterValues.getFirst().getEnd();
        assertEquals(0, BigDecimal.valueOf(3).compareTo(firstStart));
        assertEquals(0, BigDecimal.valueOf(5).compareTo(firstEnd));
        BigDecimal secondStart = mergedDataFilterValues.get(1).getStart();
        BigDecimal secondEnd = mergedDataFilterValues.get(1).getEnd();
        assertEquals(0, BigDecimal.valueOf(2).compareTo(secondStart));
        assertEquals(0, BigDecimal.valueOf(4).compareTo(secondEnd));
    }
}
