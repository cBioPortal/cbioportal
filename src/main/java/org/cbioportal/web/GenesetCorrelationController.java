/*
 * Copyright (c) 2016 The Hyve B.V.
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.cbioportal.model.GenesetCorrelation;
import org.cbioportal.service.GenesetCorrelationService;
import org.cbioportal.service.exception.GenesetNotFoundException;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@InternalApi
@RestController()
@RequestMapping("/api")
@Validated
@Tag(name = "Gene Set Correlation", description = " ")
public class GenesetCorrelationController {

    @Autowired
    private GenesetCorrelationService genesetCorrelationService;

    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfileId', T(org.cbioportal.utils.security.AccessLevel).READ)")
    @RequestMapping(value = "/genesets/{genesetId}/expression-correlation/fetch", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get the genes in a gene set that have expression correlated to the gene set scores (calculated using Spearman's correlation)")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = GenesetCorrelation.class))))
    public ResponseEntity<List<GenesetCorrelation>> fetchCorrelatedGenes(
        @Parameter(required = true, description = "Gene set ID, e.g. HINATA_NFKB_MATRIX.")
        @PathVariable String genesetId,
        @Parameter(required = true, description = "Genetic Profile ID e.g. gbm_tcga_gsva_scores")
        @RequestParam String geneticProfileId,
        @Parameter(description = "Correlation threshold (for absolute correlation value, Spearman correlation)")
        @Max(1)
        @Min(0)
        @RequestParam(defaultValue = "0.3") Double correlationThreshold,
        @Parameter(description = "Identifier of pre-defined sample list with samples to query, e.g. brca_tcga_all")
        @RequestParam(required = false) String sampleListId,
        @Parameter(description = "Fill this one if you want to specify a subset of samples:"
                + " sampleIds: custom list of samples or patients to query, e.g. [\"TCGA-A1-A0SD-01\", \"TCGA-A1-A0SE-01\"]")
        @RequestBody(required = false) List<String> sampleIds)
        throws MolecularProfileNotFoundException, SampleListNotFoundException, GenesetNotFoundException {

        if (sampleListId != null && sampleListId.trim().length() > 0) {
            return new ResponseEntity<>(
                    genesetCorrelationService.fetchCorrelatedGenes(genesetId, geneticProfileId, sampleListId, correlationThreshold.doubleValue()),
                    HttpStatus.OK);
        }
        if (sampleIds != null && sampleIds.size() > 0) {
            return new ResponseEntity<>(
                    genesetCorrelationService.fetchCorrelatedGenes(genesetId, geneticProfileId, sampleIds, correlationThreshold.doubleValue()),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<>(
                    genesetCorrelationService.fetchCorrelatedGenes(genesetId, geneticProfileId, correlationThreshold.doubleValue()),
                    HttpStatus.OK);
        }
    }
}
