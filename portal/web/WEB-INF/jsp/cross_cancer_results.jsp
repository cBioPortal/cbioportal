<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>

<%
    String siteTitle = SkinUtil.getTitle();
    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle);
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);

    ArrayList<CancerStudy> cancerStudiesWithMutations = new ArrayList<CancerStudy>(),
                           cancerStudiesWithOutMutations = new ArrayList<CancerStudy>();
    for(CancerStudy cancerStudy: cancerStudies) {
        if(cancerStudy.hasMutationData()) {
            cancerStudiesWithMutations.add(cancerStudy);
        } else {
            cancerStudiesWithOutMutations.add(cancerStudy);
        }
    }

    // Now let's reorder the loads
    cancerStudies.clear();
    cancerStudies.addAll(cancerStudiesWithMutations);
    cancerStudies.addAll(cancerStudiesWithOutMutations);

    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();
    String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);

    // Infer whether there is multiple genes or not (for histogram switching)
    int geneCount = 0;
    if(geneList.contains(":")) {
        for (String line : geneList.split("\\r?\\n")) {
            for (String token : line.trim().split(";")) {
                if(token.trim().length() > 0)
                    geneCount++;
            }
        }
    } else {
        for (String words : geneList.split(" ")) {
            for (String token : words.split("\\r?\\n")) {
                if(token.trim().length() > 0)
                    geneCount++;
            }
        }
    }

    boolean multipleGenes = geneCount > 1;

    //  Prepare gene list for URL.
    //  Extra spaces must be removed.  Otherwise AJAX Load will not work.
    geneList = Utilities.appendSemis(geneList);
    geneList = geneList.replaceAll("\\s+", " ");
    geneList = URLEncoder.encode(geneList);

%>

<jsp:include page="global/header.jsp" flush="true"/>

<%
    //  Iterate through each Cancer Study
    //  For each cancer study, init AJAX
    //  This is to prevent Chrome crash when loading the page
    String studiesList = "";
    String studiesNames = "";
    assert(cancerStudies.size() > 0);
    for (CancerStudy cancerStudy:  cancerStudies) {
        studiesList += "'" + cancerStudy.getCancerStudyStableId() + "',";
        studiesNames += "'" + cancerStudy.getName() + "',";
    }
    studiesList = studiesList.substring(0, studiesList.length()-1);
    studiesNames = studiesNames.substring(0, studiesNames.length()-1);
%>

