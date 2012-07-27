<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

<jsp:include page="css_include.jsp" flush="true" />
<jsp:include page="js_include.jsp" flush="true" />

<title><%= request.getAttribute(QueryBuilder.HTML_TITLE)%></title>

</head>

<center>
<div id="page_wrapper">
<table width="860px" cellpadding="0px" cellspacing="5px" border="0px">
  <tr valign="top">
    <td colspan="3">
    <div id="login_header_wrapper">
    <div id="login_header_top">
    <jsp:include page="header_bar.jsp" flush="true" />
    </div>
    </div>
	</td>
  </tr>

  <tr valign="top">
    <td>
        <div>