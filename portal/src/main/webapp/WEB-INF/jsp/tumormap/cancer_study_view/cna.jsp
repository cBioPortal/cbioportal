
<%@ page import="org.mskcc.cbio.portal.servlet.GisticJSON" %>
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
        .gistic-table-name {
                float: left;
                font-weight: bold;
                font-size: 120%;
                vertical-align: middle;
        }
</style>

<script type="text/javascript">   
    $(document).ready(function(){
        $('#gistic_wrapper_table').hide();
        loadGisticData(studyId);
    });
    
    function loadGisticData(cancerStudyId) {
        var params = {<%=GisticJSON.SELECTED_CANCER_STUDY%>: cancerStudyId};
        $.get("Gistic.json",
            params,
            function(data){
                if (data==null || (typeof data) != (typeof []) || data.length==0) {
                    $("#gistic-msg").html("Gistic data is not available for this cancer study.");
                    return;
                }
                $("#gistic-msg").hide();
                
                var ix = [];
                var n = data.length;
                for (var i=0; i<n; i++) {
                        ix.push([i]);
                }
                
                var oTable = $('#gistic_table').dataTable( {
                    "sDom": '<"H"<"gistic-table-name">fr>t<"F"<"datatable-paging"pil>>', // selectable columns
                    "bJQueryUI": true,
                    "bDestroy": true,
                    "aaData": ix,
                    "aoColumnDefs":[
                        {// data
                            "aTargets": [ 0 ],
                            "bVisible": false,
                            "mData" : 0
                        },
                        {// amp/del
                            "aTargets": [ 1 ],
                            "sClass": "center-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    if (data[source[0]]['ampdel'])
                                        return "<font color='red'>AMP</font>";
                                    else
                                        return "<font color='blue'>DEL</font>";
                                } else {
                                    return data[source[0]]['ampdel'];
                                }
                            }
                        },
                        {// chr
                            "aTargets": [ 2 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['chromosome'];
                                }
                            }
                        },
                        {// cytoband
                            "aTargets": [ 3 ],
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['cytoband'];
                                }
                            }
                        },
                        {// #gene
                            "aTargets": [ 4 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['sangerGenes'].length
                                        + data[source[0]]['nonSangerGenes'].length;
                                }
                            }
                        },
                        {// gene
                            "aTargets": [ 5 ],
                            "iDataSort": 4,
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    var genes = data[source[0]]['sangerGenes'].slice(0).concat(data[source[0]]['nonSangerGenes']);
                                   if (type==='display') {
                                        var linkedGenes = [];
                                        for (var i=0; i<genes.length; i++) {
                                            linkedGenes.push('<a href="<%=SkinUtil.getCbioPortalUrl()%>index.do?Action=Submit&genetic_profile_ids='
                                                +mutationProfileId+'&case_set_id='+studyId+'_all&cancer_study_id='+studyId
                                                +'&gene_list='+genes[i]+'&tab_index=tab_visualize&#mutation_details">'+genes[i]+'</a>');
                                        }
                                        return linkedGenes.join(" ");
                                    } else {
                                        return genes.join(" ");
                                    }
                                }
                            }
                        },
                        {// qval
                            "aTargets": [ 6 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    return cbio.util.round_to_2SF(data[source[0]]['qval'], 0.1);
                                } else {
                                    return data[source[0]]['qval'];
                                }
                            },
                            "asSorting": ["asc", "desc"]
                        }
                    ],
                    "aaSorting": [[6,'asc']],
                    "oLanguage": {
                        "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                        "sInfoFiltered": "",
                        "sLengthMenu": "Show _MENU_ per page"
                    },
                    "iDisplayLength": 25,
                    "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
                });
                
                oTable.css("width","100%");
                
                $('.gistic-table-name').html(n+" significantly mutated genes");
                
                $('#gistic_wrapper_table').show();
            });
    }
</script>

<div id="gistic-msg"><img src="images/ajax-loader.gif"/></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="gistic_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="gistic_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b><font color="red">AMP</font>/<font color="blue">DEL</font></b></th>
                        <th><b>Chr</b></th>
                        <th><b>Cytoband</b></th>
                        <th><b># Genes</b></th>
                        <th><b>Genes</b></th>
                        <th><b>Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>