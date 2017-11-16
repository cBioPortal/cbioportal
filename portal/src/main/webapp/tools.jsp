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

<t:template title="<%= siteTitle %>" twoColumn="false" fixedWidth="true">

    <jsp:attribute name="head_area">
        <script>
        window.loadReactApp({ defaultRoute: 'blank' });
        </script>
        <style>
            .toolArray > div {
                width:275px;
                padding-right:40px;
            }
            .toolArray h2 {
                margin-top:0;
            }
        
        </style>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <h1>Visualize Your Data</h1>

        <h2 id="download-and-install-a-local-version-of-cbioportal">1. Download and install a local version of cBioPortal</h2>
        <ul>
        <li>The source code of cBioPortal is available on <a href="https://github.com/cBioPortal/cbioportal">GitHub</a> under the terms of Affero GPL V3. </li>
        <li>Please note that, installing a local version requires system administration skills, for example, installing and configuring Tomcat and MySQL. With limited resources, we cannot provide technical support on system administration.</li>
        </ul>
        
        <h2>2. We host data for you (academic use)</h2>
        <ul>
            <li>Public data will be available to everyone. Suggestions on data sets are welcome.</li>
            <li>Private data will be accessible by you and your collaborators.</li>
            <li>Please <a href="mailto:cbioportal@cbio.mskcc.org?subject=Uploading public data">contact us</a> for details.</li>
        </ul>

        <h2>3. Commercial support</h2>
        <ul>
            <li><a href="http://thehyve.nl" target="_blank">The Hyve</a> is an open source software company that provides commercial support for cBioPortal. They can help with deployment, data loading, development, consulting and training. Please <a href=http://thehyve.nl/contact/ target="_blank">contact The Hyve</a> for details.</li>
        </ul>

        <hr>
        
        <h2>The following tools are for visualization and analysis of custom datasets</h2>

        <div class="alert alert-info" role="alert">
        When using these tools in your publication,
        <b>please cite</b> <a href="http://www.ncbi.nlm.nih.gov/pubmed/23550210">Gao et al. <i>Sci. Signal.</i> 2013</a>
        &amp;  <a href="http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract">Cerami et al. <i>Cancer Discov.</i> 2012</a>.
        </div>
        
        <div style="display:flex;" class="toolArray">
            <div style="margin-right:60px">
                <h2><a href="oncoprinter.jsp">OncoPrinter</a></h2>
                <p>Generates oncoprints from your own data. <a href="oncoprinter.jsp">Try it!</a></p>
                <a href="oncoprinter.jsp"><img class="tile-image top-image" alt="Oncoprint" src="images/oncoprint_example_small.png"></a>
            </div>
          
            <div>
                <h2><a href="mutation_mapper.jsp">MutationMapper</a></h2>
                <p>Maps mutations on a linear protein and its domains (lollipop plots). <a href="mutation_mapper.jsp">Try it!</a></p>
                <a href="mutation_mapper.jsp"><img alt="lollipop" style="height:147px" src="images/lollipop_example.png"></a>
            </div>
        </div>

        <div id="reactRoot" class="hidden"></div>
    </jsp:attribute>
    

</t:template>