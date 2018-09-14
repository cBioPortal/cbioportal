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
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
  
<%
    String siteTitle = GlobalProperties.getTitle() + "::FAQ";
    String appVersion = GlobalProperties.getAppVersion();
%>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:template title="<%= siteTitle %>" defaultRightColumn="false" fixedWidth="true">

    <jsp:attribute name="head_area">
        <!-- js files: -->
        <script>
        window.loadReactApp({ defaultRoute: 'blank' });
        
        var to;
        
        function setIframeHeight() {
            document.getElementById("faqIframe").style.height=(window.innerHeight - 250) + "px";
        }
        
        window.addEventListener("resize", function() {
            clearTimeout(to);
            
            to = setTimeout(function(){
                setIframeHeight();
            },200);
        });
        
        
        window.addEventListener("load", function(){
            setIframeHeight();
        });
        
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="reactRoot" class="hidden"></div>
        <iframe id="faqIframe" style="width:100%;border:1px solid #ddd;" src="https://docs.google.com/document/d/e/2PACX-1vSWTtIJZF2tuBimihr8ke-d00DpKh7fydFIQb5xYpE_bMYM9hZyY9OP1Vz1Ts0ow7ob-3h2S19cuB5O/pub?embedded=true"></iframe>
    </jsp:attribute>


</t:template>


