package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.mskcc.portal.model.ClinicalData;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets clinical data for specified cases.
 */
public class GetClinicalData {
    public static final String NA = "NA";

    /**
     * Gets clinical data for specified cases.
     *
     * @param caseIds Case IDs.
     * @return Tab-delimited content.
     * @throws RemoteException Remote Server IO Error.
     */
    public static ArrayList<ClinicalData> getClinicalData(String caseIds,
                                                          XDebug xdebug) throws RemoteException {
        try {

            //  Create Query Parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getClinicalData"),
                    new NameValuePair(CgdsProtocol.CASE_LIST, caseIds)
            };

            // Parse Text Response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            String content = protocol.connect(data, xdebug);
            String lines[] = content.split("\n");
            ArrayList<ClinicalData> clinicalDataList = new ArrayList<ClinicalData>();
            if (lines.length > 2) {
                for (int i = 2; i < lines.length; i++) {
                    String parts[] = lines[i].split("\t");
                    String caseId = getString(parts, 0);
                    Double osMonths = getDouble(parts, 1);
                    String osStatus = getString(parts, 2);
                    Double dfsMonths = getDouble(parts, 3);
                    String dfsStatus = getString(parts, 4);
                    Double ageAtDiagnosis = getDouble(parts, 5);
                    if (caseId != null) {
                        ClinicalData clinicalData = new ClinicalData(caseId, osMonths, osStatus,
                                dfsMonths, dfsStatus, ageAtDiagnosis);
                        clinicalDataList.add(clinicalData);
                    }
                }
            }
            return clinicalDataList;
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
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