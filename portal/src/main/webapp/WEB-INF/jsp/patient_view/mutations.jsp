<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.dao.DaoMutSig" %>
<%@ page import="org.mskcc.cbio.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>

<script type="text/javascript" src="js/lib/igv_webstart.js?<%=GlobalProperties.getAppVersion()%>"></script>

<link href="css/mutationMapper.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>

<script type="text/javascript">
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };
    var mutationOncokbInstance;

    function buildMutationsDataTable(mutations,mutEventIds, table_id, sDom, iDisplayLength, sEmptyInfo, compact) {
        var data = [];
        for (var i=0, nEvents=mutEventIds.length; i<nEvents; i++) {
            var _id = mutEventIds[i];
            data.push([mutEventIds[i]]);
        }

        var oTable = $("#"+table_id).dataTable( {
                "sDom": sDom, // selectable columns
                "oColVis": { "aiExclude": [ mutTableIndices["id"], mutTableIndices["pancan_mutations"] ] }, // always hide id column
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
                        "bSearchable": false,
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
//                                var sanger = mutations.getValue(source[0], 'sanger');
//                                if (sanger) {
//                                    tip += "<br/><a href=\"http://cancer.sanger.ac.uk/cosmic/gene/overview?ln="
//                                        +gene+"\">Sanger Cancer Gene Census</a>";
//                                }
                                var ret = "<b>"+gene+"</b>";
                                if(mutations.colExists('oncokb')) {
                                    ret = "<span class='"+table_id+"-tip oncokb oncokb_gene' gene='"+gene+"' oncokbId='"+source[0]+"'>"+ret+"</span>";
                                }else if(OncoKB.getAccess()){
                                    ret += "<img width='14' height='14' src='images/ajax-loader.gif' alt='loading' />";
                                } else {
                                    ret = "<div class='"+table_id
                                            +"-tip' alt='"+tip+"'>"+ret+"</div>";
                                }

                                return ret;
                            } else {
                                return mutations.getValue(source[0], "gene");
                            }
                        }
                    },
                    {// annotation
                        "aTargets": [ mutTableIndices["annotation"] ],
                        "sClass": "no-wrap-td",
                        "sType": "sort-icons",
                        "mDataProp": function(source,type,value) {
                            if (type === 'set') {
                                return;
                            } else if (type === 'display') {
                                var ret = "";
                                var gene = mutations.getValue(source[0], 'gene');
                                var aa = mutations.getValue(source[0], 'aa');
                                if (aa.length > 2 && aa.substring(0, 2) == 'p.')
                                    aa = aa.substring(2);
                                if (mutations.colExists('oncokb')) {
                                    var oncokbInfo = mutations.getValue(source[0], 'oncokb');

                                    ret += "<span class='annotation-item oncokb oncokb_alteration oncogenic' oncokbId='" + source[0] + "'></span>";
                                } else if (OncoKB.getAccess()) {
                                    ret += '<span class="annotation-item"><img width="14" height="14" src="images/ajax-loader.gif" alt="loading" /></span>';
                                }

                                var mcg = mutations.getValue(source[0], 'mycancergenome');
                                if (!cbio.util.checkNullOrUndefined(mcg) && mcg.length) {
                                    ret += "<span class='annotation-item " + table_id + "-tip'" +
                                            "alt='<b>My Cancer Genome links:</b><br/><ul style=\"list-style-position: inside;padding-left:0;\"><li>" + mcg.join("</li><li>") + "</li></ul>'>" +
                                            "<img width='14' height='14' src='images/mcg_logo.png'></span>";
                                }else {
                                    ret += "<span class='annotation-item'></span>";
                                }
                                if (showHotspot && mutations.getValue(source[0], 'is-hotspot')) {
                                    ret += "<span class='annotation-item " + table_id + "-hotspot' alteration='" + aa + "' oncokbId='" + source[0] + "'><img width='14' height='14' src='images/cancer-hotspots.svg'></span>";
                                }else {
                                    ret += "<span class='annotation-item'></span>";
                                }
                                if (showCivic) {
                                    ret += "<span class='annotation-item civic' proteinChange='" + aa + "' geneSymbol='" + gene + "' " +
                                        "<img width='14' height='14' src='images/ajax-loader.gif' alt='Civic Variant Entry'></span>";
                                }
                                return ret;
                            } else if (type === 'sort') {
                                var datum = {
                                    mutation: {
                                        myCancerGenome: [],
                                        isHotspot: false,
                                        get: function(a) { return this[a];}
                                    },
                                    oncokb:{}
                                };

                                if(mutations.colExists('mycancergenome')) {
                                    datum.mutation.myCancerGenome = mutations.getValue(source[0], 'mycancergenome');
                                }

                                if(mutations.colExists('is-hotspot')) {
                                    datum.mutation.isHotspot = mutations.getValue(source[0], 'is-hotspot');
                                }

                                if (mutations.colExists('oncokb')) {
                                    datum.oncokb = mutations.getValue(source[0], 'oncokb');
                                }
                                return datum;
                            } else {
                                return mutations.getValue(source[0], 'aa');
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
                                var status = mutations.getValue(source[0],'status');
                                if (!cbio.util.checkNullOrUndefined(status) && status.toLowerCase()==="germline")
                                    ret += "&nbsp;<span style='background-color:red;color:white;font-size:x-small;' class='"
                                            +table_id+"-tip' alt='Germline mutation'>Germline</span>";

                                var aaOriginal = mutations.getValue(source[0], 'aa-orig');

	                            if (window.cancerStudyId.indexOf("mskimpact") !== -1 &&
	                                isDifferentProteinChange(aa, aaOriginal))
	                            {
		                            ret += "&nbsp;<span class='"+table_id+"-tip'" +
		                                   "alt='The original annotation file indicates a different value: <b>"+normalizeProteinChange(aaOriginal)+"</b>'>" +
		                                   "<img class='mutationsProteinChangeWarning' height=13 width=13 src='images/warning.gif'></span>";
	                            }

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
                                    if (cbio.util.checkNullOrUndefined(ac)||cbio.util.checkNullOrUndefined(rc)) return "";
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
                                return "<div class='"+table_id+"-normal-freq' alt='"+source[0]+"'></div>";
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
                                           +table_id+"-tip' alt='Deep deletion'><b>DeepDel</b></span>";
                                    case "-1": return "<span style='color:blue;font-size:smaller;' class='"
                                           +table_id+"-tip' alt='Shallow deletion'><b>ShallowDel</b></span>";
                                    case "0": return "<span style='color:black;font-size:xx-small;' class='"
                                           +table_id+"-tip' alt='Diploid / normal'>Diploid</span>";
                                    case "1": return "<span style='color:red;font-size:smaller;' class='"
                                           +table_id+"-tip' alt='Low-level gain'><b>Gain</b></span>";
                                    case "2": return "<span style='color:red;' class='"
                                           +table_id+"-tip' alt='High-level amplification'><b>Amp</b></span>";
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
                        "bVisible": false, // hide pancan column for now for performance reason
                        "sClass": "center-align-td",
                        "bSearchable": false,
                        "mDataProp": function(source,type,value) {
                            if (type === 'display') {
                                var keyword = mutations.getValue(source[0], "key");
                                var hugo = mutations.getValue(source[0], "gene");
                                var proteinPos = hugo+"_"+mutations.getValue(source[0], "protein-start");

                                var ret = "<div class='pancan_mutations_histogram_thumbnail' protein_pos='"+proteinPos+"' gene='"+hugo+"' keyword='"+keyword+"'></div>";
                                    ret += "<img width='14' height='14' class='pancan_mutations_histogram_wait' src='images/ajax-loader.gif' alt='loading' />";
                                    ret += "<div class='pancan_mutations_histogram_count' style='float:right' protein_pos='"+proteinPos+"' gene='"+hugo+"' keyword='"+keyword+"'></div>";

                                return ret;
                            }
                            else if (type === "sort") {
                                if (genomicEventObs.pancan_mutation_frequencies) {
                                    var key = mutations.getValue(source[0], "key");
                                    return genomicEventObs.pancan_mutation_frequencies.countByKey(key);
                                } else {
                                    return 0;
                                }
                            }
                            else if (type === "type") {
                                return 0.0;
                            }

                            return "";
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
                        "bVisible": false,
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
                        "bVisible": false,
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
                                        xvia = xvia.replace("getma.org", "mutationassessor.org");
                                        tip += "<div class=\"mutation-assessor-main-link mutation-assessor-link\">" +
                                                "<a href=\""+xvia+"\" target=\"_blank\"><img height=\"15\" width=\"19\" src=\"images/ma.png\"> Go to Mutation Assessor</a></div>";
                                    }

                                    var msa = ma['msa'];
                                    if (msa&&msa!=='NA') {
                                        if (msa.indexOf('http://')!==0) msa='http://'+msa;
                                        msa=msa.replace("getma.org", "mutationassessor.org");
                                        tip += "<div class=\"mutation-assessor-msa-link mutation-assessor-link\">"+
                                               "<a href=\""+msa+"\" target=\"_blank\"><span class=\"ma-msa-icon\">msa</span> Multiple Sequence Alignment</a></div>";
                                    }

                                    var pdb = ma['pdb'];
                                    if (pdb&&pdb!=='NA') {
                                        pdb=pdb.replace("getma.org", "mutationassessor.org");
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
                        plotAlleleFreq("."+table_id+"-normal-freq",mutations,"normal-alt-count","normal-ref-count");
                    }
                    plotMrna("."+table_id+"-mrna",mutations);
                    plotMutRate("."+table_id+"-mut-cohort",mutations);
                    addNoteTooltip("."+table_id+"-tip");
                    addNoteTooltip("."+table_id+"-ma-tip",null,{my:'top right',at:'bottom center',viewport: $(window)});
                    if(showHotspot) {
                        addNoteTooltip('.'+table_id+'-hotspot', cbio.util.getHotSpotDesc(true));
                    }
                    addDrugsTooltip("."+table_id+"-drug-tip", 'top right', 'bottom center');
                    addCosmicTooltip(table_id);
                    listenToBamIgvClick(".igv-link");
                    //drawPanCanThumbnails(this);
                    if(mutationOncokbInstance){
                        mutationOncokbInstance.addEvents(this, 'gene');
                        mutationOncokbInstance.addEvents(this, 'alteration');
                        mutationOncokbInstance.addEvents(this, 'column');
                    }
                },
                "bPaginate": true,
                "sPaginationType": "two_button",
                "aaSorting": [[mutTableIndices["annotation"], 'asc'], [mutTableIndices["cosmic"],'desc'],[mutTableIndices["altrate"],'desc'], [mutTableIndices["gene"], 'asc']],
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

//        genomicEventObs.subscribePancanMutationsFrequency(function() {
//            drawPanCanThumbnails(oTable);
//        });

        return oTable;
    }

    function findCosmic(mutationCosmicArray, aa) {
        var count = 0;

        if (_.isArray(mutationCosmicArray)) {
            _.each(mutationCosmicArray, function (item) {
                if (item[1] === aa) {
                    count += item[2];
                }
            })
        }

        return count;
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
            <%=PatientView.SAMPLE_ID%>:caseIdsStr,
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
                    $('.mutation-show-more').html("<a href='#mutations' onclick='switchToTab(\"tab_mutations\");return false;'\n\
                  title='Show more mutations of this patient'>Show all "
                            +numAll+" mutations</a>");
                    $('.mutation-show-more').addClass('datatable-show-more');
                    var mutationSummary;
                    if (numAll===numFiltered) {
                        mutationSummary = ""+numAll+" mutations";
                    } else {
                        mutationSummary = "Mutations of interest"
                                +(numAll==0?"":(" ("
                                +numFiltered
                                +" of <a href='#mutations' onclick='switchToTab(\"tab_mutations\");return false;'\n\
                     title='Show more mutations of this patient'>"
                                +numAll
                                +"</a>)"))
                                +" <img id='mutations-summary-help' src='images/help.png' \n\
                    title='This table contains somatic mutations in genes that are \n\
                    <ul><li>either annotated cancer genes</li>\n\
                    <li>or recurrently mutated, namely\n\
                        <ul><li>MutSig Q < 0.05, if MutSig results are available</li>\n\
                        <li>otherwise, mutated in > 5% of samples in the study with &ge; 50 samples</li></ul> </li>\n\
                    <li>or with > 5 overlapping entries in COSMIC.</li></ul>' alt='help' />";
                    }
                    $('.mutation-summary-table-name').html(mutationSummary);
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
                    var byProteinPosResponse = [];

                    function munge(response, key) {
                        // munge data to get it into the format: keyword -> corresponding datum
                        return d3.nest().key(function(d) { return d[key]; }).entries(response)
                                .reduce(function(acc, next) { acc[next.key] = next.values; return acc;}, {});
                    }

                    // Get OncoKB info
                    mutationOncokbInstance = initOncoKB('patient-mutation', genomicEventObs.mutations.getEventIds(false), genomicEventObs.mutations, 'mutation', function() {
                        var mutationSummaryTable = $('#mutation_summary_table').dataTable();
                        var mutationTable = $('#mutation_table').dataTable();

                        var mutationSummaryTableData = mutationSummaryTable.fnGetData();
                        var mutationTableData = mutationTable.fnGetData();

                        var oncokbEvidence = [];
                        _.each(mutationTableData, function(ele, i) {
                            oncokbEvidence.push(mutationOncokbInstance.getVariant(ele[0]));
                        });
                        genomicEventObs.mutations.addData('oncokb', oncokbEvidence);

                        if (mutationTableData.length > 0){
                            _.each(mutationTableData, function(ele, i) {
                                mutationTable.fnUpdate(null, i, mutTableIndices["annotation"], false, false);
                            });

                            mutationTable.fnUpdate(null, 0, mutTableIndices['annotation']);
                        }

                        if (mutationSummaryTableData.length > 0){
                            _.each(mutationSummaryTableData, function(ele, i) {
                                mutationSummaryTable.fnUpdate(null, i, mutTableIndices["annotation"], false, false);
                            });

                            mutationSummaryTable.fnUpdate(null, 0, mutTableIndices['annotation']);
                        }
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

    // TODO: DUPLICATED FUNCTION from mutation-mapper.
    // we should use mutation mapper as a library in patient view...
    /**
     * Checks if given 2 protein changes are completely different from each other.
     *
     * @param proteinChange
     * @param aminoAcidChange
     * @returns {boolean}
     */
    function isDifferentProteinChange(proteinChange, aminoAcidChange)
    {
	    var different = false;

	    proteinChange = normalizeProteinChange(proteinChange);
	    aminoAcidChange = normalizeProteinChange(aminoAcidChange);

	    // if the normalized strings are exact, no need to do anything further
	    if (aminoAcidChange !== proteinChange)
	    {
		    // assuming each uppercase letter represents a single protein
		    var proteinMatch1 = proteinChange.match(/[A-Z]/g);
		    var proteinMatch2 = aminoAcidChange.match(/[A-Z]/g);

		    // assuming the first numeric value is the location
		    var locationMatch1 = proteinChange.match(/[0-9]+/);
		    var locationMatch2 = aminoAcidChange.match(/[0-9]+/);

		    // assuming first lowercase value is somehow related to
		    var typeMatch1 = proteinChange.match(/([a-z]+)/);
		    var typeMatch2 = aminoAcidChange.match(/([a-z]+)/);

		    if (locationMatch1 && locationMatch2 &&
		        locationMatch1.length > 0 && locationMatch2.length > 0 &&
		        locationMatch1[0] != locationMatch2[0])
		    {
			    different = true;
		    }
		    else if (proteinMatch1 && proteinMatch2 &&
		             proteinMatch1.length > 0 && proteinMatch2.length > 0 &&
		             proteinMatch1[0] !== "X" && proteinMatch2[0] !== "X" &&
		             proteinMatch1[0] !== proteinMatch2[0])
		    {
			    different = true;
		    }
		    else if (proteinMatch1 && proteinMatch2 &&
		             proteinMatch1.length > 1 && proteinMatch2.length > 1 &&
		             proteinMatch1[1] !== proteinMatch2[1])
		    {
			    different = true;
		    }
		    else if (typeMatch1 && typeMatch2 &&
		             typeMatch1.length > 0 && typeMatch2.length > 0 &&
		             typeMatch1[0] !== typeMatch2[0])
		    {
			    different = true;
		    }
	    }

	    return different;
    }

    // TODO: DUPLICATED FUNCTION from mutation-mapper.
    function normalizeProteinChange(proteinChange)
    {
            if (cbio.util.checkNullOrUndefined(proteinChange)) {
                return "";
            }

	    var prefix = "p.";

	    if (proteinChange.indexOf(prefix) !== -1)
	    {
		    proteinChange = proteinChange.substr(proteinChange.indexOf(prefix) + prefix.length);
	    }

	    return proteinChange;
    }

</script>

<div id="mutation_wait"><img src="images/ajax-loader.gif" alt="loading" /></div>
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
