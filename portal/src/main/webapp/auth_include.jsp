<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%
    String principal = "";
    String authenticationMethod = GlobalProperties.authenticationMethod();
    pageContext.setAttribute("authenticationMethod", authenticationMethod);
    if (authenticationMethod.equals("openid") || authenticationMethod.equals("ldap")) {
        principal = "principal.name";
    }
    else if (authenticationMethod.equals("googleplus") ||
	    		authenticationMethod.equals("saml") ||
	    		authenticationMethod.equals("ad") ||
	    		authenticationMethod.equals("social_auth")) {
        principal = "principal.username";
    }
    pageContext.setAttribute("principal", principal);
%>

<script>
<sec:authorize access="!hasRole('ROLE_ANONYMOUS')">
     window.frontendConfig.authUserName = "<sec:authentication property="${principal}" />";
    <c:choose>
        <c:when test="${authenticationMethod == 'saml'}">
             window.frontendConfig.authLogoutUrl = "/saml/logout";
        </c:when>
        <c:otherwise>
             window.frontendConfig.authLogoutUrl = "j_spring_security_logout";
        </c:otherwise>
    </c:choose>
</sec:authorize>

<% if (authenticationMethod.equals("social_auth")) { %>
    <sec:authorize access="hasRole('ROLE_ANONYMOUS')">
        window.frontendConfig.authGoogleLogin = true; 
    </sec:authorize>

<% } %> 
</script>
        
        