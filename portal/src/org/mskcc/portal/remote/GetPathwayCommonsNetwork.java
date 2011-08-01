
package org.mskcc.portal.remote;

import org.mskcc.portal.util.XDebug;

import java.io.InputStream;
import java.io.IOException;

import java.rmi.RemoteException;

import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

import org.mskcc.portal.network.Network;
import org.mskcc.portal.network.NetworkIO;
import org.mskcc.portal.util.GlobalProperties;

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
    public static Network getNetwork (Set<String> geneList,
          XDebug xdebug) throws RemoteException {
        try {
            StringBuilder sbUrl = new StringBuilder(GlobalProperties.getPathwayCommonsUrl());
			sbUrl.append("/graph?format=EXTENDED_BINARY_SIF&kind=NEIGHBORHOOD");
            for (String gene : geneList) {
                sbUrl.append("&source=urn:biopax:RelationshipXref:HGNC_");
                sbUrl.append(gene.toUpperCase());
            }
            
            MultiThreadedHttpConnectionManager connectionManager =
                    ConnectionManager.getConnectionManager();
            HttpClient client = new HttpClient(connectionManager);
            
            GetMethod method = new GetMethod(sbUrl.toString());
            try {
                int statusCode = client.executeMethod(method);
                if (statusCode == HttpStatus.SC_OK) {
                    //xdebug.logMsg(this, "URL: "+sbUrl.toString());
                    return NetworkIO.readNetworkFromCPath2(method.getResponseBodyAsStream(), true);
                } else {
                    //  Otherwise, throw HTTP Exception Object
                    throw new HttpException(statusCode + ": " + HttpStatus.getStatusText(statusCode)
                            + " Base URL:  " + sbUrl.toString());
                }            
            
            } finally {
                //  Must release connection back to Apache Commons Connection Pool
                method.releaseConnection();
            }
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        
    }
}