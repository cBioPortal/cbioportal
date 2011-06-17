<%@ page import="org.mskcc.portal.model.CaseSet" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>


<%
    String step3ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP3_ERROR_MSG);
%>

<script type="text/javascript">

    function updateCaseList() {
        var caseSetChoice = YAHOO.util.Dom.get("<%= QueryBuilder.CASE_SET_ID %>");
        var value = caseSetChoice.value;
        var caseIdTextArea = YAHOO.util.Dom.get("caseList");
        if (value == "-1") {
            YAHOO.util.Dom.setStyle(caseIdTextArea, "display", "block");
        } else {
            YAHOO.util.Dom.setStyle(caseIdTextArea, "display", "none");
        }
    }
</script>

<script type="text/javascript" onload="updateCaseList()">
</script>

<table>
    <tr>
        <td><img class="step_image" src="images/step_3<%=stepImageSuffix%>.png" alt="Step 3"></td>
        <td><span class="step">Select Patient/Case Set:</span></td>
        <td>
<%
    if (caseSets.size() == 1) {
        out.println( "</td><tr><td colspan=2><P><div class='error'>No case sets available for selected cancer type.</div></td></tr></table>");
    } else {
        out.println ("&nbsp;<select onchange='JavaScript:updateCaseList()' name='"
            + QueryBuilder.CASE_SET_ID + "' id='" + QueryBuilder.CASE_SET_ID + "'>");
        out.println ("<p>");
    }
%>
<%
    if (caseSets.size() > 1) {
        for (CaseSet caseSet:  caseSets) {
            String selected = "";
            if (caseSetId != null && caseSetId.equals(caseSet.getId())) {
                selected = " selected ";
            }
            out.print("<option " + selected + " value='" + caseSet.getId());
            out.println ("' title='" + caseSet.getDescription() + "'>");
            out.println(caseSet.getName() + "</option>");
        }
        out.println ("</select>");
        out.println ("</td></tr></table>");
        out.println("<div id='caseList'>");
        if (step3ErrorMsg != null) {
            out.println ("<p>&nbsp;<span class='error'>" + step3ErrorMsg + "</span></p>");
        } else {
            out.println ("<p>&nbsp;Enter case IDs below:</p>");
        }
        out.print ("<textarea id='" + QueryBuilder.CASE_IDS +"' name='"
            + QueryBuilder.CASE_IDS + "' rows=6 cols=80>");
        if (caseIds != null) {
            out.print (caseIds);
        } else {
            //CaseSet caseSet = caseSets.get(0);
            //out.println (caseSet.getCaseListAsString());
        }
        out.print ("</textarea>");
        out.println("</div>");
        out.println("</p>");
    }
%>
