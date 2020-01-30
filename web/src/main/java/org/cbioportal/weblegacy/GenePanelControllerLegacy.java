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
import org.mskcc.cbio.portal.model.GenePanel;
import org.mskcc.cbio.portal.model.GenePanelWithSamples;
import org.mskcc.cbio.portal.service.GenePanelServiceLegacy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author heinsz
 */

@RestController
public class GenePanelControllerLegacy {
    @Autowired
    private GenePanelServiceLegacy genePanelServiceLegacy;

    @ApiOperation(
        value = "Get gene panel information",
        nickname = "getGenePanel",
        notes = ""
    )
    @Transactional
    @RequestMapping(method = RequestMethod.GET, value = "/genepanel")
    public List<GenePanel> getGenePanel(
        @ApiParam(
            required = false,
            value = "gene panel id. If provided, the list of /" +
            "genes associated with the gene panel will be presented. Otherwise, only the stable id and description will /" +
            "be shown for all gene panels in the database."
        ) @RequestParam(required = false) String panel_id
    ) {
        if (panel_id != null) {
            return genePanelServiceLegacy.getGenePanelByStableId(panel_id);
        } else {
            return genePanelServiceLegacy.getGenePanels();
        }
    }

    @ApiOperation(
        value = "Get gene panel information for a profile and set of genes. Will return a mapping of samples and genes from the query that are in profile",
        nickname = "getGenePanelData",
        notes = ""
    )
    @Transactional
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/genepanel/data",
        produces = "application/json"
    )
    public List<GenePanelWithSamples> getGenePanelData(
        @ApiParam(
            required = true,
            value = "genetic profile id, such as those returned by /api/geneticprofiles"
        ) @RequestParam(required = true) String profile_id,
        @ApiParam(
            required = true,
            value = "List of gene hugo symbols"
        ) @RequestParam(required = true) List<String> genes
    ) {
        return genePanelServiceLegacy.getGenePanelDataByProfileAndGenes(
            profile_id,
            genes
        );
    }
}
