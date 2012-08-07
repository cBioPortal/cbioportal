
package org.mskcc.cbio.portal.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.mskcc.cbio.cgds.dao.*;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Drug;
import org.mskcc.cbio.cgds.model.DrugInteraction;
import org.mskcc.cbio.cgds.model.Interaction;
import org.mskcc.cbio.cgds.scripts.drug.AbstractDrugInfoImporter;
import org.mskcc.cbio.portal.remote.ConnectionManager;
import org.mskcc.cbio.portal.util.GlobalProperties;

/**
 *
 * @author jj
 */
public final class NetworkIO {
    
    /**
     * private constructor for utility class.
     */
    private NetworkIO(){}
    
    /**
     * Interface for get label from a node
     */
    public static interface NodeLabelHandler {
        /**
         * 
         * @param node a node
         * @return label for the node
         */
        String getLabel(Node node);
    }
    
    public static String getCPath2URL(Set<String> genes) {
        StringBuilder sbUrl = new StringBuilder(GlobalProperties.getPathwayCommonsUrl());
			sbUrl.append("/graph?format=EXTENDED_BINARY_SIF&kind=NEIGHBORHOOD");
        for (String gene : genes) {
            sbUrl.append("&source=urn:biopax:RelationshipXref:HGNC_");
            sbUrl.append(gene.toUpperCase());
        }
        
        return sbUrl.toString();
    }
    
