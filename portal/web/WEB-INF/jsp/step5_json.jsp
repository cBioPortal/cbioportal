<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<div class="query_step_section" id="step5">
    <span class="step_header">Optional Arguments:  &nbsp;&nbsp;<small>[<a href="#" id="step5_toggle">Toggle</a>]</small></span>
<br/>
<div class="toggler">
    <p/>
    <div id="optional_args">
<%
    String computeLogOddsRatioLocal = request.getParameter(org.mskcc.portal.servlet.QueryBuilder.COMPUTE_LOG_ODDS_RATIO);
    String logOddsOptionChecked = "";
    if (computeLogOddsRatioLocal != null) {
        logOddsOptionChecked = " checked ";
    }
%>
    <input type="checkbox" <%= logOddsOptionChecked %>
           name="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>"
           value="<%= Boolean.TRUE%>">Compute <B>Mutual Exclusivity / Co-occurence</B> between all pairs of genes.
        <br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Not recommended for more than 10 genes.
    </div>
    </div>
</div>