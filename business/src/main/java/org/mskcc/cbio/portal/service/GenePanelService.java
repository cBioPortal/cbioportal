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

package org.mskcc.cbio.portal.service;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.persistence.GenePanelMapper;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author grossb
 */
@Service
public class GenePanelService
{
	@Autowired
	private GenePanelMapper genePanelMapper;
	@Autowired
    private GeneService geneService;
	@Autowired
    private	StudyService studyService;

	@Transactional
	public GenePanel insertGenePanel(String stableId, String description, String cancerStudyId, List<String> geneSymbols)
	{
		DBStudy study = getStudy(cancerStudyId);
		List<Long> entrezGeneIds = getGeneIds(geneSymbols);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("internal_id", 0);
		map.put("stable_id", stableId);
		map.put("description", description);
		map.put("cancer_study_id", study.internal_id);
		genePanelMapper.insertGenePanel(map);
		
		int internalId = (Integer)map.get("internal_id");

		map = new HashMap<String, Object>();
		map.put("internal_id", internalId);
		map.put("gene_list", entrezGeneIds);
		genePanelMapper.insertGenePanelList(map);

		GenePanel genePanel = new GenePanel();
		genePanel.internalId = internalId;
		genePanel.stableId = stableId;
		genePanel.description = description;
		genePanel.cancerStudyId = cancerStudyId;
		genePanel.geneList = geneSymbols;

		return genePanel;
	}

	private DBStudy getStudy(String cancerStudyId)
	{
		String[] studies = {cancerStudyId};
		List<DBStudy> study = studyService.byStableId(new ArrayList<String>(Arrays.asList(studies)));
		assert study.size() == 1;
		return study.iterator().next();	
	}

	private DBStudy getStudy(int cancerStudyId)
	{
		Integer[] studies = {cancerStudyId};
		List<DBStudy> study = studyService.byInternalId(new ArrayList<Integer>(Arrays.asList(studies)));
		assert study.size() == 1;
		return study.iterator().next();	
	}

	private List<Long> getGeneIds(List<String> geneSymbols)
	{
		List<Long> genes = new ArrayList<Long>();
		for (DBGene gene : geneService.byHugoGeneSymbol(geneSymbols)) {
			genes.add(gene.entrezGeneId);
		}
		assert !genes.isEmpty();
		return genes;
	}

	private List<String> getGeneSymbols(List<Long> entrezGeneIds)
	{
		List<String> genes = new ArrayList<String>();
		for (DBGene gene : geneService.byEntrezGeneId(entrezGeneIds)) {
			genes.add(gene.hugoGeneSymbol);
		}
		assert !genes.isEmpty();
		return genes;
	}	

	public GenePanel getByStableId(String stableId)
	{
		GenePanel gp = 	genePanelMapper.getPanelByStableId(stableId);
		assert gp != null;
		DBStudy study = getStudy(new Integer(gp.cancerStudyId));
		gp.cancerStudyId = study.id;
		List<Long> entrezIds = genePanelMapper.getListByInternalId(gp.internalId);
		assert !entrezIds.isEmpty();
		gp.geneList = getGeneSymbols(entrezIds);  
		return gp;
	}

	public void deleteGenePanel(String stableId)
	{
		GenePanel gp = genePanelMapper.getPanelByStableId(stableId);
		assert gp != null;
		genePanelMapper.deleteGenePanel(stableId);
		genePanelMapper.deleteGenePanelList(gp.internalId);
	}

	public void deleteAllGenePanel()
	{
		genePanelMapper.deleteAllGenePanel();
		genePanelMapper.deleteAllGenePanelList();
	}
}
