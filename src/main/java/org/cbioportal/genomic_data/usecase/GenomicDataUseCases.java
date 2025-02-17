package org.cbioportal.genomic_data.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
/**
 * A record representing a collection of use cases related to genomic data operations.
 * This record encapsulates instances of various genomic data use cases, providing a centralized
 * way to access and manage them.
 *
 * @param getCNACountsByGeneSpecificUseCase the use case for retrieving CNA counts by gene-specific data.
 * @param getGenomicDataBinCountsUseCase    the use case for retrieving genomic data bin counts.
 * @param getMolecularProfileSampleCountsUseCase the use case for retrieving molecular profile sample counts.
 * @param getMutationCountsByTypeUseCase    the use case for retrieving mutation counts by type.
 * @param getMutationCountsUseCase          the use case for retrieving mutation counts.
 */
public record GenomicDataUseCases(
        GetCNACountsByGeneSpecificUseCase getCNACountsByGeneSpecificUseCase,
        GetGenomicDataBinCountsUseCase getGenomicDataBinCountsUseCase,
        GetMolecularProfileSampleCountsUseCase getMolecularProfileSampleCountsUseCase,
        GetMutationCountsByTypeUseCase getMutationCountsByTypeUseCase,
        GetMutationCountsUseCase getMutationCountsUseCase
) {
}
