/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var StudyViewInitCharts = (function(){
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
    };

    var ndx = "";
    var attrNameMapUID = []; //The relationshio between "The unique attribute name" and "The unique ID number in whole page"        
    var varCluster = []; //Clusters of displayed charts -- DC.JS require
    var varGroup = []; //Groups of displayed charts -- DC.JS require
    var chartColors = ["#3366cc","#dc3912","#ff9900","#109618",
        "#990099","#0099c6","#dd4477","#66aa00",
        "#b82e2e","#316395","#994499","#22aa99",
        "#aaaa11","#6633cc","#e67300","#8b0707",
        "#651067","#329262","#5574a6","#3b3eac",
        "#b77322","#16d620","#b91383","#f4359e",
        "#9c5935","#a9c413","#2a778d","#668d1c",
        "#bea413","#0c5922","#743411"]; // Color scale from GOOGLE charts

    var scatterStudyView;
    
    function initParameters(o) {
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
    }

    function initCharts(dataObtained) {
        var dataA = []; //Atrributes
        var dataB = []; //All data
        var pie = []; //Displayed attributes info, dataType: STRING, NUMBER, OR BOOLEAN
        var bar = []; //Displayed attributes info, dataType: NUMBER
        var combine = []; //Combine charts which created for unrecognizable data type
        var varName = []; //Store all attributes in all data
        var HTMLtagsMapUID = []; //The relationshio between "The ID of div standing for each Chart in HTML" and "The unique ID number in whole page"
        var displayedID = []; //Displayed Charts ID number
        var varDisplay = []; //Displayed Charts Name -- the display_name in each attribute       
        var varType = []; //Could be displayed Charts Type 'pie,bar'. Mix combination, seperate by comma
        var distanceMinMaxArray = []; //Only for NUMBER dataType, store min, max and difference value
        
        
        dataA = dataObtained.attr;
        dataB = dataObtained.dataObjectM;
        
        
        //Calculate the number of pie, bar charts
        //Initial varName, varType, distanceMinMaxArray, varDisplay
        for(var i=0; i< dataA.length ; i++){
            var varValuesNum = [];
            for(var j=0;j<dataB.length;j++){
                if(varValuesNum.hasOwnProperty(dataB[j][dataA[i]["attr_id"]]))
                    varValuesNum[dataB[j][dataA[i]["attr_id"]]]++;
                else
                    varValuesNum[dataB[j][dataA[i]["attr_id"]]]=0;
            }
            var keys = Object.keys(varValuesNum);
            
            if(dataA[i]["attr_id"] === "CASE_ID"){
                pie.push(dataA[i]);
                //continue;
            }else if(dataA[i]["datatype"] === "NUMBER" || dataA[i]["datatype"] === "BOOLEAN"){                
                if(selectedCol(dataA[i]["attr_id"])){                    
                    if(keys.length>10 || dataA[i]["attr_id"] === 'MUTATION_COUNT' || dataA[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS')
                        bar.push(dataA[i]);
                    else
                        pie.push(dataA[i]);
                }

                if(keys.length > 10 || dataA[i]["attr_id"] === 'MUTATION_COUNT' || dataA[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS')
                    varType[dataA[i]["attr_id"]] = "bar";
                else
                    varType[dataA[i]["attr_id"]] = "pie";
                
                if(dataA[i]["datatype"] === "NUMBER"){
                    var varValues = [];
                    for(var j=0;j<dataB.length;j++){
                        if(dataB[j][dataA[i]["attr_id"]] && dataB[j][dataA[i]["attr_id"]]!=="NA" && dataB[j][dataA[i]["attr_id"]]!=="")
                            varValues.push(dataB[j][dataA[i]["attr_id"]]);                    
                    }

                    distanceMinMaxArray[dataA[i]["attr_id"]] = {
                        distance : Math.max.apply( Math, varValues ) - Math.min.apply( Math, varValues ),
                        min: Math.min.apply( Math, varValues ),
                        max:Math.max.apply( Math, varValues )
                    };
                }

            }else if(dataA[i]["datatype"] === "STRING"){
                if(selectedCol(dataA[i]["attr_id"])){
                    pie.push(dataA[i]);
                }

                varType[dataA[i]["attr_id"]] = "pie";
            }else 
                combine.push(dataA[i]);

            varDisplay.push(dataA[i]["display_name"]);                
            varName.push(dataA[i]["attr_id"]);
        }

        ndx = crossfilter(dataB);
            
        var totalCharts = pie.length + bar.length;
        var createdChartID = 0;

        for(var i=0; i< pie.length ; i++){
            initPieChart(createdChartID,'study-view-pie-chart',pie[i]["attr_id"],pie[i]["display_name"]);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[pie[i]["attr_id"]] = createdChartID;
            displayedID.push(pie[i]["attr_id"]);
            createdChartID++;
        }

        for(var i=0; i< bar.length ; i++){
            initBarChart(createdChartID,'study-view-bar-chart',bar[i]["attr_id"],bar[i]["display_name"],distanceMinMaxArray);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[bar[i]["attr_id"]] = createdChartID;
            displayedID.push(bar[i]["attr_id"]);            
            createdChartID++;
        }

        if(createdChartID !== totalCharts){
            console.log("Initial charts function error: the number of created charts not equal to number of totalCharts. --1");
            return false;
        }

        dc.renderAll();
        dc.renderAll("group1");
       
        $('#study-view-selectAttr')
            .find('option:gt(0)')
            .remove()
            .end();
            
        var hasDisplayItem = false;
        $.each(varName, function(key, value) {
            if(displayedID.indexOf(value) === -1){
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",value)
                        .text(varDisplay[key]));
                hasDisplayItem = true;
            }
        });
            
        if(!hasDisplayItem){
            $('#study-view-selectAttr').attr('disabled','disabled');
        }
        
        var scatterPlotDataAttr = {
            min_x: distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].min,
            max_x: distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].max,
            min_y: distanceMinMaxArray['MUTATION_COUNT'].min,
            max_y: distanceMinMaxArray['MUTATION_COUNT'].max,
            mut_x : false, //have case(s) mutated in only gene x
            mut_y : false,  //have case(s) mutated in only gene y
            mut_both: false //have case(s) mutated in both genes
        };
        
        var scatterPlotOptions = {
            canvas : {  //position of components
                width: 560,
                height: 440,
                xLeft: 100,     //The left/starting point for x axis
                xRight: 500,   //The right/ending point for x axis
                yTop: 10,      //The top/ending point for y axis
                yBottom: 350   //The bottom/starting point for y axis
            },
            style : { //Default style setting
                fill: "#3366cc", //light blue
                stroke: "#0174DF", //dark blue
                stroke_width: "1.2",
                size: "60",
                shape: "circle" //default, may vary for different mutation types
            },

            names: { 
                div: "study-view-scatter-plot",
                header: "study-view-scatter-plot-header",
                body: "study-view-scatter-plot-body",
                loading_img: "study-view-scatter-plot-loading-img",
                control_panel: "study-view-scatter-plot-control-panel",
                log_scale_x: "study-view-scatter-plot-log-scale-x",
                log_scale_y: "study-view-scatter-plot-log-scale-y"        
            },
            elem : {
                svg: "",
                xScale: "",
                yScale: "",
                xAxis: "",
                yAxis: "",
                dotsGroup: "",
                axisGroup: "",
                axisTitleGroup: "",
                brush: ""
            },
            text: {
                xTitle: "Fraction of copy number altered genome",
                yTitle: "# of mutations",
                title: "Mutation Count vs Copy Number Alterations",
                fileName: "",
                xTitleHelp: "Fraction of genome that has log2 copy number value above 0.2 or bellow -0.2",
                yTitleHelp: "Number of sometic non-synonymous mutations"
            },
            legends: []
        };    
        var scatterPlotDatum = {              
            x_val: "",
            y_val: "",
            case_id: "",
            qtip: "",
            stroke: "",
            fill: ""
        };
        var scatterPlotArr = [];

        $.each(dataB, function(i,value) {
            if(!isNaN(value['COPY_NUMBER_ALTERATIONS']) && !isNaN(value['MUTATION_COUNT']) && value['COPY_NUMBER_ALTERATIONS'] !="" && value['MUTATION_COUNT']!="") {                  
                var scatterPlotDatumTmp= {};
                scatterPlotDatumTmp.x_val = value['COPY_NUMBER_ALTERATIONS'];
                scatterPlotDatumTmp.y_val = value['MUTATION_COUNT'];
                scatterPlotDatumTmp.case_id = value['CASE_ID'];
                scatterPlotDatumTmp.qtip = "Case ID: <strong><a href='tumormap.do?case_id=" + 
                     value['CASE_ID'] + "&cancer_study_id=" +
                     parObject.studyId + "' target='_blank'>" + 
                     value['CASE_ID'] + "</a></strong>";
                scatterPlotArr.push(scatterPlotDatumTmp);
            }
        });
        
        if(scatterPlotArr.length !== 0){
            scatterStudyView = new ScatterPlots();
            scatterStudyView.init(scatterPlotOptions, scatterPlotArr, scatterPlotDataAttr,true);            
            scatterStudyView.jointBrushCallback(scatterPlotCallBack);
            
            $("#" + scatterPlotOptions.names.log_scale_x).change(function() {
                scatterStudyView.updateScaleX(scatterPlotOptions.names.log_scale_x);
            });
            $("#" + scatterPlotOptions.names.log_scale_y).change(function() {
                scatterStudyView.updateScaleY(scatterPlotOptions.names.log_scale_y);
            });
            $(".study-view-scatter-plot-delete").click(function (){
                $("#study-view-scatter-plot").css('display','none');
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",'mutationCNA')
                        .text("Number of Mutation vs Fraction of copy number altered genome"));
                msnry.layout();
            });            
        }else{
            $('#study-view-scatter-plot').css('display','none');
        }
        var container = document.querySelector('#study-view-charts');
        var msnry = new Masonry( container, {
            columnWidth: 190,
            itemSelector: '.study-view-dc-chart',
            gutter:1
        });


        $('#study-view-selectAttr').change(function(){
            if($("#study-view-selectAttr")[0].selectedIndex != 0){
                $('#study-view-selectChartType').removeAttr('disabled');

                var chartType;
                if($(this).val() === 'mutationCNA')
                    chartType = ['scatter'];
                else
                    chartType = varType[$(this).val()].split(',')
                $('#study-view-selectChartType')
                        .find('option')
                        .remove()
                        .end();

                $.each(chartType, function(key, value) {
                    $('#study-view-selectChartType')
                        .append($("<option></option>")
                            .attr("value",value)
                            .text(value + " chart"));
                });
                if($('#study-view-selectAttr').find(":selected").val())
                    $('#study-view-add-chart-button').removeAttr('disabled');  
            }else{
                $('#study-view-selectChartType')
                        .find('option')
                        .remove()
                        .end()
                        .append('<option>No chart type could be selected</option>')
                        .attr('disabled','disabled');
                $('#study-view-add-chart-button').attr('disabled','disabled');
            }
        });
        
        $('.study-view-dc-chart-delete').click(function(event){
                var id = $(this).parent().parent().attr("id");
                var valueA = $(this).parent().parent().attr('value').split(',');
                var attrID = valueA[0];
                var attrName = valueA[1];
                $("div").remove("#" + id + "-main"); 
                varChart[HTMLtagsMapUID[id]].filterAll();
                dc.redrawAll();
                dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);
                msnry.layout();
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",attrID)
                        .text(attrName));
                $('#study-view-selectAttr').removeAttr('disabled');
                var filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
                var filterString = "";
                for(var i=0 ; i<filteredResult.length ; i++){
                    filterString += filteredResult[i].CASE_ID + '|';
                }
                filterString = filterString.substr(0,filterString.length-1);
                var dataTable1 = $('#dataTable').dataTable();
                dataTable1.fnFilter(filterString,0,true);
        });

        $('#study-view-add-chart-button').click(function(){
            var selectedAttr = $('#study-view-selectAttr').find(":selected").val();
            var selectedAttrDisplay = $('#study-view-selectAttr').find(":selected").text();
            var selectedChartType = $('#study-view-selectChartType').find(":selected").val();
            var chartTmpID = -1;
            
            if(selectedAttr==='mutationCNA' && selectedChartType === 'scatter'){
                $("#study-view-scatter-plot").css('display','block');
            }else{
                if(Object.keys(attrNameMapUID).indexOf(selectedAttr) !== -1){
                    chartTmpID = attrNameMapUID[selectedAttr];
                }else{
                    chartTmpID = totalCharts;
                    HTMLtagsMapUID["study-view-dc-chart-" + totalCharts] = totalCharts;
                    attrNameMapUID[selectedAttr] = totalCharts;
                    totalCharts++;       
                }
                if(selectedChartType == 'pie'){
                    initPieChart(chartTmpID,'study-view-pie-chart',selectedAttr,selectedAttrDisplay);
                }else{
                    initBarChart(chartTmpID,'study-view-bar-chart',selectedAttr,selectedAttrDisplay,distanceMinMaxArray);                
                }
                
                
                msnry.destroy();
                var container = document.querySelector('#study-view-charts');
                msnry = new Masonry( container, {
                  columnWidth: 190,
                  itemSelector: '.study-view-dc-chart',
                  gutter:1
                });

                varChart[chartTmpID].render();
                 $('#study-view-dc-chart-'+ chartTmpID +' .study-view-dc-chart-delete').click(function(event){
                    var id = $(this).parent().parent().attr("id");
                    var valueA = $(this).parent().parent().attr("value").split(',');
                    var attrID = valueA[0];
                    var attrName = valueA[1];
                    $("div").remove("#" + id + "-main"); 
                    varChart[HTMLtagsMapUID[id]].filterAll();
                    dc.redrawAll();
                    dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);
                    msnry.layout();
                    $('#study-view-selectAttr')
                        .append($("<option></option>")
                            .attr("value",attrID)
                            .text(attrName));
                    $('#study-view-selectAttr').removeAttr('disabled');
                    var filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
                    var filterString = "";
                    for(var i=0 ; i<filteredResult.length ; i++){
                        filterString += filteredResult[i].CASE_ID + '|';
                    }
                    filterString = filterString.substr(0,filterString.length-1);
                    var dataTable1 = $('#dataTable').dataTable();
                    dataTable1.fnFilter(filterString,0,true);
                });
            }
            
            msnry.layout();

            $("#study-view-selectAttr option[value='"+ selectedAttr +"']").remove();
            if ($('#study-view-selectAttr option').length === 1)
                 $('#study-view-selectAttr').attr('disabled','disabled');

            $('#study-view-selectChartType')
                    .find('option')
                    .remove()
                    .end()
                    .append('<option>No chart type could be selected</option>')
                    .attr('disabled', 'disabled');

            $('#study-view-add-chart-button').attr("disabled", "disabled");

        });
    }

    function initPieChart(_chartID,_className,_selectedAttr,_selectedAttrDisplay) {
        if(_selectedAttr === 'CASE_ID'){
            $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID
                    + "-main\" class='study-view-dc-chart study-view-pie-main' style='display:none'><div id=\"study-view-dc-chart-"
                    + _chartID + "\"></div></div>");
        }else
            $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID
                + "-main\" class='study-view-dc-chart study-view-pie-main'><div id=\"study-view-dc-chart-" + _chartID + "\" class='" + _className 
                + "'  value='"+ _selectedAttr + "," + _selectedAttrDisplay + ",pie'><div style='width:180px; float:right; text-align:center;'>\n\
                <span class='study-view-dc-chart-delete'>x</span><a href='javascript:varChart[" + _chartID 
                + "].filterAll();dc.redrawAll();'><span title='Reset Chart' class='study-view-dc-chart-change' style='font-size:10px;'>\n\
                RESET</span></a></div><div style='width:180px;float:left;text-align:center'><pieH4>" + _selectedAttrDisplay + 
                "</pieH4></div></div><div class='study-view-pie-label'></div>\n\
                <div style='width:180px; text-align:center;float:left;'></div></div>");
        
        varChart[_chartID] = dc.pieChart("#study-view-dc-chart-" + _chartID);
        varCluster[_chartID] = ndx.dimension(function (d) {
            if(!d[_selectedAttr] || d[_selectedAttr].toLowerCase()==="unknown" || d[_selectedAttr].toLowerCase()==="none")
                return "NA";
            return d[_selectedAttr];
        });
        varGroup[_chartID] = varCluster[_chartID].group();
        var tmpKeyArray = [];
        var pieWidth = 130;
        var pieRadius = (pieWidth - 20) /2;
        varChart[_chartID]
        .width(pieWidth)
        .height(pieWidth)
        .radius(pieRadius)
        .dimension(varCluster[_chartID])
        .group(varGroup[_chartID])
        .transitionDuration(600)
        .ordinalColors(chartColors)
        .label(function (d) {
            return d.value;
        })
        .ordering(function(d){ return d.key;});
        //Use to refresh Scatter Plot when click pie slice.
        
        if(_selectedAttr !== 'CASE_ID'){
            varChart[_chartID].on("filtered", function(chart,filter){
                var currentPieFilters = varChart[_chartID].filters();
                if(currentPieFilters.length === 0){
                    $("#study-view-dc-chart-" + _chartID + "-main .study-view-dc-chart-change").css('display','none');
                    $("#study-view-dc-chart-" + _chartID + "-main").css({'border-width':'1px', 'border-style':'solid'});
                }
                else{
                    $("#study-view-dc-chart-" + _chartID + "-main .study-view-dc-chart-change").css('display','block');
                    $("#study-view-dc-chart-" + _chartID + "-main").css({'border-width':'2px', 'border-style':'inset'});
                }
                var tmpDimention = varChart[attrNameMapUID["CASE_ID"]].dimension();
                var tmpResult = tmpDimention.top(Infinity);
                var tmpCaseID = [];
                
                if(typeof scatterStudyView !== 'undefined'){
                    for(var i=0; i<tmpResult.length ; i++){
                        tmpCaseID.push(tmpResult[i].CASE_ID);
                    }
                    setScatterPlotStyle(tmpCaseID,currentPieFilters);
                }
            });
            varChart[_chartID].on("postRedraw",function(chart){
                addPieLabels("study-view-dc-chart-" + _chartID);
            });
            varChart[_chartID].on("postRender",function(chart){
                addPieLabels("study-view-dc-chart-" + _chartID);
            });
        }
    }
    
    function addPieLabels(_pieChartID) {
        var label =[];
        var labelID = 0;
        var labelSize = 10;
        var fontSize = labelSize +1;
        
        $('#' + _pieChartID + '-main .study-view-pie-label').html("");
          
        $('#' + _pieChartID + '>svg>g>g').each(function(){
            var labelDatum = {};            
            var labelName = $(this).find('title').text().split(':');
            var color = $(this).find('path').attr('fill');            
            var tmpPointsInfo = $(this).find('path').attr('d').split(/[\s,MLHVCSQTAZ]/);            
            
            var x1 = Number(tmpPointsInfo[1]);
            var y1 = Number(tmpPointsInfo[2]);
            var x2 = Number(tmpPointsInfo[8]);
            var y2 = Number(tmpPointsInfo[9]);
            //addPieSliceQTip(this,labelName[0]);
            if(x1 !== x2 || y1 !== y2){
                labelDatum.id = labelID;
                labelDatum.name = labelName[0];
                labelDatum.color = color;
                labelDatum.parentID = _pieChartID;
                labelDatum.value = labelName[1];
                label.push(labelDatum);
            }
            labelID++;            
        });
        
        var totalTableNum = parseInt(label.length/5) + 1;
        
        if(label.length > 6){
            for(var j = 0 ; j < label.length ; j+=5){
                var tmpEndNum = j+5;
                var innerID = 0;                
                var tableId = parseInt(j/5);
                var showTableStyle = '';
                
                if(tableId !== 0)  
                    showTableStyle = 'style="display:none"';
                
                $('#' + _pieChartID + '-main')
                        .find('.study-view-pie-label')
                        .append("<table id='table-"+_pieChartID+"-"+tableId+"' "
                            + showTableStyle + " ></table>");
                
                for(var i=j; i< tmpEndNum; i++){
                    if(i<label.length){
                        var tmpName = label[i].name;
                        if(tmpName.length > 9)
                           tmpName = tmpName.substring(0,5) + " ...";
                        if((i-tableId) % 2 === 0){
                            $('#' + _pieChartID + '-main')
                                    .find('#table-'+_pieChartID+'-'+tableId)
                                    .append("<tr id="+ innerID +" width='150px'></tr>");
                            innerID++;
                        } 
                        $('#' + _pieChartID + '-main')
                                .find('#table-'+_pieChartID+'-'+tableId+
                                    ' tr:nth-child(' + innerID +')')
                                .append('<td class="pieLabel" id="pieLabel-'
                                    +_pieChartID+'-'+label[i].id+"-"+i
                                    +'" style="font-size:'
                                    +fontSize+'px"><svg width="75" height="13"><rect width="'
                                    +labelSize+'" height="'+labelSize+'" style="fill:' 
                                    + label[i].color + ';" /><text x="15" y="10">'
                                    +tmpName+'</text></svg></td>');
                            
                        $('#pieLabel-'+_pieChartID+'-'+i).qtip({
                            content:{text: label[i].name},
                            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'right bottom',at:'top left'}
                        });
                    }else{
                        if((i-tableId) % 2 === 0){
                            $('#' + _pieChartID + '-main')
                                    .find('#table-'+_pieChartID+'-'+tableId)
                                    .append("<tr id="+ innerID +" width='150px'></tr>");
                            innerID++;
                        } 
                        $('#' + _pieChartID + '-main')
                                .find('#table-'+_pieChartID+'-'+tableId+' tr:nth-child(' + innerID +')')
                                .append('<td style="width="75px" height="15px" font-size:'
                                    +fontSize+'px"></td>');
                    }
                }
                
                var leftArrowColor = 'blue';
                var rightArrowColor = 'blue';
                if(tableId === 0)
                    leftArrowColor = 'grey';
                if(tableId+1 === totalTableNum)
                    rightArrowColor = 'grey';
                
                $('#' + _pieChartID + '-main').find('#table-'+_pieChartID+'-'
                    +tableId+' tr:nth-child(' + innerID +')').append('<td id="pieLabel-pagging-'
                    +_pieChartID+"-"+tableId+'" style=" width="75px" height="16px" border="0px" font-size:'+fontSize
                    +'px"><svg  width="75" height="13">\n\
                            <path class="pie-label-left-pagging" d="M5 1 L0 11 L10 11 Z" fill="'+leftArrowColor+'"/>\n\
                            <text x=15 y=10 fill="black">'+(tableId+1)+'/'+totalTableNum+'</text>\n\
                            <path class="pie-label-right-pagging" d="M45 11 L40 1 L50 1 Z" fill="'+rightArrowColor+'"/>\n\
                          </svg></td>');
            
            }
            
        }else{
            var innerID = 0;
            $('#' + _pieChartID + '-main')
                    .find('.study-view-pie-label')
                    .append("<table id=table-"+_pieChartID+"-0></table>");
                
            for(var i=0; i< label.length; i++){
                var tmpName = label[i].name;
                if(tmpName.length > 9)
                   tmpName = tmpName.substring(0,5) + " ...";
                if(i % 2 === 0){
                    $('#' + _pieChartID + '-main')
                            .find('table')
                            .append("<tr id="+ innerID +" width='150px'></tr>");
                    innerID++;
                } 
                $('#' + _pieChartID + '-main')
                        .find('table tr:nth-child(' + innerID +')')
                        .append('<td class="pieLabel" id="pieLabel-'
                            +_pieChartID+'-'+label[i].id+'-'+i+'"  style="font-size:'
                            +fontSize+'px"><svg width="75" height="13"><rect width="'
                            +labelSize+'" height="'+labelSize+'" style="fill:'
                            + label[i].color + ';" /><text x="15" y="10">'
                            +tmpName+'</text></svg></td>');
                    
                $('#pieLabel-'+_pieChartID+'-'+i).qtip({
                    content:{text: label[i].name},
                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
                    show: {event: "mouseover"},
                    hide: {fixed:true, delay: 100, event: "mouseout"},
                    position: {my:'right bottom',at:'top left'}
                });
            }
        }
        
        $('#' + _pieChartID + '-main .pieLabel').mouseenter(function(){
            var idArray = $(this).attr('id').split('-');
            var childID = Number(idArray[idArray.length-2])+1;
            $('#' + _pieChartID + ' svg>g>g:nth-child(' + childID+')').css({
                'fill-opacity': '.5',
                'stroke-width': '3px'
            });
            
            var fatherID = Number(idArray[idArray.length-3]);
            
            var tmpPointsInfo = $('#' + _pieChartID + ' svg>g>g:nth-child(' + childID+')').find('path').attr('d').split(/[\s,MLHVCSQTAZ]/);            
            var tmpPointsInfo1 = $('#' + _pieChartID + ' svg>g>g:nth-child(' + childID+')').find('path').attr('d').split(/[A]/);
            
            var x1 = Number(tmpPointsInfo[1]);
            var y1 = Number(tmpPointsInfo[2]);
            var largeArc = Number(tmpPointsInfo[6]);
            var sweep = Number(tmpPointsInfo[7]);
            var x2 = Number(tmpPointsInfo[8]);
            var y2 = Number(tmpPointsInfo[9]);
            var r = Number(tmpPointsInfo[3]);
            
            if((x1-x2!==0 ||y1-y2!==0) && tmpPointsInfo1.length === 2){
                var xm = (x1 + x2) /2;
                var ym = (y1 + y2) /2;  
                var m = Math.sqrt((Math.pow(xm,2)+Math.pow(ym,2)));
                var tmpX = (r + 3) / m * xm;
                var tmpY = (r + 3) / m * ym;
                
                if(largeArc === 1 && Math.abs(x1 - x2) >0.1) {
                    tmpX = -tmpX;
                    tmpY = -tmpY;
                }

                var circleID = fatherID+"-"+(Number(childID)-1);
                var circle= makeSVG('circle', {id: circleID, cx: tmpX, cy: tmpY, r:3, stroke: 'red', 'stroke-width': 1, fill: 'red'});
                document.getElementById(_pieChartID).getElementsByTagName('svg')[0].getElementsByTagName('g')[0].appendChild(circle);
            }
        });
        
        $('#' + _pieChartID + '-main .pieLabel').mouseleave(function(){
            var idArray = $(this).attr('id').split('-');
            var childID = Number(idArray[idArray.length-2])+1;
            var fatherID = Number(idArray[idArray.length-3]);
            var circleID = fatherID+"-"+(Number(childID)-1);
            
            $("#" + _pieChartID + " svg g #" + circleID).remove();
            
            $('#' + _pieChartID + ' svg>g>g:nth-child(' + childID+')').css({
                'fill-opacity': '1',
                'stroke-width': '1px'
            });
        });
        
        $('#' + _pieChartID + '-main .pieLabel').click(function(){
            var idArray = $(this).attr('id').split('-');
            var childaLabelID = Number(idArray[idArray.length-1]);
            var childID = Number(idArray[idArray.length-2])+1;
            var chartID = Number(idArray[idArray.length-3]);
            var circleID = chartID+"-"+(Number(childID)-1);
            
            varChart[chartID].onClick({key: label[childaLabelID].name, value:label[childaLabelID].value});            
            varChart[chartID].redraw;           
            
            $("#" + _pieChartID + " svg g #" + circleID).remove();
            
            $('#' + _pieChartID + ' svg>g>g:nth-child(' + childID+')').css({
                'fill-opacity': '1',
                'stroke-width': '1px'
            });
        });
        
        $('#' + _pieChartID + '-main .pie-label-left-pagging').click(function(){
            var tmpValue = $(this).parent().parent().attr('id').split('-');
            var currentTableID = Number(tmpValue[tmpValue.length-1]);
            if(currentTableID !== 0){
                var nextTableID = currentTableID-1;

                $('#table-'+_pieChartID+'-'+currentTableID).css('display','none');            
                $('#table-'+_pieChartID+'-'+nextTableID).css('display','block');
            }
        });
        $('#' + _pieChartID + '-main .pie-label-right-pagging').click(function(){
            var fill = $(this).attr('fill');
            if(fill === 'blue'){
                var tmpValue = $(this).parent().parent().attr('id').split('-');
                var currentTableID = Number(tmpValue[tmpValue.length-1]);
                var nextTableID = currentTableID+1;
            
                $('#table-'+_pieChartID+'-'+currentTableID).css('display','none');            
                $('#table-'+_pieChartID+'-'+nextTableID).css('display','block');
            }
        });
    }
    
    function makeSVG(tag, attrs) {
        var el= document.createElementNS('http://www.w3.org/2000/svg', tag);
        for (var k in attrs)
            el.setAttribute(k, attrs[k]);
        return el;
    }
            
    function initBarChart(_chartID,_className,_selectedAttr,_selectedAttrDisplay,distanceMinMaxArray) {
        var distanceMinMax = distanceMinMaxArray[_selectedAttr].distance;
        
        var seperateDistance;
        var tmpMaxDomain;
        var numOfGroups = 10;
        var startPoint;
        var emptyValueMapping;
        var monthDomain = [];
        var monthRange = ['NA'];
        var distanceLength;
        var divider = 1;
        
        distanceLength = parseInt(distanceMinMax).toString().length;
        for(var i=0;i<distanceLength-2;i++)
            divider *= 10;
        if(distanceMinMaxArray[_selectedAttr].max < 100 && distanceMinMaxArray[_selectedAttr].max > 20)
            divider = 10;
        /*
        if(distanceMinMax > 1000 && _selectedAttrDisplay.search(/mutation/i) !== -1){
            
            //if(distanceMinMaxArray[_selectedAttr].max > divider *)
            seperateDistance = (parseInt(distanceMinMax / (10 * divider)) + 1) * divider;
            tmpMaxDomain = (parseInt(distanceMinMaxArray[_selectedAttr].max / divider) + 1) * divider;
            numOfGroups = tmpMaxDomain/seperateDistance;
            startPoint = 0;
            emptyValueMapping = startPoint-seperateDistance;
        }else 
        */
        if(distanceMinMaxArray[_selectedAttr].max <=1 && distanceMinMaxArray[_selectedAttr].max > 0 && distanceMinMaxArray[_selectedAttr].min>=-1 && distanceMinMaxArray[_selectedAttr].min<0){
            seperateDistance = 0.2;
            startPoint = (parseInt(distanceMinMaxArray[_selectedAttr].min / 0.2)-1) * 0.2;
            emptyValueMapping = startPoint -0.2;
        }else if(distanceMinMax > 1){
            seperateDistance = (parseInt(distanceMinMax / (numOfGroups * divider)) + 1) * divider;
            tmpMaxDomain = (parseInt(distanceMinMaxArray[_selectedAttr].max / divider) + 1) * divider;
            startPoint = parseInt(distanceMinMaxArray[_selectedAttr].min / divider) * divider;
            emptyValueMapping = startPoint-seperateDistance;
        }else if(distanceMinMax < 1 && distanceMinMaxArray[_selectedAttr].min >=0 ){
            seperateDistance = 0.1;
            startPoint = 0;
            emptyValueMapping = -0.1;
        }else{
            seperateDistance = 0.1;
            startPoint = -1;
            emptyValueMapping = -1.1;
        }
            
        monthDomain.push(Number(cbio.util.toPrecision(Number(emptyValueMapping),5,0.1)));

        for(var i=0; i< numOfGroups ; i++){
            var tmpValue = i*seperateDistance + startPoint;
            if(tmpValue > distanceMinMaxArray[_selectedAttr].max)
                break;
            tmpValue = Number(cbio.util.toPrecision(Number(tmpValue),5,0.1));
            monthDomain.push(tmpValue);
            monthRange.push(tmpValue.toString());
        }        
        
        var logCheckBox = "";
        
        if(distanceMinMax > 1000)
            logCheckBox = "<span style='float:right; font-size:10px; margin-right: 15px;margin-top:3px;color: grey'>Log Scale X</span><input type='checkbox' value='"
                + _chartID +","+ distanceMinMaxArray[_selectedAttr].max +","+ distanceMinMaxArray[_selectedAttr].min + "," + emptyValueMapping + "," + seperateDistance
                +"' class='study-view-bar-x-log'></input>'";
        
        $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID 
                + "-main\" class='study-view-dc-chart study-view-bar-main'><div id=\"study-view-dc-chart-" 
                + _chartID + "\" class='"+ _className +"'  value='" + _selectedAttr + "," + _selectedAttrDisplay 
                + ",bar'><div style='width:100%; float:right'><span class='study-view-dc-chart-delete'>x</span><a href='javascript:varChart[" 
                + _chartID + "].filterAll();dc.redrawAll();'><span title='Reset Chart' class='study-view-dc-chart-change'>\n\
                RESET</span></a>"+logCheckBox +"</div></div><div style='width:100%; float:center;text-align:center;'><pieH4>"
                + _selectedAttrDisplay + "</pieH4></div></div>");
        
        varChart[_chartID] = dc.barChart("#study-view-dc-chart-" + _chartID);
        if(_selectedAttrDisplay.search(/mutation/i) !== -1 && distanceMinMax > 1000){
            varCluster[_chartID] = ndx.dimension(function (d) {
                var returnValue = d[_selectedAttr];
                if(d[_selectedAttr] % 1 !== 0 && decimalPlaces(d[_selectedAttr]) > 3){
                    if(distanceMinMax < 2){
                        returnValue = d3.round(d[_selectedAttr],2);
                    }
                    else
                        returnValue = d3.round(d[_selectedAttr]);
                }
                if(returnValue === "NA" || returnValue === '' || returnValue === 'NaN'){
                    //returnValue = distanceMinMaxArray[_selectedAttr].max + 100;
                    return null;
                }
                return returnValue;
            });
        }else{
            varCluster[_chartID] = ndx.dimension(function (d) {
                    var returnValue = d[_selectedAttr];
                    if(returnValue === "NA" || returnValue === '' || returnValue === 'NaN'){
                        return emptyValueMapping;
                    }else{
                        if(d[_selectedAttr] >= 0)
                            returnValue =  parseInt(d[_selectedAttr] / seperateDistance) * seperateDistance;
                        else
                            returnValue =  (parseInt(d[_selectedAttr] / seperateDistance)-1) * seperateDistance;
                        return returnValue;
                    }
                });
        }
        varGroup[_chartID] = varCluster[_chartID].group();

        varChart[_chartID]
            .width(370)
            .height(180)
            .margins({top: 10, right: 20, bottom: 30, left: 40})
            .dimension(varCluster[_chartID])
            .group(varGroup[_chartID])
            .centerBar(true)
            .elasticY(true)
            .elasticX(false)
            .turnOnControls(true)
            .mouseZoomable(false)
            .brushOn(true)
            .transitionDuration(600)
            .renderHorizontalGridLines(true)
            .renderVerticalGridLines(true);
        
        if(_selectedAttrDisplay.search(/mutation/i) !== -1 && distanceMinMax > 1000){ 
            if(distanceMinMaxArray[_selectedAttr].distance)
                varChart[_chartID].elasticX(true);
            varChart[_chartID].x(d3.scale.linear().domain([0, distanceMinMaxArray[_selectedAttr].max+(distanceMinMaxArray[_selectedAttr].distance)/10]));
            varChart[_chartID].yAxis().tickFormat(d3.format("d"));
            varChart[_chartID].xAxis().tickFormat(d3.format("d"));
        }else{
            varChart[_chartID].centerBar(true);
            varChart[_chartID].x(d3.scale.linear().domain([monthDomain[0]-seperateDistance,monthDomain[monthDomain.length-1]+seperateDistance]));
            varChart[_chartID].yAxis().tickFormat(d3.format("d"));            
            varChart[_chartID].xAxis().tickFormat(function(v) {
                if(v === emptyValueMapping) 
                    return 'NA'; 
                else {
                    var tmpRangeValue = v.toString();
                    if(tmpRangeValue.length > 3 && v > 1){
                        var tmpRangeValue1 = parseInt(tmpRangeValue) / (Math.pow(10, tmpRangeValue.length-1)) + "e" + (tmpRangeValue.length-1);
                        var tmpRangeValue2 = parseInt(v + seperateDistance) / (Math.pow(10, tmpRangeValue.length-1)) + "e" + (tmpRangeValue.length-1);
                        return tmpRangeValue1 + "~" + tmpRangeValue2;
                    }else{
                        var upperNum = v+seperateDistance;
                        return v + "~" + Number(cbio.util.toPrecision(Number(upperNum),5,0.1));
                    }
                }
            });            
            varChart[_chartID].xAxis().tickValues(monthDomain);
            var xunitsNum = monthDomain.length*1.5;
            if(xunitsNum <= 5)
                varChart[_chartID].xUnits(function(){return 5;});
            else
                varChart[_chartID].xUnits(function(){return xunitsNum;});
       }
        
        varChart[_chartID].on("filtered", function(chart,filter){
                var tmpDimention = varChart[attrNameMapUID["CASE_ID"]].dimension();
                var tmpResult = tmpDimention.top(Infinity);
                var tmpCaseID = [];
                var currentPieFilters = varChart[_chartID].filters();

                if(currentPieFilters.length === 0){
                    $("#study-view-dc-chart-" + _chartID + "-main .study-view-dc-chart-change").css('display','none');
                    $("#study-view-dc-chart-" + _chartID + "-main").css({'border-width':'1px', 'border-style':'solid'});
                }
                else{
                    $("#study-view-dc-chart-" + _chartID + "-main .study-view-dc-chart-change").css('display','block');
                    $("#study-view-dc-chart-" + _chartID + "-main").css({'border-width':'2px', 'border-style':'inset'});
                }
            if(typeof scatterStudyView !== 'undefined'){            
                for(var i=0; i<tmpResult.length ; i++){
                    tmpCaseID.push(tmpResult[i].CASE_ID);
                }
                setScatterPlotStyle(tmpCaseID,currentPieFilters);   
            }
        });
        
        if(monthDomain.length > 5 && distanceMinMax < 1000){
            varChart[_chartID].on("postRender",function(){
                varChart[_chartID].selectAll("g.x text")
                    .attr('transform', "rotate(-30)");
                var x = varChart[_chartID].selectAll("g.x text")
                    .attr('x');
                varChart[_chartID].selectAll("g.x text")
                    .attr('x',x-5);

                varChart[_chartID].selectAll("g.x text")
                    .attr('dy','0.9em');
            });
        }
        
        $(".study-view-bar-x-log").unbind('click').click(function(e) {
            
            var valueArray = $(this).val().split(',');
            
            if($(this).prop('checked')){
                logScaleXBarChart(valueArray[0],valueArray[1]);
            }else{
                $(this).prop('checked', false);
                linearXBarChart(valueArray[0],valueArray[1],valueArray[2]);
            }
        });
    }
    function logScaleXBarChart(_chartID,_maxValue) {
        varChart[_chartID].x(d3.scale.log().nice().domain([1,_maxValue]));
        var xAxisTickValue = [];
        for(var i=10 ; i< _maxValue ;){
            var tmp = _maxValue % i;
            if(tmp !== 0){
                xAxisTickValue.push(i);
                i = i*10;
        }
            }
        varChart[_chartID].xAxis().tickValues(xAxisTickValue);
        varChart[_chartID].redraw(); 
    }
    function linearXBarChart(_chartID,_maxValue,_minValue) {
        varChart[_chartID].x(d3.scale.linear().domain([_minValue,_maxValue]));
        var xAxisTickValue = [];
        var tmpValue = parseInt((_maxValue-_minValue) / 9);
        
        for(var i = 0; i< _maxValue ;i += tmpValue){
            var lengthCurrentNumber = i.toString().length -1;
            var tmp = (parseInt(i/(Math.pow(10,lengthCurrentNumber)))) * (Math.pow(10,lengthCurrentNumber));
            xAxisTickValue.push(tmp);
            
        }
        varChart[_chartID].xAxis().tickValues(xAxisTickValue);
        //varChart[_chartID].xAxis().ticks(10);
        varChart[_chartID].redraw();    
    }
    function restyle(data) {
        var tmpA = [];
        var tmpB = [];
        
        tmpA.push({sTitle:"CASE ID"});
        for(var i=0; i< data.attr.length;i++){
            if(data.attr[i].attr_id !== 'CASE_ID'){
                var tmp = {};
                if(data.attr[i].attr_id === 'COPY_NUMBER_ALTERATIONS')
                    tmp.sTitle = 'CNA';
                else
                    tmp.sTitle = data.attr[i].attr_id.replace(/[_]/g,' ');
                tmpA.push(tmp);
            }
        }
        
        $.each(data.dataObjectM, function(key,value){ 
            tmpB[key] = [];
            $.each(tmpA, function(key1,value1){
                var tmpValue ='';
                
                if(value1.sTitle === 'CNA')
                    tmpValue = value['COPY_NUMBER_ALTERATIONS'];                
                else if ( value1.sTitle === 'COMPLETE (ACGH, MRNA, SEQUENCING)')
                    tmpValue = value[value1.sTitle];
                else
                    tmpValue = value[value1.sTitle.replace(/[ ]/g,'_')];
                
                if(!isNaN(tmpValue) && (tmpValue % 1 != 0))
                    tmpValue = cbio.util.toPrecision(Number(tmpValue),5,0.01);
                tmpB[key].push(tmpValue);
            });
        });
        
        //Add th tags based on number of attributes
        for(var i=0 ; i<tmpA.length ; i++)
            $("#dataTable tfoot tr").append("<th></th>");
       
        var dataTable1 = $('#dataTable').dataTable({
            "sScrollX": "1200px",
            "sScrollY": "300px",
            "bPaginate": false,
            "bFilter":true,
            "bScrollCollapse": true,
            "aoColumns": tmpA,
            "aaData":tmpB
        });
        
        $("#dataTable_filter label input").attr("value","");
        $('#study-view-dataTable-header').click(function(){
            var items=[];
            $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
               items.push( $(this).text() );       
            });
            var items = $.unique( items );

            varChart[attrNameMapUID['CASE_ID']].filterAll();
            varChart[attrNameMapUID['CASE_ID']].filter([items]);
            dc.redrawAll();
            setScatterPlotStyle(items,varChart[attrNameMapUID['CASE_ID']].filters());
        });
        
        $('#study-view-dataTable-updateTable').click(function(){
            var filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
            var filterString = "";
            for(var i=0 ; i<filteredResult.length ; i++){
                filterString += filteredResult[i].CASE_ID + '|';
            }
            filterString = filterString.substr(0,filterString.length-1);
            dataTable1.fnFilter(filterString,0,true);
            updateTable(dataTable1);
            resizeLeftColumn();            
            refreshSelectionInDataTable([0],dataTable1);
            dataTable1.fnAdjustColumnSizing();
        });
    }  
    
    function resizeLeftColumn(){
        var heightBody = $(".dataTables_scrollBody").css('height');
        var heightTable = $('.dataTables_scroll').css('height');
        $(".DTFC_LeftBodyLiner").css('height',heightBody);
        $(".DTFC_LeftBodyWrapper").css('height',heightBody); 
        $('.DTFC_ScrollWrapper').css('height',heightTable);            
    }
    
    function refreshSelectionInDataTable(_unrefreshID, _dataTable){
        $(".dataTables_scrollFoot tfoot th").each( function ( i ) {
            if(_unrefreshID.indexOf(i) === -1){                
                $(this).css('z-index','1500');
                this.innerHTML = fnCreateSelect( _dataTable.fnGetColumnData(i));
                $('select', this).change( function () {
                    if($(this).val() === '')
                        _dataTable.fnFilter($(this).val(), i);
                    else    
                        _dataTable.fnFilter("^"+$(this).val()+"$", i, true);
                    refreshSelectionInDataTable([0,i],_dataTable);
                    resizeLeftColumn();
                });
            }
        });
    }
    
    function updateTable(_dataTable){
        var oSettings = _dataTable.fnSettings();
        for(var iCol = 1; iCol < oSettings.aoPreSearchCols.length; iCol++) {
            oSettings.aoPreSearchCols[ iCol ].sSearch = '';
        }
        oSettings.oPreviousSearch.sSearch = '';
        _dataTable.fnDraw();
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

    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }
    
    function setScatterPlotStyle(_selectedCaseID,_filters){
        /*
        var styleDatum = {
            case_id: '',
            fill: 'blue',
            'stroke-width': '2px',
            stroke: 'red'            
        };
        */
       
        var style=[];
        for(var i=0 ; i< parObject.caseIds.length ; i++){
            var styleDatum ={};
            styleDatum.case_id = parObject.caseIds[i];
            if(_selectedCaseID.length !== parObject.caseIds.length){
                if(_selectedCaseID.indexOf(parObject.caseIds[i]) !== -1){
                    styleDatum.fill='red';
                    styleDatum.stroke = 'red';
                }else{
                    styleDatum.fill='#3366cc';
                    styleDatum.stroke = '#3366cc';
                }
            }else if(_filters.length === 0){
                styleDatum.fill='#3366cc';
                styleDatum.stroke = '#3366cc';
            }else{
                styleDatum.fill='red';
                styleDatum.stroke = 'red';
            }
            style.push(styleDatum);
        }
        scatterStudyView.updateStyle(style);
    }
    
    function resizeTable(){                 
        $('#dc-plots-loading-wait').hide();
        $('#study-view-main').show();
        /*
        var rotationAngle = 315;
        var radians = Math.PI * (rotationAngle/180);
        var numColumns = 0;
        var maxX =0;
        $('table.dataTable>thead>tr>th').each(function(){
            numColumns++;
        });
        
        for(var i =1;i<=numColumns ; i++){
            var rotatedX = $("table.dataTable>thead>tr>th:nth-child("+i+")").width();
            if(rotatedX > maxX)
                maxX = rotatedX;
        }
        maxX -= 28;
        for(var i =1;i<=numColumns ; i++){
            $("table.dataTable>thead>tr>th:nth-child("+i+")").height(maxX/Math.cos(radians));
        }
        */
        var oTable = $('#dataTable').dataTable();
        
        refreshSelectionInDataTable([0],oTable);
        
        oTable.fnAdjustColumnSizing();
        new FixedColumns( oTable);
        $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");
        $(".dataTables_scroll").css("overflow-x","scroll");
        $(".DTFC_LeftHeadWrapper").css("background-color","white");
    }
    
    function scatterPlotCallBack(_brushedCaseIds) {
       if(_brushedCaseIds.length > 0){
            varChart[attrNameMapUID['CASE_ID']].filterAll();
            varChart[attrNameMapUID['CASE_ID']].filter([_brushedCaseIds]);
            dc.redrawAll();
        }else{
            /*
            for(var i=0; i< varChart.length ; i++){
                if(varChart[i].filters().length > 0)
                    varChart[i].filterAll();
            }
            */
            dc.filterAll();
            //varChart[attrNameMapUID['CASE_ID']].filterAll();
            dc.redrawAll();
        }
    }
    
    function fnCreateSelect( aData )
    {
        var isNumericArray = true;
        var hasNullValue = false;
        for(var i=0;i<aData.length;i++){
            if(isNaN(aData[i])){
                if(aData[i] !== 'NA'){
                    isNumericArray = false;
                    break;
                }else
                    hasNullValue = true;
            }
        }
        
        if(isNumericArray && hasNullValue){
            var index = aData.indexOf('NA');
            if (index > -1) {
                aData.splice(index, 1);
            }
        }
        
        if(isNumericArray){            
            aData.sort(function(a,b) {
                return Number(a) - Number(b);
            });
            if(hasNullValue)
                aData.push('NA');
        }else{
            aData.sort();
        }
        var r='<select><option value=""></option>', i, iLen=aData.length;
        for ( i=0 ; i<iLen ; i++ )
        {
            r += '<option value="'+aData[i]+'">'+aData[i]+'</option>';
        }
        return r+'</select>';
    }
    
    function addXLogQTip(){
        $('.study-view-bar-x-log').qtip({
            content: {text:'Log Scale X'},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'right bottom',at:'top left'}
        });
    }
    
    function addPieSliceQTip(_identifier,_text){
        $(_identifier).qtip({
            content: {text:_text},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'right bottom',at:'top left'}
        });
    }
    
    return {
        init: function(o,data){
            initParameters(o);
            initCharts(data);
            restyle(data);
            resizeTable();
        }
    };
})();