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

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.SessionServiceRequestWrapper" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<%
    String siteTitle = GlobalProperties.getTitle();

    String selectedCancerStudyId =
        (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);

    if (siteTitle == null) {
        siteTitle = "cBioPortal for Cancer Genomics";
    }

%>

<%
    request.setAttribute("index.jsp", Boolean.TRUE);
    request.setAttribute("selectedCancerStudyId", selectedCancerStudyId);
%>

<t:template title="<%=siteTitle%>" cssClass="homePage" defaultRightColumn="true" twoColumn="true" fixedWidth="false">

    <jsp:attribute name="head_area">
        <!-- Include selected_study_variables in the new front-end framework and makre sure rest of legacy still working -->
        <jsp:include page="global/selected_study_variables.jsp"/>
        <jsp:include page="global/server_vars.jsp"/>
        <script>
            window.selectedCancerStudyId = '${selectedCancerStudyId}';
            if (window.selectedCancerStudyId === "all") {
                // This means no study selected
                window.selectedCancerStudyId = "";
            }

            window.loadReactApp({ defaultRoute: 'home' });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="reactRoot"></div>
    </jsp:attribute>


</t:template>
    
    
