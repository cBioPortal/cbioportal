<div id="oncoprints">
    <script type="text/javascript" src="js/oncoprint.js"></script>
    <script type="text/javascript" src="js/d3.v2.min.js"></script>
    <%--todo: we may want to import d3 globally, for now, it's just here--%>
    <script type="text/javascript">
        var oncoPrintParams = {
            cancer_study_id: "<%=cancerTypeId%>",
            case_set_str: "<%=MakeOncoPrint.getCaseSetDescriptionREFACTOR(caseSetId, caseSets)%>",
            num_cases_affected: "<%=dataSummary.getNumCasesAffected()%>" ,
            percent_cases_affected: "<%=MakeOncoPrint.alterationValueToString(dataSummary.getPercentCasesAffected())%>"
        };

        geneAlterations.fire(function(data) {
            oncoPrintParams['geneAlterations_l'] = data;

            var oncoprint = OncoPrint(oncoPrintParams);
            oncoprint.insert($('#oncoprints'));
        });

    </script>
</div>