    public static Network readNetworkFromCPath2(Set<String> genes, boolean removeSelfEdge) 
            throws DaoException, IOException {
        String cPath2Url = getCPath2URL(genes);
        
        MultiThreadedHttpConnectionManager connectionManager =
                ConnectionManager.getConnectionManager();
        HttpClient client = new HttpClient(connectionManager);

        GetMethod method = new GetMethod(cPath2Url.toString());
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                Network network = readNetworkFromCPath2(method.getResponseBodyAsStream(), true);
                Set<Node> seedNodes = addMissingGenesAndReturnSeedNodes(network, genes);
                classifyNodes(network, seedNodes);
                return network;
            } else {
                //  Otherwise, throw HTTP Exception Object
                throw new HttpException(statusCode + ": " + HttpStatus.getStatusText(statusCode)
                        + " Base URL:  " + cPath2Url);
            }            

        } finally {
            //  Must release connection back to Apache Commons Connection Pool
            method.releaseConnection();
        }
    }
    
    /**
     * Read a network from extended SIF of cPath2
     * @param isSif input stream of SIF
     * @return a network
     * @throws IOException if connection failed
     */
    public static Network readNetworkFromCPath2(InputStream isSif, boolean removeSelfEdge) throws IOException {
        Network network = new Network();
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(isSif));

        // read edges
        String line = bufReader.readLine();
        if (!line.startsWith("PARTICIPANT_A\tINTERACTION_TYPE\tPARTICIPANT_B")) {// if empty
            return network;
        }

        String[] edgeHeaders = line.split("\t");
        ArrayList<String> edgeLines = new ArrayList<String>();
        for (line = bufReader.readLine(); !line.isEmpty(); line = bufReader.readLine()) {
            edgeLines.add(line);
        }

        // read nodes
        line = bufReader.readLine();
        if (!line.startsWith("PARTICIPANT\tPARTICIPANT_TYPE\tPARTICIPANT_NAME\t"
                + "UNIFICATION_XREF\tRELATIONSHIP_XREF")) {
            System.err.print("cPath2 format changed.");
            //return network;
        }

        String[] nodeHeaders = line.split("\t");
        for (line = bufReader.readLine(); line!=null && !line.isEmpty(); line = bufReader.readLine()) {
            String[] strs = line.split("\t");
            Node node = new Node(strs[0]);
            for (int i=1; i<strs.length && i<nodeHeaders.length; i++) {
                if (nodeHeaders[i].equals("PARTICIPANT_TYPE")) {
                    NodeType type = NodeType.getByCpath2Keyword(strs[i]);
                    node.setType(type);
                } else {
                    node.setAttribute(nodeHeaders[i], strs[i]);
                }
            }

            network.addNode(node);
        }

        // add edges
        for (String edgeLine : edgeLines) {
            String[] strs = edgeLine.split("\t");

            if (strs.length<3) {// sth. is wrong
                continue;
            }

            if (removeSelfEdge && strs[0].equals(strs[2])) {
                continue;
            }

            String interaction = strs[1];
            boolean isDirect = isEdgeDirected(interaction);
            Edge edge = new Edge(isDirect, interaction);

            for (int i=3; i<strs.length&&i<edgeHeaders.length; i++) {
                if (edgeHeaders[i].equals("INTERACTION_PUBMED_ID")
                        && !strs[i].startsWith("PubMed:")) {
                    //TODO: REMOVE THIS CHECK AFTER THE CPATH2 PUBMED ISSUE IS FIXED
                    continue;
                }

                edge.addAttribute(edgeHeaders[i], strs[i]);
            }
            network.addEdge(edge, strs[0], strs[2]);
        }
        
        NetworkUtils.mergeNodesWithSameSymbol(network);

        return network;
    }
    
    private static boolean isEdgeDirected(String interaction) {
        if (interaction==null) {
            return false;
        }

        if (interaction.equals(AbstractDrugInfoImporter.DRUG_INTERACTION_TYPE)) {
            return true;
        }
        
        if (interaction.equals("COMPONENT_OF")) {
            return true;
        }
        
        if (interaction.equals("CO_CONTROL")) {
            return false;
        }
        
        if (interaction.equals("INTERACTS_WITH")) {
            return false;
        }
        
        if (interaction.equals("IN_SAME_COMPONENT")) {
            return false;
        }
        
        if (interaction.equals("METABOLIC_CATALYSIS")) {
            return true;
        }
        
        if (interaction.equals("METABOLIC_CATALYSIS")) {
            return false;
        }
        
        if (interaction.equals("SEQUENTIAL_CATALYSIS")) {
            return true;
        }
        
        if (interaction.equals("STATE_CHANGE")) {
            return true;
        }
        
        if (interaction.equals("GENERIC_OF")) {
            return true;
        }
        
        return false;        
    }
    
    /**
     * Read network in CGDS database
     * @param genes
     * @return
     * @throws Exception 
     */
    public static Network readNetworkFromCGDS(Set<String> genes, boolean removeSelfEdge) throws DaoException {
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        Map<Long,String> entrezHugoMap = getEntrezHugoMap(genes);
        Set<Long> seedGenes = new HashSet<Long>(entrezHugoMap.keySet());
        List<Interaction> interactionList = daoInteraction.getInteractions(seedGenes);
        Network net = new Network();
        for (Interaction interaction : interactionList) {
            long geneA = interaction.getGeneA();
            long geneB = interaction.getGeneB();
            if (removeSelfEdge && geneA == geneB) {
                continue;
            }
            
            String geneAID = Long.toString(geneA);
            String geneBID = Long.toString(geneB);
            
            addNode(net, geneAID, entrezToHugo(entrezHugoMap,geneA));
            addNode(net, geneBID, entrezToHugo(entrezHugoMap,geneB));
            
            String interactionType = interaction.getInteractionType();
            String pubmed = interaction.getPmids();
            String source = interaction.getSource();
            String exp = interaction.getExperimentTypes();
            boolean isDirected = isEdgeDirected(interactionType); //TODO: how about HPRD
            Edge edge = new Edge(isDirected, interactionType);
            if (pubmed!=null) {
                edge.addAttribute("INTERACTION_PUBMED_ID", pubmed);
            }
            if (source!=null) {
                edge.addAttribute("INTERACTION_DATA_SOURCE", source);
            }
            if (exp!=null) {
                edge.addAttribute("EXPERIMENTAL_TYPE", exp);
            }

            net.addEdge(edge, geneAID, geneBID);
        }

        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        DaoDrug daoDrug = DaoDrug.getInstance();
        for(DrugInteraction interaction: daoDrugInteraction.getInteractions(seedGenes)) {
            String drugID = interaction.getDrug();
            Long targetGene = interaction.getTargetGene();
            String geneID = Long.toString(targetGene);

            addDrugNode(net, daoDrug.getDrug(drugID));
            addNode(net, geneID, entrezToHugo(entrezHugoMap, targetGene));

            String interactionType = interaction.getInteractionType();
            String pubmed = interaction.getPubMedIDs();
            String source = interaction.getDataSource();

            String exp = interaction.getExperimentTypes();
            boolean isDirected = isEdgeDirected(interactionType);
            Edge edge = new Edge(isDirected, interactionType);

            if (pubmed!=null) {
                edge.addAttribute("INTERACTION_PUBMED_ID", pubmed);
            }
            if (source!=null) {
                edge.addAttribute("INTERACTION_DATA_SOURCE", source);
            }
            if (exp!=null) {
                edge.addAttribute("EXPERIMENTAL_TYPE", exp);
            }

            net.addEdge(edge, drugID, geneID);
        }
        
        Set<Node> seedNodes = addMissingGenesAndReturnSeedNodes(net, genes);
        classifyNodes(net, seedNodes);
        
        return net;
    }
    
    private static Set<Node> addMissingGenesAndReturnSeedNodes(Network net, Set<String> seedGenes)
            throws DaoException {
        Set<Node> seedNodes = new HashSet<Node>(seedGenes.size());
        Set<String> missingGenes = new HashSet<String>(seedGenes);
        for (Node node : net.getNodes()) {
            String symbol = NetworkUtils.getSymbol(node);
            if (missingGenes.remove(symbol)) {
                seedNodes.add(node);
            }
        }
        
        Map<Long,String> entrezHugoMap = getEntrezHugoMap(missingGenes);
        for (Map.Entry<Long,String> entry : entrezHugoMap.entrySet()) {
            Node node = addNode(net, entry.getKey().toString(), entry.getValue());
            seedNodes.add(node);
        }
        
        return seedNodes;
    }
    
    private static void classifyNodes(Network net, Set<Node> seedNodes) {
        for (Node seed : seedNodes) {
            seed.setAttribute("IN_QUERY", "true");
            //seed.setAttribute("IN_MEDIUM", "true");
        }
        
        for (Node node:  net.getNodes()) {
            if (seedNodes.contains(node)) {
                continue;
            }

            node.setAttribute("IN_QUERY", "false"); //TODO: remove this

////            if (seedNodes.size()==1) {
////                // mark linker nodes that has degree of 2 or more
////                if (net.getDegree(node)>=2) {
////                    node.addAttribute("IN_MEDIUM", "true");
////                }
////            } else {
//                //  mark linker nodes that links to at least 2 seed genes
//                int seedDegree = 0;
//                for (Node neighbor : net.getNeighbors(node)) {
//                    if (seedNodes.contains(neighbor)) {
//                        if (++seedDegree >= 2) {
//                            node.setAttribute("IN_MEDIUM", "true");
//                            break;
//                        }
//                    }
//                }
////            }
        }
    }
    
    private static Node addNode(Network net, String entrez, String hugo) {
        Node node = net.getNodeById(entrez);
        if (node != null) {
            return node;
        }

        node = new Node(entrez);
        node.setType(NodeType.PROTEIN);
        node.setAttribute("RELATIONSHIP_XREF", "HGNC:"+hugo+";Entrez Gene:"+entrez);
        net.addNode(node);
        return node;
    }

    private static Node addDrugNode(Network net, Drug drug) throws DaoException {
        Node node = net.getNodeById(drug.getId());
        if (node != null) {
            return node;
        }

        node = new Node(drug.getId());
        node.setType(NodeType.DRUG);
        node.setAttribute("NAME", drug.getName());
        node.setAttribute("RELATIONSHIP_XREF", drug.getResource() + ":" + drug.getId());
        node.setAttribute("ATC_CODE", drug.getATCCode());
        node.setAttribute("FDA_APPROVAL", drug.isApprovedFDA() + "");
        node.setAttribute("DESCRIPTION", drug.getDescription());
        node.setAttribute("SYNONYMS", drug.getSynonyms());
        node.setAttribute("TARGETS", createDrugTargetList(drug));

        net.addNode(node);
        return node;
    }

    private static String createDrugTargetList(Drug drug) throws DaoException {
        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        String targets = "";

        for (DrugInteraction interaction : daoDrugInteraction.getTargets(drug)) {
            CanonicalGene gene = daoGeneOptimized.getGene(interaction.getTargetGene());
            targets += gene.getStandardSymbol() + ";";
        }
        if(targets.length() > 0)
            targets = targets.substring(0, targets.length()-1);

        return targets;
    }

    private static Map<Long,String> getEntrezHugoMap(Set<String> genes) throws DaoException {
        Map<Long,String> map = new HashMap<Long,String>(genes.size());
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        for (String gene : genes) {
            CanonicalGene cGene = daoGeneOptimized.getGene(gene);
            if (cGene!=null) {
                map.put(cGene.getEntrezGeneId(),gene.toUpperCase());
            }
        }
        return map;
    }
    
    private static String entrezToHugo(Map<Long,String> mapEntrezHugo, long entrez) throws DaoException {
        String hugo = mapEntrezHugo.get(entrez);
        if (hugo==null) {
            hugo = DaoGeneOptimized.getInstance().getGene(entrez).getHugoGeneSymbolAllCaps();
            mapEntrezHugo.put(entrez, hugo);
        }
        return hugo;
    }
    
    /**
     * Write network to SIF format
     * @param network network to write
     * @param nlh 
     * @return a string in SIF format
     */    
    public static String writeNetwork2Sif(Network network, NodeLabelHandler nlh) {
        StringBuilder sb = new StringBuilder();
        
        for (Edge edge : network.getEdges()) {
            Node[] nodes = network.getNodes(edge);
            sb.append(nlh.getLabel(nodes[0]));
            sb.append("\t");
            sb.append(edge.getInteractionType());
            sb.append("\t");
            sb.append(nlh.getLabel(nodes[1]));
            sb.append("\n");
        }
        
        return sb.toString();   
    }
    
    /**
     * Write network to GraphML format
     * @param network network to write
     * @param nlh 
     * @return a tring in GraphML format
     */    
    public static String writeNetwork2GraphML(Network network, NodeLabelHandler nlh) {
        Map<String,String> mapNodeAttrNameType = new HashMap<String,String>();
        Map<String,String> mapEdgeAttrNameType = new HashMap<String,String>();
        
        StringBuilder sbNodeEdge = new StringBuilder();
        
        for (Node node : network.getNodes()) {
            sbNodeEdge.append("  <node id=\"");
            sbNodeEdge.append(node.getId());
            sbNodeEdge.append("\">\n");
            sbNodeEdge.append("   <data key=\"label\">");
            sbNodeEdge.append(nlh.getLabel(node));
            sbNodeEdge.append("</data>\n");
            
            sbNodeEdge.append("   <data key=\"type\">");
            sbNodeEdge.append(node.getType().toString());
            sbNodeEdge.append("</data>\n");
            
            exportAttributes(node.getAttributes(),sbNodeEdge,mapNodeAttrNameType);
            sbNodeEdge.append("  </node>\n");
        }
        
        for (Edge edge : network.getEdges()) {
            Node[] nodes = network.getNodes(edge);
            sbNodeEdge.append("  <edge source=\"");
            sbNodeEdge.append(nodes[0].getId());
            sbNodeEdge.append("\" target=\"");
            sbNodeEdge.append(nodes[1].getId());
            sbNodeEdge.append("\" directed=\"");
            sbNodeEdge.append(Boolean.toString(edge.isDirected()));
            sbNodeEdge.append("\">\n");
            
            sbNodeEdge.append("   <data key=\"type\">");
            sbNodeEdge.append(edge.getInteractionType());
            sbNodeEdge.append("</data>\n");
            
            exportAttributes(edge.getAttributes(),sbNodeEdge,mapEdgeAttrNameType);
            sbNodeEdge.append("  </edge>\n");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<graphml>\n");
        sb.append(" <key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n");
        sb.append(" <key id=\"type\" for=\"all\" attr.name=\"type\" attr.type=\"string\"/>\n");
        
        for (Map.Entry<String,String> entry : mapNodeAttrNameType.entrySet()) {
            sb.append(" <key id=\"")
              .append(entry.getKey())
              .append("\" for=\"node\" attr.name=\"")
              .append(entry.getKey())
              .append("\" attr.type=\"")
              .append(entry.getValue())
              .append("\"/>\n");
        }
        
        for (Map.Entry<String,String> entry : mapEdgeAttrNameType.entrySet()) {
            sb.append(" <key id=\"")
              .append(entry.getKey())
              .append("\" for=\"edge\" attr.name=\"")
              .append(entry.getKey())
              .append("\" attr.type=\"")
              .append(entry.getValue())
              .append("\"/>\n");
        }
        
        sb.append(" <graph edgedefault=\"undirected\">\n");        
        sb.append(sbNodeEdge);
        sb.append(" </graph>\n");
        
        sb.append("</graphml>\n");
        
        return sb.toString();
    }
    
    private static void exportAttributes(Map<String,Object> attrs, 
            StringBuilder to, Map<String,String> mapAttrNameType) {
        for (Map.Entry<String,Object> entry : attrs.entrySet()) {
            String attr = entry.getKey();
            Object value = entry.getValue();
            
            to.append("   <data key=\"");
            to.append(attr);
            to.append("\">");
            to.append(StringEscapeUtils.escapeXml(value.toString()));
            to.append("</data>\n");

            String type = getAttrType(value);

            String pre = mapAttrNameType.get(attr);
            if (pre!=null) {
                if (!pre.equals(type)) {
                    mapAttrNameType.put(attr, "string");
                }
            } else {
                mapAttrNameType.put(attr, type);
            }
        }
    }
    
    private static String getAttrType(Object obj) {
        if (obj instanceof Integer) {
            return "integer";
        }
        
        if (obj instanceof Float || obj instanceof Double) {
            return "double";
        }
        
        if (obj instanceof Boolean) {
            return "boolean";
        }
        
        return "string";
    }
}
