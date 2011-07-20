
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import org.owasp.validator.html.PolicyException;

import org.mskcc.portal.model.GeneticProfile;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.model.ProfileDataSummary;
import org.mskcc.portal.network.Network;
import org.mskcc.portal.network.NetworkIO;
import org.mskcc.portal.network.Node;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.remote.GetPathwayCommonsNetwork;
import org.mskcc.portal.remote.GetProfileData;
import org.mskcc.portal.util.GeneticProfileUtil;
import org.mskcc.portal.util.OncoPrintSpecificationDriver;
import org.mskcc.portal.util.ProfileMerger;
import org.mskcc.portal.util.XDebug;
import org.mskcc.portal.util.ZScoreUtil;

/**
 * Retrieving 
 * @author jj
 */
public class NetworkServlet extends HttpServlet {    
    private static final String HGNC = "HGNC";
    private static final String NODE_ATTR_IN_QUERY = "IN_QUERY";
    private static final String NODE_ATTR_IN_PORTAL = "IN_PORTAL";
    private static final String NODE_ATTR_PERCENT_ALTERED = "PERCENT_ALTERED";
    private static final String NODE_ATTR_PERCENT_MUTATED = "PERCENT_MUTATED";
    private static final String NODE_ATTR_PERCENT_CNA_AMPLIFIED = "PERCENT_CNA_AMPLIFIED";
    private static final String NODE_ATTR_PERCENT_CNA_GAINED = "PERCENT_CNA_GAINED";
    private static final String NODE_ATTR_PERCENT_CNA_HOM_DEL = "PERCENT_CNA_HOMOZYGOUSLY_DELETED";
    private static final String NODE_ATTR_PERCENT_CNA_HET_LOSS = "PERCENT_CNA_HEMIZYGOUSLY_DELETED";
    private static final String NODE_ATTR_PERCENT_MRNA_WAY_UP = "PERCENT_MRNA_WAY_UP";
    private static final String NODE_ATTR_PERCENT_MRNA_WAY_DOWN = "PERCENT_MRNA_WAY_DOWN";
    
    public void doGet(HttpServletRequest req,
                      HttpServletResponse res) 
            throws ServletException, IOException {
        this.doPost(req, res);
    }
    
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
        String geneListStr = xssUtil.getCleanInput(req, QueryBuilder.GENE_LIST);
        Set<String> queryGenes = new HashSet<String>(Arrays.asList(geneListStr.toUpperCase().split(" ")));
        
        //String geneticProfileIdSetStr = xssUtil.getCleanInput (req, QueryBuilder.GENETIC_PROFILE_IDS);
        
        Network network;
        try {
            network = GetPathwayCommonsNetwork.getNetwork(queryGenes, xdebug);
        } catch (Exception e) {
            xdebug.logMsg(this, "Failed retrieving networks from cPath2\n"+e.getMessage());
            network = new Network(); // send an empty network instead
        }
        
        // add attribute is_query to indicate if a node is in query genes
        // and get the list of genes in network
        ArrayList<String> netGenes = new ArrayList<String>();
        for (Node node : network.getNodes()) {
            Set<String> ngnc = node.getXref(HGNC);

            Boolean in_query = Boolean.FALSE;
            if (!ngnc.isEmpty()) { 
                String sym = ngnc.iterator().next();
                in_query = queryGenes.contains(sym);
                netGenes.add(sym);
            } 
            node.addAttribute(NODE_ATTR_IN_QUERY, in_query);
        }
        
        //  Get User Selected Genetic Profiles
        String geneticProfileIdsStr = xssUtil.getCleanInput(req, QueryBuilder.GENETIC_PROFILE_IDS);
        HashSet<String> geneticProfileIdSet = new HashSet<String>(Arrays.asList(geneticProfileIdsStr.split(" ")));
        
        String cancerTypeId = xssUtil.getCleanInput(req, QueryBuilder.CANCER_STUDY_ID);
        // TODO: Later: ACCESS CONTROL: change to cancer study, etc.
        //  Get Genetic Profiles for Selected Cancer Type
        ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles(cancerTypeId, xdebug);
        
        String caseIds = xssUtil.getCleanInput(req, QueryBuilder.CASE_IDS);
        
        // retrieve profiles from CGDS for new genes
        ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
        for (String profileId : geneticProfileIdSet) {                        
            GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
            if( null == profile ){
               continue;
            }
            
            GetProfileData remoteCall = new GetProfileData();
            ProfileData pData = remoteCall.getProfileData(profile, netGenes, caseIds, xdebug);
            if( pData == null ){
               System.err.println( "pData == null" );
            }else{
               if( pData.getGeneList() == null ){
                  System.err.println( "pData.getGeneList() == null" );
               }
            }
            xdebug.logMsg(this, "URI:  " + remoteCall.getURI());
            if (pData != null) {
                xdebug.logMsg(this, "Got number of genes:  " + pData.getGeneList().size());
                xdebug.logMsg(this, "Got number of cases:  " + pData.getCaseIdList().size());
            }
            xdebug.logMsg(this, "Number of warnings received:  " + remoteCall.getWarnings().size());
            profileDataList.add(pData);

        }

        ProfileMerger merger = new ProfileMerger(profileDataList);
        ProfileData netMergedProfile = merger.getMergedProfile();
        ArrayList<String> netGeneList = netMergedProfile.getGeneList();

        double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, req);
        
        ParserOutput netOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(
                StringUtils.join(netGeneList, " "), geneticProfileIdSet,
                profileList, zScoreThreshold);

        OncoPrintSpecification netOncoPrintSpec = netOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
        ProfileDataSummary netDataSummary = new ProfileDataSummary(netMergedProfile,
                netOncoPrintSpec, zScoreThreshold );
        
        // add attributes
        for (String gene : netGeneList) {
            for (Node node : network.getNodesByXref(HGNC, gene.toUpperCase())) {
                node.addAttribute(NODE_ATTR_PERCENT_ALTERED, netDataSummary.getPercentCasesWhereGeneIsAltered(gene));
                node.addAttribute(NODE_ATTR_PERCENT_MUTATED, netDataSummary.getPercentCasesWhereGeneIsMutated(gene));
                node.addAttribute(NODE_ATTR_PERCENT_CNA_AMPLIFIED, netDataSummary.getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.Amplified));
                node.addAttribute(NODE_ATTR_PERCENT_CNA_GAINED, netDataSummary.getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.Gained));
                node.addAttribute(NODE_ATTR_PERCENT_CNA_HOM_DEL, netDataSummary.getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.HomozygouslyDeleted));
                node.addAttribute(NODE_ATTR_PERCENT_CNA_HET_LOSS, netDataSummary.getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.HemizygouslyDeleted));
                node.addAttribute(NODE_ATTR_PERCENT_MRNA_WAY_UP, netDataSummary.getPercentCasesWhereMRNAIsUpRegulated(gene));
                node.addAttribute(NODE_ATTR_PERCENT_MRNA_WAY_DOWN, netDataSummary.getPercentCasesWhereMRNAIsDownRegulated(gene));
            }
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
