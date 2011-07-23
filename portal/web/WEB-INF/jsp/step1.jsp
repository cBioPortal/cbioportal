<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%
    String stepImageSuffix = globalConfig.getProperty("step_image_suffix");

    if (stepImageSuffix == null) {
        stepImageSuffix = "";
    }

%>
<br/>
<table>
    <tr>
        <td><span class="step">Select Cancer Type:</span></td>
        <td>
            <select onchange="JavaScript:submit()" name="<%= QueryBuilder.CANCER_STUDY_ID %>">
            <%
                for (CancerType cancerType : cancerTypeList) {
                    String selected = "";
                    if (cancerTypeId.equals(cancerType.getCancerTypeId())) {
                        selected = " selected=\"selected\" ";
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
    
