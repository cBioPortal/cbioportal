<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
	<tr valign="middle">
		<td valign="middle" width="25%">
			<a href="http://www.mskcc.org"><img src="images/msk_logo.png" alt="MSKCC Logo"></a>
		</td>
		<td valign="middle" width="50%">
			<span id="header_site_name">
				<center>
				<%= SkinUtil.getTitle() %>
				</center>
			</span>
		</td>
		<td valign="middle" width="25%">
			<img src="<%= SkinUtil.getTagLineImage() %>" alt="Tag Line">
		</td>
	</tr>
    <%
       if (SkinUtil.usersMustAuthenticate()) {
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