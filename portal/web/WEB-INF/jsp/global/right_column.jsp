<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%
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
<% if (SkinUtil.showRightNavDataSets()) {%>
    <h3>Data Sets</h3>
    <jsp:include page="<%= dataSetsRightColumn%>" flush="true" />
<% } %>

<% if (SkinUtil.showRightNavExamples()) {%>
    <h3>Example Queries</h3>
    <jsp:include page="<%= examplesHtml %>" flush="true" />

    <h3>What's New</h3>

    <P>Check out our new network visualization
    options.  Available under the <b>network</b> tab on all result pages.
<% } %>

    <h3>Testimonials</h3>
    <jsp:include page="../testimonials.jsp" flush="true" />
</div>
