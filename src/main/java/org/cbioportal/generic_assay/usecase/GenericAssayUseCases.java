package org.cbioportal.generic_assay.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public record GenericAssayUseCases(
        GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType,
        GetGenericAssayDataBinCounts getGenericAssayDataBinCounts,
        GetGenericAssayDataCountsUseCase getGenericAssayDataCountsUseCase,
        GetGenericAssayProfilesUseCase getGenericAssayProfilesUseCase
) {
}
