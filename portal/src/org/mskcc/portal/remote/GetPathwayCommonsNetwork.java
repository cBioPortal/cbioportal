
package org.mskcc.portal.remote;

import org.mskcc.portal.util.XDebug;

import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.List;

import org.mskcc.portal.network.Network;
import org.mskcc.portal.network.NetworkIO;

/**
 * Gets network from Pathway Commons.
 * @author jj
 */
public class GetPathwayCommonsNetwork {

    /**
     * Gets network of interest of seed genes.
     *
     * @param geneList Gene List.
     * @return Tab-delimited content.
     */
    public Network getNetwork (List<String> geneList,
          XDebug xdebug) throws RemoteException {
        try {
            StringBuilder sbUrl = new StringBuilder("http://www.pathwaycommons.org/pc2/graph?"
                    + "format=EXTENDED_BINARY_SIF&kind=NEIGHBORHOOD");
            for (String gene : geneList) {
                sbUrl.append("&source=urn:pathwaycommons:RelationshipXref:NGNC_");
                sbUrl.append(gene);
            }
            
            //xdebug.logMsg(this, "URL: "+sbUrl.toString());
            return NetworkIO.readNetworkFromCPath2(new URL(sbUrl.toString()).openStream());
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        
    }
}