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
            
            <img id="oncoprint_diagram_showmutationcolor_icon" checked="0" style="width: 16px; height: 16px" class="oncoprint_diagram_showmutationcolor_icon" src="images/colormutations.svg">
            <img id="oncoprint-diagram-showlegend-icon" checked="0" style="width: 16px; height: 16px" class="oncoprint-diagram-showlegend-icon" src="images/showlegend.svg">
            <img id="oncoprint-diagram-removeUCases-icon" checked="0" style="width: 16px; height: 16px" class="oncoprint-diagram-removeUCases-icon" src="images/removeUCases.svg">
            <img id="oncoprint-diagram-removeWhitespace-icon" checked="0" style="width: 20px; height: 16px" class="oncoprint-diagram-removeWhitespace-icon" src="images/removeWhitespace.svg">
            <img id="oncoprint-diagram-downloads-icon" style="width: 16px; height: 16px" class="oncoprint-diagram-downloads-icon" src="images/in.svg">
            <span class='oncoprint_diagram_slider_icon' style="width: 80px; height: 16px"></span>
            </span>
            </p>
        <img id="inner_loader_img" src="images/ajax-loader.gif" style="display:none;">
        <div id="oncoprint_body"></div>
        </div>
            <div id="oncoprint_legend" style="display: inline;"></div>
        <%@ include file="legend-template.jsp" %>

        <script data-main="js/src/oncoprint/main-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>
    </div>
</div>
