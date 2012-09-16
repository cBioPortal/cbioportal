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
    
    var cnaTableIndices = {id:0,gene:1,gistic:2,sanger:3,drug:4,alteration:5,altrate:6};
    function buildCnaDataTable(cnas, cnaEventIds, table_id, sDom, iDisplayLength) {
        var data = [];
        for (var i=0, nEvents=cnaEventIds.length; i<nEvents; i++) {
                data.push([cnaEventIds[i]]);
        }
        var oTable = $('#'+table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": data,
                "aoColumnDefs":[
                    {// event id
                        "aTargets": [ cnaTableIndices['id'] ],
                        "bVisible": false,
                        "mData" : 0
                    },
                    {// gene
                        "aTargets": [ cnaTableIndices['gene'] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return "<b>"+cnas.getValue(source[0], "gene")+"</b>";
                            } else {
                                return cnas.getValue(source[0], "gene");
                            }
                        }
                    },
                    {// gistic
                        "aTargets": [ cnaTableIndices['gistic'] ],
                        "bVisible": false,
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var gistic = cnas.getValue(source[0], 'gistic');
                                if (gistic==null) return "";
                                return gistic.toPrecision(2);
                            } else if (type==='sort') {
                                var gistic = cnas.getValue(source[0], 'gistic');
                                if (gistic==null) return 1.0;
                                return gistic;
                            } else if (type==='filter') {
                                return "gistic";
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return cnas.getValue(source[0], 'gistic');
                            }
                        }
                    },
                    {// sanger
                        "aTargets": [ cnaTableIndices['sanger'] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var sanger = cnas.getValue(source[0], 'sanger');
                                return sanger?'&#10004;':'';
                            } else if (type==='sort') {
                                var sanger = cnas.getValue(source[0], 'sanger');
                                return sanger?'1':'0';
                            }  else if (type==='filter') {
                                var sanger = cnas.getValue(source[0], 'sanger');
                                return sanger?'sanger':'';
                            } else {
                                return cnas.getValue(source[0], 'sanger');
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// Drugs
                        "aTargets": [ cnaTableIndices['drug'] ],
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var drug = cnas.getValue(source[0], 'drug');
                                if (!drug) return '';
                                var len = drug.length;
                                if (len==0) return '';
                                return "<a href='#' onclick='return false;' id='"
                                            +table_id+'_'+source[0]+"-drug-tip' class='"
                                            +table_id+"-drug-tip' alt='"+drug.join(',')+"'>"
                                            +len+" drug"+(len>1?"s":"")+"</a>";
                            } else if (type==='sort') {
                                var drug = cnas.getValue(source[0], 'drug');
                                return drug ? drug.length : 0;
                            } else if (type==='filter') {
                                var drug = cnas.getValue(source[0], 'drug');
                                return drug&&drug.length ? 'drugs' : '';
                            } else if (type==='type') {
                                return 0;
                            } else {
                                var drug = cnas.getValue(source[0], 'drug');
                                return drug ? drug : '';
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// alteration
                        "aTargets": [ cnaTableIndices['alteration'] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var alter = cnas.getValue(source[0], "alter");
                                var strAlt;
                                switch(alter) {
                                case -2: strAlt='Deletion'; break;
                                case 2: strAlt='Amplification'; break;
                                }
                                return "<b>"+strAlt+"</b>"
                            } else {
                                return cnas.getValue(source[0], "alter");
                            }
                        }
                    },
                    {// context
                        "aTargets": [ cnaTableIndices['altrate'] ],
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                if (!cnas.colExists('altrate')) return "<img src=\"images/ajax-loader2.gif\">";
                                var strAlt;
                                switch(cnas.getValue(source[0], "alter")) {
                                case -2: strAlt='deleted'; break;
                                case 2: strAlt='amplified'; break;
                                }
                                var alter = cnas.getValue(source[0], "alter");
                                var con = cnas.getValue(source[0], 'altrate');
                                var frac = con / numPatientInSameCnaProfile;
                                var tip = "In "+con+" sample"+(con==1?"":"s")
                                    +" (<b>"+(100*frac).toFixed(1) + "%</b>)"+" in "
                                    +cancerStudyName+", "+cnas.getValue(source[0], "gene")+" is "+strAlt;
                                var width = Math.ceil(40 * Math.log(frac+1) * Math.LOG2E)+3;
                                var clas = alter>0?"amp_percent_div":"del_percent_div"
                                return "&nbsp;<div class='"+clas+" "+table_id
                                            +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                            } else if (type==='sort') {
                                if (!cnas.colExists('altrate')) return 0;
                                return cnas.getValue(source[0], 'altrate');
                            } else if (type==='type') {
                                    return 0.0;
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
//                    {// note
//                        "bVisible": isSummary,
//                        "aTargets": [ cnaTableIndices['note'] ],
//                        "mDataProp": function(source,type,value) {
//                            if (!isSummary) return "";
//                            if (type==='set') {
//                                source[cnaTableIndices["note"]]=value;
//                            } else if (type==='display') {
//                                var notes = [];
//                                if (source[cnaTableIndices["sanger"]])
//                                    notes.push("<img src='images/sanger.png' width=15 height=15 class='"+table_id
//                                                +"-tip' alt='In <a href=\"http://cancer.sanger.ac.uk/cosmic/gene/overview?ln="
//                                                +source[cnaTableIndices["gene"]]+"\">Sanger Cancer Gene Census</a>'/>");
//                                var drug = source[cnaTableIndices["drug"]];
//                                if (drug && drug.length) {
//                                    notes.push("<img src='images/drug.png' width=15 height=15 id='"+table_id+'_'
//                                                +source[cnaTableIndices["id"]]+"-drug-note-tip' class='"
//                                                +table_id+"-drug-tip' alt='"+drug.join(',')+"'/>");
//                                }
//                                return notes.join("&nbsp;");
//                            } else if (type==='sort') {
//                                return "";
//                            } else {
//                                if (!source[cnaTableIndices["note"]]) return '';
//                                return source[cnaTableIndices["note"]];
//                            }
//                        },
//                        "bSortable" : false
//                    }
                ],
                "fnDrawCallback": function( oSettings ) {
                    addNoteTooltip("."+table_id+"-tip");
                    addDrugsTooltip("."+table_id+"-drug-tip");
                },
                "aaSorting": [[cnaTableIndices['gistic'],'asc'],[cnaTableIndices['altrate'],'desc'],[cnaTableIndices['drug'],'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page"
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        oTable.css("width","100%");
        return oTable;
    }
    
    numPatientInSameCnaProfile = <%=numPatientInSameCnaProfile%>;
    
    function updateCnaContext(oTable) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
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
            <%=CnaJSON.CNA_EVENT_ID%>:genomicEventObs.cnas.getEventIds(false).join(',')
        };
        
        $.post("cna.json", 
            params,
            function(context){
                genomicEventObs.cnas.addDataMap('altrate', context, 'id');
                updateCnaContext(cna_table);
                updateCnaContext(cna_summary_table);
            }
            ,"json"
        );
    }
    
    $(document).ready(function(){
        $('#cna_wrapper_table').hide();
        $('#cna_id_filter_msg').hide();
        var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.CNA_PROFILE%>:cnaProfileId
        };
                        
        $.post("cna.json", 
            params,
            function(data){
                genomicEventObs.cnas.setData(data);
                
                genomicEventObs.fire('cna-built');
                
                // summary table
                var cna_summary_table = buildCnaDataTable(genomicEventObs.cnas, genomicEventObs.cnas.getEventIds(true),
                        'cna_summary_table','<"H"<"cna-summary-table-name">fr>t<"F"<"cna-show-more"><"datatable-paging"pil>>',25);
                $('.cna-show-more').html("<a href='#cna' id='switch-to-cna-tab' title='Show more copy number alterations of this patient'>Show all "
                        +genomicEventObs.cnas.getNumEvents(false)+" copy number alterations</a>");
                $('#switch-to-cna-tab').click(function () {
                    switchToTab('cna');
                    return false;
                });
                $('.cna-summary-table-name').html(genomicEventObs.cnas.getNumEvents(true)
                    +' copy Number Alterations (CNAs) of Interest (out of '+genomicEventObs.cnas.getNumEvents(false)+" CNAs)"
                    +" <img class='cna_help' src='images/help.png'"
                    +" title='This table contains genes that are either recurrently copy number altered (Gistic Q-value<0.05) or in the Sanger Cancer Gene Census.'/>");
                $('#cna_summary_wrapper_table').show();
                $('#cna_summary_wait').remove();
                
                // cna
                var cna_table = buildCnaDataTable(genomicEventObs.cnas, genomicEventObs.cnas.getEventIds(false),
                        'cna_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                $('#cna_wrapper_table').show();
                $('#cna_wait').remove();

                // help
                $('.cna_help').tipTip();
                
                loadCnaContextData(cna_table, cna_summary_table);
            }
            ,"json"
        );
    });
    
    function filterCnaTableByIds(mutIdsRegEx) {
        var cna_table = $('#cna_table').dataTable();
        var n = cna_table.fnSettings().fnRecordsDisplay();
        cna_table.fnFilter(mutIdsRegEx, cnaTableIndices['id'],true);
        if (n!=cna_table.fnSettings().fnRecordsDisplay())
            $('#cna_id_filter_msg').show();
    }
    
    function unfilterCnaTableByIds() {
        var cna_table = $('#cna_table').dataTable();
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