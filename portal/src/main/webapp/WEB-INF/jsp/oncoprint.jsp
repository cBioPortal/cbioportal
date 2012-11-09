<div id="oncoprints">
    <script type="text/javascript" src="js/oncoprint.js"></script>
    <script type="text/javascript" src="js/d3.v2.min.js"></script>
    <style type="text/css" src="css/oncoprint.css"></style>
    <%--todo: we may want to import d3 globally, for now, it's just here--%>
    <script type="text/javascript">
        var oncoPrintParams = {
            cancer_study_id: "<%=cancerTypeId%>",
            case_set_str: "<%=MakeOncoPrint.getCaseSetDescriptionREFACTOR(caseSetId, caseSets)%>",
            num_cases_affected: "<%=dataSummary.getNumCasesAffected()%>" ,
            percent_cases_affected: "<%=MakeOncoPrint.alterationValueToString(dataSummary.getPercentCasesAffected())%>"
        };

        var oncoprint;
        geneAlterations.fire(function(data) {
            oncoPrintParams['geneAlterations_l'] = data;

            oncoprint = OncoPrint(oncoPrintParams);
            oncoprint.insertFullOncoPrint($('#oncoprints'));
        });

    </script>
</div>
