package org.cbioportal.legacy.service;

import java.util.List;
import org.cbioportal.legacy.model.ResourceDefinition;
import org.cbioportal.legacy.service.exception.ResourceDefinitionNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;

public interface ResourceDefinitionService {

  ResourceDefinition getResourceDefinition(String studyId, String resourceId)
      throws ResourceDefinitionNotFoundException, StudyNotFoundException;

  List<ResourceDefinition> getAllResourceDefinitionsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException;

  List<ResourceDefinition> fetchResourceDefinitions(List<String> studyIds, String projection)
      throws StudyNotFoundException;
}
