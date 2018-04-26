package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.PhylogeneticTree;
// import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface PhylogeneticTreeMapper {
    
    List<PhylogeneticTree> getPhylogeneticTrees(List<String> studyIds, List<String> patientIds, String projection, 
                                              Integer limit, Integer offset);

    // BaseMeta getMetaPhylogeneticTrees(List<String> studyIds, List<String> patientIds);
    
    // List<PhylogeneticTree> getPhylogeneticTreesByPatientListId(String studyId, String patientListId, String projection);
}
