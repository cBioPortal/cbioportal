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


package org.mskcc.cbio.cgds.util;


import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.mskcc.cbio.cgds.dao.DaoCaseList;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CaseList;
import org.mskcc.cbio.cgds.servlet.WebService;
import org.mskcc.cbio.cgds.web_api.ProtocolException;
import org.mskcc.cbio.portal.util.CaseSetUtil;

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
