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

package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.cbioportal.model.StructuralVariant;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.web.config.InternalApiTags;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.StructuralVariantFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = InternalApiTags.STRUCTURAL_VARIANTS, description = " ")
public class StructuralVariantController {
    @Autowired
    private StructuralVariantService structuralVariantService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/structural-variant/fetch", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Fetch structural variants for entrezGeneIds and molecularProfileIds or sampleMolecularIdentifiers")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = StructuralVariant.class))))
    public ResponseEntity<List<StructuralVariant>> fetchStructuralVariants(
            @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @Parameter(hidden = true) // prevent reference to this attribute in the swagger-ui interface
            @Valid @RequestAttribute(required = false, value = "interceptedStructuralVariantFilter") StructuralVariantFilter interceptedStructuralVariantFilter,
            @Parameter(required = true, description = "List of entrezGeneIds, structural variant queries and molecularProfileIds or sampleMolecularIdentifiers")
            @Valid @RequestBody(required = false) StructuralVariantFilter structuralVariantFilter) {
        
        List<String> molecularProfileIds = new ArrayList<>();
        List<String> sampleIds = new ArrayList<>();

        if (interceptedStructuralVariantFilter.getSampleMolecularIdentifiers() != null) {
            interceptedStructuralVariantFilter
                .getSampleMolecularIdentifiers()
                .forEach(sampleMolecularIdentifier -> {
                    sampleIds.add(sampleMolecularIdentifier.getSampleId());
                    molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
                    
                });
        } else {
            molecularProfileIds.addAll(interceptedStructuralVariantFilter.getMolecularProfileIds());
        }
        List<StructuralVariant> structuralVariantList = structuralVariantService.fetchStructuralVariants(
            molecularProfileIds,
            sampleIds,
            interceptedStructuralVariantFilter.getEntrezGeneIds(),
            interceptedStructuralVariantFilter.getStructuralVariantQueries()
        );

        return new ResponseEntity<>(structuralVariantList, HttpStatus.OK);

    }
}
