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

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%
    String principal = "";
    String samlLogoutURL = "/saml/logout?local=" + GlobalProperties.getSamlIsLogoutLocal();
    String authenticationMethod = GlobalProperties.authenticationMethod();
    if (authenticationMethod.equals("openid") || authenticationMethod.equals("ldap")) {
        principal = "principal.name";
    }
    else if (authenticationMethod.equals("googleplus") || authenticationMethod.equals("saml") || authenticationMethod.equals("ad")) {
        principal = "principal.username";
    }

    // retrieve right-logo from global properties. Based on the tagLineImage code.
    String rightLogo = (authenticationMethod.equals("saml")) ?
            "/" + GlobalProperties.getRightLogo() : GlobalProperties.getRightLogo();
    pageContext.setAttribute("rightLogo", rightLogo);
%>

<header>
    <a id="cbioportal-logo" href="index.do"><img src="<c:url value="/images/cbioportal_logo.png"/>" height="55px" alt="cBioPortal Logo" /></a>    
    <div id="header">
        <div id="authentication">
            <!-- Display Sign Out Button for Real (Non-Anonymous) User -->
            <sec:authorize access="!hasRole('ROLE_ANONYMOUS')">
                <p>
                    <span>You are logged in as <span class="username"><sec:authentication property='<%=principal%>' /></span> | 
                    <% if (authenticationMethod.equals("saml")) { %>
                        <a href="<c:url value="/saml/logout?local=true"/>">Sign out</a>
                    <%} else { %>
                        <a href="j_spring_security_logout">Sign out</a>
                    <% } %>
                    </span>
                </p>
            </sec:authorize>

            <% if (rightLogo != "") { %>
                <img id="institute-logo" src="<c:url value="${rightLogo}"/>" alt="Institute Logo" />
            <% } %>
        </div>

        <nav id="main-nav">
            <ul>
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
                <li class="internal">
                    <a href="visualize_your_data.jsp" float="left">Visualize Your Data</a>
                </li>
                <% } %>
            </ul>
        </nav>
    </div>
</header>