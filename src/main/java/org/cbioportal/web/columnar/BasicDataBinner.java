package org.cbioportal.web.columnar;

import org.cbioportal.model.*;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.web.columnar.util.NewClinicalDataBinUtil;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.DataBinner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Component
public class BasicDataBinner {
    private final StudyViewColumnarService studyViewColumnarService;
    private final DataBinner dataBinner;
    
    @Autowired
    public BasicDataBinner(
        StudyViewColumnarService studyViewColumnarService,
        DataBinner dataBinner
    ) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.dataBinner = dataBinner;
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
        // either Genomic data or Generic Assay data or clinical data
        List<S> dataBinFilters = fetchDataBinFilters(dataBinCountFilter);
        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();

        if (shouldRemoveSelfFromFilter && dataBinFilters.size() == 1) {
            removeSelfFromFilter(dataBinFilters.get(0), studyViewFilter);
        }
        
        List<String> uniqueKeys = dataBinFilters.stream().map(this::getDataBinFilterUniqueKey).collect(Collectors.toList());

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
            case ClinicalDataBinCountFilter clinicalDataBinCountFilter -> {
                unfilteredClinicalDataCounts = studyViewColumnarService.getClinicalDataCounts(partialFilter, uniqueKeys);
                filteredClinicalDataCounts = studyViewColumnarService.getClinicalDataCounts(studyViewFilter, uniqueKeys);
                attributeDatatypeMap = studyViewColumnarService.getClinicalAttributeDatatypeMap();
            }
            case GenomicDataBinCountFilter genomicDataBinCountFilter -> {
                unfilteredClinicalDataCounts = studyViewColumnarService.getGenomicDataBinCounts(partialFilter, uniqueKeys);
                filteredClinicalDataCounts = studyViewColumnarService.getGenomicDataBinCounts(studyViewFilter, uniqueKeys);
                attributeDatatypeMap = Collections.emptyMap();
            }
            case GenericAssayDataBinCountFilter genericAssayDataBinCountFilter -> {
                unfilteredClinicalDataCounts = studyViewColumnarService.getGenericAssayDataBinCounts(partialFilter, uniqueKeys);
                filteredClinicalDataCounts = studyViewColumnarService.getGenericAssayDataBinCounts(studyViewFilter, uniqueKeys);
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

        // Define result variables
        List<U> resultDataBins = Collections.emptyList();

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
        if (studyViewFilter != null) {
            if (dataBinFilter instanceof ClinicalDataBinFilter clinicalDataBinFilter &&
                studyViewFilter.getClinicalDataFilters() != null) {
                studyViewFilter.getClinicalDataFilters().removeIf(f -> f.getAttributeId().equals(clinicalDataBinFilter.getAttributeId()));
            } else if (dataBinFilter instanceof GenomicDataBinFilter genomicDataBinFilter &&
                studyViewFilter.getGenomicDataFilters() != null) {
                studyViewFilter.getGenomicDataFilters().removeIf(f ->
                    f.getHugoGeneSymbol().equals(genomicDataBinFilter.getHugoGeneSymbol())
                        && f.getProfileType().equals(genomicDataBinFilter.getProfileType())
                );
            } else if (dataBinFilter instanceof GenericAssayDataBinFilter genericAssayDataBinFilter &&
                studyViewFilter.getGenericAssayDataFilters() != null) {
                studyViewFilter.getGenericAssayDataFilters().removeIf(f ->
                    f.getStableId().equals(genericAssayDataBinFilter.getStableId())
                        && f.getProfileType().equals(genericAssayDataBinFilter.getProfileType())
                );
            }
        }
    }

    private <S extends DataBinFilter, T extends DataBinCountFilter> List<S> fetchDataBinFilters(T dataBinCountFilter) {
        if (dataBinCountFilter instanceof ClinicalDataBinCountFilter) {
            return (List<S>) ((ClinicalDataBinCountFilter) dataBinCountFilter).getAttributes();
        } else if (dataBinCountFilter instanceof GenomicDataBinCountFilter) {
            return (List<S>) ((GenomicDataBinCountFilter) dataBinCountFilter).getGenomicDataBinFilters();
        } else if (dataBinCountFilter instanceof GenericAssayDataBinCountFilter) {
            return (List<S>) ((GenericAssayDataBinCountFilter) dataBinCountFilter).getGenericAssayDataBinFilters();
        }
        return new ArrayList<>();
    }
    
