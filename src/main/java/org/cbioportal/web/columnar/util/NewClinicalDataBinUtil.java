package org.cbioportal.web.columnar.util;

import org.cbioportal.model.Binnable;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.DataBin;
import org.cbioportal.service.util.CustomDataSession;
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
            NewStudyViewFilterUtil.removeClinicalDataFilter(attributes.getFirst().getAttributeId(), studyViewFilter.getClinicalDataFilters());
        }

        return studyViewFilter;
    }

    public static StudyViewFilter removeSelfCustomDataFromFilter(ClinicalDataBinCountFilter dataBinCountFilter) {
        List<ClinicalDataBinFilter> attributes = dataBinCountFilter.getAttributes();
        StudyViewFilter studyViewFilter = dataBinCountFilter.getStudyViewFilter();

        if (attributes.size() == 1) {
            NewStudyViewFilterUtil.removeClinicalDataFilter(attributes.getFirst().getAttributeId(), studyViewFilter.getCustomDataFilters());
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
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId
    ) {
        List<ClinicalDataBin> clinicalDataBins = new ArrayList<>();

        for (ClinicalDataBinFilter attribute : attributes) {
            if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                List<ClinicalDataBin> dataBins = dataBinner
                    .calculateClinicalDataBins(
                        attribute,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), emptyList()),
                        unfilteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), emptyList())
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
        Map<String, List<Binnable>> filteredClinicalDataByAttributeId
    ) {
        List<ClinicalDataBin> clinicalDataBins = new ArrayList<>();

        for (ClinicalDataBinFilter attribute : attributes) {

            // if there is clinical data for requested attribute
            if (attributeDatatypeMap.containsKey(attribute.getAttributeId())) {
                List<ClinicalDataBin> dataBins = dataBinner
                    .calculateDataBins(
                        attribute,
                        filteredClinicalDataByAttributeId.getOrDefault(attribute.getAttributeId(), emptyList())
                    )
                    .stream()
                    .map(dataBin -> dataBinToClinicalDataBin(attribute, dataBin))
                    .toList();
                clinicalDataBins.addAll(dataBins);
            }
        }

        return clinicalDataBins;
    }

    /**
     * Generate a list of ClinicalData from the given data count instance.
     * Size of the generated list is equal to 'dataCount.count',
     * and each ClinicalData in the list contains the same value 'dataCount.value'
     * 
     * This method improves the performance of the data binning because it allows us to fetch only
     * the clinical data counts data which is a lot more compact and faster to generated than the actual clinical data.
     * We only need the attribute id and the value of the clinical data to generate data bins.
     * Constructing the clinical data in memory by using clinical data counts significantly improves the performance,
     * and it also allows us to use the exact same SQL used by the clinical data counts endpoint. 
     * 
     * @param dataCount ClinicalDataCount instance containing the count and the value
     * @return  a list of ClinicalData with size 'dataCount.count' and value 'dataCount.value'
     */
    public static List<ClinicalData> generateClinicalDataFromClinicalDataCount(ClinicalDataCount dataCount)
    {
        List<ClinicalData> data = new ArrayList<>(dataCount.getCount());
        
        for (int i=0; i < dataCount.getCount(); i++) {
            ClinicalData d = new ClinicalData();
            d.setAttrId(dataCount.getAttributeId());
            d.setAttrValue(dataCount.getValue());
            data.add(d);
        }
        
        return data;
    }

    public static ClinicalDataType getDataType(Map.Entry<String, CustomDataSession> entry) {
        return Boolean.TRUE.equals(entry.getValue().getData().getPatientAttribute()) ? ClinicalDataType.PATIENT : ClinicalDataType.SAMPLE;
    }
}
