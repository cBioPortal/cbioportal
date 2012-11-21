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
.ui-tooltip, .qtip{
	position: absolute;
	left: -10000em;
	top: -10000em;
 
	max-width: 600px; /* Change this? */
	min-width: 50px; /* ...and this! */
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
%>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/patient-view/genomic-overview.js"></script>
<script type="text/javascript" src="js/cancer-study-view/scatter-plot-mut-cna.js"></script>
<script type="text/javascript" src="js/cancer-study-view/load-clinical-data.js"></script>
<script type="text/javascript">
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        if (<%=noData%>) {
            $('div#summary').html("No mutation or copy number profile data is available for this tumor.");
            return;
        }
        
        $('#mutation_summary_wrapper_table').hide();
        $('#cna_summary_wrapper_table').hide();
        if (!genomicEventObs.hasMut||!genomicEventObs.hasSeg) $('#mut-cna-scatter').hide();
        if (showGenomicOverview) initGenomicsOverview();
        if (genomicEventObs.hasMut&&genomicEventObs.hasSeg) {
            loadMutCnaAndPlot("mut-cna-scatter");
            addMutCnaPlotTooltip("mut-cna-scatter");
        }
            
    });

    function initGenomicsOverview() {
        var chmInfo = new ChmInfo();
        var config = new GenomicOverviewConfig((genomicEventObs.hasMut?1:0)+(genomicEventObs.hasSeg?1:0),$("#td-content").width()-(genomicEventObs.hasMut&&genomicEventObs.hasSeg?150:50));
        config.cnTh = [<%=genomicOverviewCopyNumberCnaCutoff[0]%>,<%=genomicOverviewCopyNumberCnaCutoff[1]%>];
        var paper = createRaphaelCanvas("genomics-overview", config);
        plotChromosomes(paper,config,chmInfo);
        if (genomicEventObs.hasMut) {
            genomicEventObs.subscribeMut(function(){
                plotMuts(paper,config,chmInfo,genomicEventObs.hasSeg?1:0,genomicEventObs.mutations);
            });
        }
        
        if (genomicEventObs.hasSeg) {
            plotCopyNumberOverview(paper,config,chmInfo,genomicEventObs.hasMut);
        }
    }
    
    function plotCopyNumberOverview(paper,config,chmInfo,hasMut) {
        var params = {
            <%=CnaJSON.CMD%>:'<%=CnaJSON.GET_SEGMENT_CMD%>',
            <%=PatientView.PATIENT_ID%>:'<%=patient%>',
            cancer_study_id: cancerStudyId
        };

        $.post("cna.json", 
            params,
            function(segs){
                plotCnSegs(paper,config,chmInfo,0,segs,1,2,3,5);
            }
            ,"json"
        );
    }

    var mutCnaScatterDialogLoaded = false;
    function openMutCnaScatterDialog() {
        if (!mutCnaScatterDialogLoaded) {
            if (mutationProfileId==null) {
                alert('no mutation data');
                return;
            }
                
            if (!hasCnaSegmentData) {
                alert('no copy number segment data');
                return;
            }
            
            $('#mut_cna_more_plot_msg').hide();
            
            loadMutCnaAndPlot('mut-cna-scatter-plot','case-id-div');
            
            mutCnaScatterDialogLoaded = true;
        }
    }
    
    function loadMutCnaAndPlot(scatterPlotDiv,caseIdDiv) {
        loadMutCountCnaFrac(<%=jsonCaseIds%>, cancerStudyId,
            <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>',
            hasCnaSegmentData,
            function(dt){
                var maxMut = dt.getColumnRange(1).max;
                var vLog = maxMut>1000;
                if (vLog) $('#mut-cna-vaxis-log').attr('checked',true);
                scatterPlotMutVsCna(dt,false,vLog,scatterPlotDiv,caseIdDiv);

                $('.mut-cna-config').show();

                $(".mut-cna-axis-log").change(function() {
                    var hLog = $('#mut-cna-haxis-log').is(":checked");
                    var vLog = $('#mut-cna-vaxis-log').is(":checked");
                    scatterPlotMutVsCna(dt,hLog,vLog,scatterPlotDiv,caseIdDiv);
                });
                $('#mut_cna_more_plot_msg').show();
            }
        );
    }
    
    function addMutCnaPlotTooltip(scatterPlotDiv) {
        var params = {
            content: $('#mut_cna_scatter_dialog').remove(),
            show: { delay: 200 },
            hide: { fixed: true, delay: 100 },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-wide' },
            position: {my:'top right',at:'top left'},
            events: {
                render: function(event, api) {
                    openMutCnaScatterDialog();
                }
            }
        }
        $('#'+scatterPlotDiv).qtip(params);
    }
    
    function scatterPlotMutVsCna(dt,hLog,vLog,scatterPlotDiv,caseIdDiv) {
        var scatter = plotMutVsCna(null,scatterPlotDiv,caseIdDiv,cancerStudyId,dt,caseId,2,1,null,hLog,vLog);
        google.visualization.events.addListener(scatter, 'select', function(e){
            var s = scatter.getSelection();
            if (s.length>1) return;
            if (caseIdDiv) {
                var caseId = s.length==0 ? null : dt.getValue(s[0].row,0);
                $('#case-id-div').html(formatPatientLink(caseId,cancerStudyId));
            }
        });
    }
</script>


<%if(showPlaceHoder){%>
<br/>Clinical timeline goes here...
<br/><br/>
<%}%>

<%if(showGenomicOverview){%>
<table>
    <tr>
        <td><div id="genomics-overview"></div></td>
        <td><div id="mut-cna-scatter"><img src="images/ajax-loader.gif"/></div></td>
    </tr>
</table>

<div id="mut_cna_scatter_dialog" title="Mutation VS Copy Number Alteration" style="display:none; width:600; height:600;font-size: 11px; text-align: left;.ui-dialog {padding: 0em;};">
    <%@ include file="../cancer_study_view/mut_cna_scatter_plot.jsp" %>
    <p id='mut_cna_more_plot_msg'>Each dot represents a tumor sample in <a href='<%=linkToCancerStudy%>'><%=cancerStudy.getName()%></a>.<p>
</div>
<%}%>

<table cellpadding="0" cellspacing="0" border="0" width="100%">
<tr valign="top">
<%if(showMutations){%>
<td width="49%">
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
</td>
<%}if(showMutations&&showCNA){%>
<td>&nbsp;</td>
<%}if(showCNA){%>
<td width="49%">
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
</td>
<%}%>
</tr>
</table>
