package org.cbioportal.domain.mutation.repository;

import java.util.Collection;
import java.util.List;
import org.cbioportal.domain.mutation.PatientGenePanel;
import org.cbioportal.domain.mutation.PatientMutatedGene;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.model.meta.MutationMeta;
import org.cbioportal.shared.MutationQueryOptions;

/**
 * Repository interface for accessing mutation data or mutation.
 *
 * <p>This abstraction defines the contract for retrieving both detailed mutation records and
 * aggregated mutation metadata across multiple molecular profiles, samples, and genes.
 */
public interface MutationRepository {

  /**
   * Retrieves a list of mutations that match the specified filters and search criteria. The
   * util(molecularProfileCaseIdentifierUtil) filters and groups sampleId and molecularProfileIds to
   * sanitize users input,avoiding round trip
   *
   * @param molecularProfileIds List of molecularProfileIds
   * @param sampleIds List of sampleIds
   * @param entrezGeneIds List of entrezGeneIds
   * @param mutationQueryOptions A criteria to control the appearance of the dataset
   * @return a list of {@link Mutation} objects that match the given criteria
   * @see MutationQueryOptions
   */
  List<Mutation> getMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      MutationQueryOptions mutationQueryOptions);

  /**
   * Retrieves aggregated metadata about mutations that match the specified filters.
   *
   * <p>This method is typically used to determine dataset size, counts information without fetching
   * full mutation details.The util(molecularProfileCaseIdentifierUtil) filters and groups sampleId
   * and molecularProfileIds to sanitize users input,avoiding round trip
   *
   * @param molecularProfileIds List of molecularProfileIds
   * @param sampleIds List of sampleIds
   * @param entrezGeneIds List of Entrez gene identifiers
   * @return {@link MutationMeta} containing aggregated information about the dataset
   */
  MutationMeta getMetaMutationsInMultipleMolecularProfiles(
      List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds);

  /**
   * Checks whether a molecular profile is a mutation profile of the given study.
   *
   * @param studyId cancer study identifier
   * @param molecularProfileId molecular profile stable ID
   * @return {@code true} if the study has a mutation profile with this ID
   */
  boolean isMutationMolecularProfileOfStudy(String studyId, String molecularProfileId);

  /**
   * Retrieves which of the given genes are mutated in which patients of a mutation profile.
   * Uncalled mutations are left out.
   *
   * @param molecularProfileId mutation profile stable ID
   * @param hugoGeneSymbols upper-case Hugo gene symbols; genes are matched case-insensitively
   * @return one entry per distinct patient and mutated gene
   */
  List<PatientMutatedGene> getMutatedGenesOfPatients(
      String molecularProfileId, List<String> hugoGeneSymbols);

  /**
   * Retrieves the gene panels that patients were profiled with in a mutation profile, for the
   * reference patient and for every patient with a mutation in one of the given genes.
   *
   * @param studyId cancer study identifier of the molecular profile
   * @param molecularProfileId mutation profile stable ID
   * @param hugoGeneSymbols upper-case Hugo gene symbols; genes are matched case-insensitively
   * @param referencePatientId patient whose gene panels are included even without such mutations
   * @return one entry per distinct patient and gene panel; the reference patient has no entry if
   *     none of its samples were profiled in the molecular profile
   */
  List<PatientGenePanel> getGenePanelsOfPatients(
      String studyId,
      String molecularProfileId,
      List<String> hugoGeneSymbols,
      String referencePatientId);

  /**
   * Retrieves which of the given genes each of the given gene panels covers. The {@code WES} panel
   * covers every gene.
   *
   * @param genePanelIds gene panel stable IDs
   * @param hugoGeneSymbols upper-case Hugo gene symbols; genes are matched case-insensitively
   * @return one entry per gene panel and covered gene, with the gene panel ID and Hugo gene symbol
   */
  List<GenePanelToGene> getGenePanelGenes(
      Collection<String> genePanelIds, List<String> hugoGeneSymbols);
}
