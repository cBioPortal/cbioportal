package org.cbioportal.domain.resource;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The requested resources as the requested studies define and hold them, read once per request from
 * the definitions and the sorting keys of {@code resource} and {@code resource_compact_derived}
 * rather than from the rows' metadata.
 *
 * <p>It answers three questions: whether every requested resource exists, which contracts describe
 * the table, and whether the compact metadata table holds everything a grouped query needs.
 */
public record ResourceScope(List<ResourceScopeRow> rows) {

  public ResourceScope {
    rows = rows == null ? List.of() : List.copyOf(rows);
  }

  /** The requested resources that at least one requested study defines or holds rows of. */
  public Set<String> resourceIds() {
    Set<String> ids = new LinkedHashSet<>();
    rows.stream()
        .filter(row -> row.defined() || row.hasRows())
        .forEach(row -> ids.add(row.resourceId()));
    return ids;
  }

  /**
   * The distinct contracts over the (resource, study) pairs that hold rows, each with the lowest
   * study identifier carrying it, plus a blank row when some pair holds rows without one. Same
   * shape as the contract lookup the row queries use, so the column logic can take either.
   */
  public List<ResourceContractRow> contractRows() {
    Map<String, String> firstStudyByContract = new LinkedHashMap<>();
    rows.stream()
        .filter(ResourceScopeRow::hasRows)
        .sorted(Comparator.comparing(ResourceScopeRow::studyId))
        .forEach(
            row ->
                firstStudyByContract.putIfAbsent(
                    row.declared() ? row.customMetadata() : "", row.studyId()));
    return firstStudyByContract.entrySet().stream()
        .map(entry -> new ResourceContractRow(entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparing(ResourceContractRow::firstStudy))
        .toList();
  }

  /**
   * Whether the compact metadata table can answer a query reading these metadata keys.
   *
   * <p>The table copies a resource's rows only where its definition declares a contract with at
   * least one key, and keeps only the declared keys. So every (resource, study) pair holding rows
   * must have such a contract, and each key the query reads must be declared in every one of them;
   * otherwise the table would be missing rows or values. The table is also only as fresh as the
   * last repopulation, so every pair's copy must still be current: as many rows as {@code resource}
   * holds, copied under today's contract. Failing any of these, the query reads {@code resource}.
   */
  public boolean compactCovers(Set<String> referencedKeys) {
    if (rows.stream().noneMatch(ResourceScopeRow::hasRows)
        || !rows.stream().allMatch(ResourceScopeRow::compactCurrent)) {
      return false;
    }
    for (ResourceScopeRow row : rows.stream().filter(ResourceScopeRow::hasRows).toList()) {
      Set<String> declared =
          ResourceMetadataSchema.parse(row.customMetadata()).fieldsByKey().keySet();
      if (declared.isEmpty() || !declared.containsAll(referencedKeys)) {
        return false;
      }
    }
    return true;
  }
}
