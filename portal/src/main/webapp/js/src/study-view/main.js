/*
var mrnaExpressionClusterChart;
var tumorStage2009Chart;	
var subtypeChart;
var histologyChart;
var tumorGradeChart;
var msiStatus7MarkerCallChart;
var msiStatus5MarkerCallChart;
var methylationClusterChart;
var mlh1SilencingChart;
var cnaClusterK4Chart;
var mutationRateClusterChart;
var microRnaClusterChart;
var osStatusChart;
var dfsStatusChart;

var dataMafChart;
var dataGisticChart;
var dataRnaseqChart;
var dataCoreSampleChart;

var ageChart;
var osMonthsChart;
var dfsMonthsChart;
var microRnaScoreChart;

var dataTable;

*/

//var attributes = ["mrnaExpressionCluster","tumorStage2009","subtype","histology","tumorGrade","msiStatus7MarkerCall","msiStatus5MarkerCall","methylationCluster","mlh1Silencing","cnaClusterK4","mutationRateCluster","microRnaCluster","osStatus","dfsStatus","dataMaf","dataGistic","dataRnaseq","dataCoreSample","age","osMonths","dfsMonths","microRnaScore","dataTable"];

/*
$(function() {
	var disable = ["micro-rna-score","age","subtype","histology","msi-status-7-marker-call","msi-status-5-marker-call","methylation-cluster","mlh1-silencing","cna-cluster-k4","mutation-rate-cluster","micro-rna-cluster","os-status","dfs-status","os-months","dfs-months"];
	for(var i=0;i<disable.length;i++){
		$("#"+ disable[i] +"-menu").css("color","grey");
		$("#"+ disable[i] +"-chart").css("display","none");
	}
});
*/ 
$(function() {
    $("#pie").sortable();
    $("#row").sortable();
    $("#bar").sortable();
    
    /*
     * left panel : disabled 
     * 
     * 
     *  
    var w = window.innerWidth;
    
    $("#dialog-form").css("left",w/2-450-260);

    $( window ).resize(function() {
        var w = window.innerWidth;
        $("#dialog-form").css("left",w/2-450-260);
    });	
    
    $("#dialog-form li").click(function() { 
        var strings = this.id.split("-menu");
        if(strings[0] != 'data-table'){
            if($("#" + strings[0] + "-chart").css('display') == 'block'){
                $("#" + strings[0] + "-chart").css('display','none');
                $("#" + strings[0] + "-menu").css('color','grey');
            }
            else{
                $("#" + strings[0] + "-chart").css('display','block');
                $("#" + strings[0] + "-menu").css('color','black');
            }
        }else{
            if($("#" + strings[0] + "").css('display') == 'block'){
                $("#" + strings[0] + "").css('display','none');
                $("#" + strings[0] + "-menu").css('color','grey');
            }
            else{
                $("#" + strings[0] + "").css('display','block');
                $("#" + strings[0] + "-menu").css('color','black');
            }
        }

        var dataSub = ["maf","gistic","rnaseq","core-sample"];

        if(strings[0].indexOf("data") !== -1){
            if(strings[0] == "data"){
                if($("#data-chart").css('display') == 'block'){
                    for(var i=0; i < dataSub.length; i++){
                        if($("#data-" + dataSub[i] + "-chart").css('display') == 'block')
                            $("#data-" + dataSub[i] + "-menu").css('color','black');
                        else
                            $("#data-" + dataSub[i] + "-menu").css('color','grey');
                    }	
                    if($("#data-maf-chart").css('display') == 'none' && $("#data-gistic-chart").css('display') == 'none' && $("#data-rnaseq-chart").css('display') == 'none' && $("#data-core-sample-chart").css('display') == 'none'){
                        $("#data-chart h3").css('display','block');
                        $("#data-maf-chart").css('display','block');
                        $("#data-maf-menu").css('color','black');
                    }	
                }else{
                    for(var i=0; i < dataSub.length; i++)
                        $("#data-" + dataSub[i] + "-menu").css('color','grey');
                }
            }else {
                if($("#" + strings[0] + "").css('display') == 'block'){
                    $("#data-menu").css('color','black');
                    $("#data-chart").css('display','block');
                }else{
                    if($("#data-maf-chart").css('display') == 'none' && $("#data-gistic-chart").css('display') == 'none' && $("#data-rnaseq-chart").css('display') == 'none' && $("#data-core-sample-chart").css('display') == 'none'){
                        $("#data-menu").css('color','grey');
                        $("#data-chart").css('display','none');
                    }else{
                        $("#data-menu").css('color','black');
                        $("#data-chart").css('display','block');
                    }
                }
            }					
        }
    });
    */
});
        


