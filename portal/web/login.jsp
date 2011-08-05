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

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Login/Logout"); %>
<jsp:include page="WEB-INF/jsp/global/login_header.jsp" flush="true" />
<div id="main">
	   <table cellspacing="2px">
		 <c:if test="${not empty param.login_error}">
		   <tr>
	         <td>
	           <h1>&nbsp</h1>
			   <font color="red">
				 Your login attempt was not successful, try again.<br/><br/>
				 Reason: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
			   </font>
			 </td>
           </tr>
		   <tr><td>&nbsp</td></tr>
		 </c:if>
	     <tr>
	       <td>
			 <c:if test="${empty param.login_error}">
	           <h1>&nbsp</h1>
			 </c:if>
	         <!-- Simple OpenID Selector -->
	         <form action="<c:url value='j_spring_openid_security_check'/>" method="post" id="openid_form">
	           <input type="hidden" name="action" value="verify" />
	           <fieldset>
	             <legend>Access to this portal is only available to authorized users.</legend>
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
</div>
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
