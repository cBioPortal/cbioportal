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
            <strong>You are not authorized to access this resource. 
            You have attempted to log in as <%= DynamicState.INSTANCE.getCurrentUser() %>.
            If you think you have received this
            message in error, please contact us at
            <a style="color:#FF0000" href="mailto:cbioportal@cbio.mskcc.org">cbioportal@cbio.mskcc.org</a>
           

            </strong></p>
    </div>
    <% } %>
    <br>

        <table cellspacing="2px" width="100%">
            <tr>
                <td>
                    <fieldset>
                        <legend>
                            Login to Portal:
                        </legend>
                        <p>
                            <span style="font-size:145%">
                                <%= GlobalProperties.getAuthorizationMessage() %>
                            </span>
                        </p>

                        <p>
                            <button onclick="window.location = 'auth/google'" style="padding: 0; border:none; background: none" >
                                <IMG alt="Google+" src="images/login/images.large/googleplus_signin.png"  /></button>
                        </p>
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
<<<<<<< local
    <% } %>
       <br>
          
       <table cellspacing="2px" width="100%">
           <tr>
               <td>
                 <fieldset>
	             <legend>
                     Login to Portal:
                 </legend>
                 <p>
                     <span style="font-size:145%">
                     <%= GlobalProperties.getAuthorizationMessage() %>
                     </span>
                 </p>
           
                      <div background-color:white>
                   <button onclick="window.location='auth/google'"><IMG alt="Google+" src="images/login/images.large/googleplus_signin.png" /></button>
                   </div>
               </td>
          
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
=======
        <jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />
        </body>
>>>>>>> other
</html>
