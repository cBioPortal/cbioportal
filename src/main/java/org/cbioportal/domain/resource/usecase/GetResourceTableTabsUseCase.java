package org.cbioportal.domain.resource.usecase;

import java.util.List;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.springframework.stereotype.Service;

@Service
public class GetResourceTableTabsUseCase {
  private final ResourceDataRepository resourceDataRepository;

  public GetResourceTableTabsUseCase(ResourceDataRepository resourceDataRepository) {
    this.resourceDataRepository = resourceDataRepository;
  }

  public List<ResourceTableTab> execute(ResourceTabsRequest request) {
    if (request == null || request.studyIds() == null || request.studyIds().isEmpty()) {
      return List.of();
    }
    return resourceDataRepository.getResourceTableTabs(request);
  }
}
