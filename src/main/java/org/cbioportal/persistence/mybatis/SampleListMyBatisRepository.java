package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.SampleList;
import org.cbioportal.model.SampleListToSampleId;
import org.cbioportal.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.SampleListRepository;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SampleListMyBatisRepository implements SampleListRepository {

    @Autowired
    private SampleListMapper sampleListMapper;

    @Override
    public List<SampleList> getAllSampleLists(String projection, Integer pageSize, Integer pageNumber, String sortBy,
                                              String direction) {

        return sampleListMapper.getAllSampleLists(null, projection, pageSize,
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleLists() {

        return sampleListMapper.getMetaSampleLists(null);
    }

    @Override
    public SampleList getSampleList(String sampleListId) {

        return sampleListMapper.getSampleList(sampleListId, PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<SampleList> getSampleLists(List<String> sampleListIds, String projection) {

        return sampleListMapper.getSampleLists(sampleListIds, projection);
    }

    @Override
    public List<SampleList> getAllSampleListsInStudies(List<String> studyIds, String projection, Integer pageSize,
                                                     Integer pageNumber, String sortBy, String direction) {
        
        return sampleListMapper.getAllSampleLists(studyIds, projection, pageSize,
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleListsInStudy(String studyId) {

        return sampleListMapper.getMetaSampleLists(studyId);
    }

    @Override
    public List<String> getAllSampleIdsInSampleList(String sampleListId) {
        
        return sampleListMapper.getAllSampleIdsInSampleList(sampleListId);
    }

    @Override
    @Cacheable(cacheResolver = "staticRepositoryCacheOneResolver", condition = "@cacheEnabledConfig.getEnabled()")
	public List<SampleListToSampleId> getSampleListSampleIds(List<Integer> sampleListIds) {
        
        return sampleListMapper.getSampleListSampleIds(sampleListIds);
	}
}
