package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.ResourceDefinition;

public interface ResourceDefinitionMapper {

    ResourceDefinition getResourceDefinition(String studyId, String resourceId, String projection);

    List<ResourceDefinition> getResourceDefinitions(String studyId, String projection, Integer limit, Integer offset,
            String sortBy, String direction);

}
