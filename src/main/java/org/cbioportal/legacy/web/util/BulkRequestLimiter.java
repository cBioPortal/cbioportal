package org.cbioportal.legacy.web.util;

import java.util.Collection;
import org.cbioportal.legacy.model.SampleList;
import org.cbioportal.legacy.service.SampleListService;
import org.cbioportal.legacy.service.exception.BulkRequestTooLargeException;
import org.cbioportal.legacy.service.exception.SampleListNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Rejects molecular data requests (molecular data, discrete copy number) that would materialize a
 * whole profile in memory: requests without a gene list for more than {@code
 * bulk_request.max_samples_without_genes} samples, or with more than {@code
 * bulk_request.max_values} genes × samples. Both limits are disabled by default (negative values);
 * large requests then behave as before.
 *
 * <p>Rejected requests get a 400 with {@code bulk_request.help_text} appended, e.g. a pointer to
 * where the data can be downloaded in bulk.
 */
@Component
public class BulkRequestLimiter {

  @Autowired private SampleListService sampleListService;

  @Value("${bulk_request.max_samples_without_genes:-1}")
  private int maxSamplesWithoutGenes;

  @Value("${bulk_request.max_values:-1}")
  private long maxValues;

  @Value(
      "${bulk_request.help_text:Request a subset of genes and samples, or download the data in bulk.}")
  private String helpText;

  public boolean isEnabled() {
    return maxSamplesWithoutGenes >= 0 || maxValues >= 0;
  }

  /** Checks a request for the samples in {@code sampleListId}; a null gene list means all genes. */
  public void checkSampleList(String sampleListId, Collection<Integer> entrezGeneIds) {
    if (!isEnabled() || sampleListId == null) {
      return;
    }
    int sampleCount;
    try {
      SampleList sampleList = sampleListService.getSampleList(sampleListId);
      sampleCount = sampleList.getSampleIds() == null ? 0 : sampleList.getSampleIds().size();
    } catch (SampleListNotFoundException e) {
      // The data query reports the missing sample list itself.
      return;
    }
    checkSampleCount(sampleCount, entrezGeneIds);
  }

  /** Checks a request for {@code sampleCount} samples; a null gene list means all genes. */
  public void checkSampleCount(int sampleCount, Collection<Integer> entrezGeneIds) {
    if (!isEnabled()) {
      return;
    }
    if (entrezGeneIds == null || entrezGeneIds.isEmpty()) {
      if (maxSamplesWithoutGenes >= 0 && sampleCount > maxSamplesWithoutGenes) {
        throw tooLarge(
            "Requests without entrezGeneIds are limited to "
                + maxSamplesWithoutGenes
                + " samples ("
                + sampleCount
                + " requested).");
      }
      return;
    }
    long values = (long) sampleCount * entrezGeneIds.size();
    if (maxValues >= 0 && values > maxValues) {
      throw tooLarge(
          "Requests are limited to "
              + maxValues
              + " genes x samples ("
              + entrezGeneIds.size()
              + " x "
              + sampleCount
              + " requested).");
    }
  }

  /**
   * Checks a request for all samples of whole molecular profiles. Without a gene list the size
   * can't be bounded up front, so it is rejected whenever {@code max_samples_without_genes} is set.
   */
  public void checkWholeProfiles(Collection<Integer> entrezGeneIds) {
    if (maxSamplesWithoutGenes >= 0 && (entrezGeneIds == null || entrezGeneIds.isEmpty())) {
      throw tooLarge("Requests for whole molecular profiles must include entrezGeneIds.");
    }
  }

  private BulkRequestTooLargeException tooLarge(String reason) {
    return new BulkRequestTooLargeException(reason + " " + helpText);
  }
}
