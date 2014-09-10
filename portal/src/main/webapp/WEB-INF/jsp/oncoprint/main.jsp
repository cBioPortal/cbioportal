<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img id="outer_loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <p>
            Gene Set / Pathway is altered in <b><span id='oncoprint_num_of_altered_cases'></span> (<span id='oncoprint_percentage_of_altered_cases'></span>%)</b> of all samples.
            (<a href="faq.jsp#what-are-oncoprints" target="_blank">What are OncoPrints?</a>)
        </p>

        <div id="oncoprint_controls" style="margin-top:-10px; margin-bottom:10px;"></div>
        <%@ include file="controls-templates.jsp" %>
        
        <div id="oncoprint_whole_body">
            <span class='oncoprint-diagram-toolbar-buttons' style="float:right;margin-right:20px;">
                <button class='oncoprint-diagram-download' type="pdf">PDF</button>
                <button class='oncoprint-diagram-download' type="svg">SVG</button>
                <button class='oncoprint-sample-download' type="txt">Samples</button>
            </span>
            <img id="inner_loader_img" src="images/ajax-loader.gif" style="display:none;">
            <div id="oncoprint_body"></div>
        </div>

        <div id="oncoprint_legend"></div>
        <%@ include file="legend-template.jsp" %>

        <script data-main="js/src/oncoprint/main-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>
    </div>
</div>

<script type="text/javascript"> 
    $('.oncoprint-sample-download').qtip({
        content: {text: 'Download the list of samples, sorted in the order in which they are displayed in the OncoPrint (left to right)'},
        position: {my:'left bottom', at:'top right', viewport: $(window)},
        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
        show: {event: "mouseover"},
        hide: {fixed: true, delay: 100, event: "mouseout"}
    });
</script>    
