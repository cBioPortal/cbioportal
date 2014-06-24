/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
 * has been advised of the possibility of such damage.
*/

package org.mskcc.cbio.mutassessor;

/**
 *  Encapsulates a single line from the Mutation Assessor file.
 */
public class MutationAssessorRecord
{
	public static final String NA_STRING = "NA";
	public static final Float NA_FLOAT = null;

	private String key;
	private String impact;
	private Float impactScore;
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

	public Float getImpactScore()
	{
		return impactScore;
	}

	public void setImpactScore(Float impactScore)
	{
		this.impactScore = impactScore;
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
	 * If all fields of the record are null or NA, then returns true. Otherwise
	 * returns false.
	 *
	 * @return  true if all fields are null or NA, false otherwise
	 */
	public boolean hasNoInfo()
	{
		boolean noInfo = false;

		if ((this.impact == null || this.impact.equals(NA_STRING)) &&
		    (this.impactScore == null) &&
			(this.proteinChange == null || this.proteinChange.equals(NA_STRING)) &&
			(this.alignmentLink == null || this.alignmentLink.equals(NA_STRING)) &&
			(this.structureLink == null || this.structureLink.equals(NA_STRING)))
		{
			noInfo = true;
		}

		return noInfo;
	}
}
