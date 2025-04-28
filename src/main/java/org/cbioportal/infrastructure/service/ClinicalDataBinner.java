package org.cbioportal.infrastructure.service;

import org.cbioportal.legacy.model.Binnable;
import org.cbioportal.legacy.model.ClinicalData;
import org.cbioportal.legacy.model.ClinicalDataBin;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.web.columnar.util.NewClinicalDataBinUtil;
import org.cbioportal.legacy.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;
import org.cbioportal.legacy.web.parameter.DataBinMethod;
import org.cbioportal.legacy.web.parameter.StudyViewFilter;
import org.cbioportal.legacy.web.util.DataBinner;
import org.cbioportal.domain.studyview.StudyViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Deprecated(forRemoval = true)
@Component
@Profile("clickhouse")
public class ClinicalDataBinner {
    private final StudyViewService studyViewService;
    private final DataBinner dataBinner;

    @Autowired
    public ClinicalDataBinner(
        StudyViewService studyViewService,
        DataBinner dataBinner
    ) {
        this.studyViewService = studyViewService;
        this.dataBinner = dataBinner;
    }

    private List<ClinicalData> convertCountsToData(List<ClinicalDataCount> clinicalDataCounts) {
        return clinicalDataCounts
            .stream()
            .map(NewClinicalDataBinUtil::generateClinicalDataFromClinicalDataCount)
            .flatMap(Collection::stream)
            .toList();
    }

    @Cacheable(
        cacheResolver = "staticRepositoryCacheOneResolver",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
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

        List<String> attributeIds = attributes.stream().map(ClinicalDataBinFilter::getAttributeId).toList();

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
        List<ClinicalDataCountItem> unfilteredClinicalDataCounts = studyViewService.getClinicalDataCounts(partialFilter, attributeIds);
        List<ClinicalDataCountItem> filteredClinicalDataCounts = studyViewService.getClinicalDataCounts(studyViewFilter, attributeIds);

        // TODO ignoring conflictingPatientAttributeIds for now
        List<ClinicalData> unfilteredClinicalData = convertCountsToData(
            unfilteredClinicalDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );
        List<ClinicalData> filteredClinicalData = convertCountsToData(
            filteredClinicalDataCounts.stream().flatMap(c -> c.getCounts().stream()).toList()
        );

        Map<String, ClinicalDataType> attributeDatatypeMap = studyViewService.getClinicalAttributeDataTypeMap(studyViewFilter);

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
        } else { // dataBinMethod == DataBinMethod.DYNAMIC
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
}
