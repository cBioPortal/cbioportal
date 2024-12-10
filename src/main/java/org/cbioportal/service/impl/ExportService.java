package org.cbioportal.service.impl;

import org.apache.commons.lang3.tuple.Pair;
import org.cbioportal.model.*;
import org.cbioportal.service.*;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    PatientService patientService;
    
    @Autowired
    SampleService sampleService;
    
    @Autowired
    ClinicalDataService clinicalDataService;
    
    @Autowired
    MutationService mutationService;
    
    @Autowired
    MolecularProfileService molecularProfileService; 
    
    public void exportData(Map<String, List<String>> samples) throws SampleNotFoundException, StudyNotFoundException, PatientNotFoundException, MolecularProfileNotFoundException {
        if (samples.size() > 1) {
            //virtual study
        }
        for (Map.Entry<String, List<String>> studySamples: samples.entrySet()) {
            String studyId = studySamples.getKey();
            for (String sampleId: studySamples.getValue()) {
                Sample sample = sampleService.getSampleInStudy(studyId, sampleId);
                String patientStableId = sample.getPatientStableId();
                String sampleStableId = sample.getStableId();
                List<ClinicalData> sampleClinicalData = clinicalDataService.getAllClinicalDataOfSampleInStudy(studyId, sampleStableId, null, null, null, null, null, null);
                for (ClinicalData sampleClinicalDatum : sampleClinicalData) {
                   ClinicalAttribute clinicalAttribute = sampleClinicalDatum.getClinicalAttribute();
                   sampleClinicalDatum.getAttrId();
                   sampleClinicalDatum.getAttrValue();
                }
                Patient patient = patientService.getPatientInStudy(studyId, patientStableId);
                List<ClinicalData> patientClinicalData = clinicalDataService.getAllClinicalDataOfPatientInStudy(studyId, patientStableId, null, null, null, null, null, null);
                for (ClinicalData patientClinicalDataItem : patientClinicalData) {
                    ClinicalAttribute clinicalAttribute = patientClinicalDataItem.getClinicalAttribute();
                    patientClinicalDataItem.getAttrId();
                    patientClinicalDataItem.getAttrValue();
                }
            }
            List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers = molecularProfileService.getMolecularProfileCaseIdentifiers(List.of(studyId), studySamples.getValue());
            for (MolecularProfileCaseIdentifier molecularProfileCaseIdentifier : molecularProfileCaseIdentifiers) {
               MolecularProfile molecularProfile = molecularProfileService.getMolecularProfile(molecularProfileCaseIdentifier.getMolecularProfileId());
               MolecularProfile.MolecularAlterationType molecularAlterationType = molecularProfile.getMolecularAlterationType();
               molecularProfile.getDatatype();
               molecularProfile.getName();
               switch (molecularAlterationType) {
                   case MUTATION_EXTENDED -> {
                       List<Mutation> mutationList = mutationService.getMutationsInMultipleMolecularProfilesByGeneQueries(List.of(molecularProfileCaseIdentifier.getMolecularProfileId()), studySamples.getValue(), List.of(), "DETAILED", 10000, 1, null, null);
                       for (Mutation mutation : mutationList) {
                           mutation.getChr();
                       }
                   }
               }
            }
        }
    }
}
