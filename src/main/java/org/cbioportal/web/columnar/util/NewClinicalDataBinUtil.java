package org.cbioportal.web.columnar.util;

import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.DataBin;
import org.cbioportal.web.parameter.ClinicalDataBinCountFilter;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.util.DataBinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public class NewClinicalDataBinUtil {
    public static StudyViewFilter removeSelfFromFilter(ClinicalDataBinCountFilter dataBinCountFilter) {
        List<ClinicalDataBinFilter> attributes = dataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();

        if (attributes.size() == 1) {
            NewStudyViewFilterUtil.removeSelfFromFilter(attributes.get(0).getAttributeId(), studyViewFilter);
        }

        return studyViewFilter;
    }

    public static ClinicalDataBin dataBinToClinicalDataBin(ClinicalDataBinFilter attribute, DataBin dataBin) {
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

    public static Map<String, ClinicalDataType> toAttributeDatatypeMap(
        List<String> sampleAttributeIds,
        List<String> patientAttributeIds,
        List<String> conflictingPatientAttributeIds
    ) {
        Map<String, ClinicalDataType> attributeDatatypeMap = new HashMap<>();

        sampleAttributeIds.forEach(attribute -> {
            attributeDatatypeMap.put(attribute, ClinicalDataType.SAMPLE);
        });
        patientAttributeIds.forEach(attribute -> {
            attributeDatatypeMap.put(attribute, ClinicalDataType.PATIENT);
        });
        conflictingPatientAttributeIds.forEach(attribute -> {
            attributeDatatypeMap.put(attribute, ClinicalDataType.SAMPLE);
        });

        return attributeDatatypeMap;
    }

    public static List<ClinicalDataBin> calculateStaticDataBins(
        DataBinner dataBinner,
        List<ClinicalDataBinFilter> attributes,
        Map<String, ClinicalDataType> attributeDatatypeMap,
        Map<String, List<Binnable>> unfilteredClinicalDataByAttributeId,
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId,
        Map<String, Integer> unfilteredSamplesCountWithoutClinicalData,
        Map<String, Integer> unfilteredPatientsCountWithoutClinicalData,
        Map<String, Integer> filteredSamplesCountWithoutClinicalData,
        Map<String, Integer> filteredPatientsCountWithoutClinicalData
    ) {
        List<ClinicalDataBin> clinicalDataBins = new ArrayList<>();

        for (ClinicalDataBinFilter attribute : attributes) {
            if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                Integer numberOfFilteredCasesWithoutClinicalData = clinicalDataType == ClinicalDataType.PATIENT
                    ? filteredPatientsCountWithoutClinicalData.getOrDefault(attribute.getAttributeId(), 0)
                    : filteredSamplesCountWithoutClinicalData.getOrDefault(attribute.getAttributeId(), 0);
                Integer numberOfUnfilteredCasesWithoutClinicalData = clinicalDataType == ClinicalDataType.PATIENT
                    ? unfilteredPatientsCountWithoutClinicalData.getOrDefault(attribute.getAttributeId(), 0)
                    : unfilteredSamplesCountWithoutClinicalData.getOrDefault(attribute.getAttributeId(), 0);

                List<ClinicalDataBin> dataBins = dataBinner
                    .calculateClinicalDataBins(
                        attribute,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), emptyList()),
                        unfilteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), emptyList()),
                        numberOfFilteredCasesWithoutClinicalData,
                        numberOfUnfilteredCasesWithoutClinicalData
                    )
                    .stream()
                    .map(dataBin -> dataBinToClinicalDataBin(attribute, dataBin))
                    .toList();

                clinicalDataBins.addAll(dataBins);
            }
        }

        return clinicalDataBins;
    }

    public static List<ClinicalDataBin> calculateDynamicDataBins(
        DataBinner dataBinner,
        List<ClinicalDataBinFilter> attributes,
        Map<String, ClinicalDataType> attributeDatatypeMap,
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId,
        Map<String, Integer> filteredSamplesCountWithoutClinicalData,
        Map<String, Integer> filteredPatientsCountWithoutClinicalData
    ) {
        List<ClinicalDataBin> clinicalDataBins = new ArrayList<>();

        for (ClinicalDataBinFilter attribute : attributes) {

            // if there is clinical data for requested attribute
            if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                ClinicalDataType clinicalDataType = attributeDatatypeMap.get(attribute.getAttributeId());
                Integer numberOfFilteredCasesWithoutClinicalData = clinicalDataType == ClinicalDataType.PATIENT
                    ? filteredPatientsCountWithoutClinicalData.getOrDefault(attribute.getAttributeId(), 0)
                    : filteredSamplesCountWithoutClinicalData.getOrDefault(attribute.getAttributeId(), 0);

                List<ClinicalDataBin> dataBins = dataBinner
                    .calculateDataBins(
                        attribute,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), emptyList()),
                        numberOfFilteredCasesWithoutClinicalData.longValue()
                    )
                    .stream()
                    .map(dataBin -> dataBinToClinicalDataBin(attribute, dataBin))
                    .toList();
                clinicalDataBins.addAll(dataBins);
            }
        }

        return clinicalDataBins;
    }
}
