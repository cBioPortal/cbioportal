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
package org.mskcc.cbio.portal.util;

// imports
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.web_api.ProtocolException;

import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Utility Class to help generate Data Sets Page.
 *
 * @author Benjamin Gross.
 */
public class DataSetsUtil {

	// ref to our access control object
	private static AccessControl accessControl = SpringUtil.getAccessControl();

	// ref to total number of samples for al cancer studies
	private Integer totalNumberOfSamples;

	// ref to our list of cancer study stats & total num of samples
	private List<CancerStudyStats> cancerStudyStats;

	// ref to patient list DAO
	private DaoPatientList daoPatientList;

	/**
	 * Constructor (private).
	 */
	public DataSetsUtil() {

		try {
			daoPatientList = new DaoPatientList();
			// totalNumberOfSamples will be set while computing stats
			totalNumberOfSamples = 0;
			cancerStudyStats = computeCancerStudyStats();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the cancer study stats.
	 *
	 * @return List<CancerStudyStats>
	 */
	public List<CancerStudyStats> getCancerStudyStats() { return cancerStudyStats; }

	/**
	 * Gets total number of samples for all studies.
	 *
     * @return Integer
	 */
	public Integer getTotalNumberOfSamples() { return totalNumberOfSamples; }

	/**
	 * Routine which constructs and returns a list of CancerStudyStats
	 *
     * @return List<CancerStudyStats>
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
	 */
	private List<CancerStudyStats> computeCancerStudyStats() throws DaoException, ProtocolException {

		// what we are returning
		List<CancerStudyStats> toReturn = new ArrayList<CancerStudyStats>();

		// get list of cancer studies
		List<CancerStudy> cancerStudyList = accessControl.getCancerStudies();

		// first element is 'all', remove it
		cancerStudyList.remove(0); 

		// process the list
		for (CancerStudy cancerStudy : cancerStudyList) {
                        String citation = cancerStudy.getCitation();
                        if (citation==null) {
                            citation = "";
                        } else {
                            String pmid = cancerStudy.getPmid();
                            if (pmid!=null) {
                                citation = "<a href='http://www.ncbi.nlm.nih.gov/pubmed/"+pmid+"'>"+citation+"</a>";
                            }
                        }
			// get genetic profiles
			int sequenced = getCount(cancerStudy, "_sequenced");
			int cna = getCount(cancerStudy, "_cna");
			int RNASEQ = getCount(cancerStudy, "_rna_seq_v2_mrna");
			int tumorMRNA = getCount(cancerStudy, "_mrna");
			int normal = getCount(cancerStudy, "_normal_mrna");
			int tumorMIRNA = getCount(cancerStudy, "_microrna");
			int methylationHM27 = getCount(cancerStudy, "_methylation_hm27");
			int rppa = getCount(cancerStudy, "_rppa");
			int complete = getCount(cancerStudy, "_3way_complete");
			int all = getCount(cancerStudy, "_all");
			totalNumberOfSamples += all;
			// add to return list
			toReturn.add(new CancerStudyStats(cancerStudy.getCancerStudyStableId(), 
											  cancerStudy.getName(), citation, all, sequenced,
											  cna, RNASEQ, tumorMRNA, normal, tumorMIRNA,
											  methylationHM27, rppa, complete));
		}

		// outta here
		return toReturn;
	}

	private int getCount(CancerStudy cancerStudy, String patientListSuffix) throws DaoException {
		
		String patientListID = cancerStudy.getCancerStudyStableId() + patientListSuffix;
		PatientList desiredPatientList = daoPatientList.getPatientListByStableId(patientListID);
		
		// outta here
		return (desiredPatientList != null) ? desiredPatientList.getPatientList().size() : 0;
	}
}
