package org.cbioportal.legacy.persistence.virtualstudy;

import java.util.Comparator;
import java.util.List;
import org.cbioportal.legacy.model.ResourceDefinition;
import org.cbioportal.legacy.persistence.ResourceDefinitionRepository;
import org.cbioportal.legacy.web.parameter.Direction;
import org.cbioportal.legacy.web.parameter.sort.ResourceDefinitionSortBy;

public class VSAwareResourceDefinitionRepository implements ResourceDefinitionRepository {

  private final VirtualizationService virtualizationService;
  private final ResourceDefinitionRepository resourceDefinitionRepository;

  public VSAwareResourceDefinitionRepository(
      VirtualizationService virtualizationService,
      ResourceDefinitionRepository resourceDefinitionRepository) {
    this.virtualizationService = virtualizationService;
    this.resourceDefinitionRepository = resourceDefinitionRepository;
  }

  @Override
  public ResourceDefinition getResourceDefinition(String studyId, String resourceId) {
    return virtualizationService.handleStudyData(
        studyId,
        stid -> resourceDefinitionRepository.getResourceDefinition(stid, resourceId),
        this::virtualizeResourceDefinition);
  }

  @Override
  public List<ResourceDefinition> fetchResourceDefinitions(
      List<String> studyIds,
      String projection,
      Integer pageSize,
      Integer pageNumber,
      String sortBy,
      String direction) {
    var resultStream =
        virtualizationService
            .handleStudyData(
                studyIds,
                ResourceDefinition::getCancerStudyIdentifier,
                stids ->
                    resourceDefinitionRepository.fetchResourceDefinitions(
                        stids, projection, null, null, null, null),
                this::virtualizeResourceDefinition)
            .stream();

    if (sortBy != null) {
      resultStream = resultStream.sorted(composeComparator(sortBy, direction));
    }

    if (pageSize != null && pageNumber != null) {
      resultStream = resultStream.skip((long) pageSize * pageNumber).limit(pageSize);
    }

    return resultStream.toList();
  }

  private Comparator<ResourceDefinition> composeComparator(String sortBy, String direction) {
    ResourceDefinitionSortBy rd = ResourceDefinitionSortBy.valueOf(sortBy);
    Comparator<ResourceDefinition> result =
        switch (rd) {
          case studyId -> Comparator.comparing(ResourceDefinition::getCancerStudyIdentifier);
          case resourceId -> Comparator.comparing(ResourceDefinition::getResourceId);
          case displayName -> Comparator.comparing(ResourceDefinition::getDisplayName);
          case description -> Comparator.comparing(ResourceDefinition::getDescription);
          case resourceType -> Comparator.comparing(ResourceDefinition::getResourceType);
          case priority -> Comparator.comparing(ResourceDefinition::getPriority);
          case openByDefault -> Comparator.comparing(ResourceDefinition::getOpenByDefault);
          case customMetaData -> Comparator.comparing(ResourceDefinition::getCustomMetaData);
        };
    if (direction == null) {
      return result;
    } else {
      Direction d = Direction.valueOf(direction.toUpperCase());
      return d == Direction.ASC ? result : result.reversed();
    }
  }

  private ResourceDefinition virtualizeResourceDefinition(
      String virtualStudyId, ResourceDefinition resourceDefinition) {
    ResourceDefinition virtualizedResourceDefinition = new ResourceDefinition();
    virtualizedResourceDefinition.setResourceId(resourceDefinition.getResourceId());
    virtualizedResourceDefinition.setDisplayName(resourceDefinition.getDisplayName());
    virtualizedResourceDefinition.setDescription(resourceDefinition.getDescription());
    virtualizedResourceDefinition.setResourceType(resourceDefinition.getResourceType());
    virtualizedResourceDefinition.setPriority(resourceDefinition.getPriority());
    virtualizedResourceDefinition.setOpenByDefault(resourceDefinition.getOpenByDefault());
    virtualizedResourceDefinition.setCancerStudyIdentifier(virtualStudyId);
    virtualizedResourceDefinition.setCustomMetaData(resourceDefinition.getCustomMetaData());
    return virtualizedResourceDefinition;
  }
}
