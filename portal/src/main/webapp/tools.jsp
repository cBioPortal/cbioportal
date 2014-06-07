<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, GlobalProperties.getTitle() + "::Tools"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<style type="text/css">
    progress {
        background-color: #f3f3f3;
        border: 0;
        height: 18px;
        border-radius: 9px;
    }
</style>

<link href="css/bootstrap.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />

<body>

<div id="container" style="margin-left:50px; margin-top:50px;">
    <h1>Oncoprint</h1>
    <p>(<a href="faq.jsp#what-are-oncoprints">What are Oncoprints?</a>)</p>

    <div id="inner-conainter" style="width:70%;">
        <div id="error-box" style="display:none;" class="alert alert-error">There was an error with your file formats.</div>

        <p>
            Copy Number files should be tab delimited. They should also have
            the following fields, in this order, on the first line:
            <code>Hugo_Symbol</code>, <code>Entrez_Gene_Id</code>, followed by
            sample ids.  Subsequent lines have data for hugo gene symbols,
            entrez gene ids, and for each sample.  Data for each sample is
            discrete, ranging from <code>-2</code> to <code>+2</code>.
        </p>

        <textarea id="cna-file-example" rows=5><jsp:include page="WEB-INF/jsp/oncoprint/cna-file-example.txt"></jsp:include></textarea>

        <form id="cna-form" class="form-horizontal" enctype="multipart/form-data" method="post">
            <div class="control-group" style="margin-bottom:0;">
                <label class="control-label" for="cna">Copy Number File</label>
                <div class="controls">
                    <input id="cna" name="cna" type="file">
                </div>
            </div>
        </form>

        <div style="margin-top:20px;">
            <p>
                Mutation files should be tab delimited.  They should also have the
                following fields, in this order, on the first line:
                <code>Hugo_Symbol</code>, <code>Entrez_Gene_Id</code>,
                <code>sample_id</code>, <code>protein_change</code> (ignores case).
                All other fields are ignored.
            </p>

            <textarea id="mutation-file-example" rows=5 style="width:40%;"><jsp:include page="WEB-INF/jsp/oncoprint/mutation-file-example.txt"></jsp:include></textarea>

            <form id="mutation-form" class="form-horizontal" enctype="multipart/form-data" method="post">
                <div class="control-group">
                    <label class="control-label" for="mutation">Mutation File</label>
                    <div class="controls">
                        <input id="mutation" name="mutation" type="file">
                    </div>
                </div>
            </form>
        </div>

        <button id="create_oncoprint" type="button" class="btn" style="margin-top:20px; margin-bottom:20px;">Create</button>
    </div>

    <div id="oncoprint_controls" style="margin-bottom: 20px;"></div>

    <jsp:include page="WEB-INF/jsp/oncoprint/controls-templates.jsp"></jsp:include>

    <div id='oncoprint'></div>
    <script data-main="js/src/oncoprint/custom-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>

    <div id="download_oncoprint" style="display:none; margin-bottom:40px; margin-top:20px;">
        <span>
            <form id="pdf-form" style="display:inline;" action="svgtopdf.do" method="post" target="_blank">

                <input type="hidden" name="svgelement">
                <input type="hidden" name="filetype" value="pdf">
                <input type="submit" value="PDF">
            </form>

            <form id="svg-form" style="display:inline;" action="oncoprint_converter.svg" enctype="multipart/form-data" method="POST" target="_blank">
                <input type="hidden" name="xml">
                <input type="hidden" name="longest_label_length">
                <input type="hidden" name="format" value="svg">
                <input type="submit" value="SVG">
            </form>
        </span>
    </div>

</div>

</div>
</td>
</tr>
</table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
</html>
