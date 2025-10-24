package org.cbioportal.application.file.repositories;

import java.util.List;
import java.util.Set;
import org.cbioportal.application.file.model.GeneticProfileDatatypeMetadata;

/**
 * Repository interface for retrieving genetic profile metadata. This interface provides methods to
 * access genetic profiles for specific studies and samples, filtered by genetic alteration type and
 * datatype.
 */
public interface GeneticProfileRepository {
  /**
   * Retrieves genetic profiles for a specific study and set of sample IDs, filtered by genetic
   * alteration type and datatype.
   *
   * @param studyId the identifier of the study
   * @param sampleIds a set of sample IDs to filter the genetic profiles; null set means all samples
   * @param geneticAlterationType the type of genetic alteration (e.g., "MUTATION_EXTENDED",
   *     "COPY_NUMBER_ALTERATION")
   * @param datatype the datatype of the genetic profile (e.g., "DISCRETE", "CONTINUOUS",...)
   * @return a list of GeneticProfileDatatypeMetadata objects containing metadata for the specified
   *     genetic profiles
   */
  List<GeneticProfileDatatypeMetadata> getGeneticProfiles(
      String studyId, Set<String> sampleIds, String geneticAlterationType, String datatype);
}
