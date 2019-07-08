/*
 * Copyright (c) 2019 The Hyve B.V.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
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

import java.util.List;

import org.cbioportal.model.TreatmentMolecularData;
import org.cbioportal.service.TreatmentDataService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
import org.cbioportal.service.exception.SampleListNotFoundException;
import org.cbioportal.web.config.annotation.InternalApi;
import org.cbioportal.web.parameter.TreatmentDataFilterCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@InternalApi
@RestController
@Validated
@Api(tags = "Treatment response values", description = " ")
public class TreatmentDataController {


	@Autowired
    private TreatmentDataService treatmentDataService;

    @PreAuthorize("hasPermission(#geneticProfileId, 'GeneticProfile', 'read')")    
    @RequestMapping(value = "/genetic-profiles/{geneticProfileId}/treatment-genetic-data/fetch", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Fetch treatment \"genetic data\" items (treatment response values) by profile Id, treatment Ids and sample Ids")
    public ResponseEntity<List<TreatmentMolecularData>> fetchTreatmentGeneticDataItems(
            @ApiParam(required = true, value = "Genetic profile ID, e.g. study_es_0_treatment_ic50")
            @PathVariable String geneticProfileId,
            @ApiParam(required = true, value = "Search parameters to return the values for a given set of samples and treatment items. "
            		+ "treatmentIds: The list of identifiers (STABLE_ID) for the treatments of interest, e.g. 17-AAG. "
            		+ "Use one of these if you want to specify a subset of samples:"
            		+ "(1) sampleListId: Identifier of pre-defined sample list with samples to query, e.g. brca_tcga_all " 
            		+ "or (2) sampleIds: custom list of samples or patients to query, e.g. TCGA-BH-A1EO-01, TCGA-AR-A1AR-01")
            @RequestBody TreatmentDataFilterCriteria treatmentDataFilterCriteria) throws MolecularProfileNotFoundException, SampleListNotFoundException {

				if (treatmentDataFilterCriteria.getSampleListId() != null && treatmentDataFilterCriteria.getSampleListId().trim().length() > 0) {
					return new ResponseEntity<>(
							treatmentDataService.fetchTreatmentData(geneticProfileId, treatmentDataFilterCriteria.getSampleListId(), 
									treatmentDataFilterCriteria.getTreatmentIds()), HttpStatus.OK);
				} else {
					return new ResponseEntity<>(
							treatmentDataService.fetchTreatmentData(geneticProfileId, treatmentDataFilterCriteria.getSampleIds(), 
									treatmentDataFilterCriteria.getTreatmentIds()), HttpStatus.OK);
				}
			}

}