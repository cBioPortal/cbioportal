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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    

<%
    String siteTitle = GlobalProperties.getTitle() + "::Tools";
%>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:template title="<%= siteTitle %>" defaultRightColumn="true" fixedWidth="true">

    <jsp:attribute name="head_area">
        <script>
        window.loadReactApp({ defaultRoute: 'home' });
        window.onReactAppReady(function(){
        window.renderRightBar(document.getElementById('rightColumn'));
        });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="reactRoot" class="hidden"></div>

        <h1>cBioPortal Tools</h1>
        
        <p>The following tools are for visualization and analysis of custom datasets. When using these tools in your publication,
        <b>please cite</b> <a href="http://www.ncbi.nlm.nih.gov/pubmed/23550210">Gao et al. <i>Sci. Signal.</i> 2013</a>
        &amp;amp;  <a href="http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract">Cerami et al. <i>Cancer Discov.</i> 2012</a>.</p>

        <hr />
   
        <h3><a href="oncoprinter.jsp">OncoPrinter</a></h3>
        <p>Generates oncoprints from your own data. <a href="oncoprinter.jsp">Try it!</a></p>
        <a href="oncoprinter.jsp"><img class="tile-image top-image" alt="Oncoprint" src="images/oncoprint_example_small.png"></a>

        <hr />
 
        <h3><a href="mutation_mapper.jsp">MutationMapper</a></h3>
        <p>Maps mutations on a linear protein and its domains (lollipop plots). <a href="mutation_mapper.jsp">Try it!</a></p>
        <a href="mutation_mapper.jsp"><img alt="lollipop" style="width:250px" src="images/lollipop_example.png"></a>
        
    </jsp:attribute>


</t:template>