package org.cbioportal.web.columnar;

import org.cbioportal.model.*;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.utils.config.annotation.ConditionalOnProperty;
import org.cbioportal.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.web.columnar.util.NewClinicalDataBinUtil;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.DataBinner;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;

// BasicDataBinner is a generalized class derived from ClinicalDataBinner
// BasicDataBinner should eventually deprecate ClinicalDataBinner
// we are using BasicDataBinner for genomic data, generic assay, and custom data bin counts now
// but BasicDataBinner can support clinical data counts too
// after we switched clinical data counts to use this, then We can remove ClinicalDataBinner
@Component
@ConditionalOnProperty(name = "clickhouse_mode", havingValue = "true")
public class BasicDataBinner {
    private final StudyViewColumnarService studyViewColumnarService;
    private final DataBinner dataBinner;
    private final CustomDataFilterUtil customDataFilterUtil;
    private final CustomDataService customDataService;
    private final StudyViewFilterUtil studyViewFilterUtil;
    
    @Autowired
    public BasicDataBinner(
        StudyViewColumnarService studyViewColumnarService,
        DataBinner dataBinner,
        CustomDataFilterUtil customDataFilterUtil,
        CustomDataService customDataService,
        StudyViewFilterUtil studyViewFilterUtil
    ) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.dataBinner = dataBinner;
        this.customDataFilterUtil = customDataFilterUtil;
        this.customDataService = customDataService;
        this.studyViewFilterUtil = studyViewFilterUtil;
    }

    // convert from counts to clinical data
    private List<ClinicalData> convertCountsToData(List<ClinicalDataCount> clinicalDataCounts)
    {
        return clinicalDataCounts
            .stream()
            .map(NewClinicalDataBinUtil::generateClinicalDataFromClinicalDataCount)
            .flatMap(Collection::stream)
            .toList();
    }

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public <T extends DataBinCountFilter, S extends DataBinFilter, U extends DataBin> List<U> getDataBins(
        DataBinMethod dataBinMethod,
        T dataBinCountFilter,
        boolean shouldRemoveSelfFromFilter) {
        // get data bin filters based on the type of the filter
        // either Genomic data or Generic Assay data or custom data or clinical data
        List<S> dataBinFilters = fetchDataBinFilters(dataBinCountFilter);
        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();
        // define result variables
        List<U> resultDataBins = Collections.emptyList();
        // if no data bin filters or no study view filer object is passed in
        // return empty result
        if (dataBinFilters.isEmpty() || studyViewFilter == null) {
            return resultDataBins;
        }

        if (shouldRemoveSelfFromFilter && dataBinFilters.size() == 1) {
            removeSelfFromFilter(dataBinFilters.get(0), studyViewFilter);
        }
        
        List<String> uniqueKeys = dataBinFilters.stream().map(this::getDataBinFilterUniqueKey).toList();

        // a new StudyView filter to partially filter by study and sample ids only
        // we need this additional partial filter because we always need to know the bins generated for the initial state
        // which allows us to keep the number of bins and bin ranges consistent even if there are additional data filters.
        // we only want to update the counts for each bin, we don't want to regenerate the bins for the filtered data.
        // NOTE: partial filter is only needed when dataBinMethod == DataBinMethod.STATIC but that's always the case
        // for the frontend implementation. we can't really use dataBinMethod == DataBinMethod.DYNAMIC because of the
        // complication it brings to the frontend visualization and filtering
        StudyViewFilter partialFilter = new StudyViewFilter();
        partialFilter.setStudyIds(studyViewFilter.getStudyIds());
        partialFilter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());

        // we need to fetch data for the partial filter in order to generate the bins for initial state
        // we use the filtered data to calculate the counts for each bin, we do not regenerate bins for the filtered data
        List<ClinicalDataCountItem> unfilteredClinicalDataCounts;
        List<ClinicalDataCountItem> filteredClinicalDataCounts;
        Map<String, ClinicalDataType> attributeDatatypeMap;
        switch (dataBinCountFilter) {
            // TODO: first case is to support clinical data, but clinical data is not using this now. We should update controller to use this method later
            case ClinicalDataBinCountFilter clinicalDataBinCountFilter when !customDataService.getCustomDataSessions(uniqueKeys).isEmpty() -> {
                Map<String, CustomDataSession> customDataSessions = customDataService.getCustomDataSessions(uniqueKeys);
                List<SampleIdentifier> unfilteredSampleIdentifiers = studyViewColumnarService.getFilteredSamples(partialFilter).stream().map(sample -> studyViewFilterUtil.buildSampleIdentifier(sample.getCancerStudyIdentifier(), sample.getStableId())).toList();
                unfilteredClinicalDataCounts = customDataFilterUtil.getCustomDataCounts(unfilteredSampleIdentifiers, customDataSessions);
                List<SampleIdentifier> filteredSampleIdentifiers = studyViewColumnarService.getFilteredSamples(studyViewFilter).stream().map(sample -> studyViewFilterUtil.buildSampleIdentifier(sample.getCancerStudyIdentifier(), sample.getStableId())).toList();
                filteredClinicalDataCounts = customDataFilterUtil.getCustomDataCounts(filteredSampleIdentifiers, customDataSessions);
                attributeDatatypeMap = customDataSessions.entrySet().stream().collect(toMap(
                    Map.Entry::getKey,
                    NewClinicalDataBinUtil::getDataType
                ));
            }
            case ClinicalDataBinCountFilter clinicalDataBinCountFilter -> {
                unfilteredClinicalDataCounts = studyViewColumnarService.getClinicalDataCounts(partialFilter, uniqueKeys);
                filteredClinicalDataCounts = studyViewColumnarService.getClinicalDataCounts(studyViewFilter, uniqueKeys);
                attributeDatatypeMap = studyViewColumnarService.getClinicalAttributeDatatypeMap(studyViewFilter);
            }
            case GenomicDataBinCountFilter genomicDataBinCountFilter -> {
                unfilteredClinicalDataCounts = studyViewColumnarService.getGenomicDataBinCounts(partialFilter, genomicDataBinCountFilter.getGenomicDataBinFilters());
                filteredClinicalDataCounts = studyViewColumnarService.getGenomicDataBinCounts(studyViewFilter, genomicDataBinCountFilter.getGenomicDataBinFilters());
                attributeDatatypeMap = Collections.emptyMap();
            }
            case GenericAssayDataBinCountFilter genericAssayDataBinCountFilter -> {
                unfilteredClinicalDataCounts = studyViewColumnarService.getGenericAssayDataBinCounts(partialFilter, genericAssayDataBinCountFilter.getGenericAssayDataBinFilters());
                filteredClinicalDataCounts = studyViewColumnarService.getGenericAssayDataBinCounts(studyViewFilter, genericAssayDataBinCountFilter.getGenericAssayDataBinFilters());
                attributeDatatypeMap = Collections.emptyMap();
            }
            default -> {
                unfilteredClinicalDataCounts = Collections.emptyList();
                filteredClinicalDataCounts = Collections.emptyList();
                attributeDatatypeMap = Collections.emptyMap();
            }
        }

        // TODO ignoring conflictingPatientAttributeIds for now
        List<ClinicalData> unfilteredClinicalData = convertCountsToData(
            unfilteredClinicalDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );
        List<ClinicalData> filteredClinicalData = convertCountsToData(
            filteredClinicalDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );
        
        Map<String, List<Binnable>> unfilteredClinicalDataByAttributeId =
            unfilteredClinicalData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));

        Map<String, List<Binnable>> filteredClinicalDataByAttributeId =
            filteredClinicalData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));
        
        // TODO: need to update attributeDatatypeMap to include patient level data for Generic Assay Profiles
        if (dataBinMethod == DataBinMethod.STATIC) {
            if (!unfilteredClinicalData.isEmpty()) {
                resultDataBins = calculateStaticDataBins(
                    dataBinner,
                    dataBinFilters,
                    attributeDatatypeMap,
                    unfilteredClinicalDataByAttributeId,
                    filteredClinicalDataByAttributeId
                );
            }
        }
        // TODO: need to update attributeDatatypeMap to include patient level data for Generic Assay Profiles
        else { // dataBinMethod == DataBinMethod.DYNAMIC
            // TODO we should consider removing dynamic binning support
            //  we never use dynamic binning in the frontend because number of bins and the bin ranges can change 
            //  each time there is a new filter which makes the frontend implementation complicated
            if (!filteredClinicalData.isEmpty()) {
                resultDataBins = calculateDynamicDataBins(
                    dataBinner,
                    dataBinFilters,
                    attributeDatatypeMap,
                    filteredClinicalDataByAttributeId
                );
            }
        }

        return resultDataBins;
    }

    private <S extends DataBinFilter> void removeSelfFromFilter(S dataBinFilter, StudyViewFilter studyViewFilter) {
        switch (dataBinFilter) {
            case ClinicalDataBinFilter clinicalDataBinFilter -> {
                if (studyViewFilter.getClinicalDataFilters() != null) {
                    studyViewFilter.getClinicalDataFilters().removeIf(f -> f.getAttributeId().equals(clinicalDataBinFilter.getAttributeId()));
                }
                if (studyViewFilter.getCustomDataFilters() != null) {
                    studyViewFilter.getCustomDataFilters().removeIf(f -> f.getAttributeId().equals(clinicalDataBinFilter.getAttributeId()));
                }
            }
            case GenomicDataBinFilter genomicDataBinFilter when studyViewFilter.getGenomicDataFilters() != null ->
                    studyViewFilter.getGenomicDataFilters().removeIf(f ->
                        f.getHugoGeneSymbol().equals(genomicDataBinFilter.getHugoGeneSymbol())
                            && f.getProfileType().equals(genomicDataBinFilter.getProfileType())
                    );
            case GenericAssayDataBinFilter genericAssayDataBinFilter when studyViewFilter.getGenericAssayDataFilters() != null ->
                    studyViewFilter.getGenericAssayDataFilters().removeIf(f ->
                        f.getStableId().equals(genericAssayDataBinFilter.getStableId())
                            && f.getProfileType().equals(genericAssayDataBinFilter.getProfileType())
                    );
            default -> {
                // Do not remove any filters
            }
        }
    }

    private <S extends DataBinFilter, T extends DataBinCountFilter> List<S> fetchDataBinFilters(T dataBinCountFilter) {
        switch (dataBinCountFilter) {
            case ClinicalDataBinCountFilter clinicalDataBinCountFilter -> {
                return (List<S>) clinicalDataBinCountFilter.getAttributes();
            }
            case GenomicDataBinCountFilter genomicDataBinCountFilter -> {
                return (List<S>) genomicDataBinCountFilter.getGenomicDataBinFilters();
            }
            case GenericAssayDataBinCountFilter genericAssayDataBinCountFilter -> {
                return (List<S>) genericAssayDataBinCountFilter.getGenericAssayDataBinFilters();
            }
            default -> {
                return new ArrayList<>();
            }
        }
    }
    
    private <S extends DataBinFilter> String getDataBinFilterUniqueKey(S dataBinFilter) {
        switch (dataBinFilter) {
            case ClinicalDataBinFilter clinicalDataBinFilter -> {
                return clinicalDataBinFilter.getAttributeId();
            }
            case GenomicDataBinFilter genomicDataBinFilter -> {
                return genomicDataBinFilter.getHugoGeneSymbol() + genomicDataBinFilter.getProfileType();
            }
            case GenericAssayDataBinFilter genericAssayDataBinFilter -> {
                return genericAssayDataBinFilter.getStableId() + genericAssayDataBinFilter.getProfileType();
            }
            default -> {
                return null;
            }
        }
    }

    private <T extends DataBinFilter, U extends DataBin> List<U> calculateStaticDataBins(
        DataBinner dataBinner,
        List<T> dataBinFilters,
        Map<String, ClinicalDataType> attributeDatatypeMap,
        Map<String, List<Binnable>> unfilteredClinicalDataByAttributeId,
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId
    ) {
        List<U> result = new ArrayList<>();

        for (T dataBinFilter : dataBinFilters) {
            // if there is data for requested attribute
            if (attributeDatatypeMap.isEmpty() || attributeDatatypeMap.containsKey(getDataBinFilterUniqueKey(dataBinFilter))) {
                List<U> dataBins = dataBinner
                    .calculateClinicalDataBins(
                        dataBinFilter,
                        filteredClinicalDataByAttributeId.getOrDefault(getDataBinFilterUniqueKey(dataBinFilter), emptyList()),
                        unfilteredClinicalDataByAttributeId.getOrDefault(getDataBinFilterUniqueKey(dataBinFilter), emptyList())
                    )
                    .stream()
                    .map(dataBin -> (U) transform(dataBinFilter, dataBin))
                    .toList();

                result.addAll(dataBins);
            }
        }

        return result;
    }

    private <T extends DataBinFilter, U extends DataBin> List<U> calculateDynamicDataBins(
        DataBinner dataBinner,
        List<T> dataBinFilters,
        Map<String, ClinicalDataType> attributeDatatypeMap,
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId
    ) {
        List<U> result = new ArrayList<>();

        for (T dataBinFilter : dataBinFilters) {
            // if there is data for requested attribute
            if (attributeDatatypeMap.isEmpty() || attributeDatatypeMap.containsKey(getDataBinFilterUniqueKey(dataBinFilter))) {
                List<U> dataBins = dataBinner
                    .calculateDataBins(
                        dataBinFilter,
                        filteredClinicalDataByAttributeId.getOrDefault(getDataBinFilterUniqueKey(dataBinFilter), emptyList())
                    )
                    .stream()
                    .map(dataBin -> (U) transform(dataBinFilter, dataBin))
                    .toList();
                result.addAll(dataBins);
            }
        }

        return result;
    }

    private <T extends DataBin, S extends DataBinFilter> T transform(S dataBinFilter, DataBin dataBin) {
        switch (dataBinFilter) {
            case ClinicalDataBinFilter clinicalDataBinFilter -> {
                return (T) dataBinToClinicalDataBin(clinicalDataBinFilter, dataBin);
            }
            case GenomicDataBinFilter genomicDataBinFilter -> {
                return (T) dataBintoGenomicDataBin(genomicDataBinFilter, dataBin);
            }
            case GenericAssayDataBinFilter genericAssayDataBinFilter -> {
                return (T) dataBintoGenericAssayDataBin(genericAssayDataBinFilter, dataBin);
            }
            default -> {
                return null;
            }
        }
    }

    private ClinicalDataBin dataBinToClinicalDataBin(ClinicalDataBinFilter attribute, DataBin dataBin) {
        ClinicalDataBin clinicalDataBin = new ClinicalDataBin();
        clinicalDataBin.setAttributeId(attribute.getAttributeId());
        setCommonDataBinProperties(dataBin, clinicalDataBin);
        return clinicalDataBin;
    }

    private GenomicDataBin dataBintoGenomicDataBin(GenomicDataBinFilter genomicDataBinFilter, DataBin dataBin) {
        GenomicDataBin genomicDataBin = new GenomicDataBin();
        genomicDataBin.setHugoGeneSymbol(genomicDataBinFilter.getHugoGeneSymbol());
        genomicDataBin.setProfileType(genomicDataBinFilter.getProfileType());
        setCommonDataBinProperties(dataBin, genomicDataBin);
        return genomicDataBin;
    }

    private GenericAssayDataBin dataBintoGenericAssayDataBin(GenericAssayDataBinFilter genericAssayDataBinFilter,
                                                             DataBin dataBin) {
        GenericAssayDataBin genericAssayDataBin = new GenericAssayDataBin();
        genericAssayDataBin.setStableId(genericAssayDataBinFilter.getStableId());
        genericAssayDataBin.setProfileType(genericAssayDataBinFilter.getProfileType());
        setCommonDataBinProperties(dataBin, genericAssayDataBin);
        return genericAssayDataBin;
    }
    
    private <U extends DataBin> void setCommonDataBinProperties(DataBin originalDataBin, U targetDatabin) {
        targetDatabin.setCount(originalDataBin.getCount());
        if (originalDataBin.getSpecialValue() != null) {
            targetDatabin.setSpecialValue(originalDataBin.getSpecialValue());
        }
        if (originalDataBin.getStart() != null) {
            targetDatabin.setStart(originalDataBin.getStart());
        }
        if (originalDataBin.getEnd() != null) {
            targetDatabin.setEnd(originalDataBin.getEnd());
        }
    }
    
}
