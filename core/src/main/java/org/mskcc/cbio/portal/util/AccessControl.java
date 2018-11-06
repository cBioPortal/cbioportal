/*
 * Copyright (c) 2015 - 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

// imports
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.web_api.ProtocolException;

import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.core.userdetails.UserDetails;

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
    public static final String MULTIPLE_CANCER_STUDIES_ID = "multiple";

    /**
     * Gets Cancer Studies. Used by QueryBuilder.
     *
     * @return List<CancerStudy>
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    @PostFilter("hasPermission(filterObject.getCancerStudyStableId(), 'CancerStudyId', 'read')")
    List<CancerStudy> getCancerStudies() throws DaoException, ProtocolException;

    /**
     * Return true if the user can access the study, false otherwise.
	 *
     * @param stableStudyId
     * @return ListCancerStudy
     * @throws DaoException
     */
    @PostFilter("hasPermission(#stableStudyId, 'CancerStudyId', 'read')")
    List<CancerStudy> isAccessibleCancerStudy(String stableStudyId) throws DaoException;

    UserDetails getUserDetails();
}
