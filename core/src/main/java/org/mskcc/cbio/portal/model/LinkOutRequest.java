/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.model;

import org.mskcc.cbio.portal.servlet.ServletXssUtil;
import org.mskcc.cbio.portal.web_api.ProtocolException;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.owasp.validator.html.PolicyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.HashSet;

/**
 * Encapsulates parameters for a Link Out Request.
 */
public class LinkOutRequest {
    public static final String STABLE_PARAM_CANCER_STUDY_ID = "cancer_study_id";
    public static final String STABLE_PARAM_GENE_LIST = "gene_list";
    public static final String STABLE_PARAM_QUERY = "q";
    public static final String STABLE_PARAM_REPORT = "report";
    public static final String REPORT_FULL = "full";
    public static final String REPORT_ONCOPRINT_HTML = "oncoprint_html";
    private static Logger logger = LoggerFactory.getLogger(LinkOutRequest.class);

    private String cancerStudyId;
    private String geneList;
    private String report;
    private ServletXssUtil xssUtil;
    private boolean isCrossCancerQuery = false;

    public LinkOutRequest(HttpServletRequest httpServletRequest) throws ProtocolException, DaoException {
        try {
            xssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            logger.error("Could not instantiate XSS Util:  " + e.toString());
        }
        getParametersSafely(httpServletRequest);
        validateParameters();
    }

    public String getCancerStudyId() {
        return cancerStudyId;
    }

    public String getGeneList() {
        return geneList;
    }

    public String getGeneListUrlEncoded() throws UnsupportedEncodingException {
        return URLEncoder.encode(geneList, "UTF-8");
    }

    public String getReport() {
        return report;
    }

    private void getParametersSafely(HttpServletRequest httpServletRequest) {
        cancerStudyId = xssUtil.getCleanInput(httpServletRequest, STABLE_PARAM_CANCER_STUDY_ID);
        geneList = xssUtil.getCleanInput(httpServletRequest, STABLE_PARAM_GENE_LIST);
        if (geneList==null) {
            geneList = xssUtil.getCleanInput(httpServletRequest, STABLE_PARAM_QUERY);
        }
        report = xssUtil.getCleanInput(httpServletRequest, STABLE_PARAM_REPORT);
    }

    public boolean isIsCrossCancerQuery() {
        return isCrossCancerQuery;
    }

    private void validateParameters() throws ProtocolException, DaoException {
        validateCancerStudyId();
        validateGeneList();
        validateOutput();
    }

    private void validateGeneList() throws ProtocolException {
        if (geneList == null || geneList.length() == 0) {
            throw new ProtocolException (LinkOutRequest.STABLE_PARAM_GENE_LIST + " is not specified");
        }
    }

    private void validateCancerStudyId() throws ProtocolException, DaoException {
        if (cancerStudyId == null || cancerStudyId.length() == 0 || cancerStudyId.equalsIgnoreCase("all")) {
            isCrossCancerQuery = true;
        }
//        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
//        if (cancerStudy == null) {
//            throw new ProtocolException(cancerStudyId + " is not a recognized cancer study ID.");
//        }
    }

    private void validateOutput() throws ProtocolException {
        Set<String> validOutputs = initValidOutputSet();
        if (report == null || report.length() == 0) {
            report = REPORT_FULL;
        } else {
            if (!validOutputs.contains(report.toLowerCase())) {
                throw new ProtocolException("Unrecognized " + STABLE_PARAM_REPORT + ":  " + report);
            }
        }
    }

    private Set<String> initValidOutputSet() {
        Set<String> validOutputs = new HashSet<String>();
        validOutputs.add(REPORT_FULL);
        validOutputs.add(REPORT_ONCOPRINT_HTML);
        return validOutputs;
    }
}
