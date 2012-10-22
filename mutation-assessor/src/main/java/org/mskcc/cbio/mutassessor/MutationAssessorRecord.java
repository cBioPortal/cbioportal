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
	 * If all fields of the record are null or NA, then returns true. Otherwise
	 * returns false.
	 *
	 * @return  true if all fields are null or NA, false otherwise
	 */
	public boolean hasNoInfo()
	{
		boolean noInfo = false;

		if ((this.impact == null || this.impact.equals(NA_STRING)) &&
			(this.proteinChange == null || this.proteinChange.equals(NA_STRING)) &&
			(this.alignmentLink == null || this.alignmentLink.equals(NA_STRING)) &&
			(this.structureLink == null || this.structureLink.equals(NA_STRING)))
		{
			noInfo = true;
		}

		return noInfo;
	}
}
