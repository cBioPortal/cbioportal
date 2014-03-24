<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />

<%if(request.getAttribute("tumormap")!=null){%>
<jsp:include page="css_include_standard.jsp" flush="true" />
<jsp:include page="js_include_standard.jsp" flush="true" />
<%} else {%>
<jsp:include page="css_include.jsp" flush="true" />
<jsp:include page="js_include.jsp" flush="true" />
<%}%>
<jsp:include page="js_include_analytics_and_email.jsp" flush="true" />

    <script type="text/javascript">
        $(document).ready(function(){
            $(".oncoprint_help").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});


        });
    </script>
    <title><%= request.getAttribute(QueryBuilder.HTML_TITLE)%></title>
</head>

<center>
    <div id="page_wrapper">
        <table id="page_wrapper_table" width=100% cellpadding="0px" cellspacing="5px" border="0px">
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
                                            <% if (GlobalProperties.showDataTab()) { %>
                                            <li class="internal">
                                                <a href="data_sets.jsp">Data Sets</a>
                                            </li>
                                            <% } %>
                                            <%
                                                //  Hide the Web API and R/MAT Tabs if the Portal Requires Authentication
                                                if (!GlobalProperties.usersMustAuthenticate()) {
                                            %>
                                            <li class="internal">
                                                <a href="web_api.jsp">Web API</a>
                                            </li>
                                            <li class="internal">
                                                <a href="cgds_r.jsp">R/MATLAB</a>
                                            </li>
                                            <% } %>
                                            <li class="internal" id="results">
                                                <a href="#">Results</a>
                                            </li>
                                            <li class="internal">
                                                <a href="tutorial.jsp">Tutorials</a>
                                            </li>
                                            <li class="internal">
                                                <a href="faq.jsp">FAQ</a>
                                            </li>
                                            <% if (GlobalProperties.showNewsTab()) { %>
                                            <li class="internal">
                                                <a href="news.jsp">News</a>
                                            </li>
                                            <% } %>
                                            <!--li class="internal">
                                                <a href="tools.jsp">Tools</a>
                                            </li-->
                                            <li class="internal">
                                                <a href="about_us.jsp">About</a>
                                            </li>
					    
                                            <li class="internal" style="float:right">
					    <a href="visualize_your_data.jsp" float="right"><b><i>VISUALIZE YOUR DATA</i></b></a>
					    </li>
					    
                                            <li class="internal" style="float:right">
					    <a href="jobs.jsp" float="right"><b><i>JOBS</i></b></a>
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
                <td id="td-content">
                    <div id="content">
