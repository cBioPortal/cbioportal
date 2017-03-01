<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

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
    (<a href="release_notes_oncoprinter.jsp" onclick="return popitup('release_notes_oncoprinter.jsp');">v1.0.1</a>)
    
    <div id="inner-container" style="width:100%;"> <%-- need to be modified by dong li  used to by 70% resize problem--%>
        <div style="margin-top:20px;">
            <p>
                Please input <b>tab-delimited</b> genomic alteration events.&nbsp;&nbsp;
                <u><a id="data-format" href="#">Data format</a></u>&nbsp;&nbsp;
            </p>
            <div id="div-data-format-exp" style="background-color:#eee;display:none;">
                <h4>Data format</h4>
                The data should contain three tab-delimited columns. 
                The first row is a header row, which contains: <code>Sample Gene    Alteration  Type</code>. 
                Each following row contains a single genomic event in a single sample. 
                You can also list samples without any events at the end of the list so that the percentages can be properly calculated.
                Note: Any row which has an entry in the Gene (2nd) column must also have an entry in the Alteration (3rd) and Type (4th) columns
               <ol>
                    <li>Sample: Sample ID</li>
                    <li>Gene: Gene symbol (or other gene identifier)</li>
                    <li>Alteration: Definition of the alteration event
                        <ul>
                            <li>Mutation event: amino acid change or any other information about the mutation</li>
                            <li>Fusion event: fusion information</li>
                            <li>Copy number alteration (CNA) - please use one of the four events below: 
                                <ul>
                                    <li>AMP: high level amplification</li>
                                    <li>GAIN: low level gain</li>
                                    <li>HETLOSS: shallow deletion</li>
                                    <li>HOMDEL: deep deletion</li>
                                </ul>
                            </li>
                            <li>mRNA expression - please use one of the two events below: 
                                <ul>
                                    <li>UP: expression up</li>
                                    <li>DOWN: expression down</li>
                                </ul>
                            </li>
                            <li style="display:none">Protein expression - please use one of the two events below: 
                                <ul>
                                    <li>PROT-UP: RPPA Upregulation</li>
                                    <li>PROT-DOWN: RPPA Downregulation</li>
                                </ul>
                            </li>
                        </ul>
                    </li>
                    <li>Type: Definition of the alteration type. It has to be one of the following.
                        <ul>
                            <li>For a mutation event, please use one of the three mutation types below: 
                                <ul>
                                    <li>MISSENSE: a missense mutation</li>
                                    <li>INFRAME: a inframe mutation</li>
                                    <li>TRUNC: a truncation mutation</li>
                                </ul>
                            </li>
                            <li>FUSION: a fusion event
                            </li>
                            <li>CNA: a copy number alteration event
                            </li>
                            <li>EXP: a expression event
                            </li>
                            <li style="display:none">PROT: a protein expression event
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
                document.getElementById("mutation-file-example").value="<%@ include file="WEB-INF/jsp/oncoprint/example-genomic-events.txt" %>";
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
                document.getElementById("clinic-file-example").value="<%@ include file="WEB-INF/jsp/oncoprint/example-clinic-events.txt" %>";
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
                        <input id="mutation" name="mutation" type="file" name="files[]">
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
            <textarea id="gene_order" rows=2 style="width:40%;"></textarea>
        </div>
        <div style="margin-top:20px;">
            <p>Please define the order of samples (optional).</p>
            <textarea id="sample_order" rows=2 style="width:40%;"></textarea>
        </div>
        <button id="create_oncoprint" type="button" class="btn" style="margin-top:20px; margin-bottom:20px;">Submit</button>
        <div id="error_msg" style="color:#ff0000"></div>

    <div id="oncoprint_controls" style="margin-bottom: 20px;"></div>

    <jsp:include page="WEB-INF/jsp/oncoprint/controls-templates.jsp"></jsp:include>
    <img id="oncoprint_loader_img" src="images/ajax-loader.gif" style="display:none;" alt="loading" />
    </div>
     <div id='oncoprint'>
         <div class="btn-group btn-group-sm" id="oncoprint-diagram-toolbar-buttons" style="float:right;margin-right:15px;display: none;height:33px">           
            <button type="button" class="btn btn-default" id="oncoprint_diagram_showmutationcolor_icon" style="background-color:#efefef;margin:0px"><img checked="0" src="images/colormutations.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-removeUCases-icon" style="background-color:#efefef;margin:0px"><img class="oncoprint-diagram-removeUCases-icon" checked="0" src="images/removeUCases.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-removeWhitespace-icon" style="background-color:#efefef;margin:0px"><img class="oncoprint-diagram-removeWhitespace-icon" checked="0" src="images/removeWhitespace.svg" alt="icon" width="16" height="16" /></button>
            <button type="button" class="btn btn-default" id="oncoprint-diagram-downloads-icon" style="background-color:#efefef;margin:0px"><img class="oncoprint-diagram-downloads-icon" src="images/in.svg" alt="icon" width="16" height="16" /></button>      
            <div class="btn-group btn-group-sm">
                <button type="button" id="oncoprint_zoomout" class="btn btn-default" style="background-color:#efefef;margin:0px"><img src="images/zoom-out.svg" alt="icon" width="16" height="16" /></button>
                <span class="btn btn-default" id="oncoprint_diagram_slider_icon" style="background-color:#efefef;width: 100px;display:inline"></span> 
                <button type="button" id="oncoprint_zoomin" class="btn btn-default" style="background-color:#efefef;margin:0px"><img src="images/zoom-in.svg" alt="icon" width="16" height="16" /></button>
                 <button type="button" id="oncoprint_zoomtofit" class="btn btn-default" style="background-color:#efefef;margin:0px;border-left: 0;"><img src="images/fitalteredcases.svg" alt="icon" width="18" height="18" preserveAspectRatio="none"/></button>
            </div>
        </div>
    <div>
        <br>
        <br>
         <div id='oncoprint_body'></div>
     </div>
    <div id='oncoprint_legend' style="display: inline;"></div>
    <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/webgl/dist/oncoprint-bundle.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/geneticrules.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/lib/canvas-toBlob.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/lib/zlib.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/lib/png.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/lib/jspdf.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/lib/jspdf.plugin.addimage.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/lib/jspdf.plugin.png_support.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/setup.js?<%=GlobalProperties.getAppVersion()%>"></script>
    <script type="text/javascript" charset="utf-8" src="js/src/oncoprint/setup-oncoprinter.js?<%=GlobalProperties.getAppVersion()%>"></script>
    </script>
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
