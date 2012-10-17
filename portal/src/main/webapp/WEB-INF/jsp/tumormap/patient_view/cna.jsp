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
    
    var cnaTableIndices = {id:0,gene:1,alteration:2,altrate:3,drug:4};
    function buildCnaDataTable(cnas, cnaEventIds, table_id, sDom, iDisplayLength, sEmptyInfo) {
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
                                var gene = cnas.getValue(source[0], "gene");
                                var entrez = cnas.getValue(source[0], "entrez");
                                var tip = "<a href=\"http://www.ncbi.nlm.nih.gov/gene/"
                                    +entrez+"\">NCBI GenBank</a>";
                                var sanger = cnas.getValue(source[0], 'sanger');
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
                                return cnas.getValue(source[0], "gene");
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
                                           +" is Homozygously deleted (putative)'>HOMDEL</span>";
                                    break;
                                default: strAlt='Unknown';
                                }
                                return "<b>"+strAlt+"</b>"
                            } else if (type==='filter') {
                                switch(cnas.getValue(source[0], "alter")) {
                                case 2: return 'AMP';
                                case -2: return 'HOMDEL';
                                default: return 'Unknown';
                                }
                            } else {
                                return cnas.getValue(source[0], "alter");
                            }
                        }
                    },
                    {// context
                        "aTargets": [ cnaTableIndices['altrate'] ],
                        "bSearchable": false,
                        "mDataProp": 
                            function(source,type,value) {
                            if (type==='set') {
                                return;
                            } else if (type==='display') {
                                var con = cnas.getValue(source[0], 'altrate')-1;
                                if (con<=0) return '';
                                var frac = con / numPatientInSameCnaProfile;
                                var strAlt;
                                switch(cnas.getValue(source[0], "alter")) {
                                case -2: strAlt='deleted'; break;
                                case 2: strAlt='amplified'; break;
                                }
                                var alter = cnas.getValue(source[0], "alter");
                                var tip = "<b>"+con+" other sample"+(con==1?"":"s")
                                    +"</b> ("+(100*frac).toFixed(1) + "%)"+" in this study "
                                    +(con==1?"has ":"have ")+strAlt+" "+cnas.getValue(source[0], "gene");
                                var width = Math.min(40, Math.ceil(80 * Math.log(frac+1) * Math.LOG2E));
                                var clas = alter>0?"amp_percent_div":"del_percent_div"
                                var ret = "<div class='"+clas+" "+table_id
                                            +"-tip' style='width:"+width+"px;' alt='"+tip+"'></div>";
                                        
                                // gistic
                                var gistic = cnas.getValue(source[0], 'gistic');
                                if (gistic) {
                                    var tip = "<b>Gistic</b><br/><i>Q-value</i>: "+gistic[0].toPrecision(2)
                                                +"<br/><i>Number of genes in the peak</i>: "+gistic[1];
                                    ret += "<img class='right_float_div "+table_id+"-tip' alt='"
                                        +tip+"' src='images/mutsig.png' width=12 height=12>";
                                }
                                
                                return ret;
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
                    addNoteTooltip("."+table_id+"-tip");
                    addDrugsTooltip("."+table_id+"-drug-tip");
                },
                "aaSorting": [[cnaTableIndices['altrate'],'desc']],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page",
                    "sInfoEmpty": sEmptyInfo
                },
                "iDisplayLength": iDisplayLength,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        } );

        oTable.css("width","100%");
        addNoteTooltip("#"+table_id+" th.cna-header");
        return oTable;
    }
    
    numPatientInSameCnaProfile = <%=numPatientInSameCnaProfile%>;
    
    $(document).ready(function(){
        $('#cna_wrapper_table').hide();
        $('#cna_id_filter_msg').hide();
        var params = {<%=PatientView.PATIENT_ID%>:'<%=patient%>',
            <%=PatientView.CNA_PROFILE%>:cnaProfileId
        };
                        
        $.post("cna.json", 
            params,
            function(data){
                determineOverviewCNAs(data);
                genomicEventObs.cnas.setData(data);
                
                genomicEventObs.fire('cna-built');
                
                // summary table
                buildCnaDataTable(genomicEventObs.cnas, genomicEventObs.cnas.getEventIds(true),
                        'cna_summary_table','<"H"<"cna-summary-table-name">fr>t<"F"<"cna-show-more"><"datatable-paging"pil>>',25, "No CNA events");
                $('.cna-show-more').html("<a href='#cna' onclick='switchToTab(\"cna\");return false;' title='Show more copy number alterations of this patient'>Show all "
                        +genomicEventObs.cnas.getNumEvents(false)+" CNAs</a>");
                $('.cna-summary-table-name').html(
                    "CNA of interest <img class='cna_help' src='images/help.png'\n\
                     title='This table contains genes that are either annotated cancer genes\n\
                     or recurrently copy number altered (contained in a Gistic peak with less than\n\
                     10 genes and Q < 0.05; if Gistic results are not available,\n\
                     genes are altered in >5% of samples in the study).'/>");
                $('#cna_summary_wrapper_table').show();
                $('#cna_summary_wait').remove();
                
                // cna
                buildCnaDataTable(genomicEventObs.cnas, genomicEventObs.cnas.getEventIds(false),
                        'cna_table', '<"H"fr>t<"F"<"datatable-paging"pil>>', 100, "No CNA events of interest");
                $('#cna_wrapper_table').show();
                $('#cna_wait').remove();

                // help
                $('.cna_help').tipTip();
            }
            ,"json"
        );
    });
    
    var patient_view_gistic_qvalue_threhold = 0.05;
    var patient_view_gistic_number_genes_threshold = 10;
    var patient_view_cnaaltrate_threhold = 0.05;
    function determineOverviewCNAs(data) {
        var overview = [];
        var len = data['id'].length;
        var impact = data['impact'];
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
            if (impact[i]) {
                overview.push(true);
                continue;
            }
            
            if (noGistic) {
                var rate = 0;
                if (altrate[i][-2]) rate += altrate[i][-2];
                if (altrate[i][2]) rate += altrate[i][2];
                
                if (rate/numPatientInSameCnaProfile>=patient_view_cnaaltrate_threhold) {
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