<%@ page import="org.mskcc.cbio.portal.util.DynamicState" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
        <title><%= GlobalProperties.getTitle() %>::cBioPortal Login</title>

        <script type="text/javascript" src="js/lib/jquery-1.4.2.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
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
        <% } %>

    </head>

    <%
        String siteTitle = GlobalProperties.getTitle();
    %>

    <%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
    <%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

    <% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Login/Logout"); %>
    <jsp:include page="WEB-INF/jsp/global/login_header.jsp" flush="true" />

    <%
        String login_error = request.getParameter("login_error");
        String logout_success = request.getParameter("logout_success");
    %>

    <% if (logout_success != null) { %>
    <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:90%;margin-top:50px">
        <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
            <strong>You are now signed out.</strong></p>
    </div>
    <% } %>
    <% if (login_error != null) { %>

        <div class="ui-state-highlight ui-corner-all" style="padding: 0 .7em;width:90%;margin-top:50px">
            <p><span class="ui-icon ui-icon-info" style="float: left; margin-right: .3em;"></span>
            <strong>You are not authorized to access this resource.&nbsp;
            <% if (authenticationMethod.equals("googleplus")) { %>
            You have attempted to log in as <%= DynamicState.INSTANCE.getFailedUser() %>.
            <% } %>
            If you think you have received this message in error, please contact us at <a style="color:#FF0000" href="mailto:cbioportal-access@cbio.mskcc.org">cbioportal-access@cbio.mskcc.org</a>
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
                <% } %>
                    <fieldset>
                    <legend style="width:96px;border-bottom:none;color:#666666;font-family:verdana,arial,sans-serif;font-size:12px;">
                        Login to Portal:
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
                            <button type="button" class="btn btn-danger btn-lg" onclick="window.location = 'saml/login?idp=https://msklogin.mskcc.org/nidp/saml2/metadata'" >
                            Sign in with MSK</button>
                        </p>
                    </fieldset>
                    <% } else if (authenticationMethod.equals("googleplus")) { %>
                        <p>
                            <button onclick="window.location = 'auth/google'" style="padding: 0; border:none; background: none" >
                                <IMG alt="Google+" src="images/login/googleplus_signin.png"  /></button>
                        </p>
                    </fieldset>
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
        </center>
        </div>
        <jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
        </body>
</html>
