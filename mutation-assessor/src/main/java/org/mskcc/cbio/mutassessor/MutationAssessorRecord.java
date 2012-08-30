package org.mskcc.cbio.mutassessor;

/**
 *  Encapsulates a single line from the Mutation Assessor file.
 */
public class MutationAssessorRecord
{
	public static final String NA_STRING = "NA";

	private String key;
	private String impact;
	private String proteinChange;
	private String structureLink;
	private String alignmentLink;

	public MutationAssessorRecord(String key)
	{
		this.key = key;
	}

	public String getKey()
	{
		return key;
	}

	public String getImpact()
	{
		return impact;
	}

	public void setImpact(String impact)
	{
		this.impact = impact;
	}

	public String getProteinChange()
	{
		return proteinChange;
	}

	public void setProteinChange(String proteinChange)
	{
		this.proteinChange = proteinChange;
	}

	public String getStructureLink()
	{
		return structureLink;
	}

	public void setStructureLink(String structureLink)
	{
		this.structureLink = structureLink;
	}

	public String getAlignmentLink()
	{
		return alignmentLink;
	}

	public void setAlignmentLink(String alignmentLink)
	{
		this.alignmentLink = alignmentLink;
	}

	/**
	 * If all fields of the record are null, then returns true. Otherwise
	 * returns false.
	 *
	 * @return  true if all fields are null, false otherwise
	 */
	public boolean hasNoInfo()
	{
		boolean noInfo = false;

		if (this.impact == null &&
			this.proteinChange == null &&
			this.alignmentLink == null &&
			this.structureLink == null)
		{
			noInfo = true;
		}

		return noInfo;
	}
}
