
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
        <div id='DFS_STATUS' class='pie-chart'>
            <pieH4>MRNA EXPRESSION  <a class="reset" href="javascript:varChart[0].filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>
  
        <div id='tumor-stage-2009-chart' class='pie-chart' style="display:block">
            <pieH4>TUMOR STAGE 2009  <a class="reset" href="javascript:tumorStage2009Chart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='subtype-chart' class='pie-chart'  style="display:block">
            <pieH4>SUBTYPE DESCRIPTION <a class="reset" href="javascript:subtypeChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='histology-chart' class='pie-chart'>
            <pieH4>HISTOLOGY <a class="reset" href="javascript:histologyChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='tumor-grade-chart' class='pie-chart'>
            <pieH4>TUMOR GRADE  <a class="reset" href="javascript:tumorGradeChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='msi-status-7-marker-call-chart' class='pie-chart'>
            <pieH4>MSI STATUS 7 MARKER CALL  <a class="reset" href="javascript:msiStatus7MarkerCallChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='msi-status-5-marker-call-chart' class='pie-chart'>
            <pieH4>MSI STATUS 5 MARKER CALL  <a class="reset" href="javascript:msiStatus5MarkerCallChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>
        <div id='methylation-cluster-chart' class='pie-chart'>
            <pieH4>METHYLATION CLUSTER  <a class="reset" href="javascript:methylationClusterChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='mlh1-silencing-chart' class='pie-chart'>
            <pieH4>MLH1 SILENCING  <a class="reset" href="javascript:mlh1SilencingChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='cna-cluster-k4-chart' class='pie-chart'>
            <pieH4>CNA CLUSTER K4  <a class="reset" href="javascript:cnaClusterK4Chart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='mutation-rate-cluster-chart' class='pie-chart'>
            <pieH4>MUTATION RATE CLUSTER  <a class="reset" href="javascript:mutationRateClusterChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='micro-rna-cluster-chart' class='pie-chart'>
            <pieH4>MICRO RNA CLUSTER  <a class="reset" href="javascript:microRnaClusterChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='os-status-chart' class='pie-chart'>
            <pieH4>OVERALL SURVIVAL STATUS  <a class="reset" href="javascript:osStatusChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>

        <div id='dfs-status-chart' class='pie-chart'>
            <pieH4>DISEASE FREE STATUS  <a class="reset" href="javascript:dfsStatusChart.filterAll();dc.redrawAll();" style="display: none;">reset</a></pieH4>
        </div>			

        <div id='data-chart'>
            <pieH4>DATA</pieH4>
            <br />
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
        </div>
        -->
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
                <th class="th_6">CASE ID</th>
                <th class="th_10">SUBTYPE</th>
                <th class="th_2">AGE</th>
                <th class="th_4">2009 STAGE GROUP</th>					
                <th class="th_4">HISTOLOGY</th>					
                <th class="th_4">TUMOR GRADE</th>					
                <th class="th_4">MSI STATUS 7 MARKER CALL</th>					
                <th class="th_4">MSI STATUS 5 MARKER CALL</th>					
                <th class="th_2">DATA MAF</th>					
                <th class="th_2">DATA GISTIC</th>					
                <th class="th_2">DATA RNA SEQ</th>					
                <th class="th_2">DATA CORE SAMPLE</th>					
                <th class="th_2">MRNA EXPRESSION CLUSTER</th>					
                <th class="th_2">METHYLATION CLUSTER</th>					
                <th class="th_2">MLH1 SILENCING</th>					
                <th class="th_2">CNA CLUSTER K4</th>					
                <th class="th_4">MUTATION RATE CLUSTER</th>					
                <th class="th_2">MICRO RNA CLUSTER</th>
                <th class="th_6">MICRO RNA SCORE</th>
                <th class="th_6">CASE ID</th>
                <th class="th_4">OVERALL SURVIVAL STATUS</th>
                <th class="th_6">OVERALL SURVIVAL (MONTHS)</th>
                <th class="th_4">DISEASE FREE STATUS)</th>
                <th class="th_6">DISEASE FREE (MONTHS)</th>
                </tr>
            </thead>
        </table>		
    </div>

</div>