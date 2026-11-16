package org.cbioportal.domain.genomic_data.repository;

import java.util.List;
import org.cbioportal.legacy.model.GeneMolecularData;
import org.cbioportal.legacy.model.MolecularProfile;

/** Retrieval of individual gene-level molecular measurements of a molecular profile. */
public interface GeneMolecularDataRepository {

  /**
   * Fetches the measurements of a gene-based molecular profile.
   *
   * @param studyId the cancer study identifier the profile belongs to
   * @param molecularProfileId the stable id of the molecular profile
   * @param profileType the profile type, i.e. the molecular profile id without the study prefix
   * @param entrezGeneIds genes to fetch, or null/empty to fetch all genes in the profile
   * @param sampleListId stable id of a sample list restricting the samples, or null
   * @param sampleIds stable sample ids restricting the samples, or null. Exactly one of {@code
   *     sampleListId} and {@code sampleIds} is expected to be set
   * @param includeGene whether to populate {@link GeneMolecularData#getGene()}
   * @return the raw measurements; values are rounded as in the legacy API but may be non-numeric
   */
  List<GeneMolecularData> getGeneMolecularData(
      String studyId,
      String molecularProfileId,
      String profileType,
      List<Integer> entrezGeneIds,
      String sampleListId,
      List<String> sampleIds,
      boolean includeGene);

  /**
   * Fetches the measurements of several gene-based molecular profiles, possibly of different
   * studies.
   *
   * @param profiles the molecular profiles to read from
   * @param entrezGeneIds genes to fetch
   * @param sampleIds stable sample ids of the samples to fetch, or null for all samples. A sample
   *     id is looked up in every requested profile of its own study, see {@code sampleStudyIds}
   * @param sampleStudyIds for each entry of {@code sampleIds} the study the sample belongs to
   * @param includeGene whether to populate {@link GeneMolecularData#getGene()}
   */
  List<GeneMolecularData> getGeneMolecularDataInMultipleProfiles(
      List<MolecularProfile> profiles,
      List<Integer> entrezGeneIds,
      List<String> sampleIds,
      List<String> sampleStudyIds,
      boolean includeGene);
}
