
<%@ page import="org.mskcc.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>

under construction
<div id="clinicalTable"></div>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">   
    google.load('visualization', '1', {packages:['table']}); 
    $(document).ready(function(){
        loadClinicalData(caseSetId);
        loadMutationCount(mutationProfileId,caseIds);
    });
    
    var clincialDataTableWrapper = null;
    function loadClinicalData(caseSetId) {
        var params = {cmd:'getClinicalData',
                    case_set_id:caseSetId,
                    include_free_form:1};
        $.get("webservice.do",
            params,
            function(data){
                //$('#summary').html("<table><tr><td>"+data.replace(/^#[^\r\n]+[\r\n]+/g,"").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/[\r\n]+/g,"</td></tr><tr><td>").replace(/\t/g,"</td><td>")+"</td></tr></table>");
                var rows = data.replace(/^#[^\r\n]+[\r\n]+/g,"").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1").match(/[^\r\n]+/g);
                var matrix = [];
                for (var i=0; i<rows.length; i++) {
                    matrix.push(rows[i].split('\t'));
                }

                clincialDataTableWrapper = new DataTableWrapper();
                clincialDataTableWrapper.setDataMatrixAndFixTypes(matrix);
                mergeTablesAndVisualize();
            })
    }

    var mutDataTableWrapper = null;
    function loadMutationCount(mutationProfileId,caseIds) {
        if (mutationProfileId==null) return;
        var params = {
            <%=MutationsJSON.CMD%>: '<%=MutationsJSON.COUNT_MUTATIONS_CMD%>',
            <%=QueryBuilder.CASE_IDS%>: caseIds.join(' '),
            <%=PatientView.MUTATION_PROFILE%>: mutationProfileId
        };

        $.post("mutations.json", 
            params,
            function(mutationCounts){
                mutDataTableWrapper = new DataTableWrapper();
                mutDataTableWrapper.setDataMap(mutationCounts,['case_id','mutation_count']);
                mergeTablesAndVisualize();
            }
            ,"json"
        );
    }
    
    function mergeTablesAndVisualize() {
        var dt = mergeDataTables();
        if (dt) {
            var table = new google.visualization.Table(document.getElementById('clinicalTable'));
            table.draw(dt);
        }
    }
    
    
    function mergeDataTables() {
        if (clincialDataTableWrapper==null ||
            (mutationProfileId!=null && mutDataTableWrapper==null)) {
            return null;
        }
        
        var dt = clincialDataTableWrapper.dataTable;
        
        if (mutDataTableWrapper!=null) {
            dt = google.visualization.data.join(dt, mutDataTableWrapper.dataTable,
                    'full', [[0,0]], makeContInxArray(1,dt.getNumberOfColumns()-1),[1]);
        }
        
        clincialDataTableWrapper = null;
        mutDataTableWrapper = null;
        
        return dt;
    }
    
    function makeContInxArray(start,end) {
        var ix = [];
        for (var i=start; i<=end; i++) {
            ix.push(i);
        }
        return ix;
    }
</script>