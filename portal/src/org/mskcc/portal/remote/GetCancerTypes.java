package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.mskcc.portal.model.CancerType;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets all Cancer Types stored in Remote CGDS Server.
 */
public class GetCancerTypes {

    /**
     * Gets all Cancer Types stored in Remote CGDS Server.
     *
     * @return ArrayList of CancerType Objects.
     * @throws RemoteException Remote / Network IO Error.
     */
    public static ArrayList<CancerType> getCancerTypes(XDebug xdebug) throws RemoteException {
        ArrayList<CancerType> cancerTypeList = new ArrayList<CancerType>();
        try {
            //  Set Query Parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getCancerStudies"),
            };

            //  Parse Text Response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            String content = protocol.connect(data, xdebug);
            String lines[] = content.split("\n");
            if (lines.length > 2) {
                for (int i = 2; i < lines.length; i++) {
                    String parts[] = lines[i].split("\t");
                    String id = parts[0];
                    String name = parts[1];
                    String description = parts[2];
                    CancerType cancerType = new CancerType(id, name);
                    cancerType.setDescription(description);
                    cancerTypeList.add(cancerType);
                }
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        return cancerTypeList;
    }
}
