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
 * Class designed to represent a single row in the pdb_uniprot_alignment table.
 *
 * @author Selcuk Onur Sumer
 */
public class PdbUniprotAlignment
{
	private Integer alignmentId;
	private String pdbId;
	private String chain;
	private String uniprotId;
	private String pdbFrom;
	private String pdbTo;
	private Integer uniprotFrom;
	private Integer uniprotTo;
	private Float eValue;
	private Float identity;
	private Float identityPerc;

	private String uniprotAlign;
	private String pdbAlign;
	private String midlineAlign;

	public String getMidlineAlign()
	{
		return midlineAlign;
	}

	public void setMidlineAlign(String midlineAlign)
	{
		this.midlineAlign = midlineAlign;
	}

	public String getUniprotAlign()
	{
		return uniprotAlign;
	}

	public void setUniprotAlign(String uniprotAlign)
	{
		this.uniprotAlign = uniprotAlign;
	}

	public String getPdbAlign()
	{
		return pdbAlign;
	}

	public void setPdbAlign(String pdbAlign)
	{
		this.pdbAlign = pdbAlign;
	}

	public Float getEValue()
	{
		return eValue;
	}

	public void setEValue(Float eValue)
	{
		this.eValue = eValue;
	}

	public Float getIdentity()
	{
		return identity;
	}

	public void setIdentity(Float identity)
	{
		this.identity = identity;
	}

	public Float getIdentityPerc()
	{
		return identityPerc;
	}

	public void setIdentityPerc(Float identityPerc)
	{
		this.identityPerc = identityPerc;
	}

	public Integer getAlignmentId()
	{
		return alignmentId;
	}

	public void setAlignmentId(Integer alignmentId)
	{
		this.alignmentId = alignmentId;
	}

	public String getPdbId()
	{
		return pdbId;
	}

	public void setPdbId(String pdbId)
	{
		this.pdbId = pdbId;
	}

	public String getChain()
	{
		return chain;
	}

	public void setChain(String chain)
	{
		this.chain = chain;
	}

	public String getUniprotId()
	{
		return uniprotId;
	}

	public void setUniprotId(String uniprotId)
	{
		this.uniprotId = uniprotId;
	}

	public String getPdbFrom()
	{
		return pdbFrom;
	}

	public void setPdbFrom(String pdbFrom)
	{
		this.pdbFrom = pdbFrom;
	}

	public String getPdbTo()
	{
		return pdbTo;
	}

	public void setPdbTo(String pdbTo)
	{
		this.pdbTo = pdbTo;
	}

	public Integer getUniprotFrom()
	{
		return uniprotFrom;
	}

	public void setUniprotFrom(Integer uniprotFrom)
	{
		this.uniprotFrom = uniprotFrom;
	}

	public Integer getUniprotTo()
	{
		return uniprotTo;
	}

	public void setUniprotTo(Integer uniprotTo)
	{
		this.uniprotTo = uniprotTo;
	}
        
}
