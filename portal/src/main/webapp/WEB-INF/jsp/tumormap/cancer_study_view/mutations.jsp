
<%@ page import="org.mskcc.cbio.portal.servlet.MutSigJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

<style type="text/css">
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .dataTables_length {
                width: auto;
                float: right;
        }
        .dataTables_info {
                clear: none;
                width: auto;
                float: right;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
        .smg-table-name {
                float: left;
                font-weight: bold;
                font-size: 120%;
                vertical-align: middle;
        }
</style>

<script type="text/javascript">   
    $(document).ready(function(){
        $('#smg_wrapper_table').hide();
        loadMutSigData(studyId);
    });
    
    function loadMutSigData(cancerStudyId) {
        var params = {<%=MutSigJSON.SELECTED_CANCER_STUDY%>: cancerStudyId};
        $.get("MutSig.json",
            params,
            function(data){
                if (data==null || (typeof data) != (typeof []) || data.length==0) {
                    $("#mut-sig-msg").html("MutSig data is not available for this cancer study.");
                    return;
                }
                $("#mut-sig-msg").hide();
                
                var ix = [];
                var n = data.length;
                for (var i=0; i<n; i++) {
                        ix.push([i]);
                }
                
                var oTable = $('#smg_table').dataTable( {
                    "sDom": '<"H"<"smg-table-name">fr>t<"F"<"datatable-paging"pil>>', // selectable columns
                    "bJQueryUI": true,
                    "bDestroy": true,
                    "aaData": ix,
                    "aoColumnDefs":[
                        {// data
                            "aTargets": [ 0 ],
                            "bVisible": false,
                            "mData" : 0
                        },
                        {// gene
                            "aTargets": [ 1 ],
                            "sClass": "center-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    var gene = data[source[0]]['gene_symbol'];
                                    return '<a href="<%=SkinUtil.getCbioPortalUrl()%>index.do?Action=Submit&genetic_profile_ids='
                                        +mutationProfileId+'&case_set_id='+studyId+'_all&cancer_study_id='+studyId
                                        +'&gene_list='+gene+'&tab_index=tab_visualize&#mutation_details">'+gene+'</a>';
                                } else {
                                    return data[source[0]]['gene_symbol'];
                                }
                            }
                        },
                        {// #mut
                            "aTargets": [ 2 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['num_muts'];
                                }
                            },
                            "asSorting": ["desc", "asc"]
                        },
                        {// qval
                            "aTargets": [ 3 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    return data[source[0]]['qval'].toPrecision(3);
                                } else {
                                    return data[source[0]]['qval'];
                                }
                            },
                            "asSorting": ["asc", "desc"]
                        }
                    ],
                    "aaSorting": [[3,'asc']],
                    "oLanguage": {
                        "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                        "sInfoFiltered": "",
                        "sLengthMenu": "Show _MENU_ per page"
                    },
                    "iDisplayLength": 25,
                    "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
                });
                
                oTable.css("width","100%");
                
                $('.smg-table-name').html(n+" significantly mutated genes");
                
                $('#smg_wrapper_table').show();
            });
    }
</script>

<div id="mut-sig-msg"><img src="images/ajax-loader.gif"/></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="smg_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="smg_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b>Gene</b></th>
                        <th><b># Mutations</b></th>
                        <th><b>Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>