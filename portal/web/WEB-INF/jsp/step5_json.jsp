<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%
    if (localTabIndex != null && localTabIndex.equals(QueryBuilder.TAB_VISUALIZE)) {
%>
<div class="query_step_section" id="step5">
    <span class="step_header">
    <span class='ui-icon ui-icon-triangle-1-e' style='float:left;'></span>
    <span class='ui-icon ui-icon-triangle-1-s' style='float:left; display:none;'></span>
    Optional Arguments:  &nbsp;&nbsp;</span>
    <br/>
    <div class="toggler">
        <div id="optional_args">

    <%
       String computeLogOddsRatioLocal = request.getParameter(org.mskcc.portal.servlet.QueryBuilder.COMPUTE_LOG_ODDS_RATIO);
      String logOddsOptionChecked = "";
      if (computeLogOddsRatioLocal != null) {
         logOddsOptionChecked = " checked ";
     }
    %>
        <input class="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>" type="checkbox" <%= logOddsOptionChecked %>
           name="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>"
           value="<%= Boolean.TRUE%>">Compute <B>Mutual Exclusivity / Co-occurence</B> between all pairs of genes.
            <br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Not recommended for more than 10 genes.
       
        </div>
    </div>
</div>
<% } %>