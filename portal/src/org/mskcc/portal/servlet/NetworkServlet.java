
package org.mskcc.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mskcc.portal.network.Network;
import org.mskcc.portal.network.NetworkIO;
import org.mskcc.portal.network.NetworkUtils;
import org.mskcc.portal.network.Node;
import org.mskcc.portal.remote.GetCaseSets;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.util.GeneticProfileUtil;
import org.mskcc.portal.util.XDebug;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoGeneticAlteration;

/**
 * Retrieving 
 * @author jj
 */
public class NetworkServlet extends HttpServlet {
    private static final String NODE_ATTR_IN_QUERY = "IN_QUERY";
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
            StringBuilder messages = new StringBuilder();
            
            XDebug xdebug = new XDebug( req );
            
            String xd = req.getParameter("xdebug");
            boolean logXDebug = xd!=null && xd.equals("1");
            
            if (logXDebug) {
                xdebug.logMsg(this, "<a href=\""+getNetworkServletUrl(req)+"\" target=\"_blank\">NetworkServlet URL</a>");
            }

            //  Get User Defined Gene List
            String geneListStr = req.getParameter(QueryBuilder.GENE_LIST);
            Set<String> queryGenes = new HashSet<String>(Arrays.asList(geneListStr.toUpperCase().split(" ")));

            //String geneticProfileIdSetStr = xssUtil.getCleanInput (req, QueryBuilder.GENETIC_PROFILE_IDS);

            String netSrc = req.getParameter("netsrc");
            
            Network network;
            xdebug.startTimer();
            if (netSrc.toUpperCase().equals("cpath2")) {
                network = NetworkIO.readNetworkFromCPath2(queryGenes, true);
                if (logXDebug) {
                    xdebug.logMsg("GetPathwayCommonsNetwork", "<a href=\""+NetworkIO.getCPath2URL(queryGenes)
                            +"\" target=\"_blank\">cPath2 URL</a>");
                }
            } else {
                network = NetworkIO.readNetworkFromCGDS(queryGenes, true);
            }
            
            String netSize = req.getParameter("netsize");
            boolean topologyPruned = pruneNetwork(network,netSize);
            
            xdebug.stopTimer();
            xdebug.logMsg(this, "Successfully retrieved networks from " + netSrc
                    + ": took "+xdebug.getTimeElapsed()+"ms");

            if (!network.getNodes().isEmpty()) {                
                // add attribute is_query to indicate if a node is in query genes
                // and get the list of genes in network
                xdebug.logMsg(this, "Retrieving data from CGDS...");
                
                // get cancer study id
                String cancerTypeId = req.getParameter(QueryBuilder.CANCER_STUDY_ID);
                
                // Get case ids
                Set<String> targetCaseIds = getCaseIds(req, cancerTypeId);
                
                //  Get User Selected Genetic Profiles
                Set<GeneticProfile> geneticProfileSet = getGeneticProfileSet(req, cancerTypeId);
                
                // getzScoreThreshold
                double zScoreThreshold = Double.parseDouble(req.getParameter(QueryBuilder.Z_SCORE_THRESHOLD));

                xdebug.startTimer();
                
                DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
                for (Node node : network.getNodes()) {
                    String ngnc = NetworkUtils.getSymbol(node);
                    if (ngnc==null) {
                        continue;
                    }
                    
                    CanonicalGene canonicalGene = daoGeneOptimized.getGene(ngnc);
                    if (canonicalGene==null) {
                        continue;
                    }
                    
                    long entrezGeneId = canonicalGene.getEntrezGeneId();

                    // add attributes
                    addCGDSDataAsNodeAttribute(node, entrezGeneId, geneticProfileSet, 
                            targetCaseIds, zScoreThreshold);
                }

                xdebug.stopTimer();
                xdebug.logMsg(this, "Retrived data from CGDS. Took "+xdebug.getTimeElapsed()+"ms");
                
                String nLinker = req.getParameter("linkers");
                if (!topologyPruned && nLinker!=null && nLinker.matches("[0-9]+")) {
                    xdebug.startTimer();
                    int nBefore = network.countNodes();
                    int querySize = queryGenes.size();
                    pruneNetworkByAlteration(network, Integer.parseInt(nLinker), querySize);
                    int nAfter = network.countNodes();
                    if (nBefore!=nAfter) {
                        messages.append("The network below contains ");
                        messages.append(nAfter);
                        messages.append(" nodes, including your ");
                        messages.append(querySize);
                        messages.append(" query gene");
                        if (querySize>1) {
                            messages.append("s");
                        }
                        messages.append(" and ");
                        messages.append(nAfter-querySize);
                        messages.append(" (out of ");
                        messages.append(nBefore-querySize);
                        messages.append(") linker genes (genes that interact with at least one query gene).");
                    }
                    xdebug.stopTimer();
                    xdebug.logMsg(this, "Prune network. Took "+xdebug.getTimeElapsed()+"ms");
                }
            }


