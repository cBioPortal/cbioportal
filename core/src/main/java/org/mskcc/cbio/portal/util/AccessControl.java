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
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.web_api.ProtocolException;

import org.springframework.security.access.prepost.PostFilter;

import java.util.List;

/**
 * Utilities for managing access control.
 *
 * @author Benjamin Gross
 */
public interface AccessControl {

    public static final String ALL_CANCER_STUDIES_ID = "all";
    public static final String ALL_TCGA_CANCER_STUDIES_ID = "all_tcga";
    public static final String ALL_TARGET_CANCER_STUDIES_ID = "all_nci_target";

    /**
     * Gets Cancer Studies. Used by QueryBuilder.
     *
     * @return List<CancerStudy>
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
	 *
	 * We use @PostFilter annotation to remove elements
	 * in the return list inaccessible to the user.
     */
	@PostFilter("hasPermission(filterObject, 'read')")
    List<CancerStudy> getCancerStudies() throws DaoException, ProtocolException;

    /**
     * Return true if the user can access the study, false otherwise.
	 *
     * @param stableStudyId
     * @return ListCancerStudy
     * @throws DaoException
	 *
	 * We use @PostFilter rather than @PreAuthorize annotation to provide 
	 * permission evaluation on this cancer study so that we can process
	 * invalid permissions via QueryBuilder.validateForm().  If we use @PreAuthorize,
	 * thread execution does not return from this method call if a user has invalid permissions.
     */
	//@PreAuthorize("hasPermission(#stableStudyId, 'read')")
	@PostFilter("hasPermission(filterObject, 'read')")
    List<CancerStudy> isAccessibleCancerStudy(String stableStudyId) throws DaoException;
}
