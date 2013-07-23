<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img class="loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <h4 style="display:inline;">OncoPrint
            <small>(<a href="faq.jsp#what-are-oncoprints">What are OncoPrints?</a>)</small>
        </h4>

        <span>
            <form style="display:inline;" action="svgtopdf.do" method="post" onsubmit="this.elements['svgelement'].value=oncoprint.getPdfInput();">

                <input type="hidden" name="svgelement">
                <input type="hidden" name="filetype" value="pdf">
                <input type="submit" value="PDF">
            </form>

            <form style="display:inline;" action="oncoprint_converter.svg" enctype="multipart/form-data" method="POST"
                  onsubmit="this.elements['xml'].value=oncoprint.getPdfInput(); return true;" target="_blank">
                <input type="hidden" name="xml">
                <input type="hidden" name="longest_label_length">
                <input type="hidden" name="format" value="svg">
                <input type="submit" value="SVG">
            </form>
        </span>

        <%@ include file="controls.jsp" %>

        <div id="oncoprint_body"></div>
        <script data-main="js/src/oncoprint/main-boilerplate.js" type="text/javascript" src="js/require.js"></script>

        <div id="oncoprint_legend"></div>
        <script type="text/template" id="glyph_template">
            <svg height="23" width="6">
            <rect fill="{{bg_color}}" width="5.5" height="23"></rect>

            <rect display="{{display_mutation}}" fill="#008000" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>

            <path display="{{display_down_rppa}}" d="M0,2.182461848650375L2.5200898716287647,-2.182461848650375 -2.5200898716287647,-2.182461848650375Z" transform="translate(2.75,2.3000000000000003)"></path>
            <path display="{{display_up_rppa}}" d="M0,-2.182461848650375L2.5200898716287647,2.182461848650375 -2.5200898716287647,2.182461848650375Z" transform="translate(2.75,20.909090909090907)" aria-describedby="ui-tooltip-838"></path>

            <rect display="{{display_down_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#6699CC" fill="none" aria-describedby="ui-tooltip-732"></rect>
            <rect display="{{display_up_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#FF9999" fill="none" aria-describedby="ui-tooltip-576"></rect>
            </svg>
            <span style="position: relative; bottom: 6px;">{{text}}</span>
        </script>

    </div>
</div>
