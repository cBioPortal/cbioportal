/*
 * Copyright (c) 2016 Memorial Sloan Kettering Cancer Center.
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
package org.cbioportal.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.cbioportal.model.GenesetAlteration;
import org.cbioportal.model.GenesetHierarchyInfo;
import org.cbioportal.persistence.GenesetHierarchyRepository;
import org.cbioportal.persistence.GeneticDataRepository;
import org.cbioportal.service.GenesetHierarchyService;
import org.cbioportal.service.GeneticProfileService;
import org.cbioportal.service.exception.GeneticProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenesetHierarchyServiceImpl implements GenesetHierarchyService {

    @Autowired
    private GeneticDataRepository geneticDataRepository;
    @Autowired
    private GeneticProfileService geneticProfileService;
    @Autowired
    private GenesetHierarchyRepository genesetHierarchyRepository;
    
	@Override
	public List<GenesetHierarchyInfo> getGenesetHierarchyInfo(String geneticProfileId) throws GeneticProfileNotFoundException {
		
		//validate 
		geneticProfileService.getGeneticProfile(geneticProfileId);
		//TODO could also validate if profile is of geneset_score type
		
		//get list of genesets that have data for this profile:
		List<GenesetAlteration> genesets = geneticDataRepository.getGenesetAlterations(geneticProfileId, null, "SUMMARY");
		
		return genesetHierarchyRepository.getGenesetHierarchyItems(genesets);
	}


}
