<%@ page import="org.mskcc.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cgds.dao.DaoMutSig" %>

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .mutation-summary-table-name {
                float: left;
                font-weight: bold;
                font-size: 130%;
        }
        .mutation-show-more {
            float: left;
        }
        .dataTables_length {
                width: auto;
                float: right;
        }
        .dataTables_info {
                width: auto;
                float: right;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
</style>

<script type="text/javascript">
    
    jQuery.fn.dataTableExt.oSort['mutsig-col-asc']  = function(x,y) {
        if (x==null) {
            return y==null ? 0 : 1;
        }
        if (y==null)
            return -1;
	return ((x < y) ? -1 : ((x > y) ?  1 : 0));
    };

    jQuery.fn.dataTableExt.oSort['mutsig-col-desc'] = function(x,y) {
        if (isNaN(x)) {
            return y==null ? 0 : 1;
        }
        if (y==null)
            return -1;
	return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
    };
    
    function buildMutationsDataTable(mutations, table_id, sDom, iDisplayLength) {
        var oTable = $(table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": mutations,
                "aoColumnDefs":[
                    {// event id
                        "bVisible": false,
                        "aTargets": [ 0 ]
                    },
                    {// gene
                        "aTargets": [ 1 ],
                        "fnRender": function(obj) {
                            return "<b>"+obj.aData[ obj.iDataColumn ]+"</b>";
                        }
                    },
                    {// aa change
                        "aTargets": [ 2 ],
                        "fnRender": function(obj) {
                            return "<b><i>"+obj.aData[ obj.iDataColumn ]+"</i></b>";
                        }
                    },
                    {// mutsig
                        "sType": "mutsig-col",
                        "bVisible": false,
                        "aTargets": [ 5 ]
                    },
                    {// in overview
                        "bVisible": false,
                        "aTargets": [ 6 ]
                    },
                    {// mutation rate
                        "mDataProp": null,
                        "sDefaultContent": "<img src=\"images/ajax-loader2.gif\">",
                        "aTargets": [ 7 ]
                    },
                    {// drugs
                        "mDataProp": null,
                        "sDefaultContent": "<img src=\"images/ajax-loader2.gif\">",
                        "aTargets": [ 8 ]
                    },
                    {// note
                        "bVisible": placeHolder,
                        "mDataProp": null,
                        "sDefaultContent": "",
                        "aTargets": [ 9 ]
                    }
                ],
                "aaSorting": [[5,'asc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page"
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        // help
        $('.mutations_help').tipTip();

        $(table_id).css("width","100%");
        return oTable;
    }
    
    numPatientInSameMutationProfile = <%=numPatientInSameMutationProfile%>;
    
    function getMutationContextMap(mutationContext, geneContext, mutations) {
        var map = {};
        var nRows = mutations.length;
        for (var row=0; row<nRows; row++) {
            var eventId = mutations[row][0];
            var gene = trimHtml(mutations[row][1]);
            var aa = trimHtml(mutations[row][2]);
            var mutCon = mutationContext[eventId];
            var mutPerc = 100.0 * mutCon / numPatientInSameMutationProfile;
            var geneCon = geneContext[gene];
            var genePerc = 100.0 * geneCon / numPatientInSameMutationProfile;
            var context = gene + ": " + geneCon + " (<b>" + genePerc.toFixed(1) + "%</b>)<br/><i>"
                        + aa + "</i>: " + mutCon + " (<b>" + mutPerc.toFixed(1) + "%</b>)<br/>";
            map[eventId] = context;
        }
        return map;
     }
    
    function updateMutationContext(mutationContextMap, oTable, summaryOnly) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            if (summaryOnly && !oTable.fnGetData(row, 6)) continue;
            var eventId = oTable.fnGetData(row, 0);
            var context = mutationContextMap[eventId];
            oTable.fnUpdate(context, row, 7, false, false);
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    function loadMutationContextData(mutations, mut_table, mut_summary_table) {
        var params = {
            <%=MutationsJSON.CMD%>:'<%=MutationsJSON.GET_CONTEXT_CMD%>',
            <%=PatientView.MUTATION_PROFILE%>:'<%=mutationProfile.getStableId()%>',
            <%=MutationsJSON.MUTATION_EVENT_ID%>:mutEventIds
        };
        
        $.post("mutations.json", 
            params,
            function(context){
                var mutationContext = context['<%=MutationsJSON.MUTATION_CONTEXT%>'];
                var geneContext =  context['<%=MutationsJSON.GENE_CONTEXT%>'];
                var mutationContextMap = getMutationContextMap(mutationContext,
                            geneContext, mutations);
                
                updateMutationContext(mutationContextMap, mut_table, false);
                updateMutationContext(mutationContextMap, mut_summary_table, true);
            }
            ,"json"
        );
    }
    
    function updateMutationDrugs(drugMap, oTable, summaryOnly) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            if (summaryOnly && !oTable.fnGetData(row, 6)) continue;
            var gene = trimHtml(oTable.fnGetData(row, 1));
            var context = drugMap[gene];
            if (context==null) {
                context = "";
            }
            oTable.fnUpdate(context, row, 8, false, false);
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    function loadMutationDrugData(mut_table, mut_summary_table) {
        var params = {
            <%=MutationsJSON.CMD%>:'<%=MutationsJSON.GET_DRUG_CMD%>',
            <%=PatientView.MUTATION_PROFILE%>:'<%=mutationProfile.getStableId()%>',
            <%=MutationsJSON.MUTATION_EVENT_ID%>:mutEventIds
        };
        
        $.post("mutations.json", 
            params,
            function(drugs){
                var drugMap = getDrugMap(drugs);
                updateMutationDrugs(drugMap, mut_table, false);
                updateMutationDrugs(drugMap, mut_summary_table, true);
            }
            ,"json"
        );
    }
    
    $(document).ready(function(){
        $('#mutation_wrapper_table').hide();
        var params = {
            <%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.MUTATION_PROFILE%>:'<%=mutationProfile.getStableId()%>'
        };
                        
        $.post("mutations.json", 
            params,
            function(mutations){
                // mutations
                var mut_table = buildMutationsDataTable(mutations, '#mutation_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                $('#mutation_wrapper_table').show();
                $('#mutation_wait').remove();
                
                mutEventIds = getEventString(mutations,0);
                overviewMutEventIds = getEventString(mutations,0,6);
                overviewMutGenes = getEventString(mutations,1,6);
                
                geObs.fire('mutations-built');
                
                // summary table
                var mut_summary = buildMutationsDataTable(mutations, '#mutation_summary_table', '<"H"<"mutation-summary-table-name">fr>t<"F"<"mutation-show-more"><"datatable-paging"pil>>', 5);
                $('.mutation-summary-table-name').html('Mutations of Interest');
                $('.mutation-show-more').html("<a href='#mutations' id='switch-to-mutations-tab' title='Show more mutations of this patient'>Show more mutations</a>");
                $('#switch-to-mutations-tab').click(function () {
                    switchToTab('mutations');
                    return false;
                });
                mut_summary.fnFilter('true', 6);
                $('#mutation_summary_wrapper_table').show();
                $('#mutation_summary_wait').remove();
                
                loadMutationContextData(mutations, mut_table, mut_summary);
                loadMutationDrugData(mut_table, mut_summary);
            }
            ,"json"
        );
    });
</script>

<div id="mutation_wait"><img src="images/ajax-loader.gif"/></div>

<table cellpadding="0" cellspacing="0" border="0" id="mutation_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_table">
                <%@ include file="mutations_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>