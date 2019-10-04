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

package org.cbioportal.weblegacy;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.List;
import org.mskcc.cbio.portal.model.MutationalSignature;
import org.mskcc.cbio.portal.service.MutationalSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class MutationalSignatureController {

    @Autowired
    private MutationalSignatureService mutationalSignatureService;

    @ApiOperation(value = "Get mutation signatures for given samples in a study gene panel information",
            nickname = "getMutationalSignatures",
            notes = "")
    @Transactional
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/mutational-signature")
    public ResponseEntity<List<MutationalSignature>> getMutationalSignatures(@ApiParam(required = true, value = "Study id")
            @RequestParam(required = true)
            String study_id,
            @ApiParam(required = false, value="List of sample ids. If provided, will return mutational signatures for the given samples. /"
            + "Otherwise, will return mutational signatures for all samples in the study.")
            @RequestParam(required = false) List<String> sample_ids) {
            List<MutationalSignature> result;
        if (sample_ids != null) {
            result = mutationalSignatureService.getMutationalSignaturesBySampleIds(study_id, sample_ids);
        } else {
            result = mutationalSignatureService.getMutationalSignatures(study_id);
        }
        return new ResponseEntity(result, HttpStatus.OK);
    }
}
