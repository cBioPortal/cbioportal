<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.cgds.dao.DaoMutSig" %>

<script type="text/javascript">
    var mutTableIndices = {id:0,gene:1,aa:2,chr:3,start:4,end:5,validation:6,type:7,tumor_freq:8,norm_freq:9,altrate:10,cosmic:11,ma:12,'3d':13,drug:14};
    function buildMutationsDataTable(mutations,mutEventIds, table_id, sDom, iDisplayLength, sEmptyInfo, compact) {
        var data = [];
        for (var i=0, nEvents=mutEventIds.length; i<nEvents; i++) {
                data.push([mutEventIds[i]]);
        }
        var oTable = $("#"+table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "oColVis": { "aiExclude": [ mutTableIndices["id"] ] }, // always hide id column
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
                                var gene = mutations.getValue(source[0], "gene");
                                var entrez = mutations.getValue(source[0], "entrez");
                                var tip = "<a href=\"http://www.ncbi.nlm.nih.gov/gene/"
                                    +entrez+"\">NCBI Gene</a>";
                                var sanger = mutations.getValue(source[0], 'sanger');
                                if (sanger) {
                                    tip += "<br/><a href=\"http://cancer.sanger.ac.uk/cosmic/gene/overview?ln="
                                        +gene+"\">Sanger Cancer Gene Census</a>";
                                }
                                var ret = "<b>"+gene+"</b>";
                                if (tip) {
                                    ret = "<span class='"+table_id+"-tip' alt='"+tip+"'>"+ret+"</span>";
                                }
                                return ret;
                            } else {
                                return mutations.getValue(source[0], "gene");
                            }
                        }
                    },
                    {// aa change
                        "aTargets": [ mutTableIndices["aa"] ],
                        "sClass": "no-wrap-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var aa = mutations.getValue(source[0], 'aa');
                                if (aa.length>2&&aa.substring(0,2)=='p.')
                                    aa = aa.substring(2);
                                var ret = "<b><i>"+aa+"</i></b>";
                                if (mutations.getValue(source[0],'status')==="Germline")
                                    ret += "&nbsp;<span style='background-color:red;font-size:x-small;' class='"
                                            +table_id+"-tip' alt='Germline mutation'>Germline</span>";
                                return ret;
                            } else {
                                return mutations.getValue(source[0], 'aa');
                            }
                        },
                        "bSortable" : false
                    },
                    {// chr
                        "aTargets": [ mutTableIndices["chr"] ],
                        "bVisible": false,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return mutations.getValue(source[0], 'chr');
                            } else {
                                return mutations.getValue(source[0], 'chr');
                            }
                        }
                    },
                    {// start
                        "aTargets": [ mutTableIndices["start"] ],
                        "bVisible": false,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return mutations.getValue(source[0], 'start');
                            } else {
                                return mutations.getValue(source[0], 'start');
                            }
                        },
                        "bSortable" : false
                    },
                    {// end
                        "aTargets": [ mutTableIndices["end"] ],
                        "bVisible": false,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return mutations.getValue(source[0], 'end');
                            } else {
                                return mutations.getValue(source[0], 'end');
                            }
                        },
                        "bSortable" : false
                    },
                    {// validation
                        "bVisible": false,
                        "aTargets": [ mutTableIndices["validation"] ],
                        "sClass": "no-wrap-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else {
                                var val = mutations.getValue(source[0],'validation');
                                return val ? val : "";
                            }
                        }
                    },
                    {// type
                        "aTargets": [ mutTableIndices["type"] ],
                        "sClass": "center-align-td",
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
                                    color = 'black';
                                } else if (mutType==='In_Frame_Del') {
                                    abbr = 'IFD';
                                    color = 'black';
                                } else if (mutType==='Fusion') {
                                    abbr = 'Fusion';
                                    color = 'black';
                                } else if (mutType==='Frame_Shift_Del') {
                                    abbr = 'FS';
                                    color = 'red';
                                } else if (mutType==='Frame_Shift_Ins') {
                                    abbr = 'FS';
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
                            } else if (type==='filter') {
                                var mutType = mutations.getValue(source[0], "type");
                                if (mutType==='Missense_Mutation') {
                                    return 'MS';
                                } else if (mutType==='Nonsense_Mutation') {
                                    return 'NS';
                                } else if (mutType==='Splice_Site') {
                                    return 'SP';
                                } else if (mutType==='In_Frame_Ins') {
                                    return 'IFI';
                                } else if (mutType==='In_Frame_Del') {
                                    return 'IFD';
                                } else if (mutType==='Frame_Shift_Del') {
                                    return 'FS';
                                } else if (mutType==='Frame_Shift_Ins') {
                                    return 'FS';
                                } else if (mutType==='RNA') {
                                    return 'RNA';
                                } else if (mutType==='Nonstop_Mutation') {
                                    return 'NST';
                                } else if (mutType==='Translation_Start_Site') {
                                    return 'TSS';
                                } else {
                                    return mutType;
                                }
                            } else {
                                return mutations.getValue(source[0], "type");
                            }
                        }
                        
                    },
                    {// tumor read count frequency
                        "aTargets": [ mutTableIndices["tumor_freq"] ],
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var refCount = mutations.getValue(source[0], 'ref-count');
                                var altCount = mutations.getValue(source[0], 'alt-count');
                                if (refCount==null||altCount==null||refCount<0||altCount<0) return '';
                                if (!altCount&&!refCount) return '';
                                
                                var freq = altCount / (altCount + refCount);
                                var tip = altCount + " variant reads out of " + (refCount+altCount) + " total";
                                return "<span class='"+table_id+"-tip' alt='"+tip+"'>"+freq.toFixed(2)+"</span>"; 
                            } else if (type==='sort') {
                                var refCount = mutations.getValue(source[0], 'ref-count');
                                var altCount = mutations.getValue(source[0], 'alt-count');
                                if (!altCount&&!refCount) return 0;
                                return altCount / (altCount + refCount);
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return 0.0;
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// normal read count frequency
                        "aTargets": [ mutTableIndices["norm_freq"] ],
                        "bVisible": !compact,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var refCount = mutations.getValue(source[0], 'normal-ref-count');
                                var altCount = mutations.getValue(source[0], 'normal-alt-count');
                                if (refCount==null||altCount==null||refCount<0||altCount<0) return '';
                                if (!altCount&&!refCount) return '';
                                
                                var freq = altCount / (altCount + refCount);
                                var tip = altCount + " variant reads out of " + (refCount+altCount) + " total";
                                return "<span class='"+table_id+"-tip' alt='"+tip+"'>"+freq.toFixed(2)+"</span>"; 
                            } else if (type==='sort') {
                                var refCount = mutations.getValue(source[0], 'normal-ref-count');
                                var altCount = mutations.getValue(source[0], 'normal-alt-count');
                                if (!altCount&&!refCount) return 0;
                                return altCount / (altCount + refCount);
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return 0.0;
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// gene mutation rate
                        "aTargets": [ mutTableIndices["altrate"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                // gene context
                                var geneCon = mutations.getValue(source[0], 'genemutrate')-1;
                                if (geneCon<=0) return '';
                                
                                var ret = '';
                                
                                // keyword context
                                var keyDiv = '';
                                var keyTip = '.';
                                var key = mutations.getValue(source[0], 'key');
                                if (key!=null) {
                                    var keyCon = mutations.getValue(source[0], 'keymutrate')-1;
                                    if (keyCon>0){
                                        var keyFrac = keyCon/numPatientInSameMutationProfile;
                                        keyTip = ", out of which <b>"+keyCon
                                            +"</b> ("+(100*keyFrac).toFixed(1) + "%) "
                                            +(keyCon==1?"has ":"have ")+key+" mutations.";
                                        var keyWidth = Math.min(40, Math.ceil(80 * Math.log(keyFrac+1) * Math.LOG2E));
                                        keyDiv += "<div class='mutation_percent_div' style='width:"+keyWidth+"px;'></div>";
                                    }
                                }
                                
                                var geneFrac = geneCon/numPatientInSameMutationProfile;
                                var geneTip = "<b>"+geneCon+" other sample"+(geneCon==1?"":"s")
                                    +"</b> ("+(100*geneFrac).toFixed(1) + "%)"+" in this study "+(geneCon==1?"has":"have")+" mutated "
                                    +mutations.getValue(source[0], "gene")+keyTip;
                                var geneWidth = Math.min(40, Math.ceil(80 * Math.log(geneFrac+1) * Math.LOG2E));
                                ret += "<div class='gene_mutation_percent_div "+table_id
                                                +"-tip' style='width:"+geneWidth+"px;' alt='"+geneTip+"'>"+keyDiv+"</div>";
                                
                                // mutsig
                                var mutsig = mutations.getValue(source[0], 'mutsig');
                                if (mutsig) {
                                    var tip = "<b>MutSig</b><br/>Q-value: "+mutsig.toPrecision(2);
                                    ret += "<img class='right_float_div "+table_id+"-tip' alt='"
                                        +tip+"' src='images/mutsig.png' width=12 height=12>";
                                }
                                
                                return ret;   
                            } else if (type==='sort') {
                                return mutations.getValue(source[0], 'genemutrate');
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return mutations.getValue(source[0], 'genemutrate');
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// cosmic
                        "aTargets": [ mutTableIndices["cosmic"] ],
                        "sClass": "right-align-td",
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
                                    arr.push("<td>"+aa+"</td><td>"+c+"</td>");
                                    n += c;
                                }
                                if (n==0) return "";
                                var tip = '<b>'+n+' occurrences in COSMIC</b><br/><table class="'+table_id
                                    +'-cosmic-table"><thead><th>Mutation</th><th>Occurrence</th></thead><tbody><tr>'
                                    +arr.join('</tr><tr>')+'</tr></tbody></table>';
                                return  "<span class='"+table_id
                                                +"-cosmic-tip' alt='"+tip+"'>"+n+"</span>";
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
                    {
                        "aTargets": [ mutTableIndices["ma"] ],
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
                                
                                var ret = "";
                                if (impact) {
                                    var tip = "Predicted impact: <b>"+impact+"</b>";
                                    var xvia = ma['xvia'];
                                    if (xvia&&xvia!='NA') {
                                        if (xvia.indexOf('http://')!=0) xvia='http://'+xvia;
                                        tip += "<br/><a href='"+xvia+"'><img height=15 width=19 src='images/ma.png'> Go to Mutation Assessor</a>";
                                     }
                                    var msa = ma['msa'];
                                    if (msa&&msa!='NA') {
                                        if (msa.indexOf('http://')!=0) msa='http://'+msa;
                                        tip += "<br/><a href='"+msa+"'><img src='images/msa.png'> View Multiple Sequence Alignment</a>";
                                    }
                                    ret += "<span style='background-color:"+bgColor+";' class='"
                                                +table_id+"-tip' alt=\""+tip+"\">&nbsp;&nbsp;"+score+"&nbsp;&nbsp;</a></span>";
                                }
                                
                                return ret;
                            } else if (type==='sort') {
                                var ma = mutations.getValue(source[0], 'ma');
                                var score = ma['score'];
                                if (score==='N') return '0';
                                else if (score==='L') return '1';
                                else if (score==='M') return '2';
                                else if (score==='H') return '3';
                                else return '-1';
                            } else if (type==='filter') {
                                var ma = mutations.getValue(source[0], 'ma');
                                var score = ma['score'];
                                if (score==='N'||score==='L'||score==='M'||score==='H') return score;
                                else return '';
                            } else {
                                return mutations.getValue(source[0], 'ma');
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {
                        "aTargets": [ mutTableIndices["3d"] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var ma = mutations.getValue(source[0], 'ma');
                                
                                var ret = '';
                                var pdb = ma['pdb'];
                                if (pdb&&pdb!='NA') {
                                    if (pdb.indexOf('http://')!=0) pdb='http://'+pdb;
                                    ret += "&nbsp;<a class='"
                                            +table_id+"-tip' alt='Protein 3D structure' href='"+pdb
                                            +"'><span style='background-color:#88C;color:white;'>&nbsp;3D&nbsp;</span></a>";
                                }
                                
                                return ret;
                            } else if (type==='sort'||type==='filter') {
                                var ma = mutations.getValue(source[0], 'ma');
                                var pdb = ma['pdb'];
                                if (pdb&&pdb!='NA') return '3d';
                                else return '';
                            } else {
                                var ma = mutations.getValue(source[0], 'ma');
                                var pdb = ma['pdb'];
                                if (pdb&&pdb!='NA') return '';
                                else return pdb;
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    }
                ],
                "fnDrawCallback": function( oSettings ) {
                    addNoteTooltip("."+table_id+"-tip");
                    addDrugsTooltip("."+table_id+"-drug-tip", 'top right', 'bottom center');
                    addCosmicTooltip(table_id);
                },
                "aaSorting": [[mutTableIndices["cosmic"],'desc'],[mutTableIndices["altrate"],'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page",
                    "sEmptyTable": sEmptyInfo
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        oTable.css("width","100%");
        addNoteTooltip("#"+table_id+" th.mut-header");
        return oTable;
    }

    function addCosmicTooltip(table_id) {
        $("."+table_id+"-cosmic-tip").qtip({
            content: {
                attr: 'alt'
            },
            events: {
                render: function(event, api) {
                    $("."+table_id+"-cosmic-table").dataTable( {
                        "sDom": 't',
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "oLanguage": {
                            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                            "sInfoFiltered": "",
                            "sLengthMenu": "Show _MENU_ per page"
                        },
                        "aaSorting": [[1,'desc']],
                        "iDisplayLength": -1
                    } );
                }
            },
            hide: { fixed: true, delay: 100 },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded' },
            position: {my:'top right',at:'bottom center'}
        });
    }
    
    var numPatientInSameMutationProfile = <%=numPatientInSameMutationProfile%>;
    
    $(document).ready(function(){
        $('#mutation_id_filter_msg').hide();
        $('#mutation_wrapper_table').hide();
        var params = {
            <%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.MUTATION_PROFILE%>:mutationProfileId
        };
        
        if (drugType) {
            params['<%=PatientView.DRUG_TYPE%>'] = drugType;
        }
                        
        $.post("mutations.json", 
            params,
            function(data){
                determineOverviewMutations(data);
                genomicEventObs.mutations.setData(data);
                genomicEventObs.fire('mutations-built');
                
                // summary table
                buildMutationsDataTable(genomicEventObs.mutations,genomicEventObs.mutations.getEventIds(true), 'mutation_summary_table', 
                            '<"H"<"mutation-summary-table-name">fr>t<"F"<"mutation-show-more"><"datatable-paging"pl>>', 25, "No mutation events of interest", true);
                var numFiltered = genomicEventObs.mutations.getNumEvents(true);
                var numAll = genomicEventObs.mutations.getNumEvents(false);
                 $('.mutation-show-more').html("<a href='#mutations' onclick='switchToTab(\"mutations\");return false;'\n\
                      title='Show more mutations of this patient'>Show all "
                        +numAll+" mutations</a>");
                $('.mutation-show-more').addClass('datatable-show-more');
                $('.mutation-summary-table-name').html(
                    "Mutations of interest"
                     +(numAll==0?"":(" ("
                        +numFiltered
                        +" of <a href='#mutations' onclick='switchToTab(\"mutations\");return false;'\n\
                         title='Show more mutations of this patient'>"
                        +numAll
                        +"</a>)"))
                     +" <img id='mutations-summary-help' src='images/help.png' \n\
                        title='This table contains somatic mutations in genes that are \n\
                        <ul><li>either annotated cancer genes</li>\n\
                        <li>or recurrently mutated, namely\n\
                            <ul><li>MutSig Q < 0.05, if MutSig results are available</li>\n\
                            <li>otherwise, mutated in > 5% of samples in the study with &ge; 50 samples</li></ul> </li>\n\
                        <li>or with > 5 overlapping entries in COSMIC.</li></ul>'/>");
                $('#mutations-summary-help').qtip({
                    content: { attr: 'title' },
                    style: { classes: 'ui-tooltip-light ui-tooltip-rounded' },
                    position: { my:'top center',at:'bottom center' }
                });
                $('.mutation-summary-table-name').addClass("datatable-name");
                $('#mutation_summary_wrapper_table').show();
                $('#mutation_summary_wait').remove();
                
                // mutations
                buildMutationsDataTable(genomicEventObs.mutations,genomicEventObs.mutations.getEventIds(false),
                    'mutation_table', '<"H"<"all-mutation-table-name">fr>t<"F"C<"datatable-paging"pil>>', 100, "No mutation events", false);
                $('.all-mutation-table-name').html(
                    "This tumor has "+genomicEventObs.mutations.getNumEvents()+" nonsynonymous mutations");
                $('.all-mutation-table-name').addClass("datatable-name");
                $('#mutation_wrapper_table').show();
                $('#mutation_wait').remove();
            }
            ,"json"
        );
    });
    
    var patient_view_mutsig_qvalue_threhold = 0.05;
    var patient_view_genemutrate_threhold = 0.05;
    var patient_view_genemutrate_apply_cohort_count = 50;
    var patient_view_cosmic_threhold = 5;
    function determineOverviewMutations(data) {
        var overview = [];
        var len = data['id'].length;
        var impact = data['impact'];
        var mutsig = data['mutsig'];
        var mutrate = data['genemutrate'];
        var cosmic = data['cosmic'];
        
        var noMutsig = true;
        for (var i=0; i<len; i++) {
            if (mutsig[i]) {
                noMutsig = false;
                break;
            }
        }
        
        for (var i=0; i<len; i++) {
            if (impact[i]) {
                overview.push(true);
                continue;
            }
            
            if (noMutsig) {
                if (numPatientInSameMutationProfile>=patient_view_genemutrate_apply_cohort_count
                  && mutrate[i]/numPatientInSameMutationProfile>=patient_view_genemutrate_threhold) {
                    overview.push(true);
                    continue;
                }
            } else {
                if (mutsig[i]&&mutsig[i]<=patient_view_mutsig_qvalue_threhold) {
                    overview.push(true);
                    continue;
                }
            }
            
            var ncosmic = 0;
            if (cosmic[i]) {
                for(var aa in cosmic) {
                    ncosmic += cosmic[aa];
                    if (ncosmic>=patient_view_cosmic_threhold) {
                        break;
                    }
                }
                if (ncosmic>=patient_view_cosmic_threhold) {
                    overview.push(true);
                    continue;
                }
            }
            
            overview.push(false);
        }
        data['overview'] = overview;
    }
    
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