package org.cbioportal.domain.generic_assay.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * A record representing a collection of use cases related to generic assay data operations. This
 * record encapsulates instances of various use case classes, providing a centralized way to access
 * and manage them.
 *
 * @param getFilteredMolecularProfilesByAlterationType the use case for retrieving molecular
 *     profiles filtered by alteration type.
 * @param getGenericAssayDataBinCounts the use case for retrieving binned counts of generic assay
 *     data.
 * @param getGenericAssayDataCountsUseCase the use case for retrieving counts of generic assay data.
 * @param getGenericAssayProfilesUseCase the use case for retrieving generic assay profiles.
 */
public record GenericAssayUseCases(
    GetFilteredMolecularProfilesByAlterationType getFilteredMolecularProfilesByAlterationType,
    GetGenericAssayDataBinCounts getGenericAssayDataBinCounts,
    GetGenericAssayDataCountsUseCase getGenericAssayDataCountsUseCase,
    GetGenericAssayProfilesUseCase getGenericAssayProfilesUseCase) {}
