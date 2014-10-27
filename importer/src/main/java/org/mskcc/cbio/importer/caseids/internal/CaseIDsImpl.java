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

// package
package org.mskcc.cbio.importer.caseids.internal;

// imports
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import org.apache.commons.logging.*;

import java.util.*;
import java.util.regex.*;

/**
 * Class which implements the CaseIDs interface.
 */
public class CaseIDsImpl implements CaseIDs {

    private static final String SAMPLE_REGEX = "tcga-sample-pattern";
    private static final String PATIENT_REGEX = "tcga-patient-pattern";
    private static final String NON_TCGA_REGEX = "non-tcga-pattern";

    private static final List<String> tcgaNormalTypes = initTCGANormalTypes();
    private static final List<String> initTCGANormalTypes()
    {
        return Arrays.asList(new String[] { "10","11","12","13","14","15","16","17","18","19" });
    }

	// ref to our matchers
    private Pattern samplePattern;
    private Pattern patientPattern;
    private Pattern nonTCGAPattern;

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
            else if (caseIDFilter.getFilterName().equals(NON_TCGA_REGEX)) {
                nonTCGAPattern = Pattern.compile(caseIDFilter.getRegex());
            }
		}
	}

    @Override
    public boolean isSampleId(String caseId)
    {
        return isSampleId(0, caseId);
    }

	@Override
	public boolean isSampleId(int cancerStudyId, String caseId)
    {
        if (nonTCGAPattern.matcher(caseId).matches()) {
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, caseId);
            return (s != null);
        }
        else {
            caseId = clean(caseId);
            return (samplePattern.matcher(caseId).matches());
        }
	}

    @Override
    public boolean isNormalId(String caseId)
    {
        String cleanId = clean(caseId);
        Matcher matcher = samplePattern.matcher(cleanId);
        return (matcher.find()) ? tcgaNormalTypes.contains(matcher.group(2)) : false;
    }

    @Override
    public String getSampleId(String caseId)
    {
        return getSampleId(0, caseId);
    }

    @Override
    public String getSampleId(int cancerStudyId, String caseId)
    {
        if (nonTCGAPattern.matcher(caseId).matches()) {
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, caseId);
            return (s != null) ? s.getStableId() : caseId;
        }
        else {
            String cleanId = clean(caseId);
            Matcher matcher = samplePattern.matcher(cleanId);
            return (matcher.find()) ? matcher.group(1) : caseId;
        }
    }

    @Override
    public String getPatientId(String caseId)
    {
        return getPatientId(0, caseId);
    }

	@Override
	public String getPatientId(int cancerStudyId, String caseId)
    {
        if (nonTCGAPattern.matcher(caseId).matches()) {
            // data files should only have sample ids - get patient id via sample
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, caseId);
            if (s != null && s.getInternalPatientId() > 0) {
                Patient p = DaoPatient.getPatientById(s.getInternalPatientId());
                return (p != null) ? p.getStableId() : caseId;
            }
            else {
                return caseId;
            }
        }
        else {
            String cleanId = clean(caseId);
            Matcher matcher = patientPattern.matcher(cleanId);
            return (matcher.find()) ? matcher.group(1) : caseId;
        }
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