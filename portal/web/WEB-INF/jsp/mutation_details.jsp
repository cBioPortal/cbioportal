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
        for (GeneWithScore geneWithScore : geneWithScoreList) {
            outputGeneTable(geneWithScore, mutationMap, out, mergedCaseList);
        }
    } else {
        outputNoMutationDetails(out);
    }
    out.println("</div>");
%>

<style type="text/css" title="currentStyle"> 
        .mutation_datatables_filter {
                width: 40%;
                float: right;
                padding-top:5px;
                padding-bottom:5px;
                padding-right:5px;
        }
        .mutation_datatables_info {
                width: 55%;
                float: left;
                padding-left:5px;
                padding-top:7px;
                font-size:90%;
        }
</style>

<script type="text/javascript">

//  Place mutation_details_table in a JQuery DataTable
$(document).ready(function(){
    <%
    for (GeneWithScore geneWithScore : geneWithScoreList) {
        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0) { %>
          $('#mutation_details_table_<%= geneWithScore.getGene().toUpperCase() %>').dataTable( {
              "sDom": '<"H"<"mutation_datatables_filter"f><"mutation_datatables_info"i>>t',
              "bPaginate": false,
              "bFilter": true
          } );
        <% } %>
    <% } %>
});
</script>


<%!
    private void outputGeneTable(GeneWithScore geneWithScore,
            ExtendedMutationMap mutationMap, JspWriter out, 
            ArrayList<String> mergedCaseList) throws IOException {
        MutationTableUtil mutationTableUtil = new MutationTableUtil(geneWithScore.getGene());
        MutationCounter mutationCounter = new MutationCounter(geneWithScore.getGene(),
                mutationMap);

        if (mutationMap.getNumExtendedMutations(geneWithScore.getGene()) > 0) {
            outputHeader(out, geneWithScore, mutationCounter);
            out.println("<table cellpadding='0' cellspacing='0' border='0' " +
                    "class='display mutation_details_table' " +
                    "id='mutation_details_table_" + geneWithScore.getGene().toUpperCase()
                    +"'>");

            //  Table column headers
            out.println("<thead>");
            out.println(mutationTableUtil.getTableHeaderHtml() + "<BR>");
            out.println("</thead>");

            //  Mutations are sorted by case
            out.println("<tbody>");
            for (String caseId : mergedCaseList) {
                ArrayList<ExtendedMutation> mutationList =
                        mutationMap.getExtendedMutations(geneWithScore.getGene(), caseId);
                if (mutationList != null && mutationList.size() > 0) {
                    for (ExtendedMutation mutation : mutationList) {
                        out.println(mutationTableUtil.getDataRowHtml(mutation));
                    }
                }
            }
            out.println("</tbody>");

            //  Table column footer
            out.println("<tfoot>");
            out.println(mutationTableUtil.getTableHeaderHtml());
            out.println("</tfoot>");

            out.println("</table><p><br>");
            out.println(mutationTableUtil.getTableFooterMessage());
            out.println("<br>");
        }
    }

    private void outputHeader(JspWriter out, GeneWithScore geneWithScore,
            MutationCounter mutationCounter) throws IOException {
        out.print("<h4>" + geneWithScore.getGene().toUpperCase() + ": ");
        out.println(mutationCounter.getTextSummary());
        out.println("</h4>");
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