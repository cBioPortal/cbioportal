<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoCase" %>
<%@ page import="org.mskcc.cbio.portal.model.Case" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

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
String jsonCaseIdsInStudy = "[]";
if (mutationProfile!=null && hasCnaSegmentData) {
    List<Case> cases = DaoCase.getAllCaseIdsInCancer(cancerStudy.getInternalId());
    List<String> caseIdsInStudy = new ArrayList<String>(cases.size());
    for (Case c : cases) {
        caseIdsInStudy.add(c.getCaseId());
    }
    jsonCaseIdsInStudy = jsonMapper.writeValueAsString(caseIdsInStudy);
}
String linkToCancerStudy = GlobalProperties.getLinkToCancerStudyView(cancerStudy.getCancerStudyStableId());
%>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/src/patient-view/genomic-overview.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cancer-study-view/scatter-plot-mut-cna.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cancer-study-view/load-clinical-data.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        if (<%=noData%>) {
            $('div#summary').html("No mutation or copy number profile data is available for this tumor.");
            return;
        }
        
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

        var genomic_overview_length = $("#td-content").width() - 75;
        genomic_overview_length -= ((genomicEventObs.hasMut && genomicEventObs.hasSeg) ? 110 : 0);
        genomic_overview_length -= (hasAlleleFrequencyData&&caseIds.length===1 ? 110 : 0);
        var config = new GenomicOverviewConfig(
                (genomicEventObs.hasMut?caseIds.length:0)+(genomicEventObs.hasSeg?caseIds.length:0), genomic_overview_length);

        config.cnTh = [<%=genomicOverviewCopyNumberCnaCutoff[0]%>,<%=genomicOverviewCopyNumberCnaCutoff[1]%>];
        var paper = createRaphaelCanvas("genomics-overview", config);
        plotChromosomes(paper,config,chmInfo);
        if (genomicEventObs.hasMut) {
            genomicEventObs.subscribeMut(function(){
                for (var i=0, n=caseIds.length; i<n; i++) {
                    plotMuts(paper,config,chmInfo,i+(genomicEventObs.hasSeg?n:0),genomicEventObs.mutations,n>1?caseIds[i]:null);
                };
            });
        }
        
        if (genomicEventObs.hasSeg) {
            plotCopyNumberOverview(paper,config,chmInfo,genomicEventObs.hasMut);
        }
    }
    
    function plotCopyNumberOverview(paper,config,chmInfo,hasMut) {

        var params = {
            <%=CnaJSON.CMD%>:'<%=CnaJSON.GET_SEGMENT_CMD%>',
            <%=PatientView.CASE_ID%>:caseIdsStr,
            cancer_study_id: cancerStudyId
        };
        $.post("cna.json", 
            params,
            function(segs){
                for (var i=0, n=caseIds.length; i<n; i++) {
                    plotCnSegs(paper,config,chmInfo,i,segs[caseIds[i]],1,2,3,5,n>1?caseIds[i]:null);
                }
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
        loadMutCountCnaFrac(<%=jsonCaseIdsInStudy%>, cancerStudyId,
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
            show: {delay: 200, event: "mouseover" },
            hide: {fixed: true, delay: 100,  event: "mouseout"},
            style: { classes: 'qtip-light qtip-rounded qtip-wide' },
            position: {my:'top right',at:'top left',viewport: $(window)},
            events: {
                render: function(event, api) {
                    openMutCnaScatterDialog();
                }
            }
        }
        $('#'+scatterPlotDiv).qtip(params);
    }

    function scatterPlotMutVsCna(dt,hLog,vLog,scatterPlotDiv,caseIdDiv) {
        var emId = {};
        caseIds.forEach(function(caseId) {
            emId[caseId] = true;}
        );
        
        var scatter = plotMutVsCna(null,scatterPlotDiv,caseIdDiv,cancerStudyId,dt,emId,2,1,null,hLog,vLog);
        google.visualization.events.addListener(scatter, 'select', function(e){
            var s = scatter.getSelection();
            if (s.length>1) return;
            if (caseIdDiv) {
                var caseId = s.length===0 ? null : dt.getValue(s[0].row,0);
                $('#case-id-div').html(formatPatientLink(caseId,cancerStudyId));
            }
        });
    }
</script>


<%if(showTimeline){%>
<jsp:include page="clinical_timeline.jsp" flush="true" />
<br/>
<%}%>

<%if(showGenomicOverview){%>
<fieldset style="border-width: 1px; border-color: #ccc; border-style: solid;">
<legend style="color:#1974b8;">Genomic Overview</legend>
<table>
    <tr>
        <td><div id="genomics-overview"></div></td>
        <td valign="top">
            <span style="float: left;" id="allele-freq-plot-thumbnail"></span>
        </td>
        <td valign="top">
            <span style="float: right;" id="mut-cna-scatter"><img src="images/ajax-loader.gif"/></span>
        </td>
    </tr>
</table>

<div id="mut_cna_scatter_dialog" title="Mutation VS Copy Number Alteration" style="display:none; width:600; height:600;font-size: 11px; text-align: left;.ui-dialog {padding: 0em;};">
    <%@ include file="../cancer_study_view/mut_cna_scatter_plot.jsp" %>
    <p id='mut_cna_more_plot_msg'>Each dot represents a tumor sample in <a href='<%=linkToCancerStudy%>'><%=cancerStudy.getName()%></a>.<p>
</div>

<div id="allele-freq-plot-big" style="display:none;">
    <label>
        <input id="allelefreq_histogram_toggle" type="checkbox" checked />histogram
    </label>
    <label>
        <input id="allelefreq_curve_toggle" type="checkbox" checked />density estimation
    </label>
</div>
</fieldset>
<br/>
<%}%>

<%if(hasAlleleFrequencyData && caseIds.size() == 1) {%>
<script type="text/javascript" src="js/src/patient-view/AlleleFreqPlot.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">
    $(document).ready(function() {
        genomicEventObs.subscribeMut(function()  {

            var thumbnail = document.getElementById('allele-freq-plot-thumbnail');
            // create a small plot thumbnail

            var processed_data = AlleleFreqPlotUtils.extract_and_process(genomicEventObs, caseIds[0]);

            if (!processed_data) {
                // data failed validation, stop the train
                return;
            }

            AlleleFreqPlot(thumbnail, processed_data,
                {width: 62 , height: 64, label_font_size: "7px", xticks: 0, yticks: 0,
                    margin: {bottom: 15}
                });

            // make the curve lighter
            var thumbnail = document.getElementById('allele-freq-plot-thumbnail');
            $(thumbnail).find('.curve').attr('stroke-width', '1px');

            // create a plot on a hidden element
            var hidden_plot_id = '#allele-freq-plot-big';
            window.allelefreqplot = AlleleFreqPlot($(hidden_plot_id)[0], processed_data);

            // add qtip on allele frequency plot thumbnail
            $(thumbnail).qtip({
                content: {text: 'allele frequency plot is broken'},
                events: {
                    render: function(event, api) {
                        // grab the plot
                        var $allelefreqplot = $(allelefreqplot);
                        var content = $allelefreqplot.remove();

                        // and dump it into the qtip
                        content.show();
                        content = content[0].outerHTML;
                        api.set('content.text', content);

                        // bind toggle_histogram to toggle histogram button
                        // AFTER we've shuffled it around 
                        var histogram_toggle = true;        // initialize toggle state
                        $('#allelefreq_histogram_toggle').click(function() {
                            // qtip interferes with $.toggle
                            histogram_toggle = !histogram_toggle;
                            if (histogram_toggle) {
                                $(hidden_plot_id + ' rect').removeAttr('display');
                            }
                            else {
                                $(hidden_plot_id + ' rect').attr('display', 'none');
                            }
                        });

                        var curve_toggle = true;
                        $('#allelefreq_curve_toggle').click(function() {
                            // qtip interferes with $.toggle
                            curve_toggle = !curve_toggle;
                            if (curve_toggle) {
                                $(hidden_plot_id + ' .curve').removeAttr('display');
                            }
                            else {
                                $(hidden_plot_id + ' .curve').attr('display', 'none');
                            }
                        });
                    }
                },
	            show: {event: "mouseover"},
                hide: {fixed: true, delay: 100, event: "mouseout"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow', tip: false},
                //position: {my:'left top',at:'bottom center'}
                position: {my:'top right',at:'top right',viewport: $(window)}
            });
        });
    });
</script>
<%}%>


<table cellpadding="0" cellspacing="0" border="0" width="100%">
<%if(showMutations){%>
<tr valign="top">
<td>
<div id="mutation_summary_wait"><img src="images/ajax-loader.gif"/> Loading mutations ...</div>
<table cellpadding="0" cellspacing="0" border="0" id="mutation_summary_wrapper_table" width="100%" style="display:none;">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_summary_table">
                <%@ include file="mutations_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
<br/>
</td>
</tr>
<%}if(showCNA){%>
<tr valign="top">
<td>
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
</tr>
<%}%>
</table>
