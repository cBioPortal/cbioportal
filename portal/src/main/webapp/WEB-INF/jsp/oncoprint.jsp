<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<div id="oncoprint">
    <link rel="stylesheet" type="text/css" href="css/oncoprint.css">
    <script type="text/javascript" src="js/oncoprint.js"></script>
    <script type="text/javascript" src="js/d3.v2.min.js"></script>
    <%--todo: we may want to import d3 globally but for now, it's just here--%>

    <svg width=200 height=25 id="oncoprint_key">
        <g transform="translate(20,0)"><rect class="cna AMPLIFIED" width="5.5" height="23"></rect></g>
        <g transform="translate(40,0)"><rect class="cna DELETED" width="5.5" height="23"></rect></g>
        <g transform="translate(0,0)"><rect class="cna none" width="5.5" height="23"></rect><rect class="mutation mut" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect></g>
        <g transform="translate(60,0)"><rect class="cna none" width="5.5" height="23"></rect><rect class="mrna DOWNREGULATED" width="5.5" height="23"></rect></g>
        <g transform="translate(80,0)"><rect class="cna none" width="5.5" height="23"></rect><rect class="mrna UPREGULATED" width="5.5" height="23"></rect></g>
        <g transform="translate(100,0)"><rect class="cna none" width="5.5" height="23"></rect><path class="rppa" d="M 0 7.666666666666667 l 2.75 -7.666666666666667 l 2.75 7.666666666666667 l 0 0"></path></g>
        <g transform="translate(120,0)"><rect class="cna none" width="5.5" height="23"></rect><path class="rppa" d="M 0 15 l 2.75 7.666666666666667 l 2.75 -7.666666666666667 l 0 0"></path></g>
    </svg>
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
            geneticProfileIds: geneticProfiles
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
