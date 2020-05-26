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

import org.cbioportal.model.StructuralVariant;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class StructuralVariantServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private StructuralVariantServiceImpl structuralVariantService;
    
    @Mock
    private StructuralVariantRepository structuralVariantRepository;
    
    @Test
    public void getStructuralVariants() throws Exception {

        List<StructuralVariant> expectedStructuralVariantList = new ArrayList<>();
        StructuralVariant sampleStructuralVariant = new StructuralVariant();
        expectedStructuralVariantList.add(sampleStructuralVariant);
        
        List<String> molecularProfileIds = new ArrayList<>();
        List<Integer> entrezGeneIds = new ArrayList<>();
        molecularProfileIds.add("genetic_profile_id");
        entrezGeneIds.add(ENTREZ_GENE_ID_1);

        Mockito.when(structuralVariantRepository.fetchStructuralVariants(molecularProfileIds, 
                entrezGeneIds, null))
            .thenReturn(expectedStructuralVariantList);

        List<StructuralVariant> result = structuralVariantService.fetchStructuralVariants(molecularProfileIds, 
                entrezGeneIds, null);

        Assert.assertEquals(expectedStructuralVariantList, result);
    }
}
