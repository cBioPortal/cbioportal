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
.patient-view-scatter-qtip-wide {
    max-width: 600px !important;
}
</style>

<%
String linkToCancerStudy = GlobalProperties.getLinkToCancerStudyView(cancerStudy.getCancerStudyStableId());
%>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/src/patient-view/genomic-overview.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cancer-study-view/scatter-plot-mut-cna.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/cancer-study-view/load-clinical-data.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript" src="js/src/co-exp/components/ScatterPlots.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    var dataAttr = {};
    var dataArr = [];
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        if (<%=noData%>) {
            $('div#genomics-overview').html("No mutation or copy number profile data is available for this tumor.");
            $('#mut-cna-scatter').hide();
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
        loadMutCountCnaFrac(null, cancerStudyId,
            <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>',
            hasCnaSegmentData,
            function(dt){

                //start to get the plot values
                dataArr = [];
                var tempObj;
                var tempX = [], tempY = [];
                var highlightDot = {};
                _.each(dt.Gf, function(item){

                    if(item.c[0].v !== null && item.c[1].v !== null && item.c[2].v !== null)
                    {
                        tempObj = {};
                        tempObj.case_id = item.c[0].v;
                        tempObj.x_val = item.c[2].v;
                        tempObj.y_val = item.c[1].v;
                        if(item.c[0].v === caseIdsStr)
                        {
                            highlightDot.fill = 'red';
                            highlightDot.case_id = item.c[0].v;
                            highlightDot.x_val = item.c[2].v;
                            highlightDot.y_val = item.c[1].v;
                        }
                        else
                        {
                            tempX.push(item.c[2].v);
                            tempY.push(item.c[1].v);
                            dataArr.push(tempObj);
                        }


                    }
                });

                //push the highlight dot to the data array last
                tempX.push(highlightDot.x_val);
                tempY.push(highlightDot.y_val);
                dataArr.push(highlightDot);

                dataAttr = {min_x: Math.min.apply(Math, tempX), max_x: Math.max.apply(Math, tempX), min_y: Math.min.apply(Math, tempY), max_y: Math.max.apply(Math, tempY), profile_name: "profile name test"};


                //the end to get plot values
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
            style: { classes: 'qtip-light qtip-rounded patient-view-scatter-qtip-wide' },
            position: {my:'center right',at:'center left'},
            events: {
                render: function(event, api) {
                    openBigGraph();
                }
            }
        }
        $('#'+scatterPlotDiv).qtip(params);
    }

    function openBigGraph(){

        var plotsOpts = {"style":{"fill":"#58ACFA","stroke":"#0174DF","stroke_width":"0.1","size":"50","shape":"circle",
            "mutations":{"gene_x_mutate_fill":"#DBA901","gene_x_mutate_stroke":"#886A08","gene_y_mutate_fill":"#F5A9F2","gene_y_mutate_stroke":"#F7819F","gene_both_mutate_fill":"#FF0000","gene_both_mutate_stroke":"#B40404"}},
            "canvas":{"width":440,"height":350,"xLeft":80,"xRight":430,"yTop":10,"yBottom":300},
            "elem":{"svg":"","xScale":"","yScale":"","xAxis":"","yAxis":"","dotsGroup":"","axisGroup":"","axisTitleGroup":"","brush":""},
            "names":{"header":"coexp_plot_BRCA1_headerNew","body":"coexp_plot_BRCA1_bodyNew","div":"coexp_plot_BRCA1","loading_img":"coexp_plot_BRCA1_loading_img","log_scale_x":"coexp_plot_BRCA1_log_scale_x","log_scale_y":"coexp_plot_BRCA1_log_scale_y","show_mutations":"coexp_plot_BRCA1_show_mutations","download_pdf":"coexp_plot_BRCA1_download_pdf","download_svg":"coexp_plot_BRCA1_download_svg","control_panel":"coexp_plot_BRCA1undefined"},
            "text":{"xTitle":"Fraction of copy number altered genome","yTitle":"Mutation Count","xTitleHelp":"mRNA and microRNA Z-scores merged: mRNA expression Z-scores compared to diploid tumors (diploid for each gene), median values from all three mRNA expression platforms; and miRNA z-Scores compared to all tumours.","yTitleHelp":"mRNA and microRNA Z-scores merged: mRNA expression Z-scores compared to diploid tumors (diploid for each gene), median values from all three mRNA expression platforms; and miRNA z-Scores compared to all tumours.","title":"mRNA co-expression: BRCA1 vs. NBR2  ","fileName":"co_expression_result-BRCA1-NBR2",
                "legends":{"gene_x_mut":"gene_x mutated","gene_y_mut":"gene_y mutated","gene_both_mut":"Both mutated","non_mut":"Neither mutated"}}};



                $("#case-id-div").empty();
                $("#case-id-div").append("<div id='" + plotsOpts.names.body + "'></div>");

                var coexpPlotsBig = new ScatterPlots();

                coexpPlotsBig.initBig(plotsOpts, dataArr, dataAttr, false);

        $('#mut_cna_more_plot_msg').hide();


    }

    function scatterPlotMutVsCna(dt,hLog,vLog,scatterPlotDiv,caseIdDiv) {

        var plotsOpts = {"style":{"fill":"#58ACFA","stroke":"#0174DF","stroke_width":"1.2","size":"20","shape":"circle",
            "mutations":{"gene_x_mutate_fill":"#DBA901","gene_x_mutate_stroke":"#886A08","gene_y_mutate_fill":"#F5A9F2","gene_y_mutate_stroke":"#F7819F","gene_both_mutate_fill":"#FF0000","gene_both_mutate_stroke":"#B40404"}},
            "canvas":{"width":100,"height":100,"xLeft":10,"xRight":90,"yTop":10,"yBottom":90},
            "elem":{"svg":"","xScale":"","yScale":"","xAxis":"","yAxis":"","dotsGroup":"","axisGroup":"","axisTitleGroup":"","brush":""},
            "names":{"header":"coexp_plot_BRCA1_header","body":"coexp_plot_BRCA1_body","div":"coexp_plot_BRCA1","loading_img":"coexp_plot_BRCA1_loading_img","log_scale_x":"coexp_plot_BRCA1_log_scale_x","log_scale_y":"coexp_plot_BRCA1_log_scale_y","show_mutations":"coexp_plot_BRCA1_show_mutations","download_pdf":"coexp_plot_BRCA1_download_pdf","download_svg":"coexp_plot_BRCA1_download_svg","control_panel":"coexp_plot_BRCA1undefined"},
            "text":{"xTitle":"Fraction of copy...","yTitle":"Mutation Count","xTitleHelp":"","yTitleHelp":"","title":"Scatter Plot","fileName":"",
                "legends":{"gene_x_mut":"gene_x mutated","gene_y_mut":"gene_y mutated","gene_both_mut":"Both mutated","non_mut":"Neither mutated"}},"legends":[{"fill":"#DBA901","stroke":"#886A08","size":"20","shape":"circle","stroke_width":"1.2","text":"BRCA1 mutated"},
                {"fill":"#58ACFA","stroke":"#0174DF","size":"20","shape":"circle","stroke_width":"1.2","text":"Neither mutated"}]};


        $("#" + scatterPlotDiv).empty();
        $("#" + scatterPlotDiv).append("<div id='" + plotsOpts.names.body + "'></div>");

        var coexpPlots = new ScatterPlots();
        coexpPlots.initSmall(plotsOpts, dataArr, dataAttr, false);

        $('#mut_cna_more_plot_msg').hide();

    }
</script>

<style>
fieldset.fieldset-border {
    /*border: 0px solid #ccc !important;
    -webkit-box-shadow:  0px 0px 0px 0px #000;
            box-shadow:  0px 0px 0px 0px #000;*/
}

legend.legend-border {
    font-size: 12px !important;
    text-align: left !important;
    width:auto;
    padding:0 10px;
    border-bottom:none;
    color:#1974b8;
    margin-bottom: 0px;
}
#mutation_summary_table tbody tr div {
    white-space: nowrap;
}
</style>

<%if(showTimeline){%>
<jsp:include page="clinical_timeline.jsp" flush="true" />
<br/>
<%}%>

<%if(showGenomicOverview){%>
<fieldset class="fieldset-border">
<legend class="legend-border">Genomic Overview</legend>
<table>
    <tr>
        <td><div id="genomics-overview"></div></td>
        <%if(hasAlleleFrequencyData && caseIds.size() > 1) {%>
        <td><table id="mutation-count-graphs">
                <tr>
                    <td valign="top">
                        <span style="float: right;" id="mut-cna-scatter"><img src="images/ajax-loader.gif"/></span>
                    </td>        
                </tr>
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
        <td valign="top">
            <span style="float: right;" id="mut-cna-scatter"><img src="images/ajax-loader.gif"/></span>
        </td>        
        <%}%>
    </tr>
</table>

<div id="mut_cna_scatter_dialog" title="Mutation VS Copy Number Alteration" style="display:none; width:600; height:600;font-size: 11px; text-align: center;.ui-dialog {padding: 0em;};">
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
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow', tip: false},
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
