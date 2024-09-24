package org.cbioportal.web.columnar;

import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.service.CustomDataService;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.service.util.CustomDataSession;
import org.cbioportal.web.columnar.util.CustomDataFilterUtil;
import org.cbioportal.web.columnar.util.NewClinicalDataBinUtil;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.DataBinMethod;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.util.DataBinner;
import org.cbioportal.web.util.StudyViewFilterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Component
public class ClinicalDataBinner {
    private final StudyViewColumnarService studyViewColumnarService;
    private final DataBinner dataBinner;
    private final CustomDataService customDataService;
    private final StudyViewFilterUtil studyViewFilterUtil;
    private final CustomDataFilterUtil customDataFilterUtil;

    @Autowired
    public ClinicalDataBinner(
        StudyViewColumnarService studyViewColumnarService,
        DataBinner dataBinner,
        CustomDataService customDataService,
        StudyViewFilterUtil studyViewFilterUtil,
        CustomDataFilterUtil customDataFilterUtil
    ) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.dataBinner = dataBinner;
        this.customDataService = customDataService;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.customDataFilterUtil = customDataFilterUtil;
    }

    private List<ClinicalData> convertCountsToData(List<ClinicalDataCount> clinicalDataCounts)
    {
        return clinicalDataCounts
            .stream()
            .map(NewClinicalDataBinUtil::generateClinicalDataFromClinicalDataCount)
            .flatMap(Collection::stream)
            .toList();
    }
    
    public List<ClinicalDataBin> fetchClinicalDataBinCounts(
        DataBinMethod dataBinMethod,
        ClinicalDataBinCountFilter dataBinCountFilter,
        boolean shouldRemoveSelfFromFilter
    ) {
        List<ClinicalDataBinFilter> attributes = dataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();

        if (shouldRemoveSelfFromFilter) {
            studyViewFilter = NewClinicalDataBinUtil.removeSelfFromFilter(dataBinCountFilter);
        }

        List<String> attributeIds = attributes.stream().map(ClinicalDataBinFilter::getAttributeId).collect(Collectors.toList());

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

        // we need the clinical data for the partial filter in order to generate the bins for initial state
        // we use the filtered data to calculate the counts for each bin, we do not regenerate bins for the filtered data
        List<ClinicalDataCountItem> unfilteredClinicalDataCounts = studyViewColumnarService.getClinicalDataCounts(partialFilter, attributeIds);
        List<ClinicalDataCountItem> filteredClinicalDataCounts = studyViewColumnarService.getClinicalDataCounts(studyViewFilter, attributeIds);

        // TODO ignoring conflictingPatientAttributeIds for now
        List<ClinicalData> unfilteredClinicalData = convertCountsToData(
            unfilteredClinicalDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );
        List<ClinicalData> filteredClinicalData = convertCountsToData(
            filteredClinicalDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );

        Map<String, ClinicalDataType> attributeDatatypeMap = studyViewColumnarService.getClinicalAttributeDatatypeMap();

        Map<String, List<Binnable>> unfilteredClinicalDataByAttributeId =
            unfilteredClinicalData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));

        Map<String, List<Binnable>> filteredClinicalDataByAttributeId =
            filteredClinicalData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));

        List<ClinicalDataBin> clinicalDataBins = Collections.emptyList();

        if (dataBinMethod == DataBinMethod.STATIC) {
            if (!unfilteredClinicalData.isEmpty()) {
                clinicalDataBins = NewClinicalDataBinUtil.calculateStaticDataBins(
                    dataBinner,
                    attributes,
                    attributeDatatypeMap,
                    unfilteredClinicalDataByAttributeId,
                    filteredClinicalDataByAttributeId
                );
            }
        }
        else { // dataBinMethod == DataBinMethod.DYNAMIC
            // TODO we should consider removing dynamic binning support
            //  we never use dynamic binning in the frontend because number of bins and the bin ranges can change 
            //  each time there is a new filter which makes the frontend implementation complicated
            if (!filteredClinicalData.isEmpty()) {
                clinicalDataBins = NewClinicalDataBinUtil.calculateDynamicDataBins(
                    dataBinner,
                    attributes,
                    attributeDatatypeMap,
                    filteredClinicalDataByAttributeId
                );
            }
        }

        return clinicalDataBins;
    }

    @Cacheable(cacheResolver = "generalRepositoryCacheResolver", condition = "@cacheEnabledConfig.getEnabled()")
    public List<ClinicalDataBin> fetchCustomDataBinCounts(
        DataBinMethod dataBinMethod,
        ClinicalDataBinCountFilter dataBinCountFilter,
        boolean shouldRemoveSelfFromFilter
    ) {
        List<ClinicalDataBinFilter> attributes = dataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();

        if (shouldRemoveSelfFromFilter) {
            studyViewFilter = NewClinicalDataBinUtil.removeSelfCustomDataFromFilter(dataBinCountFilter);
        }

        List<String> attributeIds = attributes.stream().map(ClinicalDataBinFilter::getAttributeId).collect(Collectors.toList());

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

        Map<String, CustomDataSession> customDataSessions = customDataService.getCustomDataSessions(attributeIds);
        Map<String, ClinicalDataType> customAttributeByDatatype = customDataSessions.entrySet().stream().collect(toMap(
            Map.Entry::getKey,
            NewClinicalDataBinUtil::getDataType
        ));

        // we need the clinical data for the partial filter in order to generate the bins for initial state
        // we use the filtered data to calculate the counts for each bin, we do not regenerate bins for the filtered data
        List<SampleIdentifier> unfilteredSampleIdentifiers = studyViewColumnarService.getFilteredSamples(partialFilter).stream().map(sample -> studyViewFilterUtil.buildSampleIdentifier(sample.getCancerStudyIdentifier(), sample.getStableId())).collect(Collectors.toList());
        List<ClinicalDataCountItem> unfilteredCustomDataCounts = customDataFilterUtil.getCustomDataCounts(unfilteredSampleIdentifiers, customDataSessions);
        List<SampleIdentifier> filteredSampleIdentifiers = studyViewColumnarService.getFilteredSamples(studyViewFilter).stream().map(sample -> studyViewFilterUtil.buildSampleIdentifier(sample.getCancerStudyIdentifier(), sample.getStableId())).collect(Collectors.toList());
        List<ClinicalDataCountItem> filteredCustomDataCounts = customDataFilterUtil.getCustomDataCounts(filteredSampleIdentifiers, customDataSessions);

        // TODO ignoring conflictingPatientAttributeIds for now
        List<ClinicalData> unfilteredCustomData = convertCountsToData(
            unfilteredCustomDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );
        List<ClinicalData> filteredCustomData = convertCountsToData(
            filteredCustomDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );

        Map<String, List<Binnable>> unfilteredClinicalDataByAttributeId =
            unfilteredCustomData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));

        Map<String, List<Binnable>> filteredClinicalDataByAttributeId =
            filteredCustomData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));

        List<ClinicalDataBin> clinicalDataBins = Collections.emptyList();

        if (dataBinMethod == DataBinMethod.STATIC) {
            if (!unfilteredCustomData.isEmpty()) {
                clinicalDataBins = NewClinicalDataBinUtil.calculateStaticDataBins(
                    dataBinner,
                    attributes,
                    customAttributeByDatatype,
                    unfilteredClinicalDataByAttributeId,
                    filteredClinicalDataByAttributeId
                );
            }
        }
        else { // dataBinMethod == DataBinMethod.DYNAMIC
            // TODO we should consider removing dynamic binning support
            //  we never use dynamic binning in the frontend because number of bins and the bin ranges can change 
            //  each time there is a new filter which makes the frontend implementation complicated
            if (!filteredCustomData.isEmpty()) {
                clinicalDataBins = NewClinicalDataBinUtil.calculateDynamicDataBins(
                    dataBinner,
                    attributes,
                    customAttributeByDatatype,
                    filteredClinicalDataByAttributeId
                );
            }
        }

        return clinicalDataBins;
    }
}
