<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<?xml version="1.0" encoding="utf-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	  <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	  <title>cBio Cancer Genomics Portal::OpenID Login</title>

	  <!-- Simple OpenID Selector -->
	  <link type="text/css" rel="stylesheet" href="css/openid.css" />
	  <script type="text/javascript" src="js/jquery-1.4.2.min.js"></script>
	  <script type="text/javascript" src="js/openid-jquery.js"></script>
	  <script type="text/javascript">
	      $(document).ready(function() {
				  openid.init('openid_identifier');
				  //openid.setDemoMode(false); // if true, Stops form submission for client javascript-only test purposes
		  });
      </script>
</head>

<%
    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");

    if (siteTitle == null) {
		siteTitle = "cBio Cancer Genomics Portal";
	}
%>

<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>

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
            <strong>You are not authorized to access this resource.</strong></p>
        </div>
    <% } %>
       <br>
       <table cellspacing="2px">
	     <tr>
	       <td>
	         <!-- Simple OpenID Selector -->
	         <form action="<c:url value='j_spring_openid_security_check'/>" method="post" id="openid_form">
	           <input type="hidden" name="action" value="verify" />
               <p/>
               <fieldset>
	             <legend>Access to this portal is only available to authorized users.  To request access,
                 please send email to:  <%= SkinUtil.getEmailContact() %>.</legend>
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
	         <!-- /Simple OpenID Selector -->
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
