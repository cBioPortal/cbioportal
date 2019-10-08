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

import java.util.List;
import java.util.Collections;

import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantCountByGene;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructuralVariantServiceImpl implements StructuralVariantService {

    @Autowired
    private StructuralVariantRepository structuralVariantRepository;

    @Autowired
    private AlterationEnrichmentUtil alterationEnrichmentUtil;

    @Override
    public List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds, 
            List<Integer> entrezGeneIds,  List<String> sampleIds) {
        
        return structuralVariantRepository.fetchStructuralVariants(molecularProfileIds, entrezGeneIds, sampleIds);
    }

    @Override
    public List<StructuralVariantCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                               List<String> sampleIds, List<Integer> entrezGeneIds, boolean includeFrequency) {

        List<StructuralVariantCountByGene> alterationCountByGenes;
        if (molecularProfileIds.isEmpty()) {
            alterationCountByGenes = Collections.emptyList();
        } else {
            alterationCountByGenes = structuralVariantRepository.getSampleCountInMultipleMolecularProfiles(
                molecularProfileIds, sampleIds, entrezGeneIds);
            if (includeFrequency) {
                alterationEnrichmentUtil.includeFrequencyForSamples(molecularProfileIds, sampleIds, alterationCountByGenes);
            }
        }
        alterationCountByGenes.sort((a, b) -> b.getNumberOfAlteredCases() - a.getNumberOfAlteredCases());
        return alterationCountByGenes;
    }
}
