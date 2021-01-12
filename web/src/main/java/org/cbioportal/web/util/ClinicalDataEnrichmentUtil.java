package org.cbioportal.web.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalDataEnrichment;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datumbox.framework.common.dataobjects.AssociativeArray;
import com.datumbox.framework.common.dataobjects.DataTable2D;
import com.datumbox.framework.common.dataobjects.FlatDataCollection;
import com.datumbox.framework.common.dataobjects.TransposeDataCollection;
import com.datumbox.framework.core.statistics.distributions.ContinuousDistributions;
import com.datumbox.framework.core.statistics.nonparametrics.independentsamples.Chisquare;
import com.datumbox.framework.core.statistics.nonparametrics.independentsamples.KruskalWallis;

/**
 * @author kalletlak
 *
 */
@Component
public class ClinicalDataEnrichmentUtil {

    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private ClinicalAttributeUtil clinicalAttributeUtil;

    public List<ClinicalDataEnrichment> createEnrichmentsForNumericData(List<ClinicalAttribute> attributes,
            List<List<Sample>> groupedSamples) {
        List<ClinicalDataEnrichment> clinicalEnrichments = new ArrayList<ClinicalDataEnrichment>();
        
        List<ClinicalAttribute> filteredAttributes = attributes.stream()
                .filter(attribute -> attribute.getDatatype().equals("NUMBER"))
                .collect(Collectors.toList());
        
        List<String> sampleAttributeIds = new ArrayList<>();
        List<String> patientAttributeIds = new ArrayList<>();
        List<String> conflictingPatientAttributeIds = new ArrayList<>();
        
        clinicalAttributeUtil.extractCategorizedClinicalAttributes(filteredAttributes,
                sampleAttributeIds,
                patientAttributeIds,
                conflictingPatientAttributeIds);

        // list of values for all NUMBER datatype attributes and for all sample groups
        List<Map<String, List<Double>>> dataByGroupAndByAttribute = groupedSamples.stream()
                .map(groupSamples -> getNumericClinicalData(new ArrayList<>(sampleAttributeIds),
                        new ArrayList<>(patientAttributeIds), new ArrayList<>(conflictingPatientAttributeIds),
                        groupSamples))
                .collect(Collectors.toList());

        filteredAttributes.forEach(clinicalAttribute -> {

            String attributeId = clinicalAttribute.getAttrId();
            TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
            int index = 0;
            for (Map<String, List<Double>> entry : dataByGroupAndByAttribute) {
                if (entry.containsKey(attributeId)) {
                    Collection<Object> values = entry.get(attributeId).stream().collect(Collectors.toList());
                    // add only groups having values
                    if (values.size() > 0) {
                        transposeDataCollection.put(index++, new FlatDataCollection(values));
                    }
                }
            }

            Supplier<Stream<Double>> valuesStreamSupplier = () -> transposeDataCollection
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(x -> (Double) x);

            List<Double> distinctValues = valuesStreamSupplier.get().distinct().collect(Collectors.toList());

            // perform test only if there are more than one group and
            // there are atleast two distinct values
            if (transposeDataCollection.keySet().size() > 1 && distinctValues.size() > 1) {
                double pValue = KruskalWallis.getPvalue(transposeDataCollection);
                if (!Double.isNaN(pValue)) { // this happens when all the values are zero
                    ClinicalDataEnrichment clinicalEnrichment = new ClinicalDataEnrichment();
                    clinicalEnrichment.setClinicalAttribute(clinicalAttribute);
                    clinicalEnrichment.setpValue(BigDecimal.valueOf(pValue));
                    clinicalEnrichment.setScore(BigDecimal.valueOf(ContinuousDistributions.chisquareInverseCdf(pValue,
                            transposeDataCollection.keySet().size() - 1)));
                    clinicalEnrichment.setMethod("Kruskal Wallis Test");
                    clinicalEnrichments.add(clinicalEnrichment);
                }
            }
        });
        return clinicalEnrichments;
    }

