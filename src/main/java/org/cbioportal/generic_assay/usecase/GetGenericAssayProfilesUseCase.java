package org.cbioportal.generic_assay.usecase;

import org.cbioportal.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.MolecularProfile;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetGenericAssayProfilesUseCase {
    private final GenericAssayRepository repository;

    public GetGenericAssayProfilesUseCase(GenericAssayRepository repository) {
        this.repository = repository;
    }

    public List<MolecularProfile> execute() {
        return repository.getGenericAssayProfiles();
    }
}
