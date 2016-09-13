<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%
	String principal = "";
    String authenticationMethod = GlobalProperties.authenticationMethod();
	if (authenticationMethod.equals("openid") || authenticationMethod.equals("ldap")) {
		principal = "principal.name";
	}
	else if (authenticationMethod.equals("googleplus") || authenticationMethod.equals("saml") || authenticationMethod.equals("ad")) {
		principal = "principal.username";
	}
	String tagLineImage = (authenticationMethod.equals("saml")) ?
		"/" + GlobalProperties.getTagLineImage() : GlobalProperties.getTagLineImage();
	pageContext.setAttribute("tagLineImage", tagLineImage);

    // retrieve right-logo from global properties. Based on the tagLineImage code.
    String rightLogo = (authenticationMethod.equals("saml")) ?
            "/" + GlobalProperties.getRightLogo() : GlobalProperties.getRightLogo();
    pageContext.setAttribute("rightLogo", rightLogo);
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
            <img src="<c:url value="${rightLogo}"/>" alt="Institute Logo" style="max-height: 50px;">
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