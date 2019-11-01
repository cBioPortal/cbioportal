package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DataBin;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class DataBinner {
    private DataBinHelper dataBinHelper;
    private DiscreteDataBinner discreteDataBinner;
    private LinearDataBinner linearDataBinner;
    private ScientificSmallDataBinner scientificSmallDataBinner;
    private StudyViewFilterUtil studyViewFilterUtil;
    private LogScaleDataBinner logScaleDataBinner;

    @Autowired
    public DataBinner(DataBinHelper dataBinHelper,
                      DiscreteDataBinner discreteDataBinner,
                      LinearDataBinner linearDataBinner,
                      ScientificSmallDataBinner scientificSmallDataBinner,
                      StudyViewFilterUtil studyViewFilterUtil,
                      LogScaleDataBinner logScaleDataBinner) {
        this.dataBinHelper = dataBinHelper;
        this.discreteDataBinner = discreteDataBinner;
        this.linearDataBinner = linearDataBinner;
        this.scientificSmallDataBinner = scientificSmallDataBinner;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.logScaleDataBinner = logScaleDataBinner;
    }

    public List<DataBin> calculateClinicalDataBins(ClinicalDataBinFilter clinicalDataBinFilter,
                                                   ClinicalDataType clinicalDataType,
                                                   List<ClinicalData> filteredClinicalData,
                                                   List<ClinicalData> unfilteredClinicalData,
                                                   List<String> filteredIds,
                                                   List<String> unfilteredIds) {
        // calculate data bins for unfiltered clinical data
        List<DataBin> clinicalDataBins = calculateClinicalDataBins(
            clinicalDataBinFilter, clinicalDataType, unfilteredClinicalData, unfilteredIds);
        // recount
        return recalcBinCount(clinicalDataBins, clinicalDataType, filteredClinicalData, filteredIds);
    }

    public List<DataBin> recalcBinCount(List<DataBin> clinicalDataBins,
                                        ClinicalDataType clinicalDataType,
                                        List<ClinicalData> clinicalData,
                                        List<String> ids) {
        List<BigDecimal> numericalValues = clinicalData == null ?
            Collections.emptyList() : filterNumericalValues(clinicalData);
        List<String> nonNumericalValues = clinicalData == null ?
            Collections.emptyList() : filterNonNumericalValues(clinicalData);
        List<Range<BigDecimal>> ranges = clinicalData == null ?
            Collections.emptyList() : filterSpecialRanges(clinicalData);

        for (DataBin dataBin : clinicalDataBins) {
            // reset count
            dataBin.setCount(0);

            // calculate range
            Range<BigDecimal> range = dataBinHelper.calcRange(dataBin);

            if (range != null) {
                for (BigDecimal value: numericalValues) {
                    if (range.contains(value)) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }

                for (Range<BigDecimal> r: ranges) {
                    if (range.encloses(r)) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }
            } else { // if no range then it means non numerical data bin
                for (String value: nonNumericalValues) {
                    if (value.equalsIgnoreCase(dataBin.getSpecialValue())) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }
            }
            if ("NA".equalsIgnoreCase(dataBin.getSpecialValue())) {
                dataBin.setCount(countNAs(clinicalData, clinicalDataType, ids).intValue());
            }
        }

        return clinicalDataBins;
    }

    public List<DataBin> calculateClinicalDataBins(ClinicalDataBinFilter clinicalDataBinFilter,
                                                   ClinicalDataType clinicalDataType,
                                                   List<ClinicalData> clinicalData,
                                                   List<String> ids) {
        String attributeId = clinicalDataBinFilter.getAttributeId();
        boolean numericalOnly = false;

        Range<BigDecimal> range = clinicalDataBinFilter.getStart() == null && clinicalDataBinFilter.getEnd() == null ?
            Range.all() : studyViewFilterUtil.calcRange(clinicalDataBinFilter.getStart(), true, clinicalDataBinFilter.getEnd(), true);

        if (range.hasUpperBound()) {
            clinicalData = filterSmallerThanUpperBound(clinicalData, range.upperEndpoint());
            numericalOnly = true;
        }

        if (range.hasLowerBound()) {
            clinicalData = filterBiggerThanLowerBound(clinicalData, range.lowerEndpoint());
            numericalOnly = true;
        }

        DataBin upperOutlierBin = calcUpperOutlierBin(attributeId, clinicalData);
        DataBin lowerOutlierBin = calcLowerOutlierBin(attributeId, clinicalData);
        Collection<DataBin> numericalBins = calcNumericalClinicalDataBins(
            attributeId, clinicalData, clinicalDataBinFilter.getCustomBins(), lowerOutlierBin, upperOutlierBin,
            clinicalDataBinFilter.getDisableLogScale());

        List<DataBin> dataBins = new ArrayList<>();

        if (!lowerOutlierBin.getCount().equals(0)) {
            dataBins.add(lowerOutlierBin);
        }

        dataBins.addAll(numericalBins);

        if (!upperOutlierBin.getCount().equals(0)) {
            dataBins.add(upperOutlierBin);
        }

        // remove leading and trailing empty bins before adding non numerical ones
        dataBins = dataBinHelper.trim(dataBins);

        if(!numericalOnly) {
            // add non numerical and NA data bins

            dataBins.addAll(calcNonNumericalClinicalDataBins(attributeId, clinicalData));

            DataBin naDataBin = calcNaDataBin(attributeId, clinicalData, clinicalDataType ,ids);
            if (!naDataBin.getCount().equals(0)) {
                dataBins.add(naDataBin);
            }
        }

        return dataBins;
    }

    public List<Range<BigDecimal>> filterSpecialRanges(List<ClinicalData> clinicalData) {
        return clinicalData.stream()
            .map(ClinicalData::getAttrValue)
            .filter(s -> (s.contains(">") || s.contains("<")) &&
                // ignore any invalid values such as >10PY, <20%, etc.
                NumberUtils.isCreatable(dataBinHelper.stripOperator(s)))
            .map(v -> dataBinHelper.calcRange(
                // only use "<" or ">" to make sure that we only generate open ranges
                dataBinHelper.extractOperator(v).substring(0,1),
                new BigDecimal(dataBinHelper.stripOperator(v))))
            .collect(Collectors.toList());
    }

    public Collection<DataBin> calcNonNumericalClinicalDataBins(String attributeId,
                                                                List<ClinicalData> clinicalData) {
        return calcNonNumericalDataBins(attributeId, filterNonNumericalValues(clinicalData));
    }

    public List<String> filterNonNumericalValues(List<ClinicalData> clinicalData) {
        // filter out numerical values and 'NA's
        return clinicalData.stream()
                .map(ClinicalData::getAttrValue)
                .filter(s -> !NumberUtils.isCreatable(dataBinHelper.stripOperator(s)) && !dataBinHelper.isNA(s))
                .collect(Collectors.toList());
    }

    public Collection<DataBin> calcNonNumericalDataBins(String attributeId,
                                                        List<String> nonNumericalValues) {
        Map<String, DataBin> map = new LinkedHashMap<>();

        for (String value : nonNumericalValues) {
            DataBin dataBin = map.computeIfAbsent(value.trim().toUpperCase(), key -> {
                DataBin bin = new DataBin();
                bin.setAttributeId(attributeId);
                bin.setSpecialValue(value.trim());
                bin.setCount(0);
                return bin;
            });

            dataBin.setCount(dataBin.getCount() + 1);
        }

        return map.values();
    }

    public Collection<DataBin> calcNumericalClinicalDataBins(String attributeId,
                                                             List<ClinicalData> clinicalData,
                                                             List<BigDecimal> customBins,
                                                             DataBin lowerOutlierBin,
                                                             DataBin upperOutlierBin,
                                                             Boolean disableLogScale) {
        return calcNumericalDataBins(attributeId,
            filterNumericalValues(clinicalData),
            customBins,
            lowerOutlierBin,
            upperOutlierBin,
            disableLogScale);
    }

    public List<BigDecimal> filterNumericalValues(List<ClinicalData> clinicalData) {
        // filter out invalid values
        return clinicalData.stream()
            .filter(c -> NumberUtils.isCreatable(c.getAttrValue()))
            .map(c -> new BigDecimal(c.getAttrValue()))
            .collect(Collectors.toList());
    }

    public Collection<DataBin> calcNumericalDataBins(String attributeId,
                                                     List<BigDecimal> numericalValues,
                                                     List<BigDecimal> customBins,
                                                     DataBin lowerOutlierBin,
                                                     DataBin upperOutlierBin,
                                                     Boolean disableLogScale) {
        Predicate<BigDecimal> isLowerOutlier = new Predicate<BigDecimal>() {
            @Override
            public boolean test(BigDecimal d) {
                return (
                    lowerOutlierBin != null &&
                        lowerOutlierBin.getEnd() != null &&
                        (lowerOutlierBin.getSpecialValue() != null && lowerOutlierBin.getSpecialValue().contains("=") ?
                            d.compareTo(lowerOutlierBin.getEnd()) != 1  : d.compareTo(lowerOutlierBin.getEnd()) == -1)
                );
            }
        };

        Predicate<BigDecimal> isUpperOutlier = new Predicate<BigDecimal>() {
            @Override
            public boolean test(BigDecimal d) {
                return (
                    upperOutlierBin != null &&
                        upperOutlierBin.getStart() != null &&
                        (upperOutlierBin.getSpecialValue() != null && upperOutlierBin.getSpecialValue().contains("=") ?
                            d.compareTo(upperOutlierBin.getStart()) != -1 : d.compareTo(upperOutlierBin.getStart()) == 1)
                );
            }
        };

        Predicate<BigDecimal> isNotOutlier = new Predicate<BigDecimal>() {
            @Override
            public boolean test(BigDecimal d) {
                return !isUpperOutlier.test(d) && !isLowerOutlier.test(d);
            }
        };


        List<BigDecimal> sortedNumericalValues = new ArrayList<>(numericalValues);
        Collections.sort(sortedNumericalValues);

        Range<BigDecimal> boxRange = dataBinHelper.calcBoxRange(sortedNumericalValues);

        // remove initial outliers
        List<BigDecimal> withoutOutliers = sortedNumericalValues.stream().filter(isNotOutlier).collect(Collectors.toList());

        // calculate data bins for the rest of the values
        List<DataBin> dataBins = null;

        Set<BigDecimal> uniqueValues = new LinkedHashSet<>(withoutOutliers);

        if (0 < uniqueValues.size() && uniqueValues.size() <= 5) {
            // No data intervals when the number of distinct values less than or equal to 5.
            // In this case, number of bins = number of distinct data values
            dataBins = discreteDataBinner.calculateDataBins(attributeId, withoutOutliers, uniqueValues);
        } else if (withoutOutliers.size() > 0) {
            BigDecimal lowerOutlier = lowerOutlierBin.getEnd() == null ?
                boxRange.lowerEndpoint() : boxRange.lowerEndpoint().max(lowerOutlierBin.getEnd());
            BigDecimal upperOutlier = upperOutlierBin.getStart() == null ?
                boxRange.upperEndpoint() : boxRange.upperEndpoint().min(upperOutlierBin.getStart());

            if (customBins != null) {
                dataBins = linearDataBinner.calculateDataBins(attributeId, customBins, numericalValues);
            } else if (boxRange.upperEndpoint().subtract(boxRange.lowerEndpoint()).intValue() > 1000 &&
                (disableLogScale == null || !disableLogScale)) {
                dataBins = logScaleDataBinner.calculateDataBins(attributeId,
                    boxRange,
                    withoutOutliers,
                    lowerOutlierBin.getEnd(),
                    upperOutlierBin.getStart());
            } else if (dataBinHelper.isSmallData(sortedNumericalValues)) {
                dataBins = scientificSmallDataBinner.calculateDataBins(attributeId,
                    sortedNumericalValues,
                    withoutOutliers,
                    lowerOutlierBin.getEnd(),
                    upperOutlierBin.getStart());

                // override box range with data bin min & max values (ignoring actual box range for now)
                if (dataBins.size() > 0) {
                    boxRange = Range.closed(dataBins.get(0).getStart(), dataBins.get(dataBins.size() - 1).getEnd());
                }
            } else {
                dataBins = linearDataBinner.calculateDataBins(attributeId,
                    boxRange,
                    withoutOutliers,
                    lowerOutlier,
                    upperOutlier);
            }


            // adjust the outlier limits:
            //
            // - when there is no special outlier values within the original data (like "<=20", ">80")
            // then prioritize dataBin values over box range values
            //
            // - when there is special outlier values within the original data,
            // then prioritize special outlier values over dataBin values

            if (lowerOutlierBin.getEnd() == null) {

                BigDecimal end = dataBins != null && dataBins.size() > 0 ? dataBins.get(0).getStart() :
                    boxRange.lowerEndpoint();

                lowerOutlierBin.setEnd(end);
            } else if (dataBins != null && dataBins.size() > 0) {
                if (dataBins.get(0).getStart().compareTo(lowerOutlierBin.getEnd()) == 1) {
                    lowerOutlierBin.setEnd(dataBins.get(0).getStart());
                } else {
                    dataBins.get(0).setStart(lowerOutlierBin.getEnd());
                }
            }

            if (upperOutlierBin.getStart() == null) {
                BigDecimal start = dataBins != null && dataBins.size() > 0 ? dataBins.get(dataBins.size() - 1).getEnd() :
                    boxRange.upperEndpoint();

                upperOutlierBin.setStart(start);
            } else if (dataBins != null && dataBins.size() > 0) {
                if (dataBins.get(dataBins.size() - 1).getEnd().compareTo(upperOutlierBin.getStart()) == -1) {
                    upperOutlierBin.setStart(dataBins.get(dataBins.size() - 1).getStart());
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

    public List<BigDecimal> doubleValuesForSpecialOutliers(List<ClinicalData> clinicalData, String operator) {
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

    public List<ClinicalData> filterSmallerThanUpperBound(List<ClinicalData> clinicalData, BigDecimal value) {
        return (
            clinicalData.stream()
                .filter(c -> NumberUtils.isCreatable(c.getAttrValue()) && new BigDecimal(c.getAttrValue()).compareTo(value) != 1)
                .collect(Collectors.toList())
        );
    }

    public List<ClinicalData> filterBiggerThanLowerBound(List<ClinicalData> clinicalData, BigDecimal value) {
        return (
            clinicalData.stream()
                .filter(c -> NumberUtils.isCreatable(c.getAttrValue()) && new BigDecimal(c.getAttrValue()).compareTo(value) != -1)
                // collect as list
                .collect(Collectors.toList())
        );
    }

    public DataBin calcUpperOutlierBin(String attributeId, List<ClinicalData> clinicalData) {
        DataBin dataBin = dataBinHelper.calcUpperOutlierBin(attributeId,
            doubleValuesForSpecialOutliers(clinicalData, ">="),
            doubleValuesForSpecialOutliers(clinicalData, ">"));

        // for consistency always set operator to ">"
        dataBin.setSpecialValue(">");

        return dataBin;
    }

    public DataBin calcLowerOutlierBin(String attributeId, List<ClinicalData> clinicalData) {
        DataBin dataBin = dataBinHelper.calcLowerOutlierBin(attributeId,
            doubleValuesForSpecialOutliers(clinicalData, "<="),
            doubleValuesForSpecialOutliers(clinicalData, "<"));

        // for consistency always set operator to "<="
        dataBin.setSpecialValue("<=");

        return dataBin;
    }

    /**
     * NA count is: Number of clinical data marked actually as "NA" + Number of patients/samples without clinical data.
     * Assuming that clinical data is for a single attribute.
     *
     * @param attributeId   clinical data attribute id
     * @param clinicalData  clinical data list for a single attribute
     * @param ids           sample/patient ids
     *
     * @return 'NA' clinical data count as a DataBin instance
     */
    public DataBin calcNaDataBin(String attributeId,
                                 List<ClinicalData> clinicalData,
                                 ClinicalDataType clinicalDataType,
                                 List<String> ids) {
        DataBin bin = new DataBin();

        bin.setAttributeId(attributeId);
        bin.setSpecialValue("NA");

        Long count = countNAs(clinicalData, clinicalDataType, ids);

        bin.setCount(count.intValue());

        return bin;
    }

    public Long countNAs(List<ClinicalData> clinicalData, ClinicalDataType clinicalDataType, List<String> ids) {
        // Calculate number of clinical data marked actually as "NA", "NAN", or "N/A"

        Long count = clinicalData == null ? 0 :
            clinicalData.stream()
            .filter(c -> dataBinHelper.isNA(c.getAttrValue()))
            .count();

        // Calculate number of patients/samples without clinical data

        Set<String> uniqueClinicalDataIds;

        if (clinicalData != null) {
            uniqueClinicalDataIds = clinicalDataType == ClinicalDataType.PATIENT ?
                clinicalData.stream().map(ClinicalData::getPatientId).filter(Objects::nonNull).collect(Collectors.toSet()) :
                clinicalData.stream().map(ClinicalData::getSampleId).filter(Objects::nonNull).collect(Collectors.toSet());
        } else {
            uniqueClinicalDataIds = Collections.emptySet();
        }

        Set<String> uniqueInputIds = new HashSet<>(ids);

        // remove the ids with existing clinical data,
        // size of the difference (of two sets) is the count we need
        uniqueInputIds.removeAll(uniqueClinicalDataIds);
        count += uniqueInputIds.size();

        return count;
    }
}
