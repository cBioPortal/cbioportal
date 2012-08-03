<style type="text/css">
.large-plot-div {
    width:540px;
    height:400px;
    display:block;
}
</style>
<script type="text/javascript">
    function loadMutCountCnaFrac(caseIds,mutationProfileId,cnaProfileId,func) {
    
        var mutDataTable = null;
        if (mutationProfileId!=null) {
            var params = {
                <%=MutationsJSON.CMD%>: '<%=MutationsJSON.COUNT_MUTATIONS_CMD%>',
                <%=QueryBuilder.CASE_IDS%>: caseIds.join(' '),
                <%=PatientView.MUTATION_PROFILE%>: mutationProfileId
            };

            $.post("mutations.json", 
                params,
                function(mutationCounts){
                    var wrapper = new DataTableWrapper();
                    wrapper.setDataMap(mutationCounts,['case_id','mutation_count']);
                    mutDataTable = wrapper.dataTable;
                    mergeTablesAndCallFunc(mutationProfileId,cnaProfileId,
                                mutDataTable,cnaDataTable,func);
                }
                ,"json"
            );
        }


        var cnaDataTable = null;

        if (cnaProfileId!=null) {
            var params = {
                <%=CnaJSON.CMD%>: '<%=CnaJSON.GET_CNA_FRACTION_CMD%>',
                <%=QueryBuilder.CASE_IDS%>: caseIds.join(' ')
            };

            $.post("cna.json", 
                params,
                function(cnaFracs){
                    var wrapper = new DataTableWrapper();
                    // TODO: what if no segment available
                    wrapper.setDataMap(cnaFracs,['case_id','copy_number_altered_fraction']);
                    cnaDataTable = wrapper.dataTable;
                    mergeTablesAndCallFunc(mutationProfileId,cnaProfileId,
                                mutDataTable,cnaDataTable,func);
                }
                ,"json"
            );
        }
    }
    
    function mergeTablesAndCallFunc(mutationProfileId,cnaProfileId,
            mutDataTable,cnaDataTable,func) {
        if ((mutationProfileId!=null && mutDataTable==null) ||
            (cnaProfileId!=null && cnaDataTable==null)) {
            return;
        }

        func.call(window,mergeMutCnaTables(mutDataTable,cnaDataTable));
    }
    
    function mergeMutCnaTables(mutDataTable,cnaDataTable) {
        if (mutDataTable==null)
            return cnaDataTable;
        if (cnaDataTable==null)
            return mutDataTable;
            
         return google.visualization.data.join(mutDataTable, cnaDataTable,
                    'full', [[0,0]], [1],[1]);
    }
</script>
    

<fieldset style="padding:0px 1px">
    <legend style="color:blue;font-weight:bold;">Mutation Count VS. Copy Number Alteration</legend>
    <div style="display:none">
        <form name="input" action="patient.do" method="get">
            <select id="case-select" name="<%=PatientView.PATIENT_ID%>"><option id="null_case_select"></option></select>
            <input type="submit" id="submit-patient-btn" value="More About This Case" />
        </form>
    </div>
    <div id="mut-cna-scatter-plot" class="large-plot-div">
        <img src="images/ajax-loader.gif"/>
    </div>
    <table style="display:none;width:100%;" id="mut-cna-config">
        <tr width="100%">
                <td align="left">
                    H-Axis scale: <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>Normal &nbsp;
                    <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-haxis-log"/>log<br/>
                    V-Axis scale: <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>Normal &nbsp;
                    <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-vaxis-log"/>log
                </td>
                <td id="case-id-div" align="right">
                </td>
        </tr>
    </table>
</fieldset>