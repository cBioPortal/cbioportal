<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img id="outer_loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <h4 style="display:inline;">OncoPrint
            <small>(<a href="faq.jsp#what-are-oncoprints">What are OncoPrints?</a>)</small>
        </h4>

        <div id="oncoprint_controls" style="margin-top:10px; margin-bottom:20px;"></div>
        <%@ include file="controls-templates.jsp" %>

        <div id="oncoprint-statment" style="margin-left:3;">
            <p>Case Set: <%=StringEscapeUtils.escapeHtml(OncoPrintUtil.getCaseSetDescription(caseSetId, caseSets))%></p>   
        </div>
        
        <div id="oncoprint_whole_body">
            <p> Altered in <%=dataSummary.getNumCasesAffected()%> (<%=OncoPrintUtil.alterationValueToString(dataSummary.getPercentCasesAffected())%>) of cases
            <span class='oncoprint-diagram-toolbar-buttons' style="float:right;margin-right:20px;display: none;">
                
            <input type="range" min="0" max="50" value="50" style="width:100px;top: 5px;" step="5" />
            <img id="oncoprint-diagram-downloads-icon" class="oncoprint-diagram-downloads-icon" src="images/in.svg">
            </span>
            </p>
        <img id="inner_loader_img" src="images/ajax-loader.gif" style="display:none;">
        <div id="oncoprint_body"></div>
        </div>
        <div id="oncoprint_legend"></div>
        <%@ include file="legend-template.jsp" %>

        <script data-main="js/src/oncoprint/main-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>
    </div>
</div>
