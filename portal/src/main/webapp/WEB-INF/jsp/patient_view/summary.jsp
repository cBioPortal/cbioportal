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

<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoSample" %>
<%@ page import="org.mskcc.cbio.portal.model.Sample" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<style type="text/css" title="currentStyle">
#genomic-overview-tip {
    position : absolute;
    border : 0px solid gray;
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
String linkToCancerStudy = GlobalProperties.getLinkToCancerStudyView(cancerStudy.getCancerStudyStableId());
%>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/src/patient-view/genomic-overview.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    var dataAttr = {};
    var dataArr = [];
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        if (<%=noData%>) {
            $('div#genomics-overview').html("No mutation or copy number profile data is available for this tumor.");
            return;
        }
        
        $('#cna_summary_wrapper_table').hide();
        if (showGenomicOverview) initGenomicsOverview();
    });

    function initGenomicsOverview() {
        var chmInfo = new ChmInfo();

        var genomic_overview_length = $("#td-content").width() - 75;
        genomic_overview_length -= (hasAlleleFrequencyData&&caseIds.length>0 ? 110 : 0);
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
            <%=PatientView.SAMPLE_ID%>:caseIdsStr,
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
</script>

<style>
#mutation_summary_table tbody tr div {
    white-space: nowrap;
}
</style>

<%if(showTimeline){%>
<jsp:include page="clinical_timeline.jsp" flush="true" />
<br/>
<%}%>

<%if(showGenomicOverview){%>
<fieldset id="patient-view-summary-fieldset" class="ui-widget-content">
<legend>Genomic Overview</legend>
<table>
    <tr>
        <td><div id="genomics-overview"></div></td>
        <%if(hasAlleleFrequencyData && caseIds.size() > 1) {%>
        <td><table id="mutation-count-graphs">
                <tr>
                    <td valign="b">
                        <span style="float: right;" id="allele-freq-plot-thumbnail"></span>
                    </td>
                </tr>
            </table>
        </td>
        <%} else {%>
        <td valign="b">
            <span style="float: left;" id="allele-freq-plot-thumbnail"></span>
        </td>      
        <%}%>
    </tr>
</table>

<div id="allele-freq-plot-big" style="display:none;">
    <label>
        <input id="allelefreq_histogram_toggle" type="checkbox" checked />histogram
    </label>
    <label>
        <input id="allelefreq_curve_toggle" type="checkbox" checked />density estimation
    </label>
    <br/>
</div>
</fieldset>
<br/>
<%}%>
<%if(hasAlleleFrequencyData && caseIds.size() >= 1) {%>
<script type="text/javascript" src="js/src/patient-view/AlleleFreqPlot.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">
    $(document).ready(function() {
        genomicEventObs.subscribeMut(function()  {

            var thumbnail = document.getElementById('allele-freq-plot-thumbnail');
            // create a small plot thumbnail

            var processed_data = {};
            for (var i=0; i<caseIds.length; i++) {
                var pd = AlleleFreqPlotUtils.extract_and_process(genomicEventObs, caseIds[i]);
                if (!pd) {
                    // data failed validation, stop the train
                    continue;
                }
                processed_data[caseIds[i]] = pd;
            }

            AlleleFreqPlotMulti(thumbnail, processed_data,
                {width: 62 , height: 64, label_font_size: "6.5px", xticks: 0, yticks: 0,
                    margin: {bottom: 15}, nolegend:true
                });

            // make the curve lighter
            var thumbnail = document.getElementById('allele-freq-plot-thumbnail');
            $(thumbnail).find('.curve').attr('stroke-width', '1px');

            // create a plot on a hidden element
            var hidden_plot_id = '#allele-freq-plot-big';
            // NECESSARY HACK FOR FIREFOX: 
            // Firefox can't handle the method 'SVGLocatable.getBBox()', which 
            // is used in rendering the legend for AlleleFreqPlotMulti, when
            // the element it's called on is currently hidden. Thus, we must
            // show the element while that's called, then hide it again to be revealed by Qtip.
            // This sucks but is necessary unless we can replace the call to getBBox, which
            // I could not figure out a cleaner way than this to do.
            // - Adam Abeshouse (adama@cbio.mskcc.org)
            $(hidden_plot_id).show()
            window.allelefreqplot = AlleleFreqPlotMulti($(hidden_plot_id)[0], processed_data, null, window.caseMetaData.label);            
            $(hidden_plot_id).hide()

            // add qtip on allele frequency plot thumbnail
            $(thumbnail).qtip({
                content: {text: '<div id="qtip-allele-freq-plot-big"></div>'},
                events: {
                    render: (function(numCaseIds) {
                        return function(event, api) {
                        // bind toggle_histogram to toggle histogram button
                        // AFTER we've shuffled it around 
                        window.allele_freq_plot_histogram_toggle = true; // initialize toggle state
                        $('#allelefreq_histogram_toggle').click(function() {
                            // qtip interferes with $.toggle
                            window.allele_freq_plot_histogram_toggle = !window.allele_freq_plot_histogram_toggle;
                            if (window.allele_freq_plot_histogram_toggle) {
                                $('.viz_hist').show();
                            }
                            else {
                                $('.viz_hist').hide();
                            }
                        });

                        window.allele_freq_plot_curve_toggle = true;
                        $('#allelefreq_curve_toggle').click(function() {
                            // qtip interferes with $.toggle
                            window.allele_freq_plot_curve_toggle = !window.allele_freq_plot_curve_toggle;
                            if (window.allele_freq_plot_curve_toggle) {
                                $('.viz_curve').show();
                            }
                            else {
                                $('.viz_curve').hide();
                            }
                        });
                        if ($("#qtip-allele-freq-plot-big").children().length === 0) {
                            $("#qtip-allele-freq-plot-big").append(window.allelefreqplot);
                            $(window.allelefreqplot).show();
                        }
                        if (numCaseIds > 1) {
                            $("#qtip-allele-freq-plot-big").parent().parent().addClass("qtip-wide");
                        }
                    };
                })(caseIds.length)
                },
	            show: {event: "mouseover"},
                hide: {fixed: true, delay: 100, event: "mouseout"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow', tip: false},
                //position: {my:'left top',at:'bottom center'}
                position: {my:'top right',at:'top left',viewport: $(window)}
            });
        });
    });
</script>
<%}%>


<table cellpadding="0" cellspacing="0" border="0" width="100%">
<%if(showMutations){%>
<tr valign="top">
<td>
<div id="mutation_summary_wait"><img src="images/ajax-loader.gif" alt="loading" /> Loading mutations ...</div>
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
<div id="cna_summary_wait"><img src="images/ajax-loader.gif" alt="loading" /> Loading copy number alterations ...</div>
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
