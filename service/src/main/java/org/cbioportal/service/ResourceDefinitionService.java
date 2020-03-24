package org.cbioportal.service;

import java.util.List;

import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.service.exception.ResourceDefinitionNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;

public interface ResourceDefinitionService {

    ResourceDefinition getResourceDefinition(String studyId, String resourceId)
            throws ResourceDefinitionNotFoundException, StudyNotFoundException;

    List<ResourceDefinition> getAllResourceDefinitionsInStudy(String studyId, String projection, Integer pageSize,
            Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException;
}
