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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;

/**
 * Utility class related to ExtendedMutation.
 */
public class ExtendedMutationUtil
{
	public static final String NOT_AVAILABLE = "NA";

	public static String getCaseId(String barCode)
	{
		// process bar code
		// an example bar code looks like this:  TCGA-13-1479-01A-01W

		String barCodeParts[] = barCode.split("-");

		String caseId = null;

		try
		{
			caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];

			// the following condition was prompted by case ids coming from
			// private cancer studies (like SKCM_BROAD) with case id's of
			// the form MEL-JWCI-WGS-XX or MEL-Ma-Mel-XX or MEL-UKRV-Mel-XX
			if (!barCode.startsWith("TCGA") &&
			    barCodeParts.length == 4)
			{
				caseId += "-" + barCodeParts[3];
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			caseId = barCode;
		}

		return caseId;
	}

	/**
	 * Determines the most accurate amino acid change value for the given mutation.
	 *
	 * If there is an Oncotator value, returns that value.
	 * If no Oncotator value, then tries Mutation Assessor value.
	 * If no MA value either, then tries the amino_acid_change column
	 * If none of the above is valid then returns "MUTATED"
	 *
	 * @param parts     current mutation as split parts of the line
	 * @param record    MAF record for the current line
	 * @return          most accurate amino acid change
	 */
	public static String getProteinChange(String[] parts, MafRecord record)
	{
		// Note: MA may sometimes use a different isoform than Oncotator.

		// try oncotator value first
		String aminoAcidChange = record.getOncotatorProteinChange();

		// if no oncotator value, try mutation assessor value
		if (!isValidProteinChange(aminoAcidChange))
		{
			aminoAcidChange = record.getMaProteinChange();
		}

		// if no MA value either, then try amino_acid_change column
		if (!isValidProteinChange(aminoAcidChange))
		{
			aminoAcidChange = record.getMannualAminoAcidChange();
		}

		// if none is valid, then use the string "MUTATED"
		if (!isValidProteinChange(aminoAcidChange))
		{
			aminoAcidChange = "MUTATED";
		}

		// also remove the starting "p." string if any
		aminoAcidChange = normalizeProteinChange(aminoAcidChange);

		return aminoAcidChange;
	}

	/**
	 * Removes the starting "p." (if any) from the given
	 * amino acid change string.
	 *
	 * @param aminoAcidChange   aa change string to be normalized
	 * @return                  normalized aa change string
	 */
	public static String normalizeProteinChange(String aminoAcidChange)
	{
		String pDot = "p.";

		// remove the starting "p." string if any
		if (aminoAcidChange.startsWith(pDot))
		{
			aminoAcidChange = aminoAcidChange.substring(pDot.length());
		}

		return aminoAcidChange;
	}

	public static boolean isValidProteinChange(String proteinChange)
	{
		boolean invalid = proteinChange == null ||
		                  proteinChange.length() == 0 ||
		                  proteinChange.equalsIgnoreCase("NULL") ||
		                  proteinChange.equalsIgnoreCase(NOT_AVAILABLE);

		return !invalid;
	}

	public static boolean isAcceptableMutation(String mutationType)
	{
		// check for null or NA values
		if (mutationType == null ||
		    mutationType.length() == 0 ||
		    mutationType.equals("NULL") ||
		    mutationType.equals(TabDelimitedFileUtil.NA_STRING))
		{
			return false;
		}

		// check for the type
		boolean silent = mutationType.toLowerCase().startsWith("silent");
		boolean loh = mutationType.toLowerCase().startsWith("loh");
		boolean wildtype = mutationType.toLowerCase().startsWith("wildtype");
		boolean utr3 = mutationType.toLowerCase().startsWith("3'utr");
		boolean utr5 = mutationType.toLowerCase().startsWith("5'utr");
		boolean flank5 = mutationType.toLowerCase().startsWith("5'flank");
		boolean igr = mutationType.toLowerCase().startsWith("igr");
		boolean rna = mutationType.equalsIgnoreCase("rna");

		return !(silent || loh || wildtype || utr3 || utr5 || flank5 || igr || rna);
	}

	public static String getMutationType(MafRecord record)
	{
		String mutationType = record.getOncotatorVariantClassification();

		if (mutationType == null ||
		    mutationType.length() == 0 ||
		    mutationType.equals("NULL") ||
		    mutationType.equals(TabDelimitedFileUtil.NA_STRING))
		{
			mutationType = record.getVariantClassification();
		}

		return mutationType;
	}

	public static int getTumorAltCount(MafRecord record) {
		int result = TabDelimitedFileUtil.NA_INT ;

		if (record.getTumorAltCount() != TabDelimitedFileUtil.NA_INT) {
			result = record.getTumorAltCount();
		} else if(record.getTVarCov() != TabDelimitedFileUtil.NA_INT) {
			result = record.getTVarCov();
		} else if((record.getTumorDepth() != TabDelimitedFileUtil.NA_INT) &&
		          (record.getTumorVaf() != TabDelimitedFileUtil.NA_INT)) {
			result = Math.round(record.getTumorDepth() * record.getTumorVaf());
		}

		return result;
	}

