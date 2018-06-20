package org.cbioportal.web.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.Sample;
import org.cbioportal.service.ClinicalDataService;
import org.cbioportal.service.DiscreteCopyNumberService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.parameter.ClinicalDataEqualityFilter;
import org.cbioportal.web.parameter.ClinicalDataType;
import org.cbioportal.web.parameter.CopyNumberGeneFilter;
import org.cbioportal.web.parameter.CopyNumberGeneFilterElement;
import org.cbioportal.web.parameter.MutationGeneFilter;
import org.cbioportal.web.parameter.Projection;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudyViewFilterApplier {

    @Autowired
    private SampleService sampleService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private ClinicalDataService clinicalDataService;
    @Autowired
    private MutationService mutationService;
    @Autowired
    private DiscreteCopyNumberService discreteCopyNumberService;

    public List<String> apply(String studyId, StudyViewFilter studyViewFilter) throws StudyNotFoundException, MolecularProfileNotFoundException {

        List<String> sampleIds = new ArrayList<>();
        if (studyViewFilter != null && studyViewFilter.getSampleIds() != null && !studyViewFilter.getSampleIds().isEmpty()) {
            sampleIds = studyViewFilter.getSampleIds();
            List<String> studyIds = new ArrayList<>();
            sampleIds.forEach(s -> studyIds.add(studyId));
            sampleIds = sampleService.fetchSamples(studyIds, sampleIds, Projection.ID.name()).stream()
                .map(Sample::getStableId).collect(Collectors.toList());;
        } else {
            sampleIds = sampleService.getAllSamplesInStudy(studyId, Projection.ID.name(), null, null, null, null).stream()
                .map(Sample::getStableId).collect(Collectors.toList());
        }

        if (studyViewFilter == null) {
            return sampleIds;
        }
        
        List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters = studyViewFilter.getClinicalDataEqualityFilters();
        if (clinicalDataEqualityFilters != null) {
            sampleIds = filterClinicalData(studyId, clinicalDataEqualityFilters, ClinicalDataType.SAMPLE, 
                sampleIds);
            sampleIds = filterClinicalData(studyId, clinicalDataEqualityFilters, ClinicalDataType.PATIENT, 
                sampleIds);
        }

        List<MutationGeneFilter> mutatedGenes = studyViewFilter.getMutatedGenes();
        if (mutatedGenes != null && !sampleIds.isEmpty()) {
            for (MutationGeneFilter molecularProfileGeneFilter : mutatedGenes) {
                List<Mutation> mutations = mutationService.fetchMutationsInMolecularProfile(molecularProfileGeneFilter.getMolecularProfileId(), 
                    sampleIds, molecularProfileGeneFilter.getEntrezGeneIds(), null, Projection.ID.name(), null, 
                    null, null, null);
                
                sampleIds = mutations.stream().map(Mutation::getSampleId).distinct().collect(Collectors.toList());
            }
        }

        List<CopyNumberGeneFilter> cnaGenes = studyViewFilter.getCnaGenes();
        if (cnaGenes != null && !sampleIds.isEmpty()) {
            for (CopyNumberGeneFilter copyNumberGeneFilter : cnaGenes) {

                List<Integer> ampEntrezGeneIds = copyNumberGeneFilter.getAlterations().stream().filter(a -> 
                    a.getAlteration() == 2).map(CopyNumberGeneFilterElement::getEntrezGeneId).collect(Collectors.toList());
                List<DiscreteCopyNumberData> ampCNAList = new ArrayList<>();
                if (!ampEntrezGeneIds.isEmpty()) {
                    ampCNAList = discreteCopyNumberService
                        .fetchDiscreteCopyNumbersInMolecularProfile(copyNumberGeneFilter.getMolecularProfileId(), 
                        sampleIds, ampEntrezGeneIds, Arrays.asList(2), Projection.ID.name());
                }

                List<Integer> delEntrezGeneIds = copyNumberGeneFilter.getAlterations().stream().filter(a -> 
                    a.getAlteration() == -2).map(CopyNumberGeneFilterElement::getEntrezGeneId).collect(Collectors.toList());
                List<DiscreteCopyNumberData> delCNAList = new ArrayList<>();
                if (!delEntrezGeneIds.isEmpty()) {
                    delCNAList = discreteCopyNumberService
                        .fetchDiscreteCopyNumbersInMolecularProfile(copyNumberGeneFilter.getMolecularProfileId(), 
                        sampleIds, delEntrezGeneIds, Arrays.asList(-2), Projection.ID.name());
                }

                List<DiscreteCopyNumberData> resultList = new ArrayList<>();
                resultList.addAll(ampCNAList);
                resultList.addAll(delCNAList);
                sampleIds = resultList.stream().map(DiscreteCopyNumberData::getSampleId).distinct().collect(Collectors.toList());
            }
        }

        return sampleIds;
    }

    private List<String> filterClinicalData(String studyId, List<ClinicalDataEqualityFilter> clinicalDataEqualityFilters, 
        ClinicalDataType filterClinicalDataType, List<String> sampleIds) throws StudyNotFoundException {
        
        List<ClinicalDataEqualityFilter> attributes = clinicalDataEqualityFilters.stream()
            .filter(c-> c.getClinicalDataType().equals(filterClinicalDataType)).collect(Collectors.toList());
        List<ClinicalData> clinicalDataList = new ArrayList<>();
        if (!attributes.isEmpty() && !sampleIds.isEmpty()) {
            List<String> patientIds = patientService.getPatientIdsOfSamples(sampleIds);
            clinicalDataList = clinicalDataService.fetchAllClinicalDataInStudy(studyId, 
                filterClinicalDataType.equals(ClinicalDataType.PATIENT) ? patientIds : sampleIds, attributes.stream()
                .map(ClinicalDataEqualityFilter::getAttributeId).collect(Collectors.toList()), 
                filterClinicalDataType.name(), Projection.SUMMARY.name());

            clinicalDataList.forEach(c -> {
                if (c.getAttrValue().toUpperCase().equals("NAN") || c.getAttrValue().toUpperCase().equals("N/A")) {
                    c.setAttrValue("NA");
                }
            });

            List<String> ids = new ArrayList<>();
            Map<String, List<ClinicalData>> clinicalDataMap = new HashMap<>();
            
            if (filterClinicalDataType.equals(ClinicalDataType.PATIENT)) {
                clinicalDataMap = clinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::getPatientId));
            } else {
                clinicalDataMap = clinicalDataList.stream().collect(Collectors.groupingBy(ClinicalData::getSampleId));
            }
            
            for (String entityId : filterClinicalDataType.equals(ClinicalDataType.PATIENT) ? patientIds : sampleIds) {
                int count = 0;
                for (ClinicalDataEqualityFilter s : attributes) {
                    List<ClinicalData> entityClinicalData = clinicalDataMap.get(entityId);
                    if (entityClinicalData != null) {
                        Optional<ClinicalData> clinicalData = entityClinicalData.stream().filter(c -> c.getAttrId()
                            .equals(s.getAttributeId())).findFirst();
                        if (clinicalData.isPresent() && s.getValues().contains(clinicalData.get().getAttrValue())) {
                            count++;
                        } else if (!clinicalData.isPresent() && s.getValues().contains("NA")) {
                            count++;
                        }
                    } else if (s.getValues().contains("NA")) {
                        count++;
                    }
                }
                if (count == attributes.size()) {
                    ids.add(entityId);
                }
            }

            if (filterClinicalDataType.equals(ClinicalDataType.PATIENT)) {
                if (!ids.isEmpty()) {
                    ids = sampleService.getAllSamplesOfPatientsInStudy(studyId, ids, 
                        Projection.ID.name()).stream().map(Sample::getStableId).collect(Collectors.toList());
                }
            }
            
            Set<String> idsSet = new HashSet<>(ids);
            idsSet.retainAll(new HashSet<>(sampleIds));
            return new ArrayList<>(idsSet);
        }

        return sampleIds;
    }
}
