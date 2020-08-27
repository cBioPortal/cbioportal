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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantCountByGene;
import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.service.util.AlterationEnrichmentUtil;
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
    @Autowired
    private AlterationEnrichmentUtil<StructuralVariantCountByGene> alterationEnrichmentUtil;

    @Override
    public List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds,
            List<Integer> entrezGeneIds, List<String> sampleIds) {

        List<StructuralVariant> structuralVariants = structuralVariantRepository
                .fetchStructuralVariants(molecularProfileIds, entrezGeneIds, sampleIds);

        // TODO: Remove once fusions are removed from mutation table
        
        // Replace any fusion profile id with  mutation profile id
        List<String>  mutationMolecularProfileIds = molecularProfileIds
                .stream()
                .map(molecularProfileId->molecularProfileId.replace("_fusion", "_mutations"))
                .collect(Collectors.toList());

        Map<String, String> molecularProfileIdReplaceMap = new HashMap<>();

        for (int i = 0; i < molecularProfileIds.size(); i++) {
            molecularProfileIdReplaceMap.put(mutationMolecularProfileIds.get(i), molecularProfileIds.get(i));
        }

        List<StructuralVariant> variantsFromMutationtable = mutationMapperUtils.mapFusionsToStructuralVariants(
                mutationRepository.getFusionsInMultipleMolecularProfiles(mutationMolecularProfileIds, sampleIds, entrezGeneIds,
                        "DETAILED", null, null, null, null), molecularProfileIdReplaceMap);
        // TODO: Remove once fusions are removed from mutation table

        return Stream.concat(structuralVariants.stream(), variantsFromMutationtable.stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<StructuralVariantCountByGene> getSampleCountInMultipleMolecularProfiles(
            List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds,
            boolean includeFrequency, boolean includeMissingAlterationsFromGenePanel) {

        List<StructuralVariantCountByGene> countByGenes = structuralVariantRepository
                .getSampleCountInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds);

        // TODO: Remove once fusions are removed from mutation table
        if (CollectionUtils.isEmpty(countByGenes)) {
            molecularProfileIds = molecularProfileIds.stream()
                    .map(molecularProfileId -> molecularProfileId.replace("_fusion", "_mutations"))
                    .collect(Collectors.toList());

            countByGenes = mutationMapperUtils.mapFusionCountsToStructuralVariantCounts(
                    mutationRepository.getSampleCountInMultipleMolecularProfilesForFusions(molecularProfileIds,
                            sampleIds, entrezGeneIds));
        }
        // TODO: Remove once fusions are removed from mutation table

        if (includeFrequency) {
            alterationEnrichmentUtil.includeFrequencyForSamples(molecularProfileIds, sampleIds, countByGenes,
                    includeMissingAlterationsFromGenePanel);
        }
        return countByGenes;
    }

    @Override
    public List<StructuralVariantCountByGene> getPatientCountInMultipleMolecularProfiles(
            List<String> molecularProfileIds, List<String> patientIds, List<Integer> entrezGeneIds,
            boolean includeFrequency, boolean includeMissingAlterationsFromGenePanel) {

        List<StructuralVariantCountByGene> countByGenes = structuralVariantRepository
                .getPatientCountInMultipleMolecularProfiles(molecularProfileIds, patientIds, entrezGeneIds);

        // TODO: Remove once fusions are removed from mutation table
        if (CollectionUtils.isEmpty(countByGenes)) {
            molecularProfileIds = molecularProfileIds.stream()
                    .map(molecularProfileId -> molecularProfileId.replace("_fusion", "_mutations"))
                    .collect(Collectors.toList());

            countByGenes = mutationMapperUtils.mapFusionCountsToStructuralVariantCounts(mutationRepository
                    .getPatientCountInMultipleMolecularProfiles(molecularProfileIds, patientIds, entrezGeneIds));
        }
        // TODO: Remove once fusions are removed from mutation table

        if (includeFrequency) {
            alterationEnrichmentUtil.includeFrequencyForPatients(molecularProfileIds, patientIds, countByGenes,
                    includeMissingAlterationsFromGenePanel);
        }
        return countByGenes;
    }

}
