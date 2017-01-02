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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<!DOCTYPE HTML>
<html ng-app="menu" lang="eng">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">

<%if(request.getAttribute("standard-js-css")!=null){%>
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
    
        // Set API root variable for cbioportal-frontend repo
        <%
        String url = request.getRequestURL().toString();
        String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
        baseURL = baseURL.replace("https://", "").replace("http://", "");
        %>
        __API_ROOT__ = '<%=baseURL%>' + '/api';
    
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
                                            <!-- Added call GlobalProperties to check whether to show the Web API tab -->
                                            <% if (GlobalProperties.showWebApiTab()) { %>
                                            <li class="internal">
                                                <a href="web_api.jsp">Web API</a>
                                            </li>
                                            <% } %>
                                            <!-- Added call GlobalProperties to check whether to show the R Matlab tab -->
                                            <% if (GlobalProperties.showRMatlabTab()) { %>
                                            <li class="internal">
                                                <a href="cgds_r.jsp">R/MATLAB</a>
                                            </li>
                                            <% } %>
                                            <% } %>
                                            <li class="internal" id="results">
                                                <a href="#">Results</a>
                                            </li>
                                            <!-- Added call GlobalProperties to check whether to show the Tutorials tab -->
                                            <% if (GlobalProperties.showTutorialsTab()) { %>
                                            <li class="internal">
                                                <a href="tutorial.jsp">Tutorials</a>
                                            </li>
                                            <% } %>
                                            <!-- Added call GlobalProperties to check whether to show the Faqs tab -->
                                            <% if (GlobalProperties.showFaqsTab()) { %>
                                            <li class="internal">
                                                <a href="faq.jsp">FAQ</a>
                                            </li>
                                            <% } %>
                                            <% if (GlobalProperties.showNewsTab()) { %>
                                            <li class="internal">
                                                <a href="news.jsp">News</a>
                                            </li>
                                            <% } %>
                                            <!-- Added call GlobalProperties to check whether to show the Tools tab -->
                                            <% if (GlobalProperties.showToolsTab()) { %>
                                            <li class="internal">
                                                <a href="tools.jsp">Tools</a>
                                            </li>
                                            <% } %>
                                            <!-- Added call GlobalProperties to check whether to show the About tab -->
                                            <% if (GlobalProperties.showAboutTab()) { %>
                                            <li class="internal">
                                                <a href="about_us.jsp">About</a>
                                            </li>
                                            <% } %>
                                            <!-- Added for adding custom header tabs. If the customPageArray is not
                                            null, creates list items for the elements in the array. -->
                                            <%
                                            String [] customPagesArray = GlobalProperties.getCustomHeaderTabs();
                                            if(customPagesArray!=null){
                                                // as the customPagesArray should have an even length, there's a problem
                                                // if the length is uneven. In that case, don't add the last page.
                                                // This way, the user will still get feedback for the other customPages
                                                int until=customPagesArray.length - customPagesArray.length%2;
                                                for(int i=0; i<until; i=i+2){ %>
                                                    <li class="internal">
                                                        <a href="<%=customPagesArray[i].trim()%>"><%=customPagesArray[i+1].trim()%></a>
                                                    </li>
                                                <%}
                                            }%>

                                            <!-- Added call GlobalProperties to check whether to show the Visualize tab -->
                                            <% if (GlobalProperties.showVisualizeYourDataTab()) { %>
                                            <li class="internal" style="float:left">
					                            <a href="visualize_your_data.jsp" float="left">VISUALIZE YOUR DATA</a>
					                        </li>
                                            <% } %>

                                            <!--li class="internal" style="float:right">
					    <a href="jobs.jsp" float="right"><b><i>JOBS</i></b></a>
					    </li-->
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
