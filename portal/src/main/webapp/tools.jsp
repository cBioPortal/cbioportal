<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Tools"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<link href="css/bootstrap.min.css" type="text/css" rel="stylesheet" />

<style>
  .tile {
    background-color: #ecf0f5;
    border-radius: 6px;
    padding: 14px;
    position: relative;
    text-align: center;
    width: 250px;}
  .tile p {
    font-size: 15px;
    margin-bottom: 33px; }
  .tile-image {
    width: 200px;
    vertical-align: bottom; }
  .btn {
      color: white !important;
  }
</style>

<h1>Tools for custom visualization</h1>
    
<div id="container" style="margin-left:50px;">
    <div class="tile">
      <a class="btn btn-large btn-block btn-primary" href="oncoprinter.jsp">Oncoprinter</a>
      <p>Generates oncoprints from user uploaded datasets</p>
      <img class="tile-image top-image" alt="Oncoprint" src="images/oncoprint_example_small.png">
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
