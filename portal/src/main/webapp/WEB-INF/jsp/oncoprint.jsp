<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<div id="oncoprints">
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
            geneticProfileIds: geneticProfiles
        };

        $.post(DataManagerFactory.getGeneDataManager.getJsonUrl(), geneDataQuery, function(data) {

            // ...do some stuff

            var geneDataManager = DataManagerFactory.getGeneDataManager();
            geneDataManager.fire(data);
        });

//        var oncoprint;
//        geneAlterations.fire(function(data) {
//            oncoPrintParams['data'] = data;
//
//            oncoprint = OncoPrint(oncoPrintParams);
//            oncoprint.insertFullOncoPrint($('#oncoprints'));
//        });

    </script>
</div>
