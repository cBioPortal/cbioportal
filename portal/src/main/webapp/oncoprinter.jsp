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
    .tooltip-sample{
    display:none;
    background-color:white;
    position:absolute;
    border:1px solid red;
    border-radius: 5px;
    padding:5px;
    top:-30px;
    left:20px;
    width: 200px;
    }
    
    .sample-download:hover .tooltip-sample{
    display:block;
    }
</style>
<script src="js/lib/bootstrap.min.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript"></script>
        
<link href="css/bootstrap.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />

<div id="container" style="margin-left:20px;">
    <h1 style="display:inline;">OncoPrinter</h1>
    <script type="text/javascript">
        function popitup(url) {
            var newwindow=window.open(url,'OncoPrinterReleaseNotes','height=600,width=800,left=400,top=0,scrollbars=yes');
            if (window.focus) {newwindow.focus();}
            return false;
        }
    </script>
    generates <a href="faq.jsp#what-are-oncoprints">Oncoprints</a> from you own data
    (<a href="release_notes_oncoprinter.jsp" onclick="return popitup('release_notes_oncoprinter.jsp');">v1.0</a>)
    
    <div id="inner-conainter" style="width:100%;"> <%-- need to be modified by dong li  used to by 70% resize problem--%>
        <div style="margin-top:20px;">
            <p>
                Please input <b>tab-delimited</b> genomic alteration events.&nbsp;&nbsp;
                <u><a id="data-format" href="#">Data format</a></u>&nbsp;&nbsp;
            </p>
            <div id="div-data-format-exp" style="background-color:#eee;display:none;">
                <h4>Data format</h4>
                The data should contain three tab-delimited columns. 
                The first row is a header row, which contains: <code>Sample Gene   Alteration</code>. 
                Each following row contains a single genomic event in a single sample. 
                You can also list samples without any events at the end of the list so that the percentages can be properly calculated.
               <ol>
                    <li>Sample: Sample ID</li>
                    <li>Gene: Gene symbol (or other gene identifier)</li>
                    <li>Alteration: Definition of the alteration event
                        <ul>
                            <li>Supported alteration types: Mutation event: amino acid change or any other information about the mutation</li>
                            <li>Copy number alteration (CNA) - please use one of the four events below: 
                                <ul>
                                    <li>AMP: high level amplification</li>
                                    <li>GAIN: low level gain</li>
                                    <li>HETLOSS: heterozygous deletion</li>
                                    <li>HOMDEL: homozygous deletion</li>
                                </ul>
                            </li>
                            <li>mRNA expression - please use one of the two events below: 
                                <ul>
                                    <li>UP: expression up</li>
                                    <li>DOWN: expression down</li>
                                </ul>
                            </li>
                            <li style="display:none">RPPA - please use one of the two events below: 
                                <ul>
                                    <li>PROT-UP: RPPA Upregulation</li>
                                    <li>PROT-DOWN: RPPA Downregulation</li>
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
            </script>
            <table style="width:100%;">
            <tr>
            <td style="width:45%;">
            <div style="font-size:10px;">
            <p >
                <b>Input Mutation Data</b>
                <u><a id="load-example-data" href="#">Load example data</a></u>
            </p>
            
            <script type="text/javascript">
            $('#load-example-data').click(function()
            {
                document.getElementById("mutation-file-example").value="<jsp:include page="WEB-INF/jsp/oncoprint/example-genomic-events.txt"/>";
            });
            </script>
            </div>
                
            <textarea id="mutation-file-example" rows=10 style="width:95%;"></textarea>
            </td>
            <td style="width:45%;display:none;">
            <div>
            <p>
                <b>Input Clinical Data</b>
                <u><a id="clinic-load-example-data" href="#">Load example data</a></u>
            </p>
            <script type="text/javascript">
            $('#clinic-load-example-data').click(function()
            {
                document.getElementById("clinic-file-example").value="<jsp:include page="WEB-INF/jsp/oncoprint/example-clinic-events.txt"/>";
            });
            </script>
            </div>
            <textarea id="clinic-file-example" rows=10 style="width:95%;"></textarea>
            </td>
            </tr>
            <script>
                //enable user to input tab in the textarea
                function enableTab(id) {
                    var el = document.getElementById(id);
                    el.onkeydown = function(e) {
                        if (e.keyCode === 9) { // tab was pressed

                            // get caret position/selection
                            var val = this.value,
                                start = this.selectionStart,
                                end = this.selectionEnd;

                            // set textarea value to: text before caret + tab + text after caret
                            this.value = val.substring(0, start) + '\t' + val.substring(end);

                            // put caret at right position again
                            this.selectionStart = this.selectionEnd = start + 1;

                            // prevent the focus lose
                            return false;

                        }
                    };
                }

                // Enable the tab character onkeypress (onkeydown) inside textarea...
                // ... for a textarea that has an `id="mutation-file-example"`
                enableTab('mutation-file-example');
            </script>
            
            <tr>
            <td style="width:41%;">
            <form id="mutation-form" class="form-horizontal" enctype="multipart/form-data" method="post">
                <div class="control-group">
                    <label class="control-label" for="mutation">Input Mutation Data File</label>
                    <div class="controls">
                        <input id="mutation" name="mutation" type="file">
                    </div>
                </div>
            </form>
            </td>
            <td style="display:none;">
            <form id="clinic-form" class="clinic-form-horizontal" enctype="multipart/form-data" method="post">
                <div class="clinic-control-group">
                    <label class="clinic-control-label" for="mutation">Input Clinical Data File</label>
                    <div class="clinic-controls">
                        <input id="clinic" name="clinic" type="file">
                    </div>
                </div>
            </form>
            </td>
            </tr>
            </table>
        </div>

        <div style="margin-top:20px;">
            <p>Please define the order of genes (optional).</p>
            <textarea id="filter_example" rows=2 style="width:40%;"></textarea>
        </div>
        <button id="create_oncoprint" type="button" class="btn" style="margin-top:20px; margin-bottom:20px;">Submit</button>

    <div id="oncoprint_controls" style="margin-bottom: 20px;"></div>

    <jsp:include page="WEB-INF/jsp/oncoprint/controls-templates.jsp"></jsp:include>
    <div>
    <img id="oncoprint_loader_img" src="images/ajax-loader.gif" style="display:none;">
    </div>
    <div id='oncoprint'></div>
    <div id='oncoprint_legend' style="display: inline;"></div>
    <script data-main="js/src/oncoprint/custom-boilerplate.js?<%=GlobalProperties.getAppVersion()%>" type="text/javascript" src="js/require.js?<%=GlobalProperties.getAppVersion()%>"></script>
</div>
        <script type="text/javascript"> 
               $('.sample-download').qtip({
                content: {text: 'Download the list of samples, sorted in the order in which they are displayed in the OncoPrint (left to right)'},
                position: {my:'left bottom', at:'top right', viewport: $(window)},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                show: {event: "mouseover"},
                hide: {fixed: true, delay: 100, event: "mouseout"}
            });
        </script>
</div>
</td>
</tr>
</table>
<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
</div>
</body>
</html>
