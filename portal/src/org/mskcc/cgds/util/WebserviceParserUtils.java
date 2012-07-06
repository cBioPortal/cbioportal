
package org.mskcc.cgds.util;


import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.servlet.WebService;
import org.mskcc.cgds.web_api.ProtocolException;
import org.mskcc.portal.util.CaseSetUtil;

/**
 *
 * @author jgao
 */
public final class WebserviceParserUtils {
    
    private WebserviceParserUtils() {}
    
    public static ArrayList<String> getCaseList(HttpServletRequest request) throws ProtocolException,
            DaoException {
        String cases = request.getParameter(WebService.CASE_LIST);
        String caseSetId = request.getParameter(WebService.CASE_SET_ID);
        String caseIdsKey = request.getParameter(WebService.CASE_IDS_KEY);
        
        if (cases == null &&
        	caseIdsKey != null)
        {
        	cases = CaseSetUtil.getCaseIds(caseIdsKey);
        }

        ArrayList<String> caseList = new ArrayList<String>();
        if (caseSetId != null) {
            DaoCaseList dao = new DaoCaseList();
            CaseList selectedCaseList = dao.getCaseListByStableId(caseSetId);
            if (selectedCaseList == null) {
                throw new ProtocolException("Invalid " + WebService.CASE_SET_ID + ":  " + caseSetId + ".");
            }
            caseList = selectedCaseList.getCaseList();
        } else if (cases != null) {
            for (String _case : cases.split("[\\s,]+")) {
                _case = _case.trim();
                if (_case.length() == 0) continue;
                caseList.add(_case);
            }
        } else {
            throw new ProtocolException(WebService.CASE_SET_ID + " or " + WebService.CASE_LIST + " must be specified.");
        }
        return caseList;
    }
}
