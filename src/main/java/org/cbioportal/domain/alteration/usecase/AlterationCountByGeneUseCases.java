package org.cbioportal.domain.alteration.usecase;

import org.springframework.stereotype.Service;

@Service
public record AlterationCountByGeneUseCases(
    GetAlterationCountByGeneUseCase getAlterationCountByGeneUseCase,
    GetCnaAlterationCountByGeneUseCase getCnaAlterationCountByGeneUseCase) {}