    private <S extends DataBinFilter> String getDataBinFilterUniqueKey(S dataBinFilter) {
        if (dataBinFilter instanceof ClinicalDataBinFilter clinicalDataBinFilter) {
            return clinicalDataBinFilter.getAttributeId();
        } else if (dataBinFilter instanceof GenomicDataBinFilter genomicDataBinFilter) {
            return genomicDataBinFilter.getHugoGeneSymbol() + genomicDataBinFilter.getProfileType();
        } else if (dataBinFilter instanceof GenericAssayDataBinFilter genericAssayDataBinFilter) {
            return genericAssayDataBinFilter.getStableId() + genericAssayDataBinFilter.getProfileType();
        }
        return null;
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
            // TODO: consider if this is correct to passing in a empty map
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
            // TODO: consider if this is correct to passing in a empty map
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
        if (dataBinFilter instanceof  ClinicalDataBinFilter clinicalDataBinFilter) {
            return (T) dataBinToClinicalDataBin(clinicalDataBinFilter, dataBin);
        } else if (dataBinFilter instanceof GenomicDataBinFilter genomicDataBinFilter) {
            return (T) dataBintoGenomicDataBin(genomicDataBinFilter, dataBin);
        } else if (dataBinFilter instanceof GenericAssayDataBinFilter genericAssayDataBinFilter) {
            return (T) dataBintoGenericAssayDataBin(genericAssayDataBinFilter, dataBin);
        }
        return null;
    }

    private ClinicalDataBin dataBinToClinicalDataBin(ClinicalDataBinFilter attribute, DataBin dataBin) {
        ClinicalDataBin clinicalDataBin = new ClinicalDataBin();
        clinicalDataBin.setAttributeId(attribute.getAttributeId());
        clinicalDataBin.setCount(dataBin.getCount());
        if (dataBin.getEnd() != null) {
            clinicalDataBin.setEnd(dataBin.getEnd());
        }
        if (dataBin.getSpecialValue() != null) {
            clinicalDataBin.setSpecialValue(dataBin.getSpecialValue());
        }
        if (dataBin.getStart() != null) {
            clinicalDataBin.setStart(dataBin.getStart());
        }
        return clinicalDataBin;
    }

    private GenomicDataBin dataBintoGenomicDataBin(GenomicDataBinFilter genomicDataBinFilter, DataBin dataBin) {
        GenomicDataBin genomicDataBin = new GenomicDataBin();
        genomicDataBin.setCount(dataBin.getCount());
        genomicDataBin.setHugoGeneSymbol(genomicDataBinFilter.getHugoGeneSymbol());
        genomicDataBin.setProfileType(genomicDataBinFilter.getProfileType());
        if (dataBin.getSpecialValue() != null) {
            genomicDataBin.setSpecialValue(dataBin.getSpecialValue());
        }
        if (dataBin.getStart() != null) {
            genomicDataBin.setStart(dataBin.getStart());
        }
        if (dataBin.getEnd() != null) {
            genomicDataBin.setEnd(dataBin.getEnd());
        }
        return genomicDataBin;
    }

    private GenericAssayDataBin dataBintoGenericAssayDataBin(GenericAssayDataBinFilter genericAssayDataBinFilter,
                                                             DataBin dataBin) {
        GenericAssayDataBin genericAssayDataBin = new GenericAssayDataBin();
        genericAssayDataBin.setCount(dataBin.getCount());
        genericAssayDataBin.setStableId(genericAssayDataBinFilter.getStableId());
        genericAssayDataBin.setProfileType(genericAssayDataBinFilter.getProfileType());
        if (dataBin.getSpecialValue() != null) {
            genericAssayDataBin.setSpecialValue(dataBin.getSpecialValue());
        }
        if (dataBin.getStart() != null) {
            genericAssayDataBin.setStart(dataBin.getStart());
        }
        if (dataBin.getEnd() != null) {
            genericAssayDataBin.setEnd(dataBin.getEnd());
        }
        return genericAssayDataBin;
    }

}