            String graphml = NetworkIO.writeNetwork2GraphML(network, new NetworkIO.NodeLabelHandler() {
                // using HGNC gene symbol as label if available
                public String getLabel(Node node) {
                    String symbol = NetworkUtils.getSymbol(node);
                    if (symbol!=null) {
                        return symbol;
                    }
                    
                    Object strNames = node.getAttributes().get("PARTICIPANT_NAME");
                    if (strNames!=null) {
                        String[] names = strNames.toString().split(";",2);
                        if (names.length>0) {
                            return names[0];
                        }
                    }
                    
                    return node.getId();
                }
            });
            
            if (logXDebug) {
                writeXDebug(xdebug, res);
            }
            
            if (messages.length()>0) {
                writeMsg(messages.toString(), res);
            }
            
            PrintWriter writer = res.getWriter();
            writer.write(graphml);
            writer.flush();
        } catch (DaoException e) {
            //throw new ServletException (e);
            writeMsg("Error loading network. Please report this to cancergenomics@cbio.mskcc.org!\n"+e.toString(), res);
            res.getWriter().write("<graphml></graphml>");
        }
    }
    
    private boolean pruneNetwork(Network network, String netSize) {
        if ("small".equals(netSize)) {
            NetworkUtils.pruneNetwork(network, new NetworkUtils.NodeSelector() {
                public boolean select(Node node) {
                    return !isInQuery(node);
                }
            });
            return true;
        } else if ("medium".equals(netSize)) {
            NetworkUtils.pruneNetwork(network, new NetworkUtils.NodeSelector() {
                public boolean select(Node node) {
                    String inMedium = (String)node.getAttribute("IN_MEDIUM");
                    return inMedium==null || !inMedium.equals("true");
                }
            });
            return true;
        }
        return false;
    }
    
    /**
     * @param network 
     * @param nKeep keep the top altered
     */
    private void pruneNetworkByAlteration(Network network, int nKeep, int nQuery) {
        if (network.countNodes() <= nKeep + nQuery) {
            return;
        }
        
        List<Node> nodesToRemove = getNodesToRemove(network, nKeep);
        
        for (Node node : nodesToRemove) {
            network.removeNode(node);
        }
    }
    
    /**
     * 
     * @param network
     * @param n
     * @return 
     */
    private List<Node> getNodesToRemove(Network network, int n) {
        // keep track of the top nKeep
        PriorityQueue<Node> topAlteredNodes = new PriorityQueue<Node>(n,
                new Comparator<Node>() {
                    public int compare(Node n1, Node n2) {
                        return Double.compare(getTotalAlteredPercentage(n1),
                                getTotalAlteredPercentage(n2));
                    }
                });
        
        List<Node> nodesToRemove = new ArrayList<Node>();
        for (Node node : network.getNodes()) {
            if (isInQuery(node)) {
                continue;
            }
            
            if (topAlteredNodes.size()<n) {
                topAlteredNodes.add(node);
            } else {
                if (n==0) {
                    nodesToRemove.add(node);
                } else {
                    double alterPerc = getTotalAlteredPercentage(node);
                    if (alterPerc > getTotalAlteredPercentage(topAlteredNodes.peek())) {
                        nodesToRemove.add(topAlteredNodes.poll());
                        topAlteredNodes.add(node);
                    } else {
                        nodesToRemove.add(node);
                    }
                }
            }
        }
        
        return nodesToRemove;
    }
    
    private boolean isInQuery(Node node) {
        String inQuery = (String)node.getAttribute(NODE_ATTR_IN_QUERY);
        return inQuery!=null && inQuery.equals("true");
    }
    
    private double getTotalAlteredPercentage(Node node) {
        Double alterPerc = (Double)node.getAttribute(NODE_ATTR_PERCENT_ALTERED);
        return alterPerc == null ? 0.0 : alterPerc;
    }
    
    private Set<String> getCaseIds(HttpServletRequest req, String cancerStudyId) 
            throws ServletException, DaoException {
        String strCaseIds = req.getParameter(QueryBuilder.CASE_IDS);
        if (strCaseIds==null || strCaseIds.length()==0) {
            String caseSetId = req.getParameter(QueryBuilder.CASE_SET_ID);
                //  Get Case Sets for Selected Cancer Type
                ArrayList<CaseList> caseSets = GetCaseSets.getCaseSets(cancerStudyId);
                for (CaseList cs : caseSets) {
                    if (cs.getStableId().equals(caseSetId)) {
                        strCaseIds = cs.getCaseListAsString();
                        break;
                    }
                }
        }
        String[] caseArray = strCaseIds.split(" ");
        Set<String> targetCaseIds = new HashSet<String>(caseArray.length);
        for (String caseId : caseArray) {
            targetCaseIds.add(caseId);
        }
        return targetCaseIds;
    }
    
    private Set<GeneticProfile> getGeneticProfileSet(HttpServletRequest req, String cancerStudyId)
            throws ServletException, DaoException {
        Set<GeneticProfile> geneticProfileSet = new HashSet<GeneticProfile>();
        ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles(cancerStudyId);
        for (String geneticProfileIdsStr : req.getParameterValues(QueryBuilder.GENETIC_PROFILE_IDS)) {
            for (String profileId : geneticProfileIdsStr.split(" ")) {
                GeneticProfile profile = GeneticProfileUtil.getProfile(profileId, profileList);
                if( null != profile ){
                    geneticProfileSet.add(profile);
                }
            }
        }
        return geneticProfileSet;
    }
    
    private void addCGDSDataAsNodeAttribute(Node node, long entrezGeneId,
        Set<GeneticProfile> profiles, Set<String> targetCaseList, double zScoreThreshold) throws DaoException {
        Set<String> alteredCases = new HashSet<String>();
        
        for (GeneticProfile profile : profiles) {
            if (profile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED) {
                Set<String> cases = getMutatedCases(profile.getGeneticProfileId(),
                        targetCaseList, entrezGeneId);
                alteredCases.addAll(cases);
                node.setAttribute(NODE_ATTR_PERCENT_MUTATED, 1.0*cases.size()/targetCaseList.size());
            } else if (profile.getGeneticAlterationType() == GeneticAlterationType.COPY_NUMBER_ALTERATION) {
                Map<String,Set<String>> cnaCases = getCNACases(profile.getGeneticProfileId(),
                        targetCaseList, entrezGeneId);
                
                //AMP
                Set<String> cases = cnaCases.get("2");
                alteredCases.addAll(cases);
                node.setAttribute(NODE_ATTR_PERCENT_CNA_AMPLIFIED, 1.0*cases.size()/targetCaseList.size());
                
//                //GAINED
//                cases = cnaCases.get("1");
//                alteredCases.addAll(cases);
//                node.setAttribute(NODE_ATTR_PERCENT_CNA_GAINED, 1.0*cases.size()/targetCaseList.size());
//                
//                //HETLOSS
//                cases = cnaCases.get("-1");
//                alteredCases.addAll(cases);
//                node.setAttribute(NODE_ATTR_PERCENT_CNA_HET_LOSS, 1.0*cases.size()/targetCaseList.size());
                
                //HOMDEL
                cases = cnaCases.get("-2");
                alteredCases.addAll(cases);
                node.setAttribute(NODE_ATTR_PERCENT_CNA_HOM_DEL, 1.0*cases.size()/targetCaseList.size());
                
            } else if (profile.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION) {
                Set<String>[] cases = getMRnaAlteredCases(profile.getGeneticProfileId(),
                        targetCaseList, entrezGeneId, zScoreThreshold);
                alteredCases.addAll(cases[0]);
                alteredCases.addAll(cases[1]);
                node.setAttribute(NODE_ATTR_PERCENT_MRNA_WAY_UP, 1.0*cases[0].size()/targetCaseList.size());
                node.setAttribute(NODE_ATTR_PERCENT_MRNA_WAY_DOWN, 1.0*cases[1].size()/targetCaseList.size());
            }
        }

        node.setAttribute(NODE_ATTR_PERCENT_ALTERED, 1.0*alteredCases.size()/targetCaseList.size());
        
    }
    
    /**
     * 
     * @return mutated cases.
     */
    private Set<String> getMutatedCases(int geneticProfileId, Set<String> targetCaseList,
            long entrezGeneId) throws DaoException {
        ArrayList <ExtendedMutation> mutationList =
                    DaoMutation.getInstance().getMutations(geneticProfileId, targetCaseList, entrezGeneId);
        Set<String> cases = new HashSet<String>();
        for (ExtendedMutation mutation : mutationList) {
            cases.add(mutation.getCaseId());
        }
        
        return cases;
    }
    
    /**
     * 
     * @param geneticProfileId
     * @param targetCaseList
     * @param entrezGeneId
     * @return map from cna status to cases
     * @throws DaoException 
     */
    private Map<String,Set<String>> getCNACases(int geneticProfileId, Set<String> targetCaseList,
            long entrezGeneId) throws DaoException {
        Map<String,String> caseMap = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMap(geneticProfileId,entrezGeneId);
        caseMap.keySet().retainAll(targetCaseList);
        Map<String,Set<String>> res = new HashMap<String,Set<String>>();
        res.put("-2", new HashSet<String>());
        //res.put("-1", new HashSet<String>());
        //res.put("1", new HashSet<String>());
        res.put("2", new HashSet<String>());
        
        for (Map.Entry<String,String> entry : caseMap.entrySet()) {
            String cna = entry.getValue();
            if (cna.equals("2")||cna.equals("-2")) {
                String caseId = entry.getKey();
                res.get(cna).add(caseId);
            }
        }
        return res;
    }
    
    /**
     * 
     * @param geneticProfileId
     * @param targetCaseList
     * @param entrezGeneId
     * @return an array of two sets: first set contains up-regulated cases; second
     * contains down-regulated cases.
     * @throws DaoException 
     */
    private Set<String>[] getMRnaAlteredCases(int geneticProfileId, Set<String> targetCaseList,
            long entrezGeneId, double zScoreThreshold) throws DaoException {
        Map<String,String> caseMap = DaoGeneticAlteration.getInstance()
                .getGeneticAlterationMap(geneticProfileId,entrezGeneId);
        caseMap.keySet().retainAll(targetCaseList);
        Set<String>[] cases = new Set[2];
        cases[0] = new HashSet<String>();
        cases[1] = new HashSet<String>();
        
        for (Map.Entry<String,String> entry : caseMap.entrySet()) {
            String caseId = entry.getKey();
            double mrna = Double.parseDouble(entry.getValue());
            
            if (mrna>=zScoreThreshold) {
                cases[0].add(caseId);
            } else if (mrna<=-zScoreThreshold) {
                cases[1].add(caseId);
            }
        }
        
        return cases;
    }
    
    private String getNetworkServletUrl(HttpServletRequest req) {
        String geneListStr = req.getParameter(QueryBuilder.GENE_LIST);
        String geneticProfileIdsStr = req.getParameter(QueryBuilder.GENETIC_PROFILE_IDS);
        String cancerTypeId = req.getParameter(QueryBuilder.CANCER_STUDY_ID);
        String caseSetId = req.getParameter(QueryBuilder.CASE_SET_ID);
        String zscoreThreshold = req.getParameter(QueryBuilder.Z_SCORE_THRESHOLD);
        String netSrc = req.getParameter("netsrc");
        String netSize = req.getParameter("netsize");
        String nLinker = req.getParameter("linkers");
        
        return "network.do?"+QueryBuilder.GENE_LIST+"="+geneListStr
                +"&"+QueryBuilder.GENETIC_PROFILE_IDS+"="+geneticProfileIdsStr
                +"&"+QueryBuilder.CANCER_STUDY_ID+"="+cancerTypeId
                +"&"+QueryBuilder.CASE_SET_ID+"="+caseSetId
                +"&"+QueryBuilder.Z_SCORE_THRESHOLD+"="+zscoreThreshold
                +"&netsrc="+netSrc
                +"&netsize="+netSize
                +"&linkers="+nLinker;
    }
    
    private void writeXDebug(XDebug xdebug, HttpServletResponse res) 
            throws ServletException, IOException {
        PrintWriter writer = res.getWriter();
        writer.write("<!--xdebug messages begin:\n");
        for (Object msg : xdebug.getDebugMessages()) {
            writer.write(((org.mskcc.portal.util.XDebugMessage)msg).getMessage());
            writer.write("\n");
        }
        writer.write("xdebug messages end-->\n");
    }
    
    private void writeMsg(String msg, HttpServletResponse res) 
            throws ServletException, IOException {
        PrintWriter writer = res.getWriter();
        writer.write("<!--messages begin:\n");
        writer.write(msg);
        writer.write("messages end-->\n");
    }
    
    
}
