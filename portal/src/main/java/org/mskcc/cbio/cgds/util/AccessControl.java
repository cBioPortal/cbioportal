// package
package org.mskcc.cbio.cgds.util;

// imports
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.web_api.ProtocolException;

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
