/*
 * Copyright (c) 2018 The Hyve B.V.
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

import java.util.Collections;
import java.util.List;

import org.cbioportal.model.StructuralVariant;
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

    @Test
    public void getStructuralVariants() throws Exception {

        List<StructuralVariant> expectedStructuralVariantList = Collections.singletonList(new StructuralVariant());
        List<String> molecularProfileIds = Collections.singletonList("study_structural_variants");
        List<Integer> entrezGeneIds = Collections.singletonList(ENTREZ_GENE_ID_1);
        List<String> sampleIds = Collections.singletonList(SAMPLE_ID1);

        Mockito.when(structuralVariantRepository.fetchStructuralVariants(molecularProfileIds, entrezGeneIds, sampleIds))
                .thenReturn(expectedStructuralVariantList);

        List<StructuralVariant> result = structuralVariantService.fetchStructuralVariants(molecularProfileIds,
                entrezGeneIds, sampleIds);

        Assert.assertEquals(expectedStructuralVariantList, result);
    }
}
