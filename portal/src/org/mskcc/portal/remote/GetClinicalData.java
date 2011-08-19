package org.mskcc.portal.remote;

import org.mskcc.cgds.dao.DaoClinicalData;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.ClinicalData;
import org.mskcc.portal.util.XDebug;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * Gets clinical data for specified cases.
 */
public class GetClinicalData {
    public static final String NA = "NA";

    /**
     * Gets clinical data for specified cases.
     *
     * @param caseIds Case IDs.
     * @return an ArrayList of ClinicalData Objects
     * @throws DaoException DaoObject for MySQL exception.
     */
    public static ArrayList<ClinicalData> getClinicalData(String caseIds, XDebug xdebug) throws DaoException {

        try {
            DaoClinicalData daoClinicalData = new DaoClinicalData();
            String caseIdList[] = caseIds.split(" ");
            Set<String> caseSet = new HashSet<String>();
            //for (String caseID : caseIdList) {caseSet.add(caseID);}
            caseSet.addAll(Arrays.asList(caseIdList));
            return daoClinicalData.getCases(caseSet);
        } catch (DaoException e) {
            System.err.println("Database Error: " + e.getMessage());
        }
        return null;
    }


    private static Double getDouble(String parts[], int index) {
        if (parts == null) {
            return null;
        } else if (index < 0) {
            return null;
        } else {
            try {
                String value = parts[index];
                if (value.length() == 0) {
                    return null;
                } else if (value.equalsIgnoreCase(NA)) {
                    return null;
                } else {
                    try {
                        Double dValue = Double.parseDouble(value);
                        return dValue;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    private static String getString(String parts[], int index) {
        if (parts == null) {
            return null;
        } else if (index < 0) {
            return null;
        } else {
            try {
                String value = parts[index];
                if (value.length() == 0) {
                    return null;
                } else if (value.equalsIgnoreCase(NA)) {
                    return null;
                } else {
                    return value;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }
}