package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.DiscreteCopyNumberData;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.DiscreteCopyNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiscreteCopyNumberMyBatisRepository implements DiscreteCopyNumberRepository {

    @Autowired
    private DiscreteCopyNumberMapper discreteCopyNumberMapper;

    @Override
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMolecularProfileBySampleListId(
        String molecularProfileId,
        String sampleListId,
        List<Integer> entrezGeneIds,
        List<Integer> alterationTypes,
        String projection) {

        return discreteCopyNumberMapper.getDiscreteCopyNumbersBySampleListId(molecularProfileId, sampleListId, 
            entrezGeneIds, alterationTypes, projection);
    }

    @Override
    public BaseMeta getMetaDiscreteCopyNumbersInMolecularProfileBySampleListId(String molecularProfileId,
                                                                               String sampleListId,
                                                                               List<Integer> entrezGeneIds,
                                                                               List<Integer> alterationTypes) {

        return discreteCopyNumberMapper.getMetaDiscreteCopyNumbersBySampleListId(molecularProfileId, sampleListId, 
            entrezGeneIds, alterationTypes);
    }

    @Override
    public List<DiscreteCopyNumberData> fetchDiscreteCopyNumbersInMolecularProfile(String molecularProfileId,
                                                                                   List<String> sampleIds,
                                                                                   List<Integer> entrezGeneIds,
                                                                                   List<Integer> alterationTypes,
                                                                                   String projection) {

        return discreteCopyNumberMapper.getDiscreteCopyNumbersBySampleIds(molecularProfileId, sampleIds, entrezGeneIds,
            alterationTypes, projection);
    }

    @Override
    public List<DiscreteCopyNumberData> getDiscreteCopyNumbersInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                                           List<String> sampleIds,
                                                                                           List<Integer> entrezGeneIds,
                                                                                           List<Integer> alterationTypes,
                                                                                           String projection) {

        return discreteCopyNumberMapper.getDiscreteCopyNumbersInMultipleMolecularProfiles(molecularProfileIds, 
            sampleIds, entrezGeneIds, alterationTypes, projection);
    }

    @Override
    public BaseMeta fetchMetaDiscreteCopyNumbersInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                                   List<Integer> entrezGeneIds,
                                                                   List<Integer> alterationTypes) {

        return discreteCopyNumberMapper.getMetaDiscreteCopyNumbersBySampleIds(molecularProfileId, sampleIds,
            entrezGeneIds, alterationTypes);
    }

    @Override
    public List<CopyNumberCountByGene> getSampleCountByGeneAndAlterationAndSampleIds(String molecularProfileId, 
                                                                                     List<String> sampleIds, 
                                                                                     List<Integer> entrezGeneIds, 
                                                                                     List<Integer> alterations) {
        
        return discreteCopyNumberMapper.getSampleCountByGeneAndAlterationAndSampleIds(molecularProfileId, sampleIds, 
            entrezGeneIds, alterations);
    }

}
