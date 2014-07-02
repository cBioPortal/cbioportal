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

<link href="css/bootstrap.min.css" type="text/css" rel="stylesheet" />

<body>

<div id="container" style="margin-left:50px; margin-top:50px;">
    <h1>Oncoprint</h1>
    <p>(<a href="faq.jsp#what-are-oncoprints">What are Oncoprints?</a>)</p>
        
        <div style="margin-top:20px;">
            <p>
                Mutation files should be tab delimited.  They should also have the
                following fields, in this order, on the first line:
                <code>Sample</code>, <code>Gene</code>,
                <code>Alteration</code>.
                All other fields are ignored. <button id="importData" style="font-size: 13px; width: 100px;" >Input Data</button>
            </p>
            <script type="text/javascript">
            $('#importData').click(function()
            {
                document.getElementById("mutation-file-example").value="Sample	Gene	Alteration\n\
s1	BRCA1	MUT: E123A\n\
s2	BRCA2	MUT: T56fs\n\
s3	BRCA3	MUT: S50*\n\
s4	BRCA4	CNA: AMPLIFIED\n\
s5	PTEN	CNA: HOMODELETED\n\
s2	PTEN	CNA: HOMODELETED\n\
s3	BRCA1	MUT: E123A\n\
s7";
            });
            </script>
            <textarea id="mutation-file-example" rows=5 style="width:40%;"></textarea>

            <form id="mutation-form" class="form-horizontal" enctype="multipart/form-data" method="post">
                <div class="control-group">
                    <label class="control-label" for="mutation">Input Data File</label>
                    <div class="controls">
                        <input id="mutation" name="mutation" type="file">
                    </div>
                </div>
            </form>
        </div>

        <div style="margin-top:20px;">
            <p>Please input the Genes your want to show in the table</p>
            <textarea id="filter_example" rows=2 style="width:40%;"></textarea>
        </div>
        <button id="create_oncoprint" type="button" class="btn" style="margin-top:20px; margin-bottom:20px;">Create</button>
    </div>

    <div id="oncoprint_controls" style="margin-bottom: 20px;"></div>

    <jsp:include page="WEB-INF/jsp/oncoprint/controls-templates.jsp"></jsp:include>

    <div id='oncoprint'></div>
    <script data-main="js/src/oncoprint/custom-boilerplate.js" type="text/javascript" src="js/require.js"></script>

    <div id="download_oncoprint" style="display:none; margin-bottom:40px; margin-top:20px;">
        <span>
        <button class="oncoprint-download" type="pdf" style="display:inline;font-size: 13px; width: 50px;">PDF</button>
        <button class="oncoprint-download" type="svg" style="display:inline;font-size: 13px; width: 50px;">SVG</button>
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
