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
 * a grouped table, and whether the compact metadata table holds everything a grouped query needs.
 *
 * @param rows one per requested resource and requested study that defines or holds it
 * @param compactCurrent whether the compact table's copy of every requested resource, over the
 *     requested studies, is current: as many rows copied under each study's present contract as
 *     {@code resource} holds. It is only checked for a query the contracts alone would let read the
 *     compact table, and is false otherwise.
 */
public record ResourceScope(List<ResourceScopeRow> rows, boolean compactCurrent) {

  public ResourceScope {
    rows = rows == null ? List.of() : List.copyOf(rows);
  }

  /** A scope whose compact copy has not been checked, and so is not read. */
  public ResourceScope(List<ResourceScopeRow> rows) {
    this(rows, false);
  }

  public ResourceScope withCompactCurrent(boolean current) {
    return new ResourceScope(rows, current);
  }

  /** The requested resources that at least one requested study defines or holds rows of. */
  public Set<String> resourceIds() {
    Set<String> ids = new LinkedHashSet<>();
    rows.stream()
        .filter(row -> row.defined() || row.mayHoldRows())
        .forEach(row -> ids.add(row.resourceId()));
    return ids;
  }

  /**
   * The distinct contracts over the (resource, study) pairs that may hold rows, each with the
   * lowest study identifier carrying it, plus a blank row when some such pair declares none. Same
   * shape as the contract lookup the row queries use, so the column logic can take either.
   */
  public List<ResourceContractRow> contractRows() {
    Map<String, String> firstStudyByContract = new LinkedHashMap<>();
    rows.stream()
        .filter(ResourceScopeRow::mayHoldRows)
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
   * Whether the contracts would let the compact table answer a query reading these metadata keys.
   *
   * <p>The table copies a resource's rows only where its definition declares a contract with at
   * least one key, and keeps only the declared keys. So every (resource, study) pair that may hold
   * rows must have such a contract, and each key the query reads must be declared in every one of
   * them; otherwise the table would be missing rows or values.
   */
  public boolean contractsCover(Set<String> referencedKeys) {
    List<ResourceScopeRow> withRows = rows.stream().filter(ResourceScopeRow::mayHoldRows).toList();
    if (withRows.isEmpty()) {
      return false;
    }
    for (ResourceScopeRow row : withRows) {
      Set<String> declared =
          ResourceMetadataSchema.parse(row.customMetadata()).fieldsByKey().keySet();
      if (declared.isEmpty() || !declared.containsAll(referencedKeys)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Whether the compact table can answer a query reading these metadata keys: the contracts cover
   * them, and the table, which is only as fresh as the last repopulation, still holds a current
   * copy of every row. Failing either, the query reads {@code resource}.
   */
  public boolean compactCovers(Set<String> referencedKeys) {
    return compactCurrent && contractsCover(referencedKeys);
  }
}
