// package
package org.mskcc.cbio.portal.util;

// imports
import org.mskcc.cbio.cgds.dao.DaoCaseList;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.util.AccessControl;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.CancerStudyStats;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.web_api.ProtocolException;

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
			String stableID = cancerStudy.getCancerStudyStableId();
			// get genetic profiles
			int sequenced = getCount(cancerStudy, "_sequenced");
			int aCGH = getCount(cancerStudy, "_acgh");
			int RNASEQ = getCount(cancerStudy, "_rna_seq_mrna");
			int tumorMRNA = getCount(cancerStudy, "_mrna");
			int normal = getCount(cancerStudy, "_normal_mrna");
			int tumorMIRNA = getCount(cancerStudy, "_microrna");
			int methylation = getCount(cancerStudy, "_methylation");
			int rppa = getCount(cancerStudy, "_rppa");
			int complete = getComplete(cancerStudy);
			int all = getAll(cancerStudy);
			totalNumberOfSamples += all;
			// add to return list
			toReturn.add(new CancerStudyStats(cancerStudy.getCancerStudyStableId(),
											  cancerStudy.getName(), all, sequenced,
											  aCGH, RNASEQ, tumorMRNA, normal, tumorMIRNA,
											  methylation, rppa, complete));
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

	private int getAll(CancerStudy cancerStudy) throws DaoException {

		String caseListID;
		CaseList desiredCaseList;
		HashSet<String> union = new HashSet<String>();
		String[] dataTypes = {"_sequenced", "_acgh", "_rna_seq_mrna", "_mrna", "_methylation", "_rppa"};

		for (String dataType : dataTypes) {
			caseListID = cancerStudy.getCancerStudyStableId() + dataType;
			desiredCaseList = daoCaseList.getCaseListByStableId(caseListID);
			if (desiredCaseList != null) {
				union.addAll(desiredCaseList.getCaseList());
			}
		}

		// outta here
		return union.size();
	}

	private int getComplete(CancerStudy cancerStudy) throws DaoException {

		// used below
		String caseListID;
		CaseList desiredCaseList;
		HashSet<String> intersection = null;
		
		// tumorMRNA - use rna seq first, if not exist, use mrna
		String rnaDataType = null;
		String[] rnaDataTypes = {"_rna_seq_mrna", "_mrna"};
		for (String dataType : rnaDataTypes) {
			caseListID = cancerStudy.getCancerStudyStableId() + dataType;
			desiredCaseList = daoCaseList.getCaseListByStableId(caseListID);
			if (desiredCaseList != null) {
				rnaDataType = dataType;
				break;
			}
		}
		// if either expression file is missing, no complete study, bail...
		if (rnaDataType == null) {
			return 0;
		}

		// do be complete, we require acgh, sequenced, and expression data for the sample
		String[] dataTypes = {"_acgh", "_sequenced", rnaDataType};
		for (String dataType : dataTypes) {
			caseListID = cancerStudy.getCancerStudyStableId() + dataType;
			desiredCaseList = daoCaseList.getCaseListByStableId(caseListID);
			if (desiredCaseList == null) {
				return 0;
			}
			else if (intersection == null) {
				intersection = new HashSet<String>(desiredCaseList.getCaseList());
			}
			else {
				intersection.retainAll(desiredCaseList.getCaseList());
			}
		}

		// outta here
		return (intersection == null) ? 0 : intersection.size();
	}
}
