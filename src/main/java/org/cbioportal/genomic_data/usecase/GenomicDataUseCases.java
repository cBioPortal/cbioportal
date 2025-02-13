package org.cbioportal.genomic_data.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public record GenomicDataUseCases(
        GetCNACountsByGeneSpecificUseCase getCNACountsByGeneSpecificUseCase,
        GetGenomicDataBinCountsUseCase getGenomicDataBinCountsUseCase,
        GetMolecularProfileSampleCountsUseCase getMolecularProfileSampleCountsUseCase,
        GetMutationCountsByTypeUseCase getMutationCountsByTypeUseCase,
        GetMutationCountsUseCase getMutationCountsUseCase
) {
}
