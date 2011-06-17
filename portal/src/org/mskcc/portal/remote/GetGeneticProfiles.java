package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.mskcc.portal.model.GeneticProfile;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets all Genetic Profiles associated with a specific cancer type.
 */
public class GetGeneticProfiles {

    /**
     * Gets all Genetic Profiles associated with a specific cancer type.
     *
     * @param cancerTypeId Cancer Type ID.
     * @return ArrayList of GeneticProfile Objects.
     * @throws RemoteException Remote / Network IO Error.
     */
    public static ArrayList<GeneticProfile> getGeneticProfiles(String cancerTypeId, XDebug xdebug)
            throws RemoteException {
        ArrayList<GeneticProfile> profileList = new ArrayList<GeneticProfile>();
        try {

            // Prepare Query Parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getGeneticProfiles"),
                    new NameValuePair(CgdsProtocol.CANCER_TYPE_ID, cancerTypeId)
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
                    String desc = parts[2];
                    String alterationType = parts[4];
                    boolean showInAnalysisTab = true;
                    try {
                        showInAnalysisTab = Boolean.parseBoolean(parts[5]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                    }
                    GeneticProfile profile = new GeneticProfile(id, name,
                            desc, alterationType, showInAnalysisTab);
                    profileList.add(profile);
                }
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        return profileList;
    }
}
