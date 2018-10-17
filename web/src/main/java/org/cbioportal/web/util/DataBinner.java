package org.cbioportal.web.util;

import com.google.common.collect.Range;
import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DataBin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DataBinner
{
    private DataBinHelper dataBinHelper;
    private DiscreteDataBinner discreteDataBinner;
    private LinearDataBinner linearDataBinner;
    private ScientificSmallDataBinner scientificSmallDataBinner;
    private LogScaleDataBinner logScaleDataBinner;

    @Autowired
    public DataBinner(DataBinHelper dataBinHelper, 
                      DiscreteDataBinner discreteDataBinner, 
                      LinearDataBinner linearDataBinner, 
                      ScientificSmallDataBinner scientificSmallDataBinner, 
                      LogScaleDataBinner logScaleDataBinner) 
    {
        this.dataBinHelper = dataBinHelper;
        this.discreteDataBinner = discreteDataBinner;
        this.linearDataBinner = linearDataBinner;
        this.scientificSmallDataBinner = scientificSmallDataBinner;
        this.logScaleDataBinner = logScaleDataBinner;
    }

    public List<DataBin> calculateClinicalDataBins(String attributeId,
                                                   List<ClinicalData> filteredClinicalData,
                                                   List<ClinicalData> unfilteredClinicalData,
                                                   List<String> filteredIds,
                                                   List<String> unfilteredIds)
    {
        return calculateClinicalDataBins(
            attributeId, filteredClinicalData, unfilteredClinicalData, filteredIds, unfilteredIds, false);
    }
    
    public List<DataBin> calculateClinicalDataBins(String attributeId,
                                                   List<ClinicalData> filteredClinicalData,
                                                   List<ClinicalData> unfilteredClinicalData,
                                                   List<String> filteredIds,
                                                   List<String> unfilteredIds,
                                                   Boolean disableLogScale)
    {
        // calculate data bins for unfiltered clinical data
        List<DataBin> clinicalDataBins = calculateClinicalDataBins(
            attributeId, unfilteredClinicalData, unfilteredIds, disableLogScale);
        
        // recount
        return recalcBinCount(clinicalDataBins, filteredClinicalData, filteredIds);
    }

    public List<DataBin> recalcBinCount(List<DataBin> clinicalDataBins,
                                        List<ClinicalData> clinicalData,
                                        List<String> ids) 
    {
        List<Double> numericalValues = clinicalData == null ? 
            Collections.emptyList() : filterNumericalValues(clinicalData);
        List<String> nonNumericalValues = clinicalData == null ?
            Collections.emptyList() : filterNonNumericalValues(clinicalData);
        
        for (DataBin dataBin : clinicalDataBins) {
            // reset count
            dataBin.setCount(0);
            
            // calculate range
            Range<Double> range = dataBinHelper.calcRange(dataBin);
            
            if (range != null) {
                for (Double value: numericalValues) {
                    if (range.contains(value)) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }
            }
            // if no range then it means non numerical data bin
            else {
                for (String value: nonNumericalValues) {
                    if (value.equals(dataBin.getSpecialValue())) {
                        dataBin.setCount(dataBin.getCount() + 1);
                    }
                }
            }
            
            if ("NA".equals(dataBin.getSpecialValue())) {
                dataBin.setCount(countNAs(clinicalData, ids).intValue());
            }
        }
        
        return clinicalDataBins;
    }

    public List<DataBin> calculateClinicalDataBins(String attributeId,
                                                   List<ClinicalData> clinicalData,
                                                   List<String> ids)
    {
        return calculateClinicalDataBins(attributeId, clinicalData, ids, false);
    }
    
    public List<DataBin> calculateClinicalDataBins(String attributeId, 
                                                   List<ClinicalData> clinicalData, 
                                                   List<String> ids,
                                                   Boolean disableLogScale)
    {
        DataBin upperOutlierBin = calcUpperOutlierBin(attributeId, clinicalData);
        DataBin lowerOutlierBin = calcLowerOutlierBin(attributeId, clinicalData);
        Collection<DataBin> numericalBins = calcNumericalClinicalDataBins(
            attributeId, clinicalData, lowerOutlierBin, upperOutlierBin, disableLogScale);
        
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
        
        // add non numerical and NA data bins
        
        dataBins.addAll(calcNonNumericalClinicalDataBins(attributeId, clinicalData));

        DataBin naDataBin = calcNaDataBin(attributeId, clinicalData, ids);
        
        if (!naDataBin.getCount().equals(0)) {
            dataBins.add(naDataBin);
        }
        
        return dataBins;
    }

    public Collection<DataBin> calcNonNumericalClinicalDataBins(String attributeId, 
                                                                List<ClinicalData> clinicalData)
    {
        return calcNonNumericalDataBins(attributeId, filterNonNumericalValues(clinicalData));
    }

    public List<String> filterNonNumericalValues(List<ClinicalData> clinicalData) 
    {
        // filter out numerical values and 'NA's
        return clinicalData.stream()
                .map(ClinicalData::getAttrValue)
                .filter(s -> !NumberUtils.isCreatable(dataBinHelper.stripOperator(s)) && !dataBinHelper.isNA(s))
                .collect(Collectors.toList());
    }

    public Collection<DataBin> calcNonNumericalDataBins(String attributeId,
                                                        List<String> nonNumericalValues)
    {
        Map<String, DataBin> map = new LinkedHashMap<>();

        for (String value : nonNumericalValues) {
            DataBin dataBin = map.computeIfAbsent(value.trim().toUpperCase(), key -> {
                DataBin bin = new DataBin();
                bin.setAttributeId(attributeId);
                bin.setSpecialValue(key);
                bin.setCount(0);
                return bin;
            });

            dataBin.setCount(dataBin.getCount() + 1);
        }

        return map.values();
    }

    public Collection<DataBin> calcNumericalClinicalDataBins(String attributeId, 
                                                             List<ClinicalData> clinicalData, 
                                                             DataBin lowerOutlierBin, 
                                                             DataBin upperOutlierBin, 
                                                             Boolean disableLogScale)
    {
        return calcNumericalDataBins(attributeId, 
            filterNumericalValues(clinicalData), 
            lowerOutlierBin, 
            upperOutlierBin,
            disableLogScale);
    }
    
    public List<Double> filterNumericalValues(List<ClinicalData> clinicalData)
    {
        // filter out invalid values
        return clinicalData.stream()
            .filter(c -> NumberUtils.isCreatable(c.getAttrValue()))
            .map(c -> Double.parseDouble(c.getAttrValue()))
            .collect(Collectors.toList());
    }
    
    public Collection<DataBin> calcNumericalDataBins(String attributeId,
                                                     List<Double> numericalValues, 
                                                     DataBin lowerOutlierBin, 
                                                     DataBin upperOutlierBin,
                                                     Boolean disableLogScale)
    {
        Predicate<Double> isLowerOutlier = new Predicate<Double>() {
            @Override
            public boolean test(Double d) {
                return (
                    lowerOutlierBin != null && 
                    lowerOutlierBin.getEnd() != null && 
                    (lowerOutlierBin.getSpecialValue() != null && lowerOutlierBin.getSpecialValue().contains("=") ? 
                        d <= lowerOutlierBin.getEnd() : d < lowerOutlierBin.getEnd())
                );
            }
        };

        Predicate<Double> isUpperOutlier = new Predicate<Double>() {
            @Override
            public boolean test(Double d) {
                return (
                    upperOutlierBin != null &&
                    upperOutlierBin.getStart() != null && 
                    (upperOutlierBin.getSpecialValue() != null && upperOutlierBin.getSpecialValue().contains("=") ? 
                        d >= upperOutlierBin.getStart() : d > upperOutlierBin.getStart())
                );
            }
        };
        
        Predicate<Double> isNotOutlier = new Predicate<Double>() {
            @Override
            public boolean test(Double d) {
                return !isUpperOutlier.test(d) && !isLowerOutlier.test(d);
            }
        };
        
        
        List<Double> sortedNumericalValues = new ArrayList<>(numericalValues);
        Collections.sort(sortedNumericalValues);

        Range<Double> boxRange = dataBinHelper.calcBoxRange(sortedNumericalValues);
        
        // remove initial outliers
        List<Double> withoutOutliers = sortedNumericalValues.stream().filter(isNotOutlier).collect(Collectors.toList());
        
        // calculate data bins for the rest of the values
        List<DataBin> dataBins = null;
        
        Set<Double> uniqueValues = new LinkedHashSet<>(withoutOutliers);

        if (0 < uniqueValues.size() && uniqueValues.size() <= 5)
        {
            // No data intervals when the number of distinct values less than or equal to 5.
            // In this case, number of bins = number of distinct data values
            dataBins = discreteDataBinner.calculateDataBins(attributeId, withoutOutliers, uniqueValues);
        }
        else if (withoutOutliers.size() > 0)
        {
            Double lowerOutlier = lowerOutlierBin.getEnd() == null ?
                boxRange.lowerEndpoint() : Math.max(boxRange.lowerEndpoint(), lowerOutlierBin.getEnd());
            Double upperOutlier = upperOutlierBin.getStart() == null ?
                boxRange.upperEndpoint() : Math.min(boxRange.upperEndpoint(), upperOutlierBin.getStart());
            
            if (boxRange.upperEndpoint() - boxRange.lowerEndpoint() > 1000 && 
                (disableLogScale == null || !disableLogScale))
            {
                dataBins = logScaleDataBinner.calculateDataBins(attributeId,
                    boxRange,
                    withoutOutliers,
                    lowerOutlier,
                    upperOutlier);
            }
            else if (dataBinHelper.isSmallData(sortedNumericalValues)) 
            {
                dataBins = scientificSmallDataBinner.calculateDataBins(attributeId, 
                    sortedNumericalValues,
                    withoutOutliers,
                    lowerOutlierBin.getEnd(), 
                    upperOutlierBin.getStart());
                
                // override box range with data bin min & max values (ignoring actual box range for now) 
                if (dataBins.size() > 0) {
                    boxRange = Range.closed(dataBins.get(0).getStart(), dataBins.get(dataBins.size() - 1).getEnd());
                }
            }
            else 
            {
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
                 
                Double end = dataBins != null && dataBins.size() > 0 ? dataBins.get(0).getStart() : 
                    boxRange.lowerEndpoint();

                lowerOutlierBin.setEnd(end);
            }
            else if (dataBins != null && dataBins.size() > 0) {
                if (dataBins.get(0).getStart() > lowerOutlierBin.getEnd()) {
                    lowerOutlierBin.setEnd(dataBins.get(0).getStart());
                }
                else {
                    dataBins.get(0).setStart(lowerOutlierBin.getEnd());
                }
            }

            if (upperOutlierBin.getStart() == null) {
                Double start = dataBins != null && dataBins.size() > 0 ? dataBins.get(dataBins.size()-1).getEnd() :
                    boxRange.upperEndpoint();

                upperOutlierBin.setStart(start);
            }
            else if (dataBins != null && dataBins.size() > 0) {
                if (dataBins.get(dataBins.size()-1).getEnd() < upperOutlierBin.getStart()) {
                    upperOutlierBin.setStart(dataBins.get(dataBins.size()-1).getStart());
                }
                else {
                    dataBins.get(dataBins.size()-1).setEnd(upperOutlierBin.getStart());
                }
            }
        }
        
        // update upper and lower outlier counts
        List<Double> upperOutliers = sortedNumericalValues.stream().filter(isUpperOutlier).collect(Collectors.toList());
        List<Double> lowerOutliers = sortedNumericalValues.stream().filter(isLowerOutlier).collect(Collectors.toList());
        
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
    
    public List<Double> doubleValuesForSpecialOutliers(List<ClinicalData> clinicalData, String operator) 
    {
        return (
            // find the ones starting with the operator
            clinicalData.stream().filter(c -> c.getAttrValue().trim().startsWith(operator))
            // strip the operator
            .map(c -> c.getAttrValue().trim().substring(operator.length()))
            // filter out invalid values
            .filter(NumberUtils::isCreatable)
            // parse the numerical value as a Double instance
            .map(Double::parseDouble)
            // collect as list
            .collect(Collectors.toList())
        );
    }
    
    public DataBin calcUpperOutlierBin(String attributeId, List<ClinicalData> clinicalData)
    {
        return dataBinHelper.calcUpperOutlierBin(attributeId,
            doubleValuesForSpecialOutliers(clinicalData, ">="), 
            doubleValuesForSpecialOutliers(clinicalData, ">"));
    }

    public DataBin calcLowerOutlierBin(String attributeId, List<ClinicalData> clinicalData)
    {
        return dataBinHelper.calcLowerOutlierBin(attributeId,
            doubleValuesForSpecialOutliers(clinicalData, "<="),
            doubleValuesForSpecialOutliers(clinicalData, "<"));
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
    public DataBin calcNaDataBin(String attributeId, List<ClinicalData> clinicalData, List<String> ids)
    {
        DataBin bin = new DataBin();
        
        bin.setAttributeId(attributeId);
        bin.setSpecialValue("NA");
        
        Long count = countNAs(clinicalData, ids);
        
        bin.setCount(count.intValue());
        
        return bin;
    }
    
    public Long countNAs(List<ClinicalData> clinicalData, List<String> ids) 
    {
        // Calculate number of clinical data marked actually as "NA", "NAN", or "N/A"
        
        Long count = clinicalData == null ? 0 : 
            clinicalData.stream()
            .filter(c -> dataBinHelper.isNA(c.getAttrValue()))
            .count();
        
        // Calculate number of patients/samples without clinical data

        Set<String> uniqueClinicalDataIds;
        
        if (clinicalData != null)
        {
            Stream<String> sampleStream = clinicalData.stream().map(ClinicalData::getSampleId);
            Stream<String> patientStream = clinicalData.stream().map(ClinicalData::getPatientId);

            uniqueClinicalDataIds =
                Stream.concat(sampleStream, patientStream).filter(Objects::nonNull).collect(Collectors.toSet());
        }
        else {
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
