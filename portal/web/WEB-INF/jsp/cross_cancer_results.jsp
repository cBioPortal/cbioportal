<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>

<%
    String siteTitle = SkinUtil.getTitle();
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);

    ArrayList<CancerStudy> cancerStudiesWithMutations = new ArrayList<CancerStudy>(),
                           cancerStudiesWithOutMutations = new ArrayList<CancerStudy>();
    for(CancerStudy cancerStudy: cancerStudies) {
        if(cancerStudy.hasMutationData()) {
            cancerStudiesWithMutations.add(cancerStudy);
        } else {
            cancerStudiesWithOutMutations.add(cancerStudy);
        }
    }

    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();
    String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);

    //  Prepare gene list for URL.
    //  Extra spaces must be removed.  Otherwise AJAX Load will not work.
    geneList = Utilities.appendSemis(geneList);
    geneList = geneList.replaceAll("\\s+", " ");
    geneList = URLEncoder.encode(geneList);
%>

<jsp:include page="global/header.jsp" flush="true"/>

<script type="text/javascript">
    $(document).ready(function() {
        $("#toggle_query_form").tipTip();

        <%
        //  Iterate through each Cancer Study
        //  For each cancer study, init AJAX
        //  Fix: Chain the AJAX calls not to make Chrome (14) crash
        String studiesList = "";
        assert(cancerStudies.size() > 0);
        for (CancerStudy cancerStudy:  cancerStudies)
            studiesList += "'" + cancerStudy.getCancerStudyStableId() + "',";
        studiesList = studiesList.substring(0, studiesList.length()-1);
        %>

        var cancerStudies = [<%=studiesList%>];
        loadStudiesWithIndex(0);

        function loadStudiesWithIndex(bundleIndex) {
            if(bundleIndex >= cancerStudies.length)
                return;

            var cancerID = cancerStudies[bundleIndex];
            $("#study_" + cancerID).load('cross_cancer_summary.do?gene_list=<%= geneList %>&cancer_study_id=' + cancerID,
                                            function() { loadStudiesWithIndex(bundleIndex+1)});
        }
    });
</script>

<table>
    <tr>
        <td>

            <div id="results_container">

                <div class="ui-state-highlight ui-corner-all">
                    <p><span class="ui-icon ui-icon-info"
                             style="float: left; margin-right: .3em; margin-left: .3em"></span>
                        Results are available for <strong><%= (cancerStudies.size()) %>
                        cancer studies</strong>. Click each cancer study below to view a summary of
                        results.
                    </p>
                </div>

                <p><a href=""
                      title="Modify your original query.  Recommended over hitting your browser's back button."
                      id="toggle_query_form">
                    <span class='query-toggle ui-icon ui-icon-triangle-1-e'
                          style='float:left;'></span>
                    <span class='query-toggle ui-icon ui-icon-triangle-1-s'
                          style='float:left; display:none;'></span><b>Modify Query</b></a>

                <p/>

                <div style="margin-left:5px;display:none;" id="query_form_on_results_page">
                    <%@ include file="query_form.jsp" %>
                </div>

                <jsp:include page="global/small_onco_print_legend.jsp" flush="true"/>

                <script>
                    jQuery(document).ready(function() {
                        $('#accordion .head').click(function() {
                            //  This toggles the next element, right after head,
                            //  which is the accordion ajax panel
                            $(this).next().toggle();
                            //  This toggles the ui-icons within head
                            jQuery(".ui-icon", this).toggle();
                            return false;
                        }).next().hide();
                    });
                </script>

                <div id="accordion">
                    <h2 class="cross_cancer_header">Studies with Mutation Data</h2>
                    <% outputCancerStudies(cancerStudiesWithMutations, out); %>
                    <% if( !cancerStudiesWithOutMutations.isEmpty() ) {
                    %>
                    <h2 class="cross_cancer_header">Studies without Mutation Data</h2>
                    <%
                            outputCancerStudies(cancerStudiesWithOutMutations, out);
                       }
                     %>
                </div>

            </div>
            <!-- end results container -->
        </td>
    </tr>
</table>
</div>
</td>
</tr>
<tr>
    <td colspan="3">
        <jsp:include page="global/footer.jsp" flush="true"/>
    </td>
</tr>
</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true"/>

</body>
</html>

<%!
    private void outputCancerStudies(ArrayList<CancerStudy> cancerStudies,
            JspWriter out) throws IOException {
        for (CancerStudy cancerStudy : cancerStudies) {
            out.println("<div class='accordion_panel'>");
            out.println("<h1 class='head'>");

            //  output triangle icons
            //  the float:left style is required;  otherwise icons appear on their own line.
            out.println("<span class='ui-icon ui-icon-triangle-1-e' style='float:left;'></span>");
            out.println("<span class='ui-icon ui-icon-triangle-1-s'"
                    + " style='float:left;display:none;'></span>");
            out.println(cancerStudy.getName());
            out.println("<span class='percent_altered' id='percent_altered_" + cancerStudy.getCancerStudyStableId()
                    + "' style='float:right'><img src='images/ajax-loader2.gif'></span>");
            out.println("</h1>");
            out.println("<div class='accordion_ajax' id=\"study_"
                    + cancerStudy.getCancerStudyStableId() + "\">");
            out.println("</div>");
            out.println("</div>");
        }
    }
%>