package org.cbioportal.web.columnar;

import org.cbioportal.model.*;
import org.cbioportal.service.StudyViewColumnarService;
import org.cbioportal.web.columnar.util.NewClinicalDataBinUtil;
import org.cbioportal.web.parameter.*;
import org.cbioportal.web.util.DataBinner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ClinicalDataBinner {
    private final StudyViewColumnarService studyViewColumnarService;
    private final DataBinner dataBinner;
    
    @Autowired
    public ClinicalDataBinner(
        StudyViewColumnarService studyViewColumnarService,
        DataBinner dataBinner
    ) {
        this.studyViewColumnarService = studyViewColumnarService;
        this.dataBinner = dataBinner;
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
        StudyViewFilter partialFilter = new StudyViewFilter();
        partialFilter.setStudyIds(studyViewFilter.getStudyIds());
        partialFilter.setSampleIdentifiers(studyViewFilter.getSampleIdentifiers());

        // filter only by study id and sample identifiers, ignore rest
        // we need this additional partial filter because we always need to know the bins generated for the initial state
        // which allows us to keep the number of bins and bin ranges consistent even if there are additional data filters.
        // we only want to update the counts for each bin, we don't want to regenerate the bins for the filtered data.
        // NOTE: partial filter is only needed when dataBinMethod == DataBinMethod.STATIC but that's always the case
        // for the frontend implementation. we can't really use dataBinMethod == DataBinMethod.DYNAMIC because of the
        // complication it brings to the frontend visualization and filtering
        List<Sample> unfilteredSamples = studyViewColumnarService.getFilteredSamples(partialFilter);
        List<Sample> filteredSamples = studyViewColumnarService.getFilteredSamples(studyViewFilter);

        // TODO make sure unique sample and patient keys don't need to be distinct
        List<String> unfilteredUniqueSampleKeys = unfilteredSamples.stream().map(Sample::getUniqueSampleKey).collect(Collectors.toList());
        List<String> filteredUniqueSampleKeys = filteredSamples.stream().map(Sample::getUniqueSampleKey).collect(Collectors.toList());
        List<String> unfilteredUniquePatientKeys = unfilteredSamples.stream().map(Sample::getUniquePatientKey).collect(Collectors.toList());
        List<String> filteredUniquePatientKeys = filteredSamples.stream().map(Sample::getUniquePatientKey).collect(Collectors.toList());

        // TODO make sure we don't need a distinction between sample vs patient attribute ids here
        //  ideally we shouldn't because we have patient clinical data separated from sample clinical data in clickhouse
        
        // we need the clinical data for the partial filter in order to generate the bins for initial state
        // we use the filtered data to calculate the counts for each bin, we do not regenerate bins for the filtered data 
        List<ClinicalData> unfilteredClinicalDataForSamples = studyViewColumnarService.getSampleClinicalData(partialFilter, attributeIds);
        List<ClinicalData> filteredClinicalDataForSamples = studyViewColumnarService.getSampleClinicalData(studyViewFilter, attributeIds);
        List<ClinicalData> unfilteredClinicalDataForPatients = studyViewColumnarService.getPatientClinicalData(partialFilter, attributeIds);
        List<ClinicalData> filteredClinicalDataForPatients = studyViewColumnarService.getPatientClinicalData(studyViewFilter, attributeIds);

        Map<String, ClinicalDataType> attributeDatatypeMap = NewClinicalDataBinUtil.toAttributeDatatypeMap(
            unfilteredClinicalDataForSamples.stream().map(ClinicalData::getAttrId).collect(Collectors.toList()),
            unfilteredClinicalDataForPatients.stream().map(ClinicalData::getAttrId).collect(Collectors.toList()),
            Collections.emptyList() // TODO ignoring conflictingPatientAttributeIds for now
        );

        List<Binnable> unfilteredClinicalData = Stream.of(
            unfilteredClinicalDataForSamples,
            unfilteredClinicalDataForPatients
            // unfilteredClinicalDataForConflictingPatientAttributes /// TODO ignoring conflictingPatientAttributeIds for now
        ).flatMap(Collection::stream).collect(Collectors.toList());

        List<Binnable> filteredClinicalData = Stream.of(
            filteredClinicalDataForSamples,
            filteredClinicalDataForPatients
            // filteredClinicalDataForConflictingPatientAttributes // TODO ignoring conflictingPatientAttributeIds for now
        ).flatMap(Collection::stream).collect(Collectors.toList());

        Map<String, List<Binnable>> unfilteredClinicalDataByAttributeId =
            unfilteredClinicalData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));

        Map<String, List<Binnable>> filteredClinicalDataByAttributeId =
            filteredClinicalData.stream().collect(Collectors.groupingBy(Binnable::getAttrId));

        List<ClinicalDataBin> clinicalDataBins = Collections.emptyList();

        if (dataBinMethod == DataBinMethod.STATIC) {
            if (!unfilteredSamples.isEmpty() && !unfilteredClinicalData.isEmpty()) {
                clinicalDataBins = NewClinicalDataBinUtil.calculateStaticDataBins(
                    dataBinner,
                    attributes,
                    attributeDatatypeMap,
                    unfilteredClinicalDataByAttributeId,
                    filteredClinicalDataByAttributeId,
                    unfilteredUniqueSampleKeys,
                    unfilteredUniquePatientKeys,
                    filteredUniqueSampleKeys,
                    filteredUniquePatientKeys
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
                    filteredClinicalDataByAttributeId,
                    filteredUniqueSampleKeys,
                    filteredUniquePatientKeys
                );
            }
        }

        return clinicalDataBins;
    }
}
