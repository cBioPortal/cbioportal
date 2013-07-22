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

/**
 * Encapsulates a single record from the Oncotator service.
 *
 * @author Selcuk Onur Sumer
 */
public class OncotatorRecord
{
	private String key;
	private String rawJson;
	private String genomeChange;
	private String dbSnpRs;
	private String dbSnpValStatus;
	private String cosmicOverlappingMutations;

	private Transcript bestCanonicalTranscript;
	private Transcript bestEffectTranscript;

    public OncotatorRecord(String key)
    {
        this.key = key;
	    this.bestCanonicalTranscript = new Transcript();
	    this.bestEffectTranscript = new Transcript();
    }

	// Getters and Setters

    public String getKey() {
        return key;
    }

	public String getRawJson()
	{
		return rawJson;
	}

	public void setRawJson(String rawJson)
	{
		this.rawJson = rawJson;
	}

    public String getGenomeChange() {
        return genomeChange;
    }

    public void setGenomeChange(String genomeChange) {
        this.genomeChange = genomeChange;
    }

    public String getCosmicOverlappingMutations() {
        return cosmicOverlappingMutations;
    }

    public void setCosmicOverlappingMutations(String cosmicOverlappingMutations) {
        this.cosmicOverlappingMutations = cosmicOverlappingMutations;
    }

    public String getDbSnpRs() {
        return dbSnpRs;
    }

    public void setDbSnpRs(String dbSnpRs) {
        this.dbSnpRs = dbSnpRs;
    }

	public String getDbSnpValStatus()
	{
		return dbSnpValStatus;
	}

	public void setDbSnpValStatus(String dbSnpValStatus)
	{
		this.dbSnpValStatus = dbSnpValStatus;
	}

	public Transcript getBestCanonicalTranscript()
	{
		return bestCanonicalTranscript;
	}

	public void setBestCanonicalTranscript(Transcript bestCanonicalTranscript)
	{
		this.bestCanonicalTranscript = bestCanonicalTranscript;
	}

	public Transcript getBestEffectTranscript()
	{
		return bestEffectTranscript;
	}

	public void setBestEffectTranscript(Transcript bestEffectTranscript)
	{
		this.bestEffectTranscript = bestEffectTranscript;
	}
}
