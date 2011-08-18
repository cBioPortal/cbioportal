// package
package org.mskcc.cgds.util.internal;

// imports
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.util.AccessControl;
import org.mskcc.cgds.web_api.ProtocolException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

/**
 * Utilities for managing access control.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 * @author Benjamin Gross
 */
public class AccessControlImpl implements AccessControl {

	// ref to log
	private static Log log = LogFactory.getLog(AccessControlImpl.class);

    /**
     * Gets Cancer Studies. Used by QueryBuilder.
     *
     * @return List<CancerStudy>
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    public List<CancerStudy> getCancerStudiesAsList() throws DaoException, ProtocolException {

		if (log.isDebugEnabled()) {
			log.debug("getCancerStudies(), getting accessible cancer studies.");
		}

		// get list of accessible cancer studies
        List<CancerStudy> accessibleCancerStudies = getAccessibleCancerStudies();

        if (accessibleCancerStudies.size() > 0) {

            //  sort the list
            Collections.sort(accessibleCancerStudies, new CancerStudiesComparator());

            //  Then, insert "All" Cancer Types at beginning
            ArrayList<CancerStudy> finalCancerStudiesList = new ArrayList<CancerStudy>();
            CancerStudy cancerStudy = new CancerStudy("All Cancer Types", "All Cancer Types",
                                                      "all", "all", true);
            finalCancerStudiesList.add(cancerStudy);
            finalCancerStudiesList.addAll(accessibleCancerStudies);
            
            return finalCancerStudiesList;
        }
        else {
            throw new ProtocolException("No cancer studies accessible; "+
                                        "either provide credentials to access private studies, " +
                                        "or ask administrator to load public ones.\n");
        }
    }

    /**
     * Gets Cancer Studies.
     *
     * @return Cancer Studies Table.
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    public String getCancerStudiesAsTable() throws DaoException, ProtocolException {

		if (log.isDebugEnabled()) {
			log.debug("getCancerStudies(), getting accessible cancer studies.");
		}

		// get list of accessible cancer studies
        List<CancerStudy> accessibleCancerStudies = getAccessibleCancerStudies();

		if (log.isDebugEnabled()) {
			log.debug("getCancerStudies(), number of accessibleCancerStudies: " + accessibleCancerStudies.size());
		}

        StringBuffer buf = new StringBuffer();
        if (accessibleCancerStudies.size() > 0) {

            buf.append("cancer_study_id\tname\tdescription\n");
            for (CancerStudy cancerStudy : accessibleCancerStudies) {
                
                // changed to output stable identifier, instead of internal integer identifer.
                buf.append(cancerStudy.getCancerStudyStableId() + "\t");
                buf.append(cancerStudy.getName() + "\t");
                buf.append(cancerStudy.getDescription() + "\n");
            }
            return buf.toString();
        }
        else {
            throw new ProtocolException("No cancer studies accessible; " + 
                                        "either provide credentials to access private studies, " +
                                        "or ask administrator to load public ones.\n");
        }
    }

    /**
     * Return true if the user can access the study, false otherwise.
	 *
     * @param stableStudyId
     * @return boolean
     * @throws DaoException
     */
    public boolean checkAccess(String stableStudyId) throws DaoException {

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(stableStudyId);
		if (log.isDebugEnabled()) {
			log.debug("checkAccess(), stableStudyId: " + stableStudyId);
		}

		// outta here
		return checkAccess(cancerStudy);
	}

	/**
	 * Helper function that provides list of accessible cancer studies.
	 *
	 * @return List<CancerStudy>
	 * @throws DaoException
	 *
	 * We use @PostFilter annotation to remove elements
	 * in the return list inaccessible to the user.
	 */
	@PostFilter("hasPermission(filterObject, 'read')")
	private List<CancerStudy> getAccessibleCancerStudies() throws DaoException {
		return DaoCancerStudy.getAllCancerStudies();
	}

	/**
	 * Helper function that provides authorization on given cancer study.
	 *
	 * @return boolean 
	 *
	 * We use @PreAuthorize annotation to provide 
	 * permission evaluation on this cancer study.
	 */
	@PreAuthorize("hasPermission(#cancerStudy, 'read')")
	private boolean checkAccess(CancerStudy cancerStudy) {
        // set to true so works when authentication is turned off
		return true;
	}
}

/**
 * Compares Cancer Studies, so that we can sort them alphabetically.
 */
class CancerStudiesComparator implements Comparator {

    /**
     * Compare two cancer studies.
     * @param o  First Cancer Study.
     * @param o1 Second Cancer Study.
     * @return int indicating name sort order.
     */
    public int compare(Object o, Object o1) {
        CancerStudy study0 = (CancerStudy) o;
        CancerStudy study1 = (CancerStudy) o1;
        return study0.getName().compareTo(study1.getName());
    }
}