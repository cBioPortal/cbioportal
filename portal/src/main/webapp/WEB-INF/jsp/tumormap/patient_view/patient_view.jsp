<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.DrugsJSON" %>
<%@ page import="org.mskcc.cbio.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.json.simple.JSONValue" %>


<%
boolean print = "1".equals(request.getParameter("print"));
request.setAttribute("tumormap", true);
String patient = (String)request.getAttribute(PatientView.PATIENT_ID);
String patientViewError = (String)request.getAttribute(PatientView.ERROR);
String patientInfo = (String)request.getAttribute(PatientView.PATIENT_INFO);
String diseaseInfo = (String)request.getAttribute(PatientView.DISEASE_INFO);
String patientStatus = (String)request.getAttribute(PatientView.PATIENT_STATUS);
CancerStudy cancerStudy = (CancerStudy)request.getAttribute(PatientView.CANCER_STUDY);
String jsonClinicalData = JSONValue.toJSONString((Map<String,String>)request.getAttribute(PatientView.CLINICAL_DATA));
List<String> tissueImages = (List<String>)request.getAttribute(PatientView.TISSUE_IMAGES);
String otherStudy = (String)request.getAttribute(PatientView.OTHER_STUDIES_WITH_SAME_PATIENT_ID);
boolean showTissueImages = tissueImages!=null && !tissueImages.isEmpty();
String pathReportUrl = (String)request.getAttribute(PatientView.PATH_REPORT_URL);

String drugType = request.getParameter("drug_type");

GeneticProfile mutationProfile = (GeneticProfile)request.getAttribute(PatientView.MUTATION_PROFILE);
boolean showMutations = mutationProfile!=null;

GeneticProfile cnaProfile = (GeneticProfile)request.getAttribute(PatientView.CNA_PROFILE);
boolean showCNA = cnaProfile!=null;

GeneticProfile mrnaProfile = (GeneticProfile)request.getAttribute(PatientView.MRNA_PROFILE);

String isDemoMode = request.getParameter("demo");
boolean showPlaceHoder;
if (isDemoMode!=null) {
    showPlaceHoder = isDemoMode.equalsIgnoreCase("on");
} else {
    showPlaceHoder = SkinUtil.showPlaceholderInPatientView();
}

boolean showPathways = showPlaceHoder & (showMutations | showCNA);
boolean showSimilarPatient = showPlaceHoder & (showMutations | showCNA);

boolean hasCnaSegmentData = ((Boolean)request.getAttribute(PatientView.HAS_SEGMENT_DATA));
boolean showGenomicOverview = showMutations | hasCnaSegmentData;
boolean showClinicalTrials = true;
boolean showDrugs = true;

double[] genomicOverviewCopyNumberCnaCutoff = SkinUtil.getPatientViewGenomicOverviewCnaCutoff();

int numPatientInSameStudy = 0;
int numPatientInSameMutationProfile = 0;
int numPatientInSameCnaProfile = 0;

boolean noData = cnaProfile==null & mutationProfile==null;

String mutationProfileStableId = null;
String cnaProfileStableId = null;
String mrnaProfileStableId = null;
if (mutationProfile!=null) {
    mutationProfileStableId = mutationProfile.getStableId();
}
if (cnaProfile!=null) {
    cnaProfileStableId = cnaProfile.getStableId();
}
if (mrnaProfile!=null) {
    mrnaProfileStableId = mrnaProfile.getStableId();
}

