package org.mskcc.cbio.portal.remote;

import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CaseList;

import java.util.ArrayList;

/**
 * Gets all Case Sets Associated with a specific Cancer Type.
 */
public class GetCaseSets {

    /**
     * Gets all Case Sets Associated with a specific Cancer Study.
     *
     * @param cancerStudyId Cancer Study ID.
     * @return ArrayList of CaseSet Objects.
     * @throws DaoException Database Error.
     */
    public static ArrayList<CaseList> getCaseSets(String cancerStudyId)
            throws DaoException {
        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(cancerStudyId);
        if (cancerStudy != null) {
            DaoCaseList daoCaseList = new DaoCaseList();
            ArrayList<CaseList> caseList = daoCaseList.getAllCaseLists(cancerStudy.getInternalId());
            return caseList;
        } else {
            ArrayList<CaseList> caseList = new ArrayList<CaseList>();
            return caseList;
        }
    }
}
