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
    var w = window.innerWidth;
    
    $("#dialog-form").css("left",w/2-450-260);

    $( window ).resize(function() {
        var w = window.innerWidth;
        $("#dialog-form").css("left",w/2-450-260);
    });	

    $("#pie").sortable();
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

});
        


var studyId = getParameterByName("cancer_study_id");
var varChart = new Array();
var dataTable;
//console.log(studyId);

var studyView = function(){
    var studyId;
    var dataObjectM = new Array();
    var attr = new Array();
        
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
                        //console.log(usefulData[i]["caseId"]);                
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
                    initCharts();
                    restyle();
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
        var smallPie = new Array();
        var row = new Array();
        var bar = new Array();
        var combine = new Array();
        var table = new Array();
        var num=0;
        var varName = new Array();
        var varDisplay = new Array();
        var varCluster = new Array();
        var varGroup = new Array();
        //var varType = new Array();
        dataA = attr;
        dataB = dataObjectM;

        
        for(var i=0; i< dataA.length ; i++){
            //console.log(dataA[i]["datatype"]);
            if(dataA[i]["attr_id"] === "CASE_ID"){
                continue;
            }else if(dataA[i]["datatype"] === "NUMBER"){
                var varValues = new Array();
                for(var j=0;j<dataB.length;j++){
                    //console.log(dataB[j][dataA[i]["attr_id"]])
                    if(varValues.hasOwnProperty(dataB[j][dataA[i]["attr_id"]]))
                        varValues[dataB[j][dataA[i]["attr_id"]]]++;
                    else
                        varValues[dataB[j][dataA[i]["attr_id"]]]=0;
                }
                if(Object.keys(varValues).length>10)
                    bar.push(dataA[i]);
                else
                    pie.push(dataA[i]);
            }else if(dataA[i]["datatype"] === "STRING"){
                pie.push(dataA[i]);
            }
            else if(dataA[i]["datatype"] === "BOOLEAN"){
                smallPie.push(dataA[i]);
            }
            else 
                combine.push(dataA[i]);
        }


        for(var i=0; i< pie.length ; i++){
            $("#pie").append("<div id=\"" + pie[i]["attr_id"] + "\" class='pie-chart'><pieH4>" + pie[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + i + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4></div>");
            varName.push(pie[i]["attr_id"]);
            varDisplay.push(pie[i]["display_name"]);
            varChart.push(dc.pieChart("#" + pie[i]["attr_id"]));
        }
        
        for(var i=0,j=pie.length; i< bar.length ; i++,j++){
            $("#bar").append("<div id=\"" + bar[i]["attr_id"] + "\" class='bar-chart'><pieH4>" + bar[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + j + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4></div>");
            varName.push(bar[i]["attr_id"]);
            varDisplay.push(bar[i]["display_name"]);
            varChart.push(dc.barChart("#" + bar[i]["attr_id"]));
        }
        
        if(smallPie.length > 0){
            $("#pie").append("<div id='data-chart'></div>");            
            for(var i=0,j=pie.length+bar.length; i< smallPie.length ; i++,j++){
                $("#data-chart").append("<div id=\"" + smallPie[i]["attr_id"] + "\" class='data-pie-chart'><pieH4>" + smallPie[i]["display_name"] + "<a class='reset' href='javascript:varChart[" + i + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4></div>");
                varName.push(smallPie[i]["attr_id"]);
                varDisplay.push(smallPie[i]["display_name"]);
                varChart.push(dc.pieChart("#" + smallPie[i]["attr_id"]));
            }
        }
        
        var ndx = crossfilter(dataB);
        var all = ndx.groupAll();

        for(var i=0; i< pie.length ; i++){
            varCluster[i] = ndx.dimension(function (d) {
                if(!d[varName[i]])
                    return "NA";
                return d[varName[i]];
            });
            varGroup[i] = varCluster[i].group();
            varChart[i]
            .width(180)
            .height(180)
            .radius(86)
            .innerRadius(30)
            .dimension(varCluster[i])
            .group(varGroup[i])
            .transitionDuration(800)
            .label(function (d) {
                return d.key + ":" + d.value;
            });
        }
        
        for(var i=pie.length; i< pie.length + bar.length ; i++){           
            
            var varValues = new Array();
            for(var j=0;j<dataB.length;j++){
                if(dataB[j][varName[i]] && dataB[j][varName[i]]!=="NA" && dataB[j][varName[i]]!=="")
                    varValues.push(dataB[j][varName[i]]);                    
            }
            
            //This should be changed later: run the loop i times which should only run once
            varCluster[i] = ndx.dimension(function (d) {
                var returnValue = d[varName[i]];
                if(d[varName[i]] % 1 !== 0 && decimalPlaces(d[varName[i]]) > 3)
                    if(Math.max.apply( Math, varValues ) - Math.min.apply( Math, varValues )<2){
                        returnValue = d3.round(d[varName[i]],2);
                    }
                    else
                        returnValue = d3.round(d[varName[i]]);
                if(returnValue === 'NA' || returnValue === '')
                    returnValue = 'NaN';
                console.log(returnValue);    
                return returnValue;
            });
            //if(varValues.length===0)
                //continue;
            
            varGroup[i] = varCluster[i].group();
            varChart[i]
            .width(1200)
            .height(200)
            .margins({top: 10, right: 50, bottom: 30, left: 40})
            .dimension(varCluster[i])
            .group(varGroup[i])
            .centerBar(true)
            .elasticY(true)
            .mouseZoomable(false)
            .brushOn(true)
            .transitionDuration(800)
            .x(d3.scale.linear().domain([Math.min.apply( Math, varValues ), Math.max.apply( Math, varValues )]))
            .yAxis().tickFormat(d3.format("d"));
                      
        }
        
        for(var i=pie.length + bar.length; i< pie.length + bar.length + smallPie.length; i++){
            varCluster[i] = ndx.dimension(function (d) {
                return d[varName[i]];
            });
            
            
            var varValues = new Array();
            for(var j=0;j<dataB.length;j++){
                if(dataB[j][varName[i]] && dataB[j][varName[i]]!=="NA" && dataB[j][varName[i]]!=="")
                    varValues.push(dataB[j][varName[i]]);                    
            }
            
            varGroup[i] = varCluster[i].group();
            console.log(varName[i]);
            varChart[i]
            .width(100)
            .height(82)
            .radius(40)
            .dimension(varCluster[i])
            .group(varGroup[i])
            .label(function (d) {
                return d.key + ":" + d.value;
            });
                      
        }
        
        dataTable = dc.dataTableDataOnly("#dataTable");
        var CASEID = ndx.dimension(function (d) {
                return d.CASE_ID;
        });	
        dc.dataCount(".dc-data-count")
                                .dimension(ndx)
                                .group(all);
                        
                /*
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
        .columns(
            dataTableColumns
        )
        .size(500)
        .sortBy(function (d) {
                return d.CASE_ID;
        })
        .transitionDuration(800)
        */
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
        .transitionDuration(800);
        
        dc.renderAll();        
    };    
    
    function restyle(){
        var dataTable1 = $('#dataTable').dataTable({
                "sScrollX": "1200px",
                "sScrollY": "2000px",
                "bPaginate": false,
                "bFilter":true
        });
        
        $('#dataTable_filter').append("<input type=\"button\" id=\"dataTable_header\" class='header_button' value = \"Refresh Charts\"/>");
        $('#dataTable_filter').append("<input type=\"button\" id=\"dataTable_reset\" class='header_button' value = \"Reset\" />");
        $("#dataTable_filter label input").attr("value","");
        $('#dataTable_header').click(function(){
            if($("#dataTable_filter label input").val() !== ""){			
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