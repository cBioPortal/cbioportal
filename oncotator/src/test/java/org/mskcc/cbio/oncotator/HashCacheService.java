/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.oncotator;

import java.util.HashMap;

/**
 * Oncotator cache service built on a hash map for testing purposes.
 */
public class HashCacheService implements OncotatorCacheService
{
	protected HashMap<String, OncotatorRecord> cache;

	public HashCacheService()
	{
		this.cache = this.initCache();
	}

	public int put(OncotatorRecord record) throws Exception
	{
		if (this.cache.get(record.getKey()) != null)
		{
			// simulate insertion error
			throw new Exception("duplicate entry for " + record.getKey());
		}
		else
		{
			this.cache.put(record.getKey(), record);
		}

		return 1;
	}

	public OncotatorRecord get(String key) throws Exception
	{
		return this.cache.get(key);
	}

	protected HashMap<String, OncotatorRecord> initCache()
	{
		HashMap<String, OncotatorRecord> cache =
				new HashMap<String, OncotatorRecord>();

		OncotatorRecord record = this.generateRecord("11_56258437_56258437_T_C", "OR5M8",
			 "g.chr11:56258437T>C", "p.K137R", "Missense_Mutation", 1, "NA", "NA");
		cache.put("11_56258437_56258437_T_C", record);

		record = this.generateRecord("4_83788017_83788017_G_A", "NA",
		                             "NA", "NA", "NA", -1, "NA", "NA");
		cache.put("4_83788017_83788017_G_A", record);

		record = this.generateRecord("1_906209_906209_G_A", "PLEKHN1",
			"g.chr1:906209G>A", "p.W185*", "Nonsense_Mutation", 5, "NA", "NA");
		cache.put("1_906209_906209_G_A", record);

		record = this.generateRecord("1_3411011_3411043_GTGGCAGGAGCACTCCAGATGGCAGGCGGCTCC_-",
			"MEGF6", "g.chr1:3411011_3411043delGTGGCAGGAGCACTCCAGATGGCAG",
			"p.GAACHLECSCH1341del", "In_Frame_Del",	32,	"NA", "rs78303815;rs61730954");
		cache.put("1_3411011_3411043_GTGGCAGGAGCACTCCAGATGGCAGGCGGCTCC_-", record);

		return cache;
	}

	protected OncotatorRecord generateRecord(String key,
			String geneSymbol,
			String genomeChange,
			String proteinChange,
			String type,
			int exonAffected,
			String cosmic,
			String dbSnpRs)
	{
		OncotatorRecord record = new OncotatorRecord(key);

		record.setCosmicOverlappingMutations(cosmic);
		record.setDbSnpRs(dbSnpRs);
		record.setGenomeChange(genomeChange);
		record.getBestCanonicalTranscript().setProteinChange(proteinChange);
		record.getBestEffectTranscript().setProteinChange(proteinChange);
		record.getBestCanonicalTranscript().setVariantClassification(type);
		record.getBestEffectTranscript().setVariantClassification(type);
		record.getBestCanonicalTranscript().setGene(geneSymbol);
		record.getBestEffectTranscript().setGene(geneSymbol);
		record.getBestCanonicalTranscript().setExonAffected(exonAffected);
		record.getBestEffectTranscript().setExonAffected(exonAffected);

		return record;
	}
}
