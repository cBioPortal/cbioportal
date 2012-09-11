<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.cgds.dao.DaoMutSig" %>

<style type="text/css" title="currentStyle">
        .mutation-summary-table-name {
                float: left;
                font-weight: bold;
                font-size: 120%;
        }
        .mutation-show-more {
            float: left;
        }
</style>

<script type="text/javascript">
    var mutTableIndices = {id:0,key:1,chr:2,start:3,end:4,gene:5,aa:6,type:7,status:8,
        cosmic:9,mutsig:10,sanger:11,drug:12,ma:13,overview:14,mutrate:15,note:16};
    function buildMutationsDataTable(mutations, table_id, isSummary, sDom, iDisplayLength) {
        var oTable = $("#"+table_id).dataTable( {
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
                        "aTargets": [ mutTableIndices["key"] ]
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
                                if (value!=null)
                                    source[mutTableIndices["gene"]]=value;
                            } else if (type==='display') {
                                var gene = source[mutTableIndices["gene"]];
                                var ret = "<b>"+gene+"</b>";
                                if (isSummary) {
                                    if (mutGeneContext) {
                                        var geneCon = mutGeneContext[gene];
                                        var frac = geneCon/numPatientInSameMutationProfile;
                                        var tip = geneCon+" sample"+(geneCon==1?"":"s")
                                            +" (<b>"+(100*frac).toFixed(1) + "%</b>)"+" in "
                                            +cancerStudyName+(geneCon==1?" has":" have")+" mutated "+gene;
                                        var width = Math.ceil(40 * Math.log(frac+1) * Math.LOG2E)+3;
                                        ret += "&nbsp;<div class='altered_percent_div "+table_id
                                                    +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                                    } else {
                                        ret += "&nbsp;<img style='display:block;float:right;' src='images/ajax-loader2.gif'>";
                                    }
                                }
                                
                                return ret;
                            } else {
                                return source[mutTableIndices["gene"]];
                            }
                        }
                    },
                    {// aa change
                        "aTargets": [ mutTableIndices["aa"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                if (value!=null)
                                    source[mutTableIndices["aa"]]=value;
                            } else if (type==='display') {
                                var aa = source[mutTableIndices["aa"]];
                                var ret = "<b><i>"+aa+"</i></b>";
                                if (isSummary) {
                                    if (mutKeyContext) {
                                        var key = source[mutTableIndices["key"]];
                                        if (key) {
                                            var keyCon = mutKeyContext[key];
                                            var frac = keyCon / numPatientInSameMutationProfile;
                                            var tip = keyCon+" sample"+(keyCon==1?"":"s")
                                                +" (<b>"+(100*frac).toFixed(1) + "%</b>)"+" in "
                                                +cancerStudyName+(keyCon==1?" has ":" have ")+key+" mutations";
                                            var width = Math.ceil(40 * Math.log(frac+1) * Math.LOG2E)+3;
                                            ret += "&nbsp;<div class='altered_percent_div "+table_id
                                                        +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                                        }
                                    } else {
                                        ret += "&nbsp;<img style='display:block;float:right;' src='images/ajax-loader2.gif'>";
                                    }
                                }
                                return ret;
                            } else {
                                return source[mutTableIndices["aa"]];
                            }
                        }
                    },
                    {// end
                        "bVisible": true,
                        "aTargets": [ mutTableIndices["type"] ]
                    },
                    {// end
                        "bVisible": !isSummary,
                        "aTargets": [ mutTableIndices["status"] ]
                    },
                    {// cosmic
                        "bVisible": !isSummary,
                        "aTargets": [ mutTableIndices["cosmic"] ],
                        "asSorting": ["desc", "asc"],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["cosmic"]]=value;
                            } else if (type==='display') {
                                var cosmic = source[mutTableIndices["cosmic"]];
                                return formatCosmic(cosmic,table_id);
                            } else if (type==='sort') {
                                var cosmic = source[mutTableIndices["cosmic"]];
                                var n = 0;
                                if (cosmic)
                                    for(var aa in cosmic)
                                        n += cosmic[aa];
                                var ret = '000000000'+n;
                                return ret.substring(ret.length-10,ret.length);
                            }  else if (type==='filter') {
                                return "cosmic";
                            } else {
                                return source[mutTableIndices["cosmic"]];
                            }
                        }
                    },
                    {// mutsig
                        "bVisible": !isSummary,
                        "aTargets": [ mutTableIndices["mutsig"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["mutsig"]]=value;
                            } else if (type==='display') {
                                var mutsig = source[mutTableIndices["mutsig"]];
                                if (mutsig==null) return "";
                                return mutsig.toPrecision(2);
                            } else if (type==='sort') {
                                var mutsig = source[mutTableIndices["mutsig"]];
                                if (mutsig==null) return 1.0;
                                return mutsig;
                            }  else if (type==='filter') {
                                return "mutsig";
                            } else {
                                return source[mutTableIndices["mutsig"]];
                            }
                        }
                    },
                    {// sanger
                        "bVisible": !isSummary,
                        "aTargets": [ mutTableIndices["sanger"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["sanger"]]=value;
                            } else if (type==='display') {
                                var sanger = source[mutTableIndices["sanger"]];
                                return sanger?'&#10004;':'';
                            } else if (type==='sort') {
                                var sanger = source[mutTableIndices["sanger"]];
                                return sanger?'1':'0';
                            }  else if (type==='filter') {
                                var sanger = source[mutTableIndices["sanger"]];
                                return sanger?'sanger':'';
                            } else {
                                return source[mutTableIndices["sanger"]];
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {
                        "aTargets": [ mutTableIndices["ma"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["ma"]]=value;
                            } else if (type==='display') {
                                var ma = source[mutTableIndices["ma"]];
                                var score = ma['score'];
                                var bgColor,impact;
                                if (score==='N') {bgColor="white"; impact='Neutral';}
                                else if (score==='L') {bgColor="#E8E894"; impact='Low';}
                                else if (score==='M') {bgColor="#C79060"; impact='Medium';}
                                else if (score==='H') {bgColor="#C83C3C"; impact='High';}
                                else return '';
                                var ret = "<a style='background-color:"+bgColor+";' href='"+ma['xvia']+"' title='"+impact+" impact'>&nbsp;&nbsp;"+score+"&nbsp;&nbsp;</a>";
                                var msa = ma['msa'];
                                if (msa&&msa!='NA') ret += "&nbsp;<a href='"+msa+"' style='background-image:url(images/msa.png);'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>";
                                var pdb = ma['pdb'];
                                if (pdb&&pdb!='NA') ret += "&nbsp;<a href='"+pdb+"' style='width: 23px;background-image:url(images/pdb.png);'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</a>";
                                return ret;
                            } else if (type==='sort') {
                                var ma = source[mutTableIndices["ma"]];
                                var score = ma['score'];
                                if (score==='L') return '1';
                                else if (score==='M') return '2';
                                else if (score==='H') return '3';
                                else return '0';
                            } else {
                                return source[mutTableIndices["ma"]];
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// in overview
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["overview"] ]
                    },
                    {// mutation rate
                        "bVisible": !isSummary,
                        "mDataProp": 
                            function(source,type,value) {
                            if (isSummary) {
                                return '';
                            }
                            if (type==='set') {
                                source[mutTableIndices["mutrate"]]=value;
                            } else if (type==='display') {
                                if (!source[mutTableIndices["mutrate"]]) return "<img src=\"images/ajax-loader2.gif\">";
                                var gene = source[mutTableIndices["gene"]];
                                var geneCon = mutGeneContext[gene];
                                var genePerc = 100.0 * geneCon / numPatientInSameMutationProfile;
                                var ret = gene + ": " + geneCon + " (<b>" + genePerc.toFixed(1) + "%</b>)";
                                
                                var key = source[mutTableIndices["key"]];
                                if (key) {
                                    var keyCon = mutKeyContext[key];
                                    var keyPerc = 100.0 * keyCon / numPatientInSameMutationProfile;
                                    ret += "<br/>" + key + ": " + keyCon + " (<b>" + keyPerc.toFixed(1) + "%</b>)";
                                }
                                
                                var eventId = source[mutTableIndices["id"]];
                                var aa = source[mutTableIndices["aa"]];
                                var mutCon = mutAAContext[eventId];
                                var mutPerc = 100.0 * mutCon / numPatientInSameMutationProfile;
                                ret += "<br/>" + gene + " <i>" + aa + "</i>: " + mutCon + " (<b>" + mutPerc.toFixed(1) + "%</b>)";
                                
                                return ret;
                            } else if (type==='sort') {
                                if (!source[mutTableIndices["mutrate"]]) return 0;
                                var gene = source[mutTableIndices["gene"]];
                                var geneCon = '000000000'+mutGeneContext[gene];
                                return geneCon.substring(geneCon.length-10,geneCon.length);
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"],
                        "aTargets": [ mutTableIndices["mutrate"] ]
                    },
                    {// drugs
                        "bVisible": !isSummary,
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                source[mutTableIndices["drug"]]=value;
                            } else if (type==='display') {
                                var drug = source[mutTableIndices["drug"]];
                                if (!drug) return '';
                                var len = drug.length;
                                if (len==0) return '';
                                return "<a href='#' onclick='return false;' id='"
                                            +table_id+'_'+source[mutTableIndices["id"]]
                                            +"-drug-tip' class='"+table_id+"-drug-tip' alt='"+drug.join(',')+"'>"
                                            +len+" drug"+(len>1?"s":"")+"</a>";
                            } else if (type==='sort') {
                                var drug = source[mutTableIndices["drug"]];
                                var n = ''+(drug ? drug.length : 0);
                                var pad = '000000';
                                return pad.substring(0, pad.length - n.length) + n;
                            } else if (type==='filter') {
                                var drug = source[mutTableIndices["drug"]];
                                return drug&&drug.length ? 'drugs' : '';
                            } else {
                                var drug = source[mutTableIndices["drug"]];
                                return drug ? drug : '';
                            }
                        },
                        "asSorting": ["desc", "asc"],
                        "aTargets": [ mutTableIndices["drug"] ]
                    },
                    {// note 
                        "bVisible": isSummary,
                        "aTargets": [ mutTableIndices["note"] ],
                        "mDataProp": function(source,type,value) {
                            if (!isSummary) return "";
                            if (type==='set') {
                                source[mutTableIndices["note"]]=value;
                            } else if (type==='display') {
                                var notes = [];
                                if (source[mutTableIndices["cosmic"]])
                                    notes.push(formatCosmic(source[mutTableIndices["cosmic"]],table_id,true));
                                if (source[mutTableIndices["mutsig"]]!=null)
                                    notes.push("<img src='images/mutsig.png' width=15 height=15 class='"+table_id
                                                +"-tip' alt='<b>MutSig</b><br/>Q-value: "+source[mutTableIndices["mutsig"]].toPrecision(2)+"'/>");
                                if (source[mutTableIndices["sanger"]])
                                    notes.push("<img src='images/sanger.png' width=15 height=15 class='"+table_id
                                                +"-tip' alt='In Sanger Cancer Gene Census'/>");
                                var drug = source[mutTableIndices["drug"]];
                                if (drug && drug.length) {
                                    notes.push("<img src='images/drug.png' width=15 height=15 id='"+table_id+'_'
                                                +source[mutTableIndices["id"]]+"-drug-note-tip' class='"
                                                +table_id+"-drug-tip' alt='"+drug.join(',')+"'/>");
                                }
                                if (source[mutTableIndices["status"]]==="Germline")
                                    notes.push("<a class='"+table_id+"-tip' href='#' alt='Germline mutation'>G</a>");
                                return notes.join("&nbsp;");
                            } else if (type==='sort') {
                                return "";
                            } else {
                                if (!source[mutTableIndices["note"]]) return '';
                                return source[mutTableIndices["note"]];
                            }
                        },
                        "bSortable" : false
                    }
                ],
                "fnDrawCallback": function( oSettings ) {
                    addNoteTooltip("."+table_id+"-tip");
                    addDrugsTooltip("."+table_id+"-drug-tip");
                },
                "aaSorting": [[mutTableIndices["cosmic"],'desc'],[mutTableIndices["mutsig"],'asc'],[mutTableIndices["mutrate"],'desc'],[mutTableIndices["drug"],'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page"
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        oTable.css("width","100%");
        
        if (isSummary) addMutNoteSortingMenu(table_id);
        
        return oTable;
    }
    
    function addMutNoteSortingMenu(table_id) {
        $("#"+table_id+" th:last-child").qtip({
            content: {
                text: "<a href='#' onclick='sortNoteMutTable(\""+table_id+"\",\"cosmic\",\"desc\");return false;'>Sort by COSMIC frequency</a><br/>\n\
                       <a href='#' onclick='sortNoteMutTable(\""+table_id+"\",\"mutsig\",\"asc\");return false;'>Sort by MutSig Q-value</a><br/>\n\
                       <a href='#' onclick='sortNoteMutTable(\""+table_id+"\",\"drug\",\"desc\");return false;'>Sort by available potential Drugs</a><br/>\n\
                       <a href='#' onclick='sortNoteMutTable(\""+table_id+"\",\"sanger\",\"desc\");return false;'>Sort by Sanger Cancer Gene Census</a>"
            },
            hide: { fixed: true, delay: 200 },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded' },
            position: {my:'top middle',at:'bottom middle'}
        });
        
    }
    
    function sortNoteMutTable(table_id,colLabel,direction) {
        $("#"+table_id).dataTable().fnSort([[mutTableIndices[colLabel],direction]]);
        $("#"+table_id+" th:last-child").qtip("hide");
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
            return "<img src='images/cosmic.gif' width=15 height=15 class='"+table_id+"-tip' alt='"+alt+"'/>"
        else
            return "<a class='"+table_id+"-tip' onclick='return false;' href='#' alt='"+alt+"'>"+n+" occurrence"+(n==1?"":"s")+"</a>"
    }
    
    var numPatientInSameMutationProfile = <%=numPatientInSameMutationProfile%>;
    
    function updateMutationContext(oTable, summaryOnly) {
        var nRows = oTable.fnSettings().fnRecordsTotal();
        for (var row=0; row<nRows; row++) {
            if (summaryOnly && !oTable.fnGetData(row, mutTableIndices["overview"])) continue;
            if (!summaryOnly||print) {
                oTable.fnUpdate(true, row, mutTableIndices["mutrate"], false, false);
            } else {
                oTable.fnUpdate(null, row, mutTableIndices["gene"], false, false);
                oTable.fnUpdate(null, row, mutTableIndices["aa"], false, false);
            }
        }
        oTable.fnDraw();
        oTable.css("width","100%");
    }
    
    var mutGeneContext = null;
    var mutAAContext = null;
    var mutKeyContext = null;
    function loadMutationContextData(mutations, mut_table, mut_summary_table) {
        var params = {
            <%=MutationsJSON.CMD%>:'<%=MutationsJSON.GET_CONTEXT_CMD%>',
            <%=PatientView.MUTATION_PROFILE%>:mutationProfileId,
            <%=MutationsJSON.MUTATION_EVENT_ID%>:mutEventIds
        };
        
        $.post("mutations.json", 
            params,
            function(context){
                mutAAContext = context['<%=MutationsJSON.MUTATION_CONTEXT%>'];
                mutGeneContext =  context['<%=MutationsJSON.GENE_CONTEXT%>'];
                mutKeyContext =  context['<%=MutationsJSON.KEYWORD_CONTEXT%>'];
                updateMutationContext(mut_table, false);
                updateMutationContext(mut_summary_table, true);
            }
            ,"json"
        );
    }
    
    var mut_table;
    var mut_summary_table;
    var mutations;
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
                mutations = data;
                // mutations
                mut_table = buildMutationsDataTable(mutations, 'mutation_table', false, '<"H"fr>t<"F"<"datatable-paging"pil>>', 100);
                $('#mutation_wrapper_table').show();
                $('#mutation_wait').remove();
                
                mutEventIds = getEventString(mutations,mutTableIndices["id"]);
                overviewMutEventIds = getEventString(mutations,mutTableIndices["id"],mutTableIndices["overview"]);
                overviewMutGenes = getEventString(mutations,mutTableIndices["gene"],mutTableIndices["overview"]);
                mutEventIndexMap = getEventIndexMap(mutations,mutTableIndices["id"]);
                
                geObs.fire('mutations-built');
                
                // summary table
                mut_summary_table = buildMutationsDataTable(mutations, 'mutation_summary_table', print?false:true,
                            '<"H"<"mutation-summary-table-name">fr>t<"F"<"mutation-show-more"><"datatable-paging"pil>>', print?-1:10);
                $('.mutation-show-more').html("<a href='#mutations' id='switch-to-mutations-tab' title='Show more mutations of this patient'>Show all "
                    +mutations.length+" mutations</a>");
                $('#switch-to-mutations-tab').click(function () {
                    switchToTab('mutations');
                    return false;
                });
                mut_summary_table.fnFilter('true', mutTableIndices["overview"]);
                $('.mutation-summary-table-name').html(mut_summary_table.fnSettings().fnRecordsDisplay()
                    +" mutations of Interest (out of "+mutations.length+" mutations)"
                    +" <img class='mutations_help' src='images/help.png'"
                    +" title='This table contains genes that are either"
                    +" recurrently mutated (MutSig Q-value<0.05)"
                    +" or with 5 or more COSMIC overlapping mutations"
                    +" or in the Sanger Cancer Gene Census.'/>");
                $('#mutation_summary_wrapper_table').show();
                $('#mutation_summary_wait').remove();

                // help
                $('.mutations_help').tipTip();
                
                loadMutationContextData(mutations, mut_table, mut_summary_table);
            }
            ,"json"
        );
    });
    
    function getMutGeneAA(mutIds) {
        var m = [];
        for (var i=0; i<mutIds.length; i++) {
            var row = mutEventIndexMap[mutIds[i]];
            var gene = mutations[row][mutTableIndices["gene"]];
            var aa = mutations[row][mutTableIndices["aa"]];
            m.push(gene+': '+aa);
        }
        return m;
    }
    
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