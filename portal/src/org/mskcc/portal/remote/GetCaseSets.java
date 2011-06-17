package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.mskcc.portal.model.CaseSet;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets all Case Sets Associated with a specific Cancer Type.
 */
public class GetCaseSets {

    /**
     * Gets all Case Sets Associated with a specific Cancer type.
     *
     * @param cancerTypeId Cancer Type ID.
     * @return ArrayList of CaseSet Objects.
     * @throws RemoteException Remote / Network IO Error.
     */
    public static ArrayList<CaseSet> getCaseSets(String cancerTypeId, XDebug xdebug)
            throws RemoteException {
        ArrayList<CaseSet> caseList = new ArrayList<CaseSet>();
        String content = "";
        try {

            //  Create Query Parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getCaseLists"),
                    new NameValuePair(CgdsProtocol.CANCER_TYPE_ID, cancerTypeId)
            };

            // Parse Text Response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            content = protocol.connect(data, xdebug);
            String lines[] = content.split("\n");
            if (lines.length > 2) {
                for (int i = 2; i < lines.length; i++) {
                    String parts[] = lines[i].split("\t");
                    String id = parts[0];
                    String name = parts[1];
                    String desc = parts[2];
                    String cases = parts[4];
                    CaseSet caseSet = new CaseSet();
                    caseSet.setId(id);
                    caseSet.setName(name);
                    caseSet.setDescription(desc);
                    caseSet.setCaseList(cases);
                    caseList.add(caseSet);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new RemoteException("Remote Access Error:  Got line:  " + content, e);
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        return caseList;
    }
}
