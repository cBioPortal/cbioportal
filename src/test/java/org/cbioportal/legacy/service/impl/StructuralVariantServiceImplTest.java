/*
 * Copyright (c) 2018 - 2022 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.cbioportal.legacy.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.cbioportal.legacy.model.GeneFilterQuery;
import org.cbioportal.legacy.model.StructuralVariant;
import org.cbioportal.legacy.model.StructuralVariantQuery;
import org.cbioportal.legacy.persistence.StructuralVariantRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StructuralVariantServiceImplTest extends BaseServiceImplTest {

  @InjectMocks private StructuralVariantServiceImpl structuralVariantService;

  @Mock private StructuralVariantRepository structuralVariantRepository;

  List<StructuralVariant> expectedStructuralVariantList =
      Collections.singletonList(new StructuralVariant());
  List<String> molecularProfileIds = Collections.singletonList("study_structural_variants");
  List<String> sampleIds = Collections.singletonList(SAMPLE_ID1);

  @Test
  public void getStructuralVariants() {

    List<Integer> entrezGeneIds = Collections.singletonList(ENTREZ_GENE_ID_1);
    List<StructuralVariantQuery> noStructuralVariant = Collections.emptyList();

    Mockito.when(
            structuralVariantRepository.fetchStructuralVariants(
                molecularProfileIds, sampleIds, entrezGeneIds, noStructuralVariant))
        .thenReturn(expectedStructuralVariantList);

    List<StructuralVariant> result =
        structuralVariantService.fetchStructuralVariants(
            molecularProfileIds, sampleIds, entrezGeneIds, noStructuralVariant);

    Assert.assertEquals(expectedStructuralVariantList, result);
  }

  @Test
  public void getStructuralVariantsByGeneFilterQueries() {

    List<GeneFilterQuery> geneFilterQueries = new ArrayList<>();

    Mockito.when(
            structuralVariantRepository.fetchStructuralVariantsByGeneQueries(
                molecularProfileIds, sampleIds, geneFilterQueries))
        .thenReturn(expectedStructuralVariantList);

    List<StructuralVariant> result =
        structuralVariantService.fetchStructuralVariantsByGeneQueries(
            molecularProfileIds, sampleIds, geneFilterQueries);

    Assert.assertEquals(expectedStructuralVariantList, result);
  }
}
