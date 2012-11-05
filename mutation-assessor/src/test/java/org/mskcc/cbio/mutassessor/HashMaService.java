package org.mskcc.cbio.mutassessor;

import java.util.HashMap;

/**
 *
 */
public class HashMaService extends MutationAssessorService
{
	protected HashMap<String, MutationAssessorRecord> cache;

	public HashMaService()
	{
		this.cache = this.initCache();
	}

	public MutationAssessorRecord getMaRecord(String key) throws Exception
	{
		return this.cache.get(key);
	}

	protected HashMap<String, MutationAssessorRecord> initCache()
	{
		HashMap<String, MutationAssessorRecord> cache =
				new HashMap<String, MutationAssessorRecord>();

		// TODO load with some values...
		MutationAssessorRecord record = this.generateRecord(
			"11_56258437_56258437_T_C",
			"neutral", "K137R", "NA",
			"getma.org/?cm=msa&ty=f&p=OR5M8_HUMAN&rb=1&re=137&var=K137R");

		cache.put("11_56258437_56258437_T_C", record);

		record = this.generateRecord("10_100015355_100015355_C_T",
			"medium", "A524T",
		    "getma.org/pdb.php?prot=LOXL4_HUMAN&from=424&to=529&var=A524T",
		    "getma.org/?cm=msa&ty=f&p=LOXL4_HUMAN&rb=424&re=529&var=A524T");

		cache.put("10_100015355_100015355_C_T", record);

		record = this.generateRecord("3_41266137_41266137_C_T",
			"medium", "S45F", "NA",
			"getma.org/?cm=msa&ty=f&p=CTNB1_HUMAN&rb=1&re=200&var=S45F");

		cache.put("3_41266137_41266137_C_T", record);

		return cache;
	}

	protected MutationAssessorRecord generateRecord(String key,
		String impact,
		String proteinChange,
		String pdbLink,
		String msaLink)
	{
		MutationAssessorRecord record = new MutationAssessorRecord(key);

		record.setImpact(impact);
		record.setProteinChange(proteinChange);
		record.setStructureLink(pdbLink);
		record.setAlignmentLink(msaLink);

		return record;
	}
}
