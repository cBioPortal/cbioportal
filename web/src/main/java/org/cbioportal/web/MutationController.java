/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import org.cbioportal.model.Mutation;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "Mutations", description = " ")
public class MutationController {

    @RequestMapping(value = "/studies/{studyId}/samples/{sampleId}/mutations", method = RequestMethod.GET)
    @ApiOperation("Get all mutations in a sample in a study")
    public ResponseEntity<List<Mutation>> getAllMutationsInSampleInStudy(@PathVariable String studyId,
                                                                                          @PathVariable String sampleId,
                                                                                          @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                          @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}/mutations", method = RequestMethod.GET)
    @ApiOperation("Get all mutations in a patient in a study")
    public ResponseEntity<List<Mutation>> getAllMutationsInPatientInStudy(@PathVariable String studyId,
                                                                                           @PathVariable String patientId,
                                                                                           @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                           @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/mutations", method = RequestMethod.GET)
    @ApiOperation("Get all mutations in a study")
    public ResponseEntity<List<Mutation>> getAllMutationsInStudy(@PathVariable String studyId,
                                                                                  @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                  @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                  @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {
        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/mutations/query", method = RequestMethod.POST)
    @ApiOperation("Query mutations by example")
    public ResponseEntity<List<Mutation>> queryMutationsByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                   @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                   @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                   @RequestBody Mutation exampleMutation) {
        throw new UnsupportedOperationException();
    }
}
