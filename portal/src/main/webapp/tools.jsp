<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<% request.setAttribute(QueryBuilder.HTML_TITLE, SkinUtil.getTitle() + "::Tools"); %>

<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true"/>

<style type="text/css">
    progress {
        background-color: #f3f3f3;
        border: 0;
        height: 18px;
        border-radius: 9px;
    }
</style>

<link href="css/bootstrap.min.css" type="text/css" rel="stylesheet" />

<body>

<div id="container" style="margin-left:50px; margin-top:50px;">
    <h1>Oncoprint</h1>

    <form class="form-horizontal" enctype="multipart/form-data" method="post">
       <p>Choose data files to upload</p>

        <div class="control-group" style="margin-bottom:0;">
            <label class="control-label" for="cna">Copy Number File</label>
            <div class="controls">
                <input id="cna" name="cna" type="file">
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="mutation">Mutation File</label>
            <div class="controls">
                <input id="mutation" name="mutation" type="file">
            </div>
        </div>

        <div class="control-group" style="margin-top: 20px;">
            <div class="controls">
                <input id="submit" type="button" value="Create" class="btn">
                <br/>
                <br/>
                <%--<progress></progress>--%>
            </div>
        </div>
    </form>

    <p>Example</p>

    <div>
        <div>
            <p style="width:70%;">
                Copy Number files should be tab delimited. They should also have
                the following fields, in this order, on the first line:
                <code>Hugo_Symbol</code>, <code>Entrez_Gene_Id</code>, followed by
                sample ids.  Subsequant lines have data for hugo gene symbols,
                entrez gene ids, and for each sample.  Data for each sample is
                discrete, ranging from <code>-2</code> to <code>+2</code>.
            </p>

            <textarea id="cna-file-example" rows=5 style="width:70%;"><jsp:include page="WEB-INF/jsp/oncoprint/cna-file-example.txt"></jsp:include></textarea>

        </div>

        <div style="margin-top:20px;">
            <p style="width:70%;">
                Mutation files should be tab delimited.  They should also have the
                following fields, in this order, on the first line:
                <code>Hugo_Symbol</code>, <code>Entrez_Gene_Id</code>,
                <code>sample_id</code>, <code>protein_change</code> (ignores case).
                All other fields are ignored.
            </p>

            <textarea id="mutation-file-example" rows=5 style="width:40%;"><jsp:include page="WEB-INF/jsp/oncoprint/mutation-file-example.txt"></jsp:include></textarea>
        </div>

        <button id="create_sample" type="button" class="btn" style="margin-top:20px; margin-bottom:20px;">Create</button>
    </div>

    <div id="oncoprint_controls"></div>

    <jsp:include page="WEB-INF/jsp/oncoprint/controls-templates.jsp"></jsp:include>

    <div id='oncoprint' style="margin-bottom:40px;"></div>
    <script data-main="js/src/oncoprint/custom-boilerplate.js" type="text/javascript" src="js/require.js"></script>

</div>

</div>
</td>
</tr>
</table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
</html>
