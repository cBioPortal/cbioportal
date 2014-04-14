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
package org.mskcc.cbio.portal.util;

// imports
import org.mskcc.cbio.portal.dao.DaoCaseList;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.util.AccessControl;
import org.mskcc.cbio.portal.model.CaseList;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CancerStudyStats;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.web_api.ProtocolException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
	private static AccessControl accessControl = initializeAccessControl();

	// ref to total number of samples for al cancer studies
	private Integer totalNumberOfSamples;

	// ref to our list of cancer study stats & total num of samples
	private List<CancerStudyStats> cancerStudyStats;

	// ref to case list DAO
	private DaoCaseList daoCaseList;

	/**
	 * Constructor (private).
	 */
	public DataSetsUtil() {

		try {
			daoCaseList = new DaoCaseList();
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

	/** 
	 * Initializes the AccessControl member.
	 */
	protected static final AccessControl initializeAccessControl() {
		ApplicationContext context = 
			new ClassPathXmlApplicationContext("classpath:applicationContext-security.xml");
		return (AccessControl)context.getBean("accessControl");
	}

	private int getCount(CancerStudy cancerStudy, String caseListSuffix) throws DaoException {
		
		String caseListID = cancerStudy.getCancerStudyStableId() + caseListSuffix;
		CaseList desiredCaseList = daoCaseList.getCaseListByStableId(caseListID);
		
		// outta here
		return (desiredCaseList != null) ? desiredCaseList.getCaseList().size() : 0;
	}
}
