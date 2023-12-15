/*
 * Copyright (c) 2018 - 2022 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantQuery;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StructuralVariantServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private StructuralVariantServiceImpl structuralVariantService;

    @Mock
    private StructuralVariantRepository structuralVariantRepository;
    
    List<StructuralVariant> expectedStructuralVariantList = Collections.singletonList(new StructuralVariant());
    List<String> molecularProfileIds = Collections.singletonList("study_structural_variants");
    List<String> sampleIds = Collections.singletonList(SAMPLE_ID1);

    @Test
    public void getStructuralVariants() {

        List<Integer> entrezGeneIds = Collections.singletonList(ENTREZ_GENE_ID_1);
        List<StructuralVariantQuery> noStructuralVariant = Collections.emptyList();

        Mockito.when(structuralVariantRepository.fetchStructuralVariants(molecularProfileIds, sampleIds, entrezGeneIds, noStructuralVariant))
                .thenReturn(expectedStructuralVariantList);

        List<StructuralVariant> result = structuralVariantService.fetchStructuralVariants(molecularProfileIds,
            sampleIds, entrezGeneIds, noStructuralVariant);

        Assert.assertEquals(expectedStructuralVariantList, result);
    }

    @Test
    public void getStructuralVariantsByGeneFilterQueries() {
    
        List<GeneFilterQuery> geneFilterQueries = new ArrayList<>();

        Mockito.when(structuralVariantRepository.fetchStructuralVariantsByGeneQueries(molecularProfileIds, sampleIds, geneFilterQueries))
            .thenReturn(expectedStructuralVariantList);

        List<StructuralVariant> result = structuralVariantService.fetchStructuralVariantsByGeneQueries(molecularProfileIds,
            sampleIds, geneFilterQueries);

        Assert.assertEquals(expectedStructuralVariantList, result);
    }
}