<script type="text/javascript" src="http://www.google.com/jsapi"></script>
<script type="text/javascript">
    google.load("visualization", "1", {packages:["corechart"]});
    var genesQueried = "";
    var shownHistogram = 1;
    var multipleGenes = <%=multipleGenes%>;
    var maxAlterationPercent = 0;
    var lastStudyLoaded = false;


    $(document).ready(function() {
        $("#crosscancer_summary_message").hide();

        $("#chart_div2").toggle();
        $("#chart_div3").toggle();
        $("#chart_div4").toggle();
        function toggleHistograms() {
	        var histIndex = $("#hist_toggle_box").val();
            $("#chart_div1").hide();
            $("#chart_div2").hide();
            $("#chart_div3").hide();
            $("#chart_div4").hide();

	        $("#chart_div" + histIndex).show();
            shownHistogram = histIndex;
            drawChart();
        }
        $("#hist_toggle_box").change( toggleHistograms );

        var histogramData = new google.visualization.DataTable();
        var histogramData2 = new google.visualization.DataTable();
        var histogramData3 = new google.visualization.DataTable();
        var histogramData4 = new google.visualization.DataTable();

        var histogramChart = new google.visualization.ColumnChart(document.getElementById('chart_div1'));
        var histogramChart2 = new google.visualization.ColumnChart(document.getElementById('chart_div2'));
        var histogramChart3 = new google.visualization.ColumnChart(document.getElementById('chart_div3'));
        var histogramChart4 = new google.visualization.ColumnChart(document.getElementById('chart_div4'));

        var cancerStudies = [<%=studiesList%>];
        var cancerStudyNames = [<%=studiesNames%>];
        var numOfStudiesWithMutData = <%=cancerStudiesWithMutations.size()%>;

        if(!multipleGenes) {
            histogramData.addColumn('string', 'Cancer Study');
            histogramData.addColumn('number', 'Multiple Alterations');
            histogramData.addColumn('number', 'Mutation');
            histogramData.addColumn('number', 'Deletion');
            histogramData.addColumn('number', 'Amplification');

            histogramData2.addColumn('string', 'Cancer Study');
            histogramData2.addColumn('number', 'Multiple Alterations');
            histogramData2.addColumn('number', 'Mutation');
            histogramData2.addColumn('number', 'Deletion');
            histogramData2.addColumn('number', 'Amplification');

            histogramData3.addColumn('string', 'Cancer Study');
            histogramData3.addColumn('number', 'Multiple Alterations');
            histogramData3.addColumn('number', 'Mutation');
            histogramData3.addColumn('number', 'Deletion');
            histogramData3.addColumn('number', 'Amplification');
            histogramData3.addColumn('number', 'Not altered');

            histogramData4.addColumn('string', 'Cancer Study');
            histogramData4.addColumn('number', 'Multiple Alterations');
            histogramData4.addColumn('number', 'Mutation');
            histogramData4.addColumn('number', 'Deletion');
            histogramData4.addColumn('number', 'Amplification');
            histogramData4.addColumn('number', 'Not altered');
        } else {
            histogramData.addColumn('string', 'Cancer Study');
            histogramData.addColumn('number', 'Altered Cases');

            histogramData2.addColumn('string', 'Cancer Study');
            histogramData2.addColumn('number', 'Altered Cases');

            histogramData3.addColumn('string', 'Cancer Study');
            histogramData3.addColumn('number', 'Altered Cases');
            histogramData3.addColumn('number', 'Not Altered Cases');

            histogramData4.addColumn('string', 'Cancer Study');
            histogramData4.addColumn('number', 'Altered Cases');
            histogramData4.addColumn('number', 'Not Altered Cases');
        }

        for(var i=0; i < cancerStudies.length; i++) {
            if(i < numOfStudiesWithMutData ) {
                if(!multipleGenes) {
                    histogramData.addRow([cancerStudyNames[i], 0, 0, 0, 0]);
                    histogramData3.addRow([cancerStudyNames[i], 0, 0, 0, 0, 0]);
                } else {
                    histogramData.addRow([cancerStudyNames[i], 0]);
                    histogramData3.addRow([cancerStudyNames[i], 0, 0]);
                }
            } else {
                if(!multipleGenes) {
                    histogramData2.addRow([cancerStudyNames[i], 0, 0, 0, 0]);
                    histogramData4.addRow([cancerStudyNames[i], 0, 0, 0, 0, 0]);
                } else {
                    histogramData2.addRow([cancerStudyNames[i], 0]);
                    histogramData4.addRow([cancerStudyNames[i], 0, 0]);
                }
            }

        }

        drawChart();

        $("#histogram_sort").tipTip();
        $("#histogram_sort").click(function(event) {
            event.preventDefault(); // Not to scroll to the top
            sortPermanently = !sortPermanently;

            $(this).css({
                color: !sortPermanently ? "#1974b8" : "gray",
                "text-decoration": !sortPermanently ? "none" : "line-through"
            });

            drawChart();
        });

        $("#toggle_query_form").tipTip();

        loadStudiesWithIndex(0);
        function formatPercent(number) {
            return parseFloat(number.toFixed(1));
        }

        function updateHistograms(bundleIndex, cancerID) {
            var alts = eval("GENETIC_ALTERATIONS_SORTED_" + cancerID
                    + ".get('GENETIC_ALTERATIONS_SORTED_" + cancerID + "')");

            if( genesQueried.length == 0 ) {
                for(var k=0; k < alts.length; k++) {
                    genesQueried += alts[k].hugoGeneSymbol + ", ";
                }

                genesQueried = genesQueried.substr(0, genesQueried.length-2);
                var genesBold = $("<b>").html(genesQueried);
                var geneStr = " for gene";
                if(alts.length > 1)
                    geneStr += "s";

                $("#queried-genes").html(geneStr + " ").append(genesBold);
            }

            var numOfCases = alts[0].alterations.length;
            var numOfMuts = 0;
            var numOfDels = 0;
            var numOfAmp = 0;
            var numOfCombo = 0

            for(var i=0; i < numOfCases; i++) {
                var isMut = false;
                var isAmp = false;
                var isDel = false;

                var altCnt = 0;
                for(var j=0; j < alts.length; j++) {
                    var alt = alts[j].alterations[i].alteration;

                    if( alt & MUTATED ) {
                        isMut = true;
                        altCnt++;
                    }
                    if(alt & CNA_AMPLIFIED) {
                        isAmp = true;
                        altCnt++;
                    }
                    if(alt & CNA_HOMODELETED) {
                        isDel = true;
                        altCnt++
                    }
                }

                if(altCnt > 1)
                    numOfCombo++;
                else if(altCnt == 1) {
                    if(isAmp) {numOfAmp++;}
                    if(isDel) {numOfDels++}
                    if(isMut) {numOfMuts++}
                }
            }

            var numOfAltered = numOfMuts+numOfDels+numOfAmp+numOfCombo;

            var hist1, hist2;
            if(bundleIndex < numOfStudiesWithMutData) {
                hist1 = histogramData;
                hist2 = histogramData3;
            } else {
                hist1 = histogramData2;
                hist2 = histogramData4;
                bundleIndex = bundleIndex - numOfStudiesWithMutData;
            }

            if(!multipleGenes) {
                hist1.setValue(bundleIndex, 1, formatPercent((numOfCombo/numOfCases) * 100.0));
                hist1.setValue(bundleIndex, 2, formatPercent((numOfMuts/numOfCases) * 100.0));
                hist1.setValue(bundleIndex, 3, formatPercent((numOfDels/numOfCases) * 100.0));
                hist1.setValue(bundleIndex, 4, formatPercent((numOfAmp/numOfCases) * 100.0));
                tmpTotal = hist1.getValue(bundleIndex, 1) + hist1.getValue(bundleIndex, 2) + hist1.getValue(bundleIndex, 3) + hist1.getValue(bundleIndex, 4);
                if(maxAlterationPercent < tmpTotal) {
                    maxAlterationPercent = tmpTotal;
                    maxAlterationPercent = Math.ceil(maxAlterationPercent/10) * 10;
                }

                hist2.setValue(bundleIndex, 1, numOfCombo);
                hist2.setValue(bundleIndex, 2, numOfMuts);
                hist2.setValue(bundleIndex, 3, numOfDels);
                hist2.setValue(bundleIndex, 4, numOfAmp);
                hist2.setValue(bundleIndex, 5, numOfCases-numOfAltered);
            } else {
                hist1.setValue(bundleIndex, 1, formatPercent((numOfAltered/numOfCases) * 100.0));
                if(maxAlterationPercent < hist1.getValue(bundleIndex, 1)) {
                    maxAlterationPercent = hist1.getValue(bundleIndex, 1);
                    maxAlterationPercent = Math.ceil(maxAlterationPercent/10) * 10;
                }
                hist2.setValue(bundleIndex, 1, numOfAltered);
                hist2.setValue(bundleIndex, 2, numOfCases-numOfAltered);
            }

	        if(bundleIndex == 0 || bundleIndex % 2 == 0 || bundleIndex == numOfStudiesWithMutData-1 || bundleIndex == cancerStudies.length-1)
	    	    drawChart();

        }

        function loadStudiesWithIndex(bundleIndex) {
            if(bundleIndex >= cancerStudies.length) {
                $("#crosscancer_summary_loading").fadeOut();
                $("#crosscancer_summary_message").fadeIn();
                lastStudyLoaded = true;
                return;
            }

            var cancerID = cancerStudies[bundleIndex];
            $("#study_" + cancerID)
                .load('cross_cancer_summary.do?gene_list=<%= geneList %>&cancer_study_id=' + cancerID,
                        function() {

                            setTimeout(function() {
                                updateHistograms(bundleIndex, cancerID);
                            }, 760);

                            $("#crosscancer_summary_loading_done").html("" + (bundleIndex+1));
                            loadStudiesWithIndex(bundleIndex+1);
                        }
                 );
        }

        function sumSort(dataView, skipLast) {
            var numOfRows = dataView.getNumberOfRows();
            var numOfCols = dataView.getNumberOfColumns();
            if(skipLast) {
                numOfCols--;
            }
            var rowIndex = [];
            for(var i=0; i < numOfRows; i++)
                rowIndex.push(i);

            rowIndex.sort(function(a, b) {
                var sumA = 0;
                var sumB = 0;
                for(var j=1; j < numOfCols; j++) {
                    sumA += dataView.getValue(a, j);
                    sumB += dataView.getValue(b, j);
                }

                return sumB-sumA;
            });

            return rowIndex;
        }

       var sortPermanently = false;
       function drawChart() {
           var histogramView = new google.visualization.DataView(histogramData);
           var histogramView2 = new google.visualization.DataView(histogramData2);
           var histogramView3 = new google.visualization.DataView(histogramData3);
           var histogramView4 = new google.visualization.DataView(histogramData4);

           if(sortPermanently) {
               var skipLast = shownHistogram > 2;

               var hv = !skipLast ? histogramView : histogramView3;
               var hv2 = !skipLast ? histogramView2 : histogramView4;

               var sortedIndex = sumSort(hv, skipLast);
               histogramView.setRows(sortedIndex);
               histogramView3.setRows(sortedIndex);
               var sortedIndex2 = sumSort(hv2, skipLast);
               histogramView2.setRows(sortedIndex2);
               histogramView4.setRows(sortedIndex2);
           }

           var options = {
              title: 'Percent Sample Alteration for Each Cancer Study w/ Mutation Data (' + genesQueried + ')',
              hAxis: {title: 'Cancer Study'},
              colors: ['#aaaaaa', '#008000', '#002efa', '#ff2617'],
              legend: {
                position: 'bottom'
              },
              hAxis: {
                slantedTextAngle: 45
              },
              vAxis: {
                    title: 'Percent Altered',
                    maxValue: lastStudyLoaded ? maxAlterationPercent : 100,
                    minValue: 0
              },
    	      animation: {
                  duration: 750,
                  easing: 'linear'
    	      },
              isStacked: true
            };

            histogramChart.draw(histogramView, options);
            
	    var options2 = {
              title: 'Percent Sample Alteration for Each Cancer Study w/ Mutation Data (' + genesQueried + ')',
              hAxis: {title: 'Cancer Study'},
              colors: ['#aaaaaa', '#008000', '#002efa', '#ff2617'],
              legend: {
                position: 'bottom'
              },
              hAxis: {
                slantedTextAngle: 45
              },
              vAxis: {
	            title: 'Percent Altered',
                maxValue: lastStudyLoaded ? maxAlterationPercent : 100,
                minValue: 0
              },
              animation: {
                    duration: 750,
                    easing: 'linear'
      	      },
              isStacked: true

            };

            histogramChart2.draw(histogramView2, options2);

            var options3 = {
              title: 'Number of Altered Cases for Each Cancer Study w/ Mutation data (' + genesQueried + ')',
              hAxis: {title: 'Cancer Study'},
              colors: multipleGenes ? ['#aaaaaa', '#eeeeee'] : ['#aaaaaa',  '#008000', '#002efa', '#ff2617', '#eeeeee'],
              legend: {
                position: 'bottom'
              },
              animation: {
                duration: 750,
                easing: 'linear'
        	  },
              hAxis: {
                slantedTextAngle: 45
              },
              yAxis: {
                title: 'Number of cases'
              },
              isStacked: true
            };

            histogramChart3.draw(histogramView3, options3);
            
	    var options4 = {
              title: 'Number of Altered Cases for Each Cancer Study w/o Mutation Data (' + genesQueried + ')',
              hAxis: {title: 'Cancer Study'},
              colors: multipleGenes ? ['#aaaaaa', '#eeeeee'] : ['#aaaaaa',  '#008000', '#002efa', '#ff2617', '#eeeeee'],
              legend: {
                position: 'bottom'
              },
              hAxis: {
                slantedTextAngle: 45
              },
              animation: {
                duration: 750,
                easing: 'linear'
          	  },
	          yAxis: {
	      	    title: 'Number of cases'
	          },
              isStacked: true
            };

            histogramChart4.draw(histogramView4, options4);
       }
    });
