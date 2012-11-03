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
		//MutationAssessorRecord record = this.generateRecord();
		//cache.put("", record);

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
