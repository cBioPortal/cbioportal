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
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.model.CaseIDFilterMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class which implements the CaseIDs interface.
 */
public class CaseIDsImpl implements CaseIDs {

    private static final String SAMPLE_REGEX = "tcga-sample-pattern";
    private static final String PATIENT_REGEX = "tcga-patient-pattern";

	// ref to our matchers
    private Pattern samplePattern;
    private Pattern patientPattern;

	/**
	 * Constructor.
     *
     * @param config Config
	 */
	public CaseIDsImpl(Config config) {

		// get all the filters
		Collection<CaseIDFilterMetadata> caseIDFilters = config.getCaseIDFilterMetadata(Config.ALL);

		// sanity check
		if (caseIDFilters == null) {
			throw new IllegalArgumentException("cannot instantiate a proper collection of CaseIDFilterMetadata objects.");
		}

		// setup our matchers
		for (CaseIDFilterMetadata caseIDFilter : caseIDFilters) {
            if (caseIDFilter.getFilterName().equals(PATIENT_REGEX)) {
                patientPattern = Pattern.compile(caseIDFilter.getRegex());
            }
            else if (caseIDFilter.getFilterName().equals(SAMPLE_REGEX)) {
                samplePattern = Pattern.compile(caseIDFilter.getRegex());
            }

		}
	}

	/**
	 * Determines if given case id is a tumor case id.
	 *
     * @param caseID String
	 * @return boolean
	 */
	@Override
	public boolean isSampleId(String caseId)
    {
        caseId = clean(caseId);
        return (samplePattern.matcher(caseId).matches());
	}

    @Override
    public String getSampleId(String caseId)
    {
        String cleanId = clean(caseId);
        Matcher matcher = samplePattern.matcher(cleanId);
        return (matcher.find()) ? matcher.group(1) : caseId;
    }

	@Override
	public String getPatientId(String caseId)
    {
        String cleanId = clean(caseId);
        Matcher matcher = patientPattern.matcher(cleanId);
        return (matcher.find()) ? matcher.group(1) : caseId;
	}

    private String clean(String caseId)
    {
        if (caseId.contains("Tumor")) {
            return caseId.replace("Tumor", "01");
        }
        else if (caseId.contains("Normal")) {
            return caseId.replace("Normal", "11");
        }
        else {
            return caseId;
        }
    }
}