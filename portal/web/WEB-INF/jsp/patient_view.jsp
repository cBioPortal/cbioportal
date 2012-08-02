<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.portal.servlet.DrugsJSON" %>
<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>


<%
request.setAttribute("standard_js_only", true);
String patient = (String)request.getAttribute(PatientView.PATIENT_ID);
String patientViewError = (String)request.getAttribute(PatientView.ERROR);
String patientInfo = (String)request.getAttribute(PatientView.PATIENT_INFO);
String diseaseInfo = (String)request.getAttribute(PatientView.DISEASE_INFO);
String patientStatus = (String)request.getAttribute(PatientView.PATIENT_STATUS);
CancerStudy cancerStudy = (CancerStudy)request.getAttribute(PatientView.CANCER_STUDY);

GeneticProfile mutationProfile = (GeneticProfile)request.getAttribute(PatientView.MUTATION_PROFILE);
boolean showMutations = mutationProfile!=null;

GeneticProfile cnaProfile = (GeneticProfile)request.getAttribute(PatientView.CNA_PROFILE);
boolean showCNA = cnaProfile!=null;

String isDemoMode = request.getParameter("demo");
boolean showPlaceHoder;
if (isDemoMode!=null) {
    showPlaceHoder = isDemoMode.equalsIgnoreCase("on");
} else {
    showPlaceHoder = SkinUtil.showPlaceholderInPatientView();
}

boolean showPathways = showMutations | showCNA;
boolean showSimilarPatient = showMutations | showCNA;
boolean showGenomicOverview = showMutations | showCNA;

double[] genomicOverviewCopyNumberCnaCutoff = SkinUtil.getPatientViewGenomicOverviewCnaCutoff();

int numPatientInSameStudy = 0;
int numPatientInSameMutationProfile = 0;
int numPatientInSameCnaProfile = 0;

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

<jsp:include page="global/header.jsp" flush="true" />

<table width="100%">
    <tr>
        <td ncol="2"><b><u><%=patientInfo%></u></b></td>
    </tr>
    <tr>
        <td><%=diseaseInfo%></td>
        <td align="right"><%=patientStatus%></td>
    </tr>
</table>


<div id="patient-tabs">
    <ul>
        
    <li><a href='#summary' class='patient-tab' title='Events of Interest'>Summary</a></li>
    
    <%if(showMutations){%>
    <li><a href='#mutations' class='patient-tab' title='Mutations'>Mutations</a></li>
    <%}%>
    
    <%if(showCNA){%>
    <li><a href='#cna' class='patient-tab' title='Copy Number Alterations'>Copy Number Alterations</a></li>
    <%}%>

    <%if(showPathways){%>
    <li><a href='#pathways' class='patient-tab' title='Pathway View'>Network</a></li>
    <%}%>
    
    <%if(showSimilarPatient){%>
    <li><a href='#similar-patients' class='patient-tab' title='Similar Patients'>Similar Patients</a></li>
    <%}%>
    
    </ul>

    <div class="patient-section" id="summary">
        <%@ include file="patient_view/summary.jsp" %>
    </div>

    <%if(showMutations){%>
    <div class="patient-section" id="mutations">
        <%@ include file="patient_view/mutations.jsp" %>
    </div>
    <%}%>

    <%if(showCNA){%>
    <div class="patient-section" id="cna">
        <%@ include file="patient_view/cna.jsp" %>
    </div>
    <%}%>

    <%if(showPathways){%>
    <div class="patient-section" id="pathways">
        <%@ include file="patient_view/pathways.jsp" %>
    </div>
    <%}%>

    <%if(showSimilarPatient){%>
    <div class="patient-section" id="similar-patients">
        <%@ include file="patient_view/similar_patients.jsp" %>
    </div>
    <%}%>

</div>
    
<div id="drugs_dialog" title="Drugs" style="font-size: 11px; .ui-dialog {padding: 0em;};">
    <img id='drugs-loader-img' src="images/ajax-loader.gif"/>
    <table id="drugs_table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Target Genes</th>
                <th>Name</th>
                <th>Synonyms</th>
                <th>FDA Approved?</th>
                <th>Description</th>
                <th>Data Source</th>
            </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
</div>
<%  
}
%>
        </div>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />    

<script type="text/javascript" src="js/patient-view/genomic-event-observer.js"></script>
<script type="text/javascript">
$(document).ready(function(){
    setUpPatientTabs();
    initTabs();
    initDrugDialog();
});

function setUpPatientTabs() {
    $('#patient-tabs').tabs();
    $('#patient-tabs').show();
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

function initDrugDialog() {
    $('#drugs_dialog').dialog({autoOpen: false,
        modal: true,
        minHeight: 200,
        maxHeight: 600,
        height: 400,
        minWidth: 300,
        width: 800
        });
}

function openDrugDialog(drugIds) {
    $('#drugs_dialog').dialog('open');
    $('#drugs-loader-img').show();
    $('drugs_table').hide();
    var params = {
        <%=DrugsJSON.DRUG_IDS%>: drugIds
    };

    $.post("drugs.json", 
        params,
        function(drugs){
            $('#drugs_table').dataTable( {
                "sDom": '<"H"fr>t<"F"<"datatable-paging"pil>>',
                "bJQueryUI": true,
                "bDestroy": true,
                "aaData": drugs,
                "aoColumnDefs":[
                    {// data source
                        "aTargets": [ 4 ],
                        "fnRender": function(obj) {
                            return obj.aData[ obj.iDataColumn ]?"Yes":"No";
                        }
                    },
                    {// data source
                        "aTargets": [ 6 ],
                        "fnRender": function(obj) {
                            var source = obj.aData[ obj.iDataColumn ];
                            if (source.toLowerCase()!="drugbank") return source;
                            var drugId = obj.aData[ 0 ];
                            return "<a href=\"http://www.drugbank.ca/drugs/"+drugId+"\" target=\"_blank\">"+ source + "</a>";
                        }
                    }
                ],
                "oLanguage": {
                    "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                    "sInfoFiltered": "",
                    "sLengthMenu": "Show _MENU_ per page"
                },
                "iDisplayLength": 25,
                "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
            } );

            $('#drugs_table').css("width","100%");
            $('#drugs_table').show();
            $('#drugs-loader-img').remove();
        }
        ,"json"
    );
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

var cnaEventIds = null;
var overviewCnaEventIds = null;
var mutEventIds = null;
var overviewMutEventIds = null;
var overviewCnaGenes = null;
var overviewMutGenes = null;

var placeHolder = <%=Boolean.toString(showPlaceHoder)%>;

function getDrugMap(drugs) {
    var map = {};
    for (var gene in drugs) {
        var d = drugs[gene];
        var strDrugs = d.join(',');
        map[gene] = "<a href=\"#\" onclick=\"openDrugDialog('"+strDrugs+"'); return false;\">"+d.length+" drug"+(d.length>1?"s":"")+"</a>";
    }
    return map;
}

function trimHtml(html) {
    return html.replace(/<[^>]*>/g,"");
}

function idRegEx(ids) {
    return "(^"+ids.join("$)|(^")+"$)";
}

var caseId = '<%=patient%>';
var geObs =  new GenomicEventObserver(<%=showMutations%>,<%=showCNA%>);

</script>

</body>
</html>