package org.cbioportal.domain.generic_assay.usecase;

import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.MolecularProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * Use case for retrieving generic assay molecular profiles. This class interacts with the {@link
 * GenericAssayRepository} to fetch the required data.
 */
public class GetGenericAssayProfilesUseCase {

  private final GenericAssayRepository repository;

  /**
   * Constructs a use case for retrieving generic assay profiles.
   *
   * @param repository The repository used to fetch generic assay profiles.
   */
  public GetGenericAssayProfilesUseCase(GenericAssayRepository repository) {
    this.repository = repository;
  }

  /**
   * Executes the use case to retrieve all generic assay profiles.
   *
   * @return A list of {@link MolecularProfile} representing generic assay profiles.
   */
  public List<MolecularProfile> execute() {
    return repository.getGenericAssayProfiles();
  }
}
