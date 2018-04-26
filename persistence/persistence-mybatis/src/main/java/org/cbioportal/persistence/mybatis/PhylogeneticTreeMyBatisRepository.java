package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.PhylogeneticTree;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PhylogeneticTreeRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class PhylogeneticTreeMyBatisRepository implements PhylogeneticTreeRepository {
    
    @Autowired
    private PhylogeneticTreeMapper phylogeneticTreeMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;


    @Override
    public List<PhylogeneticTree> getPhylogeneticTreesInPatientInStudy(String studyId, String patientId,
                                                                    String projection, Integer pageSize,
                                                                    Integer pageNumber) {

        return phylogeneticTreeMapper.getPhylogeneticTrees(Arrays.asList(studyId), Arrays.asList(patientId), 
            projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber));
    }

    // @Override
    // public BaseMeta getMetaPhylogeneticTreesInPatientInStudy(String studyId, String patientId) {

    //     return phylogeneticTreeMapper.getMetaPhylogeneticTrees(Arrays.asList(studyId), Arrays.asList(patientId));
    // }

    @Override
    public List<PhylogeneticTree> fetchPhylogeneticTrees(List<String> studyIds, List<String> patientIds, 
                                                       String projection) {
        
        return phylogeneticTreeMapper.getPhylogeneticTrees(studyIds, patientIds, projection, 0, 0);
    }

    // @Override
    // public BaseMeta fetchMetaPhylogeneticTrees(List<String> studyIds, List<String> patientIds) {
        
    //     return phylogeneticTreeMapper.getMetaPhylogeneticTrees(studyIds, patientIds);
    // }

    // @Override
    // public List<PhylogeneticTree> getPhylogeneticTreesByPatientListId(String studyId, String patientListId, 
    //                                                                String projection) {
        
    //     return phylogeneticTreeMapper.getPhylogeneticTreesByPatientListId(studyId, patientListId, projection);
    // }
}