if (patientViewError!=null) {
    out.print(patient);
    out.print(": ");
    out.println();
    out.print(patientViewError);
} else {
    numPatientInSameStudy = (Integer)request.getAttribute(PatientView.NUM_CASES_IN_SAME_STUDY);
    if (mutationProfile!=null) {
        numPatientInSameMutationProfile = (Integer)request.getAttribute(
                PatientView.NUM_CASES_IN_SAME_MUTATION_PROFILE);
    }
    if (cnaProfile!=null) {
        numPatientInSameCnaProfile = (Integer)request.getAttribute(
                PatientView.NUM_CASES_IN_SAME_CNA_PROFILE);
    }
%>

<jsp:include page="../../global/header.jsp" flush="true" />

<%if(otherStudy!=null) {%>
    <p style="background-color: lightyellow;"><%=otherStudy%></p>
<%}%>

<table width="100%">
    <tr>
        <td><b><u><%=patient%></u></b>&nbsp;&nbsp;<%=patientInfo%></td>
        <td align="right"><a href="#" id="more-clinical-a">More about this patient</a></td>
    </tr>
    <tr>
        <td><%=diseaseInfo%></td>
        <td align="right"><%=patientStatus%></td>
    </tr>
</table>


<div id="patient-tabs">
    <ul>
        
    <li><a href='#summary' class='patient-tab'>Summary</a></li>
    
    <%if(showMutations){%>
    <li><a href='#mutations' class='patient-tab'>Mutations</a></li>
    <%}%>
    
    <%if(showCNA){%>
    <li><a href='#cna' class='patient-tab'>Copy Number Alterations</a></li>
    <%}%>

    <%if(showDrugs){%>
    <li><a href='#drugs' class='patient-tab'>Drugs</a></li>
    <%}%>

    <%if(showClinicalTrials){%>
    <li><a href='#clinical-trials' class='patient-tab'>Clinical Trials</a></li>
    <%}%>
    
    <%if(showTissueImages){%>
    <li><a href='#images' class='patient-tab'>Tissue Images</a></li>
    <%}%>
    
    <%if(pathReportUrl!=null){%>
    <li><a href='#path-report' class='patient-tab'>Pathology Report</a></li>
    <%}%>

    <%if(showPathways){%>
    <li><a href='#pathways' class='patient-tab'>Network</a></li>
    <%}%>
    
    <%if(showSimilarPatient){%>
    <li><a href='#similar-patients' class='patient-tab'>Similar Patients</a></li>
    <%}%>

    </ul>

    <div class="patient-section" id="summary">
        <%@ include file="summary.jsp" %>
    </div>

    <%if(showMutations){%>
    <div class="patient-section" id="mutations">
        <%@ include file="mutations.jsp" %>
    </div>
    <%}%>

    <%if(showCNA){%>
    <div class="patient-section" id="cna">
        <%@ include file="cna.jsp" %>
    </div>
    <%}%>

    <%if(showTissueImages){%>
    <div class="patient-section" id="images">
        <%@ include file="tissue_images.jsp" %>
    </div>
    <%}%>

    <%if(pathReportUrl!=null){%>
    <div class="patient-section" id="path-report">
        <%@ include file="path_report.jsp" %>
    </div>
    <%}%>

    <%if(showPathways){%>
    <div class="patient-section" id="pathways">
        <%@ include file="pathways.jsp" %>
    </div>
    <%}%>

    <%if(showSimilarPatient){%>
    <div class="patient-section" id="similar-patients">
        <%@ include file="similar_patients.jsp" %>
    </div>
    <%}%>

    <%if(showDrugs){%>
    <div class="patient-section" id="drugs">
        <%@ include file="drugs.jsp" %>
    </div>
    <%}%>

    <%if(showClinicalTrials){%>
        <div class="patient-section" id="clinical-trials">
            <%@ include file="clinical_trials.jsp" %>
        </div>
    <%}%>

</div>
<%  
}
%>
        </div>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="../../global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="../../global/xdebug.jsp" flush="true" />

