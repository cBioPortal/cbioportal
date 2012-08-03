<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.cgds.dao.DaoCase" %>
<%@ page import="org.mskcc.cbio.cgds.model.Case" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.json.simple.JSONValue" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

<style type="text/css" title="currentStyle">
#genomic-overview-tip {
    position : absolute;
    border : 1px solid gray;
    background-color : #efefef;
    padding : 3px;
    z-index : 1000;
    max-width : 300px;
}
</style>

<%
String jsonCaseIds = "[]";
if (mutationProfile!=null && cnaProfile!=null) {
    List<Case> cases = DaoCase.getAllCaseIdsInCancer(cancerStudy.getInternalId());
    List<String> caseIds = new ArrayList<String>(cases.size());
    for (Case c : cases) {
        caseIds.add(c.getCaseId());
    }
    jsonCaseIds = JSONValue.toJSONString(caseIds);
}
String linkToCancerStudy = SkinUtil.getLinkToCancerStudyView(cancerStudy.getCancerStudyStableId());

String mutationProfileStableId = null;
String cnaProfileStableId = null;
if (mutationProfile!=null) {
    mutationProfileStableId = mutationProfile.getStableId();
}
if (cnaProfile!=null) {
    cnaProfileStableId = cnaProfile.getStableId();
}
%>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/patient-view/genomic-overview.js"></script>
<script type="text/javascript" src="js/cancer-study-view/scatter-plot-mut-cna.js"></script>
<script type="text/javascript" src="js/cancer-study-view/load-clinical-data.js"></script>
<script type="text/javascript">
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        $('#mutation_summary_wrapper_table').hide();
        $('#cna_summary_wrapper_table').hide();
        initGenomicsOverview();
        initMutCnaScatterDialog();
    });

    function initGenomicsOverview() {
        var chmInfo = new ChmInfo();
        var config = new GenomicOverviewConfig((geObs.hasMut?1:0)+(geObs.hasCna?1:0),$("#td-content").width()-50);
        config.cnTh = [<%=genomicOverviewCopyNumberCnaCutoff[0]%>,<%=genomicOverviewCopyNumberCnaCutoff[1]%>];
        var paper = createRaphaelCanvas("genomics-overview", config);
        plotChromosomes(paper,config,chmInfo);
        if (geObs.hasMut) {
            geObs.subscribeMut(function(){
                var muts = $('#mutation_table').dataTable().fnGetData();
                plotMuts(paper,config,chmInfo,geObs.hasCna?1:0,muts,mutTableIndices['chr'],mutTableIndices['start'],mutTableIndices['end'],mutTableIndices['id'],geObs.hasCna);
            });
        }
        
        if (geObs.hasCna) {
            plotCopyNumberOverview(paper,config,chmInfo,geObs.hasMut);
        }
    }
    
    function plotCopyNumberOverview(paper,config,chmInfo,hasMut) {
        var params = {
            <%=CnaJSON.CMD%>:'<%=CnaJSON.GET_SEGMENT_CMD%>',
            <%=PatientView.PATIENT_ID%>:'<%=patient%>'
        };

        $.post("cna.json", 
            params,
            function(segs){
                plotCnSegs(paper,config,chmInfo,0,segs,1,2,3,5,hasMut);
            }
            ,"json"
        );
    }
    
    function initMutCnaScatterDialog() {
        $('#mut_cna_scatter_dialog').dialog({autoOpen: false,
            modal: true,
            minHeight: 200,
            maxHeight: 600,
            height: 550,
            minWidth: 300,
            width: 600
            });
    }

    var mutCnaScatterDialogLoaded = false;
    function openMutCnaScatterDialog() {
        if (!mutCnaScatterDialogLoaded) {
            if (<%=mutationProfileStableId==null%>) {
                alert('no mutation data');
                return;
            }
                
            if (<%=cnaProfileStableId==null%>) {
                alert('no cna data');
                return;
            }
            
            $('#mut_cna_more_plot_msg').hide();
            
            loadMutCountCnaFrac(<%=jsonCaseIds%>,
                <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>',
                <%=cnaProfileStableId==null%>?null:'<%=cnaProfileStableId%>',
                function(dt){
                    var scatter = plotMutVsCna(null,'mut-cna-scatter-plot','case-id-div',dt,2,1,null,false,false);
                    google.visualization.events.addListener(scatter, 'select', function(e){
                        var s = scatter.getSelection();
                        if (s.length>1) return;
                        var caseId = s.length==0 ? null : dt.getValue(s[0].row,0);
                        $('#case-id-div').html(formatPatientLink(caseId));
                    });
                    
                    for (var i=0, rows = dt.getNumberOfRows(); i<rows; i++) {
                        if (dt.getValue(i,0)===caseId) {
                            scatter.setSelection([{'row': i}]);
                            $('#case-id-div').html(formatPatientLink(caseId));
                            break;
                        }
                    }
                    
                    $('#mut-cna-config').show();

                    $(".mut-cna-axis-log").change(function() {
                        var hLog = $('#mut-cna-haxis-log').is(":checked");
                        var vLog = $('#mut-cna-vaxis-log').is(":checked");
                        plotMutVsCna(null,'mut-cna-scatter-plot','case-id-div',dt,2,1,null,hLog,vLog);
                    });
                    $('#mut_cna_more_plot_msg').show();
                }
            );
            mutCnaScatterDialogLoaded = true;
        }
        
        $('#mut_cna_scatter_dialog').dialog('open');

    }
</script>


<%if(showPlaceHoder){%>
<br/>Clinical timeline goes here...
<br/><br/>
<%}%>

<%if(showGenomicOverview){%>
<div id="genomic-overview-tip"></div>
<div id="genomics-overview"></div>
<br/>
<div id="mut_cna_scatter_dialog" title="Drugs" style="font-size: 11px; text-align: left;.ui-dialog {padding: 0em;};">
    <%@ include file="../cancer_study_view/mut_cna_scatter_plot.jsp" %>
    <p id='mut_cna_more_plot_msg'><sup>*</sup>One dot in this plot represents a case/patient in <a href='<%=linkToCancerStudy%>'><%=cancerStudy.getName()%></a>.<p>
</div>
<%}%>
        
<%if(showMutations){%>
<div id="mutation_summary_wait"><img src="images/ajax-loader.gif"/> Loading mutations ...</div>
<table cellpadding="0" cellspacing="0" border="0" id="mutation_summary_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_summary_table">
                <%@ include file="mutations_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
<br/>
<%}%>

<%if(showCNA){%>
<div id="cna_summary_wait"><img src="images/ajax-loader.gif"/> Loading copy number alterations ...</div>
<table cellpadding="0" cellspacing="0" border="0" id="cna_summary_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="cna_summary_table">
                <%@ include file="cna_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
<br/>
<%}%>
