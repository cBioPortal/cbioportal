/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.portal.mut_diagram;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoPdbUniprotResidueMapping;
import org.mskcc.cbio.portal.model.GeneWithScore;
import org.mskcc.cbio.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.cbio.portal.mut_diagram.impl.CgdsIdMappingService;
import org.mskcc.cbio.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

/**
 * @author Selcuk Onur Sumer
 */
public class Mutation3dProcessor
{
	private ObjectMapper objectMapper;
	private FeatureService featureService;
	private IdMappingService idMappingService;

	public Mutation3dProcessor()
	{
		objectMapper = new ObjectMapper();
		objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

		PfamGraphicsCacheLoader cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
		featureService = new CacheFeatureService(cacheLoader);

		idMappingService = new CgdsIdMappingService(DaoGeneOptimized.getInstance());
	}

	public String process(GeneWithScore geneWithScore)
	{
		// final object to be send as JSON
		JSONObject jsonObject = new JSONObject();
		String pdbId = null;
		String geneSymbol = geneWithScore.getGene().toUpperCase();

		try
		{
			// find out the first uniprot id
			List<String> ids = idMappingService.getUniProtIds(geneSymbol);
			String uniprotId = ids.get(0);
			String label = null;

			List<Sequence> sequences = featureService.getFeatures(uniprotId);

			if (!sequences.isEmpty()) {
				Sequence sequence = sequences.get(0);

				if (sequence.getMetadata() != null) {
					label = (String)sequence.getMetadata().get("identifier");
				}
			}

			Map<String, Set<String>> pdbChainMap =
					DaoPdbUniprotResidueMapping.mapToPdbChains(label);

			// TODO getting only the first pdb id
			if (pdbChainMap.keySet().iterator().hasNext())
			{
				pdbId = pdbChainMap.keySet().iterator().next();
			}
		}
		catch (DaoException e)
		{
			e.printStackTrace();
		}

		// TODO using mutation locations also get locations on the chain
		// see DaoPdbUniprotResidueMapping.mapToPdbChains() -> need to implement this first

		jsonObject.put("hugoGeneSymbol", geneSymbol);
		jsonObject.put("pdbId", pdbId);

		return JSONValue.toJSONString(jsonObject);
	}
}
