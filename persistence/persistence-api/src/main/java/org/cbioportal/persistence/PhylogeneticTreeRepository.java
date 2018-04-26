package org.cbioportal.persistence;

import org.cbioportal.model.PhylogeneticTree;
// import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface PhylogeneticTreeRepository {

    List<PhylogeneticTree> getPhylogeneticTreesInPatientInStudy(String studyId, String patientId, String projection,
                                                             Integer pageSize, Integer pageNumber);

    // BaseMeta getMetaPhylogeneticTreesInPatientInStudy(String studyId, String patientId);

    List<PhylogeneticTree> fetchPhylogeneticTrees(List<String> studyIds, List<String> patientIds, String projection);

    // BaseMeta fetchMetaPhylogeneticTrees(List<String> studyIds, List<String> patientIds);

    // List<PhylogeneticTree> getPhylogeneticTreesByPatientListId(String studyId, String patientListId, String projection);
}
