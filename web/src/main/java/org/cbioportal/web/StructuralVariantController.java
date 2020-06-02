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

import org.cbioportal.model.StructuralVariant;
import org.cbioportal.service.StructuralVariantService;
import org.cbioportal.web.parameter.StructuralVariantFilter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.cbioportal.web.parameter.SampleMolecularIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.*;

@PublicApi
@RestController
@Validated
@Api(tags = PublicApiTags.STRUCTURAL_VARIANTS, description = " ")
public class StructuralVariantController {
    @Autowired
    private StructuralVariantService structuralVariantService;

    @PreAuthorize("hasPermission(#involvedCancerStudies, 'Collection<CancerStudyId>', 'read')")
    @RequestMapping(value = "/structuralvariant/fetch", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch structural variants for entrezGeneIds and molecularProfileIds or sampleMolecularIdentifiers")
    public ResponseEntity<List<StructuralVariant>> fetchStructuralVariants(
            @ApiIgnore // prevent reference to this attribute in the swagger-ui interface
            @RequestAttribute(required = false, value = "involvedCancerStudies") Collection<String> involvedCancerStudies,
            @Valid @RequestAttribute(required = false, value = "interceptedStructuralVariantFilter") StructuralVariantFilter interceptedStructuralVariantFilter,
            @ApiParam(required = true, value = "List of entrezGeneIds and molecularProfileIds or sampleMolecularIdentifiers")
            @Valid @RequestBody(required = false) StructuralVariantFilter structuralVariantFilter) {

        List<StructuralVariant> structuralVariantList;

        if (interceptedStructuralVariantFilter.getSampleMolecularIdentifiers() != null) {
            List<SampleMolecularIdentifier> sampleMolecularIdentifiers = interceptedStructuralVariantFilter.getSampleMolecularIdentifiers();
            List<String> molecularProfileIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();

            for (SampleMolecularIdentifier sampleMolecularIdentifier : sampleMolecularIdentifiers) {
                molecularProfileIds.add(sampleMolecularIdentifier.getMolecularProfileId());
                sampleIds.add(sampleMolecularIdentifier.getSampleId());
            }
            structuralVariantList = structuralVariantService.fetchStructuralVariants(molecularProfileIds, interceptedStructuralVariantFilter.getEntrezGeneIds(), sampleIds);

        } else {
            List<String> sampleIds = new ArrayList<>();
            structuralVariantList = structuralVariantService.fetchStructuralVariants(interceptedStructuralVariantFilter.getMolecularProfileIds(), interceptedStructuralVariantFilter.getEntrezGeneIds(), sampleIds);
        }

        return new ResponseEntity<>(structuralVariantList, HttpStatus.OK);

    }
}
