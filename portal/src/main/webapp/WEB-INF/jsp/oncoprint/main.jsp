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
            <span class='oncoprint-diagram-toolbar-buttons'>
            <button class='oncoprint-diagram-download' type="pdf">PDF</button>
            <button class='oncoprint-diagram-download' type="svg">SVG</button>
            <button class='oncoprint-sample-download' type="txt">Sample</button>
            </span>
            </p>
        <img id="inner_loader_img" src="images/ajax-loader.gif" style="display:none;">
        <div id="oncoprint_body"></div>
        </div>
        <div id="oncoprint_legend"></div>
        <%@ include file="legend-template.jsp" %>

        <script data-main="js/src/oncoprint/main-boilerplate.js" type="text/javascript" src="js/require.js"></script>
    </div>
</div>
