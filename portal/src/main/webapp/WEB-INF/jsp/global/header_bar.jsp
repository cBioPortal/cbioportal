<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
	<tr valign="middle">
		<td valign="middle" width="25%">
                    <img src="images/cbioportal_logo.png" height="50px" alt="cBioPortal Logo">
		</td>
		<td valign="middle" align="center" width="50%">
			<img src="<%= GlobalProperties.getTagLineImage() %>" alt="Tag Line">
		</td>
		<td valign="middle" align="right" width="25%">
			<a href="http://www.mskcc.org"><img src="images/msk_logo.png" height="50px" alt="MSKCC Logo"></a>
		</td>
	</tr>
    <%
       if (GlobalProperties.usersMustAuthenticate()) {
    %>
    <!-- Display Sign Out Button for Real (Non-Anonymous) User -->
    <sec:authorize access="!hasRole('ROLE_ANONYMOUS')">
	<tr>
		<td colspan=3>
            <br><nobr><span style="float:right;font-size:10px;">You are logged in as <sec:authentication property='principal.name' />. <a href="j_spring_security_logout">Sign out</a>.</span></nobr>
        </td>
    </tr>
    </sec:authorize>
    <% } %>
</table>