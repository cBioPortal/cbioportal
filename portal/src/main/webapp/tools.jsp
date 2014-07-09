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
       <div id="inner-conainter" style="width:70%;"> 
        <div style="margin-top:20px;">
            <p>
                Please input <b>tab-delimited</b> genomic alteration events.&nbsp;&nbsp;
                <u><a id="data-format" href="#">Data format</a></u>&nbsp;&nbsp;
                <u><a id="load-example-data" href="#">Load example data</a></u>
            </p>
            
            <div id="div-data-format-exp" style="background-color:#eee;display:none;">
                <h4>Data format</h4>
                The data should contains three columns separated by tab.
                The first row is always <code>Sample Gene   Alteration</code>.
                Every other row can contain one genomic event (mutation or copy number alteration) in one sample.
                You can also input samples without any events at the end so that the percentages can be properly calculated. 
               <ol>
                    <li>Sample column: sample ID or barcode</li>
                    <li>Gene column: gene symbol or any gene ID</li>
                    <li>Alteration column
                        <ul>
                            <li>Mutation event: amino acid change or any other information about the mutation</li>
                            <li>Copy number alteration (CNA): supported CNA events include
                                <ul>
                                    <li>AMP: high level amplification</li>
                                    <li>GAIN: low level gain</li>
                                    <li>HETLOSS: heterozygous deletion</li>
                                    <li>HOMDEL: homozygous deletion</li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                </ol>
            </div>
            
            <script type="text/javascript">
            $('#data-format').click(function()
            {
                $("#div-data-format-exp").slideToggle();
            });
            $('#load-example-data').click(function()
            {
                document.getElementById("mutation-file-example").value="<jsp:include page="WEB-INF/jsp/oncoprint/example-genomic-events.txt"/>";
            });
            </script>
            <textarea id="mutation-file-example" rows=10 style="width:40%;"></textarea>

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
            <p>Please define the order of genes (optional).</p>
            <textarea id="filter_example" rows=2 style="width:40%;"></textarea>
        </div>
        <button id="create_oncoprint" type="button" class="btn" style="margin-top:20px; margin-bottom:20px;">Create</button>

    <div id="oncoprint_controls" style="margin-bottom: 20px;"></div>

    <jsp:include page="WEB-INF/jsp/oncoprint/controls-templates.jsp"></jsp:include>

    <div id='oncoprint'></div>
    <script data-main="js/src/oncoprint/custom-boilerplate.js" type="text/javascript" src="js/require.js"></script>

    <div id="download_oncoprint" style="display:none; margin-bottom:40px; margin-top:20px;">
        <span>
        <button class="oncoprint-download" type="pdf" style="display:inline;font-size: 13px; width: 50px;">PDF</button>
        <button class="oncoprint-download" type="svg" style="display:inline;font-size: 13px; width: 50px;">SVG</button>
        <button class="sample-download" type="txt" style="display:inline;font-size: 13px; width: 75px;">SAMPLES</button>
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
