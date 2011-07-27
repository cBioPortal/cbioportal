<!-- Include Dynamic Query Javascript -->
<script type="text/javascript" src="js/dynamicQuery.js"></script>

<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<script type="text/javascript">

    // Store the current selected options as global variables;
    window.cancer_study_id_selected = '<%= cancerTypeId%>';
    window.case_set_id_selected = '<%= caseSetId %>';
    window.gene_set_id_selected = '<%= geneSetChoice %>';

</script>

<div class="main_query_panel">
    <%@ include file="step1_json.jsp" %>
    <%@ include file="step2_json.jsp" %>
    <%@ include file="step3_json.jsp" %>
    <%@ include file="step4_json.jsp" %>
    <%@ include file="step5_json.jsp" %>
    <p/>
    <input type=submit name="<%= QueryBuilder.ACTION%>" value="<%= QueryBuilder.ACTION_SUBMIT %>"/>

    <!--
    <p><small><a id='json_cancer_studies' href="">Toggle Experimental JSON Results</a></small></p>
    <div class="markdown" style="display:none;" id="cancer_results">
    </div>
    -->
</div>
