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

<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>

<script type="text/javascript">
    var cnaTableIndices = cbio.util.arrayToAssociatedArrayIndices(["id","case_ids","gene","alteration", "annotation","cytoband","mrna","altrate","drug"]);
    function buildCnaDataTable(cnas, cnaEventIds, table_id, sDom, iDisplayLength, sEmptyInfo) {
        var data = [];
        var oncokbInstance;

        if(OncoKB.getAccess()) {
            var oncokbInstanceManager = new OncoKB.addInstanceManager();
            oncokbInstance = oncokbInstanceManager.addInstance('patient-cna');
            if(oncokbGeneStatus) {
                oncokbInstance.setGeneStatus(oncokbGeneStatus);
            }
            oncokbInstance.setTumorType(OncoKB.utils.getTumorTypeFromClinicalDataMap(clinicalDataMap));
        }

        for (var i=0, nEvents=cnaEventIds.length; i<nEvents; i++) {
            if(oncokbInstance) {
                var _id = cnaEventIds[i];
                var alter = '';
                switch(cnas.getValue(_id, "alter")) {
                    case 2:
                        alter = 'amplification';
                        break;
                    case -2:
                        alter = 'deletion';
                        break;
                    default:
                        alter = null;
                }
                oncokbInstance.addVariant(_id, cnas.getValue(_id, "entrez"), cnas.getValue(_id, "gene"), alter,
                    (_.isObject(patientInfo) ? (patientInfo.CANCER_TYPE_DETAILED || patientInfo.CANCER_TYPE) : '') || cancerType,
                    alter);
            }
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
                    {// case_ids
                        "aTargets": [ cnaTableIndices["case_ids"] ],
                        "sClass": "center-align-td",
                        "bVisible": caseIds.length>1,
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var samples = cnas.getValue(source[0], "caseIds");
                                var ret = [];
                                for (var i=0, n=caseIds.length; i<n; i++) {
                                    var caseId = caseIds[i];
                                    if ($.inArray(caseId,samples)>=0) {
                                        ret.push("<svg width='12' height='12' class='"
                                            +table_id+"-case-label' alt='"+caseId+"'></svg>");
                                    } else {
                                        ret.push("<span><svg width='12' height='12'></svg></span>");
                                    }
                                }
                                
                                return ret.join("&nbsp;");
                            } else if (type==='sort') {
                                var samples = cnas.getValue(source[0], "caseIds");
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
                                return cnas.getValue(source[0], "caseIds");
                            }
                        }
                    },
                    {// gene
                        "aTargets": [ cnaTableIndices['gene'] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var gene = cnas.getValue(source[0], "gene");
                                var entrez = cnas.getValue(source[0], "entrez");
                                var tip = "<a href=\"http://www.ncbi.nlm.nih.gov/gene/"
                                    +entrez+"\">NCBI Gene</a>";
//                                var sanger = cnas.getValue(source[0], 'sanger');
//                                if (sanger) {
//                                    tip += "<br/><a href=\"http://cancer.sanger.ac.uk/cosmic/gene/overview?ln="
//                                        +gene+"\">Sanger Cancer Gene Census</a>";
//                                }
                                var ret = "<b>"+gene+"</b>";
                                if(cnas.colExists('oncokb')) {
                                    ret = "<span class='"+table_id+"-tip oncokb oncokb_gene' gene='"+gene+"' oncokbId='"+source[0]+"'>"+ret+"</span>";
                                }else if(OncoKB.getAccess()){
                                    ret += "<img width='14' height='14' src='images/ajax-loader.gif' alt='loading' />";
                                }else {
                                    
                                    ret = "<span class='"+table_id+"-tip' alt='"+tip+"'>"+ret+"</span>";
                                }
                                return ret;
                            } else {
                                return cnas.getValue(source[0], "gene");
                            }
                        }
                    },
                    {// cytoband
                        "aTargets": [ cnaTableIndices['cytoband'] ],
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else {
                                return cnas.getValue(source[0], "cytoband");
                            }
                        }
                    },
                    {// alteration
                        "aTargets": [ cnaTableIndices['alteration'] ],
                        "sClass": "center-align-td",
                        "mDataProp": function(source,type,value) {
                            if (type==='set') {
                                source[1]=value;
                                return;
                            } else if (type==='display') {
                                var alter = cnas.getValue(source[0], "alter");
                                var gene = cnas.getValue(source[0], "gene");
                                var strAlt;
                                switch(alter) {
                                case 2:
                                    strAlt="<span style='color:red;' class='"
                                           +table_id+"-tip' alt='"+gene
                                           +" is amplified (putative)'>AMP</span>";
                                    break;
                                case -2:
                                    strAlt="<span style='color:blue;' class='"
                                           +table_id+"-tip' alt='"+gene
                                           +" is deeply deleted (putative)'>DeepDel</span>";
                                    break;
                                default: strAlt='Unknown';
                                }
                                strAlt = "<b>"+strAlt+"</b>";

                                return strAlt;
                            } else if (type==='filter') {
                                switch(cnas.getValue(source[0], "alter")) {
                                case 2: return 'AMP';
                                case -2: return 'DeepDel';
                                default: return 'Unknown';
                                }
                            } else {
                                return cnas.getValue(source[0], "alter");
                            }
                        }
                    },
                    {// annotation
                        "aTargets": [cnaTableIndices["annotation"]],
                        "sClass": "no-wrap-td",
                        "sType": "sort-icons",
                        "mDataProp": function (source, type, value) {
                            if (type === 'set') {
                                return '';
                            } else if (type === 'display') {
                                var str = '';
                                if (cnas.colExists('oncokb')) {
                                    str += "&nbsp;<span class='annotation-item oncokb oncokb_alteration oncogenic' oncokbId='" + source[0] + "'></span>";
                                    str += "<span class='oncokb oncokb_column' oncokbId='" + source[0] + "'></span>";
                                } else if (OncoKB.getAccess()) {
                                    str += '<img width="14" height="14" src="images/ajax-loader.gif" alt="loading" />';
                                }
                                return str;
                            } else if (type === 'sort') {
                                var datum = {
                                    mutation: {
                                        myCancerGenome: [],
                                        isHotspot: false
                                    },
                                    oncokb:{}
                                };

                                if(cnas.colExists('mycancergenome')) {
                                    datum.mutation.myCancerGenome = cnas.getValue(source[0], 'mycancergenome');
                                }

                                if(cnas.colExists('is-hotspot')) {
                                    datum.mutation.isHotspot = cnas.getValue(source[0], 'is-hotspot');
                                }

                                if (cnas.colExists('oncokb')) {
                                    datum.oncokb = cnas.getValue(source[0], 'oncokb');
                                }
                                return datum;
                            } else {
                                return '';
                            }
                        }
                    },
                    {// mrna
                        "aTargets": [ cnaTableIndices['mrna'] ],
                        "bVisible": !cnas.colAllNull('mrna'),
                        "sClass": "center-align-td",
                        "bSearchable": false,
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var mrna = cnas.getValue(source[0], 'mrna');
                                if (mrna===null) return "<span style='color:gray;' class='"
                                           +table_id+"-tip' alt='mRNA data is not available for this gene.'>NA</span>";
                                return "<div class='"+table_id+"-mrna' alt='"+source[0]+"'></div>";
                            } else if (type==='sort') {
                                var mrna = cnas.getValue(source[0], 'mrna');
                                return mrna ? mrna['perc'] : 50;
                            } else if (type==='type') {
                                    return 0.0;
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// context
                        "aTargets": [ cnaTableIndices['altrate'] ],
                        "sClass": "center-align-td",
                        "bSearchable": false,
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                return "<div class='"+table_id+"-cna-cohort' alt='"+source[0]+"'></div>";
                            } else if (type==='sort') {
                                var altrate = cnas.getValue(source[0], 'altrate');
                                return altrate;
                            } else if (type==='type') {
                                    return 0.0;
                            } else {
                                return '';
                            }
                        },
                        "asSorting": ["desc", "asc"]
                    },
                    {// Drugs
                        "aTargets": [ cnaTableIndices['drug'] ],
                        "bVisible": false,
                        "sClass": "center-align-td",
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var drug = cnas.getValue(source[0], 'drug');
                                if (!drug) return '';
                                var len = drug.length;
                                if (len==0) return '';
                                return "<img src='images/drug.png' width=12 height=12 id='"
                                            +table_id+'_'+source[0]+"-drug-tip' class='"
                                            +table_id+"-drug-tip' alt='"+drug.join(',')+"'>";
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
                    }
                ],
                "fnDrawCallback": function( oSettings ) {
                    if (caseIds.length>1) {
                        plotCaseLabel('.'+table_id+'-case-label',true);
                    }
                    plotMrna("."+table_id+"-mrna",cnas);
                    plotCnaAltRate("."+table_id+"-cna-cohort",cnas);
                    addNoteTooltip("."+table_id+"-tip");
                    addDrugsTooltip("."+table_id+"-drug-tip", 'top right', 'bottom center');
                    if(oncokbInstance){
                        oncokbInstance.addEvents(this, 'gene');
                        oncokbInstance.addEvents(this, 'alteration');
                        oncokbInstance.addEvents(this, 'column');
                    }
                },
                "bPaginate": true,
                "sPaginationType": "two_button",
                "aaSorting": [[cnaTableIndices["annotation"], 'asc'], [cnaTableIndices['altrate'],'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page",
                    "sEmptyTable": sEmptyInfo
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        if(oncokbInstance) {
            oncokbInstance.getIndicator().then(function () {
                var tableData = oTable.fnGetData();
                var oncokbEvidence = [];
                _.each(tableData, function(ele, i) {
                    oncokbEvidence.push(oncokbInstance.getVariant(ele[0]));
                });
                cnas.addData('oncokb', oncokbEvidence)
                if (tableData.length > 0)
                {
                    _.each(tableData, function(ele, i) {
                        oTable.fnUpdate(null, i, cnaTableIndices["annotation"], false, false);
                    });

                    oTable.fnUpdate(null, 0, cnaTableIndices['annotation']);
                }
            });
        }
        oTable.css("width","100%");
        addNoteTooltip("#"+table_id+" th.cna-header");
        return oTable;
    }

    function plotCnaAltRate(div,cnas) {
        $(div).each(function() {
            if (!$(this).is(":empty")) return;
            var gene = $(this).attr("alt");
            var altrate = cnas.getValue(gene, 'altrate');
            var perc = 100 * altrate / numPatientInSameCnaProfile;
            var alter = cnas.getValue(gene, "alter");
            var data = [perc, 100-perc];
            var colors = [alter>0?"red":"blue", "#ccc"];
            
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
                    .text(perc.toFixed(1)+"%");
            
            var gSvg = percg.append("g");
            
            var pie = d3AccBar(gSvg, data, 30, colors);

            var tip = ""+altrate+" sample"+(altrate===1?"":"s")
                    +" (<b>"+perc.toFixed(1) + "%</b>)"+" in this study "
                    +(altrate===1?"has ":"have ")+(alter===-2?"deleted ":"amplified ")
                    +cnas.getValue(gene, "gene");
            qtip($(percg), tip);
            
            // gistic
            var gistic = cnas.getValue(gene, 'gistic');
            if (gistic) {
                tip = "<b>Gistic</b><br/><i>Q-value</i>: "+gistic[0].toPrecision(2)
                            +"<br/><i>Number of genes in the peak</i>: "+gistic[1];
                var circle = svg.append("g")
                    .attr("transform", "translate(80,6)");
                d3CircledChar(circle,"G","#55C","#66C");
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
    
    function d3MrnaHist(div,mrna) {
        var hist = mrna.hist;
        var category = mrna.category;
        var width = 40,
            height = 12;
        var x = d3.scale.linear()
            .domain([0, 6])
            .range([0, width]);
        var y = d3.scale.linear()
            .domain([0, d3.max(hist)])
            .range([height, 0]);
        var svg = d3.select(div).append("svg")
            .attr("width", width)
            .attr("height", height)
            .append("g");
        var bar = svg.selectAll(".bar")
            .data(hist)
            .enter().append("g")
            .attr("class", "bar")
            .attr("transform", function(d,i) { return "translate(" + x(i) + "," + y(d) + ")"; });
        bar.append("rect")
            .attr("x", 1)
            .attr("width", x(1)-2)
            .attr("height", function(d) { return height - y(d); })
            .attr("fill", "gray")
            .attr("stroke", function(d,i) { return i!==category?"gray":(i<3?"blue":"red")});
    }
    
    numPatientInSameCnaProfile = <%=numPatientInSameCnaProfile%>;
    
    $(document).ready(function(){
        $('#cna_wrapper_table').hide();
        $('#cna_id_filter_msg').hide();
        var params = {<%=PatientView.SAMPLE_ID%>:caseIdsStr,
            <%=PatientView.CNA_PROFILE%>:cnaProfileId
        };
        
        if (mrnaProfileId) {
            params['<%=PatientView.MRNA_PROFILE%>'] = mrnaProfileId;
        }
        
        if (drugType) {
            params['<%=PatientView.DRUG_TYPE%>'] = drugType;
        }
                        
        $.post("cna.json", 
            params,
            function(data){
                determineOverviewCNAs(data);
                genomicEventObs.cnas.setData(data);
                
                genomicEventObs.fire('cna-built');
                
                // summary table
                buildCnaDataTable(genomicEventObs.cnas, genomicEventObs.cnas.getEventIds(true),
                        'cna_summary_table','<"H"<"cna-summary-table-name">fr>t<"F"<"cna-show-more"><"datatable-paging"pl>>',25, "No CNA events of interest");
                var numFiltered = genomicEventObs.cnas.getNumEvents(true);
                var numAll = genomicEventObs.cnas.getNumEvents(false);
                if (numAll>0) {
                    $('.cna-show-more').html("<a href='#cna' onclick='switchToTab(\"tab_cna\");return false;' \n\
                        title='Show more copy number alterations of this patient'>Show all "
                        +numAll+" CNAs</a>");
                    $('.cna-show-more').addClass('datatable-show-more');
                } 
                $('.cna-summary-table-name').html(
                    "CNA of interest"
                    +(numAll==0?"":(" ("
                        +numFiltered
                        +" of <a href='#cna' onclick='switchToTab(\"tab_cna\");return false;'\n\
                         title='Show more copy number alterations of this patient'>"
                        +numAll
                        +"</a>)"))
                     +" <img id='cna-summary-help' src='images/help.png'\n\
                     title='This table contains copy number altered genes that are \n\
                     <ul><li>either annotated cancer genes</li>\n\
                     <li>or recurrently copy number altered, namely\n\
                        <ul><li>contained in a Gistic peak with less than 10 genes and Q < 0.05, if Gistic results are available</li>\n\
                        <li>otherwise, altered in >5% of samples in the study with &ge; 50 samples.</li></ul></li></ul>' alt='help' />");
                $('#cna-summary-help').qtip({
                    content: { attr: 'title' },
                    style: { classes: 'qtip-light qtip-rounded' },
                    position: { my:'top center',at:'bottom center',viewport: $(window) }
                });
                $('.cna-summary-table-name').addClass("datatable-name");
                $('#cna_summary_wrapper_table').show();
                $('#cna_summary_wait').remove();
                
                // cna
                buildCnaDataTable(genomicEventObs.cnas, genomicEventObs.cnas.getEventIds(false),
                        'cna_table', '<"H"<"all-cna-table-name">fr>t<"F"<"datatable-paging"pil>>', 100, "No CNA events");
                $('.all-cna-table-name').html(
                    ""+genomicEventObs.cnas.getNumEvents()+" copy number altered genes");
                $('.all-cna-table-name').addClass("datatable-name");
                $('#cna_wrapper_table').show();
                $('#cna_wait').remove();
            }
            ,"json"
        );
    });
    
    var patient_view_gistic_qvalue_threhold = 0.05;
    var patient_view_gistic_number_genes_threshold = 10;
    var patient_view_cnaaltrate_threhold = 0.05;
    var patient_view_cnaaltrate_apply_cohort_count = 50;
    function determineOverviewCNAs(data) {
        var overview = [];
        var len = data['id'].length;
        var cancerGene = data['cancer-gene'];
        var gistic = data['gistic'];
        var altrate = data['altrate'];
        
        var noGistic = true;
        for (var i=0; i<len; i++) {
            if (gistic[i]) {
                noGistic = false;
                break;
            }
        }
        
        for (var i=0; i<len; i++) {
            if (cancerGene[i]) {
                overview.push(true);
                continue;
            }
            
            if (noGistic) {
                var rate = 0;
                if (altrate[i][-2]) rate += altrate[i][-2];
                if (altrate[i][2]) rate += altrate[i][2];
                
                if (numPatientInSameCnaProfile>=patient_view_cnaaltrate_apply_cohort_count
                  && rate/numPatientInSameCnaProfile>=patient_view_cnaaltrate_threhold) {
                    overview.push(true);
                    continue;
                }
            } else {
                if (gistic[i]&&gistic[i][0]<=patient_view_gistic_qvalue_threhold
                        &&gistic[i][1]<=patient_view_gistic_number_genes_threshold) {
                    overview.push(true);
                    continue;
                }
            }
            
            overview.push(false);
        }
        data['overview'] = overview;
    }
    
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

<div id="cna_wait"><img src="images/ajax-loader.gif" alt="loading" /></div>
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
