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

package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructuralVariantFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantQuery;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.cbioportal.persistence.mybatis.util.MolecularProfileCaseIdentifierUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Repository
public class StructuralVariantMyBatisRepository implements StructuralVariantRepository {

    @Autowired
    private StructuralVariantMapper structuralVariantMapper;
    @Autowired
    private MolecularProfileCaseIdentifierUtil molecularProfileCaseIdentifierUtil;

    @Override
    public List<StructuralVariant> fetchStructuralVariants(List<String> molecularProfileIds,
                                                           List<String> sampleIds,
                                                           List<Integer> entrezGeneIds,
                                                           List<StructuralVariantQuery> structuralVariantQueries) {
        if (molecularProfileIds == null || molecularProfileIds.isEmpty()) {
            return new ArrayList<>();
        }
        return molecularProfileCaseIdentifierUtil.getGroupedCasesByMolecularProfileId(molecularProfileIds, sampleIds)
            .entrySet()
            .stream()
            .flatMap(entry ->
                structuralVariantMapper
                    .fetchStructuralVariants(
                        asList(entry.getKey()),
                        new ArrayList<>(entry.getValue()),
                        entrezGeneIds,
                        structuralVariantQueries
                    )
                    .stream()
            )
            .collect(Collectors.toList());
    }

    @Override
    public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(List<String>  molecularProfileIds,
                                                                        List<String> sampleIds,
                                                                        List<GeneFilterQuery> geneQueries) {
        if (geneQueries == null || geneQueries.isEmpty()
            || molecularProfileIds == null || molecularProfileIds.isEmpty()) {
            return new ArrayList<>();
        }
        return structuralVariantMapper.fetchStructuralVariantsByGeneQueries(molecularProfileIds, sampleIds, geneQueries);
    }

    @Override
    public List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(List<String> molecularProfileIds,
                                                                             List<String> sampleIds,
                                                                             List<StructuralVariantFilterQuery> structVarQueries) {
        if (structVarQueries == null || structVarQueries.isEmpty()
            || molecularProfileIds == null || molecularProfileIds.isEmpty()) {
            return new ArrayList<>();
        }
        return structuralVariantMapper.fetchStructuralVariantsByStructVarQueries(molecularProfileIds, sampleIds, structVarQueries);
    }
}
