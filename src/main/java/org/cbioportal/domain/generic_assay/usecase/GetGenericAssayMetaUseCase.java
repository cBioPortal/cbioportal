package org.cbioportal.domain.generic_assay.usecase;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
/**
 * Use case for retrieving generic assay meta data. This class interacts with the {@link
 * GenericAssayRepository} to fetch meta data by stable IDs and/or molecular profile IDs.
 */
public class GetGenericAssayMetaUseCase {

  private final GenericAssayRepository repository;

  public GetGenericAssayMetaUseCase(GenericAssayRepository repository) {
    this.repository = repository;
  }

  /**
   * Executes the use case to retrieve generic assay meta data.
   *
   * @param stableIds optional list of generic assay stable IDs to filter by
   * @param molecularProfileIds optional list of molecular profile IDs to filter by
   * @param projection projection level (e.g. "ID", "SUMMARY", "DETAILED")
   * @return a list of {@link GenericAssayMeta}
   */
  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()",
      key =
          "{#stableIds == null ? null : new java.util.TreeSet(#stableIds.?[#this != null]),"
              + " #molecularProfileIds == null ? null : new java.util.TreeSet(#molecularProfileIds.?[#this != null]),"
              + " #projection}")
  public List<GenericAssayMeta> execute(
      List<String> stableIds, List<String> molecularProfileIds, String projection) {

    if (molecularProfileIds != null) {
      List<String> sortedProfileIds = molecularProfileIds.stream().distinct().sorted().toList();
      if (sortedProfileIds.isEmpty()) {
        return Collections.emptyList();
      }

      if ("ID".equals(projection)) {
        // Lightweight path: resolve IDs only, skip meta fetch
        Set<String> resolvedIds =
            new LinkedHashSet<>(repository.getGenericAssayStableIdsByProfileIds(sortedProfileIds));
        if (stableIds != null) {
          resolvedIds.retainAll(new HashSet<>(stableIds));
        }
        return resolvedIds.stream().map(GenericAssayMeta::new).toList();
      }

      // Single merged query: profile → entity + meta join
      return repository.getGenericAssayMetaByProfileIds(sortedProfileIds, stableIds);
    }

    if (stableIds == null || stableIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> distinctStableIds = stableIds.stream().distinct().toList();

    if ("ID".equals(projection)) {
      return distinctStableIds.stream().map(GenericAssayMeta::new).toList();
    }

    return repository.getGenericAssayMetaByStableIds(distinctStableIds);
  }
}
