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
        <ul>

    <%
       String computeLogOddsRatioLocal = request.getParameter(org.mskcc.portal.servlet.QueryBuilder.COMPUTE_LOG_ODDS_RATIO);
      String logOddsOptionChecked = "";
      if (computeLogOddsRatioLocal != null) {
         logOddsOptionChecked = " checked ";
     }
    %>
        <li>
        <input class="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>" type="checkbox" <%= logOddsOptionChecked %>
           name="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>"
           value="<%= Boolean.TRUE%>">Compute <B>Mutual Exclusivity / Co-occurence</B> between all pairs of genes.
           <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(Not recommended for more than 10 genes.)
        </li>
       
            <br/>
        <li>
            Select network size: 
            <select class="select-net-size" name="netsize">
                <option value="small">Small network: including query genes only</option>
                <option value="medium">Medium network: including query genes and neighbor genes that interact with two or more query genes</option>
                <option value="large">Large network: including query genes and all neighbor genes</option>
                <option value="default" selected="selected">Default: medium network (if at least two query genes) or large network (if only one query gene) </option>
            </select>
        </li>
        </ul>
        </div>
    </div>
</div>
<% } %>