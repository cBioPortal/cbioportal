<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<link rel="stylesheet" type="text/css" href="css/study-view.css">

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script src="js/src/study-view/dc.js"></script>
<script src="js/src/study-view/crossfilter.js"></script>
<script src="js/src/study-view/dataTables.fixedColumns.js"></script>
<script src="js/src/study-view/masonry.pkgd.min.js"></script>
<script src="js/src/plots-view/component/ScatterPlots.js"></script>
<script src="js/src/study-view/view/DcRedrawAllDataTable.js"></script>
<script src="js/src/study-view/StudyViewControl.js"></script>
<script src="js/src/study-view/data/StudyViewProxy.js"></script>
<script src="js/src/study-view/view/StudyViewInitCharts.js"></script>
<script src="js/src/study-view/main.js"></script> 

<div id="dc-plots-loading-wait">
    <img src="images/ajax-loader.gif"/>
</div>

<div id="study-view-main" style="display: none;">
    <div id="study-view-add-chart">
        <!--
        <div id='random' style="float: left; height: 30px;">
            <span id='study-view-add-chart-button'>Add Chart</span>
        <select id='study-view-selectAttr'>
            <option>Please select attribute</option>
        </select>
        </div>
        -->
        <div  id='testDropDown'> 
            <i><strong><span style='color: grey'>Add Chart</span></strong></i>
            <ul>   
            </ul>
        </div>
    </div>
    <hr/>
    <div id="study-view-charts">
        <div id='study-view-scatter-plot' class='study-view-dc-chart w3 h2'>
            <div id='study-view-scatter-plot-header' style="float: right">
                <form style="display:inline-block" action="svgtopdf.do" method="post" id="study-view-scatter-plot-pdf">
                    <input type="hidden" name="svgelement" id="study-view-scatter-plot-pdf-value">
                    <input type="hidden" name="filetype" value="pdf">
                    <input type="hidden" name="filename" value="Scatter_Plot_result-<%=cancerStudy.getCancerStudyStableId()%>.pdf">
                    <input type='submit' value="PDF">                
                </form>
                <form style="display:inline-block" action="svgtopdf.do" method="post" id="study-view-scatter-plot-svg">
                    <input type="hidden" name="svgelement" id="study-view-scatter-plot-svg-value">
                    <input type="hidden" name="filetype" value="svg">
                    <input type="hidden" name="filename" value="Scatter_Plot_result-<%=cancerStudy.getCancerStudyStableId()%>.svg">
                    <input type='submit' value="SVG">                
                </form>
                <input type='checkbox' id='study-view-scatter-plot-log-scale-x'></input><span style="margin: 5px 10px 0px 0px; color: grey">Log Scale X</span>
                <input type='checkbox' id='study-view-scatter-plot-log-scale-y'></input><span style="margin: 5px 50px 0px 0px; color: grey">Log Scale y</span>
                <span class='study-view-scatter-plot-delete'>x</span>
            </div>
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
    <!--<div id="dataTableLoading" style="text-align:center;display:none"><img src="images/ajax-loader.gif"></div>-->
    <div id='data-table-chart'>
        <table id="dataTable">
            <tfoot>
                <tr>
                </tr>
            </tfoot>
        </table>		
    </div>

</div>