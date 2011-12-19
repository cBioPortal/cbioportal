<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.cgds.dao.DaoSangerCensus" %>
<%@ page import="org.mskcc.cgds.model.SangerCancerGene" %>
<%@ page import="java.io.IOException" %>
<div class="frequency_section" id="frequency_plot">
<p><h4>Sanger Cancer Gene Census Information:</h4>

<%
DaoSangerCensus daoSangerCensus = DaoSangerCensus.getInstance();
HashMap<String, SangerCancerGene> censusMap = daoSangerCensus.getCancerGeneSet();
int numCancerGenes = getNumCancerGenes(geneWithScoreList, censusMap);
if (numCancerGenes > 1) {
    out.println ("<P><B>" + numCancerGenes + "</B> of your query genes are known cancer genes, as cataloged"
        + " by the <a href='http://www.sanger.ac.uk/genetics/CGP/Census/'>Sanger Cancer Gene Census</a>:");
%>
<p>
<div class="map">
<table width=95%>
    <tr>
        <th>Gene</th>
        <th>Tumor Types (Somatic)</th>
        <th>Tumor Types (Germline)</th>
        <th>Tissue Types</th>
        <th>Mutation Types</th>
    </tr>
<% for (GeneWithScore geneWithScore : geneWithScoreList) {
    if (censusMap.containsKey(geneWithScore.getGene())) {
        SangerCancerGene cancerGene = censusMap.get(geneWithScore.getGene());
        out.println ("<tr><td><a href='http://www.ncbi.nlm.nih.gov/gene/"
               + cancerGene.getGene().getEntrezGeneId()
               + "'>" + cancerGene.getGene().getHugoGeneSymbolAllCaps() + "</a></td>");
        ArrayList <String> tumorTypesSomatic = cancerGene.getTumorTypesSomaticMutationList();
        outputParts(out, tumorTypesSomatic);
        ArrayList <String> tumorTypesGermline = cancerGene.getTumorTypesGermlineMutationList();
        outputParts(out, tumorTypesGermline);
        ArrayList <String> tissueTypes = cancerGene.getTissueTypeList();
        outputParts(out, tissueTypes);
        ArrayList <String> mutationTypes = cancerGene.getMutationTypeList();
        outputParts(out, mutationTypes);
        out.println ("</tr>");
    }
} %>
</table>
<br>
<% } else {
    out.println ("<p>None of your query genes are known cancer genes, as cataloged"
        + "by the <a href='http://www.sanger.ac.uk/genetics/CGP/Census/'>Sanger Cancer Gene Census</a>.");
} %>
</div>

<%!
    private int getNumCancerGenes(ArrayList <GeneWithScore> geneWithScoreList,
            HashMap<String, SangerCancerGene> censusMap) {
        int numCancerGenes = 0;
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            if (censusMap.containsKey(geneWithScore.getGene())) {
                numCancerGenes++;
            }
        }
        return numCancerGenes;
    }

    private void outputParts(JspWriter out, ArrayList<String> partsList) throws IOException {
        out.println ("<td>");
        int counter = 0;
        for (String part:  partsList) {
            out.print (part.trim());
            counter++;
            if (counter < partsList.size()) {
                out.println (", ");
            }
        }
        out.println ("</td>");
    }
%>
