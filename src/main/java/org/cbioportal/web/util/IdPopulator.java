package org.cbioportal.web.util;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.util.ClinicalAttributeUtil;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdPopulator {
    
    @Autowired
    private ClinicalAttributeUtil clinicalAttributeUtil;
    @Autowired
    private StudyViewFilterApplier studyViewFilterApplier;
    @Autowired
    private StudyViewFilterUtil studyViewFilterUtil;
    @Autowired
    private PatientService patientService;
    
    public IdPopulator() {}

    public BinningIds populateIdLists(
        List<SampleIdentifier> samples,
        List<ClinicalAttribute> clinicalAttributes
    ) {
        BinningIds binningIds = new BinningIds();
        List<String> studyIds = binningIds.getStudyIds();
        List<String> sampleIds = binningIds.getSampleIds();
        List<String> patientIds = binningIds.getPatientIds();
        List<String> studyIdsOfPatients = binningIds.getStudyIdsOfPatients();
        List<String> uniqueSampleKeys = binningIds.getUniqueSampleKeys();
        List<String> uniquePatientKeys = binningIds.getUniquePatientKeys();
        List<String> sampleAttributeIds = binningIds.getSampleAttributeIds();
        List<String> patientAttributeIds = binningIds.getPatientAttributeIds();
        List<String> conflictingPatientAttributeIds = binningIds.getConflictingPatientAttributeIds();

        studyViewFilterUtil.extractStudyAndSampleIds(
            samples,
            studyIds,
            sampleIds
        );

        patientService.getPatientsOfSamples(studyIds, sampleIds).forEach(patient -> {
            patientIds.add(patient.getStableId());
            studyIdsOfPatients.add(patient.getCancerStudyIdentifier());
        });

        uniqueSampleKeys.addAll(studyViewFilterApplier.getUniqkeyKeys(studyIds, sampleIds));
        uniquePatientKeys.addAll(studyViewFilterApplier.getUniqkeyKeys(studyIdsOfPatients, patientIds));

        if (clinicalAttributes != null && !clinicalAttributes.isEmpty()) {
            clinicalAttributeUtil.extractCategorizedClinicalAttributes(
                clinicalAttributes,
                sampleAttributeIds,
                patientAttributeIds,
                conflictingPatientAttributeIds
            );
        }
        return binningIds;
    }

}
