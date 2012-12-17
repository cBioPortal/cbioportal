<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
    <h4>OncoPrint</h4>

    <h4 style="padding-top:15px;" onclick='$("#oncoprint_controls").toggle();'>
        <span class="ui-icon ui-icon-triangle-1-e" style="float:left;" onclick='$("#oncoprint_conrols .ui-icon-triangle-1-e").toggle();$("#oncoprint_conrols .ui-icon-triangle-1-s").toggle();'>
        <span class="ui-icon ui-icon-triangle-1-s" style="float:left;display:none;" onclick='$("#oncoprint_conrols .ui-icon-triangle-1-e").toggle();$("#oncoprint_conrols .ui-icon-triangle-1-s").toggle();'></span>
        </span>Customize</h4>
    <table id='oncoprint_controls' style="padding-left:13px; padding-top:5px;">
        <tr>
            <td><input type='checkbox' onclick='oncoprint.toggleUnaltered();'>Only Show Altered Cases</td>
            <td><input type='checkbox' onclick='if ($(this).is(":checked")) {oncoprint.defaultSort();} else {oncoprint.memoSort();}'>Unsort Cases</td>
        </tr>

        <tr>
            <td style="padding-right: 15px;"><span>Adjust Width</span><div id="width_scroller" style="display: inline-table;"></div></td>
            <td><input type='checkbox' onclick='oncoprint.toggleWhiteSpace();'>Remove Whitespace</td>
        </tr>
    </table>
<div id="oncoprint">
    <link rel="stylesheet" type="text/css" href="css/oncoprint.css">
    <script type="text/javascript" src="js/oncoprint.js"></script>
    <script type="text/javascript" src="js/d3.v2.min.js"></script>
    <%--todo: we may want to import d3 globally but for now, it's just here--%>

    <script type="text/javascript">
        var oncoPrintParams = {
            cancer_study_id: "<%=cancerTypeId%>",
            case_set_str: "<%=StringEscapeUtils.escapeHtml(MakeOncoPrint.getCaseSetDescriptionREFACTOR(caseSetId, caseSets))%>",
            num_cases_affected: "<%=dataSummary.getNumCasesAffected()%>",
            percent_cases_affected: "<%=MakeOncoPrint.alterationValueToString(dataSummary.getPercentCasesAffected())%>"
        };

        var geneDataQuery = {
            genes: genes,
            samples: samples,
            geneticProfileIds: geneticProfiles,
            z_score_threshold: <%=zScoreThreshold%>,
            rppa_score_threshold: <%=rppaScoreThreshold%>
        };

        var oncoprint;      // global
        $.post(DataManagerFactory.getGeneDataJsonUrl(), geneDataQuery, function(data) {

            oncoPrintParams['data'] = data;

            oncoprint = Oncoprint($('#oncoprint')[0], oncoPrintParams);

            oncoprint.draw();

            var geneDataManager = DataManagerFactory.getGeneDataManager();
            geneDataManager.fire(data);
        });
    </script>
</div>

<div id="oncoprint_key">
    <svg id="cna" style="display:none;width:280px;" height=40>
        <g transform="translate(0,10)">
            <rect class="cna AMPLIFIED" width="5.5" height="23"></rect>
            <text x=10 y=16>Amplification</text>
        </g>
        <g transform="translate(100,10)">
            <rect class="cna AMPLIFIED" width="5.5" height="23"></rect>
            <rect class="cna HOMODELETED" width="5.5" height="23"></rect>
            <text x=10 y=16>Homozygous deletion</text>
        </g>
    </svg>

    <svg id="mrna" style="display:none;width:330px;" height=40>
        <g transform="translate(0,10)">
            <rect class="cna none" width="5.5" height="23"></rect><rect class="mrna UPREGULATED" width="5.5" height="23"></rect>
            <text x=10 y=16>MRNA Upregulated</text>
        </g>

        <g transform="translate(140,10)">
            <rect class="cna none" width="5.5" height="23"></rect><rect class="mrna DOWNREGULATED" width="5.5" height="23"></rect>
            <text x=10 y=16>MRNA Downregulated</text>
        </g>
    </svg>

    <svg id="rppa" style="display:none;width:330px;" height=40>
        <g transform="translate(0,10)">
            <rect class="cna none" width="5.5" height="23"></rect><path class="rppa" d="M 0 7.666666666666667 l 2.75 -7.666666666666667 l 2.75 7.666666666666667 l 0 0"></path>
            <text x=10 y=16>RPPA Upregulated</text>
        </g>

        <g transform="translate(135,10)">
            <rect class="cna none" width="5.5" height="23"></rect><path class="rppa" d="M 0 15 l 2.75 7.666666666666667 l 2.75 -7.666666666666667 l 0 0"></path>
            <text x=10 y=16>RPPA Downregulated</text>
        </g>
    </svg>

    <svg id="mutation" style="display:none;" width=150 height=40>
        <g transform="translate(0,10)">
            <rect class="cna none" width="5.5" height="23"></rect><rect class="mutation mut" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>
            <text x=10 y=16>Mutation</text>
        </g>
    </svg>
</div>

