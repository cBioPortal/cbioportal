package org.cbioportal.domain.alteration.usecase;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public record AlterationCountByGeneUseCases(
    GetAlterationCountByGeneUseCase getAlterationCountByGeneUseCase,
    GetCnaAlterationCountByGeneUseCase getCnaAlterationCountByGeneUseCase) {


}
