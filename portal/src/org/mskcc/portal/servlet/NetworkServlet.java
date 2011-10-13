
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

import org.mskcc.portal.network.Edge;
import org.owasp.validator.html.PolicyException;

import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.model.ProfileDataSummary;
import org.mskcc.portal.network.Network;
import org.mskcc.portal.network.NetworkIO;
import org.mskcc.portal.network.Node;
import org.mskcc.portal.oncoPrintSpecLanguage.GeneticTypeLevel;
import org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.portal.remote.GetCaseSets;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.remote.GetPathwayCommonsNetwork;
import org.mskcc.portal.util.GeneticProfileUtil;
import org.mskcc.portal.util.OncoPrintSpecificationDriver;
import org.mskcc.portal.util.ProfileMerger;
import org.mskcc.portal.util.XDebug;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.web_api.GetProfileData;

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
    
    @Override
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
    @Override
    public void doPost(HttpServletRequest req,
                      HttpServletResponse res)
            throws ServletException, IOException {
        res.setContentType("text/xml");
        try {
            XDebug xdebug = new XDebug( req );
            
            String xd = req.getParameter("xdebug");

            ServletXssUtil xssUtil;
            try {
                xssUtil = ServletXssUtil.getInstance();
            } catch (PolicyException e) {
                throw new ServletException (e);
            }
            
            if (xd!=null && xd.equals("1")) {
                xdebug.logMsg(this, "<a href=\""+getNetworkServletUrl(req, xssUtil)+"\" target=\"_blank\">NetworkServlet URL</a>");
            }

            //  Get User Defined Gene List
            String geneListStr = xssUtil.getCleanInput(req, QueryBuilder.GENE_LIST);
            Set<String> queryGenes = new HashSet<String>(Arrays.asList(geneListStr.toUpperCase().split(" ")));

            //String geneticProfileIdSetStr = xssUtil.getCleanInput (req, QueryBuilder.GENETIC_PROFILE_IDS);

            Network network;
            try {
                xdebug.startTimer();
                network = GetPathwayCommonsNetwork.getNetwork(queryGenes, xdebug);
                xdebug.stopTimer();
                xdebug.logMsg(this, "Successfully retrieved networks from cPath2: took "+xdebug.getTimeElapsed()+"ms");
            } catch (Exception e) {
                xdebug.logMsg(this, "Failed retrieving networks from cPath2\n"+e.getMessage());
                network = new Network(); // send an empty network instead
            }

            // remove small molecules
//            xdebug.startTimer();
//            network.filter(new Network.Filter() {
//                public boolean filterNode(Node node) {
//                    if (node == null) {
//                        return true;
//                    }
//
//                    return !"Protein".equals(node.getType());
//                }
//
//                public boolean filterEdge(Edge edge) {
//                    return filterNode(edge.getSourceNode()) || filterNode(edge.getTargetNode());
//                }
//            });
//            xdebug.stopTimer();
//            xdebug.logMsg(this, "Removed non-protein nodes: took "+xdebug.getTimeElapsed()+"ms");

            if (!network.getNodes().isEmpty()) {
                
                // add attribute is_query to indicate if a node is in query genes
                // and get the list of genes in network
                xdebug.logMsg(this, "Retrieving data from CGDS...");
                
                ArrayList<String> netGenes = new ArrayList<String>();
                for (Node node : network.getNodes()) {
                    Set<String> ngnc = node.getXref(HGNC);

                    boolean inQuery = false;
                    if (!ngnc.isEmpty()) {
                        String sym = ngnc.iterator().next();
                        inQuery = queryGenes.contains(sym);
                        netGenes.add(sym);
                    }
                    node.addAttribute(NODE_ATTR_IN_QUERY, Boolean.toString(inQuery));
                }

                //  Get User Selected Genetic Profiles
                xdebug.startTimer();
                HashSet<String> geneticProfileIdSet = new HashSet<String>();

                for (String geneticProfileIdsStr : req.getParameterValues(QueryBuilder.GENETIC_PROFILE_IDS)) {
                    geneticProfileIdSet.addAll(Arrays.asList(geneticProfileIdsStr.split(" ")));
                }

                String cancerTypeId = xssUtil.getCleanInput(req, QueryBuilder.CANCER_STUDY_ID);
                
                xdebug.stopTimer();
                xdebug.logMsg(this, "Got User Selected Genetic Profiles. Took "+xdebug.getTimeElapsed()+"ms");
                
                //  Get Genetic Profiles for Selected Cancer Type
                xdebug.startTimer();
                ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles(cancerTypeId);

                String caseIds = xssUtil.getCleanInput(req, QueryBuilder.CASE_IDS);

                if (caseIds==null || caseIds.isEmpty()) {
                    String caseSetId = xssUtil.getCleanInput(req, QueryBuilder.CASE_SET_ID);
                        //  Get Case Sets for Selected Cancer Type
                        ArrayList<CaseList> caseSets = GetCaseSets.getCaseSets(cancerTypeId);
                        for (CaseList cs : caseSets) {
                            if (cs.getStableId().equals(caseSetId)) {
                                caseIds = cs.getCaseListAsString();
                                break;
                            }
                        }
                }
                
                xdebug.stopTimer();
                xdebug.logMsg(this, "Got Genetic Profiles for Selected Cancer Type. Took "+xdebug.getTimeElapsed()+"ms");

                // retrieve profile data from CGDS for new genes
                Set<GeneticAlterationType> alterationTypes = new HashSet();
                ArrayList<ProfileData> profileDataList = new ArrayList<ProfileData>();
                for (String profileId : geneticProfileIdSet) {
                    xdebug.startTimer();
                    GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
                    alterationTypes.add(profile.getGeneticAlterationType());
                    if( null == profile ){
                       continue;
                    }

                    GetProfileData remoteCall = new GetProfileData(profile, netGenes, caseIds);
                    ProfileData pData = remoteCall.getProfileData();
                    if( pData == null ){
                       System.err.println( "pData == null" );
                    }else{
                       if( pData.getGeneList() == null ){
                          System.err.println( "pData.getValidGeneList() == null" );
                       }
                    }
                    profileDataList.add(pData);
                
                    xdebug.stopTimer();
                    xdebug.logMsg(this, "Got profile data from CGDS for new genes for "
                            +profile.getProfileName()+". Took "+xdebug.getTimeElapsed()+"ms");
                }

                xdebug.startTimer();
                ProfileMerger merger = new ProfileMerger(profileDataList);
                xdebug.stopTimer();
                xdebug.logMsg(this, "Merged profiles. Took "+xdebug.getTimeElapsed()+"ms");
                
                xdebug.startTimer();
                ProfileData netMergedProfile = merger.getMergedProfile();
                ArrayList<String> netGeneList = netMergedProfile.getGeneList();

                double zScoreThreshold = Double.parseDouble(xssUtil.getCleanInput(req, QueryBuilder.Z_SCORE_THRESHOLD));

                ParserOutput netOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver(
                        StringUtils.join(netGeneList, " "), geneticProfileIdSet,
                        profileList, zScoreThreshold);

                OncoPrintSpecification netOncoPrintSpec = netOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
                ProfileDataSummary netDataSummary = new ProfileDataSummary(netMergedProfile,
                        netOncoPrintSpec, zScoreThreshold );
                
                xdebug.stopTimer();
                xdebug.logMsg(this, "Got profile data summary. Took "+xdebug.getTimeElapsed()+"ms");

                // add attributes
                xdebug.startTimer();
                for (String gene : netGeneList) {
                    for (Node node : network.getNodesByXref(HGNC, gene.toUpperCase())) {
                        node.addAttribute(NODE_ATTR_PERCENT_ALTERED, netDataSummary
                                .getPercentCasesWhereGeneIsAltered(gene));
                        if (alterationTypes.contains(GeneticAlterationType.MUTATION_EXTENDED) ||
                                alterationTypes.contains(GeneticAlterationType.MUTATION_EXTENDED)) {
                            node.addAttribute(NODE_ATTR_PERCENT_MUTATED, netDataSummary
                                    .getPercentCasesWhereGeneIsMutated(gene));
                        }

                        if (alterationTypes.contains(GeneticAlterationType.COPY_NUMBER_ALTERATION)) {
                            node.addAttribute(NODE_ATTR_PERCENT_CNA_AMPLIFIED, netDataSummary
                                    .getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.Amplified));
                            node.addAttribute(NODE_ATTR_PERCENT_CNA_GAINED, netDataSummary
                                    .getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.Gained));
                            node.addAttribute(NODE_ATTR_PERCENT_CNA_HOM_DEL, netDataSummary
                                    .getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.HomozygouslyDeleted));
                            node.addAttribute(NODE_ATTR_PERCENT_CNA_HET_LOSS, netDataSummary
                                    .getPercentCasesWhereGeneIsAtCNALevel(gene, GeneticTypeLevel.HemizygouslyDeleted));
                        }

                        if (alterationTypes.contains(GeneticAlterationType.MRNA_EXPRESSION)) {
                            node.addAttribute(NODE_ATTR_PERCENT_MRNA_WAY_UP, netDataSummary
                                    .getPercentCasesWhereMRNAIsUpRegulated(gene));
                            node.addAttribute(NODE_ATTR_PERCENT_MRNA_WAY_DOWN, netDataSummary
                                    .getPercentCasesWhereMRNAIsDownRegulated(gene));
                        }
                    }
                }
                
                xdebug.stopTimer();
                xdebug.logMsg(this, "Added node attributes. Took "+xdebug.getTimeElapsed()+"ms");
            }


            String graphml = NetworkIO.writeNetwork2GraphML(network, new NetworkIO.NodeLabelHandler() {
                // using HGNC gene symbol as label if available
                public String getLabel(Node node) {
                    Set<String> ngnc = node.getXref(HGNC);
                    if (!ngnc.isEmpty()) {
                        return ngnc.iterator().next();
                    }
                    
                    Set<Object> strNames = node.getAttributes().get("PARTICIPANT_NAME");
                    if (strNames!=null && !strNames.isEmpty()) {
                        String[] names = strNames.iterator().next().toString().split(";");
                        if (names.length>0) {
                            return names[0];
                        }
                    }
                    
                    return node.getId();
                }
            });
            
            if (xd!=null && xd.equals("1")) {
                writeXDebug(xdebug,req,res);
            }
            
            PrintWriter writer = res.getWriter();
            writer.write(graphml);
            writer.flush();
        } catch (DaoException e) {
            throw new ServletException (e);
        }
    }
    
    private String getNetworkServletUrl(HttpServletRequest req, ServletXssUtil xssUtil) {
        String geneListStr = xssUtil.getCleanInput(req, QueryBuilder.GENE_LIST);
        String geneticProfileIdsStr = xssUtil.getCleanInput(req, QueryBuilder.GENETIC_PROFILE_IDS);
        String cancerTypeId = xssUtil.getCleanInput(req, QueryBuilder.CANCER_STUDY_ID);
        String caseSetId = xssUtil.getCleanInput(req, QueryBuilder.CASE_SET_ID);
        String zscoreThreshold = xssUtil.getCleanInput(req, QueryBuilder.Z_SCORE_THRESHOLD);
        
        return "network.do?"+QueryBuilder.GENE_LIST+"="+geneListStr
                +"&"+QueryBuilder.GENETIC_PROFILE_IDS+"="+geneticProfileIdsStr
                +"&"+QueryBuilder.CANCER_STUDY_ID+"="+cancerTypeId
                +"&"+QueryBuilder.CASE_SET_ID+"="+caseSetId
                +"&"+QueryBuilder.Z_SCORE_THRESHOLD+"="+zscoreThreshold;
    }
    
    private void writeXDebug(XDebug xdebug, HttpServletRequest req,
                      HttpServletResponse res) 
            throws ServletException, IOException {
        PrintWriter writer = res.getWriter();
        writer.write("<!--xdebug messages begin:\n");
        for (Object msg : xdebug.getDebugMessages()) {
            writer.write(((org.mskcc.portal.util.XDebugMessage)msg).getMessage());
            writer.write("\n");
        }
        writer.write("xdebug messages end-->\n");
    }
}
