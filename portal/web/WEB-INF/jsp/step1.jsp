<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%
    String stepImageSuffix = globalConfig.getProperty("step_image_suffix");

    if (stepImageSuffix == null) {
        stepImageSuffix = "";
    }

%>
<P>
<table>
    <tr>
        <td><img class="step_image" src="images/step_1<%=stepImageSuffix%>.png" alt="Step 1:"></td>
        <td><span class="step">Select Cancer Type:</span></td>
        <td>
            <select onchange="JavaScript:submit()" name="<%= QueryBuilder.CANCER_TYPE_ID %>">
            <%
                for (CancerType cancerType : cancerTypeList) {
                    String selected = "";
                    if (cancerTypeId.equals(cancerType.getCancerTypeId())) {
                        selected = " selected ";
                    }
                    out.print("<option " + selected + " value='" + cancerType.getCancerTypeId() + "'>");
                    out.println(cancerType.getCancerName() + "</option>");
                }
            %>
            </select>
        </td>
    </tr>
</table>
<div id="cancer_type_desc">
<p>
<%
    for (CancerType cancerType : cancerTypeList) {
        if (cancerTypeId.equals(cancerType.getCancerTypeId())) {
            out.println(cancerType.getDescription());
        }
    }
%>
</p>
</div>
    
