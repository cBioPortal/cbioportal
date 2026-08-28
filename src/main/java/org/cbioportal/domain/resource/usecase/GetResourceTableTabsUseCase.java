package org.cbioportal.domain.resource.usecase;

import java.util.List;
import org.cbioportal.domain.resource.ResourceTableTab;
import org.cbioportal.domain.resource.ResourceTabsRequest;
import org.cbioportal.domain.resource.repository.ResourceDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetResourceTableTabsUseCase {
  // TEMPORARY diagnostic logging for the empty-tabs-response investigation; remove once resolved.
  private static final Logger DEBUG_LOG =
      LoggerFactory.getLogger(GetResourceTableTabsUseCase.class);

  private final ResourceDataRepository resourceDataRepository;

  public GetResourceTableTabsUseCase(ResourceDataRepository resourceDataRepository) {
    this.resourceDataRepository = resourceDataRepository;
  }

  public List<ResourceTableTab> execute(ResourceTabsRequest request) {
    if (request == null || request.studyIds() == null || request.studyIds().isEmpty()) {
      DEBUG_LOG.info("TEMP-DEBUG execute() short-circuiting with empty list: request={}", request);
      return List.of();
    }
    DEBUG_LOG.info("TEMP-DEBUG execute() calling repository with request={}", request);
    try {
      List<ResourceTableTab> result = resourceDataRepository.getResourceTableTabs(request);
      DEBUG_LOG.info("TEMP-DEBUG execute() repository returned {} tabs: {}", result.size(), result);
      return result;
    } catch (RuntimeException ex) {
      DEBUG_LOG.error("TEMP-DEBUG execute() repository call threw an exception", ex);
      throw ex;
    }
  }
}
