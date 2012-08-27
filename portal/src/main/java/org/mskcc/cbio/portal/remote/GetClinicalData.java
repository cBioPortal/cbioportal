package org.mskcc.cbio.portal.remote;

import org.mskcc.cbio.cgds.dao.DaoClinicalData;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.ClinicalData;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Gets clinical data for specified cases.
 */
public class GetClinicalData {
    public static final String NA = "NA";

    /**
     * Gets clinical data for specified cases.
     *
     * @param setOfCaseIds Case IDs.
     * @return an ArrayList of ClinicalData Objects
     * @throws DaoException, as of August 2011 GetClinicalData has direct access to DAO Objects.
     */
    public static ArrayList<ClinicalData> getClinicalData(HashSet<String> setOfCaseIds) throws DaoException {
        if (setOfCaseIds != null && setOfCaseIds.size() > 0) {
            DaoClinicalData daoClinicalData = new DaoClinicalData();
            return daoClinicalData.getCases(setOfCaseIds);
        } else {
            return new ArrayList<ClinicalData>();
        }
    }
}