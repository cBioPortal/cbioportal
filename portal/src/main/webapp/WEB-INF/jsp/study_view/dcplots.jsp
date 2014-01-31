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
<script src="js/src/plots-view/component/ScatterPlots.js"></script>
<script src="js/src/study-view/main.js"></script> 

<div id="study-view-main">
    <div id="study-view-add-chart">
        <select id='study-view-selectAttr'>
            <option>Please select attribute</option>
        </select>
        <select id='study-view-selectChartType'>
            <option>Please select chart type</option>
        </select>
        <input type='button' value='Add Chart' id='study-view-add-chart-button' class='study-view-header-button' disabled/>
    </div>
    <hr/>
    <div id="study-view-charts">
        <div id='study-view-scatter-plot' class='study-view-dc-chart study-view-pie-chart w4 h2'>
            <div id='study-view-scatter-plot-header'></div>
            <div id='study-view-scatter-plot-body'></div>
            <div id='study-view-scatter-plot-loading-img'></div>     
            <div id='study-view-scatter-plot-control-panel'></div>
        </div>
    </div>
    
    <div id="study-view-update">
        <div id="study-view-updateContent">
            <div style="float: right">
                <input type='button' id='study-view-dataTable-header' class='study-view-header-button' value = 'Update Charts'/>
            </div>
            <img src="images/arrow_top.png" height='25px' alt='Update Charts' style="float: right"/>
            <div style="float: right">
                <input type='button' id='study-view-dataTable-updateTable' class='study-view-header-button' value = 'Update Table' />
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