</script>

<table>
    <tr>
        <td>

            <div id="results_container">

                <div class="ui-state-highlight ui-corner-all">
                    <p id="crosscancer_summary_message"><span class="ui-icon ui-icon-info"
                             style="float: left; margin-right: .3em; margin-left: .3em"></span>
                        Results are available for <strong><%= (cancerStudies.size()) %>
                        cancer studies</strong>. Click each cancer study below to view a summary of
                        results<span id="queried-genes"></span>.
                    </p>
                    <p id="crosscancer_summary_loading">
                        <img src='images/ajax-loader2.gif' style="margin-right: .6em; margin-left: 1.0em">
                        Loading summaries for cancer studies...
                        (<span id="crosscancer_summary_loading_done">0</span>/<%=cancerStudies.size()%> done)
                    </p>
                </div>

                <p><a href=""
                      title="Modify your original query.  Recommended over hitting your browser's back button."
                      id="toggle_query_form">
                    <span class='query-toggle ui-icon ui-icon-triangle-1-e'
                          style='float:left;'></span>
                    <span class='query-toggle ui-icon ui-icon-triangle-1-s'
                          style='float:left; display:none;'></span><b>Modify Query</b></a>

                <p/>

                <div style="margin-left:5px;display:none;" id="query_form_on_results_page">
                    <%@ include file="query_form.jsp" %>
                </div>

                <br/>
                <hr align="left" class="crosscancer-hr"/>
                <h1 class="crosscancer-header">Summary for All Cancer Studies</h1>
                <br/>
                <br/>

                <div id="historam_toggle" style="text-align: right; padding-right: 125px">
                    <select id="hist_toggle_box">
                        <option value="1">Show percent of altered cases (studies w/ mutation data)</option>
                        <option value="2">Show percent of altered cases (studies w/o mutation data)</option>
                        <option value="3">Show number of altered cases (studies w/ mutation data)</option>
                        <option value="4">Show number of altered cases (studies w/o mutation data)</option>
                    </select>
                    |
                    <a href="#" id="histogram_sort" title="Sorts/unsorts histograms by alteration in descending order">Sort</a>
                </div>
                <div id="chart_div1" style="width: 975px; height: 400px;"></div>
                <div id="chart_div2" style="width: 975px; height: 400px;"></div>
                <div id="chart_div3" style="width: 975px; height: 400px;"></div>
                <div id="chart_div4" style="width: 975px; height: 400px;"></div>
                <br/>
                <br/>

                <hr align="left" class="crosscancer-hr"/>
                <h1 class="crosscancer-header">Details for Each Cancer Study</h1>
                <br/>

                <jsp:include page="global/small_onco_print_legend.jsp" flush="true"/>

                <script>
                    var windowTmp = this;

                    jQuery(document).ready(function() {
                        $(".sortable").sortable({connectWith: '.sortable'});

                        $('#accordion .head').click(function() {
                            //  This toggles the next element, right after head,
                            //  which is the accordion ajax panel
                            $(this).next().toggle();
                            //  This toggles the ui-icons within head
                            jQuery(".ui-icon", this).toggle();
							// redraw oncoprint (TBD: only draw on opening)
							eval("DrawOncoPrintHeader(ONCOPRINT_" + this.id +
							", LONGEST_LABEL_" + this.id + ".get('LONGEST_LABEL_" + this.id +
							"'), HEADER_VARIABLES_" + this.id + ", false)");
							eval("DrawOncoPrintBody(ONCOPRINT_" + this.id +
							", LONGEST_LABEL_" + this.id + ".get('LONGEST_LABEL_" + this.id +
							"'), GENETIC_ALTERATIONS_SORTED_" + this.id +
							".get('GENETIC_ALTERATIONS_SORTED_" + this.id + "'), false)");
                            return false;
                        }).next().hide();

                        $(".movable-icon").tipTip();

                        var oq2Id = "oql2";
                        var oqTopLoc = $("#oncoquery-legend").position().top;
                        var oqClone = $("#oncoquery-legend").clone().attr("id", oq2Id);
                        $("#oncoquery-legend").after(oqClone);
                        oqClone.hide();

                        $.fn.showOnScroll = function() {
                            var $this = this,
                                $window = $(windowTmp);

                            $window.scroll(function(e){
                                if ($window.scrollTop() > oqTopLoc) {
                                    $this.fadeIn();
                                    $this.css({
                                        position: 'fixed',
                                        top: 0,
                                        margin: 0,
                                        'padding-top': '15px',
                                        'padding-bottom': '15px',
                                        border: '2px solid #777777'
                                    });
                                } else {
                                    $this.fadeOut();
                                }
                            });
                        };

                        oqClone.showOnScroll();
                    });
                </script>




                <div id="accordion">
                    <h2 class="cross_cancer_header">Studies with Mutation Data</h2>
                    <div class="sortable">
                    <% outputCancerStudies(cancerStudiesWithMutations, out); %>
                    <% if( !cancerStudiesWithOutMutations.isEmpty() ) {
                    %>
                    </div>
                    <div class="sortable">
                    <h2 class="cross_cancer_header">Studies without Mutation Data</h2>
                    <%
                            outputCancerStudies(cancerStudiesWithOutMutations, out);
                       }
                     %>
                    </div>
                </div>

            </div>
            <!-- end results container -->
        </td>
    </tr>
