
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<link rel="stylesheet" type="text/css" href="css/study-view.css">

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="js/src/study-view/d3.js"></script>
<script src="js/src/study-view/dc.js"></script>
<script src="js/src/study-view/crossfilter.js"></script>
<script src="js/src/study-view/jquery.dataTables.js"></script>
<!--<script src="js/src/study-view/loadData.js"></script>-->
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
    <div id="pie">
         
            <!--
            <div id='data-maf-chart' class='data-pie-chart'>
                <pieH3>MAF <a class="reset" href="javascript:dataMafChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH3>
            </div>
            <div id='data-gistic-chart' class='data-pie-chart'>
                <pieH3>GISTIC <a class="reset" href="javascript:dataGisticChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH3>
            </div>
            <div id='data-rnaseq-chart' class='data-pie-chart'>
                <pieH3>RNASEQ <a class="reset" href="javascript:dataRnaseqChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH3>
            </div>
            <div id='data-core-sample-chart' class='data-pie-chart'>
                <pieH3>CORE SAMPLE <a class="reset" href="javascript:dataCoreSampleChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH3>
            </div>
            -->
        
    </div>

    <div id="row">
    </div>
    <div id="bar">
        <!--
        <div id='age-chart' class="bar-chart">
            <pieH4>NUMBER OF PATIENTS BASED ON AGE DISTRIBUTION  <a class="reset" href="javascript:ageChart.filterAll(); dc.redrawAll(); " style="display: none;">reset</a></pieH4>
        </div>

        <div id='micro-rna-score-chart' class="bar-chart">
            <pieH4>MICRO RNA SCORE  <a class="reset" href="javascript:microRnaScoreChart.filterAll(); dc.redrawAll(); " style="display: none;">reset</a></pieH4>
        </div>

        <div id='os-months-chart' class="bar-chart">
            <pieH4>OVERALL SURVIVAL IN MONTHS SINCE DIAGNOSIS  <a class="reset" href="javascript:osMonthsChart.filterAll(); dc.redrawAll(); " style="display: none;">reset</a></pieH4>
        </div>

        <div id='dfs-months-chart' class="bar-chart">
            <pieH4>DISEASE FREE IN MONTHS SINCE TREATMENT  <a class="reset" href="javascript:dfsMonthsChart.filterAll(); dc.redrawAll(); " style="display: none;">reset</a></pieH4>
        </div>	
        -->
    </div>

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
                <th>OVERALL SURVIVAL STATUS</th>
                <th>OVERALL SURVIVAL (MONTHS)</th>
                <th>DISEASE FREE STATUS)</th>
                <th>DISEASE FREE (MONTHS)</th>
                </tr>
            </thead>
        </table>		
    </div>

</div>