<%@ page import="org.mskcc.cgds.model.ExtendedMutation" %>
<%@ page import="org.mskcc.portal.html.MutationTableUtil" %>
<%@ page import="org.mskcc.portal.model.ExtendedMutationMap" %>
<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.MutationCounter" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.IOException" %>
<%
    ArrayList<ExtendedMutation> extendedMutationList = (ArrayList<ExtendedMutation>)
            request.getAttribute(QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST);
    ExtendedMutationMap mutationMap = new ExtendedMutationMap(extendedMutationList,
            mergedProfile.getCaseIdList());

    out.println("<div class='section' id='mutation_details'>");

    if (mutationMap.getNumGenesWithExtendedMutations() > 0) {
        outputOmaHeader(out);
        out.println("<div class='map'>");

        for (GeneWithScore geneWithScore : geneWithScoreList) {
            outputGeneTable(geneWithScore, mutationMap, out, mergedCaseList);
        }
        out.println("</div>");
    } else {
        outputNoMutationDetails(out);
    }
    out.println("</div>");
%>

<%!
    private void outputGeneTable(GeneWithScore geneWithScore,
            ExtendedMutationMap mutationMap, JspWriter out, 
            ArrayList<String> mergedCaseList) throws IOException {
        MutationTableUtil mutationTableUtil = new MutationTableUtil(geneWithScore.getGene());
        MutationCounter mutationCounter = new MutationCounter(geneWithScore.getGene(),
                mutationMap);

        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0) {
            outputHeader(out, geneWithScore, mutationCounter);
            out.println("<table width='100%' cellspacing='0px'>");

            //  Table column headers
            out.println(mutationTableUtil.getTableHeaderHtml());

            //  Mutations are sorted by case
            for (String caseId : mergedCaseList) {
                ArrayList<ExtendedMutation> mutationList =
                        mutationMap.getExtendedMutations(geneWithScore.getGene(), caseId);
                if (mutationList != null && mutationList.size() > 0) {
                    for (ExtendedMutation mutation : mutationList) {
                        out.println(mutationTableUtil.getDataRowHtml(mutation));
                    }
                }
            }
            out.println("</table><p>");
            out.println(mutationTableUtil.getTableFooterMessage());
        }
    }

    private void outputHeader(JspWriter out, GeneWithScore geneWithScore,
            MutationCounter mutationCounter) throws IOException {
        out.print("<h5>" + geneWithScore.getGene().toUpperCase() + ": ");
        out.println(mutationCounter.getTextSummary());
        out.println("</h5>");
    }

    private void outputNoMutationDetails(JspWriter out) throws IOException {
        out.println("<p>There are no mutation details available for the gene set entered.</p>");
        out.println("<br><br>");
    }

    private void outputOmaHeader(JspWriter out) throws IOException {
        out.println("** Predicted functional impact (via " +
                "<a href='http://mutationassessor.org'>Mutation Assessor</a>)" +
                " is provided for missense mutations only.  ");
        out.println("<br><br>");
    }
%>