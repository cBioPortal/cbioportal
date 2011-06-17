package org.mskcc.portal.remote;

import org.apache.commons.httpclient.NameValuePair;
import org.mskcc.portal.util.XDebug;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Gets network of interest.
 */
public class GetNetwork {
    public static final String NA = "NA";

    /**
     * Gets network of interest of seed genes.
     *
     * @param geneList Gene List.
     * @return Tab-delimited content.
     * @throws java.rmi.RemoteException Remote Server IO Error.
     */
    public static String getNetwork (ArrayList<String> geneList,
          XDebug xdebug) throws RemoteException {
        try {

            //  Prepare gene list
            StringBuffer geneBuf = new StringBuffer();
            for (String gene : geneList) {
                geneBuf.append(gene + " ");
            }

            //  Create Query Parameters
            NameValuePair[] data = {
                    new NameValuePair(CgdsProtocol.CMD, "getNetwork"),
                    new NameValuePair(CgdsProtocol.GENE_LIST, geneBuf.toString())
            };

            // Parse Text Response
            CgdsProtocol protocol = new CgdsProtocol(xdebug);
            String content = protocol.connect(data, xdebug);
            String lines[] = content.split("\n");
            StringBuffer sif = new StringBuffer();
            if (lines.length > 2) {
                for (int i = 2; i < lines.length; i++) {
                    sif.append(lines[i] + "\n");
                }
            }
            return sif.toString();
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
    }
}