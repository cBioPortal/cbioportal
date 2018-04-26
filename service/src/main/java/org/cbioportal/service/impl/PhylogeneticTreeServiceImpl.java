package org.cbioportal.service.impl;

import org.cbioportal.model.PhylogeneticTree;
// import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PhylogeneticTreeRepository;
import org.cbioportal.service.PhylogeneticTreeService;
import org.cbioportal.service.PatientService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhylogeneticTreeServiceImpl implements PhylogeneticTreeService {

    @Autowired
    private PhylogeneticTreeRepository phylogeneticTreeRepository;
    @Autowired
    private PatientService patientService;

    @Override
    @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    public List<PhylogeneticTree> getPhylogeneticTreesInPatientInStudy(String studyId, String patientId,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber) throws PatientNotFoundException, 
        StudyNotFoundException {
        
        patientService.getPatientInStudy(studyId, patientId);

        return phylogeneticTreeRepository.getPhylogeneticTreesInPatientInStudy(studyId, patientId, projection, pageSize,
            pageNumber);
    }

    // @Override
    // @PreAuthorize("hasPermission(#studyId, 'CancerStudy', 'read')")
    // public BaseMeta getMetaPhylogeneticTreesInPatientInStudy(String studyId, String patientId)
    //     throws PatientNotFoundException, StudyNotFoundException {

    //     patientService.getPatientInStudy(studyId, patientId);
        
    //     return phylogeneticTreeRepository.getMetaPhylogeneticTreesInPatientInStudy(studyId, patientId);
    // }

    @Override
    @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    public List<PhylogeneticTree> fetchPhylogeneticTrees(List<String> studyIds, List<String> patientIds, 
                                                       String projection) {
        
        return phylogeneticTreeRepository.fetchPhylogeneticTrees(studyIds, patientIds, projection);
    }

    // @Override
    // @PreAuthorize("hasPermission(#studyIds, 'List<CancerStudyId>', 'read')")
    // public BaseMeta fetchMetaPhylogeneticTrees(List<String> studyIds, List<String> patientIds) {
        
    //     return phylogeneticTreeRepository.fetchMetaPhylogeneticTrees(studyIds, patientIds);
    // }

    // @Override
    // public List<PhylogeneticTree> getPhylogeneticTreesByPatientListId(String studyId, String patientListId, 
    //                                                                String projection) {
        
    //     return phylogeneticTreeRepository.getPhylogeneticTreesByPatientListId(studyId, patientListId, projection);
    // }
}
