package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.ResourceDefinitionRepository;
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceDefinitionMyBatisRepository implements ResourceDefinitionRepository {

    @Autowired
    private ResourceDefinitionMapper resourceDefinitionMapper;

    @Override
    public ResourceDefinition getResourceDefinition(String studyId, String resourceId) {
        return resourceDefinitionMapper.getResourceDefinition(studyId, resourceId,
                PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<ResourceDefinition> fetchResourceDefinitions(List<String> studyIds, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return resourceDefinitionMapper.getResourceDefinitions(studyIds, projection, pageSize,
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
    }
}
