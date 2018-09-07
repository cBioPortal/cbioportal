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

<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%= GlobalProperties.getTitle() %>::cBioPortal Login</title>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<%@ page import="java.lang.Exception" %>
<%@ page import="org.springframework.security.web.WebAttributes" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<jsp:include page="WEB-INF/jsp/global/css_include.jsp" flush="true" />
<jsp:include page="WEB-INF/jsp/global/js_include.jsp" flush="true" />
<%
    String authenticationMethod = GlobalProperties.authenticationMethod();
    if (authenticationMethod.equals("openid")) {
%>
    <link type="text/css" rel="stylesheet" href="css/openid.css" />
    <script type="text/javascript" src="js/lib/openid-jquery.js"></script>
    <script type="text/javascript">
      $(document).ready(function() {
              openid.init('openid_identifier');
              //openid.setDemoMode(false); // if true, Stops form submission for client javascript-only test purposes
      });
    </script>
<%
    }
   String siteTitle = GlobalProperties.getTitle();
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Login/Logout"); %>
<%
    String login_error = request.getParameter("login_error");
    String logout_success = request.getParameter("logout_success");
%>
</head>
<body>
  <center>
  <div id="page_wrapper">
  <table width="860px" cellpadding="0px" cellspacing="5px" border="0px">
    <tr valign="top">
      <td colspan="3">
        <div id="login_header_wrapper">
          <div id="login_header_top">
            <jsp:include page="WEB-INF/jsp/global/header_bar.jsp" flush="true" />
          </div>
        </div>
      </td>
    </tr>

    <tr valign="top">
      <td>
        <div>

          <% if (logout_success != null) { %>
          <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:90%;margin-top:50px">
            <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
            <strong>You are now signed out.   It is recommended that you close your browser to complete the termination of this session.</strong></p>
          </div>
          <% } %>

          <% if (login_error != null) { %>
          <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:90%;margin-top:50px">
            <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
            <strong>You are not authorized to access this resource.&nbsp;

              <% if (authenticationMethod.equals("googleplus")) { 
                    Exception lastException = (Exception) request.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                    if (lastException != null) {
                        %>
                            You have attempted to log in as <%= lastException.getMessage() %>.
                        <%
                    }
                 }
              %>

              <!-- removed hard-coded login contact html, instead calling GlobalProperties -->
              <%= GlobalProperties.getLoginContactHtml() %>
            </strong></p>
          </div>
          <% } %>

          <br>

          <table cellspacing="2px" width="100%">
            <tr>
              <td>
                <% if (authenticationMethod.equals("openid")) { %>
                  <!-- Simple OpenID Selector -->
                  <form style="width:  100%;" action="<c:url value='j_spring_openid_security_check'/>" method="post" id="openid_form">
                  <input type="hidden" name="action" value="verify" />
                  <p/>

                <% } else if (authenticationMethod.equals("ad") || authenticationMethod.equals("ldap")) { %>
                  <form name='loginForm' action="<c:url value='j_spring_security_check' />" method='POST'>
                <% } %>

                <fieldset id="login-fieldset">
                  <legend>
                      Login to cBioPortal:
                  </legend>
                  <p>
                    <span style="color:#666666;font-family:verdana,arial,sans-serif;font-size:145%">
                      <%= GlobalProperties.getAuthorizationMessage() %>
                    </span>
                  </p>

                <% if (authenticationMethod.equals("openid")) { %>
                  <div id="openid_choice">
                    <p>Please click your account provider:</p>
                    <div id="openid_btns"></div>
                  </div>
                  <div id="openid_input_area">
                    <input id="openid_identifier" name="openid_identifier" type="text" value="http://" />
                    <input id="openid_submit" type="submit" value="Sign-In"/>
                  </div>
                  <noscript>
                    <p>OpenID is a service that allows you to log-on to many different websites using a single identity.
                    Find out <a href="http://openid.net/what/">more about OpenID</a> and <a href="http://openid.net/get/">how to get an OpenID enabled account</a>.</p>
                  </noscript>
                </fieldset>
                </form>

                <% } else if (authenticationMethod.equals("saml")) { %>
                  <p>
                    <!-- removed the hard-coded saml registration html and calling GlobalProperties instead -->
                    <button id="saml_login_button" type="button" class="btn btn-danger btn-lg" onclick="window.location = 'saml/login?idp=<%= GlobalProperties.getSamlIdpMetadataEntityid() %>'" >
                    <%= GlobalProperties.getLoginSamlRegistrationHtml() %></button>
                  </p>
                </fieldset>

                <% } else if (authenticationMethod.equals("googleplus") || authenticationMethod.equals("social_auth")) { %>
                  <p>
                    <button onclick="window.location = 'auth/google'" style="padding: 0; border:none; background: none" >
                        <!-- we need alt != "Google+" because otherwise it gets hidden by Ad Block Plus chrome plugin -->
                      <IMG alt="cBioPortal Google+ Log-in" src="images/login/googleplus_signin.png"  />
                    </button>
                  </p>
                </fieldset>

                <% } else if (authenticationMethod.equals("ad") || authenticationMethod.equals("ldap")){ %>
                  <div>
                    <label for=username>Username: </label> <input type='text' id='username' name='j_username' value=''>  <br/>
                    <label for=password>Password: </label> <input type='password' name='j_password' /> <br/>
                    <input name="submit" type="submit" value="submit" />
                  </div>
                </fieldset>
                </form>
                <% } %>

              </td>
            </tr>
          </table>
        </td>
      </tr>

      <tr>
        <td colspan="3">
          <jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
        </td>
      </tr>

    </table>
  </div>
  </center>

  <jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />

</body>
</html>
