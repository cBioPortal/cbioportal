<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>

<%
String patient = (String)request.getAttribute(PatientView.PATIENT_ID);
String patient_view_error = (String)request.getAttribute(PatientView.ERROR);
%>

<jsp:include page="global/header_above_bar.jsp" flush="true" />

<tr>
    <td>
Patient: 
<%
out.print(patient);
if (patient_view_error!=null) {
    out.print("<br/>");
    out.println();
    out.print(patient_view_error);
} else {
    
}
%>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />    
</form>

</body>
</html>