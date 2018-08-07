<%
    String siteTitle = GlobalProperties.getTitle();
%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@taglib prefix="t" tagdir="/WEB-INF/tags" %>

<t:template title="<%= siteTitle %>" defaultRightColumn="false" fixedWidth="false">

    <jsp:attribute name="head_area">
        <script>
            window.loadReactApp({ defaultRoute: 'mutation_mapper' });
        </script>
    </jsp:attribute>

    <jsp:attribute name="body_area">
        <div id="reactRoot"></div>
    </jsp:attribute>

</t:template>