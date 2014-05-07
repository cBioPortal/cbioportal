<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoMutSig" %>

<script type="text/javascript" src="js/lib/igv_webstart.js"></script>

<script type="text/javascript" src="js/src/patient-view/PancanMutationHistogram.js"></script>
<link href="css/mutation/mutation_table.css" type="text/css" rel="stylesheet"/>

<script type="text/javascript">
    var mutTableIndices =
            ["id","case_ids","gene","aa","chr","start","end","ref","_var","validation","type",
             "tumor_freq","tumor_var_reads","tumor_ref_reads","norm_freq","norm_var_reads",
             "norm_ref_reads","bam","cna","mrna","altrate","pancan_mutations", "cosmic","ma","drug"];

    mutTableIndices = cbio.util.arrayToAssociatedArrayIndices(mutTableIndices);

    var drawPanCanThumbnails = function(oTable) {
        var num_rows = oTable.fnSettings().fnRecordsTotal();
        var pancan_column = mutTableIndices['pancan_mutations'];
        var dummy = false;      // doesn't matter
        // now that there's data, iterate throw the pancan column which checks whether there is pancan data or not
        for (var i = 0; i < num_rows; i+=1) {
            oTable.fnUpdate(dummy, i, pancan_column, false, false);
        }

        // batch bar chart and sparkline generation
        (function($thumbnails) {
            var createOneThumbNail = function(idx, thumbnail) {

                // qtip on each pancan mutations histogram thumbnail
                var $thumbnail = $(thumbnail);

                // if already drawn, then quit
                if($thumbnail.attr("data-pancan-done")) {
                    return;
                }

                var gene = $thumbnail.attr('gene');
                var keyword = $thumbnail.attr('keyword');

                // i want to use this once and not use it again until qtip time,
                // that's why this is duplicated
                var byKeywordData = genomicEventObs.pancan_mutation_frequencies[keyword];
                var byHugoData = genomicEventObs.pancan_mutation_frequencies[gene];

                // -- sparkline --

                var invisible_container = document.getElementById("pancan_mutations_histogram_container");
                PancanMutationHistogram(byKeywordData, byHugoData, window.cancer_study_meta_data, invisible_container, {sparkline: true});
                var content = invisible_container.innerHTML;
                $thumbnail.html(content);
                $(invisible_container).empty();     // N.B.

                $thumbnail.children('svg').qtip({
                    content: {text: 'pancancer mutation bar chart is broken'},
                    events: {
                        render: function(event, api) {
                            var gene = $thumbnail.attr('gene');
                            var keyword = $thumbnail.attr('keyword');
                            var byKeywordData = genomicEventObs.pancan_mutation_frequencies[keyword];
                            var byHugoData = genomicEventObs.pancan_mutation_frequencies[gene];
                            var invisible_container = document.getElementById("pancan_mutations_histogram_container");
                            var histogram = PancanMutationHistogram(byKeywordData, byHugoData, window.cancer_study_meta_data, invisible_container, {this_cancer_study: window.cancerStudyName});

                            var content = invisible_container.innerHTML;
                            api.set('content.text', content);

                            // correct the qtip width
                            var svg_width = $(invisible_container).find('svg').attr('width');
                            $(this).css('max-width', parseInt(svg_width));

                            var this_svg = $(this).find('svg')[0];
                            histogram.qtip(this_svg);

                            $(invisible_container).empty();     // N.B.
                        }
                    },
                    hide: { fixed: true, delay: 100 },
                    style: { classes: 'qtip-light qtip-rounded qtip-shadow', tip: true },
                    position: {my:'center right',at:'center left',viewport: $(window)}
                });

                $thumbnail.attr("data-pancan-done", true);
            };

            // Defer the function so that the table works much faster
            $thumbnails.each(function(idx, thumbnail) {
                _.defer(createOneThumbNail, idx, thumbnail);
            });
        })($(oTable).find('.pancan_mutations_histogram_thumbnail'));
    };

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
                    {// case_ids
                        "aTargets": [ mutTableIndices["case_ids"] ],
                        "sClass": "center-align-td",
                        "bVisible": caseIds.length>1,
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var samples = mutations.getValue(source[0], "caseIds");
                                var ret = [];
                                for (var i=0, n=caseIds.length; i<n; i++) {
                                    var caseId = caseIds[i];
                                    if ($.inArray(caseId,samples)>=0) {
                                        ret.push("<svg width='12' height='12' class='"
                                            +table_id+"-case-label' alt='"+caseId+"'></svg>");
                                    } else {
                                        ret.push("<svg width='12' height='12'></svg>");
                                    }
                                }
                                
                                return "<div>"+ret.join("&nbsp;")+"</div>";
                            } else if (type==='sort') {
                                var samples = mutations.getValue(source[0], "caseIds");
                                var ix = [];
                                samples.forEach(function(caseId){
                                    ix.push(caseMetaData.index[caseId]);
                                });
                                ix.sort();
                                var ret = 0;
                                for (var i=0; i<ix.length; i++) {
                                    ret += Math.pow(10,i)*ix[i];
                                }
                                return ret;
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return mutations.getValue(source[0], "caseIds");
                            }
                        },
                        "asSorting": ["desc", "asc"]
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
                    {// ref
                        "aTargets": [ mutTableIndices["ref"] ],
                        "bVisible": false,
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return mutations.getValue(source[0], 'ref');
                            } else {
                                return mutations.getValue(source[0], 'ref');
                            }
                        }
                    },
                    {// var
                        "aTargets": [ mutTableIndices["_var"] ],
                        "bVisible": false,
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return mutations.getValue(source[0], 'var');
                            } else {
                                return mutations.getValue(source[0], 'var');
                            }
                        }
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
                            } else if (type==='display'||type==='filter') {
                                var mutType = mutations.getValue(source[0], "type");
                                var abbr, color;
                                if (mutType==='Missense_Mutation'||mutType==='missense') {
                                    abbr = 'Missense';
                                    color = 'green';
                                } else if (mutType==='Nonsense_Mutation') {
                                    abbr = 'Nonsense';
                                    color = 'red';
                                } else if (mutType==='Splice_Site') {
                                    abbr = 'Splice Site';
                                    color = 'red';
                                } else if (mutType==='In_Frame_Ins') {
                                    abbr = 'Insertion';
                                    color = 'black';
                                } else if (mutType==='In_Frame_Del') {
                                    abbr = 'Deletion';
                                    color = 'black';
                                } else if (mutType==='Fusion') {
                                    abbr = 'Fusion';
                                    color = 'black';
                                } else if (mutType==='Frame_Shift_Del') {
                                    abbr = 'Frameshift';
                                    color = 'red';
                                } else if (mutType==='Frame_Shift_Ins') {
                                    abbr = 'Frameshift';
                                    color = 'red';
                                } else if (mutType==='RNA') {
                                    abbr = 'RNA';
                                    color = 'green';
                                } else if (mutType==='Nonstop_Mutation') {
                                    abbr = 'Nonstop';
                                    color = 'red';
                                } else if (mutType==='Translation_Start_Site') {
                                    abbr = 'Translation Start Site';
                                    color = 'green';
                                } else {
                                    abbr = mutType;
                                    color = 'gray';
                                }
                                
                                if (type==='filter') return abbr;
                                
                                return "<span style='color:"+color+";' class='"
                                            +table_id+"-tip' alt='"+mutType+"'><b>"
                                            +abbr+"</b></span>";
                            } else {
                                return mutations.getValue(source[0], "type");
                            }
                        }
                        
                    },
                    {// tumor read count frequency
                        "aTargets": [ mutTableIndices["tumor_freq"] ],
                        "bVisible": hasAlleleFrequencyData,
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var refCount = mutations.getValue(source[0], 'ref-count');
                                var altCount = mutations.getValue(source[0], 'alt-count');
                                if (caseIds.length===1) {
                                    var ac = altCount[caseIds[0]];
                                    var rc = refCount[caseIds[0]];
                                    if (!ac||!rc) return "";
                                    var freq = ac / (ac + rc);
                                    var tip = ac + " variant reads out of " + (rc+ac) + " total";
                                    return "<span class='"+table_id+"-tip' alt='"+tip+"'>"+freq.toFixed(2)+"</span>";
                                }
                                
                                if ($.isEmptyObject(refCount)||$.isEmptyObject(altCount))
                                    return "";
                                return "<div class='"+table_id+"-tumor-freq' alt='"+source[0]+"'></div>";
                            } else if (type==='sort') {
                                var refCount = mutations.getValue(source[0], 'ref-count')[caseIds[0]];
                                var altCount = mutations.getValue(source[0], 'alt-count')[caseIds[0]];
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
                    {// tumor read count frequency
                        "aTargets": [ mutTableIndices["tumor_var_reads"] ],
                        "bVisible": false,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var altCount = mutations.getValue(source[0], 'alt-count');
                                if (caseIds.length===1) return altCount[caseIds[0]]?altCount[caseIds[0]]:"";
                                
                                var arr = [];
                                for (var ac in altCount) {
                                    arr.push(ac+": "+altCount[ac].toFixed(2));
                                } 
                                return arr.join("<br/>")
                            } else if (type==='sort') {
                                var altCount = mutations.getValue(source[0], 'alt-count')[caseIds[0]];
                                if (!altCount) return 0;
                                return altCount;
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return 0.0;
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// tumor read count frequency
                        "aTargets": [ mutTableIndices["tumor_ref_reads"] ],
                        "bVisible": false,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var altCount = mutations.getValue(source[0], 'ref-count');
                                if (caseIds.length===1) return altCount[caseIds[0]]?altCount[caseIds[0]]:"";
                                
                                var arr = [];
                                for (var ac in altCount) {
                                    arr.push(ac+": "+altCount[ac].toFixed(2));
                                } 
                                return arr.join("<br/>")
                            } else if (type==='sort') {
                                var refCount = mutations.getValue(source[0], 'ref-count')[caseIds[0]];
                                if (!refCount) return 0;
                                return refCount;
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
                        "bVisible": !compact&&hasAlleleFrequencyData,
                        "sClass": caseIds.length>1 ? "center-align-td":"right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var refCount = mutations.getValue(source[0], 'normal-ref-count');
                                var altCount = mutations.getValue(source[0], 'normal-alt-count');
                                if (caseIds.length===1) {
                                    var ac = altCount[caseIds[0]];
                                    var rc = refCount[caseIds[0]];
                                    if (!ac&&!rc) return "";
                                    var freq = ac / (ac + rc);
                                    var tip = ac + " variant reads out of " + (rc+ac) + " total";
                                    return "<span class='"+table_id+"-tip' alt='"+tip+"'>"+freq.toFixed(2)+"</span>";
                                }
                                
                                if ($.isEmptyObject(refCount)||$.isEmptyObject(altCount))
                                    return "";
                                return "<div class='"+table_id+"-tumor-freq' alt='"+source[0]+"'></div>"; 
                            } else if (type==='sort') {
                                var refCount = mutations.getValue(source[0], 'normal-ref-count')[caseIds[0]];
                                var altCount = mutations.getValue(source[0], 'normal-alt-count')[caseIds[0]];
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
                    {// tumor read count frequency
                        "aTargets": [ mutTableIndices["norm_var_reads"] ],
                        "bVisible": false,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var altCount = mutations.getValue(source[0], 'normal-alt-count');
                                if (caseIds.length===1) return altCount[caseIds[0]]?altCount[caseIds[0]]:"";
                                
                                var arr = [];
                                for (var ac in altCount) {
                                    arr.push(ac+": "+altCount[ac].toFixed(2));
                                } 
                                return arr.join("<br/>")
                            } else if (type==='sort') {
                                var altCount = mutations.getValue(source[0], 'normal-alt-count')[caseIds[0]];
                                if (!altCount) return 0;
                                return altCount;
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return 0.0;
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// tumor read count frequency
                        "aTargets": [ mutTableIndices["norm_ref_reads"] ],
                        "bVisible": false,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var altCount = mutations.getValue(source[0], 'normal-ref-count');
                                if (caseIds.length===1) return altCount[caseIds[0]]?altCount[caseIds[0]]:"";
                                
                                var arr = [];
                                for (var ac in altCount) {
                                    arr.push(ac+": "+altCount[ac].toFixed(2));
                                } 
                                return arr.join("<br/>")
                            } else if (type==='sort') {
                                var refCount = mutations.getValue(source[0], 'normal-ref-count')[caseIds[0]];
                                if (!refCount) return 0;
                                return refCount;
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return 0.0;
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// tumor read count frequency
                        "aTargets": [ mutTableIndices["bam"] ],
                        "bVisible": false,//viewBam,
                        "sClass": "right-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else {
                                var samples = mutations.getValue(source[0], "caseIds");
                                var chr = mutations.getValue(source[0], "chr");
                                var start = mutations.getValue(source[0], "start");
                                var end = mutations.getValue(source[0], "end");
                                var ret = [];
                                for (var i=0, n=samples.length; i<n; i++) {
                                    if (mapCaseBam[samples[i]]) {
                                        ret.push('<a class="igv-link" alt="igvlinking.json?cancer_study_id'
                                                +'=prad_su2c&case_id='+samples[i]+'&locus=chr'+chr+'%3A'+start+'-'+end+'">'
                                                +'<span style="background-color:#88C;color:white">&nbsp;IGV&nbsp;</span></a>');
                                    }
                                }
                                return ret.join("&nbsp;");
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// cna
                        "aTargets": [ mutTableIndices['cna'] ],
                        "bVisible": !mutations.colAllNull('cna'),
                        "sClass": "center-align-td",
                        "bSearchable": false,
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var cna = mutations.getValue(source[0], 'cna');
                                switch (cna) {
                                    case "-2": return "<span style='color:blue;' class='"
                                           +table_id+"-tip' alt='Homozygously deleted'><b>HOMDEL</b></span>";
                                    case "-1": return "<span style='color:blue;font-size:smaller;' class='"
                                           +table_id+"-tip' alt='Heterozygously deleted'><b>hetloss</b></span>";
                                    case "0": return "<span style='color:black;font-size:xx-small;' class='"
                                           +table_id+"-tip' alt='Diploid / normal'>diploid</span>";
                                    case "1": return "<span style='color:red;font-size:smaller;' class='"
                                           +table_id+"-tip' alt='Low-level gain'><b>gain</b></span>";
                                    case "2": return "<span style='color:red;' class='"
                                           +table_id+"-tip' alt='High-level amplification'><b>AMP</b></span>";
                                    default: return "<span style='color:gray;font-size:xx-small;' class='"
                                           +table_id+"-tip' alt='CNA data is not available for this gene.'>NA</span>";
                                }
                            } else if (type==='sort') {
                                var cna = mutations.getValue(source[0], 'cna');
                                return cna?cna:0;
                            } else if (type==='type') {
                                return 0.0;
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// mrna
                        "aTargets": [ mutTableIndices['mrna'] ],
                        "bVisible": !mutations.colAllNull('mrna'),
                        "sClass": "center-align-td",
                        "bSearchable": false,
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var mrna = mutations.getValue(source[0], 'mrna');
                                if (mrna===null) return "<span style='color:gray;font-size:xx-small;' class='"
                                           +table_id+"-tip' alt='mRNA data is not available for this gene.'>NA</span>";
                                return "<div class='"+table_id+"-mrna' alt='"+source[0]+"'></div>";
                            } else if (type==='sort') {
                                var mrna = mutations.getValue(source[0], 'mrna');
                                return mrna ? mrna['perc'] : 50;
                            } else if (type==='type') {
                                    return 0.0;
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// gene mutation rate
                        "aTargets": [ mutTableIndices["altrate"] ],
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return "<div class='"+table_id+"-mut-cohort' alt='"+source[0]+"'></div>";
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
                    {// pancan mutations
                        "aTargets": [ mutTableIndices["pancan_mutations"] ],
                        "sClass": "center-align-td",
                        "bSearchable": false,
                        "mDataProp": function(source,type,value) {
                            if (type === 'display') {
                                if (genomicEventObs.pancan_mutation_frequencies) {

                                    var keyword = mutations.getValue(source[0], "key");
                                    var hugo = mutations.getValue(source[0], "gene");

                                    // This is for the moustache-like templates
                                    // prevents collisions with JSP tags
                                    _.templateSettings = {
                                        interpolate : /\{\{(.+?)\}\}/g
                                    };
                                    var thumbnail_template = _.template("<div class='pancan_mutations_histogram_thumbnail' gene='{{gene}}' keyword='{{keyword}}'></div>");

                                    return thumbnail_template({gene: hugo, keyword: keyword});
                                } else {
                                    return "<img width='15' height='15' id='pancan_mutations_histogram' src='images/ajax-loader.gif'/>";
                                }
                            }
                            else if (type === "sort") {
                                if (genomicEventObs.pancan_mutation_frequencies) {
                                    var hugo = mutations.getValue(source[0], "gene");
                                    var byHugoData = genomicEventObs.pancan_mutation_frequencies[hugo];

                                    var total_mutation_count = _.reduce(byHugoData, function(acc, next) {
                                        return acc + next.count;
                                    }, 0);

                                    return total_mutation_count;
                                }
                            }
                            else if (type === "type") {
                                return 0.0;
                            }

                            return "foobar";
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// cosmic
                        "aTargets": [ mutTableIndices["cosmic"] ],
                        "sClass": "right-align-td",
                        "asSorting": ["desc", "asc"],
                        "bSearchable": false,
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var cosmic = mutations.getValue(source[0], 'cosmic');
                                if (!cosmic) return "";
                                var arr = [];
                                var n = 0;
                                cosmic.forEach(function(c) {
                                    arr.push("<td>"+c[0]+"</td><td>"+c[1]+"</td><td>"+c[2]+"</td>");
                                    n += c[2];
                                });
                                if (n===0) return "";
                                var tip = '<b>'+n+' occurrences of '+mutations.getValue(source[0], 'key')
                                    +' mutations in COSMIC</b><br/><table class="'+table_id
                                    +'-cosmic-table uninitialized"><thead><th>COSMIC ID</th><th>Protein Change</th><th>Occurrence</th></thead><tbody><tr>'
                                    +arr.join('</tr><tr>')+'</tr></tbody></table>';
                                return  "<span class='"+table_id
                                                +"-cosmic-tip' alt='"+tip+"'>"+n+"</span>";
                            } else if (type==='sort') {
                                var cosmic = mutations.getValue(source[0], 'cosmic');
                                var n = 0;
                                if (cosmic) {
                                    cosmic.forEach(function(c) {
                                        n += c[2];
                                    });
                                }
                                return n;
                            } else if (type==='type') {
                                return 0;
                            } else {
                                return mutations.getValue(source[0], 'cosmic');
                            }
                        }
                    },
                    {// drugs
                        "aTargets": [ mutTableIndices["drug"] ],
                        "sClass": "center-align-td",
                        "bSearchable": false,
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var drug = mutations.getValue(source[0], 'drug');
                                if (!drug) return '';
                                var len = drug.length;
                                if (len===0) return '';
                                return "<img src='images/drug.png' width=12 height=12 id='"
                                            +table_id+'_'+source[0]+"-drug-tip' class='"
                                            +table_id+"-drug-tip' alt='"+drug.join(',')+"'>";
                            } else if (type==='sort') {
                                var drug = mutations.getValue(source[0], 'drug');
                                return drug ? drug.length : 0;
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
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var ma = mutations.getValue(source[0], 'ma');
                                
                                var score = ma['score'];
                                var maclass,impact;
                                if (score==='N') {maclass="oma_link oma_neutral"; impact='Neutral';}
                                else if (score==='L') {maclass="oma_link oma_low"; impact='Low';}
                                else if (score==='M') {maclass="oma_link oma_medium"; impact='Medium';}
                                else if (score==='H') {maclass="oma_link oma_high"; impact='High';}
                                
                                var ret = "";
                                if (impact) {
                                    var tip = "";
                                    var xvia = ma['xvia'];
                                    if (xvia!=null) {
                                        if (xvia.indexOf('http://')!==0) xvia='http://'+xvia;
                                        
                                        tip += "<div class=\"mutation-assessor-main-link mutation-assessor-link\">" +
                                                "<a href=\""+xvia+"\" target=\"_blank\"><img height=\"15\" width=\"19\" src=\"images/ma.png\"> Go to Mutation Assessor</a></div>";
                                    }
                                    
                                    var msa = ma['msa'];
                                    if (msa&&msa!=='NA') {
                                        if (msa.indexOf('http://')!==0) msa='http://'+msa;
                                        tip += "<div class=\"mutation-assessor-msa-link mutation-assessor-link\">"+
                                               "<a href=\""+msa+"\" target=\"_blank\"><span class=\"ma-msa-icon\">msa</span> Multiple Sequence Alignment</a></div>";
                                    }
                                    
                                    var pdb = ma['pdb'];
                                    if (pdb&&pdb!=='NA') {
                                        if (pdb.indexOf('http://')!==0) pdb='http://'+pdb;
                                        tip += "<div class=\"mutation-assessor-3d-link mutation-assessor-link\">"+
                                               "<a href=\""+pdb+"\" target=\"_blank\"><span class=\"ma-3d-icon\">3D</span> Mutation Assessor 3D View</a></div>";
                                    }

                                    ret += "<span class='"+maclass+" "+table_id+"-ma-tip' alt='"+tip+"'>"+impact+"</span>";
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
                    }
                ],
                "fnDrawCallback": function( oSettings ) {
                    if (caseIds.length>1) {
                        plotCaseLabel('.'+table_id+'-case-label',true);
                        plotAlleleFreq("."+table_id+"-tumor-freq",mutations,"alt-count","ref-count");
                        plotAlleleFreq("."+table_id+"-tumor-freq",mutations,"normal-alt-count","normal-ref-count");
                    }
                    plotMrna("."+table_id+"-mrna",mutations);
                    plotMutRate("."+table_id+"-mut-cohort",mutations);
                    addNoteTooltip("."+table_id+"-tip");
                    addNoteTooltip("."+table_id+"-ma-tip",null,{my:'top right',at:'bottom center',viewport: $(window)});
                    addDrugsTooltip("."+table_id+"-drug-tip", 'top right', 'bottom center');
                    addCosmicTooltip(table_id);
                    listenToBamIgvClick(".igv-link");
                    drawPanCanThumbnails(this);
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

        genomicEventObs.subscribePancanMutationsFrequency(function() {
            drawPanCanThumbnails(oTable);
            // redraw based on the update
            oTable.fnDraw();
        });

        return oTable;
    }
    
    function listenToBamIgvClick(elem) {
        $(elem).each(function(){
                // TODO use mutation id, instead of binding url to attr alt
                var url = $(this).attr("alt");

                $(this).click(function(evt) {
                        // get parameters from the server and call related igv function
                        $.getJSON(url, function(data) {
                                //console.log(data);
                                // TODO this call displays warning message (resend)
                                prepIGVLaunch(data.bamFileUrl, data.encodedLocus, data.referenceGenome, data.trackName);
                        });
                });
        });
    }

    function plotMutRate(div,mutations) {
        $(div).each(function() {
            if (!$(this).is(":empty")) return;
            var gene = $(this).attr("alt");
            var keymutrate = mutations.getValue(gene, 'keymutrate');
            var keyperc = 100 * keymutrate / numPatientInSameMutationProfile;
            var genemutrate = mutations.getValue(gene, 'genemutrate');
            var geneperc = 100 * genemutrate / numPatientInSameMutationProfile;
            
            var data = [keyperc, geneperc-keyperc, 100-geneperc];
            var colors = ["green", "lightgreen", "#ccc"];
                        
            var svg = d3.select($(this)[0])
                .append("svg")
                .attr("width", 86)
                .attr("height", 12);
        
            var percg = svg.append("g");
            percg.append("text")
                    .attr('x',70)
                    .attr('y',11)
                    .attr("text-anchor", "end")
                    .attr('font-size',10)
                    .text(geneperc.toFixed(1)+"%");
            
            var gSvg = percg.append("g");
            var pie = d3AccBar(gSvg, data, 30, colors);
            var tip = ""+genemutrate+" sample"+(genemutrate===1?"":"s")
                + " (<b>"+geneperc.toFixed(1) + "%</b>)"+" in this study "+(genemutrate===1?"has":"have")+" mutated "
                + mutations.getValue(gene, "gene")
                + ", out of which "+keymutrate
                + " (<b>"+keyperc.toFixed(1) + "%</b>) "
                + (keymutrate===1?"has ":"have ")+mutations.getValue(gene,'key')+" mutations.";
            qtip($(percg), tip);
            
            // mutsig
            var mutsig = mutations.getValue(gene, 'mutsig');
            if (mutsig) {
                tip = "<b>MutSig</b><br/>Q-value: "+mutsig.toPrecision(2);
                var circle = svg.append("g")
                    .attr("transform", "translate(80,6)");
                d3CircledChar(circle,"M","#55C","#66C");
                qtip($(circle), tip);
            }
            
        });
        
        function qtip(el, tip) {
            $(el).qtip({
                content: {text: tip},
	            show: {event: "mouseover"},
                hide: {fixed: true, delay: 200, event: "mouseout"},
                style: { classes: 'qtip-light qtip-rounded' },
                position: {my:'top right',at:'bottom center',viewport: $(window)}
            });
        }
    }

    function addCosmicTooltip(table_id) {
        $("."+table_id+"-cosmic-tip").qtip({
            content: {
                attr: 'alt'
            },
            events: {
                render: function(event, api) {
                    $("."+table_id+"-cosmic-table.uninitialized").dataTable( {
                        "sDom": 'pt',
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "aoColumnDefs": [
                            {
                                "aTargets": [ 0 ],
                                "mDataProp": function(source,type,value) {
                                    if (type==='set') {
                                        source[0]=value;
                                    } else if (type==='display') {
                                        return '<a href="http://cancer.sanger.ac.uk/cosmic/mutation/overview?id='+source[0]+'">'+source[0]+'</a>';
                                    } else {
                                        return source[0];
                                    }
                                }
                            },
                            {
                                "aTargets": [ 1 ],
                                "mDataProp": function(source,type,value) {
                                    if (type==='set') {
                                        source[1]=value;
                                    } else if (type==='sort') {
                                        return parseInt(source[1].replace( /^\D+/g, ''));
                                    } else if (type==='type') {
                                        return 0;
                                    } else {
                                        return source[1];
                                    }
                                }
                            }
                        ],
                        "oLanguage": {
                            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                            "sInfoFiltered": "",
                            "sLengthMenu": "Show _MENU_ per page"
                        },
                        "aaSorting": [[2,'desc']],
                        "iDisplayLength": 10
                    } ).removeClass('uninitialized');
                }
            },
	        show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"},
            style: { classes: 'qtip-light qtip-rounded qtip-wide' },
            position: {my:'top right',at:'bottom center',viewport: $(window)}
        });
    }
    
    var numPatientInSameMutationProfile = <%=numPatientInSameMutationProfile%>;
    
    $(document).ready(function(){
        $('#mutation_id_filter_msg').hide();
        var params = {
            <%=PatientView.CASE_ID%>:caseIdsStr,
            <%=PatientView.MUTATION_PROFILE%>:mutationProfileId
        };
        
        if (cnaProfileId) {
            params['<%=PatientView.CNA_PROFILE%>'] = cnaProfileId;
        }
        
        if (mrnaProfileId) {
            params['<%=PatientView.MRNA_PROFILE%>'] = mrnaProfileId;
        }
        
        if (drugType) {
            params['<%=PatientView.DRUG_TYPE%>'] = drugType;
        }
                        
        $.post("mutations.json", 
            params,
            function(data) {
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
                    style: { classes: 'qtip-light qtip-rounded' },
                    position: { my:'top center',at:'bottom center',viewport: $(window) }
                });
                $('.mutation-summary-table-name').addClass("datatable-name");
                $('#mutation_summary_wrapper_table').show();
                $('#mutation_summary_wait').remove();
                
                // mutations
                buildMutationsDataTable(genomicEventObs.mutations,genomicEventObs.mutations.getEventIds(false),
                    'mutation_table', '<"H"<"all-mutation-table-name">fr>t<"F"C<"datatable-paging"pil>>', 100, "No mutation events", false);
                $('.all-mutation-table-name').html(
                    ""+genomicEventObs.mutations.getNumEvents()+" nonsynonymous mutations");
                $('.all-mutation-table-name').addClass("datatable-name");
                $('#mutation_wrapper_table').show();
                $('#mutation_wait').remove();

                var pancanMutationsUrl = "pancancerMutations.json";
                var byKeywordResponse = [];
                var byHugoResponse = [];

                function munge(response, key) {
                    // munge data to get it into the format: keyword -> corresponding datum
                    return d3.nest().key(function(d) { return d[key]; }).entries(response)
                            .reduce(function(acc, next) { acc[next.key] = next.values; return acc;}, {});
                }

                var splitJobs = function(cmd, reqData, type) {
                    var jobs = [];
                    var batchSize = 1000;

                    var numOfBatches = Math.ceil(reqData.length / batchSize);
                    for(var b=0; b<numOfBatches; b++) {
                        var first = b*batchSize;
                        var last = Math.min((b+1)*batchSize, reqData.length);

                        var accData = reqData.slice(first, last).join(",");

                        jobs.push(
                                $.post(pancanMutationsUrl,
                                        {
                                            cmd: cmd,
                                            q: accData
                                        }, function(batchData) {
                                            if(cmd == "byKeywords") {
                                                byKeywordResponse = byKeywordResponse.concat(batchData);
                                            } else if( cmd == "byHugos") {
                                                byHugoResponse = byHugoResponse.concat(batchData);
                                            } else {
                                                console.trace("Ooops! Something is wrong!");
                                            }
                                        }
                                )
                        );

                    }

                    return jobs;
                };

                var jobs = splitJobs("byKeywords", genomicEventObs.mutations.data.key)
                                .concat(splitJobs("byHugos", genomicEventObs.mutations.data.gene));
                $.when.apply($, jobs).done(function() {
                    genomicEventObs.pancan_mutation_frequencies
                            = _.extend(munge(byKeywordResponse, "keyword"), munge(byHugoResponse, "hugo"));
                    genomicEventObs.fire("pancan-mutation-frequency-built");
                });

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
        var cancerGene = data['cancer-gene'];
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
            if (cancerGene[i]) {
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
            var cosmicI= cosmic[i];
            if (cosmicI) {
                var lenI = cosmicI.length;
                for(var j=0; j<lenI && ncosmic<patient_view_cosmic_threhold; j++) {
                    ncosmic += cosmicI[j][2];
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
<div  id="pancan_mutations_histogram_container"></div>
<table cellpadding="0" cellspacing="0" border="0" id="mutation_wrapper_table" width="100%" style="display:none;">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_table">
                <%@ include file="mutations_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
