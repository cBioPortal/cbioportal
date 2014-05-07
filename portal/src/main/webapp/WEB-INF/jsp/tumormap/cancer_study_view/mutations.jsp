
<%@ page import="org.mskcc.cbio.portal.servlet.MutSigJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<script type="text/javascript">   
    $(document).ready(function(){
        $('#smg_wrapper_table').hide();
        if (mutationProfileId) loadMutatedGenes(cancerStudyId, mutationProfileId, hasMutSig);
    });
    
    function loadMutatedGenes(cancerStudyId, mutationProfileId, hasMutSig) {
        var params = {
            cmd: 'get_smg',
            mutation_profile: mutationProfileId
        };
        $.get("mutations.json",
            params,
            function(data){
                if (data===null || (typeof data) !== (typeof []) || data.length===0) {
                    $("#mut-sig-msg").html("No recurrently mutated genes (SMGs) are detected in this cancer study.");
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
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    var gene = data[source[0]]['gene_symbol'];
                                    return '<a href="index.do?Action=Submit&genetic_profile_ids='
                                        +mutationProfileId+'&case_set_id='+cancerStudyId+'_all&cancer_study_id='+cancerStudyId
                                        +'&gene_list='+gene+'&tab_index=tab_visualize&#mutation_details">'+gene+'</a>';
                                } else {
                                    return data[source[0]]['gene_symbol'];
                                }
                            }
                        },
                        {// cytoband
                            "aTargets": [ 2 ],
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else {
                                    return data[source[0]]['cytoband'];
                                }
                            },
                        },
                        {// gene size
                            "aTargets": [ 3 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    var length = data[source[0]]['length'];
                                    return length ? length : "";
                                } else if (type==='sort') {
                                    var length = data[source[0]]['length'];
                                    return length ? length : 0;
                                } else if (type==='type') {
                                    return 0.0;
                                } else {
                                    var length = data[source[0]]['length'];
                                    return length ? length : 0;
                                }
                            }
                        },
                        {// #muts
                            "aTargets": [ 4 ],
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
                        {// #muts / bp
                            "aTargets": [ 5 ],
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    var muts = data[source[0]]['num_muts'];
                                    var length = data[source[0]]['length'];
                                    return length ? cbio.util.toPrecision(muts/length, 3, 0.01) : "";
                                } else if (type==='sort') {
                                    var muts = data[source[0]]['num_muts'];
                                    var length = data[source[0]]['length'];
                                    return length ? muts/length : 0;
                                } else if (type==='type') {
                                    return 0.0;
                                } else {
                                    var muts = data[source[0]]['num_muts'];
                                    var length = data[source[0]]['length'];
                                    return length ? muts/length : 0;
                                }
                            },
                            "asSorting": ["desc", "asc"]
                        },
                        {// mutsig qval
                            "aTargets": [ 6 ],
                            "bVisible": hasMutSig,
                            "sClass": "right-align-td",
                            "mDataProp": function(source,type,value) {
                                if (type==='set') {
                                    return;
                                } else if (type==='display') {
                                    var qval = data[source[0]]['qval'];
                                    return cbio.util.checkNullOrUndefined(qval) ? "":cbio.util.toPrecision(qval, 3, 0.01);
                                } else if (type==='sort') {
                                    var qval = data[source[0]]['qval'];
                                    return cbio.util.checkNullOrUndefined(qval) ? 1 : qval;
                                } else if (type==='type') {
                                    return 0.0;
                                } else {
                                    var qval = data[source[0]]['qval'];
                                    return cbio.util.checkNullOrUndefined(qval) ? 1 : qval;;
                                }
                            },
                            "asSorting": ["asc", "desc"]
                        }
                    ],
                    "aaSorting": [[6,'asc'],[5,'desc']],
                    "oLanguage": {
                        "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                        "sInfoFiltered": "",
                        "sLengthMenu": "Show _MENU_ per page"
                    },
                    "iDisplayLength": 25,
                    "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
                });
                
                oTable.css("width","100%");
                
                $('.smg-table-name').html(n+" mutated genes <img id='mutations-summary-help' src='images/help.png' title='Genes that <ul><li>are in the top 500 (ranked by mutations per nucleotide) recurrently mutated (2 or more mutations)</li><li>or are cancer genes</li><li>or are detected by MutSig</li></ul>.'>");
                $('#mutations-summary-help').qtip({
                    content: { attr: 'title' },
                    style: { classes: 'qtip-light qtip-rounded' },
                    position: { my:'top center',at:'bottom center',viewport: $(window) }
                });
                
                $('.smg-table-name').addClass('data-table-name');
                
                $('#smg_wrapper_table').show();
            });
    }
</script>

<div id="mut-sig-msg"><img src="images/ajax-loader.gif"/></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="smg_wrapper_table">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="smg_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b>Gene</b></th>
                        <th><b>Cytoband</b></th>
                        <th><b>Gene size (Nucleotides)</b></th>
                        <th><b># Mutations</b></th>
                        <th><b># Mutations / Nucleotide</b></th>
                        <th><b>Mutsig Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>