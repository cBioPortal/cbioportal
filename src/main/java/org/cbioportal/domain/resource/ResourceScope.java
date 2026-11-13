package org.cbioportal.domain.resource;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The requested resources as the requested studies define and hold them, read once per request from
 * the definitions and the sorting key of {@code resource} rather than from the rows' metadata.
 *
 * <p>It answers two questions: whether every requested resource exists, and which contracts
 * describe a grouped table.
 *
 * @param rows one per requested resource and requested study that defines or holds it
 */
public record ResourceScope(List<ResourceScopeRow> rows) {

  public ResourceScope {
    rows = rows == null ? List.of() : List.copyOf(rows);
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
}
