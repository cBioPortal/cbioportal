<%@ page import="org.mskcc.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.portal.servlet.CnaJSON" %>


<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .cna-summary-table-name {
                float: left;
                font-weight: bold;
                font-size: 130%;
        }
        .cna-show-more {
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
    
    jQuery.fn.dataTableExt.oSort['gistic-col-asc']  = function(x,y) {
        if (x==null) {
            return y==null ? 0 : 1;
        }
        if (y==null)
            return -1;
	return ((x < y) ? -1 : ((x > y) ?  1 : 0));
    };

    jQuery.fn.dataTableExt.oSort['gistic-col-desc'] = function(x,y) {
        if (isNaN(x)) {
            return y==null ? 0 : 1;
        }
        if (y==null)
            return -1;
	return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
    };
    
    numPatientInSameCnaProfile = <%=numPatientInSameCnaProfile%>;
    
    function getCnaContextMap(cnaContext) {
        var map = {};
        for (var eventId in cnaContext) {
            var con = cnaContext[eventId];
            var perc = 100.0 * con / numPatientInSameCnaProfile;
            var context = con + " (<b>" + perc.toFixed(1) + "%</b>)<br/>";
            map[eventId] = context;
        }
        return map;
    }
    
    function updateCnaContext(cnaContextMap, oTable, summaryOnly) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            if (summaryOnly && !oTable.fnGetData(row, 6)) continue;
            var eventId = oTable.fnGetData(row, 0);
            var context = cnaContextMap[eventId];
            oTable.fnUpdate(context, row, 7, false);
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    function loadCnaContextData(cna_table, cna_summary_table) {
        
        var params = {
            <%=CnaJSON.CMD%>:'<%=CnaJSON.GET_CONTEXT_CMD%>',
            <%=PatientView.CNA_PROFILE%>:'<%=cnaProfile.getStableId()%>',
            <%=CnaJSON.CNA_EVENT_ID%>:cnaEventIds
        };
        
        $.post("cna.json", 
            params,
            function(context){
                var cnaContextMap = getCnaContextMap(context);
                updateCnaContext(cnaContextMap, cna_table, false);
                updateCnaContext(cnaContextMap, cna_summary_table, true);
                alert('done');
            }
            ,"json"
        );
    }
    
    var placeHolder = <%=Boolean.toString(showPlaceHoder)%>;
    function buildCnaDataTable(cnas, table_id, sDom, iDisplayLength) {
        var oTable = $(table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": cnas,
                "aoColumnDefs":[
                    {// event id
                        "bVisible": false,
                        "aTargets": [ 0 ]
                    },
                    {// clinical trials
                        "bVisible": placeHolder,
                        "aTargets": [ 3 ]
                    },
                    {// note
                        "bVisible": placeHolder,
                        "aTargets": [ 4 ]
                    },
                    {// gistic
                        "sType": "gistic-col",
                        "bVisible": false,
                        "aTargets": [ 5 ]
                    },
                    {// show in summary
                        "bVisible": false,
                        "aTargets": [ 6 ]
                    },
                    {
                        "mDataProp": null,
                        "sDefaultContent": "<img src=\"images/ajax-loader2.gif\">",
                        "aTargets": [ 7 ]
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
        $('.cna_help').tipTip();

        $(table_id).css("width","100%");
        return oTable;
    }
    
    $(document).ready(function(){
        $('#cna_wrapper_table').hide();
        var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.CNA_PROFILE%>:'<%=cnaProfile.getStableId()%>'
        };
                        
        $.post("cna.json", 
            params,
            function(cnas){
                // cna
                var cna_table = buildCnaDataTable(cnas, '#cna_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                $('#cna_wrapper_table').show();
                $('#cna_wait').remove();
                
                cnaEventIds = getEventIdString(cnas);
                
                $('#similar_patients_table').trigger('cna-built');
                
                // summary table
                var cna_summary = buildCnaDataTable(cnas, '#cna_summary_table', '<"H"<"cna-summary-table-name">fr>t<"F"<"cna-show-more"><"datatable-paging"pil>>', 5);
                $('.cna-summary-table-name').html('Copy Number Alterations of Interest');
                $('.cna-show-more').html("<a href='#cna' id='switch-to-cna-tab' title='Show more copy number alterations of this patient'>Show more copy number alterations</a>");
                $('#switch-to-cna-tab').click(function () {
                    switchToTab('cna');
                    return false;
                });
                cna_summary.fnFilter('true', 6);
                $('#cna_summary_wrapper_table').show();
                $('#cna_summary_wait').remove();
                
                loadCnaContextData(cna_table, cna_summary);
            }
            ,"json"
        );
    });
</script>

<div id="cna_wait"><img src="images/ajax-loader.gif"/></div>

<table cellpadding="0" cellspacing="0" border="0" id="cna_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="cna_table">
                <%@ include file="cna_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>