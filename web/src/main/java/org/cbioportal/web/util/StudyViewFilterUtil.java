package org.cbioportal.web.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.DataBin;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
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
}
