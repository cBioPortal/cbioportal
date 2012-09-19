
package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.dao.DaoGeneticAlteration;
import org.mskcc.cbio.cgds.dao.DaoInteraction;
import org.mskcc.cbio.cgds.dao.DaoMutation;
import org.mskcc.cbio.cgds.model.*;
import org.mskcc.cbio.portal.network.*;
import org.mskcc.cbio.portal.remote.GetCaseSets;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;
import org.mskcc.cbio.portal.util.CaseSetUtil;
import org.mskcc.cbio.portal.util.GeneticProfileUtil;
import org.mskcc.cbio.portal.util.SkinUtil;
import org.mskcc.cbio.portal.util.XDebug;

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
        try {
            String cmd = req.getParameter("cmd");
            if ("getdatasources".equalsIgnoreCase(cmd)) {
                res.setContentType("text/plain");
                List<String> dataSources = DaoInteraction.getInstance().getDataSources();
                res.getWriter().write(StringUtils.join(dataSources, "\n"));
            } else {
                processGetNetworkRequest(req, res);
            }
        } catch (Exception e) {
            //throw new ServletException (e);
            writeMsg("Error loading network. Please report this to "
                    + SkinUtil.getEmailContact()+ "!\n"+e.toString(), res);
            res.getWriter().write("");
        }
    }
    
    public void processGetNetworkRequest(HttpServletRequest req,
                      HttpServletResponse res)
            throws ServletException, IOException {
        try {
            StringBuilder messages = new StringBuilder();
            
            XDebug xdebug = new XDebug( req );
            
            String xd = req.getParameter("xdebug");
            boolean logXDebug = xd!=null && xd.equals("1");

            //  Get User Defined Gene List
            String geneListStr = req.getParameter(QueryBuilder.GENE_LIST);
            Set<String> queryGenes = new HashSet<String>(Arrays.asList(geneListStr.toUpperCase().split(" ")));
            int nMiRNA = filterNodes(queryGenes);
            if (nMiRNA>0) {
                messages.append("MicroRNAs were excluded from the network query. ");
            }

            //String geneticProfileIdSetStr = xssUtil.getCleanInput (req, QueryBuilder.GENETIC_PROFILE_IDS);

            String netSrc = req.getParameter("netsrc");
            String dsSrc = req.getParameter("source");
            List<String> dataSources = dsSrc == null ? null :
                    Arrays.asList(dsSrc.split("[, ]+"));
            
            Network network;
            xdebug.startTimer();
            if ("cpath2".equalsIgnoreCase(netSrc)) {
                network = NetworkIO.readNetworkFromCPath2(queryGenes, true);
                if (logXDebug) {
                    xdebug.logMsg("GetPathwayCommonsNetwork", "<a href=\""+NetworkIO.getCPath2URL(queryGenes)
                            +"\" target=\"_blank\">cPath2 URL</a>");
                }
            } else {
                network = NetworkIO.readNetworkFromCGDS(queryGenes, dataSources, true);
            }
            
            int nBefore = network.countNodes();
            int querySize = queryGenes.size();
            
            String netSize = req.getParameter("netsize");
            boolean topologyPruned = pruneNetwork(network,netSize);
            
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
                messages.append(") neighbor genes that interact with at least two query genes.\n");
            }
            
            xdebug.stopTimer();
            xdebug.logMsg(this, "Successfully retrieved networks from " + netSrc
                    + ": took "+xdebug.getTimeElapsed()+"ms");
                
            // get cancer study id
            // if cancer study id is null, return the current network
            String cancerTypeId = req.getParameter(QueryBuilder.CANCER_STUDY_ID);

            if (network.countNodes()!=0 && cancerTypeId!=null) {
            
                // add attribute is_query to indicate if a node is in query genes
                // and get the list of genes in network
                xdebug.logMsg(this, "Retrieving data from CGDS...");
                
                // Get case ids
                Set<String> targetCaseIds = getCaseIds(req, cancerTypeId);
                
                //  Get User Selected Genetic Profiles
                Set<GeneticProfile> geneticProfileSet = getGeneticProfileSet(req, cancerTypeId);
                
                // getzScoreThreshold
                double zScoreThreshold = Double.parseDouble(req.getParameter(QueryBuilder.Z_SCORE_THRESHOLD));

                xdebug.startTimer();
                
                Map<String,Map<String,Integer>> mapQueryGeneAlterationCaseNumber 
                        = getMapQueryGeneAlterationCaseNumber(req);
                
                DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
                
                Set<Node> queryNodes = new HashSet<Node>();
                for (Node node : network.getNodes()) {
                    if(node.getType().equals(NodeType.DRUG))
                        continue;

                    String ngnc = NetworkUtils.getSymbol(node);
                    if (ngnc==null) {
                        continue;
                    }
                    
                    if (mapQueryGeneAlterationCaseNumber!=null) {
                        if (queryGenes.contains(ngnc)) {
                            queryNodes.add(node);
                            continue;
                        }
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
                
                if (mapQueryGeneAlterationCaseNumber!=null) {
                    addAttributesForQueryGenes(queryNodes, targetCaseIds.size(), mapQueryGeneAlterationCaseNumber);
                }
                
                String nLinker = req.getParameter("linkers");
                if (!topologyPruned && nLinker!=null && nLinker.matches("[0-9]+")) {
                    String strDiffusion = req.getParameter("diffusion");
                    double diffusion;
                    try {
                        diffusion = Double.parseDouble(strDiffusion);
                    } catch (Exception ex) {
                        diffusion = 0;
                    }
                    
                    xdebug.startTimer();
                    pruneNetworkByAlteration(network, diffusion, Integer.parseInt(nLinker), querySize);
                    nAfter = network.countNodes();
                    if (nBefore!=nAfter) {
                        messages.append("The network below contains ");
                        messages.append(nAfter);
                        messages.append(" nodes, including your ");
                        messages.append(querySize);
                        messages.append(" query gene");
                        if (querySize>1) {
                            messages.append("s");
                        }
                        messages.append(" and the ");
                        messages.append(nAfter-querySize);
                        messages.append(" most frequently altered neighbor genes ");
                        messages.append(" (out of a total of ");
                        messages.append(nBefore-querySize);
                        messages.append(").\n");
                    }
                    xdebug.stopTimer();
                    xdebug.logMsg(this, "Prune network. Took "+xdebug.getTimeElapsed()+"ms");
                }

                String encodedQueryAlteration = encodeQueryAlteration(mapQueryGeneAlterationCaseNumber);

                if (logXDebug) {
                    xdebug.logMsg(this, "<a href=\""+getNetworkServletUrl(req, false, 
                            false, false, encodedQueryAlteration)
                            +"\" target=\"_blank\">NetworkServlet URL</a>");
                }
                
                messages.append("Download the complete network in ");
                messages.append("<a href=\"");
                messages.append(getNetworkServletUrl(req, true, true, false, encodedQueryAlteration));
                messages.append("\">GraphML</a> ");
                messages.append("or <a href=\"");
                messages.append(getNetworkServletUrl(req, true, true, true, encodedQueryAlteration));
                messages.append("\">SIF</a>");
                messages.append(" for import into <a href=\"http://cytoscape.org\" target=\"_blank\">Cytoscape</a>");
                messages.append(" (<a href=\"http://chianti.ucsd.edu/cyto_web/plugins/displayplugininfo.php?");
                messages.append("name=GraphMLReader\" target=\"_blank\">GraphMLReader plugin</a>");
                messages.append(" is required for importing GraphML).");
            }
            
            String format = req.getParameter("format");
            boolean sif = format!=null && format.equalsIgnoreCase("sif");

            String download = req.getParameter("download");
            if (download!=null && download.equalsIgnoreCase("on")) {
                res.setContentType("application/octet-stream");
                res.addHeader("content-disposition","attachment; filename=cbioportal."+(sif?"sif":"graphml"));
                messages.append("In order to open this file in Cytoscape, please install GraphMLReader plugin.");
            } else {
                res.setContentType("text/"+(sif?"plain":"xml"));
            }
            
            String gzip = req.getParameter("gzip");
            boolean isGzip = gzip != null && gzip.equalsIgnoreCase("on");
            if (isGzip) {
                res.setHeader("Content-Encoding", "gzip");
            }
            
            NetworkIO.NodeLabelHandler nodeLabelHandler = new NetworkIO.NodeLabelHandler() {
                // using HGNC gene symbol as label if available
                public String getLabel(Node node) {
                    if(node.getType().equals(NodeType.DRUG))
                        return (String) node.getAttribute("NAME");

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
            };
            
            String graph;
            if (sif) {
                graph = NetworkIO.writeNetwork2Sif(network, nodeLabelHandler);
            } else {
                graph = NetworkIO.writeNetwork2GraphML(network,nodeLabelHandler);
            }
            
            if (logXDebug) {
                writeXDebug(xdebug, res);
            }
            
            String msgoff = req.getParameter("msgoff");
            if ((msgoff==null || !msgoff.equals("t")) && messages.length()>0) {
                writeMsg(messages.toString(), res);
            }
            
            if (isGzip) {
                GZIPOutputStream out = new GZIPOutputStream(res.getOutputStream());
                out.write(graph.getBytes());
                out.close();
            } else {
                PrintWriter writer = res.getWriter();
                writer.write(graph);
                writer.close();
            }
        } catch (Exception e) {
            //throw new ServletException (e);
            writeMsg("Error loading network. Please report this to "
                    + SkinUtil.getEmailContact()+ "!\n"+e.toString(), res);
            res.getWriter().write("<graphml></graphml>");
        }
    }
    
    private int filterNodes(Set<String> queryGenes) {
        int n = 0;
        try {
            DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
            for (Iterator<String> it = queryGenes.iterator(); it.hasNext();) {
                String symbol = it.next();
                CanonicalGene gene = daoGeneOptimized.getGene(symbol);
                if (gene.isMicroRNA() || gene.isPhosphoProtein()) {
                    it.remove();
                    n++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return n;
    }
    
    private boolean pruneNetwork(Network network, String netSize) {
        if ("small".equals(netSize)) {
            NetworkUtils.pruneNetwork(network, new NetworkUtils.NodeSelector() {
                public boolean select(Node node) {
                    return !isInQuery(node) || !node.getType().equals(NodeType.DRUG);
                }
            });
            return true;
        } else if ("medium".equals(netSize)) {
            NetworkUtils.pruneNetwork(network, new NetworkUtils.NodeSelector() {
                public boolean select(Node node) {
                    String inMedium = (String)node.getAttribute("IN_MEDIUM");
                    return inMedium==null || !inMedium.equals("true") || !node.getType().equals(NodeType.DRUG);
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
    private void pruneNetworkByAlteration(Network network, double diffusion, int nKeep, int nQuery) {
        if (network.countNodes() <= nKeep + nQuery) {
            return;
        }
        
        List<Node> nodesToRemove = getNodesToRemove(network, diffusion, nKeep);
        
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
    private List<Node> getNodesToRemove(final Network network, final double diffusion, final int n) {
        final Map<Node,Double> mapDiffusion = getMapDiffusedTotalAlteredPercentage(network, diffusion);
        
        // keep track of the top nKeep
        PriorityQueue<Node> topAlteredNodes = new PriorityQueue<Node>(n,
                new Comparator<Node>() {
                    public int compare(Node n1, Node n2) {
                        int ret = mapDiffusion.get(n1).compareTo(mapDiffusion.get(n2));
                        if (diffusion!=0 && ret==0) { // if the same diffused perc, use own perc
                            ret = Double.compare(getTotalAlteredPercentage(n1),
                                    getTotalAlteredPercentage(n2));
                        }
                            
                        if (ret==0) { // if the same, rank according to degree
                            ret = network.getDegree(n1) - network.getDegree(n2);
                        }
                        
                        return ret;
                    }
                });
        
        List<Node> nodesToRemove = new ArrayList<Node>();
        for (Node node : network.getNodes()) {
            if (isInQuery(node) || node.getType().equals(NodeType.DRUG)) {
                continue;
            }
            
            if (topAlteredNodes.size()<n) {
                topAlteredNodes.add(node);
            } else {
                if (n==0) {
                    nodesToRemove.add(node);
                } else {
                    if (mapDiffusion.get(node) > mapDiffusion.get(topAlteredNodes.peek())) {
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
    
    private Map<Node,Double> getMapDiffusedTotalAlteredPercentage(Network network, double diffusion) {
        Map<Node,Double> map = new HashMap<Node,Double>();
        for (Node node : network.getNodes()) {
            map.put(node, getDiffusedTotalAlteredPercentage(network,node,diffusion));
        }
        return map;
    }
    
    private double getDiffusedTotalAlteredPercentage(Network network, Node node, double diffusion) {
        double alterPerc = getTotalAlteredPercentage(node);
        if (diffusion==0) {
            return alterPerc;
        }
        
        for (Node neighbor : network.getNeighbors(node)) {
            double diffused = diffusion * getTotalAlteredPercentage(neighbor);
            if (diffused > alterPerc) {
                alterPerc = diffused;
            }
        }
        
        return alterPerc;
    }
    
    private double getTotalAlteredPercentage(Node node) {
        Double alterPerc = (Double)node.getAttribute(NODE_ATTR_PERCENT_ALTERED);
        return alterPerc == null ? 0.0 : alterPerc;
    }
    
    private Set<String> getCaseIds(HttpServletRequest req, String cancerStudyId) 
            throws ServletException, DaoException {
    	String caseIdsKey = req.getParameter(QueryBuilder.CASE_IDS_KEY);
    	String strCaseIds = CaseSetUtil.getCaseIds(caseIdsKey);
    	
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
        //String[] caseArray = strCaseIds.split(" ");
        String[] caseArray = strCaseIds.split("\\s+");
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
    
    private void addAttributesForQueryGenes(Set<Node> nodes, int nCases,
            Map<String,Map<String,Integer>> mapQueryGeneAlterationCaseNumber) {
        for (Node node : nodes) {
            String symbol = NetworkUtils.getSymbol(node);
            Map<String,Integer> mapAltrationCaseNumber = mapQueryGeneAlterationCaseNumber.get(symbol);
            for (Map.Entry<String,Integer> entry : mapAltrationCaseNumber.entrySet()) {
                node.setAttribute(mapHeatMapKeyToAttrName.get(entry.getKey()), 1.0*entry.getValue()/nCases);
            }
        }
    }
    
    private Map<String,Map<String,Integer>> getMapQueryGeneAlterationCaseNumber(HttpServletRequest req) {
        String geneAlt = req.getParameter("query_alt");
        if (geneAlt!=null) {
            return decodeQueryAlteration(geneAlt);
        }
        
        String heatMap = req.getParameter("heat_map");
        if (heatMap!=null) {
            Map<String,Map<String,Integer>> mapQueryGeneAlterationCaseNumber 
                    = new HashMap<String,Map<String,Integer>>();
            String[] heatMapLines = heatMap.split("\r?\n");
            String[] genes = heatMapLines[0].split("\t");

            for (int i=1; i<genes.length; i++) {
                Map<String,Integer> map = new HashMap<String,Integer>();
                map.put("Any", 0);
                mapQueryGeneAlterationCaseNumber.put(genes[i], map);
            }

            for (int i=1; i<heatMapLines.length; i++) {
                String[] strs = heatMapLines[i].split("\t");
                for (int j=1; j<strs.length; j++) {
                    Map<String,Integer> map = mapQueryGeneAlterationCaseNumber.get(genes[j]);
                    if (!strs[j].isEmpty()) {
                        map.put("Any", map.get("Any")+1);
                    }

                    for (String type : strs[j].split(";")) {
                        type = type.trim();
                        if (type.isEmpty()) {
                            continue;
                        }
                        
                        // add to specific type
                        Integer num = map.get(type);
                        if (num==null) {
                            map.put(type, 1);
                        } else {
                            map.put(type, num+1);
                        }
                    }
                }
            }
            
            return mapQueryGeneAlterationCaseNumber;
        }
        
        return null;
    }
    
    private static final Map<String,String> mapHeatMapKeyToAttrName;
    static {
        mapHeatMapKeyToAttrName = new HashMap<String,String>();
        mapHeatMapKeyToAttrName.put("Any", NetworkServlet.NODE_ATTR_PERCENT_ALTERED);
        mapHeatMapKeyToAttrName.put("AMP", NetworkServlet.NODE_ATTR_PERCENT_CNA_AMPLIFIED);
        mapHeatMapKeyToAttrName.put("GAIN", NetworkServlet.NODE_ATTR_PERCENT_CNA_GAINED);
        mapHeatMapKeyToAttrName.put("HETLOSS", NetworkServlet.NODE_ATTR_PERCENT_CNA_HET_LOSS);
        mapHeatMapKeyToAttrName.put("HOMDEL", NetworkServlet.NODE_ATTR_PERCENT_CNA_HOM_DEL);
        mapHeatMapKeyToAttrName.put("DOWN", NetworkServlet.NODE_ATTR_PERCENT_MRNA_WAY_DOWN);
        mapHeatMapKeyToAttrName.put("UP", NetworkServlet.NODE_ATTR_PERCENT_MRNA_WAY_UP);
        mapHeatMapKeyToAttrName.put("MUT", NetworkServlet.NODE_ATTR_PERCENT_MUTATED);
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
                if (!cases.isEmpty()) {
                    alteredCases.addAll(cases);
                    node.setAttribute(NODE_ATTR_PERCENT_CNA_AMPLIFIED, 1.0*cases.size()/targetCaseList.size());
                }
                
//                //GAINED
//                cases = cnaCases.get("1");
//                if (!cases.isEmpty()) {
//                alteredCases.addAll(cases);
//                node.setAttribute(NODE_ATTR_PERCENT_CNA_GAINED, 1.0*cases.size()/targetCaseList.size());
//                }
//                
//                //HETLOSS
//                cases = cnaCases.get("-1");
//                if (!cases.isEmpty()) {
//                alteredCases.addAll(cases);
//                node.setAttribute(NODE_ATTR_PERCENT_CNA_HET_LOSS, 1.0*cases.size()/targetCaseList.size());
//                }
                
                //HOMDEL
                cases = cnaCases.get("-2");
                if (!cases.isEmpty()) {
                    alteredCases.addAll(cases);
                    node.setAttribute(NODE_ATTR_PERCENT_CNA_HOM_DEL, 1.0*cases.size()/targetCaseList.size());
                }
                
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
            double mrna;
            try {
                mrna = Double.parseDouble(entry.getValue());
            } catch (Exception e) {
                continue;
            }
            
            if (mrna>=zScoreThreshold) {
                cases[0].add(caseId);
            } else if (mrna<=-zScoreThreshold) {
                cases[1].add(caseId);
            }
        }
        
        return cases;
    }
    
    private Map<String,Map<String,Integer>> decodeQueryAlteration(String strQueryAlteration) {
        if (strQueryAlteration==null || strQueryAlteration.isEmpty()) {
            return null;
        }
        
        Map<String,Map<String,Integer>> ret = new HashMap<String,Map<String,Integer>>();
        try {
            String[] genes = strQueryAlteration.split(";");
            for (String perGene : genes) {
                int ix = perGene.indexOf(":");
                String gene = perGene.substring(0, ix);
                Map<String,Integer> map = new HashMap<String,Integer>();
                ret.put(gene, map);
                
                String[] alters = perGene.substring(ix+1).split(",");
                for (String alter : alters) {
                    String[] parts = alter.split(":");
                    map.put(parts[0], Integer.valueOf(parts[1]));
                }
            }
            return ret;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String encodeQueryAlteration(Map<String,Map<String,Integer>> queryAlteration) {
        if (queryAlteration==null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,Map<String,Integer>> entry1 : queryAlteration.entrySet()) {
            sb.append(entry1.getKey());
            sb.append(':');
            for (Map.Entry<String,Integer> entry2 : entry1.getValue().entrySet()) {
                sb.append(entry2.getKey());
                sb.append(':');
                sb.append(entry2.getValue());
                sb.append(',');
            }
            sb.setCharAt(sb.length()-1, ';');
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    
    private String getNetworkServletUrl(HttpServletRequest req, boolean complete, 
            boolean download, boolean sif, String strQueryAlteration) {
        String geneListStr = req.getParameter(QueryBuilder.GENE_LIST);
        String geneticProfileIdsStr = req.getParameter(QueryBuilder.GENETIC_PROFILE_IDS);
        String cancerTypeId = req.getParameter(QueryBuilder.CANCER_STUDY_ID);
        String caseSetId = req.getParameter(QueryBuilder.CASE_SET_ID);
        String zscoreThreshold = req.getParameter(QueryBuilder.Z_SCORE_THRESHOLD);
        String netSrc = req.getParameter("netsrc");
        String netSize = req.getParameter("netsize");
        String nLinker = req.getParameter("linkers");
        String strDiffusion = req.getParameter("diffusion");
        
        String ret = "network.do?"+QueryBuilder.GENE_LIST+"="+geneListStr
                +"&"+QueryBuilder.GENETIC_PROFILE_IDS+"="+geneticProfileIdsStr
                +"&"+QueryBuilder.CANCER_STUDY_ID+"="+cancerTypeId
                +"&"+QueryBuilder.CASE_SET_ID+"="+caseSetId
                +"&"+QueryBuilder.Z_SCORE_THRESHOLD+"="+zscoreThreshold
                +"&netsrc="+netSrc
                +"&msgoff=t";
        
        if (strQueryAlteration!=null) {
            
            ret += "&query_alt="+strQueryAlteration;
        }
        
        if (!complete) {
            ret += "&netsize=" + netSize 
                + "&linkers=" + nLinker
                +"&diffusion="+strDiffusion;
        }
        
        if (download) {
            ret += "&download=on";
        }
        
        if (sif) {
            ret += "&format=sif";
        }
         
        return ret;
    }
    
    private void writeXDebug(XDebug xdebug, HttpServletResponse res) 
            throws ServletException, IOException {
        PrintWriter writer = res.getWriter();
        writer.write("<!--xdebug messages begin:\n");
        for (Object msg : xdebug.getDebugMessages()) {
            writer.write(((org.mskcc.cbio.portal.util.XDebugMessage)msg).getMessage());
            writer.write("\n");
        }
        writer.write("xdebug messages end-->\n");
    }
    
    private void writeMsg(String msg, HttpServletResponse res) 
            throws ServletException, IOException {
        PrintWriter writer = res.getWriter();
        writer.write("<!--messages begin:\n");
        writer.write(msg);
        writer.write("\nmessages end-->\n");
    }
    
    
}
