package org.cbioportal.legacy.web.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.service.MolecularDataService;
import org.cbioportal.legacy.service.MolecularProfileService;
import org.cbioportal.legacy.service.SampleListService;
import org.cbioportal.legacy.service.exception.BulkRequestTooLargeException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.cbioportal.legacy.web.parameter.SampleMolecularIdentifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rejects molecular data and discrete copy number requests that would load too much data into
 * memory, with HTTP 400 and {@code bulk_request.help_text} (e.g. a pointer to bulk downloads).
 *
 * <ul>
 *   <li>{@code bulk_request.max_matrix_size}: genes × samples for dense data (molecular data, and
 *       copy number event types derived from it). A request without entrezGeneIds counts as {@link
 *       #ALL_GENES} genes.
 *   <li>{@code bulk_request.max_rows_per_request}: the number of matching rows for sparse data
 *       (HOMDEL/AMP copy number events), checked with a count query before the data is fetched.
 * </ul>
 *
 * Both limits are disabled by default (negative values).
 */
@Component
public class BulkRequestLimiter {

  /** Number of genes assumed for requests without entrezGeneIds (whole-genome profiles). */
  static final int ALL_GENES = 20000;

  private final SampleListService sampleListService;
  private final MolecularDataService molecularDataService;
  private final MolecularProfileService molecularProfileService;
  private final long maxMatrixSize;
  private final long maxRowsPerRequest;
  private final String helpText;

  public BulkRequestLimiter(
      SampleListService sampleListService,
      MolecularDataService molecularDataService,
      MolecularProfileService molecularProfileService,
      @Value("${bulk_request.max_matrix_size:-1}") long maxMatrixSize,
      @Value("${bulk_request.max_rows_per_request:-1}") long maxRowsPerRequest,
      @Value(
              "${bulk_request.help_text:Request a subset of genes and samples, or download the data in bulk.}")
          String helpText) {
    this.sampleListService = sampleListService;
    this.molecularDataService = molecularDataService;
    this.molecularProfileService = molecularProfileService;
    this.maxMatrixSize = maxMatrixSize;
    this.maxRowsPerRequest = maxRowsPerRequest;
    this.helpText = helpText;
  }

  public boolean isRowLimitEnabled() {
    return maxRowsPerRequest >= 0;
  }

  /** Dense request for {@code sampleCount} samples; a null or empty gene list means all genes. */
  public void checkMatrix(long sampleCount, Collection<Integer> entrezGeneIds) {
    if (maxMatrixSize < 0) {
      return;
    }
    long genes = geneCount(entrezGeneIds);
    long size = sampleCount * genes;
    if (size > maxMatrixSize) {
      throw tooLarge(
          "Requests are limited to "
              + maxMatrixSize
              + " genes x samples ("
              + genes
              + " x "
              + sampleCount
              + " requested"
              + (entrezGeneIds == null || entrezGeneIds.isEmpty()
                  ? "; without entrezGeneIds all genes are counted"
                  : "")
              + ").");
    }
  }

  /** Dense request for the samples in {@code sampleListId}. */
  public void checkMatrixForSampleList(String sampleListId, Collection<Integer> entrezGeneIds) {
    if (maxMatrixSize < 0 || sampleListId == null) {
      return;
    }
    SampleList sampleList;
    try {
      sampleList = sampleListService.getSampleList(sampleListId);
    } catch (SampleListNotFoundException e) {
      // The data query reports the missing sample list itself.
      return;
    }
    List<String> sampleIds = sampleList.getSampleIds();
    checkMatrix(sampleIds == null ? 0 : sampleIds.size(), entrezGeneIds);
  }

  /** Dense request for all samples of each of {@code molecularProfileIds}. */
  public void checkMatrixForProfiles(
      Collection<String> molecularProfileIds, Collection<Integer> entrezGeneIds) {
    if (maxMatrixSize < 0 || molecularProfileIds == null) {
      return;
    }
    long samples = 0;
    for (String molecularProfileId : new HashSet<>(molecularProfileIds)) {
      Integer count = molecularDataService.getNumberOfSamplesInMolecularProfile(molecularProfileId);
      samples += count == null ? 0 : count;
    }
    checkMatrix(samples, entrezGeneIds);
  }

  /**
   * Dense request by sample/profile pairs. Data is returned for every requested sample in every
   * requested profile of the same study, so the effective size is the sum over studies of distinct
   * samples × distinct profiles.
   */
  public void checkMatrixForSampleMolecularIdentifiers(
      Collection<SampleMolecularIdentifier> identifiers, Collection<Integer> entrezGeneIds) {
    if (maxMatrixSize < 0 || identifiers == null) {
      return;
    }
    Set<String> profileIds =
        identifiers.stream()
            .map(SampleMolecularIdentifier::getMolecularProfileId)
            .collect(Collectors.toSet());
    Map<String, String> studyByProfile =
        molecularProfileService.getMolecularProfiles(profileIds, "SUMMARY").stream()
            .collect(
                Collectors.toMap(
                    MolecularProfile::getStableId,
                    MolecularProfile::getCancerStudyIdentifier,
                    (a, b) -> a));

    Map<String, Set<String>> samplesByStudy = new HashMap<>();
    Map<String, Set<String>> profilesByStudy = new HashMap<>();
    for (SampleMolecularIdentifier identifier : identifiers) {
      String profileId = identifier.getMolecularProfileId();
      String studyId = studyByProfile.getOrDefault(profileId, profileId);
      samplesByStudy.computeIfAbsent(studyId, k -> new HashSet<>()).add(identifier.getSampleId());
      profilesByStudy.computeIfAbsent(studyId, k -> new HashSet<>()).add(profileId);
    }
    long samples =
        samplesByStudy.entrySet().stream()
            .mapToLong(e -> (long) e.getValue().size() * profilesByStudy.get(e.getKey()).size())
            .sum();
    checkMatrix(samples, entrezGeneIds);
  }

  /** Sparse request that would return {@code rowCount} rows. */
  public void checkRows(long rowCount) {
    if (maxRowsPerRequest >= 0 && rowCount > maxRowsPerRequest) {
      throw tooLarge(
          "Requests are limited to " + maxRowsPerRequest + " rows (" + rowCount + " matching).");
    }
  }

  private static long geneCount(Collection<Integer> entrezGeneIds) {
    return entrezGeneIds == null || entrezGeneIds.isEmpty() ? ALL_GENES : entrezGeneIds.size();
  }

  private BulkRequestTooLargeException tooLarge(String reason) {
    return new BulkRequestTooLargeException(reason + " " + helpText);
  }
}
