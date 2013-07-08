/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.portal.mut_diagram;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.dao.DaoMutation;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.util.ExtendedMutationUtil;
import org.mskcc.cbio.portal.util.OmaLinkUtil;
import org.mskcc.cbio.portal.util.SequenceCenterUtil;
import org.mskcc.cbio.portal.util.SkinUtil;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A service designed to return a JSON array of mutation objects.
 *
 * @author Selcuk Onur Sumer
 */
public class MutationDataProcessor
{
	private static final Logger logger = Logger.getLogger(MutationDataProcessor.class);

	public String getMutationData(String geneSymbol, List<ExtendedMutation> mutations)
	{
		// final object to be send as JSON
		JSONArray mutationArray = new JSONArray();

		Map<String, Integer> countMap = this.getMutationCountMap(mutations);

		// extract row data for each mutation
		for (ExtendedMutation mutation : mutations)
		{
			HashMap<String, Object> mutationData = new HashMap<String, Object>();

			int cancerStudyId = DaoGeneticProfile.getGeneticProfileById(mutation.getGeneticProfileId()).getCancerStudyId();
			String cancerStudyStableId = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId)
					.getCancerStudyStableId();
			String linkToPatientView = SkinUtil.getLinkToPatientView(mutation.getCaseId(), cancerStudyStableId);

			mutationData.put("mutationEventId", mutation.getMutationEventId());
			mutationData.put("geneSymbol", mutation.getGeneSymbol());
			mutationData.put("caseId", mutation.getCaseId());
			mutationData.put("linkToPatientView", linkToPatientView);
			mutationData.put("proteinChange", mutation.getProteinChange());
			mutationData.put("mutationType", mutation.getMutationType());
			mutationData.put("cosmic", mutation.getOncotatorCosmicOverlapping());
			mutationData.put("cosmicCount", this.getCosmicCount(mutation));
			mutationData.put("functionalImpactScore", mutation.getFunctionalImpactScore());
			mutationData.put("msaLink", this.getMsaLink(mutation));
			mutationData.put("xVarLink", this.getXVarLink(mutation));
			mutationData.put("pdbLink", this.getPdbLink(mutation));
			mutationData.put("mutationStatus", mutation.getMutationStatus());
			mutationData.put("validationStatus", mutation.getValidationStatus());
			mutationData.put("sequencingCenter", this.getSequencingCenter(mutation));
			mutationData.put("ncbiBuildNo", this.getNcbiBuild(mutation));
			mutationData.put("chr", this.getChromosome(mutation));
			mutationData.put("startPos", mutation.getStartPosition());
			mutationData.put("endPos", mutation.getEndPosition());
			mutationData.put("referenceAllele", mutation.getReferenceAllele());
			mutationData.put("variantAllele", this.getVariantAllele(mutation));
			mutationData.put("tumorFreq", this.getTumorFreq(mutation));
			mutationData.put("normalFreq", this.getNormalFreq(mutation));
			mutationData.put("tumorRefCount", this.getTumorRefCount(mutation));
			mutationData.put("tumorAltCount", this.getTumorAltCount(mutation));
			mutationData.put("normalRefCount", this.getNormalRefCount(mutation));
			mutationData.put("normalAltCount", this.getNormalAltCount(mutation));
			mutationData.put("canonicalTranscript", mutation.isCanonicalTranscript());
			mutationData.put("refseqMrnaId", mutation.getOncotatorRefseqMrnaId());
			mutationData.put("codonChange", mutation.getOncotatorCodonChange());
			mutationData.put("uniprotId", this.getUniprotId(mutation));
			mutationData.put("mutationCount", countMap.get(mutation.getCaseId()));

			mutationArray.add(mutationData);
		}

