
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.validator.html.PolicyException;

import org.mskcc.portal.network.Network;
import org.mskcc.portal.network.NetworkIO;
import org.mskcc.portal.network.Node;
import org.mskcc.portal.remote.GetPathwayCommonsNetwork;
import org.mskcc.portal.util.XDebug;

/**
 * Retrieving 
 * @author jj
 */
public class NetworkServlet extends HttpServlet {    
    private static final String HGNC = "HGNC";
    
    /**
     * Processes Post Request.
     * 
     * @param req   HttpServletRequest Object.
     * @param res   HttpServletResponse Object.
     * @throws ServletException Servlet Error.
     * @throws IOException IO Error.
     */
    public void doPost(HttpServletRequest req,
                      HttpServletResponse res)
            throws ServletException, IOException {
        XDebug xdebug = new XDebug( req );
        
        ServletXssUtil xssUtil;
        try {
            xssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException (e);
        }
        
        //  Get User Defined Gene List
        String geneListStr = xssUtil.getCleanInput (req, QueryBuilder.GENE_LIST);
        Set<String> genes = new HashSet<String>(Arrays.asList(geneListStr.split(" ")));
        
        //String geneticProfileIdSetStr = xssUtil.getCleanInput (req, QueryBuilder.GENETIC_PROFILE_IDS);
        
        Network network;
        try {
            network = GetPathwayCommonsNetwork.getNetwork(genes, xdebug);
        } catch (Exception e) {
            xdebug.logMsg(this, "Failed retrieving networks from cPath2\n"+e.getMessage());
            network = new Network(); // send an empty network instead
        }
        
        
        String graphml = NetworkIO.writeNetwork2GraphML(network, new NetworkIO.NodeLabelHandler() {
            // using HGNC gene symbol as label if available
            public String getLabel(Node node) {
                Set<String> ngnc = node.getXref(HGNC);
                if (ngnc.isEmpty())
                    return node.getId();
                return ngnc.iterator().next();
            }
        });
        PrintWriter writer = res.getWriter();
        writer.write(graphml);
        writer.flush();
    }
    
}
