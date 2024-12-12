package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.model.Binnable;
import org.cbioportal.model.DataBin;
import org.cbioportal.web.parameter.BinsGeneratorConfig;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.DataBinFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class DataBinner {
    private static final Integer DEFAULT_DISTINCT_VALUE_THRESHOLD = 10;

    @Autowired
    private DiscreteDataBinner discreteDataBinner;
    @Autowired
    private LinearDataBinner linearDataBinner;
    @Autowired
    private ScientificSmallDataBinner scientificSmallDataBinner;
    @Autowired
    private LogScaleDataBinner logScaleDataBinner;

    /**
     * This method should only be invoked by legacy endpoints because it requires sample/patient ids.
     * 
     * @deprecated 
     */
    @Deprecated
    public <T extends DataBinFilter> List<DataBin> calculateClinicalDataBins(
        T dataBinFilter,
        ClinicalDataType clinicalDataType,
        List<Binnable> filteredClinicalData,
        List<Binnable> unfilteredClinicalData,
        List<String> filteredIds,
        List<String> unfilteredIds
    ) {
        // calculate data bins for unfiltered clinical data
        List<DataBin> dataBins = calculateDataBins(
            dataBinFilter, clinicalDataType, unfilteredClinicalData, unfilteredIds);

        // recount
        return recalcBinCount(dataBins, clinicalDataType, filteredClinicalData, filteredIds);
    }

    public <T extends DataBinFilter> List<DataBin> calculateClinicalDataBins(
        T dataBinFilter,
        List<Binnable> filteredClinicalData,
        List<Binnable> unfilteredClinicalData
    ) {
        // calculate data bins for unfiltered clinical data
        // we need this additional calculation to know the bins generated for the initial state.
        // this allows us to keep the number of bins and bin ranges consistent.
        // we only want to update the counts for each bin, we don't want to regenerate the bins for the filtered data.
        List<DataBin> dataBins = calculateDataBins(
            dataBinFilter,
            unfilteredClinicalData
        );

        // recount
        return recalcBinCount(
            dataBins,
            filteredClinicalData,
            countNAs(filteredClinicalData)
        );
    }

    /**
     * This method should only be invoked by legacy endpoints because it requires sample/patient ids.
     *
     * @deprecated
     */
    @Deprecated
    public List<DataBin> recalcBinCount(
        List<DataBin> dataBins,
        ClinicalDataType clinicalDataType,
        List<Binnable> clinicalData,
        List<String> caseIds
    ) {
        return recalcBinCount(
            dataBins,
            clinicalData,
            countNAs(clinicalData, clinicalDataType, caseIds)
        );
    }
    
    public List<DataBin> recalcBinCount(
        List<DataBin> dataBins,
        List<Binnable> clinicalData,
        Long naCount
    ) {
        List<BigDecimal> numericalValues = clinicalData == null ?
            Collections.emptyList() : filterNumericalValues(clinicalData);
        List<String> nonNumericalValues = clinicalData == null ?
            Collections.emptyList() : filterNonNumericalValues(clinicalData);
        List<Range<BigDecimal>> ranges = clinicalData == null ?
            Collections.emptyList() : filterSpecialRanges(clinicalData);

        for (DataBin dataBin : dataBins) {
            // reset count
            dataBin.setCount(0);

            // calculate range
            Range<BigDecimal> range = DataBinHelper.calcRange(dataBin);

            if (range != null) {
                for (BigDecimal value : numericalValues) {
                    if (range.contains(value)) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }

                for (Range<BigDecimal> r : ranges) {
                    if (range.encloses(r)) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }
            } else { // if no range then it means non numerical data bin
                for (String value : nonNumericalValues) {
                    if (value.equalsIgnoreCase(dataBin.getSpecialValue())) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }
            }
            if ("NA".equalsIgnoreCase(dataBin.getSpecialValue())) {
                dataBin.setCount(naCount.intValue());
            }
        }

        return dataBins;
    }

    /**
     * This method should only be invoked by legacy endpoints because it requires sample/patient ids.
     *
     * @deprecated
     */
    @Deprecated
    public <T extends DataBinFilter> List<DataBin> calculateDataBins(
        T dataBinFilter,
        ClinicalDataType clinicalDataType,
        List<Binnable> clinicalData,
        List<String> caseIds
    ) {
        return calculateDataBins(
            dataBinFilter,
            clinicalDataType,
            clinicalData,
            caseIds,
            DEFAULT_DISTINCT_VALUE_THRESHOLD
        );
    }

    public <T extends DataBinFilter> List<DataBin> calculateDataBins(
        T dataBinFilter,
        List<Binnable> clinicalData
    ) {
        return calculateDataBins(
            dataBinFilter,
            clinicalData,
            DEFAULT_DISTINCT_VALUE_THRESHOLD
        );
    }

    public <T extends DataBinFilter> List<DataBin> calculateDataBins(
        T dataBinFilter,
        List<Binnable> clinicalData,
        Integer distinctValueThreshold
    ) {
        DataBin naDataBin = calcNaDataBin(clinicalData);

        return calculateDataBins(dataBinFilter, clinicalData, naDataBin, distinctValueThreshold);
    }

    /**
     * This method should only be invoked by legacy endpoints because it requires sample/patient ids.
     *
     * @deprecated
     */
    @Deprecated
    public <T extends DataBinFilter> List<DataBin> calculateDataBins(
        T dataBinFilter,
        ClinicalDataType clinicalDataType,
        List<Binnable> clinicalData,
        List<String> caseIds,
        Integer distinctValueThreshold
    ) {
        DataBin naDataBin = calcNaDataBin(clinicalData, clinicalDataType, caseIds);
        
        return calculateDataBins(dataBinFilter, clinicalData, naDataBin, distinctValueThreshold);
    }
    
    public <T extends DataBinFilter> List<DataBin> calculateDataBins(
        T dataBinFilter,
        List<Binnable> clinicalData,
        DataBin naDataBin,
        Integer distinctValueThreshold
    ) {
        boolean numericalOnly = false;

        Range<BigDecimal> range = dataBinFilter.getStart() == null && dataBinFilter.getEnd() == null ?
            Range.all() : DataBinHelper.calcRange(dataBinFilter.getStart(), true, dataBinFilter.getEnd(), true);

        if (range.hasUpperBound()) {
            clinicalData = filterSmallerThanUpperBound(clinicalData, range.upperEndpoint());
            numericalOnly = true;
        }

        if (range.hasLowerBound()) {
            clinicalData = filterBiggerThanLowerBound(clinicalData, range.lowerEndpoint());
            numericalOnly = true;
        }

        DataBin upperOutlierBin = calcUpperOutlierBin(clinicalData);
        DataBin lowerOutlierBin = calcLowerOutlierBin(clinicalData);
        Collection<DataBin> numericalBins = calcNumericalClinicalDataBins(
            dataBinFilter,
            clinicalData,
            dataBinFilter.getCustomBins(),
            dataBinFilter.getBinMethod(),
            dataBinFilter.getBinsGeneratorConfig(),
            lowerOutlierBin,
            upperOutlierBin,
            dataBinFilter.getDisableLogScale(),
            distinctValueThreshold
        );

        List<DataBin> dataBins = new ArrayList<>();

        if (!lowerOutlierBin.getCount().equals(0)) {
            dataBins.add(lowerOutlierBin);
        }

        dataBins.addAll(numericalBins);

        if (!upperOutlierBin.getCount().equals(0)) {
            dataBins.add(upperOutlierBin);
        }

        // remove leading and trailing empty bins before adding non numerical ones
        dataBins = DataBinHelper.trim(dataBins);

        // in some cases every numerical bin actually contains only a single discrete value
        // convert interval bins to distinct (single value) bins in these cases
        dataBins = DataBinHelper.convertToDistinctBins(
            dataBins, filterNumericalValues(clinicalData), filterSpecialRanges(clinicalData)
        );

        if (!numericalOnly) {
            // add non numerical and NA data bins

            dataBins.addAll(calcNonNumericalClinicalDataBins(clinicalData));

            if (!naDataBin.getCount().equals(0)) {
                dataBins.add(naDataBin);
            }
        }

        return dataBins;
    }

    public List<Range<BigDecimal>> filterSpecialRanges(List<Binnable> clinicalData) {
        return clinicalData.stream()
            .map(Binnable::getAttrValue)
            .filter(s -> (s.contains(">") || s.contains("<")) &&
                // ignore any invalid values such as >10PY, <20%, etc.
                NumberUtils.isCreatable(DataBinHelper.stripOperator(s)))
            .map(v -> DataBinHelper.calcRange(
                // only use "<" or ">" to make sure that we only generate open ranges
                DataBinHelper.extractOperator(v).substring(0, 1),
                new BigDecimal(DataBinHelper.stripOperator(v))))
            .collect(Collectors.toList());
    }

    public Collection<DataBin> calcNonNumericalClinicalDataBins(List<Binnable> clinicalData) {
        return calcNonNumericalDataBins(filterNonNumericalValues(clinicalData));
    }

    public List<String> filterNonNumericalValues(List<Binnable> clinicalData) {
        // filter out numerical values and 'NA's
        return clinicalData.stream()
            .map(Binnable::getAttrValue)
            .filter(s -> !NumberUtils.isCreatable(DataBinHelper.stripOperator(s)) && !DataBinHelper.isNA(s))
            .collect(Collectors.toList());
    }

    public Collection<DataBin> calcNonNumericalDataBins(List<String> nonNumericalValues) {
        Map<String, DataBin> map = new LinkedHashMap<>();

        for (String value : nonNumericalValues) {
            DataBin dataBin = map.computeIfAbsent(value.trim().toUpperCase(), key -> {
                DataBin bin = new DataBin();
                bin.setSpecialValue(value.trim());
                bin.setCount(0);
                return bin;
            });

            dataBin.setCount(dataBin.getCount() + 1);
        }

        return map.values();
    }

    public <T extends DataBinFilter> Collection<DataBin> calcNumericalClinicalDataBins(
        DataBinFilter dataBinFilter,
        List<Binnable> clinicalData,
        List<BigDecimal> customBins,
        DataBinFilter.BinMethod binMethod,
        BinsGeneratorConfig binsGeneratorConfig,
        DataBin lowerOutlierBin,
        DataBin upperOutlierBin,
        Boolean disableLogScale,
        Integer distinctValueThreshold
    ) {
        return calcNumericalDataBins(
            dataBinFilter,
            filterNumericalValues(clinicalData),
            customBins,
            binMethod,
            binsGeneratorConfig,
            lowerOutlierBin,
            upperOutlierBin,
            disableLogScale,
            distinctValueThreshold
        );
    }

    public List<BigDecimal> filterNumericalValues(List<Binnable> clinicalData) {
        // filter out invalid values
        return clinicalData.stream()
            .filter(c -> NumberUtils.isCreatable(c.getAttrValue()))
            .map(c -> new BigDecimal(c.getAttrValue()))
            .collect(Collectors.toList());
    }

    public <T extends DataBinFilter> Collection<DataBin> calcNumericalDataBins(
        DataBinFilter dataBinFilter,
        List<BigDecimal> numericalValues,
        List<BigDecimal> customBins,
        DataBinFilter.BinMethod binMethod,
        BinsGeneratorConfig binsGeneratorConfig,
        DataBin lowerOutlierBin,
        DataBin upperOutlierBin,
        Boolean disableLogScale,
        Integer distinctValueThreshold
    ) {

        Predicate<BigDecimal> isLowerOutlier = d -> (
            lowerOutlierBin != null &&
                lowerOutlierBin.getEnd() != null &&
                (lowerOutlierBin.getSpecialValue() != null && lowerOutlierBin.getSpecialValue().contains("=") ?
                    d.compareTo(lowerOutlierBin.getEnd()) != 1 : d.compareTo(lowerOutlierBin.getEnd()) == -1)
        );

        Predicate<BigDecimal> isUpperOutlier = d -> (
            upperOutlierBin != null &&
                upperOutlierBin.getStart() != null &&
                (upperOutlierBin.getSpecialValue() != null && upperOutlierBin.getSpecialValue().contains("=") ?
                    d.compareTo(upperOutlierBin.getStart()) != -1 : d.compareTo(upperOutlierBin.getStart()) == 1)
        );

        Predicate<BigDecimal> isNotOutlier =
            d -> !isUpperOutlier.test(d) && !isLowerOutlier.test(d);

        List<BigDecimal> sortedNumericalValues = new ArrayList<>(numericalValues);
        Collections.sort(sortedNumericalValues);

        Range<BigDecimal> boxRange = DataBinHelper.calcBoxRange(sortedNumericalValues);

        // remove initial outliers
        List<BigDecimal> withoutOutliers = sortedNumericalValues.stream().filter(isNotOutlier).collect(Collectors.toList());

        // calculate data bins for the rest of the values
        List<DataBin> dataBins = null;

        Set<BigDecimal> uniqueValues = new LinkedHashSet<>(withoutOutliers);

        if (0 < uniqueValues.size() && uniqueValues.size() <= distinctValueThreshold) {
            // No data intervals when the number of distinct values less than or equal to the threshold.
            // In this case, number of bins = number of distinct data values
            dataBins = discreteDataBinner.calculateDataBins(withoutOutliers, uniqueValues);
        } else if (!withoutOutliers.isEmpty()) {

            if (DataBinFilter.BinMethod.CUSTOM == binMethod && customBins != null) {
                // adjust custom bins w.r.t. outliers (if any)
                customBins = this.adjustCustomBins(customBins, lowerOutlierBin, upperOutlierBin);
                dataBins = linearDataBinner.calculateDataBins(customBins, numericalValues);
            } else if (DataBinFilter.BinMethod.GENERATE == binMethod && binsGeneratorConfig != null) {
                List<BigDecimal> bins = DataBinHelper.generateBins(sortedNumericalValues, binsGeneratorConfig.getBinSize(), binsGeneratorConfig.getAnchorValue());
                dataBins = linearDataBinner.calculateDataBins(bins, numericalValues);
            } else if (DataBinFilter.BinMethod.MEDIAN == binMethod) {
                // NOOP - handled later
            } else if (DataBinFilter.BinMethod.QUARTILE == binMethod) {
                List<BigDecimal> boundaries = DataBinHelper.calcQuartileBoundaries(sortedNumericalValues);
                dataBins = linearDataBinner.calculateDataBins(boundaries, numericalValues);
            } else if (boxRange.upperEndpoint().subtract(boxRange.lowerEndpoint()).compareTo(new BigDecimal(1000)) == 1 &&
                (disableLogScale == null || !disableLogScale)) {
                dataBins = logScaleDataBinner.calculateDataBins(
                    boxRange,
                    withoutOutliers,
                    lowerOutlierBin.getEnd(),
                    upperOutlierBin.getStart());
            } else if (DataBinHelper.isSmallData(sortedNumericalValues)) {
                dataBins = scientificSmallDataBinner.calculateDataBins(
                    sortedNumericalValues,
                    withoutOutliers,
                    lowerOutlierBin.getEnd(),
                    upperOutlierBin.getStart());

                // override box range with data bin min & max values (ignoring actual box range for now)
                if (!dataBins.isEmpty()) {
                    boxRange = Range.closed(dataBins.get(0).getStart(), dataBins.get(dataBins.size() - 1).getEnd());
                }
            } else {
                Boolean areAllIntegers = DataBinHelper.areAllIntegers(uniqueValues);

                if (areAllIntegers) {
                    boxRange = Range.closed(
                        new BigDecimal(boxRange.lowerEndpoint().longValue()),
                        new BigDecimal(boxRange.upperEndpoint().longValue())
                    );
                }

                BigDecimal lowerOutlier = lowerOutlierBin.getEnd() == null ? boxRange.lowerEndpoint()
                    : boxRange.lowerEndpoint().max(lowerOutlierBin.getEnd());
                BigDecimal upperOutlier = upperOutlierBin.getStart() == null ? boxRange.upperEndpoint()
                    : boxRange.upperEndpoint().min(upperOutlierBin.getStart());

                Optional<String> attributeId = dataBinFilter instanceof ClinicalDataBinFilter
                    ? Optional.of(((ClinicalDataBinFilter) dataBinFilter).getAttributeId())
                    : Optional.empty();

                dataBins = linearDataBinner.calculateDataBins(areAllIntegers, boxRange, withoutOutliers, lowerOutlier,
                    upperOutlier, attributeId);
            }

            if (DataBinFilter.BinMethod.MEDIAN == binMethod
                // In edge cases all quartile values can be identical (all
                // values are in the outlier bins). 
                || (DataBinFilter.BinMethod.QUARTILE == binMethod && dataBins.size() == 0)) {
                BigDecimal median = DataBinHelper.calcMedian(sortedNumericalValues);
                lowerOutlierBin.setEnd(median);
                upperOutlierBin.setStart(median);
                // Covers the situation where there is a single custom boundary (i.e. there are only outlier bins).
            } else if (DataBinFilter.BinMethod.CUSTOM == binMethod && customBins != null &&
                customBins.size() == 1) {
                lowerOutlierBin.setEnd(customBins.get(0));
                upperOutlierBin.setStart(customBins.get(0));
            }

            // adjust the outlier limits:
            //
            // - when there is no special outlier values within the original data (like "<=20", ">80")
            // then prioritize dataBin values over box range values
            //
            // - when there is special outlier values within the original data,
            // then prioritize special outlier values over dataBin values

            if (lowerOutlierBin.getEnd() == null) {

                BigDecimal end = dataBins != null && !dataBins.isEmpty() ? dataBins.get(0).getStart() :
                    boxRange.lowerEndpoint();

                lowerOutlierBin.setEnd(end);
            } else if (dataBins != null && !dataBins.isEmpty()) {
                if (dataBins.get(0).getStart().compareTo(lowerOutlierBin.getEnd()) == 1) {
                    lowerOutlierBin.setEnd(dataBins.get(0).getStart());
                } else {
                    dataBins.get(0).setStart(lowerOutlierBin.getEnd());
                }
            }

            if (upperOutlierBin.getStart() == null) {
                BigDecimal start = dataBins != null && !dataBins.isEmpty() ? dataBins.get(dataBins.size() - 1).getEnd() :
                    boxRange.upperEndpoint();

                upperOutlierBin.setStart(start);
            } else if (dataBins != null && !dataBins.isEmpty()) {
                if (dataBins.get(dataBins.size() - 1).getEnd().compareTo(upperOutlierBin.getStart()) == -1) {
                    upperOutlierBin.setStart(dataBins.get(dataBins.size() - 1).getEnd());
                } else {
                    dataBins.get(dataBins.size() - 1).setEnd(upperOutlierBin.getStart());
                }
            }
        }

        // update upper and lower outlier counts
        List<BigDecimal> upperOutliers = sortedNumericalValues.stream().filter(isUpperOutlier).collect(Collectors.toList());
        List<BigDecimal> lowerOutliers = sortedNumericalValues.stream().filter(isLowerOutlier).collect(Collectors.toList());

        if (upperOutliers.size() > 0) {
            upperOutlierBin.setCount(upperOutlierBin.getCount() + upperOutliers.size());
        }

        if (lowerOutliers.size() > 0) {
            lowerOutlierBin.setCount(lowerOutlierBin.getCount() + lowerOutliers.size());
        }

        if (dataBins == null) {
            dataBins = Collections.emptyList();
        }

        return dataBins;
    }

    public List<BigDecimal> doubleValuesForSpecialOutliers(List<Binnable> clinicalData, String operator) {
        return (
            // find the ones starting with the operator
            clinicalData.stream().filter(c -> c.getAttrValue().trim().startsWith(operator))
                // strip the operator
                .map(c -> c.getAttrValue().trim().substring(operator.length()))
                // filter out invalid values
                .filter(NumberUtils::isCreatable)
                // parse the numerical value as a BigDecimal instance
                .map(integer -> new BigDecimal(integer))
                // collect as list
                .collect(Collectors.toList())
        );
    }

    public List<Binnable> filterSmallerThanUpperBound(List<Binnable> clinicalData, BigDecimal value) {
        return (
            clinicalData.stream()
                .filter(c -> NumberUtils.isCreatable(c.getAttrValue()) && new BigDecimal(c.getAttrValue()).compareTo(value) != 1)
                .collect(Collectors.toList())
        );
    }

    public List<Binnable> filterBiggerThanLowerBound(List<Binnable> clinicalData, BigDecimal value) {
        return (
            clinicalData.stream()
                .filter(c -> NumberUtils.isCreatable(c.getAttrValue()) && new BigDecimal(c.getAttrValue()).compareTo(value) != -1)
                // collect as list
                .collect(Collectors.toList())
        );
    }

    public DataBin calcUpperOutlierBin(List<Binnable> clinicalData) {
        DataBin dataBin = DataBinHelper.calcUpperOutlierBin(
            doubleValuesForSpecialOutliers(clinicalData, ">="),
            doubleValuesForSpecialOutliers(clinicalData, ">"));

        // for consistency always set operator to ">"
        dataBin.setSpecialValue(">");

        return dataBin;
    }

    public DataBin calcLowerOutlierBin(List<Binnable> clinicalData) {
        DataBin dataBin = DataBinHelper.calcLowerOutlierBin(
            doubleValuesForSpecialOutliers(clinicalData, "<="),
            doubleValuesForSpecialOutliers(clinicalData, "<"));

        // for consistency always set operator to "<="
        dataBin.setSpecialValue("<=");

        return dataBin;
    }

    /**
     * We cannot do an exact calculation for values below/above the special outlier values.
     * We need to adjust custom bins for those cases.
     * 
     * example: assume we have special values <18 and >90 in our dataset
     * custom bins => [10, 15, 40, 70, 95, 100]
     * adjusted custom bins => [18, 40, 70, 90]
     */
    public List<BigDecimal> adjustCustomBins(
        List<BigDecimal> customBins,
        DataBin lowerOutlierBin,
        DataBin upperOutlierBin
    ) {
        BigDecimal lowerBound = lowerOutlierBin.getEnd();
        BigDecimal upperBound = upperOutlierBin.getStart();

        // filter out values less than lower bound and greater than the upper bound
        List<BigDecimal> binsWithinBounds = customBins
            .stream()
            .filter(bin -> lowerBound == null || bin.compareTo(lowerBound) != -1)
            .filter(bin -> upperBound == null || bin.compareTo(upperBound) != 1)
            .collect(Collectors.toList());

        // we need to add back lower bound if we removed values below the lower bound from the original bin list
        if (customBins.stream().anyMatch(bin -> lowerBound != null && bin.compareTo(lowerBound) == -1)) {
            binsWithinBounds.add(0, lowerBound);
        }

        // we need to add back upper bound if we removed values above the upper bound from the original bin list
        if (customBins.stream().anyMatch(bin -> upperBound != null && bin.compareTo(upperBound) == 1)) {
            binsWithinBounds.add(upperBound);
        }

        return binsWithinBounds;
    }

    /**
     * NA count is: Number of clinical data marked actually as "NA" + Number of patients/samples without clinical data.
     * Assuming that clinical data is for a single attribute.
     *
     * This method should only be invoked by legacy endpoints because it requires sample/patient ids.
     * 
     * @param clinicalData clinical data list for a single attribute
     * @param caseIds          sample/patient ids
     *
     * @return 'NA' clinical data count as a DataBin instance
     * @deprecated
     */
    @Deprecated
    public DataBin calcNaDataBin(
        List<Binnable> clinicalData,
        ClinicalDataType clinicalDataType,
        List<String> caseIds
    ) {
        DataBin bin = initNaDataBin();
        bin.setCount(countNAs(clinicalData, clinicalDataType, caseIds).intValue());

        return bin;
    }

    /**
     * This function assumes that all the NA values are already in the provided clinical data list.
     *
     * @param clinicalData  clinical data list for a single attribute
     *
     * @return 'NA' clinical data count as a DataBin instance
     */
    public DataBin calcNaDataBin(
        List<Binnable> clinicalData
    ) {
        DataBin bin = initNaDataBin();
        bin.setCount(countNAs(clinicalData).intValue());

        return bin;
    }
    
    public DataBin initNaDataBin() {
        DataBin bin = new DataBin();
        bin.setSpecialValue("NA");
        return bin;
    }

    /**
     * This method should only be invoked by legacy endpoints because it requires sample/patient ids
     * 
     * @deprecated 
     */
    @Deprecated
    public Long countNAs(List<Binnable> clinicalData, ClinicalDataType clinicalDataType, List<String> caseIds) {
        // Calculate the number of clinical data marked actually as "NA", "NAN", or "N/A"
        
        Long count = countNAs(clinicalData);

        // Calculate number of patients/samples without clinical data

        Set<String> uniqueClinicalDataIds;

        if (clinicalData != null) {
            uniqueClinicalDataIds = clinicalData
                .stream()
                .filter(Objects::nonNull)
                .map(datum -> computeUniqueCaseId(datum, clinicalDataType))
                .collect(Collectors.toSet());
        } else {
            uniqueClinicalDataIds = Collections.emptySet();
        }

        Set<String> uniqueInputIds = new HashSet<>(caseIds);

        // remove the ids with existing clinical data,
        // size of the difference (of two sets) is the count we need
        uniqueInputIds.removeAll(uniqueClinicalDataIds);
        count += uniqueInputIds.size();

        return count;
    }

    /**
     * Calculate number of clinical data marked actually as "NA", "NAN", or "N/A"
     */
    public Long countNAs(List<Binnable> clinicalData) {
        return clinicalData == null ? 0 :
            clinicalData.stream()
                .filter(c -> DataBinHelper.isNA(c.getAttrValue()))
                .count();
    }

    private String computeUniqueCaseId(Binnable clinicalData, ClinicalDataType clinicalDataType) {
        return clinicalData.getStudyId() + (clinicalDataType == ClinicalDataType.PATIENT
            ? clinicalData.getPatientId()
            : clinicalData.getSampleId());
    }
}
