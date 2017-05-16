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
        window.loadReactApp({ defaultRoute: 'blank' });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="reactRoot" class="hidden"></div>
        
        <h1>Visualization Tools</h1>
        
        <p>The following tools are for visualization and analysis of custom datasets. </p>
        <p>When using these tools in your publication,
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
    
    <jsp:attribute name="right_column">
        <div class="rightBarSection">
        <h3>Visualize Your Own Data</h3>
        <p>If you want to visualize your own data in cBioPortal, you have the following options:</p>
        <p>1. We host data for you (academic use)</p>
        <ul>
        <li><b>MSKCC users can send us their data by filling out this <a href="https://docs.google.com/forms/d/1UPQF-T9HQStHK87UEYzgf-hTiKT-ok6fZDxxw_lANr8/viewform">form</a>.</b></li>
        <li>Public data will be available to everyone. Suggestions on data sets are welcome. Please <a href="mailto:cbioportal@cbio.mskcc.org?subject=Uploading public data">contact us</a> for details.</li>
        <li>Private data will be accessible by you and your collaborators. Please <a href="mailto:cbioportal@cbio.mskcc.org?subject=Uploading private data">contact us</a> for details.</li>
        </ul>
        <p id="use-our-tools-to-visualize-your-data">2. Use our tools to visualize your data</p>
        <ul>
        <li><a href="oncoprinter.jsp">Oncoprinter</a> lets you create Oncoprints from your own, custom data.</li>
        <li><a href="mutation_mapper.jsp">MutationMapper</a> draws mutation diagrams (lollipop plots) from your custom data.</li>
        </ul>
        <p id="download-and-install-a-local-version-of-cbioportal">3. Download and install a local version of cBioPortal</p>
        <ul>
        <li>The source code of cBioPortal is available on <a href="https://github.com/cBioPortal/cbioportal">GitHub</a> under the terms of Affero GPL V3. </li>
        <li>Please note that, installing a local version requires system administration skills, for example, installing and configuring Tomcat and MySQL. With limit resources, we cannot provide technical support on system administration.</li>
        </ul>

        <hr>

        <p>Please email any questions to <a href="mailto:cbioportal@cbio.mskcc.org?subject=Questions about downloading software or hosting data">cbioportal@cbio.mskcc.org</a>.</p>
        </div>
    </jsp:attribute>


</t:template>