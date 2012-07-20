
<%@ page import="org.mskcc.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>

under construction
<div id="clinicalTable"></div>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">   
    google.load('visualization', '1', {packages:['table']}); 
    $(document).ready(function(){
        loadClinicalData(caseSetId,mutationProfileId,cnaProfileId);
    });
    
    function loadClinicalData(caseSetId, mutationProfileId, cnaProfileId) {
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

                dataTableWrapper.setDataMatrix(matrix);
                var caseIds = dataTableWrapper.getColumnData(0);
                if (mutationProfileId!=null) {
                    dataTableWrapper.dataTable.addColumn('number','mutation_count');
                    var mutCol = dataTableWrapper.dataTable.getNumberOfColumns()-1;
                    loadMutationCount(mutationProfileId,caseIds,mutCol);
                }
            })
    }

    function loadMutationCount(mutationProfileId,caseIds,ixCol) {
        var params = {
            <%=MutationsJSON.CMD%>: '<%=MutationsJSON.COUNT_MUTATIONS_CMD%>',
            <%=QueryBuilder.CASE_IDS%>: caseIds.join(' '),
            <%=PatientView.MUTATION_PROFILE%>: mutationProfileId
        };

        $.post("mutations.json", 
            params,
            function(mutationCounts){
                for (var i=0; i<caseIds.length; i++) {
                    dataTableWrapper.dataTable.setCell(i,ixCol,mutationCounts[caseIds[i]]);
                }
                
                var table = new google.visualization.Table(document.getElementById('clinicalTable'));
                table.draw(dataTableWrapper.dataTable, {showRowNumber: true});
            }
            ,"json"
        );
    }

</script>