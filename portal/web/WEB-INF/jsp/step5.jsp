<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<% if (!tabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) { %>



<p>
<h4>Optional Arguments:  &nbsp;&nbsp;<small>[<a href="#" id="step5">Toggle</a>]</small></h4>
<br/>
<div class="toggler">
    <div id="optional_args">
<p>
<%
String computeLogOddsRatio = request.getParameter(QueryBuilder.COMPUTE_LOG_ODDS_RATIO);
String logOddsOptionChecked = "";
if (computeLogOddsRatio != null) {
    logOddsOptionChecked = " checked ";
}
%>
<input type="checkbox" <%= logOddsOptionChecked %> name="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>" value="<%= Boolean.TRUE%>">Compute <B>Mutual Exclusivity / Co-occurence</B> between all pairs of genes.
    <br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Not recommended for more than 10 genes.
</p>
<% } %>
        </div> <!-- end optional_args -->
    </div>     <!-- end toggler -->