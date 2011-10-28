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
      
      String netSize = request.getParameter("netsize");
      if (netSize==null) netSize = "large";
    %>
        <li>
        <input class="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>" type="checkbox" <%= logOddsOptionChecked %>
           name="<%= QueryBuilder.COMPUTE_LOG_ODDS_RATIO%>"
           value="<%= Boolean.TRUE%>">Compute <B>Mutual Exclusivity / Co-occurence</B> between all pairs of genes.
           <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(Not recommended for more than 10 genes.)
        </li>
       
            <br/>
        <li>
            Network size: 
            <input type="radio" name="netsize" value="small"
                   <%if(netSize.equals("small")){%>checked="checked"<%}%>
                   >Small network <img class="netsize_help" src="images/help.png" 
                   title="including query genes only."/>&nbsp;&nbsp;
            <input type="radio" name="netsize" value="medium"
                   <%if(netSize.equals("medium")){%>checked="checked"<%}%>
                   >Medium network <img class="netsize_help" src="images/help.png" 
                   title="including query genes and neighbor genes that interact with two or more query genes."/>&nbsp;&nbsp;
            <input type="radio" name="netsize" value="large"
                   <%if(netSize.equals("large")){%>checked="checked"<%}%>
                   >Large network <img class="netsize_help" src="images/help.png" 
                   title="including query genes and all neighbor genes"/>&nbsp;&nbsp;
            </li>
        </ul>
        </div>
    </div>
</div>
<% } %>