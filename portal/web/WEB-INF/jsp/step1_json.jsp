<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>

<div class="query_step_section">
<span class="step_header">Select Cancer Study:</span>
<select id="select_cancer_type" name="<%= QueryBuilder.CANCER_STUDY_ID %>">
</select>

<!-- This div shows the cancer description -->
<div id="cancer_study_desc">
</div>
</div>    
    
