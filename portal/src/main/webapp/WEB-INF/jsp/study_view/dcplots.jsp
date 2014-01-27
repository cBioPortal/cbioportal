<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<link rel="stylesheet" type="text/css" href="css/study-view.css">

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="js/src/study-view/d3.js"></script>
<script src="js/src/study-view/dc.js"></script>
<script src="js/src/study-view/crossfilter.js"></script>
<script src="js/src/study-view/jquery.dataTables.js"></script>
<script src="js/src/study-view/dataTables.fixedColumns.js"></script>
<script src="js/src/study-view/masonry.pkgd.min.js"></script>
<script src="js/src/study-view/main.js"></script> 

<!--
<div id="dialog-form">
        <pieH4>Please select attributes</pieH4>
        <ul>
                <li id="mrna-expression-cluster-menu"><a>MRNA EXPRESSION CLUSTER</a></li>
                <li id="tumor-stage-2009-menu"><a>TUMOR STAGE 2009</a></li>			
                <li id="subtype-menu"><a>SUBTYPE DESCRIPTION</a></li>			
                <li id="histology-menu"><a>HISTOLOGY</a></li>			
                <li id="tumor-grade-menu"><a>TUMOR GRADE</a></li>			
                <li id="msi-status-7-marker-call-menu"><a>MSI STATUS 7 MARKER CALL</a></li>			
                <li id="msi-status-5-marker-call-menu"><a>MSI STATUS 5 MARKER CALL</a></li>			
                <li id="methylation-cluster-menu"><a>METHYLATION CLUSTER</a></li>			
                <li id="mlh1-silencing-menu"><a>MLH1 SILENCING</a></li>			
                <li id="cna-cluster-k4-menu"><a>CNA CLUSTER K4</a></li>			
                <li id="mutation-rate-cluster-menu"><a>MUTATION RATE CLUSTER</a></li>			
                <li id="micro-rna-cluster-menu"><a>MICRO RNA CLUSTER</a></li>			
                <li id="os-status-menu"><a>OVERALL SURVIVAL STATUS</a></li>			
                <li id="dfs-status-menu"><a>DISEASE FREE STATUS</a></li>			
                <br />
                <li id="data-menu"><a>DATA</a></li>
                <li id="data-maf-menu"><a class="subData">MAF -</a></li>
                <li id="data-gistic-menu"><a class="subData">GISTIC -</a></li>
                <li id="data-rnaseq-menu"><a class="subData">RNASEQ -</a></li>
                <li id="data-core-sample-menu"><a class="subData">CODE SAMPLE -</a></li>		
                <br />			
                <li id="age-menu"><a>PATIENT AGE DISTRIBUTION</a></li>			
                <li id="micro-rna-score-menu"><a>MICRO RNA SCORE</a></li>			
                <li id="os-months-menu"><a>OVERALL SURVIVAL (MONTH)</a></li>			
                <li id="dfs-months-menu"><a>DISEASE FREE (MONTH)</a></li>			
                <li id="data-table-menu"><a>DATA TABLE</a></li>			
        </ul>
</div>
-->
<div id="main">	
    <!--
    <div id="mainTitle">
        <pieH4>TUMOR SAMPLE ANALYSIS</pieH4>
    </div>
-->
    <div id="add-chart">
        <select id='selectAttr'>
            <option>Please select attribute</option>
        </select>
        <select id='selectChartType'>
            <option>Please select chart type</option>
        </select>
        <input type='button' value='Add Chart' id='add-chart-button' class='header_button' disabled/>
    </div>
    <br/><hr/><br/>
    <div id="pie"></div>

    <div id="row"></div>
    <div id="bar"></div>
    <div id="update">
        <div id="updateContent">
            <div style="float: right">
                <input type='button' id='dataTable_header' class='header_button' value = 'Update Charts'/><!--
                <input type='button' id='dataTable_reset' class='header_button' value = 'Reset'/>
                                                                                                            -->
            </div>
            <img src="images/arrow_top.png" height='25px' alt='Update Charts' style="float: right"/>
            <div style="float: right">
                <input type='button' id='dataTable_updateTable' class='header_button' value = 'Update Table' />
            </div>
            <img src="images/arrow_bottom.png" height='25px' alt='Update  Table' style="float: right"/>
        </div>
    </div>
    <div id="dataTableLoading" style="text-align:center;display:none"><img src="images/ajax-loader.gif"></div>
    <div id='data-table-chart'>
        <table id="dataTable">
            <thead>
                <tr class="header">
                <th>CASE ID</th>
                <th>SUBTYPE</th>
                <th>GENDER</th>
                <th>AGE</th>
                <th>2009 STAGE GROUP</th>					
                <th>HISTOLOGY</th>					
                <th>TUMOR GRADE</th>					
                <th>MSI STATUS 7 MARKER CALL</th>					
                <th>MSI STATUS 5 MARKER CALL</th>					
                <th>DATA MAF</th>					
                <th>DATA GISTIC</th>					
                <th>DATA RNA SEQ</th>					
                <th>DATA CORE SAMPLE</th>					
                <th>MRNA EXPRESSION CLUSTER</th>					
                <th>METHYLATION CLUSTER</th>					
                <th>MLH1 SILENCING</th>					
                <th>CNA CLUSTER K4</th>					
                <th>MUTATION RATE CLUSTER</th>					
                <th>MICRO RNA CLUSTER</th>
                <th>MICRO RNA SCORE</th>
                <th>OVERALL SURVIVAL (STATUS)</th>
                <th>OVERALL SURVIVAL (MONTHS)</th>
                <th>DISEASE FREE (STATUS)</th>
                <th>DISEASE FREE (MONTHS)</th>
                <th>MUTATION COUNT</th>
                <th>COPY NUMBER ALTERATIONS</th>
                </tr>
            </thead>
        </table>		
    </div>

</div>