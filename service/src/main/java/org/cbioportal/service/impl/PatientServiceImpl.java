package org.cbioportal.service.impl;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl implements PatientService {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private StudyService studyService;
    
    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, 
                                               String sortBy, String direction) throws StudyNotFoundException {
        
        studyService.getStudy(studyId);
        
        return patientRepository.getAllPatientsInStudy(studyId, projection, pageSize, pageNumber, sortBy, direction);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public BaseMeta getMetaPatientsInStudy(String studyId) throws StudyNotFoundException {

        studyService.getStudy(studyId);
        
        return patientRepository.getMetaPatientsInStudy(studyId);
    }

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public Patient getPatientInStudy(String studyId, String patientId) throws PatientNotFoundException, 
        StudyNotFoundException {

        studyService.getStudy(studyId);

        Patient patient = patientRepository.getPatientInStudy(studyId, patientId);

        if (patient == null) {
            throw new PatientNotFoundException(studyId, patientId);
        }

        return patient;
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    public List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection) {
        
        return patientRepository.fetchPatients(studyIds, patientIds, projection);
    }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    public BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds) {
        
        return patientRepository.fetchMetaPatients(studyIds, patientIds);
    }
}