var studyId = getParameterByName("cancer_study_id");
var varChart = new Array();
var dataTable;
var removeKeyIndex = new Array();
//console.log(studyId);

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
    $(".DTFC_LeftHeadWrapper").css("background-color","white");     
    
    
    dataTable1.fnAdjustColumnSizing();

    $("#dataTable_filter label input").attr("value","");    
    $('#dataTable_header').click(function(){
            if($("#dataTable_filter label input").val() !== ""){			
                    console.log("Inside...2");
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
    $('#dataTable_updateTable').click(function(){
            dc.redrawAllDataTable("group1");
    });
    $('#dataTable_reset').click(function(){
            dataTable.filter(null);
            dc.redrawAll();
            dc.redrawAll("group1");
    });
    
};

var studyView = function(){
    var studyId;
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
                            "DFS_MONTHS"];
    
    return {
        set_studyId: function (s) {studyId = s;},
        getData: function(){
            var usefulData;
            var dataObject = new Array(); 

            $.getJSON("ClinicalFreeForm.json?studyId="+studyId,function(data) {
                $.each(data, function(i, field){
                    if(i == "freeFormData")
                        usefulData = field;
                });

                for(var i=0; i < usefulData.length;i++){
                    if(usefulData[i]["caseId"] in dataObject){
                        dataObject[usefulData[i]["caseId"]][usefulData[i]["paramName"]] = usefulData[i]["paramValue"];
                    }
                    else{
                        dataObject[usefulData[i]["caseId"]] = new Array();
                        dataObject[usefulData[i]["caseId"]][usefulData[i]["paramName"]] = usefulData[i]["paramValue"];
                    }
                }

                var keys = Object.keys(dataObject);

                for(var j = 0; j< keys.length ; j++){
                    dataObjectM[j] = new Array();
                    dataObjectM[j]["CASE_ID"] = keys[j];
                    for (var key in dataObject[keys[j]])
                        dataObjectM[j][key] = dataObject[keys[j]][key];
                }
                var tmpStr = "";
                for (var j = 0; j< keys.length ; j++)
                    tmpStr += keys[j] + "+";
                tmpStr = tmpStr.substr(0,tmpStr.length-1);  

                //in case the length of url beyong the maximum length, we use ajax instead of getJSON(which by using POST method instead of GET)
                $.post("clinicalAttributes.json?cancer_study_id="+studyId+"&case_list=" + tmpStr,function(data){
                    attr = data;
                    var columnNameSelected = initCharts();
                    columnNameSelected.unshift("CASE_ID");
                    restyle(columnNameSelected,columnNameTotal);
                    //dcFunc(dataObjectM);
                    //printHTML();
                });
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
        var varDisplay = new Array();
        var varCluster = new Array();
        var varGroup = new Array();
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

        
        for(var i=0; i< dataA.length ; i++){
            var varValues = new Array();
            for(var j=0;j<dataB.length;j++){
                if(varValues.hasOwnProperty(dataB[j][dataA[i]["attr_id"]]))
                    varValues[dataB[j][dataA[i]["attr_id"]]]++;
                else
                    varValues[dataB[j][dataA[i]["attr_id"]]]=0;
            }
            
            if(dataA[i]["attr_id"] === "CASE_ID"){
                continue;
            }else if(dataA[i]["datatype"] === "NUMBER" || dataA[i]["datatype"] === "BOOLEAN"){                
                if(Object.keys(varValues).length>10)
                    bar.push(dataA[i]);
                else
                    pie.push(dataA[i]);
            }else if(dataA[i]["datatype"] === "STRING"){
                pie.push(dataA[i]);
            }
            else 
                combine.push(dataA[i]);
        }


        for(var i=0; i< pie.length ; i++){
            $("#pie").append("<div id=\"pie_" + i + "\" class='pie-chart'><pieH4>" + pie[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + i + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4></div>");
            varName.push(pie[i]["attr_id"]);
            varDisplay.push(pie[i]["display_name"]);
            varChart.push(dc.pieChart("#pie_" + i));
        }
        
        for(var i=0,j=pie.length; i< row.length; i++,j++){
            $("#row").append("<div id=\"row_" + i + "\" class='row-chart'><pieH4>" + row[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + j + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4></div>");
            varName.push(row[i]["attr_id"]);
            varDisplay.push(row[i]["display_name"]);
            varChart.push(dc.rowChart("#row_" + i));
        }
        
        for(var i=0,j=pie.length+row.length; i< bar.length ; i++,j++){
            $("#bar").append("<div id=\"bar_" + i + "\" class='bar-chart'><pieH4>" + bar[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + j + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4></div>");
            varName.push(bar[i]["attr_id"]);
            varDisplay.push(bar[i]["display_name"]);
            varChart.push(dc.barChart("#bar_" + i));
        }
        
        var ndx = crossfilter(dataB);
        var all = ndx.groupAll();

        //Initial all pie charts
        for(var i=0; i< pie.length ; i++){
            varCluster[i] = ndx.dimension(function (d) {
                if(!d[varName[i]] || d[varName[i]].toLowerCase()==="unknown" || d[varName[i]].toLowerCase()==="none")
                    return "NA";
                return d[varName[i]];
            });
            varGroup[i] = varCluster[i].group();
            varChart[i]
            .width(200)
            .height(200)
            .radius(70)
            .dimension(varCluster[i])
            .group(varGroup[i])
            .transitionDuration(1200)
            .ordinalColors(chartColors)
            .label(function (d) {
                return d.key + ":" + d.value;
            });
        }
        
        //Initial all row charts
        for(var i=pie.length; i< pie.length + row.length ; i++){
            var rowChartHeight = rowKeys[i-pie.length].length * 25 +50;
            $(varName[i]).css("height",rowChartHeight+50+"px")
            varCluster[i] = ndx.dimension(function (d) {
                if(!d[varName[i]])
                    return "NA";
                return d[varName[i]];
            });
            varGroup[i] = varCluster[i].group();
            
            varChart[i]
            .width(300)
            .height(rowChartHeight)
            .dimension(varCluster[i])
            .group(varGroup[i])
            .transitionDuration(1200)
            .elasticX(true)
            .ordinalColors(chartColors)
            .title(function (d) {
                    return d.value;
            })
            .label(function (d) {
                return d.key + ":" + d.value;
            });
        }
        
        //Initial all bar charts
        for(var i=pie.length + row.length; i< pie.length + row.length + bar.length ; i++){           
            
            var varValues = new Array();
            for(var j=0;j<dataB.length;j++){
                if(dataB[j][varName[i]] && dataB[j][varName[i]]!=="NA" && dataB[j][varName[i]]!=="")
                    varValues.push(dataB[j][varName[i]]);                    
            }
            
            var distanceMinMax = Math.max.apply( Math, varValues ) - Math.min.apply( Math, varValues );
            
            //This should be changed later: run the loop i times which should only run once
            varCluster[i] = ndx.dimension(function (d) {
                var returnValue = d[varName[i]]; 
                if(d[varName[i]] % 1 !== 0 && decimalPlaces(d[varName[i]]) > 3)
                    if(distanceMinMax < 2){
                        returnValue = d3.round(d[varName[i]],2);
                    }
                    else
                        returnValue = d3.round(d[varName[i]]);
                if(returnValue === "NA" || returnValue === '')
                    returnValue = Math.min.apply( Math, varValues )-100;
                //if(i===15)
                //console.log("original Value:" + d[varName[i]] + " Return Value:" + returnValue);
                return returnValue;
            });
            
            var barScale = 50;
            
            varGroup[i] = varCluster[i].group();
                
            varChart[i]
                .width(560)
                .height(200)
                .margins({top: 10, right: 10, bottom: 30, left: 40})
                .dimension(varCluster[i])
                .group(varGroup[i])
                .gap(5)
                .centerBar(true)
                .elasticY(false)
                .mouseZoomable(false)
                .brushOn(true)
                .transitionDuration(1200)
        
            if(distanceMinMax < 1){
                varChart[i].x(d3.scale.linear().nice([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                varChart[i].yAxis().tickFormat(d3.format("d"));
                varChart[i].xAxis().ticks(10);
                varChart[i].xUnits(function(){return barScale;});
            }else if(distanceMinMax < 2){
                varChart[i].x(d3.scale.linear().domain([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                varChart[i].yAxis().tickFormat(d3.format("d"));
                varChart[i].xAxis().ticks(10);
                varChart[i].xUnits(function(){return barScale;});
            }else if(varDisplay[i].search(/month/i) != -1){                
                varChart[i].x(d3.scale.linear().domain([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                varChart[i].xAxis().ticks(10);
                varChart[i].yAxis().tickFormat(d3.format("d"));
                varChart[i].xAxis().tickFormat(d3.format("d"));
            }else{
                varChart[i].x(d3.scale.linear().domain([Math.min.apply( Math, varValues )-distanceMinMax/barScale, Math.max.apply( Math, varValues )+distanceMinMax/barScale]))
                varChart[i].yAxis().tickFormat(d3.format("d"));
                varChart[i].xAxis().tickFormat(d3.format("d"));
                varChart[i].xAxis().ticks(10);
                varChart[i].xUnits(function(){return barScale;});
            }
        }
       
        dataTable = dc.dataTableDataOnly("#dataTable","group1");
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
                }
        ])
        .size(2000)
        .sortBy(function (d) {
                return d.CASE_ID;
        })
        .transitionDuration(1200);
        
        dc.renderAll();
        dc.renderAll("group1");
        
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
        $('#dataTable_header').click(function(){
            if($("#dataTable_filter label input").val() !== ""){			
                    console.log("Inside...1");
                    
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
        $('#dataTable_updateTable').click(function(){
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

var study_view = studyView();
study_view.set_studyId(studyId);
study_view.getData();


$('#dataTable_reset').click(function(){
        dataTable.filter(null);
        dc.redrawAll();
});
        
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}
