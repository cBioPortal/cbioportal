
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<style type="text/css">
.small-plot-div {
    width:270px;
    height:200px;
    display:block;
}
</style>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/cancer-study-view/plot-clinical-data.js"></script>
<script type="text/javascript" src="js/cancer-study-view/scatter-plot-mut-cna.js"></script>
<script type="text/javascript">   
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        $('#summary-plot-table').hide();
        $('#submit-patient-btn').attr("disabled", true);
        //setupCaseSelect(caseIds);
        loadClinicalData(caseSetId);
        loadMutCountCnaFrac(caseIds,mutationProfileId,cnaProfileId,mutCnaLoaded);
        csObs.fireSelection(getRefererCaseId(),null);
    });
    
    function setupCaseSelect(caseIds) {
        var caseSelect = $('#case-select');
        for (var i=0; i<caseIds.length; i++) {
            caseSelect
                .append($("<option></option>")
                .attr("value",caseIds[i])
                .attr("id",caseIds[i]+"_select")
                .text(caseIds[i]));
        }
        csObs.subscribe('case-select',function(caseId){
            var op = caseId ? $("#"+caseId+"_select") : $("#null_case_select");
            op.attr("selected","selected");
        },true);
        caseSelect.change(function(e) {
            var caseId = $('#case-select  option:selected').attr('value');
            if (caseId=="") caseId=null;
            csObs.fireSelection(caseId,'case-select');
        });
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
                mergeTablesAndVisualize();
            })
    }
    
    var mutCnaDataTable = null;
    function mutCnaLoaded(mutCnaDt) {
        mutCnaDataTable = mutCnaDt;
        mergeTablesAndVisualize();
        clincialDataTable = null;
        mutCnaDataTable = null;
    }

    function mergeTablesAndVisualize() {
        var dt = mergeDataTables();
        if (dt) {
            resetAllPlots(dt);
        }
    }
    
    function mergeDataTables() {
        if (clincialDataTable==null ||
            ((mutationProfileId!=null || cnaProfileId!=null) && mutCnaDataTable==null)) {
            return null;
        }
        
        if (mutCnaDataTable==null)
            return clincialDataTable;
        
        return google.visualization.data.join(clincialDataTable, mutCnaDataTable,
                    'full', [[0,0]],
                    makeContInxArray(1,clincialDataTable.getNumberOfColumns()-1),
                    makeContInxArray(1,mutCnaDataTable.getNumberOfColumns()-1));
    }
 
    // replot all
    function resetAllPlots(dt) {
        var headerMap = getHeaderMap(dt);
        var caseMap = getCaseMap(dt);

        $('#clinical-data-loading-wait').hide();
        $('#summary-plot-table').show();

        drawDataTable('clinical-data-table',dt,caseMap);

        var colCna = headerMap['copy_number_altered_fraction'];
        var colMut = headerMap['mutation_count'];
        plotMutVsCna(csObs,'mut-cna-scatter-plot','case-id-div',dt,colCna,colMut,caseMap,false,false);

        $('#mut-cna-config').show();

        $(".mut-cna-axis-log").change(function() {
            mutCnaAxisScaleChanged(dt,colCna,colMut,caseMap);
        });

        resetSmallPlots(dt);
    }
    
    function mutCnaAxisScaleChanged(dt,colCna,colMut,caseMap) {
        var hLog = $('#mut-cna-haxis-log').is(":checked");
        var vLog = $('#mut-cna-vaxis-log').is(":checked");
        plotMutVsCna(csObs,'mut-cna-scatter-plot','case-id-div',dt,colCna,colMut,caseMap,hLog,vLog);
    }
    
    var csObs = new CaseSelectObserver();

</script>

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