package org.cbioportal.web.util;

import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.service.AttributeByStudyService;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.util.BinnableCustomDataValue;
import org.cbioportal.service.util.CustomAttributeWithData;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.service.util.CustomDataValue;
import org.cbioportal.web.columnar.util.NewClinicalDataBinUtil;
import org.cbioportal.web.parameter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class ClinicalDataBinUtil {

    @Autowired
    private AttributeByStudyService clinicalAttributeService;
    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private ClinicalDataFetcher clinicalDataFetcher;
    @Autowired
    private DataBinner dataBinner;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private CustomDataService customDataService;
    @Autowired
    private IdPopulator idPopulator;

    public StudyViewFilter removeSelfFromFilter(ClinicalDataBinCountFilter dataBinCountFilter) {
        return NewClinicalDataBinUtil.removeSelfFromFilter(dataBinCountFilter);
    }

    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        DataBinMethod dataBinMethod,
        ClinicalDataBinCountFilter dataBinCountFilter
    ) {
        return this.fetchClinicalDataBinCounts(
            dataBinMethod,
            dataBinCountFilter,
            // by default call the method to remove self from filter
            true
        );
    }

    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        DataBinMethod dataBinMethod,
        ClinicalDataBinCountFilter dataBinCountFilter,
        boolean shouldRemoveSelfFromFilter
    ) {
        StudyViewFilter studyViewFilter = toStudyViewFilter(dataBinCountFilter, shouldRemoveSelfFromFilter);
        List<SampleIdentifier> unfilteredSamples = filterByStudyAndSample(studyViewFilter);
        List<String> attributeIds = toAttributeIds(dataBinCountFilter.getAttributes());
        List<ClinicalAttribute> clinicalAttributes = fetchClinicalAttributes(attributeIds, unfilteredSamples);
        BinningIds binningIds = idPopulator.populateIdLists(unfilteredSamples, clinicalAttributes);
        Map<String, ClinicalDataType> attributeByDatatype = toAttributeDatatypeMap(binningIds);
        BinningData<Binnable> unfilteredData = (BinningData<Binnable>) (BinningData<? extends Binnable>) fetchBinningData(binningIds);
        return createBins(
            dataBinMethod, 
            dataBinCountFilter, 
            studyViewFilter,
            attributeByDatatype, 
            clinicalAttributes,
            binningIds, 
            unfilteredSamples,
            unfilteredData
        );
    }

    public List<ClinicalDataBin> fetchCustomDataBinCounts(
        DataBinMethod dataBinMethod,
        ClinicalDataBinCountFilter dataBinCountFilter,
        boolean shouldRemoveSelfFromFilter
    ) {
        List<String> attributeIds = toAttributeIds(dataBinCountFilter.getAttributes());
        Map<String, CustomDataSession> customDataSessions = customDataService.getCustomDataSessions(attributeIds);
        Map<String, List<Binnable>> customDataByAttributeId = createCustomDataByAttributeId(customDataSessions);
        Map<String, ClinicalDataType> customAttributeByDatatype = createCustomAttributeDatatypeMap(customDataSessions);

        StudyViewFilter studyViewFilter = toStudyViewFilter(dataBinCountFilter, shouldRemoveSelfFromFilter);
        List<SampleIdentifier> unfilteredSamples = filterByStudyAndSample(studyViewFilter);
        List<ClinicalAttribute> customDataAttributes = toCustomAttributes(customDataSessions);
            
        BinningIds unfilteredIds = idPopulator.populateIdLists(unfilteredSamples, customDataAttributes);
        BinningData<Binnable> unfilteredData = fetchCustomBinningData(customDataByAttributeId, unfilteredIds);

        return createBins(
            dataBinMethod, 
            dataBinCountFilter, 
            studyViewFilter,
            customAttributeByDatatype, 
            customDataAttributes, 
            unfilteredIds, 
            unfilteredSamples,
            unfilteredData
        );
    }

    private List<ClinicalDataBin> createBins(
        DataBinMethod dataBinMethod,
        ClinicalDataBinCountFilter dataBinCountFilter,
        StudyViewFilter studyViewFilter,
        Map<String, ClinicalDataType> clinicalDataAttributeDatatypeMap,
        List<ClinicalAttribute> clinicalAttributes,
        BinningIds unfilteredIds,
        List<SampleIdentifier> unfilteredSampleIds,
        BinningData<Binnable> unfilteredData
    ) {
        List<SampleIdentifier> filteredSampleIds = filterSampleIds(studyViewFilter, unfilteredSampleIds);

        BinningIds filteredIds;
        List<Binnable> filteredClinicalData;
        if (filteredSampleIds.equals(unfilteredSampleIds)) {
            // if filtered and unfiltered samples are exactly the same, no need to fetch clinical data again:
            filteredIds = new BinningIds(unfilteredIds);
            filteredClinicalData = unfilteredData.getAllData();
        } else {
            filteredIds = idPopulator.populateIdLists(filteredSampleIds, clinicalAttributes);
            filteredClinicalData = filterClinicalData(unfilteredData, filteredIds);
        }

        List<ClinicalDataBinFilter> attributes = dataBinCountFilter.getAttributes();
        if (dataBinMethod == DataBinMethod.STATIC) {
            if (unfilteredSampleIds.isEmpty() || unfilteredData.getAllData().isEmpty()) {
                return emptyList();
            }
            return calculateStaticDataBins(
                attributes,
                clinicalDataAttributeDatatypeMap,
                toClinicalDataByAttributeId(unfilteredData.getAllData()),
                toClinicalDataByAttributeId(filteredClinicalData),
                unfilteredIds.getUniqueSampleKeys(),
                unfilteredIds.getUniquePatientKeys(),
                filteredIds.getUniqueSampleKeys(),
                filteredIds.getUniquePatientKeys()
            );
        } else { // dataBinMethod == DataBinMethod.DYNAMIC
            if (filteredClinicalData.isEmpty()) {
                return emptyList();
            }
            return calculateDynamicDataBins(
                attributes,
                clinicalDataAttributeDatatypeMap,
                toClinicalDataByAttributeId(filteredClinicalData),
                filteredIds.getUniqueSampleKeys(),
                filteredIds.getUniquePatientKeys()
            );
        }
    }

    private List<ClinicalAttribute> toCustomAttributes(
        Map<String, CustomDataSession> customDataSessions
    ) {
        return customDataSessions
            .entrySet()
            .stream()
            .map(e -> toClinicalAttribute(e.getKey(), e.getValue().getData()))
            .collect(toList());
    }

    private ClinicalAttribute toClinicalAttribute(String key, CustomAttributeWithData data) {
        ClinicalAttribute result = new ClinicalAttribute();
        result.setPatientAttribute(data.getPatientAttribute());
        result.setAttrId(key);
        result.setDatatype(data.getDatatype());
        return result;
    }

    private Map<String, List<Binnable>> toClinicalDataByAttributeId(List<Binnable> unfilteredData) {
        return unfilteredData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));
    }

    private List<Binnable> filterClinicalData(
        BinningData<Binnable> unfilteredData, 
        BinningIds filteredIds
    ) {
        return studyViewFilterUtil.filterClinicalData(
            unfilteredData.samples,
            unfilteredData.patients,
            unfilteredData.conflictingPatientAttributes,
            filteredIds.getStudyIds(),
            filteredIds.getSampleIds(),
            filteredIds.getStudyIdsOfPatients(),
            filteredIds.getPatientIds(),
            filteredIds.getSampleAttributeIds(),
            filteredIds.getPatientAttributeIds(),
            filteredIds.getConflictingPatientAttributeIds()
        );
    }

    private List<SampleIdentifier> filterSampleIds(StudyViewFilter studyViewFilter, List<SampleIdentifier> unfilteredSampleIds) {
        return studyViewFilterUtil.shouldSkipFilterForClinicalDataBins(studyViewFilter)
            ? unfilteredSampleIds
            : studyViewFilterApplier.apply(studyViewFilter);
    }

    private List<String> toAttributeIds(List<ClinicalDataBinFilter> dataBinCountFilter) {
        return dataBinCountFilter.stream()
            .map(ClinicalDataBinFilter::getAttributeId).collect(toList());
    }

    private StudyViewFilter toStudyViewFilter(ClinicalDataBinCountFilter dataBinCountFilter, boolean shouldRemoveSelfFromFilter) {
        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();

        if (shouldRemoveSelfFromFilter) {
            studyViewFilter = removeSelfFromFilter(dataBinCountFilter);
        }
        return studyViewFilter;
    }

    private Map<String, List<Binnable>> createCustomDataByAttributeId(Map<String, CustomDataSession> customDataSessions) {
        return customDataSessions.entrySet().stream()
            .collect(toMap(
                Map.Entry::getKey,
                entry -> entry
                    .getValue()
                    .getData()
                    .getData()
                    .stream()
                    .map(mapCustomToBinnable(entry))
                    .collect(toList())
            ));
    }

    private BinningData<ClinicalData> fetchBinningData(BinningIds binningIds) {
        List<ClinicalData> samples = clinicalDataFetcher.fetchClinicalDataForSamples(
            binningIds.getStudyIds(),
            binningIds.getSampleIds(),
            binningIds.getSampleAttributeIds()
        );

        List<ClinicalData> patients = clinicalDataFetcher.fetchClinicalDataForPatients(
            binningIds.getStudyIdsOfPatients(),
            binningIds.getPatientIds(),
            binningIds.getPatientAttributeIds()
        );

        List<ClinicalData> conflictingPatientAttributes = clinicalDataFetcher.fetchClinicalDataForConflictingPatientAttributes(
            binningIds.getStudyIdsOfPatients(),
            binningIds.getPatientIds(),
            binningIds.getConflictingPatientAttributeIds()
        );
        return new BinningData<>(samples, patients, conflictingPatientAttributes);
    }
    
    private BinningData<Binnable> fetchCustomBinningData(
        Map<String, List<Binnable>> clinicalDataByAttributeId, 
        BinningIds binningIds
    ) {
        List<Binnable> clinicalDataForPatients = clinicalDataByAttributeId
            .values()
            .stream()
            .filter(e -> e.get(0).isPatientAttribute())
            .flatMap(List::stream)
            .collect(toList());

        List<Binnable> clinicalDataForSamples = clinicalDataByAttributeId
            .values()
            .stream()
            .filter(e -> !e.get(0).isPatientAttribute())
            .flatMap(List::stream)
            .collect(toList());

        List<ClinicalData> unfilteredClinicalDataForConflictingPatientAttributes = clinicalDataFetcher.fetchClinicalDataForConflictingPatientAttributes(
            binningIds.getStudyIdsOfPatients(),
            binningIds.getPatientIds(),
            binningIds.getConflictingPatientAttributeIds()
        );
        return new BinningData<>(
            clinicalDataForSamples,
            clinicalDataForPatients,
            (List<Binnable>) (List<? extends Binnable>) unfilteredClinicalDataForConflictingPatientAttributes
        );
    }


    private Function<CustomDataValue, Binnable> mapCustomToBinnable(Map.Entry<String, CustomDataSession> entry) {
        return customDataValue -> {
            final String attributeId = entry.getKey();
            final Boolean patientAttribute = entry.getValue().getData().getPatientAttribute();
            return new BinnableCustomDataValue(
                customDataValue, 
                attributeId, 
                patientAttribute
            );
        };
    }

    public List<ClinicalDataBin> calculateStaticDataBins(
        List<ClinicalDataBinFilter> attributes, 
        Map<String, ClinicalDataType> attributeDatatypeMap,
        Map<String, List<Binnable>> unfilteredClinicalDataByAttributeId,
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId,
        List<String> unfilteredUniqueSampleKeys,
        List<String> unfilteredUniquePatientKeys,
        List<String> filteredUniqueSampleKeys,
        List<String> filteredUniquePatientKeys
    ) {
        List<ClinicalDataBin> clinicalDataBins = new ArrayList<>();

        for (ClinicalDataBinFilter attribute : attributes) {
            if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                List<String> filteredIds = clinicalDataType == ClinicalDataType.PATIENT ? filteredUniquePatientKeys
                    : filteredUniqueSampleKeys;
                List<String> unfilteredIds = clinicalDataType == ClinicalDataType.PATIENT
                    ? unfilteredUniquePatientKeys
                    : unfilteredUniqueSampleKeys;

                List<ClinicalDataBin> dataBins = dataBinner
                    .calculateClinicalDataBins(attribute, clinicalDataType,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                            emptyList()),
                        unfilteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                            emptyList()),
                        filteredIds, unfilteredIds)
                    .stream()
                    .map(dataBin -> NewClinicalDataBinUtil.dataBinToClinicalDataBin(attribute, dataBin))
                    .toList();

                clinicalDataBins.addAll(dataBins);
            }
        }

        return clinicalDataBins;
    }

    public List<ClinicalDataBin> calculateDynamicDataBins(
        List<ClinicalDataBinFilter> attributes,
        Map<String, ClinicalDataType> attributeDatatypeMap,
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId,
        List<String> filteredUniqueSampleKeys,
        List<String> filteredUniquePatientKeys
    ) {
        List<ClinicalDataBin> clinicalDataBins = new ArrayList<>();

        for (ClinicalDataBinFilter attribute : attributes) {

            // if there is clinical data for requested attribute
            if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                List<String> filteredIds = clinicalDataType == ClinicalDataType.PATIENT
                    ? filteredUniquePatientKeys
                    : filteredUniqueSampleKeys;

                List<ClinicalDataBin> dataBins = dataBinner
                    .calculateDataBins(attribute, clinicalDataType,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(),
                            emptyList()),
                        filteredIds)
                    .stream()
                    .map(dataBin -> NewClinicalDataBinUtil.dataBinToClinicalDataBin(attribute, dataBin))
                    .toList();
                clinicalDataBins.addAll(dataBins);
            }
        }

        return clinicalDataBins;
    }
    private Map<String, ClinicalDataType> toAttributeDatatypeMap(BinningIds binningIds) {
        return toAttributeDatatypeMap(
            binningIds.getSampleAttributeIds(),
            binningIds.getPatientAttributeIds(),
            binningIds.getConflictingPatientAttributeIds()
        );
    }
    public Map<String, ClinicalDataType> toAttributeDatatypeMap(
        List<String> sampleAttributeIds,
        List<String> patientAttributeIds,
        List<String> conflictingPatientAttributeIds
    ) {
        return NewClinicalDataBinUtil.toAttributeDatatypeMap(
            sampleAttributeIds,
            patientAttributeIds,
            conflictingPatientAttributeIds
        );
    }

    private Map<String, ClinicalDataType> createCustomAttributeDatatypeMap(
        Map<String, CustomDataSession> customDataSessions
    ) {
        return customDataSessions.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            ClinicalDataBinUtil::getDataType
        ));
    }

    public List<SampleIdentifier> filterByStudyAndSample(
        StudyViewFilter studyViewFilter
    ) {
        StudyViewFilter filter = null;

        // only filter by study id and sample identifiers
        if (studyViewFilter != null) {
            filter = new StudyViewFilter();
            filter.setStudyIds(studyViewFilter.getStudyIds());
            filter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());
        }

        return studyViewFilterApplier.apply(filter);
    }

    private static ClinicalDataType getDataType(Map.Entry<String, CustomDataSession> entry) {
        return entry.getValue().getData().getPatientAttribute() ? ClinicalDataType.PATIENT : ClinicalDataType.SAMPLE;
    }
    
    private List<ClinicalAttribute> fetchClinicalAttributes(List<String> attributeIds, List<SampleIdentifier> unfilteredSamples) {

        List<String> studyIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();
        studyViewFilterUtil.extractStudyAndSampleIds(
            unfilteredSamples,
            studyIds,
            sampleIds
        );
        
       return clinicalAttributeService.getClinicalAttributesByStudyIdsAndAttributeIds(studyIds, attributeIds);
    }
    
}
