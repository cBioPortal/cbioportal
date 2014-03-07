<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<link rel="stylesheet" type="text/css" href="css/study-view.css">

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="js/src/study-view/dc.js"></script>
<script src="js/src/study-view/crossfilter.js"></script>
<script src="js/src/study-view/dataTables.fixedColumns.js"></script>
<script src="js/src/study-view/masonry.pkgd.min.js"></script>
<script src="js/src/study-view/ScatterPlots.js"></script>
<script src="js/src/study-view/view/DcRedrawAllDataTable.js"></script>
<script src="js/src/study-view/StudyViewBoilerplate.js"></script>
<script src="js/src/study-view/StudyViewControl.js"></script>
<script src="js/src/study-view/data/StudyViewProxy.js"></script>
<script src="js/src/study-view/view/StudyViewInitCharts.js"></script>
<script src="js/src/study-view/main.js"></script>

<div id="dc-plots-loading-wait">
    <img src="images/ajax-loader.gif"/>
</div>

<div id="study-view-main" style="display: none;">
    <div id="study-view-header-function">
        <div  id='study-view-header-left'> 
            <!--<span id='study-view-header-left-4' class="study-view-header study-view-header-left boxLeft">Remove All NA value</span>-->
            <span id='study-view-header-left-0' class="study-view-header study-view-header-left boxLeft">Select cases by IDs</span>
            <form method="post" action="index.do" style='float: left'>
                <input type="hidden" name="cancer_study_id" value="<%=cancerStudy.getCancerStudyStableId()%>">
                <input type="hidden" name="case_set_id" value="-1">
                <input type="hidden" id="study-view-header-left-case-ids" name="case_ids" value="">
                <input type="submit" id='study-view-header-left-1' class="study-view-header study-view-header-left boxLeft hidden" value="Query selected cases"/>
            </form>
            <span id='study-view-header-left-2' class="study-view-header study-view-header-left boxLeft hidden">Clear selected cases</span>
            <span id='study-view-header-left-3' class="hidden" style='margin-top: 4px; margin-left: 4px; background-color: lightyellow'></span>
        </div>
        <div class="hidden" id="study-view-case-select-custom-dialog">
            Please input case IDs (one per line)
            <textarea rows="20" cols="50" id="study-view-case-select-custom-input"></textarea><br/>
            <button type='button' id="study-view-case-select-custom-submit-btn">Select</button>
        </div>
        <div  id='study-view-add-chart' class="study-view-header boxRight"> 
            <span style='color: grey'>Add Chart</span>
            <ul>   
            </ul>
        </div>
    </div>
    <hr/>
    
    <div id="study-view-charts"></div>
    
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
    <!--<div id="dataTableLoading" style="text-align:center;display:none"><img src="images/ajax-loader.gif"></div>-->
    <div id='data-table-chart'></div>

</div>