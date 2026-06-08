package org.cbioportal.domain.generic_assay.usecase;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
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
   * Executes the use case, streaming each matching {@link GenericAssayMeta} to {@code consumer} as
   * it is produced. Streaming keeps memory bounded: for large profile sets (e.g. methylation, which
   * can resolve to hundreds of thousands of entities) the full result is never materialized into a
   * list. This path is intentionally not cached — the cache key would be unique per study/profile
   * combination (near-zero hit rate) while pinning the entire result on the heap.
   *
   * @param stableIds optional list of generic assay stable IDs to filter by
   * @param molecularProfileIds optional list of molecular profile IDs to filter by
   * @param projection projection level (e.g. "ID", "SUMMARY", "DETAILED")
   * @param consumer invoked once per matching {@link GenericAssayMeta}
   */
  public void execute(
      List<String> stableIds,
      List<String> molecularProfileIds,
      String projection,
      Consumer<GenericAssayMeta> consumer) {

    if (molecularProfileIds != null) {
      List<String> sortedProfileIds = molecularProfileIds.stream().distinct().sorted().toList();
      if (sortedProfileIds.isEmpty()) {
        return;
      }

      if ("ID".equals(projection)) {
        // Lightweight path: resolve IDs only, skip meta fetch
        Set<String> resolvedIds =
            new LinkedHashSet<>(repository.getGenericAssayStableIdsByProfileIds(sortedProfileIds));
        if (stableIds != null) {
          resolvedIds.retainAll(new HashSet<>(stableIds));
        }
        resolvedIds.forEach(id -> consumer.accept(new GenericAssayMeta(id)));
        return;
      }

      // Single merged query: profile → entity + meta join
      repository.getGenericAssayMetaByProfileIds(sortedProfileIds, stableIds, consumer);
      return;
    }

    if (stableIds == null || stableIds.isEmpty()) {
      return;
    }

    List<String> distinctStableIds = stableIds.stream().distinct().toList();

    if ("ID".equals(projection)) {
      distinctStableIds.forEach(id -> consumer.accept(new GenericAssayMeta(id)));
      return;
    }

    repository.getGenericAssayMetaByStableIds(distinctStableIds, consumer);
  }
}
