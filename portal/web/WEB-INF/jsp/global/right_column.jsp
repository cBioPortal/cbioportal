<%@ page import="org.mskcc.portal.util.Config" %><%
    Config globalConfig = Config.getInstance();
    String dataSetsRightColumn = globalConfig.getProperty("data_sets_right_column");
    if (dataSetsRightColumn == null) {
        dataSetsRightColumn = "../../../content/data_sets_right_column.html";
    } else {
        dataSetsRightColumn = "../../../content/" + dataSetsRightColumn;
    }
    String examplesHtml = globalConfig.getProperty("examples_right_column");
    if (examplesHtml == null) {
        examplesHtml = "../../../content/examples.html";
    } else {
        examplesHtml = "../../../content/" + examplesHtml;
    }

%>

<div id="right_side">
<!--
<h3>Getting Started</h3>

	<p>New to the portal?</p>

    <p>Check out our <a href="video.jsp">video tutorial</a>.</p>
-->
<h3>Data Sets</h3>

 <jsp:include page="<%= dataSetsRightColumn%>" flush="true" />

<h3>Example Queries</h3>

 <jsp:include page="<%= examplesHtml %>" flush="true" />    

</div>
