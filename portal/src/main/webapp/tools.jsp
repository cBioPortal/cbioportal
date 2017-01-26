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

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Tools"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<style>
  .tile {
    float: left;
    margin-left: 20px;
    background-color: #ecf0f5;
    border-radius: 6px;
    padding: 14px;
    position: relative;
    text-align: center;
    width: 270px;
    height: 290px;}
  .tile p {
    font-size: 15px;
    margin-top: 10px !important;
    margin-bottom: 33px; }
  .tile-image {
    width: 200px;
    vertical-align: bottom; }
  .btn {
      color: white !important;
  }
</style>

<h1>cBioPortal Tools</h1>

<div id="container">

<p>The following tools are for visualization and analysis of custom datasets. When using these tools in your publication,
    <b>please cite</b> <a href="http://www.ncbi.nlm.nih.gov/pubmed/23550210">Gao et al. <i>Sci. Signal.</i> 2013</a> 
 &amp;  <a href="http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract">Cerami et al. <i>Cancer Discov.</i> 2012</a>.</p>

    <div class="tile">
      <a class="btn btn-large btn-block btn-primary" href="oncoprinter.jsp">OncoPrinter</a>
      <p>Generates oncoprints from your own data</p>
      <img class="tile-image top-image" alt="Oncoprint" src="images/oncoprint_example_small.png">
    </div>

    <div class="tile">
      <a class="btn btn-large btn-block btn-primary" href="mutation_mapper.jsp">MutationMapper</a>
      <p>Maps mutations on a linear protein and its domains (lollipop plots)</p>
      <img class="tile-image top-image" alt="lollipop" src="images/lollipop_example.png">
    </div>
</div>

</td>
    <td width="172">
	<jsp:include page="WEB-INF/jsp/global/right_column.jsp" flush="true" />
    </td>
</tr>
</table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
</html>
