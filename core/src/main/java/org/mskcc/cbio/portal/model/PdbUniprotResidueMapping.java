/** Copyright (c) 2013 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.model;

/**
 * Class designed to represent a single row in the pdb_uniprot_residue_mapping table.
 *
 * @author Selcuk Onur Sumer
 */
public class PdbUniprotResidueMapping
{
	private Integer alignmentId;
	private Integer pdbPos;
	private String pdbInsertionCode;
	private Integer uniprotPos;
	private String match;

	public PdbUniprotResidueMapping(Integer alignmentId,
			Integer pdbPos,
			String pdbInsertionCode,
			Integer uniprotPos,
			String match)
	{
		this.alignmentId = alignmentId;
		this.pdbPos = pdbPos;
                this.pdbInsertionCode = pdbInsertionCode;
		this.uniprotPos = uniprotPos;
		this.match = match;
	}

	public Integer getAlignmentId()
	{
		return alignmentId;
	}

	public void setAlignmentId(Integer alignmentId)
	{
		this.alignmentId = alignmentId;
	}

	public Integer getPdbPos()
	{
		return pdbPos;
	}

	public void setPdbPos(Integer pdbPos)
	{
		this.pdbPos = pdbPos;
	}

	public Integer getUniprotPos()
	{
		return uniprotPos;
	}

	public void setUniprotPos(Integer uniprotPos)
	{
		this.uniprotPos = uniprotPos;
	}

	public String getMatch()
	{
		return match;
	}

	public void setMatch(String match)
	{
		this.match = match;
	}

        public String getPdbInsertionCode() {
            return pdbInsertionCode;
        }

        public void setPdbInsertionCode(String pdbInsertionCode) {
            this.pdbInsertionCode = pdbInsertionCode;
        }
        
        public String getJmolPdbResidue() {
            return pdbPos.toString() + (pdbInsertionCode==null?"":("^"+pdbInsertionCode));
        }
}