		return JSONValue.toJSONString(mutationArray);
	}

	/**
	 * Returns the MSA (alignment) link for the given mutation.
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding MSA link
	 */
	protected String getMsaLink(ExtendedMutation mutation)
	{
		String urlMsa = "";

		if (linkIsValid(mutation.getLinkMsa()))
		{
			try
			{
				if(mutation.getLinkMsa().length() == 0 ||
				   mutation.getLinkMsa().equals("NA"))
				{
					urlMsa = "NA";
				}
				else
				{
					urlMsa = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkMsa());
				}
			}
			catch (MalformedURLException e)
			{
				logger.error("Could not parse OMA URL:  " + e.getMessage());
			}
		}

		return urlMsa;
	}

	/**
	 * Returns the PDB (structure) link for the given mutation.
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding PDB link
	 */
	protected String getPdbLink(ExtendedMutation mutation)
	{
		String urlPdb = "";

		if (linkIsValid(mutation.getLinkPdb()))
		{
			try
			{
				if(mutation.getLinkPdb().length() == 0 ||
				   mutation.getLinkPdb().equals("NA"))
				{
					urlPdb = "NA";
				}
				else
				{
					urlPdb = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkPdb());
				}
			}
			catch (MalformedURLException e)
			{
				logger.error("Could not parse OMA URL:  " + e.getMessage());
			}
		}

		return urlPdb;
	}

	/**
	 * Returns the xVar (mutation assessor) link for the given mutation.
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding xVar link
	 */
	protected String getXVarLink(ExtendedMutation mutation)
	{
		String xVarLink = "";

		if (linkIsValid(mutation.getLinkXVar()))
		{
			try
			{
				xVarLink = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkXVar());
			}
			catch (MalformedURLException e)
			{
				logger.error("Could not parse OMA URL:  " + e.getMessage());
				//return HtmlUtil.createEmptySpacer();
			}
		}

		return xVarLink;
	}

	/**
	 * Checks the validity of the given link.
	 *
	 * @param link  string representation of a URL
	 * @return      true if valid, false otherwise
	 */
	protected boolean linkIsValid(String link)
	{
		return link != null &&
		       link.length() > 0 &&
		       !link.equalsIgnoreCase("NA");
	}

	protected String getSequencingCenter(ExtendedMutation mutation)
	{
		return SequenceCenterUtil.getSequencingCenterAbbrev(mutation.getSequencingCenter());
	}

	/**
	 * Creates an html "a" element for the cosmic overlapping value
	 * of the given mutation. The text of the element will be the sum
	 * of all cosmic values, and the id of the element will be the
	 * (non-parsed) cosmic overlapping string.
	 *
	 * @param mutation  mutation instance
	 * @return          string representing an "a" element for the cosmic value
	 */
	protected int getCosmicCount(ExtendedMutation mutation)
	{
		if (mutation.getOncotatorCosmicOverlapping() == null ||
		    mutation.getOncotatorCosmicOverlapping().equals("NA"))
		{
			return 0;
		}

		// calculate total cosmic count
		Integer total = ExtendedMutationUtil.calculateCosmicCount(mutation);

		if (total > 0)
		{
			return total;
		}
		else
		{
			return 0;
		}
	}

	/**
	 * Returns one of the tumor sequence alleles which is different from
	 * the reference allele.
	 *
	 * @param mutation  mutation instance
	 * @return          tumor sequence allele different from the reference allele
	 */
	protected String getVariantAllele(ExtendedMutation mutation)
	{
		String varAllele = mutation.getTumorSeqAllele1();

		if (mutation.getReferenceAllele() != null &&
		    mutation.getReferenceAllele().equals(mutation.getTumorSeqAllele1()))
		{
			varAllele = mutation.getTumorSeqAllele2();
		}

		return varAllele;
	}

	/**
	 * Returns the corresponding NCBI build number (hg18 or hg19).
	 *
	 * @param mutation  mutation instance
	 * @return          corresponding NCBI build number
	 */
	protected String getNcbiBuild(ExtendedMutation mutation)
	{
		String build = mutation.getNcbiBuild();

		if (build == null)
		{
			return build;
		}
		if (build.equals("36") ||
		    build.equals("36.1"))
		{
			return "hg18";
		}
		else if (build.equals("37") ||
		         build.equalsIgnoreCase("GRCh37"))
		{
			return "hg19";
		}
		else
		{
			return build;
		}
	}

	/**
	 * Returns the corresponding chromosome.
	 *
	 * @param mutation  mutation instance
	 * @return          chromosome number
	 */
	protected String getChromosome(ExtendedMutation mutation)
	{
		if (mutation.getChr() == null)
		{
			return null;
		}
		else if (mutation.getChr().equals("NA"))
		{
			return "NA";
		}
		else
		{
			return "chr" + mutation.getChr();
		}
	}

	protected Integer getNormalAltCount(ExtendedMutation mutation)
	{
		Integer count = mutation.getNormalAltCount();

		if (count == TabDelimitedFileUtil.NA_INT)
		{
			count = null;
		}

		return count;
	}

	protected Integer getNormalRefCount(ExtendedMutation mutation)
	{
		Integer count = mutation.getNormalRefCount();

		if (count == TabDelimitedFileUtil.NA_INT)
		{
			count = null;
		}

		return count;
	}

	protected Integer getTumorAltCount(ExtendedMutation mutation)
	{
		Integer count = mutation.getTumorAltCount();

		if (count == TabDelimitedFileUtil.NA_INT)
		{
			count = null;
		}

		return count;
	}

	protected Integer getTumorRefCount(ExtendedMutation mutation)
	{
		Integer count = mutation.getTumorRefCount();

		if (count == TabDelimitedFileUtil.NA_INT)
		{
			count = null;
		}

		return count;
	}

	protected Double getNormalFreq(ExtendedMutation mutation)
	{
		Integer altCount = this.getNormalAltCount(mutation);
		Integer refCount = this.getNormalRefCount(mutation);

		return this.getFreq(altCount, refCount);
	}

	protected Double getTumorFreq(ExtendedMutation mutation)
	{
		Integer altCount = this.getTumorAltCount(mutation);
		Integer refCount = this.getTumorRefCount(mutation);

		return this.getFreq(altCount, refCount);
	}

	protected Double getFreq(Integer altCount, Integer refCount)
	{
		Double freq;

		if (altCount == null ||
		    refCount == null)
		{
			freq = null;
		}
		else
		{
			freq = altCount.doubleValue() /
			       (altCount.doubleValue() + refCount.doubleValue());
		}

		return freq;
	}

	protected String getUniprotId(ExtendedMutation mutation)
	{
		// TODO uniprot name or uniprot accession
		return mutation.getOncotatorUniprotName();
	}

	/**
	 * Creates a map of mutation counts for the given list of mutations.
	 *
	 * @param mutations     list of mutations
	 * @return              map build on (case id, mutation count) pairs
	 */
	protected Map<String, Integer> getMutationCountMap(List<ExtendedMutation> mutations)
	{
		// build mutation count map
		List<String> caseIds = new LinkedList<String>();

		Integer geneticProfileId = -1;

		// assuming same genetic profile id for all mutations in the list
		if (mutations.size() > 0)
		{
			geneticProfileId = mutations.iterator().next().getGeneticProfileId();
		}

		// collect case ids
		for (ExtendedMutation mutation : mutations)
		{
			caseIds.add(mutation.getCaseId());
		}

		Map<String, Integer> counts;

		// retrieve count map
		try
		{
			counts = DaoMutation.countMutationEvents(geneticProfileId, caseIds);
		}
		catch (DaoException e)
		{
			counts = null;
		}

		return counts;
	}
}
