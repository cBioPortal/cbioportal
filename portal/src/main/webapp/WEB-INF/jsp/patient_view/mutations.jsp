<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.cgds.dao.DaoMutSig" %>

<style type="text/css" title="currentStyle">
        .mutation-summary-table-name {
                float: left;
                font-weight: bold;
                font-size: 100%;
                vertical-align: middle;
        }
        .mutation-show-more {
            float: left;
        }
</style>

<script type="text/javascript">
    var mutTableIndices = {id:0,gene:1,genemutrate:2,mutsig:3,
        sanger:4,drug:5,aa:6,type:7,keymutrate:8,cosmic:9,ma:10};
    function buildMutationsDataTable(mutations,mutEventIds, table_id, sDom, iDisplayLength) {
        var data = [];
        for (var i=0, nEvents=mutEventIds.length; i<nEvents; i++) {
                data.push([mutEventIds[i]]);
        }
        var oTable = $("#"+table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": data,
                "aoColumnDefs":[
                    {// event id
                        "aTargets": [ mutTableIndices["id"] ],
                        "bVisible": false,
                        "mData" : 0
                    },
                    {// gene
                        "aTargets": [ mutTableIndices["gene"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return "<b>"+mutations.getValue(source[0], "gene")+"</b>";
                            } else {
                                return mutations.getValue(source[0], "gene");
                            }
                        }
                    },
                    {// gene mutation rate
                        "aTargets": [ mutTableIndices["genemutrate"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                if (!mutations.colExists('genemutrate')) return "<img height=12 width=12 src='images/ajax-loader2.gif'>";
                                var geneCon = mutations.getValue(source[0], 'genemutrate')-1;
                                if (geneCon<=0) return '';
                                var frac = geneCon/numPatientInSameMutationProfile;
                                var tip = "<b>"+geneCon+" other sample"+(geneCon==1?"":"s")
                                    +"</b> ("+(100*frac).toFixed(1) + "%)"+" in the cohort study ("
                                    +cancerStudyName+") "+(geneCon==1?"has":"have")+" mutated "
                                    +mutations.getValue(source[0], "gene");
                                var width = Math.min(40, Math.ceil(80 * Math.log(frac+1) * Math.LOG2E)+3);
                                return "<div class='mutation_percent_div "+table_id
                                                +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                            } else if (type==='sort') {
                                if (!mutations.colExists('genemutrate')) return 0;
                                return mutations.getValue(source[0], 'genemutrate');
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                if (mutations['genemutrate']==null) return 0;
                                return mutations.getValue(source[0], 'genemutrate');
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// mutsig
                        "aTargets": [ mutTableIndices["mutsig"] ],
                        "bVisible": !mutations.colAllNull('mutsig'),
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var mutsig = mutations.getValue(source[0], 'mutsig');
                                if (mutsig==null) return "";
                                var tip = "<b>MutSig</b><br/>Q-value: "+mutsig.toPrecision(2);
                                var width = Math.ceil(3*Math.min(10,-Math.log(mutsig)*Math.LOG10E));
                                return  "<div class='mutation_percent_div "+table_id
                                                +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                            } else if (type==='sort') {
                                var mutsig = mutations.getValue(source[0], 'mutsig');
                                if (mutsig==null) return 1.0;
                                return mutsig;
                            } else if (type==='filter') {
                                return "mutsig";
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return mutations.getValue(source[0], 'mutsig');
                            }
                        }
                    },
                    {// sanger
                        "aTargets": [ mutTableIndices["sanger"] ],
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var sanger = mutations.getValue(source[0], 'sanger');
                                if (!sanger) return '';
                                return "<img src='images/sanger.png' width=12 height=12 class='"+table_id
                                        +"-tip' alt='In <a href=\"http://cancer.sanger.ac.uk/cosmic/gene/overview?ln="
                                        +mutations.getValue(source[0], 'gene')+"\">Sanger Cancer Gene Census</a>'/>";
                            } else if (type==='sort') {
                                var sanger = mutations.getValue(source[0], 'sanger');
                                return sanger?'1':'0';
                            }  else if (type==='filter') {
                                var sanger = mutations.getValue(source[0], 'sanger');
                                return sanger?'sanger':'';
                            } else {
                                return mutations.getValue(source[0], 'sanger');
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// drugs
                        "aTargets": [ mutTableIndices["drug"] ],
                        "sClass": "center-align-td",
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var drug = mutations.getValue(source[0], 'drug');
                                if (!drug) return '';
                                var len = drug.length;
                                if (len==0) return '';
                                return "<img src='images/drug.png' width=12 height=12 id='"
                                            +table_id+'_'+source[0]+"-drug-tip' class='"
                                            +table_id+"-drug-tip' alt='"+drug.join(',')+"'>";
                            } else if (type==='sort') {
                                var drug = mutations.getValue(source[0], 'drug');
                                return drug ? drug.length : 0;
                            } else if (type==='filter') {
                                var drug = mutations.getValue(source[0], 'drug');
                                return drug&&drug.length ? 'drugs' : '';
                            } else if (type==='type') {
                                return 0;
                            } else {
                                var drug = mutations.getValue(source[0], 'drug');
                                return drug ? drug : '';
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// aa change
                        "aTargets": [ mutTableIndices["aa"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var aa = mutations.getValue(source[0], 'aa');
                                if (aa.length>2&&aa.substring(0,2)=='p.')
                                    aa = aa.substring(2);
                                var ret = "<b><i>"+aa+"</i></b>";
                                if (mutations.getValue(source[0],'status')==="Germline")
                                    ret += "&nbsp;<span style='background-color:red;' class='"
                                            +table_id+"-tip' alt='Germline mutation'>G</span>"
                                return ret;
                            } else {
                                return mutations.getValue(source[0], 'aa');
                            }
                        },
                        "bSortable" : false
                    },
                    {// type
                        "aTargets": [ mutTableIndices["type"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var mutType = mutations.getValue(source[0], "type");
                                var abbr, color;
                                if (mutType==='Missense_Mutation') {
                                    abbr = 'MS';
                                    color = 'green';
                                } else if (mutType==='Nonsense_Mutation') {
                                    abbr = 'NS';
                                    color = 'red';
                                } else if (mutType==='Splice_Site') {
                                    abbr = 'SP';
                                    color = 'red';
                                } else if (mutType==='In_Frame_Ins') {
                                    abbr = 'IFI';
                                    color = 'green';
                                } else if (mutType==='In_Frame_Del') {
                                    abbr = 'IFD';
                                    color = 'green';
                                } else if (mutType==='Frame_Shift_Del') {
                                    abbr = 'FSD';
                                    color = 'red';
                                } else if (mutType==='Frame_Shift_Ins') {
                                    abbr = 'FSI';
                                    color = 'red';
                                } else if (mutType==='RNA') {
                                    abbr = 'RNA';
                                    color = 'green';
                                } else if (mutType==='Nonstop_Mutation') {
                                    abbr = 'NST';
                                    color = 'red';
                                } else if (mutType==='Translation_Start_Site') {
                                    abbr = 'TSS';
                                    color = 'green';
                                } else {
                                    abbr = mutType;
                                    color = 'gray';
                                }
                                return "<span style='color:"+color+";' class='"
                                            +table_id+"-tip' alt='"+mutType+"'><b>"
                                            +abbr+"</b></span>";
                            } else {
                                return mutations.getValue(source[0], "type");
                            }
                        }
                        
                    },
                    {// key mutation rate
                        "aTargets": [ mutTableIndices["keymutrate"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                if (!mutations.colExists('keymutrate')) 
                                    return "<img height=12 width=12 src='images/ajax-loader2.gif'>";
                                var key = mutations.getValue(source[0], 'key');
                                if (key==null) return '';
                                var keyCon = mutations.getValue(source[0], 'keymutrate')-1;
                                if (keyCon<=0) return '';
                                var frac = keyCon/numPatientInSameMutationProfile;
                                var tip = "<b>"+keyCon+" other sample"+(keyCon==1?"":"s")
                                    +"</b> ("+(100*frac).toFixed(1) + "%)"+" in the cohort study ("
                                    +cancerStudyName+") "+(keyCon==1?"has ":"have ")+key+" mutations";
                                var width = Math.min(40, Math.ceil(80 * Math.log(frac+1) * Math.LOG2E)+3);
                                return "<div class='mutation_percent_div "+table_id
                                            +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                            } else if (type==='sort') {
                                if (!mutations.colExists('keymutrate')) return 0;
                                return mutations.getValue(source[0], 'keymutrate');
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                if (mutations['keymutrate']==null) return 0;
                                return mutations.getValue(source[0], 'keymutrate');
                            }
                        },
                        //"sWidth": "50px",
                        "asSorting": ["desc", "asc"]
                    },
                    {// cosmic
                        "aTargets": [ mutTableIndices["cosmic"] ],
                        "asSorting": ["desc", "asc"],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var cosmic = mutations.getValue(source[0], 'cosmic');
                                if (!cosmic) return "";
                                var arr = [];
                                var n = 0;
                                for(var aa in cosmic) {
                                    var c = cosmic[aa];
                                    arr.push("<td>"+aa+":</td><td>"+c+"</td>");
                                    n += c;
                                }
                                if (n==0) return "";
                                var tip = '<table><tr><td colspan=2><b>'+n+' occurrences in COSMIC</b></td></tr><tr>'+arr.join('</tr><tr>')+'</tr></table>';
                                var width = Math.ceil(10*Math.min(4,Math.log(n)*Math.LOG10E));
                                return  "<div class='mutation_percent_div "+table_id
                                                +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                            } else if (type==='sort') {
                                var cosmic = mutations.getValue(source[0], 'cosmic');
                                var n = 0;
                                if (cosmic)
                                    for(var aa in cosmic)
                                        n += cosmic[aa];
                                return n;
                            } else if (type==='type') {
                                return 0;
                            } else if (type==='filter') {
                                var cosmic = mutations.getValue(source[0], 'cosmic');
                                return !cosmic||cosmic.length==0?"":"cosmic";
                            } else {
                                return mutations.getValue(source[0], 'cosmic');
                            }
                        }
                    },
                    {
                        "aTargets": [ mutTableIndices["ma"] ],
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var ma = mutations.getValue(source[0], 'ma');
                                var score = ma['score'];
                                var bgColor,impact;
                                if (score==='N') {bgColor="white"; impact='Neutral';}
                                else if (score==='L') {bgColor="#E8E894"; impact='Low';}
                                else if (score==='M') {bgColor="#C79060"; impact='Medium';}
                                else if (score==='H') {bgColor="#C83C3C"; impact='High';}
                                else return '';
                                
                                var tip = "Predicted impact: <b>"+impact+"</b>";
                                var xvia = ma['xvia'];
                                if (xvia&&xvia!='NA')
                                    tip += "<br/><a href='>'"+xvia+"'><img height=15 width=19 src='images/ma.png'> Go to Mutation Assessor</a>";
                                var msa = ma['msa'];
                                if (msa&&msa!='NA')
                                    tip += "<br/><a href='"+msa+"'><img src='images/msa.png'> View Multiple Sequence Alignment</a>";
                                var pdb = ma['pdb'];
                                if (pdb&&pdb!='NA') 
                                    tip += "<br/><a href='"+pdb+"'><img src='images/pdb.png'> View Protein Structure</a>";
                                    
                                return "<span style='background-color:"+bgColor+";' class='"
                                            +table_id+"-tip' alt=\""+tip+"\">&nbsp;&nbsp;"+score+"&nbsp;&nbsp;</a>";
                            } else if (type==='sort') {
                                var ma = mutations.getValue(source[0], 'ma');
                                var score = ma['score'];
                                if (score==='N') return '0';
                                else if (score==='L') return '1';
                                else if (score==='M') return '2';
                                else if (score==='H') return '3';
                                else return '-1';
                            } else {
                                return mutations.getValue(source[0], 'ma');
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    }
//                    {// note 
//                        "bVisible": isSummary,
//                        "aTargets": [ mutTableIndices["note"] ],
//                        "mDataProp": function(source,type,value) {
//                            if (!isSummary) return "";
//                            if (type==='set') {
//                                source[mut_source_header_indices["note"]]=value;
//                            } else if (type==='display') {
//                                var notes = [];
//                                if (source[mut_source_header_indices["cosmic"]])
//                                    notes.push(formatCosmic(source[mut_source_header_indices["cosmic"]],table_id,true));
//                                if (source[mut_source_header_indices["mutsig"]]!=null)
//                                    notes.push("<img src='images/mutsig.png' width=15 height=15 class='"+table_id
//                                                +"-tip' alt='<b>MutSig</b><br/>Q-value: "+source[mut_source_header_indices["mutsig"]].toPrecision(2)+"'/>");
//                                if (source[mut_source_header_indices["sanger"]])
//                                    notes.push("<img src='images/sanger.png' width=15 height=15 class='"+table_id
//                                                +"-tip' alt='In <a href=\"http://cancer.sanger.ac.uk/cosmic/gene/overview?ln="
//                                                +source[mut_source_header_indices["gene"]]+"\">Sanger Cancer Gene Census</a>'/>");
//                                var drug = source[mut_source_header_indices["drug"]];
//                                if (drug && drug.length) {
//                                    notes.push("<img src='images/drug.png' width=15 height=15 id='"+table_id+'_'
//                                                +source[mut_source_header_indices["id"]]+"-drug-note-tip' class='"
//                                                +table_id+"-drug-tip' alt='"+drug.join(',')+"'/>");
//                                }
//                                if (source[mut_source_header_indices["status"]]==="Germline")
//                                    notes.push("<a class='"+table_id+"-tip' href='#' alt='Germline mutation'>G</a>");
//                                return notes.join("&nbsp;");
//                            } else if (type==='sort') {
//                                return "";
//                            } else {
//                                if (!source[mut_source_header_indices["note"]]) return '';
//                                return source[mut_source_header_indices["note"]];
//                            }
//                        },
//                        "bSortable" : false
//                    }
                ],
                "fnDrawCallback": function( oSettings ) {
                    addNoteTooltip("."+table_id+"-tip");
                    addDrugsTooltip("."+table_id+"-drug-tip");
                },
                "aaSorting": [[mutTableIndices["cosmic"],'desc'],[mutTableIndices["mutsig"],'asc'],[mutTableIndices["drug"],'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page"
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        oTable.css("width","100%");
        addNoteTooltip("#"+table_id+" th.mut-header");
        return oTable;
    }
    
    function formatCosmic(cosmic,table_id,img) {
        if (!cosmic) return "";
        var arr = [];
        var n = 0;
        for(var aa in cosmic) {
            var c = cosmic[aa];
            arr.push("<td>"+aa+":</td><td>"+c+"</td>");
            n += c;
        }
        if (n==0) return "";
        var alt = '<table><tr><td colspan=2><b>COSMIC</b></td></tr><tr>'+arr.join('</tr><tr>')+'</tr></table>'
        if (img)
            return "<img src='images/cosmic.gif' width=12 height=12 class='"+table_id+"-tip' alt='"+alt+"'/>"
        else
            return "<a class='"+table_id+"-tip' onclick='return false;' href='#' alt='"+alt+"'>"+n+"</a>"
    }
    
    var numPatientInSameMutationProfile = <%=numPatientInSameMutationProfile%>;
    
    function updateMutationContext(oTable) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            oTable.fnUpdate(true, row, mutTableIndices["genemutrate"], false, false);
            oTable.fnUpdate(true, row, mutTableIndices["keymutrate"], false, false);
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    function loadMutationContextData(mutations, mut_table, mut_summary_table) {
        var params = {
            <%=MutationsJSON.CMD%>:'<%=MutationsJSON.GET_CONTEXT_CMD%>',
            <%=PatientView.MUTATION_PROFILE%>:mutationProfileId,
            <%=MutationsJSON.MUTATION_EVENT_ID%>:genomicEventObs.mutations.getEventIds(false).join(',')
        };
        
        $.post("mutations.json", 
            params,
            function(context){
                genomicEventObs.mutations.addDataMap('genemutrate',context['<%=MutationsJSON.GENE_CONTEXT%>'],'gene');
                genomicEventObs.mutations.addDataMap('keymutrate',context['<%=MutationsJSON.KEYWORD_CONTEXT%>'],'key');
                updateMutationContext(mut_table);
                updateMutationContext(mut_summary_table);
            }
            ,"json"
        );
    }
    
    $(document).ready(function(){
        $('#mutation_id_filter_msg').hide();
        $('#mutation_wrapper_table').hide();
        var params = {
            <%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.MUTATION_PROFILE%>:mutationProfileId
        };
                        
        $.post("mutations.json", 
            params,
            function(data){
                genomicEventObs.mutations.setData(data);
                genomicEventObs.fire('mutations-built');
                
                // summary table
                var mut_summary_table = buildMutationsDataTable(genomicEventObs.mutations,genomicEventObs.mutations.getEventIds(true), 'mutation_summary_table', 
                            '<"H"<"mutation-summary-table-name">fr>t<"F"<"mutation-show-more"><"datatable-paging"pil>>', 25);
                $('.mutation-show-more').html("<a href='#mutations' id='switch-to-mutations-tab' title='Show more mutations of this patient'>Show all "
                    +genomicEventObs.mutations.getNumEvents(false)+" mutations</a>");
                $('#mutation-show-more-link').click(function () {
                    switchToTab('mutations');
                    return false;
                });
                $('.mutation-summary-table-name').html(
                    "Mutations <img class='mutations_help' src='images/help.png' \n\
                        title='This table contains genes that are either \n\
                        recurrently mutated (MutSig Q-value<0.05) \n\
                        or with 5 or more COSMIC overlapping mutations\n\
                        or in the Sanger Cancer Gene Census.'/>");
                $('#mutation_summary_wrapper_table').show();
                $('#mutation_summary_wait').remove();
                
                // mutations
                var mut_table = buildMutationsDataTable(genomicEventObs.mutations,genomicEventObs.mutations.getEventIds(false),
                    'mutation_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                $('#mutation_wrapper_table').show();
                $('#mutation_wait').remove();

                // help
                $('.mutations_help').tipTip();
                
                loadMutationContextData(genomicEventObs.mutations, mut_table, mut_summary_table);
            }
            ,"json"
        );
    });
    
    function getMutGeneAA(mutIds) {
        var m = [];
        for (var i=0; i<mutIds.length; i++) {
            var gene = genomicEventObs.mutations.getValue(mutIds[i],'gene');
            var aa = genomicEventObs.mutations.getValue(mutIds[i],'aa');
            m.push(gene+': '+aa);
        }
        return m;
    }
    
    function filterMutationsTableByIds(mutIdsRegEx) {
        var mut_table = $('#mutation_table').dataTable();
        var n = mut_table.fnSettings().fnRecordsDisplay();
        mut_table.fnFilter(mutIdsRegEx, mutTableIndices["id"],true);
        if (n!=mut_table.fnSettings().fnRecordsDisplay())
            $('#mutation_id_filter_msg').show();
    }
    
    function unfilterMutationsTableByIds() {
        var mut_table = $('#mutation_table').dataTable();
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