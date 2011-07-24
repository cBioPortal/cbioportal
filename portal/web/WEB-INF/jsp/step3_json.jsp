<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>

<div class="query_step_section">
    <span class="step_header">Select Patient/Case Set:</span>
    <select id="select_case_set" name="<%= QueryBuilder.CASE_SET_ID %>"></select>

    <div id='custom_case_list_section' style="display:none;">
        <p>&nbsp;Enter case IDs below:</p>
        <textarea id='custom_case_set_ids' name='<%= QueryBuilder.CASE_IDS %>' rows=6 cols=80>
        </textarea>
    </div>
</div>
