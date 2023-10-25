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

import org.cbioportal.model.GeneFilterQuery;
import org.cbioportal.model.StructuralVariantFilterQuery;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.model.StructuralVariantQuery;
import org.cbioportal.persistence.StructuralVariantRepository;
import org.cbioportal.service.StructuralVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.springframework.util.CollectionUtils;

@Service
public class StructuralVariantServiceImpl implements StructuralVariantService {

    @Autowired
    private StructuralVariantRepository structuralVariantRepository;

    @Override
    public List<StructuralVariant> fetchStructuralVariants(
        List<String> molecularProfileIds,
        List<String> sampleIds,
        List<Integer> entrezGeneIds,
        List<StructuralVariantQuery> structuralVariantQueries
    ) {
        return structuralVariantRepository.fetchStructuralVariants(
            molecularProfileIds, sampleIds, entrezGeneIds, structuralVariantQueries
        );
    }

    @Override
    public List<StructuralVariant> fetchStructuralVariantsByGeneQueries(List<String> molecularProfileIds,
                                                                        List<String> sampleIds,
                                                                        List<GeneFilterQuery> geneQueries) {

        return structuralVariantRepository.fetchStructuralVariantsByGeneQueries(
            molecularProfileIds, sampleIds, geneQueries);
    }

    @Override
    public List<StructuralVariant> fetchStructuralVariantsByStructVarQueries(List<String> molecularProfileIds,
                                                                             List<String> sampleIds,
                                                                             List<StructuralVariantFilterQuery> structVarQueries) {
        return structuralVariantRepository.fetchStructuralVariantsByStructVarQueries(
            molecularProfileIds, sampleIds, structVarQueries);
    }

}
