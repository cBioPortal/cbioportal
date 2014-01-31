
var varChart = new Array();
var dataTableDC;
var removeKeyIndex = new Array();
    
$(function() {

    
    dc.redrawAllDataTable = function(group) {
        var dataTable1 = $('#dataTable').dataTable();

        dataTable1.fnDestroy();

        var charts = dc.chartRegistry.list(group);
        for (var i = 0; i < charts.length; i++) {
            charts[i].redraw();
        }

        if(dc._renderlet !== null)
            dc._renderlet(group);

        dataTable1 = $('#dataTable').dataTable({
                "sScrollX": "1200px",
                "sScrollY": "300px",
                "bPaginate": false,
                "bFilter":true,
                "bScrollCollapse": true
        });
        for(var i =0 ; i< removeKeyIndex.length ; i++){
            dataTable1.fnSetColumnVis(removeKeyIndex[i],false);
        }

        var numColumns = dataTable1.fnSettings().aoColumns.length;
        var maxX = 0;

        for(var i =1;i<=numColumns ; i++){
            var rotatedX = $("table.dataTable>thead>tr>th:nth-child("+i+")").height();
            if(rotatedX > maxX)
                maxX = rotatedX;
        }

        for(var i =1;i<=numColumns ; i++){
            $("table.dataTable>thead>tr>th:nth-child("+i+")").height(maxX);
        }  

        new FixedColumns( dataTable1);
        $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");   
        $(".dataTables_scroll").css("overflow-x","hidden");      
        $(".DTFC_LeftHeadWrapper").css("background-color","white");     


        dataTable1.fnAdjustColumnSizing();

        $("#dataTable_filter label input").attr("value","");    
        $('#study-view-dataTable-header').click(function(){
                if($("#dataTable_filter label input").val() !== ""){			
                        console.log("Inside...2");
                        var items=[];
                        $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
                           items.push( $(this).text() );       
                        });
                        var items = $.unique( items );
                        dataTableDC.filter(null);
                        dataTableDC.filter([items]);
                        dc.redrawAll();
                }else{
                        dataTableDC.filter(null);
                        dc.redrawAll();
                }
        });
        $('#study-view-dataTable-updateTable').click(function(){
                dc.redrawAllDataTable("group1");
        });
    };

    var studyView = function(){
        var studyId;
        var caseIds;
        var cnaProfileId;
        var mutationProfileId;

        var dataObjectM = new Array();
        var attr = new Array();

        var columnNameTotal =  ["CASE_ID","SUBTYPE","GENDER","AGE",
                                "TUMOR_STAGE_2009","HISTOLOGY","TUMOR_GRADE",
                                "MSI_STATUS_7_MARKER_CALL","MSI_STATUS_5_MARKER_CALL","DATA_MAF",
                                "DATA_GISTIC","DATA_CORE_SAMPLE","DATA_RNASEQ",
                                "MRNA_EXPRESSION_CLUSTER","METHYLATION_CLUSTER",
                                "MLH1_SILENCING","CNA_CLUSTER_K4",
                                "MUTATION_RATE_CLUSTER","MICRO_RNA_CLUSTER",
                                "MICRO_RNA_SCORE","OS_STATUS","OS_MONTHS","DFS_STATUS",
                                "DFS_MONTHS","MUTATION_COUNT","COPY_NUMBER_ALTERATIONS"];

        return {
            set_studyId: function (s) {studyId = s;},
            set_caseIds: function (s) {caseIds = s;},
            set_cnaProfileId: function (s) {cnaProfileId = s;},
            set_mutationProfileId: function (s) {mutationProfileId = s;},
            getData: function(){
                var usefulData;
                var dataObject = new Array(); 
                var caseIdStr = caseIds.join(' ');
                var webserviceData = {
                    cmd: "getClinicalData",
                    format: "json",
                    cancer_study_id: studyId,
                    case_list: caseIdStr
                };
                var clinicalAttributesData = {
                    cancer_study_id: studyId,
                    case_list: caseIdStr
                };
                var mutationsData = {
                    cmd: "count_mutations",
                    cases_ids: caseIdStr,
                    mutation_profile: mutationProfileId
                };
                var cnaData = {
                    cmd: "get_cna_fraction",
                    case_ids: caseIdStr,
                    cancer_study_id: studyId
                };
                
                $.when(  $.ajax({type: "POST", url: "webservice.do", data: webserviceData}), 
                        $.ajax({type: "POST", url: "clinicalAttributes.json", data: clinicalAttributesData}),
                        $.ajax({type: "POST", url: "mutations.json", data: mutationsData}),
                        $.ajax({type: "POST", url: "cna.json", data: cnaData}))
                
                    .done(function(a1, a2, a3, a4){
                        $.each(a1[0], function(i, field){
                            if(i == "data")
                                usefulData = field;
                        });

                        for(var i=0; i < usefulData.length;i++){
                            if(usefulData[i]["sample"] in dataObject){
                                dataObject[usefulData[i]["sample"]][usefulData[i]["attr_id"]] = usefulData[i]["attr_val"];
                            }
                            else{
                                dataObject[usefulData[i]["sample"]] = new Array();
                                dataObject[usefulData[i]["sample"]][usefulData[i]["attr_id"]] = usefulData[i]["attr_val"];
                            }
                        }
                        
                        var keys = Object.keys(dataObject);
                        var keyNumMapping = [];
                        for(var j = 0; j< keys.length ; j++){
                            dataObjectM[j] = new Array();
                            dataObjectM[j]["CASE_ID"] = keys[j];
                            dataObjectM[j]["MUTATION_COUNT"] = "";
                            dataObjectM[j]["COPY_NUMBER_ALTERATIONS"] = "";
                            keyNumMapping[keys[j]] = j;
                            for (var key in dataObject[keys[j]])
                                dataObjectM[j][key] = dataObject[keys[j]][key];
                        }

                        attr = a2[0];
                        if(a3[0].length != 0){
                            var newAttri1 = new Array();
                            newAttri1.attr_id = 'MUTATION_COUNT';
                            newAttri1.display_name = 'Mutation Count';
                            newAttri1.description = 'Mutation Count';
                            newAttri1.datatype = 'NUMBER';                        

                            jQuery.each(a3[0], function(i,val){
                                dataObjectM[keyNumMapping[i]]['MUTATION_COUNT'] = val.toString();
                            });    
                            attr.push(newAttri1);
                        }
                        if(a4[0].length != 0){
                            var newAttri2 = new Array();
                            newAttri2.attr_id = 'COPY_NUMBER_ALTERATIONS';
                            newAttri2.display_name = 'Copy Number Alterations';
                            newAttri2.description = 'Copy Number Alterations';
                            newAttri2.datatype = 'NUMBER';

                            jQuery.each(a4[0], function(i,val){
                                dataObjectM[keyNumMapping[i]]['COPY_NUMBER_ALTERATIONS'] = val.toString();
                            }); 
                            attr.push(newAttri2);
                        }

                        var columnNameSelected = initCharts();
                        columnNameSelected.unshift("CASE_ID");
                        restyle(columnNameSelected,columnNameTotal);
                    });
            }        
        };

        function initCharts(){
            var dataA = new Array();
            var dataB = new Array();
            var pie = new Array();
            var row = new Array();
            var rowKeys = new Array();
            var bar = new Array();
            var combine = new Array();
            var varName = new Array();
            var varNameIDMapping = new Array();
            var varDisplay = new Array();            
            var varType = new Array();
            var varCluster = new Array();
            var varGroup = new Array();
            var distanceMinMaxArray = new Array();
            var chartColors = ["#3366cc","#dc3912","#ff9900","#109618",
            "#990099","#0099c6","#dd4477","#66aa00",
            "#b82e2e","#316395","#994499","#22aa99",
            "#aaaa11","#6633cc","#e67300","#8b0707",
            "#651067","#329262","#5574a6","#3b3eac",
            "#b77322","#16d620","#b91383","#f4359e",
            "#9c5935","#a9c413","#2a778d","#668d1c",
            "#bea413","#0c5922","#743411"];
            
            dataA = attr;
            dataB = dataObjectM;

            //c.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(fraction_of_copy_number_altered_genome)/)
            
            for(var i=0; i< dataA.length ; i++){
                var varValues = new Array();
                for(var j=0;j<dataB.length;j++){
                    if(varValues.hasOwnProperty(dataB[j][dataA[i]["attr_id"]]))
                        varValues[dataB[j][dataA[i]["attr_id"]]]++;
                    else
                        varValues[dataB[j][dataA[i]["attr_id"]]]=0;
                }
                var keys = Object.keys(varValues);
                    
                if(dataA[i]["attr_id"] === "CASE_ID"){
                    continue;
                }else if(dataA[i]["datatype"] === "NUMBER" || dataA[i]["datatype"] === "BOOLEAN"){                
                    if(selectedCol(dataA[i]["attr_id"])){                    
                        if(keys.length>10)
                            bar.push(dataA[i]);
                        else
                            pie.push(dataA[i]);
                    }
                    
                    if(keys.length > 10)
                        varType[dataA[i]["attr_id"]] = "bar,row";
                    else if(keys.length > 30)
                        varType[dataA[i]["attr_id"]] = "bar";
                    else
                        varType[dataA[i]["attr_id"]] = "pie,row";
                    
                }else if(dataA[i]["datatype"] === "STRING"){
                    if(selectedCol(dataA[i]["attr_id"])){
                        pie.push(dataA[i]);
                    }
                    
                    varType[dataA[i]["attr_id"]] = "pie,row";
                }
                else 
                    combine.push(dataA[i]);
                
                varDisplay.push(dataA[i]["display_name"]);                
                varName.push(dataA[i]["attr_id"]);
                rowKeys[dataA[i]["attr_id"]] = keys;
            }
            
            var totalCharts = pie.length + bar.length + row.length;
            var createdChartID = 0;
            
            for(var i=0; i< pie.length ; i++){
                $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + createdChartID + "\" class='study-view-dc-chart study-view-pie-chart'><div style='width:100%; float:left'><pieH4>" + pie[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + createdChartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                varNameIDMapping["study-view-dc-chart-" + createdChartID] = createdChartID;
                varChart.push(dc.pieChart("#study-view-dc-chart-" + createdChartID));
                createdChartID++;
            }

            for(var i=0; i< row.length; i++){
                $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + createdChartID + "\" class='study-view-dc-chart study-view-row-chart'><div style='width:100%; float:left'><pieH4>" + row[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + createdChartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                varNameIDMapping["study-view-dc-chart-" + createdChartID] = createdChartID;
                varChart.push(dc.rowChart("#study-view-dc-chart-" + createdChartID));
                createdChartID++;
            }

            for(var i=0; i< bar.length ; i++){
                $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + createdChartID + "\" class='study-view-dc-chart study-view-bar-chart'><div style='width:100%; float:left'><pieH4>" + bar[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + createdChartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                varNameIDMapping["study-view-dc-chart-" + createdChartID] = createdChartID;
                varChart.push(dc.barChart("#study-view-dc-chart-" + createdChartID));
                createdChartID++;
            }
            
            if(createdChartID !== totalCharts){
                console.log("Initial charts function error: the number of created charts not equal to number of totalCharts. --1");
                return false;
            }
                
            var ndx = crossfilter(dataB);
            var all = ndx.groupAll();

            //Initial all pie charts
            createdChartID = 0;
            
            for(var i=0; createdChartID < pie.length ;i++){
                varCluster[createdChartID] = ndx.dimension(function (d) {
                    if(!d[pie[i]["attr_id"]] || d[pie[i]["attr_id"]].toLowerCase()==="unknown" || d[pie[i]["attr_id"]].toLowerCase()==="none")
                        return "NA";
                    return d[pie[i]["attr_id"]];
                });
                varGroup[createdChartID] = varCluster[createdChartID].group();
                
                var pieWidth = 180;
                var pieRadius = (pieWidth - 60) /2;
                varChart[createdChartID]
                .width(pieWidth)
                .height(pieWidth)
                .radius(pieRadius)
                .dimension(varCluster[createdChartID])
                .group(varGroup[createdChartID])
                .transitionDuration(1200)
                .ordinalColors(chartColors)
                .label(function (d) {
                    return d.key + ":" + d.value;
                });
                
                createdChartID++;
            }

            //Initial all row charts
            /*
            while(createdChartID < pie.length+row.length){
                var rowChartHeight = rowKeys[createdChartID-pie.length].length * 25 +50;
                $(varName[createdChartID]).css("height",rowChartHeight+50+"px")
                varCluster[createdChartID] = ndx.dimension(function (d) {
                    if(!d[varName[createdChartID]])
                        return "NA";
                    return d[varName[createdChartID]];
                });
                varGroup[createdChartID] = varCluster[createdChartID].group();

                varChart[createdChartID]
                .width(300)
                .height(rowChartHeight)
                .dimension(varCluster[createdChartID])
                .group(varGroup[createdChartID])
                .transitionDuration(1200)
                .elasticX(true)
                .ordinalColors(chartColors)
                .title(function (d) {
                        return d.value;
                })
                .label(function (d) {
                    return d.key + ":" + d.value;
                });
                
                createdChartID++;
            }
            */
            //Initial all bar charts
            for(var i = 0 ; createdChartID < pie.length+row.length+bar.length ; i++){           

                var varValues = new Array();
                for(var j=0;j<dataB.length;j++){
                    if(dataB[j][bar[i]["attr_id"]] && dataB[j][bar[i]["attr_id"]]!=="NA" && dataB[j][bar[i]["attr_id"]]!=="")
                        varValues.push(dataB[j][bar[i]["attr_id"]]);                    
                }

                distanceMinMaxArray[bar[i]["attr_id"]] = {
                    distance : Math.max.apply( Math, varValues ) - Math.min.apply( Math, varValues ),
                    min: Math.min.apply( Math, varValues ),
                    max:Math.max.apply( Math, varValues )
                };
                        
                var distanceMinMax = distanceMinMaxArray[bar[i]["attr_id"]].distance;
                
                varCluster[createdChartID] = ndx.dimension(function (d) {
                    var returnValue = d[bar[i]["attr_id"]]; 
                    if(d[bar[i]["attr_id"]] % 1 !== 0 && decimalPlaces(d[bar[i]["attr_id"]]) > 3)
                        if(distanceMinMax < 2){
                            returnValue = d3.round(d[bar[i]["attr_id"]],2);
                        }
                        else
                            returnValue = d3.round(d[bar[i]["attr_id"]]);
                    if(returnValue === "NA" || returnValue === '')
                        returnValue = Math.min.apply( Math, varValues )-100;
                    return returnValue;
                });
                
                var barScale = 50;

                varGroup[createdChartID] = varCluster[createdChartID].group();

                varChart[createdChartID]
                    .width(560)
                    .height(180)
                    .margins({top: 10, right: 10, bottom: 30, left: 40})
                    .dimension(varCluster[createdChartID])
                    .group(varGroup[createdChartID])
                    .gap(5)
                    .centerBar(true)
                    .elasticY(false)
                    .mouseZoomable(false)
                    .brushOn(true)
                    .transitionDuration(1200)
                    .renderHorizontalGridLines(true)
                    .renderVerticalGridLines(true)

                if(distanceMinMax < 1){
                    varChart[createdChartID].x(d3.scale.linear().nice([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                    varChart[createdChartID].yAxis().tickFormat(d3.format("d"));
                    varChart[createdChartID].xAxis().ticks(10);
                    varChart[createdChartID].xUnits(function(){return barScale;});
                }else if(distanceMinMax < 2){
                    varChart[createdChartID].x(d3.scale.linear().domain([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                    varChart[createdChartID].yAxis().tickFormat(d3.format("d"));
                    varChart[createdChartID].xAxis().ticks(10);
                    varChart[createdChartID].xUnits(function(){return barScale;});
                }else if(varDisplay[createdChartID].search(/month/i) != -1){                
                    varChart[createdChartID].x(d3.scale.linear().domain([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                    varChart[createdChartID].xAxis().ticks(10);
                    varChart[createdChartID].yAxis().tickFormat(d3.format("d"));
                    varChart[createdChartID].xAxis().tickFormat(d3.format("d"));
                }else{
                    varChart[createdChartID].x(d3.scale.linear().domain([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                    varChart[createdChartID].yAxis().tickFormat(d3.format("d"));
                    varChart[createdChartID].xAxis().tickFormat(d3.format("d"));
                    varChart[createdChartID].xAxis().ticks(10);
                    varChart[createdChartID].xUnits(function(){return barScale;});
                }
                
                createdChartID++;
            }
            
            if(createdChartID !== totalCharts){
                console.log("Initial charts function error: the number of created charts not equal to number of totalCharts. --2");
                return false;
            }
            
            dataTableDC = dc.dataTableDataOnly("#dataTable","group1");
            var CASEID = ndx.dimension(function (d) {
                    return d.CASE_ID;
            });	
            dc.dataCount(".dc-data-count")
                                    .dimension(ndx)
                                    .group(all);


            var dataTableColumns = new Array();

            for(var i=0; i< varChart.length ; i++){
                dataTableColumns[i] = function (d) {
                    return d[varName[i]];
                };
            }


            dataTableDC
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
                        return d.GENDER;
                    },
                    function (d) {
                        return d.AGE;
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
                        return d.MICRO_RNA_SCORE;
                    },
                    function (d) {
                        return d.OS_STATUS;
                    },
                    function (d) {
                        return d.OS_MONTHS;
                    },
                    function (d) {
                        return d.DFS_STATUS;
                    },
                    function (d) {
                        return d.DFS_MONTHS;
                    },
                    function (d) {
                        return d.MUTATION_COUNT;
                    },
                    function (d) {
                        return d.COPY_NUMBER_ALTERATIONS;
                    }
            ])
            .size(2000)
            .sortBy(function (d) {
                    return d.CASE_ID;
            })
            .transitionDuration(1200);

            dc.renderAll();
            dc.renderAll("group1");
            
            $('#study-view-selectAttr')
                .find('option:gt(0)')
                .remove()
                .end()
        
            $.each(varName, function(key, value) {   
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",value)
                        .text(varDisplay[key]));                
            });
            
            $('#study-view-selectAttr').change(function(){
                
                var chartType = varType[$(this).val()].split(',');
                
                if(chartType.length > 1){
                    $('#study-view-selectChartType')
                        .find('option:gt(0)')
                        .remove()
                        .end();                    
                }else{
                    $('#study-view-selectChartType')
                        .remove()
                        .end();
                }
                
                $.each(chartType, function(key, value) {
                    $('#study-view-selectChartType')
                        .append($("<option></option>")
                            .attr("value",value)
                            .text(value + " chart"));
                });
                
               $('#study-view-add-chart-button').attr('disabled','disabled');  
            });
            
            $('#study-view-selectChartType').change(function(){
                
                if($('#study-view-selectChartType').find(":selected").val() !== "" && $('#study-view-selectAttr').find(":selected").val())
                    $('#study-view-add-chart-button').removeAttr('disabled');
            });
            
            var container = document.querySelector('#study-view-charts');
            var msnry = new Masonry( container, {
              columnWidth: 190,
              itemSelector: '.study-view-dc-chart',
              gutter:1
            });
            
            $('.study-view-dc-chart-delete').click(function(event){
                var id = $(this).parent().parent().attr("id");
                $("div").remove("#" + id); 
                varChart[varNameIDMapping[id]].filterAll();
                dc.redrawAll();
                dc.deregisterChart(varChart[varNameIDMapping[id]]);
                msnry.layout();
            });
            
            $('#study-view-add-chart-button').click(function(){
                var selectedAttr = $('#study-view-selectAttr').find(":selected").val();
                var selectedAttrDisplay = $('#study-view-selectAttr').find(":selected").text();
                var selectedChartType = $('#study-view-selectChartType').find(":selected").val();
                
                
                if(selectedChartType == 'pie'){
                    $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + totalCharts + "\" class='study-view-dc-chart study-view-pie-chart'><div style='width:100%; float:left'><pieH4>" + selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + totalCharts + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                    varChart.push(dc.pieChart("#study-view-dc-chart-" + totalCharts));
                    varCluster[totalCharts] = ndx.dimension(function (d) {
                        if(!d[selectedAttr] || d[selectedAttr].toLowerCase()==="unknown" || d[selectedAttr].toLowerCase()==="none")
                            return "NA";
                        return d[selectedAttr];
                    });
                    varGroup[totalCharts] = varCluster[totalCharts].group();

                    var pieWidth = 180;
                    var pieRadius = (pieWidth - 60) /2;
                    varChart[totalCharts]
                    .width(pieWidth)
                    .height(pieWidth)
                    .radius(pieRadius)
                    .dimension(varCluster[totalCharts])
                    .group(varGroup[totalCharts])
                    .transitionDuration(1200)
                    .ordinalColors(chartColors)
                    .label(function (d) {
                        return d.key + ":" + d.value;
                    });
                   
                }else if(selectedChartType == 'row'){
                    var rowChartHeight = rowKeys[selectedAttr].length * 25 +50;
                    var keyMaxLength = 0;
                    var chartWidth = 0;
                    for(var i=0 ; i < rowKeys[selectedAttr].length ; i++){
                        if(rowKeys[selectedAttr][i].length > keyMaxLength)
                            keyMaxLength = rowKeys[selectedAttr][i].length;
                    }
                    
                    if(keyMaxLength > 30){
                        $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + totalCharts + "\" class='study-view-dc-chart study-view-row-chart w3'><div style='width:100%; float:left'><pieH4>" + selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + totalCharts + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                        chartWidth = "560px";
                    }else if(keyMaxLength > 20){
                        $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + totalCharts + "\" class='study-view-dc-chart study-view-row-chart w2'><div style='width:100%; float:left'><pieH4>" + selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + totalCharts + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                        chartWidth = "370";
                    }else{
                        $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + totalCharts + "\" class='study-view-dc-chart study-view-row-chart w1'><div style='width:100%; float:left'><pieH4>" + selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + totalCharts + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                        chartWidth = "180";
                    }
                    
                    varChart.push(dc.rowChart("#study-view-dc-chart-" + totalCharts));  
                    
                    varCluster[totalCharts] = ndx.dimension(function (d) {
                        if(!d[selectedAttr])
                            return "NA";
                        return d[selectedAttr];
                    });
                    varGroup[totalCharts] = varCluster[totalCharts].group();

                    varChart[totalCharts]
                    .width(chartWidth)
                    .margins({top: 10, right: 10, bottom: 20, left: 10})
                    .height(rowChartHeight)
                    .dimension(varCluster[totalCharts])
                    .group(varGroup[totalCharts])
                    .transitionDuration(1200)
                    .elasticX(true)
                    .ordinalColors(chartColors)
                    .label(function (d) {
                        return d.key + ":" + d.value;
                    });                   
                    
                    varChart[totalCharts].xAxis().ticks(4);
                }else{
                    $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + totalCharts + "\" class='study-view-dc-chart study-view-bar-chart'><div style='width:100%; float:left'><pieH4>" + selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + totalCharts + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span></div></div>");
                    varChart.push(dc.barChart("#study-view-dc-chart-" + totalCharts));
                    var distanceMinMax = distanceMinMaxArray[selectedAttr].distance;
                    var distanceMin = distanceMinMaxArray[selectedAttr].min;
                    var distanceMax = distanceMinMaxArray[selectedAttr].max;
                    
                    varCluster[totalCharts] = ndx.dimension(function (d) {
                        var returnValue = d[selectedAttr]; 
                        if(d[selectedAttr] % 1 !== 0 && decimalPlaces(d[selectedAttr]) > 3)
                            if(distanceMinMax < 2){
                                returnValue = d3.round(d[selectedAttr],2);
                            }
                            else
                                returnValue = d3.round(d[selectedAttr]);
                        if(returnValue === "NA" || returnValue === '')
                            returnValue = distanceMin-100;
                        return returnValue;
                    });

                    var barScale = 50;

                    varGroup[totalCharts] = varCluster[totalCharts].group();

                    varChart[totalCharts]
                        .width(560)
                        .height(180)
                        .margins({top: 10, right: 10, bottom: 30, left: 40})
                        .dimension(varCluster[totalCharts])
                        .group(varGroup[totalCharts])
                        .gap(5)
                        .centerBar(true)
                        .elasticY(false)
                        .mouseZoomable(false)
                        .brushOn(true)
                        .transitionDuration(1200)
                        .renderHorizontalGridLines(true)
                        .renderVerticalGridLines(true)

                    if(distanceMinMax < 1){
                        varChart[totalCharts].x(d3.scale.linear().nice([distanceMin-distanceMinMax/barScale, distanceMax+distanceMinMax/barScale]))
                        varChart[totalCharts].yAxis().tickFormat(d3.format("d"));
                        varChart[totalCharts].xAxis().ticks(10);
                        varChart[totalCharts].xUnits(function(){return barScale;});
                    }else if(distanceMinMax < 2){
                        varChart[totalCharts].x(d3.scale.linear().domain([distanceMin-distanceMinMax/barScale, distanceMax+distanceMinMax/barScale]))
                        varChart[totalCharts].yAxis().tickFormat(d3.format("d"));
                        varChart[totalCharts].xAxis().ticks(10);
                        varChart[totalCharts].xUnits(function(){return barScale;});
                    }else if(selectedAttrDisplay.search(/month/i) != -1){                
                        varChart[totalCharts].x(d3.scale.linear().domain([distanceMin-distanceMinMax/barScale, distanceMax+distanceMinMax/barScale]))
                        varChart[totalCharts].xAxis().ticks(10);
                        varChart[totalCharts].yAxis().tickFormat(d3.format("d"));
                        varChart[totalCharts].xAxis().tickFormat(d3.format("d"));
                    }else{
                        varChart[totalCharts].x(d3.scale.linear().domain([distanceMin-distanceMinMax/barScale, distanceMax+distanceMinMax/barScale]))
                        varChart[totalCharts].yAxis().tickFormat(d3.format("d"));
                        varChart[totalCharts].xAxis().tickFormat(d3.format("d"));
                        varChart[totalCharts].xAxis().ticks(10);
                        varChart[totalCharts].xUnits(function(){return barScale;});
                    }
                }
                
                varNameIDMapping["study-view-dc-chart-" + totalCharts] = totalCharts;
            
                msnry.destroy();
                var container = document.querySelector('#study-view-charts');
                msnry = new Masonry( container, {
                  columnWidth: 190,
                  itemSelector: '.study-view-dc-chart',
                  gutter:1
                });
                varChart[totalCharts].render();
                msnry.layout();
                
                $('.study-view-dc-chart-delete').click(function(event){
                    var id = $(this).parent().parent().attr("id");
                    $("div").remove("#" + id); 
                    varChart[varNameIDMapping[id]].filterAll();
                    dc.redrawAll();
                    dc.deregisterChart(varChart[varNameIDMapping[id]]);
                    msnry.layout();
                });
                
                totalCharts++;                
            });
            
            return varName;
        };    

        function restyle(columnNameSelected,columnNameTotal){
            var dataTable1 = $('#dataTable').dataTable({
                "sScrollX": "1200px",
                "sScrollY": "300px",
                "bPaginate": false,
                "bFilter":true,
                "bScrollCollapse": true
            });

            var keyIndex = new Array();
            for(var i =0 ; i< columnNameSelected.length ; i++){
                var key = columnNameTotal.indexOf(columnNameSelected[i])
                if(key>=0){
                    keyIndex.push(key);
                }
            }
            for(var i =0 ; i< columnNameTotal.length ; i++){
                if(keyIndex.indexOf(i) === -1) {
                    removeKeyIndex.push(i);
                    dataTable1.fnSetColumnVis(i,false);
                }
            }
            $("#dataTable_filter label input").attr("value","");
            $('#study-view-dataTable-header').click(function(){
                if($("#dataTable_filter label input").val() !== ""){			
                        console.log("Inside...1");

                        var items=[];
                        $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
                           items.push( $(this).text() );       
                        });
                        var items = $.unique( items );
                        dataTableDC.filter(null);
                        dataTableDC.filter([items]);
                        dc.redrawAll();
                }else{
                        dataTableDC.filter(null);
                        dc.redrawAll();
                }
            });
            $('#study-view-dataTable-updateTable').click(function(){
                dc.redrawAllDataTable("group1"); 
            });
            
            
        }

        function decimalPlaces(num) {
            var match = (''+num).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);
            if (!match) { return 0; }
            return Math.max(
                0,
                // Number of digits right of decimal point.
                (match[1] ? match[1].length : 0)
                // Adjust for scientific notation.
                - (match[2] ? +match[2] : 0));
        }
    };

    $(document).ready(function(){    
        var study_view = studyView();
        study_view.set_studyId(cancerStudyId);
        study_view.set_caseIds(caseIds);
        study_view.set_mutationProfileId(mutationProfileId);
        study_view.set_cnaProfileId(cnaProfileId);
        study_view.getData();
    });

    function getParameterByName(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }
    
    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }

});
