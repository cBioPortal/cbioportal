<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Tools"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<link href="css/bootstrap.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />

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
    <b>please cite</b> <a href="http://www.cbioportal.org/public-portal/sci_signal_reprint.jsp">Gao et al. <i>Sci. Signal.</i> 2013</a> 
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
