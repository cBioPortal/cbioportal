package org.cbioportal.application.file.repositories;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.utils.CloseableIterator;

/**
 * Repository interface for retrieving gene panel matrix data. This interface provides methods to
 * access gene panel matrix items for specific studies and samples.
 */
public interface GenePanelMatrixRepository {

  /**
   * Checks if gene panel matrix items exist for the specified study and sample IDs.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to filter the gene panel matrix items; null set means all
   *     samples
   * @return true if gene panel matrix items exist for the specified study and sample IDs, false
   *     otherwise
   */
  boolean hasGenePanelMatrix(String studyId, Set<String> sampleIds);

  /**
   * Retrieves gene panel matrix items for a specific study and set of sample IDs.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to filter the gene panel matrix items; null set means all
   *     samples
   * @return an iterator containing gene panel matrix items for the specified study and samples
   */
  CloseableIterator<GenePanelMatrixItem> getGenePanelMatrix(String studyId, Set<String> sampleIds);

  /**
   * Retrieves distinct gene profile IDs associated with the gene panel matrix for a specific study
   * and set of sample IDs.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to filter the gene profile IDs; null set means all samples
   * @return a list of distinct gene profile IDs associated with the gene panel matrix for the
   *     specified study and samples
   */
  List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(String studyId, Set<String> sampleIds);
}
