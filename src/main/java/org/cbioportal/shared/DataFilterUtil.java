package org.cbioportal.shared;

import org.cbioportal.legacy.web.parameter.DataFilter;
import org.cbioportal.legacy.web.parameter.DataFilterValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public abstract class DataFilterUtil {
    private DataFilterUtil() {
    }

    /**
     * Merge the range of numerical bins in DataFilters to reduce the number of scans that runs on the database when filtering.
     */
    public static <T extends DataFilter> List<T> mergeDataFilters(List<T> filters) {
        // this should throw error or move to all binning endpoints in the future for input validation
        if (!areValidFilters(filters)) {
            return filters;
        }

        boolean hasNumericalValue = false;
        List<T> mergedDataFilters = new ArrayList<>();

        for (T filter : filters) {
            List<DataFilterValue> mergedValues = new ArrayList<>();
            List<DataFilterValue> nonNumericalValues = new ArrayList<>();

            // record the start and end of current merging range
            BigDecimal mergedStart = null;
            BigDecimal mergedEnd = null;
            // for each value
            for (DataFilterValue dataFilterValue : filter.getValues()) {
                // if it is non-numerical, leave it as is
                if (dataFilterValue.getValue() != null) {
                    nonNumericalValues.add(dataFilterValue);
                    continue;
                }
                // else it is numerical so start merging process
                hasNumericalValue = true;
                BigDecimal start = dataFilterValue.getStart();
                BigDecimal end = dataFilterValue.getEnd();

                // if current merging range is null, we take current bin's range
                if (mergedStart == null && mergedEnd == null) {
                    mergedStart = start;
                    mergedEnd = end;
                }
                // else we already has a merging range, we check if this one is consecutive of our range
                else if (mergedEnd.equals(start)) {
                    // if true, we expand our range
                    mergedEnd = end;
                } else {
                    // otherwise it's a gap, so we save our current range first, and then use current bin to start the next range
                    mergedValues.add(new DataFilterValue(mergedStart, mergedEnd));
                    mergedStart = start;
                    mergedEnd = end;
                }
            }

            // in the end we need to save the final range, but if everything is non-numerical then no need to
            if (hasNumericalValue) {
                mergedValues.add(new DataFilterValue(mergedStart, mergedEnd));
            }
            mergedValues.addAll(nonNumericalValues);
            filter.setValues(mergedValues);
            mergedDataFilters.add(filter);
        }

        return mergedDataFilters;
    }

    public static <T extends DataFilter> boolean areValidFilters(List<T> filters) {
        if (filters == null || filters.isEmpty()) {
            return false;
        }

        for (T filter : filters) {
            if (!isValidFilter(filter)) {
                return false;
            }
        }
        return true;
    }

    private static <T extends DataFilter> boolean isValidFilter(T filter) {
        if (filter == null || filter.getValues() == null || filter.getValues().isEmpty()) {
            return false;
        }

        BigDecimal start = null;
        BigDecimal end = null;
        for (DataFilterValue value : filter.getValues()) {
            if (!validateDataFilterValue(value, start, end)) {
                return false;
            }
            // update start and end values to check next bin range
            if (value.getStart() != null) {
                start = value.getStart();
            }
            if (value.getEnd() != null) {
                end = value.getEnd();
            }
        }
        return true;
    }

    private static boolean validateDataFilterValue(DataFilterValue value, BigDecimal lastStart, BigDecimal lastEnd) {
        // non-numerical value should not have numerical value
        if (value.getValue() != null) {
            return value.getStart() == null && value.getEnd() == null;
        }

        // check if start < end
        if (value.getStart() != null && value.getEnd() != null
            && value.getStart().compareTo(value.getEnd()) >= 0) {
            return false;
        }

        // check if start stays increasing and no overlapping
        if (value.getStart() != null
            && ((lastStart != null && lastStart.compareTo(value.getStart()) >= 0)
            || (lastEnd != null && value.getStart().compareTo(lastEnd) < 0))) {
            return false;
        }

        // check if end stays increasing
        return value.getEnd() == null || lastEnd == null
            || lastEnd.compareTo(value.getEnd()) < 0;
    }
}
