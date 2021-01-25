package org.cbioportal.web.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.map.MultiKeyMap;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.ClinicalDataCountItem;
import org.cbioportal.model.DataBin;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Patient;
import org.cbioportal.model.SampleList;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataFilter;
import org.cbioportal.web.parameter.CustomDataSession;
import org.cbioportal.web.parameter.CustomDataValue;
import org.cbioportal.web.parameter.DataFilterValue;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.stereotype.Component;

@Component
public class StudyViewFilterUtil {
    public void extractStudyAndSampleIds(List<SampleIdentifier> sampleIdentifiers, List<String> studyIds, List<String> sampleIds) {
        for (SampleIdentifier sampleIdentifier : sampleIdentifiers) {
            studyIds.add(sampleIdentifier.getStudyId());
            sampleIds.add(sampleIdentifier.getSampleId());
        }
    }

    public void removeSelfFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter!= null && studyViewFilter.getClinicalDataFilters() != null) {
            studyViewFilter.getClinicalDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }


    public void removeSelfCustomDataFromFilter(String attributeId, StudyViewFilter studyViewFilter) {
        if (studyViewFilter!= null && studyViewFilter.getCustomDataFilters() != null) {
            studyViewFilter.getCustomDataFilters().removeIf(f -> f.getAttributeId().equals(attributeId));
        }
    }
    
    public String getCaseUniqueKey(String studyId, String caseId) {
        return studyId + caseId;
    }

    public String getGenomicDataFilterUniqueKey(String hugoGeneSymbol, String profileType) {
        return hugoGeneSymbol + profileType;
    }

    public ClinicalDataBin dataBinToClinicalDataBin(ClinicalDataBinFilter attribute, DataBin dataBin) {
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

    public Map<String, List<MolecularProfile>> categorizeMolecularPorfiles(List<MolecularProfile> molecularProfiles) {
        return molecularProfiles.stream().collect(Collectors.groupingBy(molecularProfile -> {
            return molecularProfile.getStableId().replace(molecularProfile.getCancerStudyIdentifier() + "_", "");
        }));
    }

    public Map<String, List<SampleList>> categorizeSampleLists(List<SampleList> sampleLists) {
        return sampleLists.stream().collect(Collectors.groupingBy(sampleList -> {
            return sampleList.getStableId().replace(sampleList.getCancerStudyIdentifier() + "_", "");
        }));
    }

    public Integer getFilteredCountByDataEquality(List<ClinicalDataFilter> attributes, MultiKeyMap clinicalDataMap,
            String entityId, String studyId, Boolean negateFilters) {
        Integer count = 0;
        for (ClinicalDataFilter s : attributes) {
            List<String> filteredValues = s.getValues()
                    .stream()
                    .map(DataFilterValue::getValue)
                    .collect(Collectors.toList());
            filteredValues.replaceAll(String::toUpperCase);
            if (clinicalDataMap.containsKey(studyId, entityId, s.getAttributeId())) {
                String value = (String) clinicalDataMap.get(studyId, entityId, s.getAttributeId());
                if (negateFilters ^ filteredValues.contains(value)) {
                    count++;
                }
            } else if (negateFilters ^ filteredValues.contains("NA")) {
                count++;
            }
        }
        return count;
    }

    public List<ClinicalDataCountItem> getClinicalDataCountsFromCustomData(List<CustomDataSession> customDataSessions,
            Map<String, SampleIdentifier> filteredSamplesMap, List<Patient> patients) {
        int totalSamplesCount = filteredSamplesMap.keySet().size();
        int totalPatientsCount = patients.size();

        return customDataSessions.stream().map(customDataSession -> {

            Map<String, List<CustomDataValue>> groupedDatabyValue = customDataSession.getData().getData().stream()
                    .filter(datum -> {
                        return filteredSamplesMap
                                .containsKey(getCaseUniqueKey(datum.getStudyId(), datum.getSampleId()));
                    }).collect(Collectors.groupingBy(CustomDataValue::getValue));

            ClinicalDataCountItem clinicalDataCountItem = new ClinicalDataCountItem();
            clinicalDataCountItem.setAttributeId(customDataSession.getId());

            List<ClinicalDataCount> clinicalDataCounts = groupedDatabyValue.entrySet().stream()
                    .map(entry -> {
                        long count = entry.getValue().stream().map(datum -> {
                            return getCaseUniqueKey(datum.getStudyId(),
                                    customDataSession.getData().getPatientAttribute()
                                            ? datum.getPatientId()
                                            : datum.getSampleId());
        
                        }).distinct().count();
                        ClinicalDataCount dataCount = new ClinicalDataCount();
                        dataCount.setValue(entry.getKey());
                        dataCount.setCount(Math.toIntExact(count));
                        return dataCount;
                    })
                    .filter(c -> !c.getValue().toUpperCase().equals("NA") && !c.getValue().toUpperCase().equals("NAN")
                            && !c.getValue().toUpperCase().equals("N/A"))
                    .collect(Collectors.toList());

            int totalCount = clinicalDataCounts.stream().mapToInt(ClinicalDataCount::getCount).sum();
            int naCount = 0;
            if (customDataSession.getData().getPatientAttribute()) {
                naCount = totalPatientsCount - totalCount;
            } else {
                naCount = totalSamplesCount - totalCount;
            }
            if (naCount > 0) {
                ClinicalDataCount clinicalDataCount = new ClinicalDataCount();
                clinicalDataCount.setAttributeId(customDataSession.getId());
                clinicalDataCount.setValue("NA");
                clinicalDataCount.setCount(naCount);
                clinicalDataCounts.add(clinicalDataCount);
            }

            clinicalDataCountItem.setCounts(clinicalDataCounts);
            return clinicalDataCountItem;
        }).collect(Collectors.toList());
    }
}