	public static int getTumorRefCount(MafRecord record) {
		int result = TabDelimitedFileUtil.NA_INT;

		if (record.getTumorRefCount() != TabDelimitedFileUtil.NA_INT) {
			result = record.getTumorRefCount();
		} else if((record.getTVarCov() != TabDelimitedFileUtil.NA_INT) &&
		          (record.getTTotCov() != TabDelimitedFileUtil.NA_INT)) {
			result = record.getTTotCov()-record.getTVarCov();
		} else if((record.getTumorDepth() != TabDelimitedFileUtil.NA_INT) &&
		          (record.getTumorVaf() != TabDelimitedFileUtil.NA_INT)) {
			result = record.getTumorDepth() - Math.round(record.getTumorDepth() * record.getTumorVaf());
		}

		return result;
	}

	public static int getNormalAltCount(MafRecord record) {
		int result = TabDelimitedFileUtil.NA_INT ;

		if (record.getNormalAltCount() != TabDelimitedFileUtil.NA_INT) {
			result = record.getNormalAltCount();
		} else if(record.getNVarCov() != TabDelimitedFileUtil.NA_INT) {
			result = record.getNVarCov();
		} else if((record.getNormalDepth() != TabDelimitedFileUtil.NA_INT) &&
		          (record.getNormalVaf() != TabDelimitedFileUtil.NA_INT)) {
			result = Math.round(record.getNormalDepth() * record.getNormalVaf());
		}

		return result;
	}

	public static int getNormalRefCount(MafRecord record) {
		int result = TabDelimitedFileUtil.NA_INT;

		if (record.getNormalRefCount() != TabDelimitedFileUtil.NA_INT) {
			result = record.getNormalRefCount();
		} else if((record.getNVarCov() != TabDelimitedFileUtil.NA_INT) &&
		          (record.getNTotCov() != TabDelimitedFileUtil.NA_INT)) {
			result = record.getNTotCov()-record.getNVarCov();
		} else if((record.getNormalDepth() != TabDelimitedFileUtil.NA_INT) &&
		          (record.getNormalVaf() != TabDelimitedFileUtil.NA_INT)) {
			result = record.getNormalDepth() - Math.round(record.getNormalDepth() * record.getNormalVaf());
		}

		return result;
	}

	/**
	 * Generates a new ExtendedMutation instance with default values.
	 * The mutation instance returned by this method will not have
	 * any field with a "null" value.
	 *
	 * @return  a mutation instance initialized by default values
	 */
	public static ExtendedMutation newMutation()
	{
		Integer defaultInt = TabDelimitedFileUtil.NA_INT;
		String defaultStr = TabDelimitedFileUtil.NA_STRING;
		//Long defaultLong = TabDelimitedFileUtil.NA_LONG;
		Long defaultLong = -1L;
		Float defaultFloat = TabDelimitedFileUtil.NA_FLOAT;
		CanonicalGene defaultGene = new CanonicalGene("INVALID");

		ExtendedMutation mutation = new ExtendedMutation();

		mutation.setGeneticProfileId(defaultInt);
		mutation.setSampleId(defaultInt);
		mutation.setGene(defaultGene);
		mutation.setSequencingCenter(defaultStr);
		mutation.setSequencer(defaultStr);
		mutation.setProteinChange(defaultStr);
		mutation.setMutationType(defaultStr);
		mutation.setChr(defaultStr);
		mutation.setStartPosition(defaultLong);
		mutation.setEndPosition(defaultLong);
		mutation.setValidationStatus(defaultStr);
		mutation.setMutationStatus(defaultStr);
		mutation.setFunctionalImpactScore(defaultStr);
		mutation.setFisValue(defaultFloat);
		mutation.setLinkXVar(defaultStr);
		mutation.setLinkPdb(defaultStr);
		mutation.setLinkMsa(defaultStr);
		mutation.setNcbiBuild(defaultStr);
		mutation.setStrand(defaultStr);
		mutation.setVariantType(defaultStr);
		mutation.setAllele(defaultStr, defaultStr, defaultStr);
		mutation.setDbSnpRs(defaultStr);
		mutation.setDbSnpValStatus(defaultStr);
		mutation.setMatchedNormSampleBarcode(defaultStr);
		mutation.setMatchNormSeqAllele1(defaultStr);
		mutation.setMatchNormSeqAllele2(defaultStr);
		mutation.setTumorValidationAllele1(defaultStr);
		mutation.setTumorValidationAllele2(defaultStr);
		mutation.setMatchNormValidationAllele1(defaultStr);
		mutation.setMatchNormValidationAllele2(defaultStr);
		mutation.setVerificationStatus(defaultStr);
		mutation.setSequencingPhase(defaultStr);
		mutation.setSequenceSource(defaultStr);
		mutation.setValidationMethod(defaultStr);
		mutation.setScore(defaultStr);
		mutation.setBamFile(defaultStr);
		mutation.setTumorAltCount(defaultInt);
		mutation.setTumorRefCount(defaultInt);
		mutation.setNormalAltCount(defaultInt);
		mutation.setNormalRefCount(defaultInt);
		mutation.setOncotatorDbSnpRs(defaultStr);
		mutation.setOncotatorCodonChange(defaultStr);
		mutation.setOncotatorRefseqMrnaId(defaultStr);
		mutation.setOncotatorUniprotName(defaultStr);
		mutation.setOncotatorUniprotAccession(defaultStr);
		mutation.setOncotatorProteinPosStart(defaultInt);
		mutation.setOncotatorProteinPosEnd(defaultInt);
		mutation.setCanonicalTranscript(true);

		return mutation;
	}
}