<link href="css/jquery.qtip.min.css" type="text/css" rel="stylesheet"/>

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css";
        @import "css/data_table_ColVis.css";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .dataTables_filter {
                width: 40%;
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
        .gene_mutation_percent_div {
                display: block;
                float: left;
                background-color: lightgreen;
                height: 12px;
        }
        .mutation_percent_div {
                display: block;
                float: left;
                background-color: green;
                height: 12px;
        }
        .amp_percent_div {
                display: block;
                float: left;
                background-color: red;
                height: 12px;
        }
        .del_percent_div {
                display: block;
                float: left;
                background-color: blue;
                height: 12px;
        }
        .left_float_div {
                display: block;
                float: left;
        }
        .right_float_div {
                display: block;
                float: right;
        }
        .ui-tooltip-wide {
            max-width: 600px;
        }
        .datatable-name {
                float: left;
                font-weight: bold;
                font-size: 120%;
                vertical-align: middle;
        }
        .datatable-show-more {
            float: left;
        }
</style>

<script type="text/javascript" src="js/src/patient-view/genomic-event-observer.js"></script>
<script type="text/javascript">

var print = <%=print%>;
var placeHolder = <%=Boolean.toString(showPlaceHoder)%>;
var mutationProfileId = <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>';
var cnaProfileId = <%=cnaProfileStableId==null%>?null:'<%=cnaProfileStableId%>';
var mrnaProfileId = <%=mrnaProfileStableId==null%>?null:'<%=mrnaProfileStableId%>';
var hasCnaSegmentData = <%=hasCnaSegmentData%>;
var showGenomicOverview = <%=showGenomicOverview%>;
var caseId = '<%=patient%>';
var cancerStudyName = "<%=cancerStudy.getName()%>";
var cancerStudyId = '<%=cancerStudy.getCancerStudyStableId()%>';
var genomicEventObs =  new GenomicEventObserver(<%=showMutations%>,<%=showCNA%>, hasCnaSegmentData);
var drugType = drugType?'<%=drugType%>':null;

$(document).ready(function(){
    if (print) $('#page_wrapper_table').css('width', '900px');
    setUpPatientTabs();
    initTabs();
});

function setUpPatientTabs() {
    $('#patient-tabs').tabs();
    $('#patient-tabs').show();
    addMoreCinicalTooltip();
    fixCytoscapeWebRedraw();
}

function initTabs() {
    var tabContainers = $('.patient-section');
    tabContainers.hide().filter(':first').show();

    $('.patient-tab').click(function () {
            tabContainers.hide();
            tabContainers.filter(this.hash).show();
            $('.patient-tab').removeClass('selected');
            $(this).addClass('selected');
            return false;
    }).filter(':first').click();   
}

function fixCytoscapeWebRedraw() {
    // to initially hide the network tab
    $("#pathways").attr('style', 'display: none !important; height: 0px; width: 0px; visibility: hidden;');
    
    // to fix problem of flash repainting
    $("a.patient-tab").click(function(){
        if($(this).attr("href")=="#pathways") {
            $("#pathways").removeAttr('style');
        } else {
            $("#pathways").attr('style', 'display: block !important; height: 0px; width: 0px; visibility: hidden;');
        }
    });
}

function switchToTab(toTab) {
    $('.patient-section').hide();
    $('.patient-section#'+toTab).show();
    $('#patient-tabs').tabs('select',$('#patient-tabs ul a[href="#'+toTab+'"]').parent().index());
}

function getEventString(eventTableData,dataCol,overviewCol) {
    var s = [];
    for (var i=0; i<eventTableData.length; i++) {
        if (overviewCol==null || eventTableData[i][overviewCol])
            s.push(eventTableData[i][dataCol]);
    }
    return s.join(",");
}

function getEventIndexMap(eventTableData,idCol) {
    var m = {};
    for (var i=0; i<eventTableData.length; i++) {
        m[eventTableData[i][idCol]] = i;
    }
    return m;
}
    
function addNoteTooltip(elem, content) {
    $(elem).qtip({
        content: (typeof variable === 'undefined' ? {attr: 'alt'} : content),
        hide: { fixed: true, delay: 100 },
        style: { classes: 'ui-tooltip-light ui-tooltip-rounded' },
        position: {my:'top left',at:'bottom center'}
    });
}

function addMoreCinicalTooltip() {
    var clinicalDataMap = <%=jsonClinicalData%>;
    var clinicalData = [];
    for (var key in clinicalDataMap) {
        clinicalData.push([key, clinicalDataMap[key]]);
    }
    
    if (clinicalData.length==0) {
        $('#more-clinical-a').remove();
    } else {
        $('#more-clinical-a').qtip({
            content: {
                text: '<table id="more-clinical-table"></table>'
            },
            events: {
                render: function(event, api) {
                    $('#more-clinical-table').dataTable( {
                        "sDom": 't',
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "aaData": clinicalData,
                        "aoColumnDefs":[
                            {
                                "aTargets": [ 0 ],
                                "fnRender": function(obj) {
                                    return '<b>'+obj.aData[ obj.iDataColumn ]+'</b>';
                                }
                            },
                            {
                                "aTargets": [ 1 ],
                                "bSortable": false
                            }
                        ],
                        "aaSorting": [[0,'asc']],
                        "oLanguage": {
                            "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                            "sInfoFiltered": "",
                            "sLengthMenu": "Show _MENU_ per page"
                        },
                        "iDisplayLength": -1
                    } );
                }
            },
            hide: { fixed: true, delay: 100 },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-wide' },
            position: {my:'top right',at:'bottom right'}
        });
    }
}

function addDrugsTooltip(elem, my, at) {
    $(elem).each(function(){
        $(this).qtip({
            content: {
                text: '<img src="images/ajax-loader.gif"/>',
                ajax: {
                    url: 'drugs.json',
                    type: 'POST',
                    data: {<%=DrugsJSON.DRUG_IDS%>: $(this).attr('alt')},
                    success: function(drugs,status) {
                        var txt = [];
                        for (var i=0, n=drugs.length; i<n; i++) {
                            var drug = drugs[i];
                            var txtDrug = [];
                            if (drug[2]) {
                                txtDrug.push("Drug name:</b></td><td><b>"+drug[2]+"</b>");
                            }
                            if (drug[1]) {
                                txtDrug.push("Target:</b></td><td><b>"+drug[1]+"</b>");
                            }
                            if (drug[3]) {
                                txtDrug.push("Synonyms:</b></td><td>"+drug[3]);
                            }
                            if (drug[4]) {
                                txtDrug.push("FDA approved?</b></td><td>"+(drug[4]?"Yes":"No"));
                            }
                            if (drug[5]) {
                                txtDrug.push("Description:</b></td><td>"+drug[5]);
                            }
                            if (drug[7]) { // xref
                                var xref = [];
                                var nci = drug[7]['NCI_Drug'];
                                if (nci) xref.push("<a href='http://www.cancer.gov/drugdictionary?CdrID="+nci+"'>NCI</a>");
                                var pharmgkb = drug[7]['PharmGKB'];
                                if (pharmgkb) xref.push("<a href='http://www.pharmgkb.org/views/index.jsp?objId="+pharmgkb+"'>PharmGKB</a>");
                                var drugbank = drug[7]['DrugBank'];
                                if (drugbank) xref.push("<a href='http://www.drugbank.ca/drugs/"+drugbank+"'>DrugBank</a>");
                                var keggdrug = drug[7]['KEGG Drug'];
                                if (keggdrug) xref.push("<a href='http://www.genome.jp/dbget-bin/www_bget?dr:"+keggdrug+"'>KEGG Drug</a>");
                                
                                if (xref.length) {
                                    txtDrug.push("Data sources:</b></td><td>"+xref.join(",&nbsp;"));
                                }
                            }
                            if (drug[8]>0) {
                                var nci = drug[7]['NCI_Drug'];
                                if (nci) {
                                    txtDrug.push("Clinical Trials:</b></td><td><a href='http://www.cancer.gov/Search/ClinicalTrialsLink.aspx?idtype=1&id="+nci+"'>"+drug[8]+" clinical trial"+(drug[8]>1?"s":"")+"</a>");
                                }
                            }
                            txt.push("<table><tr valign='top'><td nowrap='nowrap'><b>"+txtDrug.join("</td></tr><tr valign='top'><td nowrap='nowrap'><b>")+"</td></tr></table>");
                        }
                        var html = txt.join('<hr><br/>');
                        this.set('content.text', html);
                    }
                }
            },
            hide: { fixed: true, delay: 100 },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-wide' },
            position: { my: my, at: at }
        });
    });
}

/**
* modified from http://jsfiddle.net/H2SKt/1/
**/
function d3PieChart(svg, data, radius, colors) {
    var chart = svg
        .data([data])
        .append("g")
        .attr("transform", "translate(" + radius + "," + radius + ")");

    var arc = d3.svg.arc()
        .outerRadius(radius);

    var pie = d3.layout.pie()
        .value(function(d) { return d; })
        .sort(null);

    var arcs = chart.selectAll("g.slice")
        .data(pie) 
        .enter()
        .append("g")
        .attr("class", "slice");

    arcs.append("path")
        .attr("fill", function(d, i) { return colors[i]; } )
        .attr("d", arc);

    return chart;
}

function d3AccBar(svg, data, width, colors) {
    var acc = [];
    var sum = 0;
    for (var i=0; i<data.length; i++) {
        acc.push(sum);
        sum += data[i];
    }
    
    var vd = [];
    for (var i=0; i<data.length; i++) {
        vd.push({
            start: width*acc[i]/sum,
            width: width*data[i]/sum,
            color: colors[i]
        });
    }

    var chart = svg.selectAll(".bar")
        .data(vd) 
        .enter()
        .append("g")
        .attr("class", "bar")
        .attr("transform", function(d,i) { return "translate(" + d.start + "," + 3 + ")"; });

    chart.append("rect")
        .attr("width", function(d, i) { return d.width; })
        .attr("height", 8)
        .attr("fill", function(d, i) { return d.color; } );

    return chart;
}

function d3CircledChar(g,ch) {
    g.append("circle")
        .attr("r",5)
        .attr("stroke","#55C")
        .attr("fill","none");
    g.append("text")
        .attr("x",-3)
        .attr("y",3)
        .attr("font-size",7)
        .attr("fill","#66C")
        .text(ch);
}
    
function plotMrna(div,alts) {
    $(div).each(function() {
        if (!$(this).is(":empty")) return;
        var gene = $(this).attr("alt");
        var mrna = alts.getValue(gene, 'mrna');
        d3MrnaBar($(this)[0],mrna.perc);
        $(this).qtip({
            content: {text: "mRNA level of the gene in this tumor<br/><b>mRNA z-score</b>: "
                        +mrna.zscore.toFixed(2)+"<br/><b>Percentile</b>: "+mrna.perc+"%"},
            hide: { fixed: true, delay: 10 },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded' },
            position: {my:'top left',at:'bottom center'}
        });
    });
}

function d3MrnaBar(div,mrnaPerc) {
    var textWidth = 30,
        graphWidth = 30,
        circleR = 3,
        width = graphWidth+textWidth+2*circleR,
        height = 12;

    var svg = d3.select(div).append('svg')
        .attr("width", width)
        .attr("height", height);

    svg.append("text")
        .attr("x", width)
        .attr('y',11)
        .attr("text-anchor", "end")
        .attr('font-size',10)
        .text(mrnaPerc+"%");

    var bar = svg.append("g")
                .attr("transform", "translate(" + circleR + "," + 0 + ")");

    bar.append("line")
        .attr("x1",-circleR)
        .attr("y1",height/2)
        .attr("x2",graphWidth+circleR)
        .attr("y2",height/2)
        .attr("style", "stroke:gray;stroke-width:2");

    bar.append("circle")
        .attr("cx", graphWidth * mrnaPerc/100)
        .attr("cy", height/2)
        .attr("r", circleR)
        .attr("fill", mrnaPerc>75 ? "red" : (mrnaPerc<25?"blue":"gray"));

}

function formatPatientLink(caseId,cancerStudyId) {
    return caseId==null?"":'<a title="Go to patient-centric view" href="case.do?case_id='+caseId+'&cancer_study_id='+cancerStudyId+'">'+caseId+'</a>'
}

function trimHtml(html) {
    return html.replace(/<[^>]*>/g,"");
}

function idRegEx(ids) {
    return "(^"+ids.join("$)|(^")+"$)";
}
</script>

</body>
</html>
