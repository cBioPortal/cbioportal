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
                font-size: 120%;
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
    
    var mutTableIndices = {id:0,chr:1,start:2,end:3,gene:4,aa:5,type:6,status:7,mutsig:8,overview:9,mutrate:10,drug:11,note:12};
    function buildMutationsDataTable(mutations, table_id, sDom, iDisplayLength) {
        var oTable = $(table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": mutations,
                "aoColumnDefs":[
                    {// event id
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["id"] ]
                    },
                    {// chr
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["chr"] ]
                    },
                    {// start
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["start"] ]
                    },
                    {// end
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["end"] ]
                    },
                    {// gene
                        "aTargets": [ mutTableIndices["gene"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["gene"]]=value;
                            } else if (type==='display') {
                                return "<b>"+source[mutTableIndices["gene"]]+"</b>";
                            } else {
                                return source[mutTableIndices["gene"]];
                            }
                        }
                    },
                    {// aa change
                        "aTargets": [ mutTableIndices["aa"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["aa"]]=value;
                            } else if (type==='display') {
                                return "<b><i>"+source[mutTableIndices["aa"]]+"</i></b>";
                            } else {
                                return source[mutTableIndices["aa"]];
                            }
                        }
                    },
                    {// mutsig
                        "sType": "mutsig-col",
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["mutsig"] ]
                    },
                    {// in overview
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["overview"] ]
                    },
                    {// mutation rate
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["mutrate"]]=value;
                            } else if (type==='display') {
                                if (!source[mutTableIndices["mutrate"]]) return "<img src=\"images/ajax-loader2.gif\">";
                                var eventId = source[mutTableIndices["id"]];
                                var gene = source[mutTableIndices["gene"]];
                                var aa = source[mutTableIndices["aa"]];
                                var mutCon = mutAAContext[eventId];
                                var mutPerc = 100.0 * mutCon / numPatientInSameMutationProfile;
                                var geneCon = mutGeneContext[gene];
                                var genePerc = 100.0 * geneCon / numPatientInSameMutationProfile;
                                return gene + ": " + geneCon + " (<b>" + genePerc.toFixed(1) + "%</b>)<br/><i>"
                                            + aa + "</i>: " + mutCon + " (<b>" + mutPerc.toFixed(1) + "%</b>)<br/>";
                            } else if (type==='sort') {
                                if (!source[mutTableIndices["mutrate"]]) return 0;
                                var gene = source[mutTableIndices["gene"]];
                                var geneCon = ''+mutGeneContext[gene];
                                var pad = '000000';
                                return pad.substring(0, pad.length - geneCon.length) + geneCon;
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"],
                        "aTargets": [ mutTableIndices["mutrate"] ]
                    },
                    {// drugs
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["drug"]]=value;
                            } else if (type==='display') {
                                if (!source[mutTableIndices["drug"]]) return "<img src=\"images/ajax-loader2.gif\">";
                                var drug = mutDrugs[source[mutTableIndices["gene"]]];
                                if (drug==null) return '';
                                var len = drug.length;
                                return "<a href=\"#\" onclick=\"openDrugDialog('"
                                            +drug.join(',')+"'); return false;\">"
                                            +len+" drug"+(len>1?"s":"")+"</a>";
                            } else if (type==='sort') {
                                if (!source[mutTableIndices["drug"]]) return 0;
                                var drug = mutDrugs[source[mutTableIndices["gene"]]];
                                var n = ''+(drug ? drug.length : 0);
                                var pad = '000000';
                                return pad.substring(0, pad.length - n.length) + n;
                            } else if (type==='filter') {
                                if (!source[mutTableIndices["drug"]]) return '';
                                var drug = mutDrugs[source[mutTableIndices["gene"]]];
                                return drug ? 'drug' : '';
                            } else {
                                if (!source[mutTableIndices["drug"]]) return '';
                                var drug = mutDrugs[source[mutTableIndices["gene"]]];
                                return drug ? drug : '';
                            }
                        },
                        "asSorting": ["desc", "asc"],
                        "aTargets": [ mutTableIndices["drug"] ]
                    },
                    {// note 
                       "bVisible": placeHolder,
                        "mDataProp": null,
                        "sDefaultContent": "",
                        "aTargets": [ mutTableIndices["note"] ]
                    }
                ],
                "aaSorting": [[mutTableIndices["mutsig"],'asc'],[mutTableIndices["mutrate"],'desc'],[mutTableIndices["drug"],'desc']],
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
    
    var numPatientInSameMutationProfile = <%=numPatientInSameMutationProfile%>;
    
    function updateMutationContext(oTable, summaryOnly) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            if (summaryOnly && !oTable.fnGetData(row, mutTableIndices["overview"])) continue;
            oTable.fnUpdate('true', row, mutTableIndices["mutrate"], false, false);
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    var mutGeneContext = null;
    var mutAAContext = null;
    function loadMutationContextData(mutations, mut_table, mut_summary_table) {
        var params = {
            <%=MutationsJSON.CMD%>:'<%=MutationsJSON.GET_CONTEXT_CMD%>',
            <%=PatientView.MUTATION_PROFILE%>:'<%=mutationProfile.getStableId()%>',
            <%=MutationsJSON.MUTATION_EVENT_ID%>:mutEventIds
        };
        
        $.post("mutations.json", 
            params,
            function(context){
                mutAAContext = context['<%=MutationsJSON.MUTATION_CONTEXT%>'];
                mutGeneContext =  context['<%=MutationsJSON.GENE_CONTEXT%>'];
                updateMutationContext(mut_table, false);
                updateMutationContext(mut_summary_table, true);
            }
            ,"json"
        );
    }
    
    function updateMutationDrugs(oTable, summaryOnly) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            if (summaryOnly && !oTable.fnGetData(row, mutTableIndices["overview"])) continue;
            oTable.fnUpdate('true', row, mutTableIndices["drug"], false, false);
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    var mutDrugs = null;
    function loadMutationDrugData(mut_table, mut_summary_table) {
        var params = {
            <%=MutationsJSON.CMD%>:'<%=MutationsJSON.GET_DRUG_CMD%>',
            <%=PatientView.MUTATION_PROFILE%>:'<%=mutationProfile.getStableId()%>',
            <%=MutationsJSON.MUTATION_EVENT_ID%>:mutEventIds
        };
        
        $.post("mutations.json", 
            params,
            function(drugs){
                mutDrugs = drugs;
                updateMutationDrugs(mut_table, false);
                updateMutationDrugs(mut_summary_table, true);
            }
            ,"json"
        );
    }
    
    var mut_table;
    var mut_summary_table;
    $(document).ready(function(){
        $('#mutation_id_filter_msg').hide();
        $('#mutation_wrapper_table').hide();
        var params = {
            <%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.MUTATION_PROFILE%>:'<%=mutationProfile.getStableId()%>'
        };
                        
        $.post("mutations.json", 
            params,
            function(mutations){
                // mutations
                mut_table = buildMutationsDataTable(mutations, '#mutation_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                $('#mutation_wrapper_table').show();
                $('#mutation_wait').remove();
                
                mutEventIds = getEventString(mutations,mutTableIndices["id"]);
                overviewMutEventIds = getEventString(mutations,mutTableIndices["id"],mutTableIndices["overview"]);
                overviewMutGenes = getEventString(mutations,mutTableIndices["gene"],mutTableIndices["overview"]);
                
                geObs.fire('mutations-built');
                
                // summary table
                mut_summary_table = buildMutationsDataTable(mutations, '#mutation_summary_table', '<"H"<"mutation-summary-table-name">fr>t<"F"<"mutation-show-more"><"datatable-paging"pil>>', 5);
                $('.mutation-show-more').html("<a href='#mutations' id='switch-to-mutations-tab' title='Show more mutations of this patient'>Show all "+mutations.length+" mutations</a>");
                $('#switch-to-mutations-tab').click(function () {
                    switchToTab('mutations');
                    return false;
                });
                mut_summary_table.fnFilter('true', mutTableIndices["overview"]);
                $('.mutation-summary-table-name').html(mut_summary_table.fnSettings().fnRecordsDisplay()+' mutations of Interest (out of '+mutations.length+' mutations)');
                $('#mutation_summary_wrapper_table').show();
                $('#mutation_summary_wait').remove();
                
                loadMutationContextData(mutations, mut_table, mut_summary_table);
                loadMutationDrugData(mut_table, mut_summary_table);
            }
            ,"json"
        );
    });
    
    function filterMutationsTableByIds(mutIdsRegEx) {
        var n = mut_table.fnSettings().fnRecordsDisplay();
        mut_table.fnFilter(mutIdsRegEx, mutTableIndices["id"],true);
        if (n!=mut_table.fnSettings().fnRecordsDisplay())
            $('#mutation_id_filter_msg').show();
    }
    
    function unfilterMutationsTableByIds() {
        mut_table.fnFilter("", mutTableIndices["id"]);
        $('#mutation_id_filter_msg').hide();
    }
</script>

<div id="mutation_wait"><img src="images/ajax-loader.gif"/></div>
<div id="mutation_id_filter_msg"><font color="red">The following table contains filtered mutations.</font>
<button onclick="unfilterMutationsTableByIds(); return false;" style="font-size: 1em;">Show all mutations</button></div>
<table cellpadding="0" cellspacing="0" border="0" id="mutation_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_table">
                <%@ include file="mutations_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>