/*
 * Copyright (c) 2018 - 2022 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.service.impl;

import java.util.*;
import java.util.List;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantFilterQuery;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.cbioportal.legacy.persistence.StructuralVariantRepository;
import org.cbioportal.legacy.service.StructuralVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructuralVariantServiceImpl implements StructuralVariantService {

  @Autowired private StructuralVariantRepository structuralVariantRepository;

  @Override
  public List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<StructuralVariantQuery> structuralVariantQueries) {
    return structuralVariantRepository.fetchStructuralVariants(
        molecularProfileIds, sampleIds, entrezGeneIds, structuralVariantQueries);
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(
      List<String> molecularProfileIds, List<String> sampleIds, List<GeneFilterQuery> geneQueries) {

    return structuralVariantRepository.fetchStructuralVariantsByGeneQueries(
        molecularProfileIds, sampleIds, geneQueries);
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<StructuralVariantFilterQuery> structVarQueries) {
    return structuralVariantRepository.fetchStructuralVariantsByStructVarQueries(
        molecularProfileIds, sampleIds, structVarQueries);
  }
}
