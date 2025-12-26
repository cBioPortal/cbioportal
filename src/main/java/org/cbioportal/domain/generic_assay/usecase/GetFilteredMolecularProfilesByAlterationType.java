package org.cbioportal.domain.generic_assay.usecase;

import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.MolecularProfile;
import org.springframework.stereotype.Service;

@Service
/**
 * A use case class responsible for retrieving molecular profiles filtered by a specific alteration
 * type. This class acts as an intermediary between the application logic and the data repository,
 * delegating the data retrieval to the {@link GenericAssayRepository}.
 */
public class GetFilteredMolecularProfilesByAlterationType {
  private final GenericAssayRepository genericAssayRepository;

  /**
   * Constructs a new instance of {@link GetFilteredMolecularProfilesByAlterationType}.
   *
   * @param genericAssayRepository the repository used to access molecular profile data. Must not be
   *     {@code null}.
   */
  public GetFilteredMolecularProfilesByAlterationType(
      GenericAssayRepository genericAssayRepository) {
    this.genericAssayRepository = genericAssayRepository;
  }

  /**
   * Executes the use case to retrieve molecular profiles filtered by a specific alteration type.
   *
   * @param studyViewFilterContext the context containing study view filter criteria. Must not be
   *     {@code null}.
   * @param alterationType the type of alteration used to filter molecular profiles. Must not be
   *     {@code null}.
   * @return a list of {@link MolecularProfile} objects representing the molecular profiles that
   *     match the provided alteration type. The list may be empty if no profiles match the
   *     criteria.
   */
  public List<MolecularProfile> execute(
      StudyViewFilterContext studyViewFilterContext, String alterationType) {
    return genericAssayRepository.getFilteredMolecularProfilesByAlterationType(
        studyViewFilterContext, alterationType);
  }
}
