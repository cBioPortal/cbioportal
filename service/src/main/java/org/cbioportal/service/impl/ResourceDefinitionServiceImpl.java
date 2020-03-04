package org.cbioportal.service.impl;

import java.util.List;

import org.cbioportal.model.ResourceDefinition;
import org.cbioportal.persistence.ResourceDefinitionRepository;
import org.cbioportal.service.ResourceDefinitionService;
import org.cbioportal.service.StudyService;
import org.cbioportal.service.exception.ResourceDefinitionNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceDefinitionServiceImpl implements ResourceDefinitionService {

    @Autowired
    private ResourceDefinitionRepository resourceDefinitionRepository;
    @Autowired
    private StudyService studyService;

    @Override
    public ResourceDefinition getResourceDefinition(String studyId, String resourceId)
            throws ResourceDefinitionNotFoundException, StudyNotFoundException {

        studyService.getStudy(studyId);

        ResourceDefinition resourceDefinition = resourceDefinitionRepository.getResourceDefinition(studyId, resourceId);

        if (resourceDefinition == null) {
            throw new ResourceDefinitionNotFoundException(studyId, resourceId);
        }

        return resourceDefinition;
    }

    @Override
    public List<ResourceDefinition> getAllResourceDefinitionsInStudy(String studyId, String projection,
            Integer pageSize, Integer pageNumber, String sortBy, String direction) throws StudyNotFoundException {

        studyService.getStudy(studyId);

        return resourceDefinitionRepository.getAllResourceDefinitionsInStudy(studyId, projection, pageSize, pageNumber,
                sortBy, direction);
    }

}
