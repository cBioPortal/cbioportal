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

        <%@ page import="org.mskcc.cbio.portal.model.CancerStudyStats" %>
        <%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
        <%@ page import="org.mskcc.cbio.portal.util.DataSetsUtil" %>
        <%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
        <%@ page import="java.util.ArrayList" %>
        <%@ page import="java.util.List" %>

            <%
   String siteTitle = GlobalProperties.getTitle();
%>

            <% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Data Sets"); %>

        <jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
        <div id="main">
        <table cellspacing="2px">
        <tr>
        <td width="100%">
        <div id="datasets" class="cbioportal-frontend"></div>

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
        </form>
        <jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />

        <div id="reactRoot"></div>
        <script type="text/javascript">

        // Set API root variable for cbioportal-frontend repo
            <%
String url = request.getRequestURL().toString();
String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath() + "/";
baseURL = baseURL.replace("https://", "").replace("http://", "");
%>

        __API_ROOT__ = '<%=baseURL%>' + '/api';

        window.loadReactApp({ defaultRoute:'datasets'});

        window.onReactAppReady(function(){
            window.renderDatasetList(document.getElementById('datasets'));
        });
        
        </script>

        </body>
        </html>
