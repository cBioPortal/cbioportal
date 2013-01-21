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

	public int put(OncotatorRecord record) throws OncotatorCacheException
	{
		if (this.cache.get(record.getKey()) != null)
		{
			// simulate insertion error
			throw new OncotatorCacheException("duplicate entry for " + record.getKey());
		}
		else
		{
			this.cache.put(record.getKey(), record);
		}

		return 1;
	}

	public OncotatorRecord get(String key) throws OncotatorCacheException
	{
		return this.cache.get(key);
	}

	protected HashMap<String, OncotatorRecord> initCache()
	{
		HashMap<String, OncotatorRecord> cache =
				new HashMap<String, OncotatorRecord>();

		OncotatorRecord record = this.generateRecord("11_56258437_56258437_T_C",
		                                             "NA",
		                                             "NA",
		                                             "NA",
		                                             "Missense_Mutation",
		                                             "p.K137R",
		                                             "OR5M8",
		                                             "NM_001005282",
		                                             "NP_001005282",
		                                             "OR5M8_HUMAN",
		                                             "Q8NGP6",
		                                             "c.(409-411)AAG>AGG",
		                                             "c.410A>G",
		                                             1,
		                                             "g.chr11:56258437T>C");

		cache.put("11_56258437_56258437_T_C", record);

		// silent mutation, no need to populate
		record = this.generateRecord("4_83788017_83788017_G_A", "NA", "NA", "NA", "NA", "NA",
		                              "NA", "NA", "NA", "NA", "NA", "NA", "NA", -1, "NA");
		cache.put("4_83788017_83788017_G_A", record);

		record = this.generateRecord("1_906209_906209_G_A",
		                             "NA",
		                             "NA",
		                             "NA",
		                             "Nonsense_Mutation",
		                             "p.W185*",
		                             "PLEKHN1",
		                             "NM_032129",
		                             "NP_115505",
		                             "PKHN1_HUMAN",
		                             "Q494U1",
		                             "c.(553-555)TGG>TGA",
		                             "c.555G>A",
		                             5,
		                             "g.chr1:906209G>A");

		cache.put("1_906209_906209_G_A", record);

		record = this.generateRecord("1_3411011_3411043_GTGGCAGGAGCACTCCAGATGGCAGGCGGCTCC_-",
		                             "NA",
		                             "rs78303815;rs61730954",
		                             "by1000genomes",
		                             "In_Frame_Del",
		                             "p.GAACHLECSCH1341del",
		                             "MEGF6",
		                             "NM_001409",
		                             "NP_001400",
		                             "MEGF6_HUMAN",
		                             "O75095",
		                             "c.(4021-4053)GGAGCCGCCTGCCATCTGGAGTGCTCCTGCCACdel",
		                             "c.4021_4053delGGAGCCGCCTGCCATCTGGAGTGCTCCTGCCAC",
		                             32,
		                             "g.chr1:3411011_3411043delGTGGCAGGAGCACTCCAGATGGCAG");

		cache.put("1_3411011_3411043_GTGGCAGGAGCACTCCAGATGGCAGGCGGCTCC_-", record);

		return cache;
	}

	protected OncotatorRecord generateRecord(String key,
			String cosmic,
			String dbSnpRs,
			String dbSnpValStatus,
			String type,
			String proteinChange,
			String geneSymbol,
			String refseqMrnaId,
			String refseqProtId,
			String uniprotEntry,
			String uniprotAccession,
			String codonChange,
			String transcriptChange,
			int exonAffected,
			String genomeChange)
	{
		OncotatorRecord record = new OncotatorRecord(key);

		record.setCosmicOverlappingMutations(cosmic);
		record.setDbSnpRs(dbSnpRs);
		record.setDbSnpValStatus(dbSnpValStatus);
		record.setGenomeChange(genomeChange);
		record.getBestCanonicalTranscript().setProteinChange(proteinChange);
		record.getBestEffectTranscript().setProteinChange(proteinChange);
		record.getBestCanonicalTranscript().setVariantClassification(type);
		record.getBestEffectTranscript().setVariantClassification(type);
		record.getBestCanonicalTranscript().setGene(geneSymbol);
		record.getBestEffectTranscript().setGene(geneSymbol);
		record.getBestCanonicalTranscript().setExonAffected(exonAffected);
		record.getBestEffectTranscript().setExonAffected(exonAffected);
		record.getBestCanonicalTranscript().setRefseqMrnaId(refseqMrnaId);
		record.getBestEffectTranscript().setRefseqMrnaId(refseqMrnaId);
		record.getBestCanonicalTranscript().setRefseqProtId(refseqProtId);
		record.getBestEffectTranscript().setRefseqProtId(refseqProtId);
		record.getBestCanonicalTranscript().setUniprotName(uniprotEntry);
		record.getBestEffectTranscript().setUniprotName(uniprotEntry);
		record.getBestCanonicalTranscript().setUniprotAccession(uniprotAccession);
		record.getBestEffectTranscript().setUniprotAccession(uniprotAccession);
		record.getBestCanonicalTranscript().setCodonChange(codonChange);
		record.getBestEffectTranscript().setCodonChange(codonChange);
		record.getBestCanonicalTranscript().setTranscriptChange(transcriptChange);
		record.getBestEffectTranscript().setTranscriptChange(transcriptChange);

		return record;
	}
}
