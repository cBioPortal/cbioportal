package org.cbioportal.legacy.service.impl;

import java.util.Collections;
import java.util.List;
import org.cbioportal.legacy.model.ResourceDefinition;
import org.cbioportal.legacy.persistence.ResourceDefinitionRepository;
import org.cbioportal.legacy.service.ResourceDefinitionService;
import org.cbioportal.legacy.service.StudyService;
import org.cbioportal.legacy.service.exception.ResourceDefinitionNotFoundException;
import org.cbioportal.legacy.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceDefinitionServiceImpl implements ResourceDefinitionService {

  @Autowired private ResourceDefinitionRepository resourceDefinitionRepository;
  @Autowired private StudyService studyService;

  @Override
  public ResourceDefinition getResourceDefinition(String studyId, String resourceId)
      throws ResourceDefinitionNotFoundException, StudyNotFoundException {

    studyService.studyExists(studyId);

    ResourceDefinition resourceDefinition =
        resourceDefinitionRepository.getResourceDefinition(studyId, resourceId);

    if (resourceDefinition == null) {
      throw new ResourceDefinitionNotFoundException(studyId, resourceId);
    }

    return resourceDefinition;
  }

  @Override
  public List<ResourceDefinition> getAllResourceDefinitionsInStudy(
      String studyId,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction)
      throws StudyNotFoundException {

    studyService.studyExists(studyId);

    return resourceDefinitionRepository.fetchResourceDefinitions(
        Collections.singletonList(studyId), projection, pageSize, pageNumber, sortBy, direction);
  }

  @Override
  public List<ResourceDefinition> fetchResourceDefinitions(List<String> studyIds, String projection)
      throws StudyNotFoundException {
    return resourceDefinitionRepository.fetchResourceDefinitions(
        studyIds, projection, null, null, null, null);
  }
}
