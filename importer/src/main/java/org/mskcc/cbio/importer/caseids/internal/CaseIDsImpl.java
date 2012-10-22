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

// package
package org.mskcc.cbio.importer.caseids.internal;

// imports
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.model.ImportDataMatrix;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which implements the CaseIDs interface.
 */
public final class CaseIDsImpl implements CaseIDs {

	// our logger
	private static final Log LOG = LogFactory.getLog(CaseIDsImpl.class);

	// regex used when getting firehose run dates from the broad
    private static final Pattern STANDARD_TUMOR_PATTERN =
		Pattern.compile("^(\\w*)\\s*(\\w*)\\s*(\\w*)$");

	/**
	 * Default Constructor.
	 */
	public CaseIDsImpl() {}

	/**
	 * Converts the given case id to mskcc format.
	 *
	 * @param caseID String
	 * @return String
	 */
	@Override
	public String convertCaseID(final String caseID) {
		return caseID;
	}

	/**
	 * Determines if given case id is a tumor case id.
	 *
     * @param caseID String
	 * @return boolean
	 */
	@Override
	public boolean isTumorCaseID(final String caseID) {

		/*
		Matcher lineMatcher = FIREHOSE_GET_RUNS_LINE_REGEX.matcher(lineOfOutput);
		if (lineMatcher.find()) {
			if (lineMatcher.group(3).equals("yes")) {
			}
		}
		*/

		return true;
	}

	/**
	 * Computes the number of case ids within the give import data matrix.
	 *
     * @param importDataMatrix ImportDataMatrix
	 * @return int
	 */
	@Override
	public int getCaseCount(final ImportDataMatrix importDataMatrix) {

		int toReturn = 0;

		Collection<String> columnHeaders = importDataMatrix.getColumnHeaders();
		for (String columnHeader : columnHeaders) {
			if (isTumorCaseID(columnHeader)) {
				++toReturn;
			}
		}

		// outta here
		return toReturn;
	}
}
