package org.cbioportal.web.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.ClinicalDataCountItem.ClinicalDataType;
import org.cbioportal.model.ClinicalDataEnrichment;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalDataService;
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

    private ClinicalDataService clinicalDataService;

    @Autowired
    public ClinicalDataEnrichmentUtil(ClinicalDataService clinicalDataService) {
        this.clinicalDataService = clinicalDataService;
    }

    public List<ClinicalDataEnrichment> createEnrichmentsForNumericData(List<ClinicalAttribute> attributes,
            List<List<Sample>> groupedSamples) {

        List<ClinicalDataEnrichment> clinicalEnrichments = new ArrayList<ClinicalDataEnrichment>();

        // list of values for all NUMBER datatype attributes and for all sample groups
        List<Map<String, List<Double>>> dataByGroupAndByAttribute = groupedSamples.stream()
                .map(groupSamples -> getNumericClinicalData(attributes, groupSamples)).collect(Collectors.toList());

        attributes.forEach(clinicalAttribute -> {

            String attributeKey = getClinicalAttributeKey(clinicalAttribute);

            TransposeDataCollection transposeDataCollection = new TransposeDataCollection();
            int index = 0;
            for (Map<String, List<Double>> entry : dataByGroupAndByAttribute) {
                if (entry.containsKey(attributeKey)) {
                    Collection<Object> values = entry.get(attributeKey).stream().collect(Collectors.toList());
                    // add only groups having values
                    if (values.size() > 0) {
                        transposeDataCollection.put(index++, new FlatDataCollection(values));
                    }
                }
            }
            
            Double totalCount = transposeDataCollection
                    .values()
                    .stream()
                    .flatMap(collection -> collection.stream())
                    .mapToDouble(x -> (Double) x)
                    .sum();

            // perform test only if there are more than one group and
            // total count across all groups in greater than 0
            if (transposeDataCollection.keySet().size() > 1 && totalCount > 0) {
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

        // ClinicalDataCountItem for all STRING datatype attributes and for all sample groups
        List<Map<String, ClinicalDataCountItem>> dataCountsByGroupAndByAttribute = groupedSamples.stream()
                .map(groupSamples -> getClinicalDataCounts(attributes, groupSamples)).collect(Collectors.toList());

        attributes.forEach(clinicalAttribute -> {

            String attributeKey = getClinicalAttributeKey(clinicalAttribute);

            // get counts for all categories in all group for a given attribute
            List<Map<String, Integer>> categoryCountsByGroup = dataCountsByGroupAndByAttribute.stream().map(e -> {
                if (e.containsKey(attributeKey)) {
                    return e.get(attributeKey).getCounts().stream()
                            .collect(Collectors.toMap(ClinicalDataCount::getValue, ClinicalDataCount::getCount));
                }
                return new HashMap<String, Integer>();
            }).collect(Collectors.toList());

            // TODO: should we exclude NA category
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
    private Map<String, List<Double>> getNumericClinicalData(List<ClinicalAttribute> attributes, List<Sample> samples) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        List<String> patientIds = new ArrayList<>();
        for (Sample sample : samples) {
            studyIds.add(sample.getCancerStudyIdentifier());
            sampleIds.add(sample.getStableId());
            patientIds.add(sample.getPatientStableId());
        }

        Map<String, List<Double>> dataByAttribute = new HashMap<>();

        List<ClinicalAttribute> sampleAttributes = new ArrayList<ClinicalAttribute>();
        List<ClinicalAttribute> patientAttributes = new ArrayList<ClinicalAttribute>();

        extractAttributes(attributes, "NUMBER", sampleAttributes, patientAttributes);

        if (!sampleAttributes.isEmpty()) {
            dataByAttribute
                    .putAll(clinicalDataService
                            .fetchClinicalData(studyIds, sampleIds,
                                    sampleAttributes.stream().map(ClinicalAttribute::getAttrId)
                                            .collect(Collectors.toList()),
                                    ClinicalDataType.SAMPLE.name(), "SUMMARY")
                            .stream()
                            // filter are non numeric data to fix
                            // https://github.com/cBioPortal/cbioportal/issues/6228
                            .filter(x -> NumberUtils.isCreatable(x.getAttrValue()))
                            .collect(Collectors.groupingBy(x -> x.getAttrId() + "SAMPLE",
                                    Collectors.mapping(x -> Double.valueOf(x.getAttrValue()), Collectors.toList()))));
        }

        if (!patientAttributes.isEmpty()) {
            dataByAttribute
                    .putAll(clinicalDataService
                            .fetchClinicalData(studyIds, patientIds,
                                    patientAttributes.stream().map(ClinicalAttribute::getAttrId)
                                            .collect(Collectors.toList()),
                                    ClinicalDataType.PATIENT.name(), "SUMMARY")
                            .stream()
                            // filter are non numeric data to fix
                            // https://github.com/cBioPortal/cbioportal/issues/6228
                            .filter(x -> NumberUtils.isCreatable(x.getAttrValue()))
                            .collect(Collectors.groupingBy(x -> x.getAttrId() + "PATIENT",
                                    Collectors.mapping(x -> Double.valueOf(x.getAttrValue()), Collectors.toList()))));
        }

        return dataByAttribute;
    }

    /**
     * get data category counts for all STRING datatype attributes for given samples
     * 
     * @param attributes
     * @param samples
     * @return
     */
    private Map<String, ClinicalDataCountItem> getClinicalDataCounts(List<ClinicalAttribute> attributes,
            List<Sample> samples) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        for (Sample sample : samples) {
            studyIds.add(sample.getCancerStudyIdentifier());
            sampleIds.add(sample.getStableId());
        }

        List<ClinicalDataCountItem> clinicalDataCountItems = new ArrayList<>();

        List<ClinicalAttribute> sampleAttributes = new ArrayList<ClinicalAttribute>();
        List<ClinicalAttribute> patientAttributes = new ArrayList<ClinicalAttribute>();

        extractAttributes(attributes, "STRING", sampleAttributes, patientAttributes);

        if (!sampleAttributes.isEmpty()) {
            clinicalDataCountItems.addAll(clinicalDataService.fetchClinicalDataCounts(studyIds, sampleIds,
                    sampleAttributes.stream().map(ClinicalAttribute::getAttrId).collect(Collectors.toList()),
                    ClinicalDataType.SAMPLE));
        }

        if (!patientAttributes.isEmpty()) {
            clinicalDataCountItems.addAll(clinicalDataService.fetchClinicalDataCounts(studyIds, sampleIds,
                    patientAttributes.stream().map(ClinicalAttribute::getAttrId).collect(Collectors.toList()),
                    ClinicalDataType.PATIENT));
        }

        return clinicalDataCountItems.stream()
                .collect(Collectors.toMap(
                        clinicalDataCountItem -> clinicalDataCountItem.getAttributeId()
                                + clinicalDataCountItem.getClinicalDataType().name(),
                        clinicalDataCountItem -> clinicalDataCountItem));

    }

    private String getClinicalAttributeKey(ClinicalAttribute attribute) {
        return attribute.getAttrId() + (attribute.getPatientAttribute() ? "PATIENT" : "SAMPLE");
    }

    private void extractAttributes(List<ClinicalAttribute> attributes, String datatype,
            List<ClinicalAttribute> sampleAttributes, List<ClinicalAttribute> patientAttributes) {
        attributes.forEach(attribute -> {
            if (attribute.getDatatype().equals(datatype)) {
                (attribute.getPatientAttribute() ? patientAttributes : sampleAttributes).add(attribute);
            }
        });
    }

    // For categorical values, group data is valid if all the values are not 0
    private boolean isValidGroupdData(List<Integer> values) {
        return values.stream().map(value -> (value == 0 ? false : true)).reduce(false,
                (value1, value2) -> value1 || value2);
    }

}
