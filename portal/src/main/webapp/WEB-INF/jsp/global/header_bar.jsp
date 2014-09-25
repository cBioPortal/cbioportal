<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%
	String principal = "";
    String authenticationMethod = GlobalProperties.authenticationMethod();
	if (authenticationMethod.equals("openid")) {
		principal = "principal.name";
	}
	else if (authenticationMethod.equals("googleplus") || authenticationMethod.equals("saml")) {
		principal = "principal.username";
	}
	String tagLineImage = (authenticationMethod.equals("saml")) ?
		"/" + GlobalProperties.getTagLineImage() : GlobalProperties.getTagLineImage();
	pageContext.setAttribute("tagLineImage", tagLineImage);
%>
<table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
	<tr valign="middle">
		<td valign="middle" width="25%">
                    <img src="<c:url value="/images/cbioportal_logo.png"/>" height="50px" alt="cBioPortal Logo">
		</td>
		<td valign="middle" align="center" width="50%">
			<img src="<c:url value="${tagLineImage}"/>" alt="Tag Line" style="max-height: 50px;">
		</td>
		<td valign="middle" align="right" width="25%">
			<a href="http://www.mskcc.org"><img src="<c:url value="/images/mskcc_logo_3d_grey.jpg"/>" height="50px" alt="MSKCC Logo"></a>
		</td>
	</tr>
    <!-- Display Sign Out Button for Real (Non-Anonymous) User -->
    <sec:authorize access="!hasRole('ROLE_ANONYMOUS')">
	<tr>
		<td></td><td></td>
        <td align="right" style="font-size:10px;background-color:white">
        <% if (authenticationMethod.equals("saml")) { %>
        You are logged in as <sec:authentication property='<%=principal%>' />. <a href="<c:url value="/saml/logout"/>">Sign out</a>.
        <%} else { %>
        You are logged in as <sec:authentication property='<%=principal%>' />. <a href="j_spring_security_logout">Sign out</a>.
        <% } %>
        </td>
    </tr>

    </sec:authorize>
</table>