package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.maf.MafRecord;

/**
 * Utility class to help extracting correct information from
 * extended mutation data.
 */
public class ExtendedMutationUtil
{
	/**
	 * Retrieves the amino acid change information from the given
	 * ExtendedMutation instance. If oncotator protein change value
	 * is available, then simply returns it. If it is not, then
	 * returns the amino acid change value.
	 *
	 * @param mutation  mutation instance with AA change info
	 * @return          AA change info as a string
	 */
	public static String getAminoAcidChange(ExtendedMutation mutation)
	{
		String aaChange = mutation.getOncotatorProteinChange();

		// TODO If we have a Mutation Assessor score for a given missense mutation,
		// we should use the AA change provided by Mutation Assessor.
		// MA may sometimes use a different isoform than Oncotator,
		// but we want to make sure that the links to MA match what we show in the portal.

		if (aaChange == null ||
		    aaChange.equals(MafRecord.NA_STRING))
		{
			aaChange = mutation.getAminoAcidChange();
		}

		return aaChange;
	}
}
