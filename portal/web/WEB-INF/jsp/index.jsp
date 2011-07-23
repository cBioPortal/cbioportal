<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.portal.model.*" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.portal.util.Config" %>


<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");
    String popeye = globalConfig.getProperty("popeye");

    if (popeye == null) {
        popeye = "preview.jsp";
    } 
    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }
%>

<%
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    ArrayList<CancerType> cancerTypeList = (ArrayList<CancerType>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    ArrayList<GeneticProfile> profileList = (ArrayList<GeneticProfile>) request.getAttribute
            (QueryBuilder.PROFILE_LIST_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute
            (QueryBuilder.GENETIC_PROFILE_IDS);

    ArrayList<CaseSet> caseSets = (ArrayList<CaseSet>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String caseIds = xssUtil.getCleanInput(request, QueryBuilder.CASE_IDS);
    String geneList = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
    String geneSetChoice = request.getParameter(QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "custom";
    }
    GeneSetUtil geneSetUtil = new GeneSetUtil();
    ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();

    String tabIndex = xssUtil.getCleanInput(request, QueryBuilder.TAB_INDEX);
    if (tabIndex == null) {
        tabIndex = QueryBuilder.TAB_VISUALIZE;
    } else {
        tabIndex = URLEncoder.encode(tabIndex);
    }

    request.setAttribute("index.jsp", Boolean.TRUE);
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    String cgdsUrl = GlobalProperties.getCgdsUrl();
    String cgdsUrlHome = cgdsUrl.replace("webservice.do", "");
    String xdebug = xssUtil.getCleanInput(request, QueryBuilder.XDEBUG);
    ArrayList <GeneWithScore> topMutatedGenesList = (ArrayList <GeneWithScore>) request.getAttribute("top_mutations");
    String userMessage = (String) request.getAttribute(QueryBuilder.USER_ERROR_MESSAGE);
%>

<script type="text/javascript">

    function swapTabs(param) {
        var form = YAHOO.util.Dom.get("main_form");
        var hiddenTab = YAHOO.util.Dom.get("<%= QueryBuilder.TAB_INDEX %>");
        hiddenTab.value = param;
        form.submit();
    }

</script>



<jsp:include page="global/header.jsp" flush="true" />
    <%
    if (xdebug != null) {
    %>
    <form id="main_form" action="index.do" method="POST">
    <% } else { %>
    <form id="main_form" action="index.do" method="POST">
    <% } %>
    <input type="hidden" id="<%= QueryBuilder.TAB_INDEX %>" name="<%= QueryBuilder.TAB_INDEX %>"
           value="<%= tabIndex %>">
    <%
        if (xdebug != null) {
        %>
            <input type="hidden" name="xdebug" value="<%= xdebug %>">
        <%
        }
    %>





    <table cellspacing="2px">
        <tr>
            <td>
            <div class="welcome">
                <table>
                <tr>
                   <td style="width: 350px">
                      <P><%= SkinUtil.getBlurb() %></p>
                      <p>The portal is developed and maintained by the <a href="http://cbio.mskcc.org/">Computational Biology Center</a> at <br><a href="http://www.mskcc.org/">Memorial Sloan-Kettering Cancer Center</a>. </p>
                   </td>
                   <td style="width: 300px">
                       <jsp:include page="<%= popeye %>" flush="true" />
                   </td>
                </tr>
                </table>
            </div>

            <%
            if (userMessage != null) {
                out.println ("<div class='user_message'>" + userMessage + "</div>");
            }
            %>                  
            <%
                if (tabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) {
                    out.println ("<span class=\"tab_inactive\"><a href=\"javascript:swapTabs('"
                            + QueryBuilder.TAB_VISUALIZE +"');\">"
                            + "Query</a></span>");
                    out.println ("<span class=\"tab_active\">Download Data</span>");
                } else {
                    out.println ("<span class=\"tab_active\">Query</span>");
                    out.println ("<span class=\"tab_inactive\"><a href=\"javascript:swapTabs('"
                            + QueryBuilder.TAB_DOWNLOAD +"');\">Download Data</a></span>");
                }
            %>
            <div class="main_panel">
            <table width="100%">
                <tr>
                    <td>
                        <%@ include file="step1.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td>
                        <%@ include file="step2.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td>
                        <%@ include file="step3.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td>
                        <%@ include file="step4.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td>
                        <%@ include file="step5.jsp" %>
                    </td>
                </tr>
                <tr>
                    <td>
                    <% if (tabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) {
                        out.println ("<p>Clicking submit will generate a tab-delimited file containing"
                            + " your requested data.</p>");
                        String transposeStr = request.getParameter("transpose");
                        String transposeChecked = "";
                        if (transposeStr != null) {
                            transposeChecked = " checked ";
                        }
                        out.println("<P><input type=checkbox " + transposeChecked
                                + " name=transpose>Transpose data matrix.</P>");
                    } %>
                    <%  if (finalProfileList.size() > 0) { %>                        
                    <input type=submit name="<%= QueryBuilder.ACTION%>" value="<%= QueryBuilder.ACTION_SUBMIT %>"/>
                    <% } %>
                    </td>
                </tr>
            </table>

            <p><small><a id='json_cancer_studies' href="">Toggle Experimental JSON Results</a></small></p>
            <div class="markdown" style="display:none;" id="cancer_results">
            </div>

            <script type="text/javascript">
            $(document).ready(function(){
                //  Get Cancer Studies JSON Data
                jQuery.getJSON("cancer_studies.json",function(json){
                    jQuery.each(json,function(key,cancer_study){
                        $("#cancer_results").append('<h1>Cancer Study:  ' + key + '</h1>');
                        $("#cancer_results").append('<h2>Genomic Profiles:' + '</h2>');
                        $("#cancer_results").append('<ul>');
                        jQuery.each(cancer_study.genomic_profiles,function(i, genomic_profile) {
                            $("#cancer_results").append('<li>' + genomic_profile.name + ': ' + genomic_profile.description + "</li>'");
                        }); //  end for each genomic profile loop
                        $("#cancer_results").append('</ul>');
                        $("#cancer_results").append('<h2>Case Sets:' + '</h2>');
                        $("#cancer_results").append('<ul>');
                        jQuery.each(cancer_study.case_sets,function(i, case_set) {
                            $("#cancer_results").append('<li>' + case_set.name + ': ' + case_set.description + "</li>'");
                        }); //  end for each genomic profile loop
                        $("#cancer_results").append('</ul>');
                    });  //  end for each cancer study loop
                });  //  end getJSON function

                //  Provide toggle for JSON Results
                $('#json_cancer_studies').click(function(event) {
                  event.preventDefault();
                  $('#cancer_results').toggle();
                });

            });  //  end document ready function
            </script>
                
            </div>
            </td>
        </tr>
    </table>
    </td>
    <td width="172">
	<jsp:include page="global/right_column.jsp" flush="true" />
    </td>
  </tr>
  <tr>
    <td colspan="3">
        <script type="text/javascript">
            $(document).ready(function() {
               window.sessionStorage.clear(); 
            });
        </script>
	<jsp:include page="global/footer.jsp" flush="true" />    
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="global/xdebug.jsp" flush="true" />
</body>
</html>
