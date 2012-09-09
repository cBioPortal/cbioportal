<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>


<style type="text/css" title="currentStyle">
        .cna-summary-table-name {
                float: left;
                font-weight: bold;
                font-size: 120%;
        }
        .cna-show-more {
            float: left;
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
    
    function updateCnaContext(oTable, summaryOnly) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            if (summaryOnly && !oTable.fnGetData(row, cnaTableIndices['overview'])) continue;
            oTable.fnUpdate(true, row, cnaTableIndices['altrate'], false, false);
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    var cnaContext = null;
    function loadCnaContextData(cna_table, cna_summary_table) {
        var params = {
            <%=CnaJSON.CMD%>:'<%=CnaJSON.GET_CONTEXT_CMD%>',
            <%=PatientView.CNA_PROFILE%>:cnaProfileId,
            <%=CnaJSON.CNA_EVENT_ID%>:cnaEventIds
        };
        
        $.post("cna.json", 
            params,
            function(context){
                cnaContext = context;
                updateCnaContext(cna_table, false);
                updateCnaContext(cna_summary_table, true);
            }
            ,"json"
        );
    }
    
    var cnaTableIndices = {id:0,gene:1,alteration:2,gistic:3,sanger:4,drug:5,overview:6,altrate:7,note:8};
    function buildCnaDataTable(cnas, table_id, isSummary, sDom, iDisplayLength) {
        var oTable = $(table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": cnas,
                "aoColumnDefs":[
                    {// event id
                        "bVisible": false,
                        "aTargets": [ cnaTableIndices['id'] ]
                    },
                    {// gene
                        "aTargets": [ cnaTableIndices['gene'] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[cnaTableIndices["gene"]]=value;
                            } else if (type==='display') {
                                return "<b>"+source[cnaTableIndices["gene"]]+"</b>";
                            } else {
                                return source[cnaTableIndices["gene"]];
                            }
                        }
                    },
                    {// alteration
                        "aTargets": [ cnaTableIndices['alteration'] ],
                        "fnRender": function(obj) {
                            return "<b>"+obj.aData[ obj.iDataColumn ]+"</b>";
                        }
                    },
                    {// gistic
                        "sType": "gistic-col",
                        "bVisible": false,
                        "aTargets": [ cnaTableIndices['gistic'] ]
                    },
                    {// sanger
                        "bVisible": !isSummary,
                        "aTargets": [ cnaTableIndices['sanger'] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[cnaTableIndices["sanger"]]=value;
                            } else if (type==='display') {
                                var sanger = source[cnaTableIndices["sanger"]];
                                return sanger?'Y':'';
                            } else if (type==='sort') {
                                var sanger = source[cnaTableIndices["sanger"]];
                                return sanger?'Y':'N';
                            }  else if (type==='filter') {
                                var sanger = source[cnaTableIndices["sanger"]];
                                return sanger?'sanger':'';
                            } else {
                                return source[cnaTableIndices["sanger"]];
                            }
                        }
                    },
                    {// show in summary
                        "bVisible": false,
                        "aTargets": [ cnaTableIndices['overview'] ]
                    },
                    {// context
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                source[cnaTableIndices["altrate"]]=value;
                            } else if (type==='display') {
                                if (!source[cnaTableIndices["altrate"]]) return "<img src=\"images/ajax-loader2.gif\">";
                                var con = cnaContext[source[cnaTableIndices["id"]]];
                                var perc = 100.0 * con / numPatientInSameCnaProfile;
                                return con + " (<b>" + perc.toFixed(1) + "%</b>)<br/>";
                            } else if (type==='sort') {
                                if (!source[cnaTableIndices["altrate"]]) return 0;
                                var con = ''+cnaContext[source[cnaTableIndices["id"]]];
                                var pad = '000000';
                                return pad.substring(0, pad.length - con.length) + con;
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"],
                        "aTargets": [ cnaTableIndices['altrate'] ]
                    },
                    {// Drugs
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                source[cnaTableIndices["drug"]]=value;
                            } else if (type==='display') {
                                var drug = source[cnaTableIndices["drug"]];
                                if (!drug) return '';
                                var len = drug.length;
                                if (len==0) return '';
                                return "<a href=\"#\" onclick=\"openDrugDialog('"
                                            +drug.join(',')+"'); return false;\">"
                                            +len+" drug"+(len>1?"s":"")+"</a>";
                            } else if (type==='sort') {
                                var drug = source[cnaTableIndices["drug"]];
                                var n = ''+(drug ? drug.length : 0);
                                var pad = '000000';
                                return pad.substring(0, pad.length - n.length) + n;
                            } else if (type==='filter') {
                                var drug = source[cnaTableIndices["drug"]];
                                return drug ? 'drug' : '';
                            } else {
                                var drug = source[cnaTableIndices["drug"]];
                                return drug ? drug : '';
                            }
                        },
                        "asSorting": ["desc", "asc"],
                        "aTargets": [ cnaTableIndices['drug'] ]
                    },
                    {// note
                        "bVisible": placeHolder,
                        "mDataProp": null,
                        "sDefaultContent": "",
                        "aTargets": [ cnaTableIndices['note'] ]
                    }
                ],
                "aaSorting": [[cnaTableIndices['gistic'],'asc'],[cnaTableIndices['altrate'],'desc'],[cnaTableIndices['drug'],'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page"
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        $(table_id).css("width","100%");
        return oTable;
    }
    
    var cna_table;
    var cna_summary_table;
    $(document).ready(function(){
        $('#cna_wrapper_table').hide();
        $('#cna_id_filter_msg').hide();
        var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.CNA_PROFILE%>:cnaProfileId
        };
                        
        $.post("cna.json", 
            params,
            function(cnas){
                // cna
                cna_table = buildCnaDataTable(cnas, '#cna_table', false, '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                $('#cna_wrapper_table').show();
                $('#cna_wait').remove();
                
                cnaEventIds = getEventString(cnas,cnaTableIndices['id']);
                overviewCnaEventIds = getEventString(cnas,cnaTableIndices['id'],cnaTableIndices['overview']);
                overviewCnaGenes = getEventString(cnas,cnaTableIndices['gene'],cnaTableIndices['overview']);
                
                geObs.fire('cna-built');
                
                // summary table
                cna_summary_table = buildCnaDataTable(cnas, '#cna_summary_table', true, '<"H"<"cna-summary-table-name">fr>t<"F"<"cna-show-more"><"datatable-paging"pil>>', 10);
                $('.cna-show-more').html("<a href='#cna' id='switch-to-cna-tab' title='Show more copy number alterations of this patient'>Show all "+cnas.length+" copy number alterations</a>");
                $('#switch-to-cna-tab').click(function () {
                    switchToTab('cna');
                    return false;
                });
                cna_summary_table.fnFilter('true', cnaTableIndices['overview']);
                $('.cna-summary-table-name').html(cna_summary_table.fnSettings().fnRecordsDisplay()
                    +' copy Number Alterations (CNAs) of Interest (out of '+cnas.length+" CNAs)"
                    +" <img class='cna_help' src='images/help.png'"
                    +" title='This table contains genes that are either recurrently copy number altered (Gistic Q-value<0.05) or in the Sanger Cancer Gene Census.'/>");
                $('#cna_summary_wrapper_table').show();
                $('#cna_summary_wait').remove();

                // help
                $('.cna_help').tipTip();
                
                loadCnaContextData(cna_table, cna_summary_table);
            }
            ,"json"
        );
    });
    
    function filterCnaTableByIds(mutIdsRegEx) {
        var n = cna_table.fnSettings().fnRecordsDisplay();
        cna_table.fnFilter(mutIdsRegEx, cnaTableIndices['id'],true);
        if (n!=cna_table.fnSettings().fnRecordsDisplay())
            $('#cna_id_filter_msg').show();
    }
    
    function unfilterCnaTableByIds() {
        cna_table.fnFilter("", cnaTableIndices['id']);
        $('#cna_id_filter_msg').hide();
    }
</script>

<div id="cna_wait"><img src="images/ajax-loader.gif"/></div>
<div id="cna_id_filter_msg"><font color="red">The following table contains filtered copy number alterations (CNAs).</font>
<button onclick="unfilterCnaTableByIds(); return false;" style="font-size: 1em;">Show all CNAs</button></div>

<table cellpadding="0" cellspacing="0" border="0" id="cna_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="cna_table">
                <%@ include file="cna_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>