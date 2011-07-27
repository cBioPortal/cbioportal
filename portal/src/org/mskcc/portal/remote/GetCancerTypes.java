package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.mskcc.portal.model.CancerType;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

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
        CancerType cancerType = new CancerType("all", "All Cancer Types");
        cancerTypeList.add(cancerType);
        try {
            //  Set Query Parameters
            NameValuePair[] data = {
                    //new  new NameValuePair(CgdsProtocol.CMD, "getCancerStudies"),
                    new NameValuePair(CgdsProtocol.CMD, "getCancerTypes"),
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
                    cancerType = new CancerType(id, name);
                    cancerType.setDescription(description);
                    cancerTypeList.add(cancerType);
                }
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }

        //  Before returning the list, sort it alphabetically
        Collections.sort(cancerTypeList, new CancerTypeComparator());
        return cancerTypeList;
    }
}

/**
 * Compares Cancer Studies, so that we can sort them alphabetically.
 */
class CancerTypeComparator implements Comparator {

    /**
     * Compare two cancer studies.
     * @param o  First Cancer Study.
     * @param o1 Second Cancer Study.
     * @return int indicating name sort order.
     */
    public int compare(Object o, Object o1) {
        CancerType study0 = (CancerType) o;
        CancerType study1 = (CancerType) o1;
        return study0.getCancerName().compareTo(study1.getCancerName());
    }
}
