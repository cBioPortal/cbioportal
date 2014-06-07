<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img id="outer_loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <h4 style="display:inline;">OncoPrint
            <small>(<a href="faq.jsp#what-are-oncoprints">What are OncoPrints?</a>)</small>
        </h4>

        <span>
            <form id="pdf-form" style="display:inline;" action="svgtopdf.do" method="post" target="_blank">
                <input type="hidden" name="svgelement">
                <input type="hidden" name="filetype" value="pdf">
                <input type="hidden" name="filename" value="oncoprint.pdf">
                <input type="submit" value="PDF">
            </form>

            <form id="svg-form" style="display:inline;" action="oncoprint_converter.svg" enctype="multipart/form-data" method="POST" target="_blank">
                <input type="hidden" name="xml">
                <input type="hidden" name="longest_label_length">
                <input type="hidden" name="format" value="svg">
                <input type="submit" value="SVG">
            </form>
        </span>

        <div id="oncoprint_controls" style="margin-top:10px; margin-bottom:20px;"></div>
        <%@ include file="controls-templates.jsp" %>

        <div style="margin-left:3;">
            <p>Case Set: <%=StringEscapeUtils.escapeHtml(OncoPrintUtil.getPatientSetDescription(patientSetId, patientSets))%></p>
            <p>Altered in <%=dataSummary.getNumCasesAffected()%> (<%=OncoPrintUtil.alterationValueToString(dataSummary.getPercentCasesAffected())%>) of cases</p>
        </div>

        <img id="inner_loader_img" src="images/ajax-loader.gif" style="display:none;">

        <div id="oncoprint_body"></div>

        <div id="oncoprint_legend"></div>
        <%@ include file="legend-template.jsp" %>

        <script data-main="js/src/oncoprint/main-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>
    </div>
</div>
