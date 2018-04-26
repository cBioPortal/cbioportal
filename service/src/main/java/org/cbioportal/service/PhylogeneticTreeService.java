package org.cbioportal.service;

import org.cbioportal.model.PhylogeneticTree;
// import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

import java.util.List;

public interface PhylogeneticTreeService {

    List<PhylogeneticTree> getPhylogeneticTreesInPatientInStudy(String studyId, String patientId, String projection,
                                                             Integer pageSize, Integer pageNumber) throws PatientNotFoundException, StudyNotFoundException;

    // BaseMeta getMetaPhylogeneticTreesInPatientInStudy(String studyId, String patientId) throws PatientNotFoundException, StudyNotFoundException;

    List<PhylogeneticTree> fetchPhylogeneticTrees(List<String> studyIds, List<String> patientIds, String projection);

    // BaseMeta fetchMetaPhylogeneticTrees(List<String> studyIds, List<String> patientIds);

    // List<PhylogeneticTree> getPhylogeneticTreesByPatientListId(String studyId, String patientListId, String projection);
}
