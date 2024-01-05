package org.cbioportal.persistence.mysql;

import java.util.List;

import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.ResourceDefinitionRepository;
import org.cbioportal.persistence.mysql.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mysql")
public class ResourceDefinitionMyBatisRepository implements ResourceDefinitionRepository {

    @Autowired
    private ResourceDefinitionMapper resourceDefinitionMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

    @Override
    public ResourceDefinition getResourceDefinition(String studyId, String resourceId) {
        return resourceDefinitionMapper.getResourceDefinition(studyId, resourceId,
                PersistenceConstants.DETAILED_PROJECTION);
    }

    @Override
    public List<ResourceDefinition> fetchResourceDefinitions(List<String> studyIds, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        return resourceDefinitionMapper.getResourceDefinitions(studyIds, projection, pageSize,
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }
}
