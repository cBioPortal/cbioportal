
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<style type="text/css">
.small-plot-div {
    width:270px;
    height:200px;
    display:block;
}
#clinical-data-table-div {
    width: 1200px;
    height: 500px;
    overflow-x:scroll;
    overflow-y:scroll;
}
#clinical-msg {
    background-color: lightyellow;
}
</style>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/cancer-study-view/plot-clinical-data.js"></script>
<script type="text/javascript" src="js/cancer-study-view/scatter-plot-mut-cna.js"></script>
<script type="text/javascript">   
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        $('#summary-plot-table').hide();
        $('#clinical-data-table-div').hide();
        if (!mutationProfileId||!hasCnaSegmentData) {
            $('#summary-plot-table').html(""); // remove all if no cna-mut plot
                                               // small plots will be auto generated
        }
        $('#submit-patient-btn').attr("disabled", true);
        initMsgListener();
        loadClinicalData(caseSetId);
        loadMutCountCnaFrac(caseIds,cancerStudyId,mutationProfileId,hasCnaSegmentData,mutCnaLoaded);
        csObs.fireSelection(getRefererCaseId(),null);
    });
    
    function initMsgListener() {
        csObs.subscribe('clinical-msg',function(caseId) {
            if (caseId==null) {
                $('#clinical-msg').hide();
            } else if ((typeof caseId)==(typeof '')) {
                $('#clinical-msg').html("&nbsp;"+formatPatientLink(caseId,cancerStudyId)+
                    " is selected. <button type='button' onclick='csObs.fireSelection(null,null);'>Clear selection</button>");
                $('#clinical-msg').show();
            } else if ((typeof caseId)==(typeof {})) {
                var numSelected = 0;
                for (var id in caseId) {
                    numSelected++;
                }
                if (numSelected==0) {
                    $('#clinical-msg').hide();
                } else if (numSelected==1) {
                    $('#clinical-msg').html("&nbsp;"+formatPatientLink(id,cancerStudyId)+
                        " is selected. <button type='button' onclick='csObs.fireSelection(null,null);'>Clear selection</button>");
                    $('#clinical-msg').show();
                } else {
                    var ids = [];
                    for (var id in caseId) {
                        ids.push(id);
                    }
                    var form = '<form method="post" action="index.do">&nbsp;'
                            + numSelected+' cases are selected.'
                            + '<input type="hidden" name="cancer_study_id" value="'+cancerStudyId
                            + '"><input type="hidden" name="case_set_id" value="-1">'
                            + '<input type="hidden" name="case_ids" value="'+ids.join(" ")
                            + '"><input type="submit" value="Query selection">'
                            + '<input type="submit" onclick="csObs.fireSelection(null,null);return false;" value="Clear selection"></form>';
                    $('#clinical-msg').html(form);
                    $('#clinical-msg').show();
                }
            }
        },false);
    }
    
    var clincialDataTable = null;
    function loadClinicalData(caseSetId) {
        var params = {cmd:'getClinicalData',
                    case_set_id:caseSetId,
                    include_free_form:1};
        $.get("webservice.do",
            params,
            function(data){
                var rows = data.replace(/^#[^\r\n]+[\r\n]+/g,"")
                        .replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1")
                        .match(/[^\r\n]+/g);
                var matrix = [];
                for (var i=0; i<rows.length; i++) {
                    matrix.push(rows[i].split('\t'));
                }

                var wrapper = new DataTableWrapper();
                wrapper.setDataMatrixAndFixTypes(matrix);
                clincialDataTable = wrapper.dataTable;
                waitAndDrawTable();
            })
    }
    
    var mutCnaDataTable = null;
    function mutCnaLoaded(mutCnaDt) {
        if (mutationProfileId&&hasCnaSegmentData) {
            var caseMap = getCaseMap(mutCnaDt);

            $('#clinical-data-loading-wait').hide();
            $('#summary-plot-table').show();
            
            var maxMut = mutCnaDt.getColumnRange(1).max;
            var vLog = maxMut>1000;
            if (vLog) $('#mut-cna-vaxis-log').attr('checked',true);

            plotMutVsCna(csObs,'mut-cna-scatter-plot','case-id-div',cancerStudyId,mutCnaDt,null,2,1,caseMap,false,vLog);

            $('#mut-cna-config').show();

            $(".mut-cna-axis-log").change(function() {
                mutCnaAxisScaleChanged(mutCnaDt,2,1,caseMap);
            });
        }
        
        mutCnaDataTable = mutCnaDt;
        waitAndDrawTable();
    }
    
    function mergeDataTables() {
        if (clincialDataTable==null ||
            ((mutationProfileId!=null || hasCnaSegmentData) && mutCnaDataTable==null)) {
            // wait for the data
            return null;
        }
        
        if (mutCnaDataTable==null)
            return clincialDataTable;
        
        if (clincialDataTable.getNumberOfColumns()==0)
            return mutCnaDataTable;
        
        return google.visualization.data.join(clincialDataTable, mutCnaDataTable,
                    'full', [[0,0]],
                    makeContInxArray(1,clincialDataTable.getNumberOfColumns()-1),
                    makeContInxArray(1,mutCnaDataTable.getNumberOfColumns()-1));
    }
    
    function waitAndDrawTable() {
        var dt = mergeDataTables();
        if (dt) {
            $('#clinical-data-loading-wait').hide();
            $('#summary-plot-table').show();
            resetSmallPlots(dt);
            var caseMap = getCaseMap(dt);
            drawDataTable('clinical_table',dt,caseMap,cancerStudyId);
            $('#clinical_table_filter').css('float', 'left')
            $('#clinical_table_filter').css('text-align','left');
            $('#clinical-data-table-div').show();
        }
    }
    
    function mutCnaAxisScaleChanged(dt,colCna,colMut,caseMap) {
        var hLog = $('#mut-cna-haxis-log').is(":checked");
        var vLog = $('#mut-cna-vaxis-log').is(":checked");
        plotMutVsCna(csObs,'mut-cna-scatter-plot','case-id-div',cancerStudyId,dt,null,colCna,colMut,caseMap,hLog,vLog);
    }
    
    var csObs = new CaseSelectObserver();

</script>

<div id="clinical-msg"></div>

<div id="clinical-data-loading-wait">
    <img src="images/ajax-loader.gif"/>
</div>

<table id="summary-plot-table">
    <tr>
        <td id="small-plot-td-1"></td>
        <td id="small-plot-td-2"></td>
        <td rowspan="2" colspan="2">
            <%@ include file="mut_cna_scatter_plot.jsp" %>
        </td>
    </tr>
    <tr>
        <td id="small-plot-td-3"></td>
        <td id="small-plot-td-4"></td>
    </tr>
    
</table>

&nbsp;<br/>        
      
<div id="clinical-data-table-div">
    <table cellpadding="0" cellspacing="0" border="0" class="display" id="clinical_table">
        <thead>
            <tr valign="bottom">
            </tr>
        </thead>
    </table>
</div>
