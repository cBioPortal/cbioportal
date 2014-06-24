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

package org.mskcc.cbio.maf;

/**
 * Encapsulates details regarding a single row in a Fusion file.
 *
 * @author Selcuk Onur Sumer
 */
public class FusionRecord
{
	private String hugoGeneSymbol;
	private long entrezGeneId;
	private String center; // sequencing center
	private String tumorSampleID;
	private String fusion;
	private String dnaSupport;
	private String rnaSupport;
	private String method;
	private String frame;

	public String getHugoGeneSymbol()
	{
		return hugoGeneSymbol;
	}

	public void setHugoGeneSymbol(String hugoGeneSymbol)
	{
		this.hugoGeneSymbol = hugoGeneSymbol;
	}

	public long getEntrezGeneId()
	{
		return entrezGeneId;
	}

	public void setEntrezGeneId(long entrezGeneId)
	{
		this.entrezGeneId = entrezGeneId;
	}

	public String getCenter()
	{
		return center;
	}

	public void setCenter(String center)
	{
		this.center = center;
	}

	public String getTumorSampleID()
	{
		return tumorSampleID;
	}

	public void setTumorSampleID(String tumorSampleID)
	{
		this.tumorSampleID = tumorSampleID;
	}

	public String getFusion()
	{
		return fusion;
	}

	public void setFusion(String fusion)
	{
		this.fusion = fusion;
	}

	public String getDnaSupport()
	{
		return dnaSupport;
	}

	public void setDnaSupport(String dnaSupport)
	{
		this.dnaSupport = dnaSupport;
	}

	public String getRnaSupport()
	{
		return rnaSupport;
	}

	public void setRnaSupport(String rnaSupport)
	{
		this.rnaSupport = rnaSupport;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getFrame()
	{
		return frame;
	}

	public void setFrame(String frame)
	{
		this.frame = frame;
	}
}
