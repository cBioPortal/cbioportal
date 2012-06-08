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
<table width=100% cellpadding="0px" cellspacing="5px" border="0px">
  <tr valign="top">
    <td colspan="3">
	 <div id="header_wrapper">
    <div id="header">

       <jsp:include page="header_bar.jsp" flush="true" />
        
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
                    <li class="internal">
					   	<a href="tutorial.jsp">Tutorials</a>
					</li>
                    <% if (SkinUtil.showNewsTab()) { %>
                        <li class="internal">
                            <a href="news.jsp">News</a>
                        </li>
                    <% } %>
                    <li class="internal">
					  	<a href="faq.jsp">FAQ</a>
					</li>
                    <% if (SkinUtil.showDataTab()) { %>
                        <li class="internal">
                            <a href="data_sets.jsp">Data Sets</a>
                        </li>
                    <% } %>
                    <li class="internal">
					   	<a href="about_us.jsp">About</a>
					</li>
                    <%
                        //  Hide the Web API and R/MAT Tabs if the Portal Requires Authentication
                        if (!SkinUtil.usersMustAuthenticate()) {
                    %>
                        <li class="internal">
                            <a href="web_api.jsp">Web API</a>
                        </li>
                        <li class="internal">
                            <a href="cgds_r.jsp">R/MATLAB</a>
                        </li>
                    <% } %>
                    <li class="internal">
                        <a href="networks.jsp">Networks</a>
                    </li>
                    <li>
                        <a href="http://www.twitter.com/cbioportal"><img style="margin-top:5px; margin-bottom:4px"
                            src="images/twitter-b.png" title="Follow us on Twitter" alt="Follow us on Twitter"/></a>
                    </li>
                   <li>
                       <a href="http://groups.google.com/group/cbioportal"><img style="margin-top:4px; margin-bottom:4px;"
                            src="images/google_groups.png" title="Open forum for questions, discussions and suggestions" alt="Google Groups"/></a>
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
