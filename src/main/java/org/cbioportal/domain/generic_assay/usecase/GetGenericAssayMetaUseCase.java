package org.cbioportal.domain.generic_assay.usecase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
  // Cache key normalizes input lists into sorted TreeSets so that callers passing
  // the same IDs in different order share the same cache entry. Null-safe: null
  // lists produce null keys. Non-null entries are filtered to exclude nulls.
  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()",
      key =
          "{#stableIds == null ? null : new java.util.TreeSet(#stableIds.?[#this != null]),"
              + " #molecularProfileIds == null ? null : new java.util.TreeSet(#molecularProfileIds.?[#this != null]),"
              + " #projection}")
  public List<GenericAssayMeta> execute(
      List<String> stableIds, List<String> molecularProfileIds, String projection) {
    return execute(stableIds, molecularProfileIds, projection, null, null, null);
  }

  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()",
      key =
          "{#stableIds == null ? null : new java.util.TreeSet(#stableIds.?[#this != null]),"
              + " #molecularProfileIds == null ? null : new java.util.TreeSet(#molecularProfileIds.?[#this != null]),"
              + " #projection,"
              + " (#searchTerm == null or #searchTerm.trim().isEmpty() ? null : #searchTerm.trim()),"
              + " #pageSize, #pageNumber}")
  public List<GenericAssayMeta> execute(
      List<String> stableIds,
      List<String> molecularProfileIds,
      String projection,
      String searchTerm,
      Integer pageSize,
      Integer pageNumber) {
    String normalizedSearchTerm = normalizeSearchTerm(searchTerm);
    Integer offset = pageSize == null || pageNumber == null ? null : pageSize * pageNumber;

    if (molecularProfileIds != null) {
      List<String> sortedProfileIds = molecularProfileIds.stream().distinct().sorted().toList();
      if (sortedProfileIds.isEmpty()) {
        return Collections.emptyList();
      }

      if ("ID".equals(projection)) {
        // Lightweight path: resolve IDs only, skip meta fetch
        var filteredIds =
            resolveFilteredIdsByProfileIds(sortedProfileIds, stableIds, normalizedSearchTerm);
        return pageIds(filteredIds, pageSize, pageNumber).stream()
            .map(GenericAssayMeta::new)
            .toList();
      }

      // Single merged query: profile → entity + meta join
      return repository.getGenericAssayMetaByProfileIds(
          sortedProfileIds, stableIds, normalizedSearchTerm, pageSize, offset);
    }

    if (stableIds == null || stableIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> distinctStableIds = stableIds.stream().distinct().toList();

    if ("ID".equals(projection)) {
      return pageIds(
              filterIdsBySearchTerm(distinctStableIds, normalizedSearchTerm), pageSize, pageNumber)
          .stream()
          .map(GenericAssayMeta::new)
          .toList();
    }

    return repository.getGenericAssayMetaByStableIds(
        distinctStableIds, normalizedSearchTerm, pageSize, offset);
  }

  // Mirrors the ID-projection branches in execute(): count must use the same match
  // criteria (stable ID only, no name/description) as the data it's counting, or the
  // reported total-count won't line up with what's actually paginable for that projection.
  @Cacheable(
      cacheResolver = "generalRepositoryCacheResolver",
      condition = "@cacheEnabledConfig.getEnabled()",
      key =
          "{#stableIds == null ? null : new java.util.TreeSet(#stableIds.?[#this != null]),"
              + " #molecularProfileIds == null ? null : new java.util.TreeSet(#molecularProfileIds.?[#this != null]),"
              + " #projection,"
              + " (#searchTerm == null or #searchTerm.trim().isEmpty() ? null : #searchTerm.trim()),"
              + " 'count'}")
  public Integer count(
      List<String> stableIds,
      List<String> molecularProfileIds,
      String projection,
      String searchTerm) {
    String normalizedSearchTerm = normalizeSearchTerm(searchTerm);

    if (molecularProfileIds != null) {
      List<String> sortedProfileIds = molecularProfileIds.stream().distinct().sorted().toList();
      if (sortedProfileIds.isEmpty()) {
        return 0;
      }
      if ("ID".equals(projection)) {
        return resolveFilteredIdsByProfileIds(sortedProfileIds, stableIds, normalizedSearchTerm)
            .size();
      }
      return repository.countGenericAssayMetaByProfileIds(
          sortedProfileIds, stableIds, normalizedSearchTerm);
    }

    if (stableIds == null || stableIds.isEmpty()) {
      return 0;
    }

    List<String> distinctStableIds = stableIds.stream().distinct().toList();
    if ("ID".equals(projection)) {
      return filterIdsBySearchTerm(distinctStableIds, normalizedSearchTerm).size();
    }

    return repository.countGenericAssayMetaByStableIds(distinctStableIds, normalizedSearchTerm);
  }

  private String normalizeSearchTerm(String searchTerm) {
    if (searchTerm == null || searchTerm.isBlank()) {
      return null;
    }
    return searchTerm.trim();
  }

  private boolean containsSearchText(String value, String searchTerm) {
    return searchTerm == null
        || (value != null
            && value.toLowerCase(Locale.ROOT).contains(searchTerm.toLowerCase(Locale.ROOT)));
  }

  private List<String> filterIdsBySearchTerm(Collection<String> ids, String normalizedSearchTerm) {
    return ids.stream().filter(id -> containsSearchText(id, normalizedSearchTerm)).toList();
  }

  private List<String> resolveFilteredIdsByProfileIds(
      List<String> sortedProfileIds, List<String> stableIds, String normalizedSearchTerm) {
    Set<String> resolvedIds =
        new LinkedHashSet<>(repository.getGenericAssayStableIdsByProfileIds(sortedProfileIds));
    if (stableIds != null) {
      resolvedIds.retainAll(new HashSet<>(stableIds));
    }
    return filterIdsBySearchTerm(resolvedIds, normalizedSearchTerm);
  }

  private List<String> pageIds(List<String> ids, Integer pageSize, Integer pageNumber) {
    if (pageSize == null || pageNumber == null) {
      return ids;
    }
    int offset = pageSize * pageNumber;
    if (offset >= ids.size()) {
      return Collections.emptyList();
    }
    return ids.subList(offset, Math.min(offset + pageSize, ids.size()));
  }
}
