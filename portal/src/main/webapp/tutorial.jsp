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
    
<%
String siteTitle = GlobalProperties.getTitle() + "::Video Tutorial";
%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:template title="<%= siteTitle %>">

    <jsp:attribute name="head_area">
        <script>
        window.loadReactApp({ defaultRoute: 'home' });

        window.onReactAppReady(function(){
        //window.renderQuerySelector(document.getElementById('querySelectorTarget'));
        window.renderRightBar(document.getElementById('rightColumn'));
        });

        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="reactRoot" class="hidden"></div>
        
        <h1>Tutorials &amp; Guides</h1>
        
        <h3>Step-by-step Guide to cBioPortal: a Protocol Paper</h3>
        <p>Gao, Aksoy, Dogrusoz, Dresdner, Gross, Sumer, Sun, Jacobsen, Sinha, Larsson, Cerami, Sander, Schultz. <br/>
        <b>Integrative analysis of complex cancer genomics and clinical profiles using the cBioPortal.</b> <br/>
        <i>Sci. Signal.</i> 6, pl1 (2013).
        [<a href="http://www.ncbi.nlm.nih.gov/pubmed/23550210">Reprint</a>].</p>

        <hr/>
        
        <h3>Tutorial:  Getting Started</h3>
       
        <p>
            <a href="http://www.slideshare.net/EthanCerami/cbio-cancer-genomics-portal-getting-started" title="cBioPortal for Cancer Genomics: Getting started" target="_blank">cBioPortal for Cancer Genomics: Getting started</a>
        </p>
        <iframe style="border:1px solid #999" src="http://www.slideshare.net/slideshow/embed_code/10438088" width="595" height="497" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe> 
        
        <p>View more <a href="http://www.slideshare.net/" target="_blank">presentations</a> from <a href="http://www.slideshare.net/EthanCerami" target="_blank">Ethan Cerami</a> </p>

        <hr/>
        
        <h3>Tutorial:  Network View</h3>
        <p><a href="http://www.slideshare.net/EthanCerami/network-view" title="cBioPortal for Cancer Genomics: Network View" target="_blank">cBioPortal for Cancer Genomics: Network View</a>
        </p>
        <iframe style="border:1px solid #999" src="http://www.slideshare.net/slideshow/embed_code/10579031" width="595" height="497" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe> 
        <p>View more <a href="http://www.slideshare.net/" target="_blank">presentations</a> from <a href="http://www.slideshare.net/EthanCerami" target="_blank">Ethan Cerami</a> </p>
  
    </jsp:attribute>


</t:template>




