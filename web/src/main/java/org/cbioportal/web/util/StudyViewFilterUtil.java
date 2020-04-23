package org.cbioportal.web.util;

import com.google.common.collect.Range;

import org.cbioportal.model.ClinicalDataBin;
import org.cbioportal.model.DataBin;
import org.cbioportal.model.GenomicDataBin;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.cbioportal.web.parameter.ClinicalDataBinFilter;
import org.cbioportal.web.parameter.GenomicDataBinFilter;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    
    
    public void removeSelfFromFilter(GenomicDataBinFilter genomicDataBinFilter, StudyViewFilter studyViewFilter) {
        if (studyViewFilter != null && studyViewFilter.getGenomicDataFilters() != null) {
            studyViewFilter.getGenomicDataFilters().removeIf(f -> {
                return f.getHugoGeneSymbol().equals(genomicDataBinFilter.getHugoGeneSymbol())
                        && f.getProfileType().equals(genomicDataBinFilter.getProfileType());
            });
        }
    }

    public Range<BigDecimal> calcRange(BigDecimal start, boolean startInclusive, BigDecimal end, boolean endInclusive) {
        // check for invalid filter (no start or end provided)
        if (start == null && end == null) {
            return null;
        } else if (start == null) {
            if (endInclusive) {
                return Range.atMost(end);
            } else {
                return Range.lessThan(end);
            }
        } else if (end == null) {
            if (startInclusive) {
                return Range.atLeast(start);
            } else {
                return Range.greaterThan(start);
            }
        } else if (startInclusive) {
            if (endInclusive) {
                return Range.closed(start, end);
            } else {
                return Range.closedOpen(start, end);
            }
        } else {
            if (endInclusive) {
                return Range.openClosed(start, end);
            } else {
                return Range.open(start, end);
            }
        }
    }

    public String getCaseUniqueKey(String studyId, String caseId) {
        return studyId + caseId;
    }

    public String getGenomicDataFilterUniqueKey(String hugoGeneSymbol, String profileType) {
        return hugoGeneSymbol + profileType;
    }

    public GenomicDataBin dataBintoGenomicDataBin(GenomicDataBinFilter genomicDataBinFilter, DataBin dataBin) {
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
