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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.service.util.MutationMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StructuralVariantServiceImpl implements StructuralVariantService {

    @Autowired
    private StructuralVariantRepository structuralVariantRepository;
    @Autowired
    private MutationRepository mutationRepository;
    @Autowired
    private MutationMapperUtils mutationMapperUtils;

    @Override
    public List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds,
            List<Integer> entrezGeneIds, List<String> sampleIds) {

        List<String> structuralVariantMolecularProfileIds = new ArrayList<String>();
        List<String> structuralVariantSampleIds = new ArrayList<String>();

        List<String> fusionVariantMolecularProfileIds = new ArrayList<String>();
        List<String> fusionVariantSampleIds = new ArrayList<String>();
        Map<String, String> molecularProfileIdReplaceMap = new HashMap<>();

        // TODO: Remove once fusions are removed from mutation table
        for (int i = 0; i < molecularProfileIds.size(); i++) {
            String molecularProfileId = molecularProfileIds.get(i);
            if (molecularProfileId.endsWith("_structural_variants")) {
                structuralVariantMolecularProfileIds.add(molecularProfileId);
                structuralVariantSampleIds.add(sampleIds.get(i));
            } else if (molecularProfileId.endsWith("_fusion")) {
                String mutationMolecularProfileId = molecularProfileId.replace("_fusion", "_mutations");
                molecularProfileIdReplaceMap.put(mutationMolecularProfileId, molecularProfileId);
                fusionVariantMolecularProfileIds.add(molecularProfileId);
                fusionVariantSampleIds.add(sampleIds.get(i));
            }
        }

        List<StructuralVariant> structuralVariants = new ArrayList<>();
        List<StructuralVariant> variantsFromMutationtable = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(structuralVariantMolecularProfileIds)) {
            structuralVariants = structuralVariantRepository.fetchStructuralVariants(
                    structuralVariantMolecularProfileIds, entrezGeneIds, structuralVariantSampleIds);
        }

        if (CollectionUtils.isNotEmpty(fusionVariantMolecularProfileIds)) {
            variantsFromMutationtable = mutationMapperUtils.mapFusionsToStructuralVariants(
                    mutationRepository.getFusionsInMultipleMolecularProfiles(fusionVariantMolecularProfileIds,
                            fusionVariantSampleIds, entrezGeneIds, "DETAILED", null, null, null, null),
                    molecularProfileIdReplaceMap, CollectionUtils.isEmpty(entrezGeneIds));
        }

        // TODO: Remove once fusions are removed from mutation table

        return Stream.concat(structuralVariants.stream(), variantsFromMutationtable.stream())
                .collect(Collectors.toList());
    }

}
