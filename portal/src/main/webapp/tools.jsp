<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Tools"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<link href="css/bootstrap.min.css" type="text/css" rel="stylesheet" />

<div id="container" style="margin-left:50px;">
    <h1>Tools for custom visualization</h1>
    
    <div style="background-color:#eee;">
        <h3><a href="oncoprinter.jsp">Oncoprinter</a></h3>
        <p>
            Generates oncoprints from user uploaded datasets.
            (<a href="faq.jsp#what-are-oncoprints">What are Oncoprints?</a>)
        </p>
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
