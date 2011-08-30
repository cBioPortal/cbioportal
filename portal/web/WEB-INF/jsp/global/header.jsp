<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

<jsp:include page="css_include.jsp" flush="true" />
<jsp:include page="js_include.jsp" flush="true" />

    <script type="text/javascript">
        $(document).ready(function(){
            $(".oncoprint_help").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});
        });
    </script>
    <title><%= request.getAttribute(QueryBuilder.HTML_TITLE)%></title>
</head>

<center>
<div id="page_wrapper">
<table width="860px" cellpadding="0px" cellspacing="5px" border="0px">
  <tr valign="top">
    <td colspan="3">
	 <div id="header_wrapper">
    <div id="header">

        <table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
    	<tr>
	        <td class="logo" width="250px"><a href="http://www.mskcc.org"><img src="images/msk_logo.png" alt="MSKCC Logo"/></a></td>
            <td class="logo" width="330px"><a href="index.do"><img src="<%= SkinUtil.getHeaderImage() %>" alt="Main Logo"/></a></td>
            <td class="logo" width="200px"><img src="<%= SkinUtil.getTagLineImage() %>" alt="Tag Line"/>
                <%
                   if (SkinUtil.usersMustAuthenticate()) {
                %>
                <nobr><span style="float:right;font-size:10px;">You are logged in as <sec:authentication property='principal.name' />. <a href="j_spring_security_logout">Sign out</a>.</span></nobr>
                <% } %>
            </td>
        </tr>
        </table>
       <table width="100%">
        <tr>
            <td class="navigation">
               <ul>
                    <li class="selected">
					    <a href="index.do">Home</a>
					</li>
					<li class="internal" id="results">
					    <a href="#">Results</a>
					</li>
                    <!--
                    <li class="internal">
					   	<a href="video.jsp">Tutorial</a>
					</li>
					-->
					<li class="internal">
					   	<a href="news.jsp">News</a>
					</li>
					<li class="internal">
					  	<a href="faq.jsp">FAQ</a>
					</li>
                    <li class="internal">
					   	<a href="data_sets.jsp">Data Sets</a>
					</li>
                    <li class="internal">
					   	<a href="about_us.jsp">About</a>
					</li>
                    <li class="internal">
					   	<a href="web_api.jsp">Web API</a>
					</li>
					<li class="internal">
					   	<a href="cgds_r.jsp">R Package</a>
					</li>
                    <li class="internal">
                        <a href="networks.jsp">Networks</a>
                    </li>
                    <li>
                        <a href="http://www.twitter.com/cbioportal"><img style="margin-top:5px; margin-bottom:4px"
                            src="images/twitter-b.png" title="Follow us on Twitter" alt="Follow us on Twitter"/></a>
                    </li>
                   <li>
                       <a href="http://cbio.mskcc.org"><img style="margin-top:6px; margin-bottom:4px; margin-right:-3px"
                            src="images/cbioLogo.png" title="cBio@MSKCC" alt="cBio@MSKCC"/></a>
                   </li>
				 </ul> 
            </td>
        </tr>
    </table>
    <!-- End DIV id="header" -->
    </div>

    <!-- End DIV id="header_wrapper" -->    
    </div>
	</td>
  </tr>

  <tr valign="top">
    <td>
        <div id="content">
