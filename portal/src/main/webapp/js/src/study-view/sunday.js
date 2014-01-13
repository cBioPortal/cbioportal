/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



    function dcFunc(data){
        mrnaExpressionClusterChart = dc.pieChart("#mrna-expression-cluster-chart");
        tumorStage2009Chart = dc.rowChart("#tumor-stage-2009-chart");
        subtypeChart = dc.rowChart("#subtype-chart");
        histologyChart = dc.pieChart("#histology-chart");
        tumorGradeChart = dc.pieChart("#tumor-grade-chart");
        msiStatus7MarkerCallChart = dc.pieChart("#msi-status-7-marker-call-chart");
        msiStatus5MarkerCallChart = dc.pieChart("#msi-status-5-marker-call-chart");
        methylationClusterChart = dc.pieChart("#methylation-cluster-chart");
        mlh1SilencingChart = dc.pieChart("#mlh1-silencing-chart");
        cnaClusterK4Chart = dc.pieChart("#cna-cluster-k4-chart");
        mutationRateClusterChart = dc.pieChart("#mutation-rate-cluster-chart");
        microRnaClusterChart = dc.pieChart("#micro-rna-cluster-chart");
        osStatusChart = dc.pieChart("#os-status-chart");
        dfsStatusChart = dc.pieChart("#dfs-status-chart");
        //data combine group
        dataMafChart = dc.pieChart("#data-maf-chart");
        dataGisticChart = dc.pieChart("#data-gistic-chart");
        dataRnaseqChart = dc.pieChart("#data-rnaseq-chart");
        dataCoreSampleChart = dc.pieChart("#data-core-sample-chart");

        //HTML id map to js id - bar chart
        ageChart = dc.barChart("#age-chart");
        osMonthsChart = dc.barChart("#os-months-chart");
        dfsMonthsChart = dc.barChart("#dfs-months-chart");

        microRnaScoreChart = dc.barChart("#micro-rna-score-chart");

        dataTable = dc.dataTableDataOnly("#dataTable");

        //Bonding all of data into 'all'
        var ndx = crossfilter(data);
        var all = ndx.groupAll();

        //Initial dimensions of each chart
        var mrnaExpressionCluster = ndx.dimension(function (d) {
                return d.MRNA_EXPRESSION_CLUSTER;
        });

        var tumorStage2009 = ndx.dimension(function (d) {
                return d.TUMOR_STAGE_2009;
        });

        var CASEID = ndx.dimension(function (d) {
                return d.CASE_ID;
        });		

        var subtype = ndx.dimension(function (d) {
                return d.SUBTYPE;
        });

        var histology = ndx.dimension(function (d) {
                return d.HISTOLOGY;
        });

        var tumorGrade = ndx.dimension(function (d) {
                return d.TUMOR_GRADE;
        });

        var msiStatus7MarkerCall = ndx.dimension(function (d) {
                return d.MSI_STATUS_7_MARKER_CALL;
        });

        var msiStatus5MarkerCall = ndx.dimension(function (d) {
                return d.MSI_STATUS_5_MARKER_CALL;
        });

        var methylationCluster = ndx.dimension(function (d) {
                return d.METHYLATION_CLUSTER;
        });

        var mlh1Silencing = ndx.dimension(function (d) {
                return d.MLH1_SILENCING;
        });

        var cnaClusterK4 = ndx.dimension(function (d) {
                return d.CNA_CLUSTER_K4;
        });

        var mutationRateCluster = ndx.dimension(function (d) {
                return d.MUTATION_RATE_CLUSTER;
        });

        var microRnaCluster = ndx.dimension(function (d) {
                return d.MICRO_RNA_CLUSTER;
        });

        var osStatus = ndx.dimension(function (d) {
                return d.OS_STATUS;
        });

        var dfsStatus = ndx.dimension(function (d) {
                return d.DFS_STATUS;
        });

        var dataMaf = ndx.dimension(function (d) {
                return d.DATA_MAF;
        });

        var dataGistic = ndx.dimension(function (d) {
                return d.DATA_GISTIC;
        });

        var dataRnaseq = ndx.dimension(function (d) {
                return d.DATA_RNASEQ;
        });

        var dataCoreSample = ndx.dimension(function (d) {
                return d.DATA_CORE_SAMPLE;
        });

        var age = ndx.dimension(function (d) {
                if(!isNaN(d.AGE))
                        return d.AGE;
                else
                        return '';
        });

        var microRnaScore = ndx.dimension(function (d) {
                if(!isNaN(d.MICRO_RNA_SCORE))
                        return d.MICRO_RNA_SCORE;
                else
                        return '';
        });

        var osMonths = ndx.dimension(function (d) {
                if(!isNaN(d.OS_MONTHS))
                        return d.OS_MONTHS;
                else
                        return '';
        });

        var dfsMonths = ndx.dimension(function (d) {
                if(!isNaN(d.DFS_MONTHS))
                        return d.DFS_MONTHS;
                else
                        return '';
        });

        //Initial groups of each chart dimension
        var mrnaExpressionClusterGroup = mrnaExpressionCluster.group();
        var tumorStage2009Group = tumorStage2009.group();
        var subtypeGroup = subtype.group();
        var histologyGroup = histology.group();
        var tumorGradeGroup = tumorGrade.group();
        var msiStatus7MarkerCallGroup = msiStatus7MarkerCall.group();
        var msiStatus5MarkerCallGroup = msiStatus5MarkerCall.group();
        var methylationClusterGroup = methylationCluster.group();
        var mlh1SilencingGroup = mlh1Silencing.group();
        var cnaClusterK4Group = cnaClusterK4.group();
        var mutationRateClusterGroup = mutationRateCluster.group();
        var microRnaClusterGroup = microRnaCluster.group();
        var osStatusGroup = osStatus.group();
        var dfsStatusGroup = dfsStatus.group();

        var dataMafGroup = dataMaf.group();
        var dataGisticGroup = dataGistic.group();
        var dataRnaseqGroup = dataRnaseq.group();
        var dataCoreSampleGroup = dataCoreSample.group();

        var ageGroup = age.group();
        var osMonthsGroup =  osMonths.group()  
        var dfsMonthsGroup = dfsMonths.group()  
        var microRnaScoreGroup = microRnaScore.group()  


        var widthMain = $('#main').width();
        //MRNA EXPRESSION CLUSTER PIE CHART
        mrnaExpressionClusterChart
        .width(180)
        .height(180)
        .radius(86)
        .innerRadius(30)
        .dimension(mrnaExpressionCluster)
        .group(mrnaExpressionClusterGroup)
        .label(function (d) {
                if (mrnaExpressionClusterChart.hasFilter() && !mrnaExpressionClusterChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)


        //TUMOR STAGE 2009 PIE CHART
        tumorStage2009Chart
        .width(200)
        .height(180)
        .margins({top: 0, left: 10, right: 10, bottom: 20})
        .dimension(tumorStage2009)
        .group(tumorStage2009Group)
        .label(function (d) {
                if (tumorStage2009Chart.hasFilter() && !tumorStage2009Chart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)
        .elasticX(true)
        .labelOffsetY(18)
        .xAxis().ticks(4);



        //SUBTYPE PIE CHART
        subtypeChart
        .width(200)
        .height(180)
        .margins({top: 0, left: 10, right: 10, bottom: 20})
        .dimension(subtype)
        .group(subtypeGroup)
        .label(function (d) {
                if (subtypeChart.hasFilter() && !subtypeChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .title(function (d) {
                return d.value;
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)
        .elasticX(true)
        .labelOffsetY(18)
        .xAxis().ticks(4);

        //HISTOLOGY PIE CHART
        histologyChart
        .width(180)
        .height(180)
        .radius(88)
        .dimension(histology)
        .group(histologyGroup)
        .label(function (d) {
                if (histologyChart.hasFilter() && !histologyChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)

        //TUMOR GRADE PIE CHART
        tumorGradeChart
        .width(180)
        .height(180)
        .radius(88)
        .dimension(tumorGrade)
        .group(tumorGradeGroup)
        .label(function (d) {
                if (tumorGradeChart.hasFilter() && !tumorGradeChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)

        //MSI STATUS 7 MARKER CALL PIE CHART
        msiStatus7MarkerCallChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(msiStatus7MarkerCall)
        .group(msiStatus7MarkerCallGroup)
        .label(function (d) {
                if (msiStatus7MarkerCallChart.hasFilter() && !msiStatus7MarkerCallChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)

        //MSI STATUS 5 MARKER CALL CHART
        msiStatus5MarkerCallChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(msiStatus5MarkerCall)
        .group(msiStatus5MarkerCallGroup)
        .label(function (d) {
                if (msiStatus5MarkerCallChart.hasFilter() && !msiStatus5MarkerCallChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //METHYLATION CLUSTER CHART
        methylationClusterChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(methylationCluster)
        .group(methylationClusterGroup)
        .label(function (d) {
                if (methylationClusterChart.hasFilter() && !methylationClusterChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //MLH1 SILENCING CHART
        mlh1SilencingChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(mlh1Silencing)
        .group(mlh1SilencingGroup)
        .label(function (d) {
                if (mlh1SilencingChart.hasFilter() && !mlh1SilencingChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //CNA CLUSTER K4 CHART
        cnaClusterK4Chart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(cnaClusterK4)
        .group(cnaClusterK4Group)
        .label(function (d) {
                if (cnaClusterK4Chart.hasFilter() && !cnaClusterK4Chart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //MUTATION RATE CLUSTER
        mutationRateClusterChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(mutationRateCluster)
        .group(mutationRateClusterGroup)
        .label(function (d) {
                if (mutationRateClusterChart.hasFilter() && !mutationRateClusterChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //MICRO RNA CLUSTER CHART
        microRnaClusterChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(microRnaCluster)
        .group(microRnaClusterGroup)
        .label(function (d) {
                if (microRnaClusterChart.hasFilter() && !microRnaClusterChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //OVERALL SURVIVAL STATUS CHART
        osStatusChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(osStatus)
        .group(osStatusGroup)
        .label(function (d) {
                if (osStatusChart.hasFilter() && !osStatusChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //DIESASE FREE STATUS CHART
        dfsStatusChart
        .width(180)
        .height(180)
        .radius(88)		
        .innerRadius(30)
        .dimension(dfsStatus)
        .group(dfsStatusGroup)
        .label(function (d) {
                if (dfsStatusChart.hasFilter() && !dfsStatusChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        .transitionDuration(800)

        //DATA MAF PIE CHART
        dataMafChart
        .width(100)
        .height(82)
        .radius(40)
        .dimension(dataMaf)
        .group(dataMafGroup)
        .label(function (d) {
                if (dataMafChart.hasFilter() && !dataMafChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)

        //DATA GISTIC PIE CHART
        dataGisticChart
        .width(100)
        .height(82)
        .radius(40)
        .dimension(dataGistic)
        .group(dataGisticGroup)
        .label(function (d) {
                if (dataGisticChart.hasFilter() && !dataGisticChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)

        //DATA RNASEQ PIE CHART
        dataRnaseqChart
        .width(100)
        .height(82)
        .radius(40)
        .dimension(dataRnaseq)
        .group(dataRnaseqGroup)
        .label(function (d) {
                if (dataRnaseqChart.hasFilter() && !dataRnaseqChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)

        //DATA MAF PIE CHART
        dataCoreSampleChart
        .width(100)
        .height(82)
        .radius(40)
        .dimension(dataCoreSample)
        .group(dataCoreSampleGroup)
        .label(function (d) {
                if (dataCoreSampleChart.hasFilter() && !dataCoreSampleChart.hasFilter(d.key))
                        return d.key + "(0%)";
                return d.key + "(" + Math.floor(d.value / all.value() * 100) + "%)";
        })
        //.colorAccessor(function(d, i){return d.value;})
        .transitionDuration(800)

        //AGE BAR CHART
        ageChart
        .width(widthMain)
        .height(200)
        .margins({top: 10, right: 50, bottom: 30, left: 40})
        .dimension(age)
        .group(ageGroup)
        .elasticY(true)
        .centerBar(true)
        .mouseZoomable(false)
        .gap(1)
        .brushOn(true)
        .transitionDuration(800)
        .round(dc.round.floor)
        .x(d3.scale.linear().domain([20, 100]))
        ageChart.xAxis().tickFormat(function (v) { return v; });
        ageChart.xAxis().ticks(20);
        ageChart.yAxis().tickFormat(d3.format("d"));
        ageChart.xUnits(function(){return 100;});		

        //OVERALL MONTHS CHART
        osMonthsChart
        .width(widthMain)
        .height(200)
        .margins({top: 10, right: 50, bottom: 30, left: 40})
        .dimension(osMonths)
        .group(osMonthsGroup)
        .elasticY(true)
        .centerBar(true)
        .brushOn(false)
        .mouseZoomable(true)
        .transitionDuration(800)
        .round(dc.round.floor)
        .x(d3.scale.linear().domain([0, 200]))

        //osMonthsChart.xAxis().tickFormat(d3.format("d"));
        osMonthsChart.xAxis().ticks(20);
        osMonthsChart.yAxis().tickFormat(d3.format("d"));	
        osMonthsChart.xUnits(function(){return 200;});	

        //DISEASE MONTHS CHART
        dfsMonthsChart
        .width(widthMain)
        .height(200)
        .margins({top: 10, right: 50, bottom: 30, left: 40})
        .dimension(dfsMonths)
        .group(dfsMonthsGroup)
        .elasticY(true)
        .centerBar(true)
        .brushOn(false)
        .mouseZoomable(true)
        .transitionDuration(800)
        .round(dc.round.floor)
        .x(d3.scale.linear().domain([0, 200]))

        dfsMonthsChart.yAxis().tickFormat(d3.format("d"));
        dfsMonthsChart.xAxis().ticks(20);
        dfsMonthsChart.xUnits(function(){return 400;});

        //MICRO RNA SCORE BAR CHART
        microRnaScoreChart
        .width(widthMain)
        .height(200)
        .margins({top: 10, right: 50, bottom: 30, left: 40})
        .dimension(microRnaScore)
        .group(microRnaScoreGroup)
        .centerBar(true)
        .elasticY(true)
        .mouseZoomable(false)
        .brushOn(true)
        .transitionDuration(800)
        .round(dc.round.floor)
        .x(d3.scale.linear().domain([-1, 1]))
        microRnaScoreChart.yAxis().tickFormat(d3.format("d"));
        microRnaScoreChart.xAxis().ticks(20);
        microRnaScoreChart.yAxis().ticks(4);	
        microRnaScoreChart.xUnits(function(){return 400;});

        //DATA TABLE
        dc.dataCount(".dc-data-count")
                                .dimension(ndx)
                                .group(all);

        dataTable
        .dimension(CASEID)
        .group(function (d) {
                return 1 ;
        })
        .columns([
                function (d) {
                        return d.CASE_ID;
                },
                function (d) {
                        return d.SUBTYPE;
                },
                function (d) {
                        if(!isNaN(d.AGE))
                                return d.AGE;
                        else
                                return '';
                },
                function (d) {
                        return d.TUMOR_STAGE_2009;
                },
                function (d) {
                        return d.HISTOLOGY;
                },
                function (d) {
                        return d.TUMOR_GRADE;
                },
                function (d) {
                        return d.MSI_STATUS_7_MARKER_CALL;
                },
                function (d) {
                        return d.MSI_STATUS_5_MARKER_CALL;
                },
                function (d) {
                        return d.DATA_MAF;
                },
                function (d) {
                        return d.DATA_GISTIC;
                },
                function (d) {
                        return d.DATA_RNASEQ;
                },
                function (d) {
                        return d.DATA_CORE_SAMPLE;
                },
                function (d) {
                        return d.MRNA_EXPRESSION_CLUSTER;
                },
                function (d) {
                        return d.METHYLATION_CLUSTER;
                },
                function (d) {
                        return d.MLH1_SILENCING;
                },
                function (d) {
                        return d.CNA_CLUSTER_K4;
                },
                function (d) {
                        return d.MUTATION_RATE_CLUSTER;
                },
                function (d) {
                        return d.MICRO_RNA_CLUSTER;
                },
                function (d) {
                        if(!isNaN(d.MICRO_RNA_SCORE))
                                return d.MICRO_RNA_SCORE;
                        else
                                return '';
                },
                function (d) {
                        return d.CASE_ID;
                },
                function (d) {
                        return d.OS_STATUS;
                },
                function (d) {
                        if(!isNaN(d.OS_MONTHS))
                                return d.OS_MONTHS;
                        else
                                return '';
                },
                function (d) {
                        return d.DFS_STATUS;
                },
                function (d) {
                        if(!isNaN(d.DFS_MONTHS))
                                return d.DFS_MONTHS;
                        else
                                return '';
                }
        ])
        .size(500)
        .sortBy(function (d) {
                return d.CASE_ID;
        })
        .transitionDuration(800)

        //Render all of charts
        dc.renderAll();


        var dataTable1 = $('#dataTable').dataTable({
                "sScrollX": widthMain,
                "sScrollY": "500px",
                "bPaginate": false,
                "bFilter":true
        });

        $('#dataTable_filter').append("<input type=\"button\" id=\"dataTable_header\" class='header_button' value = \"Refresh Charts\"/>");
        $('#dataTable_filter').append("<input type=\"button\" id=\"dataTable_reset\" class='header_button' value = \"Reset\" />");
        $("#dataTable_filter label input").attr("value","");
        $('#dataTable_header').click(function(){
                if($("#dataTable_filter label input").val() != ""){			
                        //console.log("Inside...1");

                        var items=[];
                        $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
                           items.push( $(this).text() );       
                        });
                        var items = $.unique( items );
                        dataTable.filter(null);
                        dataTable.filter([items]);
                        dc.redrawAll();
                }else{
                        dataTable.filter(null);
                        dc.redrawAll();
                }
        });

        $('#dataTable_reset').click(function(){
                dataTable.filter(null);
                dc.redrawAll();
        });
    }

    function printHTML(){
        for(var i=0 ; i < varName.length ; i++){
            //console.log("<div id='" + varName[i] + "' class='pie-chart'><pieH4>" + varDisplay[i] + "<a class='reset' href='javascript:varChart[" + i + "].filterAll();dc.redrawAll();' style='display: none;'>reset</a></pieH4></div>");
            $("#pie").append("<div id=\"" + varName[i] + "\" class='pie-chart'><pieH4>" + varDisplay[i] + "<a class='reset' href='javascript:varChart[" + i + "].filterAll();dc.redrawAll();' style='display: none;'>reset</a></pieH4></div>");
        }    
    }
    
    
    
