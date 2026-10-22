/*
 * Copyright (c) 2018 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.persistence.mybatis;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantFilterQuery;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.cbioportal.legacy.persistence.StructuralVariantRepository;
import org.cbioportal.legacy.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class StructuralVariantMyBatisRepository implements StructuralVariantRepository {

  @Autowired private StructuralVariantMapper structuralVariantMapper;
  @Autowired private MolecularProfileCaseIdentifierUtil molecularProfileCaseIdentifierUtil;

  @Override
  public List<StructuralVariant> fetchStructuralVariants(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<Integer> entrezGeneIds,
      List<StructuralVariantQuery> structuralVariantQueries) {
    if (molecularProfileIds == null || molecularProfileIds.isEmpty()) {
      return new ArrayList<>();
    }
    return structuralVariantMapper.fetchStructuralVariants(
        molecularProfileIds, sampleIds, entrezGeneIds, structuralVariantQueries);
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(
      List<String> molecularProfileIds, List<String> sampleIds, List<GeneFilterQuery> geneQueries) {
    if (geneQueries == null
        || geneQueries.isEmpty()
        || molecularProfileIds == null
        || molecularProfileIds.isEmpty()) {
      return new ArrayList<>();
    }
    return structuralVariantMapper.fetchStructuralVariantsByGeneQueries(
        molecularProfileIds, sampleIds, geneQueries);
  }

  @Override
  public List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(
      List<String> molecularProfileIds,
      List<String> sampleIds,
      List<StructuralVariantFilterQuery> structVarQueries) {
    if (structVarQueries == null
        || structVarQueries.isEmpty()
        || molecularProfileIds == null
        || molecularProfileIds.isEmpty()) {
      return new ArrayList<>();
    }
    return structuralVariantMapper.fetchStructuralVariantsByStructVarQueries(
        molecularProfileIds, sampleIds, structVarQueries);
  }
}