</table>
</div>
</td>
</tr>
<tr>
    <td colspan="3">
        <jsp:include page="global/footer.jsp" flush="true"/>
    </td>
</tr>
</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true"/>

</body>
</html>

<%!
    private void outputCancerStudies(ArrayList<CancerStudy> cancerStudies,
            JspWriter out) throws IOException {
        for (CancerStudy cancerStudy : cancerStudies) {
            out.println("<div class='accordion_panel'>");
            out.println("<h1 class='head' id=\"" + cancerStudy.getCancerStudyStableId() + "\">");

            //  output triangle icons
            //  the float:left style is required;  otherwise icons appear on their own line.
            out.println("<span class='ui-icon ui-icon-triangle-1-e' style='float:left;'></span>");
            out.println("<span class='ui-icon ui-icon-triangle-1-s'"
                    + " style='float:left;display:none;'></span>");
            out.println(cancerStudy.getName());
            out.println("<span class='ui-icon ui-icon-triangle-2-n-s movable-icon' style='float:right;'"
                    + " title='You can drag this box and drop it to anywhere on the list.'></span>");
            out.println("<span class='percent_altered' id='percent_altered_" + cancerStudy.getCancerStudyStableId()
                    + "' style='float:right'><img src='images/ajax-loader2.gif'></span>");
            out.println("</h1>");
            out.println("<div class='accordion_ajax' id=\"study_"
                    + cancerStudy.getCancerStudyStableId() + "\">");
            out.println("</div>");
            out.println("</div>");
        }
    }
%>
