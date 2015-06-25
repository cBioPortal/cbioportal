/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.web;

import java.util.LinkedList;
import org.mskcc.cbio.portal.model.*;
;

import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import org.mskcc.cbio.portal.service.CancerStudyAlterationFrequencyService;

@Controller
public class CancerStudyAlterationFrequencyController
{
	@Autowired
	private CancerStudyAlterationFrequencyService alterationFrequencyService;
@RequestMapping("/alteration_frequencies")	
  public @ResponseBody List<CancerStudyAlterationFrequency> getAlterationFrequencies(@RequestParam(required=true) List<Long> entrez_gene_ids,
											@RequestParam(required=true) List<Integer> internal_study_ids,
											@RequestParam(required=false) List<String> data_type,
											@RequestParam(required=false) List<Integer> positions,
											@RequestParam(required=false) List<String> mutation_types) throws Exception {
	  if (data_type == null) {
		  data_type = new LinkedList<>();
		  data_type.add("MUT");
		  data_type.add("CNA");
	  }
	  if (data_type.contains("MUT") && data_type.contains("CNA")) {
		return alterationFrequencyService.getMutCna(entrez_gene_ids, internal_study_ids);
	  } else if (data_type.contains("MUT")) {
		  if (positions != null && mutation_types != null) {
			return alterationFrequencyService.getMutByPositionAndType(entrez_gene_ids.get(0), internal_study_ids.get(0), positions.get(0), mutation_types);
		  } else if (positions != null) {
			return alterationFrequencyService.getMutByPosition(entrez_gene_ids.get(0), internal_study_ids.get(0), positions);
		  } else if (mutation_types != null) {
			return alterationFrequencyService.getMutByType(entrez_gene_ids.get(0), internal_study_ids.get(0), mutation_types);
		  } else {
			return alterationFrequencyService.getMut(entrez_gene_ids, internal_study_ids);
		  }
	  } else {
		  return alterationFrequencyService.getCna(entrez_gene_ids, internal_study_ids);
	  }
  }
  
  @RequestMapping("/alteration_frequencies/mutation_types")
  public @ResponseBody List<String> getMutationTypes() {
	  return alterationFrequencyService.getMutationTypes();
  }
}