    public List<ClinicalDataEnrichment> createEnrichmentsForCategoricalData(List<ClinicalAttribute> attributes,
            List<List<Sample>> groupedSamples) {
        List<ClinicalDataEnrichment> clinicalEnrichments = new ArrayList<ClinicalDataEnrichment>();

        List<ClinicalAttribute> filteredAttributes = attributes.stream()
                .filter(attribute -> attribute.getDatatype().equals("STRING"))
                .collect(Collectors.toList());
        
        List<String> filteredAttributeIds = filteredAttributes.stream().map(ClinicalAttribute::getAttrId).collect(Collectors.toList());
        
        // ClinicalDataCountItem for all STRING datatype attributes and for all sample groups
        List<Map<String, ClinicalDataCountItem>> dataCountsByGroupAndByAttribute = groupedSamples.stream()
                .map(groupSamples -> getClinicalDataCounts(filteredAttributeIds, groupSamples)).collect(Collectors.toList());

        filteredAttributes.forEach(clinicalAttribute -> {

            String attributeId = clinicalAttribute.getAttrId();

            // get counts for all categories in all group for a given attribute
            List<Map<String, Integer>> categoryCountsByGroup = dataCountsByGroupAndByAttribute.stream().map(e -> {
                if (e.containsKey(attributeId)) {
                    return e.get(attributeId).getCounts().stream()
                            .collect(Collectors.toMap(ClinicalDataCount::getValue, ClinicalDataCount::getCount));
                }
                return new HashMap<String, Integer>();
            }).collect(Collectors.toList());

            Set<String> allPossibleCategories = categoryCountsByGroup.stream().flatMap(x -> x.keySet().stream())
                    .collect(Collectors.toSet());

            if (allPossibleCategories.size() > 1) {
                DataTable2D dataTable = new DataTable2D();
                int groupIndex = 0;
                for (Map<String, Integer> groupCategoryCounts : categoryCountsByGroup) {
                    Map<String, Integer> allCategoryCounts = allPossibleCategories.stream()
                            .collect(Collectors.toMap(category -> category, category -> {
                                return groupCategoryCounts.containsKey(category) ? groupCategoryCounts.get(category)
                                        : 0;
                            }));

                    // filter group if all the categories values are 0
                    if (isValidGroupdData(new ArrayList<>(allCategoryCounts.values()))) {
                        AssociativeArray categoryCounts = new AssociativeArray();
                        categoryCounts.putAll(allCategoryCounts);
                        dataTable.put(groupIndex++, categoryCounts);
                    }
                }

                if (dataTable.size() > 1 && dataTable.isValid()) {
                    double pValue = Chisquare.getPvalue(dataTable);
                    ClinicalDataEnrichment clinicalEnrichment = new ClinicalDataEnrichment();
                    clinicalEnrichment.setClinicalAttribute(clinicalAttribute);
                    clinicalEnrichment.setpValue(BigDecimal.valueOf(pValue));
                    clinicalEnrichment.setScore(BigDecimal.valueOf((Double) Chisquare.getScoreValue(dataTable)));
                    clinicalEnrichment.setMethod("Chi-squared Test");
                    clinicalEnrichments.add(clinicalEnrichment);
                }

            }
        });

        return clinicalEnrichments;
    }

    /**
     * get data for all NUMBER datatype attributes for given samples
     *
     * @param attributes
     * @param samples
     * @return
     */
    private Map<String, List<Double>> getNumericClinicalData(List<String> sampleAttributeIds, 
            List<String> patientAttributeIds, 
            List<String> conflictingPatientAttributeIds, 
            List<Sample> samples) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();
        for (Sample sample : samples) {
            studyIds.add(sample.getCancerStudyIdentifier());
            sampleIds.add(sample.getStableId());
            patientIds.add(sample.getPatientStableId());
        }

        Map<String, List<Double>> dataByAttribute = new HashMap<>();

        List<ClinicalData> clinicalDatas = new ArrayList<ClinicalData>();
        if (!sampleAttributeIds.isEmpty()) {
            clinicalDatas.addAll(clinicalDataService
                            .fetchClinicalData(studyIds, sampleIds, sampleAttributeIds, ClinicalDataType.SAMPLE.name(), "SUMMARY"));
        }

        if (!patientAttributeIds.isEmpty()) {
            clinicalDatas.addAll(clinicalDataService
                    .fetchClinicalData(studyIds, patientIds, patientAttributeIds, ClinicalDataType.PATIENT.name(), "SUMMARY"));
        }
        
        if (!conflictingPatientAttributeIds.isEmpty()) {
            clinicalDatas.addAll(clinicalDataService
                    .getPatientClinicalDataDetailedToSample(studyIds, patientIds, conflictingPatientAttributeIds));
        }
        
        dataByAttribute = clinicalDatas.stream()
        // filter are non numeric data to fix
        // https://github.com/cBioPortal/cbioportal/issues/6228
        .filter(x -> NumberUtils.isCreatable(x.getAttrValue()))
        .collect(Collectors.groupingBy(x -> x.getAttrId(),
                Collectors.mapping(x -> Double.valueOf(x.getAttrValue()), Collectors.toList())));

        return dataByAttribute;
    }

    /**
     * get data category counts for all STRING datatype attributes for given samples
     *
     * @param attributes
     * @param samples
     * @return
     */
    private Map<String, ClinicalDataCountItem> getClinicalDataCounts(List<String> attributeIds, List<Sample> samples) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        for (Sample sample : samples) {
            studyIds.add(sample.getCancerStudyIdentifier());
            sampleIds.add(sample.getStableId());
        }

        List<ClinicalDataCountItem> clinicalDataCountItems = clinicalDataService.fetchClinicalDataCounts(studyIds,
                sampleIds, attributeIds);

        return clinicalDataCountItems.stream()
                // Exclude NA category
                .map(clinicalDataCountItem -> {
                    List<ClinicalDataCount> filteredClinicalDataCount = clinicalDataCountItem.getCounts()
                            .stream()
                            .filter(clinicalDataCount -> !clinicalDataCount.getValue().equals("NA"))
                            .collect(Collectors.toList());
                    clinicalDataCountItem.setCounts(filteredClinicalDataCount);
                    return clinicalDataCountItem;
                })
                .collect(Collectors.toMap(clinicalDataCountItem -> clinicalDataCountItem.getAttributeId(),
                        clinicalDataCountItem -> clinicalDataCountItem));

    }

    // For categorical values, group data is valid if all the values are not 0
    private boolean isValidGroupdData(List<Integer> values) {
        return values.stream().map(value -> (value == 0 ? false : true)).reduce(false,
                (value1, value2) -> value1 || value2);
    }

}
