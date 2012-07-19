package org.mskcc.cbio.portal.model;

import org.mskcc.portal.servlet.ServletXssUtil;
import org.mskcc.cgds.web_api.ProtocolException;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.owasp.validator.html.PolicyException;
import org.apache.log4j.Logger;

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
    public static final String STABLE_PARAM_REPORT = "report";
    public static final String REPORT_FULL = "full";
    public static final String REPORT_ONCOPRINT_HTML = "oncoprint_html";
    private static Logger logger = Logger.getLogger(LinkOutRequest.class);

    private String cancerStudyId;
    private String geneList;
    private String report;
    private ServletXssUtil xssUtil;

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
        report = xssUtil.getCleanInput(httpServletRequest, STABLE_PARAM_REPORT);
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
        if (cancerStudyId == null || cancerStudyId.length() == 0) {
            throw new ProtocolException(LinkOutRequest.STABLE_PARAM_CANCER_STUDY_ID + " is not specified");
        }
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy == null) {
            throw new ProtocolException(cancerStudyId + " is not a recognized cancer study ID.");
        }
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
