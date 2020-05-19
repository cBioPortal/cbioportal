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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
        structuralVariants.addAll(mutationMapperUtils.mapFusionsToStructuralVariants(
                mutationRepository.getFusionsInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds,
                        "DETAILED", null, null, null, null)));
        // TODO: Remove once fusions are removed from mutation table

        return structuralVariants;
    }

    @Override
    public List<StructuralVariantCountByGene> getSampleCountInMultipleMolecularProfiles(
            List<String> molecularProfileIds, List<String> sampleIds, List<Integer> entrezGeneIds,
            boolean includeFrequency, boolean includeMissingAlterationsFromGenePanel) {

        List<StructuralVariantCountByGene> countsFromStructuralVariant = structuralVariantRepository
                .getSampleCountInMultipleMolecularProfiles(molecularProfileIds, sampleIds, entrezGeneIds);

        // TODO: Remove once fusions are removed from mutation table
        List<StructuralVariantCountByGene> countsFromMutation = mutationMapperUtils
                .mapFusionCountsToStructuralVariantCounts(
                        mutationRepository.getSampleCountInMultipleMolecularProfilesForFusions(molecularProfileIds,
                                sampleIds, entrezGeneIds));

        List<StructuralVariantCountByGene> countByGenes = new ArrayList<StructuralVariantCountByGene>();
        if (CollectionUtils.isEmpty(countsFromMutation)) {
            countByGenes = countsFromStructuralVariant;
        } else if (CollectionUtils.isEmpty(countsFromStructuralVariant)) {
            countByGenes = countsFromMutation;
        } else {
            // if both the list contains the sample entrez gene id then merge them by add
            // those counts
            countByGenes = Stream
                    .concat(countsFromStructuralVariant.stream(), countsFromMutation.stream())
                    .collect(Collectors.toMap(StructuralVariantCountByGene::getEntrezGeneId, Function.identity(),
                            (count1, count2) -> {
                                count1.setTotalCount(count1.getTotalCount() + count2.getTotalCount());
                                count1.setNumberOfProfiledCases(
                                        count1.getNumberOfProfiledCases() + count2.getNumberOfProfiledCases());
                                count1.setNumberOfAlteredCases(
                                        count1.getNumberOfAlteredCases() + count2.getNumberOfAlteredCases());
                                return count1;
                            }))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

        }
        // TODO: Remove once fusions are removed from mutation table

        if (includeFrequency) {
            alterationEnrichmentUtil.includeFrequencyForSamples(molecularProfileIds, sampleIds, countByGenes,
                    includeMissingAlterationsFromGenePanel);
        }
        return countByGenes;
    }

}
