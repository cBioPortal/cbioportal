/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var StudyViewInitCharts = (function(){
    

    var numOfCases,
        scatterStudyView,
        ndx,
        clickedCaseId = '',
        dataArr = {},
        removedChart = [],
        attrNameMapUID = [], //The relationshio between "The unique attribute name" and "The unique ID number in whole page"        
        varCluster = [], //Clusters of displayed charts -- DC.JS require
        varGroup = [], //Groups of displayed charts -- DC.JS require
        disableFiltId = [0],
        brushedCaseIds = [],
        shiftClickedCaseIds = [],
        chartColors = ["#3366cc","#dc3912","#ff9900","#109618",
            "#990099","#0099c6","#dd4477","#66aa00",
            "#b82e2e","#316395","#994499","#22aa99",
            "#aaaa11","#6633cc","#e67300","#8b0707",
            "#651067","#329262","#5574a6","#3b3eac",
            "#b77322","#16d620","#b91383","#f4359e",
            "#9c5935","#a9c413","#2a778d","#668d1c",
            "#bea413","#0c5922","#743411"], // Color scale from GOOGLE charts
        parObject = {
            studyId: "",
            caseIds: "",
            cnaProfileId: "",
            mutationProfileId: "",
        };
    
    function initParameters(o) {
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
    }

    function initCharts(dataObtained) {
        var pie = [], //Displayed attributes info, dataType: STRING, NUMBER, OR BOOLEAN
            bar = [], //Displayed attributes info, dataType: NUMBER
            combine = [], //Combine charts which created for unrecognizable data type
            varName = [], //Store all attributes in all data
            HTMLtagsMapUID = [], //The relationshio between "The ID of div standing for each Chart in HTML" and "The unique ID number in whole page"
            displayedID = [], //Displayed Charts ID number
            varDisplay = [], //Displayed Charts Name -- the display_name in each attribute       
            varType = [], //Could be displayed Charts Type 'pie,bar'. Mix combination, seperate by comma
            distanceMinMaxArray = [], //Only for NUMBER dataType, store min, max and difference value
            scatterPlotArr = [],
            scatterPlotDataAttr = {},
            scatterPlotOptions = {};
            
        var dataA = dataObtained.attr, //Atrributes
            dataB = dataObtained.dataObjectM; //All data
        
        var headerLeftQtip = jQuery.extend(true, {}, StudyViewBoilerplate.headerCaseSelectCustomDialog);
        headerLeftQtip.position.target = $(window);
        headerLeftQtip.content.text = $('#study-view-case-select-custom-dialog')
        $('#study-view-header-left-0').qtip(headerLeftQtip);;
        
        numOfCases = dataB.length;        
        ndx = crossfilter(dataB);
        //Calculate the number of pie, bar charts
        //Initial varName, varType, distanceMinMaxArray, varDisplay
        for(var i=0; i< dataA.length ; i++){
            var _varValuesNum = [];
            for(var j=0;j<dataB.length;j++){
                if(_varValuesNum.hasOwnProperty(dataB[j][dataA[i]["attr_id"]]))
                    _varValuesNum[dataB[j][dataA[i]["attr_id"]]]++;
                else
                    _varValuesNum[dataB[j][dataA[i]["attr_id"]]]=0;
            }
            var keys = Object.keys(_varValuesNum);
            
            if(dataA[i]["attr_id"] === "CASE_ID"){
                pie.push(dataA[i]);
            }else if(dataA[i]["datatype"] === "NUMBER" || dataA[i]["datatype"] === "BOOLEAN"){                
                if(selectedCol(dataA[i]["attr_id"])){                    
                    if(keys.length>10 || dataA[i]["attr_id"] === 'MUTATION_COUNT' 
                            || dataA[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS')
                        bar.push(dataA[i]);
                    else
                        pie.push(dataA[i]);
                }

                if(keys.length > 10 || dataA[i]["attr_id"] === 'MUTATION_COUNT' 
                        || dataA[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS'){
                    varType[dataA[i]["attr_id"]] = "bar";
                }else{
                    varType[dataA[i]["attr_id"]] = "pie";
                }
                
                if(dataA[i]["datatype"] === "NUMBER"){
                    var _varValues = [];
                    for(var j=0;j<dataB.length;j++){
                        if(dataB[j][dataA[i]["attr_id"]] && 
                                dataB[j][dataA[i]["attr_id"]]!=="NA" && 
                                dataB[j][dataA[i]["attr_id"]]!=="")
                            _varValues.push(dataB[j][dataA[i]["attr_id"]]);                    
                    }

                    distanceMinMaxArray[dataA[i]["attr_id"]] = {
                        distance : Math.max.apply( Math, _varValues ) - Math.min.apply( Math, _varValues ),
                        min: Math.min.apply( Math, _varValues ),
                        max:Math.max.apply( Math, _varValues )
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

        var totalCharts = pie.length + bar.length,
            createdChartID = 0;
    
        var para = {
            chartID: 0,
            chartDivID: 'study-view-bar-chart',
            attrId: '',
            displayName: '',
            distanceArray:distanceMinMaxArray
        };
            
        for(var i=0; i< pie.length ; i++){
            para.chartID = createdChartID;
            para.chartDivID = 'study-view-pie-chart';
            para.attrId = pie[i]["attr_id"];
            para.displayName = pie[i]["display_name"];
            initPieChart(para);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[pie[i]["attr_id"]] = createdChartID;
            displayedID.push(pie[i]["attr_id"]);
            createdChartID++;
        }

        for(var i=0; i< bar.length ; i++){
            para.chartID = createdChartID;
            para.chartDivID = 'study-view-bar-chart';
            para.attrId = bar[i]["attr_id"];
            para.displayName = bar[i]["display_name"];
            if(bar[i]["attr_id"] === 'MUTATION_COUNT' && distanceMinMaxArray[bar[i]["attr_id"]].distance > 1000){
                initLogBarChart(para);
            }else{
                initBarChart(para);
            }
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[bar[i]["attr_id"]] = createdChartID;
            displayedID.push(bar[i]["attr_id"]);            
            createdChartID++;
        }

        if(createdChartID !== totalCharts){
            console.log("Initial charts function error: " + 
                "the number of created charts not equal to number of totalCharts. --1");
            return false;
        }

        dc.renderAll();
        dc.renderAll("group1");
       
        $('#study-view-add-chart ul').find('li').remove().end();
            
        $.each(varName, function(key, value) {
            if(displayedID.indexOf(value) === -1){
                $('#study-view-add-chart ul')
                    .append($("<li></li>")
                        .attr("id",value)
                        .text(varDisplay[key]));
            }
        });
        
        
        $.each(dataB, function(i,value) {
            if( !isNaN(value['COPY_NUMBER_ALTERATIONS']) && 
                !isNaN(value['MUTATION_COUNT']) && 
                value['COPY_NUMBER_ALTERATIONS'] !="" && 
                value['MUTATION_COUNT']!="") {                  
                    var _scatterPlotDatumTmp= {};
                    _scatterPlotDatumTmp.x_val = value['COPY_NUMBER_ALTERATIONS'];
                    _scatterPlotDatumTmp.y_val = value['MUTATION_COUNT'];
                    _scatterPlotDatumTmp.case_id = value['CASE_ID'];
                    _scatterPlotDatumTmp.qtip = "Case ID: <strong>" + 
                        "<a href='tumormap.do?case_id=" + 
                        value['CASE_ID'] + "&cancer_study_id=" +
                        parObject.studyId + "' target='_blank'>" + 
                        value['CASE_ID'] + "</a></strong>";
                    scatterPlotArr.push(_scatterPlotDatumTmp);
            }
        });
        
        if(scatterPlotArr.length !== 0){
            scatterStudyView = new ScatterPlots();
            scatterPlotDataAttr = jQuery.extend(true, {}, StudyViewBoilerplate.scatterPlotDataAttr);
            scatterPlotOptions = jQuery.extend(true, {}, StudyViewBoilerplate.scatterPlotOptions);    
            scatterPlotDataAttr.min_x = distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].min;
            scatterPlotDataAttr.max_x = distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].max;
            scatterPlotDataAttr.min_y = distanceMinMaxArray['MUTATION_COUNT'].min;
            scatterPlotDataAttr.max_y = distanceMinMaxArray['MUTATION_COUNT'].max;    
        
            scatterStudyView.init(scatterPlotOptions, scatterPlotArr, scatterPlotDataAttr,true);            
            scatterStudyView.jointBrushCallback(scatterPlotBrushCallBack);
            scatterStudyView.jointClickCallback(scatterPlotClickCallBack);
            
            if(scatterPlotDataAttr.max_x > 1000){
                $("#" + scatterPlotOptions.names.log_scale_x).attr('checked',true);
                scatterStudyView.updateScaleX(scatterPlotOptions.names.log_scale_x);
            }
            if(scatterPlotDataAttr.max_y > 1000){
                $("#" + scatterPlotOptions.names.log_scale_y).attr('checked',true);
                scatterStudyView.updateScaleY(scatterPlotOptions.names.log_scale_y);
            }
           
            $("#" + scatterPlotOptions.names.log_scale_x).change(function() {
                scatterStudyView.updateScaleX(scatterPlotOptions.names.log_scale_x);
            });
            $("#" + scatterPlotOptions.names.log_scale_y).change(function() {
                scatterStudyView.updateScaleY(scatterPlotOptions.names.log_scale_y);
            });
            $(".study-view-scatter-plot-delete").unbind('click');
            $(".study-view-scatter-plot-delete").click(function (){
                $("#study-view-scatter-plot").css('display','none');
                $('#study-view-add-chart ul')
                        .append($('<li></li>')
                            .attr('id','mutationCNA')
                            .text('Number of Mutation vs Fraction of copy number altered genome'));
                msnry.layout();
                addClick();
                removeMarker();
                redrawChartsAfterDeletion();
                setScatterPlotStyle([],[]);
            });            
        
            $("#study-view-scatter-plot-pdf").submit(function(){
                setSVGElementValue("study-view-scatter-plot-body",
                    "study-view-scatter-plot-pdf-value",
                    scatterPlotOptions);
            });
            $("#study-view-scatter-plot-svg").submit(function(){
                setSVGElementValue("study-view-scatter-plot-body",
                    "study-view-scatter-plot-svg-value",
                    scatterPlotOptions);
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
        
        $('#study-view-add-chart ul').hide();
        $('#study-view-add-chart').mouseenter(function(){
           $('#study-view-add-chart ul').show(300);
        });
        $('#study-view-add-chart').mouseleave(function(){
           $('#study-view-add-chart ul').hide(300);
        });
        
        addClick();
        
        $('.study-view-dc-chart-delete').unbind('click');
        $('.study-view-dc-chart-delete').click(function(event){
                var _id = $(this).parent().parent().attr("id").split("-");
                var _valueA = $(this).parent().parent().attr('value').split(',');
                deleteChart(_id[_id.length-1],_valueA);
                msnry.layout();
                addClick();
        });
        $('#study-view-header-left-2').unbind('click');
        $('#study-view-header-left-2').click(function (){
            dc.filterAll();
            dc.redrawAll();
            changeHeader();
        });
        /*
        $('#study-view-header-left-4').click(function (){
            removeAllNAValue(dataObtained);
        });
        */
        $("#study-view-case-select-custom-submit-btn").click(function() {
            var caseIds = $('#study-view-case-select-custom-input').val().trim().split(/\s+/);
            filterChartsByGivingIDs(caseIds);
            $('#study-view-header-left-0').qtip('toggle');
        });
    
        function addClick(){
            $('#study-view-add-chart ul li').unbind('click');
            $('#study-view-add-chart ul li').click(function() {
                var _chartType = [];
                if($(this).attr('id') === 'mutationCNA')
                    _chartType = ['scatter'];
                else
                    _chartType = varType[$(this).attr('id')].split(',');
                
                var selectedAttr = $(this).attr('id');
                var selectedAttrDisplay = $(this).text();
                var selectedChartType = _chartType[0];
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
                    
                    var _para = {
                        chartID: chartTmpID,
                        chartDivID: 'study-view-bar-chart',
                        attrId: selectedAttr,
                        displayName: selectedAttrDisplay,
                        distanceArray:distanceMinMaxArray
                    };
                    
                    if(selectedChartType === 'pie'){
                        _para.chartDivID = 'study-view-pie-chart';
                        initPieChart(_para);
                    }else{
                        initBarChart(_para);                
                    }


                    msnry.destroy();
                    var container = document.querySelector('#study-view-charts');
                    msnry = new Masonry( container, {
                      columnWidth: 190,
                      itemSelector: '.study-view-dc-chart',
                      gutter:1
                    });

                    varChart[chartTmpID].render();
                    
                    $('#study-view-dc-chart-'+ chartTmpID +' .study-view-dc-chart-delete').unbind('click');
                    $('#study-view-dc-chart-'+ chartTmpID +' .study-view-dc-chart-delete').click(function(event){
                        var valueA = $(this).parent().parent().attr("value").split(',');
                        deleteChart(chartTmpID,valueA);
                        addClick();
                        msnry.layout();
                    });
                }
                
                var index = removedChart.indexOf(chartTmpID);
                if (index > -1) {
                    removedChart.splice(index, 1);
                }
                msnry.layout();
                
                $('#study-view-add-chart ul').find('li[id="' + selectedAttr + '"]').remove();
            });
        }
    }
    
    function deleteChart(_chartID,_value){
        $("div").remove("#study-view-dc-chart-" + _chartID + "-main"); 
        if(varChart[_chartID].hasFilter()){
            varChart[_chartID].filterAll();
            dc.redrawAll();
        }
        dc.deregisterChart(varChart[_chartID]);
        $('#study-view-add-chart ul')
                .append($('<li></li>').attr('id',_value[0]).text(_value[1]));
        var _filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
        var _filterString = "";
        for(var i=0 ; i<_filteredResult.length ; i++){
            _filterString += _filteredResult[i].CASE_ID + '|';
        }
        _filterString = _filterString.substr(0,_filterString.length-1);
        $('#dataTable').dataTable().fnFilter(_filterString,0,true);
        removedChart.push(Number(_chartID));
    }
    
    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue,scatterPlotDataAttr){
        $("#" + _svgParentDivId + " .plots-title-x-help").remove();
        $("#" + _svgParentDivId + " .plots-title-y-help").remove();
        
        var svgElement = $("#" + _svgParentDivId).html();
        $("#" + _idNeedToSetValue).val(svgElement);
        scatterStudyView.updateTitleHelp(scatterPlotDataAttr.names.log_scale_x, scatterPlotDataAttr.names.log_scale_y);
    }
    
    function initPieChart(_para) {
        var chartID = _para.chartID,
            className = _para.chartDivID,
            selectedAttr = _para.attrId,
            selectedAttrDisplay = _para.displayName,
            pieWidth = 130,
            pieRadius = (pieWidth - 20) /2;
        if(selectedAttr === 'CASE_ID'){
            $("#study-view-charts")
                    .append("<div id=\"study-view-dc-chart-" + chartID + 
                    "-main\" class='study-view-dc-chart study-view-pie-main' "+
                    "style='display:none'><div id=\"study-view-dc-chart-" +
                    chartID + "\"></div></div>");
        }else
            $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + 
                chartID + "-main\" class='study-view-dc-chart study-view-pie-main'>"+
                "<div id=\"study-view-dc-chart-" + chartID + "\" class='" + 
                className + "'  value='"+ selectedAttr + "," + 
                selectedAttrDisplay + ",pie'>"+
                "<div style='width:180px; float:right; text-align:center;'>"+
                "<span class='study-view-dc-chart-delete'>x</span>"+
                "<a href='javascript:varChart[" + chartID + "].filterAll();dc.redrawAll();'>" +
                "<span title='Reset Chart' class='study-view-dc-chart-change' "+
                "style='font-size:10px;'>RESET</span></a></div>"+
                "<div style='width:180px;float:left;text-align:center'><pieH4>" +
                selectedAttrDisplay + "</pieH4></div></div>"+
                "<div class='study-view-pie-label'></div>"+
                "<div style='width:180px; text-align:center;float:left;'></div></div>");
        
        varChart[chartID] = dc.pieChart("#study-view-dc-chart-" + chartID);
        varCluster[chartID] = ndx.dimension(function (d) {
            if(!d[selectedAttr] || d[selectedAttr].toLowerCase()==="unknown" 
                    || d[selectedAttr].toLowerCase()==="none")
                return "NA";
            return d[selectedAttr];
        });
        varGroup[chartID] = varCluster[chartID].group();
        
        varChart[chartID]
        .width(pieWidth)
        .height(pieWidth)
        .radius(pieRadius)
        .dimension(varCluster[chartID])
        .group(varGroup[chartID])
        .transitionDuration(600)
        .ordinalColors(chartColors)
        .label(function (d) {
            return d.value;
        })
        .ordering(function(d){ return d.key;});
        //Use to refresh Scatter Plot when click pie slice.
        
        if(selectedAttr !== 'CASE_ID'){
            varChart[chartID].on("filtered", function(chart,filter){
                var currentPieFilters = varChart[chartID].filters();
                if(currentPieFilters.length === 0){
                    $("#study-view-dc-chart-" + chartID + 
                            "-main .study-view-dc-chart-change")
                            .css('display','none');
                    $("#study-view-dc-chart-" + chartID + "-main")
                            .css({'border-width':'1px', 'border-style':'solid'});
                }else{
                    $("#study-view-dc-chart-" + chartID + 
                            "-main .study-view-dc-chart-change")
                            .css('display','block');
                    $("#study-view-dc-chart-" + chartID + "-main")
                            .css({'border-width':'2px', 'border-style':'inset'});
                }

                var _tmpResult = varChart[attrNameMapUID["CASE_ID"]].dimension().top(Infinity),
                    _tmpCaseID = [];
                
                if(typeof scatterStudyView !== 'undefined'){
                    for(var i=0; i<_tmpResult.length ; i++){
                        _tmpCaseID.push(_tmpResult[i].CASE_ID);
                    }
                    setScatterPlotStyle(_tmpCaseID,currentPieFilters);
                }
                
                changeHeader();
                removeMarker();
            });
            varChart[chartID].on("postRedraw",function(chart){
                addPieLabels("study-view-dc-chart-" + chartID);
            });
            varChart[chartID].on("postRender",function(chart){
                addPieLabels("study-view-dc-chart-" + chartID);
            });
        }
    }
    
    function changeHeader(){
        var tmpDimention = varChart[attrNameMapUID["CASE_ID"]].dimension();
        var tmpResult = tmpDimention.top(Infinity);
        var tmpResultLength = tmpResult.length;
        var tmpCaseID = [];
        for(var i=0; i<tmpResult.length ; i++){
            tmpCaseID.push(tmpResult[i].CASE_ID);
        }
        if(tmpResultLength === numOfCases){
            var hasFilter = false;
            for(var i=0; i<varChart.length; i++){
                if(removedChart.indexOf(i) === -1){
                    if (varChart[i].filters().length > 0)
                        hasFilter = true;
                }
            }
            if(hasFilter){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','block');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-3").text(tmpResultLength + " cases are selected.");
                $("#study-view-header-left-case-ids").val(tmpCaseID.join(" "));
            }else{
                $("#study-view-header-left-0").css('display','block');
                $("#study-view-header-left-1").css('display','none');
                $("#study-view-header-left-2").css('display','none');
                $("#study-view-header-left-3").css('display','none');
            }
        }else{
            if(tmpResultLength === 1){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','none');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-3").html("");
                $("#study-view-header-left-3")
                        .append("<a title='Go to patient-centric view' " + 
                        "href='case.do?cancer_study_id=" + parObject.studyId +
                        "&amp;case_id=" + tmpCaseID[0] + "'>" + tmpCaseID[0] + 
                        "</a>" + " is selected.");                
            }else{
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','block');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-3").text(tmpResultLength + " cases are selected.");
                $("#study-view-header-left-case-ids").val(tmpCaseID.join(" "));
            }
        }
    }
    
    function addPieLabels(_pieChartID) {
        var label =[],
            totalTableNum = 1,
            labelID = 0,
            labelSize = 10,
            fontSize = labelSize +1;
        
        $('#' + _pieChartID + '-main .study-view-pie-label').html("");
          
        $('#' + _pieChartID + '>svg>g>g').each(function(){
            var _labelDatum = {},          
                labelName = $(this).find('title').text().split(':'),
                color = $(this).find('path').attr('fill'),            
                tmpPointsInfo = $(this).find('path').attr('d').split(/[\s,MLHVCSQTAZ]/);            
            
            var _x1 = Number(tmpPointsInfo[1]),
                _y1 = Number(tmpPointsInfo[2]),
                _x2 = Number(tmpPointsInfo[8]),
                _y2 = Number(tmpPointsInfo[9]);
                
            //addPieSliceQTip(this,labelName[0]);
            if(_x1 !== _x2 || _y1 !== _y2){
                _labelDatum.id = labelID;
                _labelDatum.name = labelName[0];
                _labelDatum.color = color;
                _labelDatum.parentID = _pieChartID;
                _labelDatum.value = labelName[1];
                label.push(_labelDatum);
            }
            labelID++;            
        });
        
        if(label.length % 5 === 0)
            totalTableNum = label.length/5;
        else
            totalTableNum = parseInt(label.length/5) + 1;
        
        if(label.length > 6){
            for(var j = 0 ; j < label.length ; j+=5){
                var innerID = 0,
                    tableId = parseInt(j/5),
                    showTableStyle = '',
                    leftArrowColor = 'blue',
                    rightArrowColor = 'blue';
            
                if(tableId !== 0)  
                    showTableStyle = 'style="display:none"';
                
                $('#' + _pieChartID + '-main')
                        .find('.study-view-pie-label')
                        .append("<table id='table-"+_pieChartID+"-"+tableId+"' "+
                            showTableStyle + " ></table>");
                
                for(var i=j; i< j+5; i++){
                    if(i<label.length){
                        var _tmpName = label[i].name;
                        if(_tmpName.length > 9)
                           _tmpName = _tmpName.substring(0,5) + " ...";
                        if((i-tableId) % 2 === 0){
                            $('#' + _pieChartID + '-main')
                                    .find('#table-'+_pieChartID+'-'+tableId)
                                    .append("<tr id="+ innerID +" width='150px'></tr>");
                            innerID++;
                        } 
                        $('#' + _pieChartID + '-main')
                                .find('#table-'+_pieChartID+'-'+tableId+
                                    ' tr:nth-child(' + innerID +')')
                                .append('<td class="pieLabel" id="pieLabel-' +
                                    _pieChartID+'-'+label[i].id + "-" + i +
                                    '" style="font-size:' + fontSize +
                                    'px"><svg width="75" height="13"><rect width="' +
                                    labelSize+'" height="'+ labelSize +
                                    '" style="fill:' + label[i].color + 
                                    ';" /><text x="15" y="10">' + _tmpName +
                                    '</text></svg></td>');
                            
                        //Only add qtip when the length of pie label bigger than 9
                        if(label[i].name.length > 9){
                            var _qtip = jQuery.extend(true,{},StudyViewBoilerplate.pieLabelQtip);
                            _qtip.content.text = label[i].name;
                            $('#pieLabel-'+_pieChartID+'-'+label[i].id+'-'+i).qtip(_qtip);
                        }
                    }else{
                        if((i-tableId) % 2 === 0){
                            $('#' + _pieChartID + '-main')
                                    .find('#table-'+_pieChartID+'-'+tableId)
                                    .append("<tr id="+ innerID +" width='150px'></tr>");
                            innerID++;
                        } 
                        $('#' + _pieChartID + '-main')
                                .find('#table-'+_pieChartID+'-'+tableId+
                                    ' tr:nth-child(' + innerID +')')
                                .append('<td style="width="75px" height="15px"'+
                                    ' font-size:'+fontSize+'px"></td>');
                    }
                }
                
                
                if(tableId === 0)
                    leftArrowColor = 'grey';
                if(tableId+1 === totalTableNum)
                    rightArrowColor = 'grey';
                
                $('#' + _pieChartID + '-main')
                        .find('#table-'+_pieChartID+'-' + tableId+
                            ' tr:nth-child(' + innerID +')')
                        .append('<td id="pieLabel-pagging-' +_pieChartID+"-"+
                            tableId+'" style=" width="75px" height="16px"'+
                            ' border="0px" font-size:'+fontSize+'px">'+
                            '<svg  width="75" height="13">'+
                            '<path class="pie-label-left-pagging" '+
                            'd="M5 1 L0 11 L10 11 Z" fill="'+leftArrowColor+
                            '"/><text x=15 y=10 fill="black">'+
                            (tableId+1)+'/'+totalTableNum+'</text>'+
                            '<path class="pie-label-right-pagging"'+
                            'd="M45 11 L40 1 L50 1 Z" fill="'+rightArrowColor+
                            '"/></svg></td>');
            }
        }else{
            var innerID = 0;
            $('#' + _pieChartID + '-main')
                    .find('.study-view-pie-label')
                    .append("<table id=table-"+_pieChartID+"-0></table>");
                
            for(var i=0; i< label.length; i++){
                var _tmpName = label[i].name;
                if(_tmpName.length > 9){
                    _tmpName = _tmpName.substring(0,5) + " ...";
                }
                if(i % 2 === 0){
                    $('#' + _pieChartID + '-main')
                            .find('table')
                            .append("<tr id="+ innerID +" width='150px'></tr>");
                    innerID++;
                } 
                $('#' + _pieChartID + '-main')
                        .find('table tr:nth-child(' + innerID +')')
                        .append('<td class="pieLabel" id="pieLabel-'+
                            _pieChartID +'-'+label[i].id+'-'+i+
                            '"  style="font-size:'+fontSize+'px">'+
                            '<svg width="75" height="13"><rect width="'+
                            labelSize+'" height="'+labelSize+'" style="fill:'+
                            label[i].color + ';" /><text x="15" y="10">'+
                            _tmpName+'</text></svg></td>');
                    
                //Only add qtip when the length of pie label bigger than 9
                if(label[i].name.length > 9){
                    var _qtip = jQuery.extend(true,{},StudyViewBoilerplate.pieLabelQtip);
                    _qtip.content.text = label[i].name;
                    $('#pieLabel-'+_pieChartID+'-'+label[i].id+'-'+i).qtip(_qtip);
                }
            }
        }
        
        $('#' + _pieChartID + '-main .pieLabel').mouseenter(function(){
            var idArray = $(this).attr('id').split('-'),
                childID = Number(idArray[idArray.length-2])+1;
                
            $('#' + _pieChartID + ' svg>g>g:nth-child(' + childID+')').css({
                'fill-opacity': '.5',
                'stroke-width': '3px'
            });
            
            var fatherID = Number(idArray[idArray.length-3]);
            
            var tmpPointsInfo = $('#' + _pieChartID + ' svg>g>g:nth-child(' + 
                    childID+')').find('path').attr('d').split(/[\s,MLHVCSQTAZ]/),          
                tmpPointsInfo1 = $('#' + _pieChartID + ' svg>g>g:nth-child(' + 
                    childID+')').find('path').attr('d').split(/[A]/);
            
            var x1 = Number(tmpPointsInfo[1]),
                y1 = Number(tmpPointsInfo[2]),
                largeArc = Number(tmpPointsInfo[6]),
                sweep = Number(tmpPointsInfo[7]),
                x2 = Number(tmpPointsInfo[8]),
                y2 = Number(tmpPointsInfo[9]),
                r = Number(tmpPointsInfo[3]);
            
            if((x1-x2!==0 ||y1-y2!==0) && tmpPointsInfo1.length === 2){
                var _xm = (x1 + x2) /2,
                    _ym = (y1 + y2) /2;
                    
                var m = Math.sqrt((Math.pow(_xm,2)+Math.pow(_ym,2)));
                
                var _tmpX = (r + 3) / m * _xm,
                    _tmpY = (r + 3) / m * _ym;
                
                if(_xm === 0 && _ym === 0){
                    _tmpY = 0;
                    _tmpX = r + 6;
                }
            
                if(largeArc === 1 && Math.abs(x1 - x2) >0.1) {
                    _tmpX = -_tmpX;
                    _tmpY = -_tmpY;
                }
                
                var circleID = "circle-" +fatherID+"-"+(Number(childID)-1);
                    
                d3.select("#" + _pieChartID + " svg g").append("path")
                    .attr("transform", function(d) { return "translate(" + _tmpX + "," + _tmpY + ")"; })
                    .attr("d", d3.svg.symbol().size('25').type('circle'))
                    .attr('fill',"red")
                    .attr('id',circleID)
                    .attr('class','circle');
            }
        });
        
        $('#' + _pieChartID + '-main .pieLabel').mouseleave(function(){
            var idArray = $(this).attr('id').split('-');
            var childID = Number(idArray[idArray.length-2])+1;
            var fatherID = Number(idArray[idArray.length-3]);
            var circleID = fatherID+"-"+(Number(childID)-1);
            
            $("#" + _pieChartID + " svg g #circle-" + circleID).remove();
            
            $('#' + _pieChartID + ' svg>g>g:nth-child(' + childID+')').css({
                'fill-opacity': '1',
                'stroke-width': '1px'
            });
        });
        
        $('#' + _pieChartID + '-main .pieLabel').click(function(){
            var idArray = $(this).attr('id').split('-');
            
            var childaLabelID = Number(idArray[idArray.length-1]),
                childID = Number(idArray[idArray.length-2])+1,
                chartID = Number(idArray[idArray.length-3]);
            
            var circleID = chartID+"-"+(Number(childID)-1);
            
            varChart[chartID].onClick({
                key: label[childaLabelID].name, 
                value:label[childaLabelID].value
            });       
            
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
            
    function initLogBarChart(_para) {
        var chartID = _para.chartID,
            className = _para.chartDivID,
            selectedAttr = _para.attrId,
            selectedAttrDisplay = _para.displayName,
            distanceMinMaxArray = _para.distanceArray;
        
        var logCheckBox = "<span id='scale-span-"+chartID+
                "' style='float:right; font-size:10px; margin-right: 15px;"+
                "margin-top:3px;color: grey'>Log Scale X</span>"+
                "<input type='checkbox' checked value='"+ chartID +","+ 
                distanceMinMaxArray[selectedAttr].max +","+ 
                distanceMinMaxArray[selectedAttr].min + "," + selectedAttr +
                "' id='scale-input-"+chartID+
                "' class='study-view-bar-x-log'></input>";
        
        var contentHTML = "<div id=\"study-view-dc-chart-" + chartID + 
                "\" class='"+ className +"'  value='" + selectedAttr + "," + 
                selectedAttrDisplay+ ",bar'><div style='width:100%; float:right'>"+
                "<span class='study-view-dc-chart-delete'>x</span>"+
                "<a href='javascript:varChart[" + chartID + "].filterAll();dc.redrawAll();'>"+
                "<span  title='Reset Chart' class='study-view-dc-chart-change'>"+
                "RESET</span></a>"+logCheckBox +"</div></div>"+
                "<div style='width:100%; float:center;text-align:center;'><pieH4>"
                + selectedAttrDisplay + "</pieH4></div>";
        
        if($("#study-view-dc-chart-" + chartID+ "-main").length === 0){
            $("#study-view-charts")
                    .append("<div id=\"study-view-dc-chart-" + chartID+ 
                        "-main\" class='study-view-dc-chart study-view-bar-main'>" +
                        contentHTML + "</div>");
                
            $("#scale-input-"+chartID).change(function(e) {
                $(this).parent().parent().find('svg').remove();
                
                if(varChart[chartID].hasFilter()){
                    varChart[chartID].filterAll();
                    dc.redrawAll();
                }
                
                dc.deregisterChart(varChart[chartID]);                
                
                var _para = {
                    chartID: chartID,
                    chartDivID: className,
                    attrId: selectedAttr,
                    displayName: selectedAttrDisplay,
                    distanceArray:distanceMinMaxArray
                };
                if($(this).attr('checked')){
                    initLogBarChart(_para);
                }else{
                    initBarChart(_para);
                }
                varChart[chartID].render();
            });
        }
        
        varChart[chartID] = dc.barChart("#study-view-dc-chart-" + chartID);
        
        var emptyValueMapping = "1000",
            maxDomain = 10000,
            monthDomain = [];

        for(var i=0; ;i+=0.5){
            var _tmpValue = parseInt(Math.pow(10,i));
            monthDomain.push(_tmpValue);
            if(_tmpValue > distanceMinMaxArray[selectedAttr].max){
                emptyValueMapping = Math.pow(10,i+0.5);
                monthDomain.push(emptyValueMapping);
                maxDomain = Math.pow(10,i+1);
                break;
            }
        }
        varCluster[chartID] = ndx.dimension(function (d) {
            var returnValue = Number(d[selectedAttr]);
            if(isNaN(returnValue)){
                return emptyValueMapping;
            }else{
                returnValue = Number(returnValue);
                for(var i=1;i<monthDomain.length;i++){
                    if(d[selectedAttr] < monthDomain[i] && d[selectedAttr] >= monthDomain[i-1])
                        returnValue = parseInt(Math.pow(10,i/2-0.25));
                }
                return returnValue;
            }
        }); 
        
        varGroup[chartID] = varCluster[chartID].group();

        varChart[chartID]
            .width(370)
            .height(180)
            .margins({top: 10, right: 20, bottom: 30, left: 40})
            .dimension(varCluster[chartID])
            .group(varGroup[chartID])
            .centerBar(true)
            .elasticY(true)
            .elasticX(false)
            .turnOnControls(true)
            .mouseZoomable(false)
            .brushOn(true)
            .transitionDuration(600)
            .renderHorizontalGridLines(true)
            .renderVerticalGridLines(true);
    
        varChart[chartID].centerBar(true);
        varChart[chartID].x(d3.scale.log().nice().domain([0.7,maxDomain]));
        varChart[chartID].yAxis().tickFormat(d3.format("d"));            
        varChart[chartID].xAxis().tickFormat(function(v) {
            var returnValue = v;
            if(v === emptyValueMapping) 
                returnValue = 'NA';
            else {
                var index = monthDomain.indexOf(v);
                if(index % 2 === 0)
                    return v.toString();
                else
                    return '';
            }
            return returnValue; 
        });            
        
        varChart[chartID].xAxis().tickValues(monthDomain);
        var xunitsNum = monthDomain.length*1.3;
        if(xunitsNum <= 5)
            varChart[chartID].xUnits(function(){return 5;});
        else
            varChart[chartID].xUnits(function(){return xunitsNum;});
        
        varChart[chartID].on("filtered", function(chart,filter){
            var tmpCaseID = [],
                currentPieFilters = varChart[chartID].filters(),
                tmpDimention = varChart[attrNameMapUID["CASE_ID"]].dimension();
                
            var tmpResult = tmpDimention.top(Infinity);
            
            if(currentPieFilters.length === 0){
                $("#study-view-dc-chart-" + chartID + 
                        "-main .study-view-dc-chart-change")
                        .css('display','none');
                $("#study-view-dc-chart-" + chartID + "-main")
                        .css({'border-width':'1px', 'border-style':'solid'});
            }
            else{
                $("#study-view-dc-chart-" + chartID + 
                        "-main .study-view-dc-chart-change")
                        .css('display','block');
                $("#study-view-dc-chart-" + chartID + "-main")
                        .css({'border-width':'2px', 'border-style':'inset'});
            }
            
            if(typeof scatterStudyView !== 'undefined'){
                for(var i=0; i<tmpResult.length ; i++){
                    tmpCaseID.push(tmpResult[i].CASE_ID);
                }
                setScatterPlotStyle(tmpCaseID,currentPieFilters);   
            }
            
            changeHeader();
            removeMarker();
        });
    }
    
    function initBarChart(_para) {
        var chartID = _para.chartID,
            className = _para.chartDivID,
            selectedAttr = _para.attrId,
            selectedAttrDisplay = _para.displayName,
            distanceMinMaxArray = _para.distanceArray;
        
        var distanceMinMax = distanceMinMaxArray[selectedAttr].distance;
        
        var seperateDistance,
            tmpMaxDomain,
            numOfGroups = 10,
            startPoint,
            emptyValueMapping,
            monthDomain = [],
            distanceLength,
            divider = 1;
        
        distanceLength = parseInt(distanceMinMax).toString().length;
        
        for(var i=0;i<distanceLength-2;i++)
            divider *= 10;
        if(distanceMinMaxArray[selectedAttr].max < 100 && 
                distanceMinMaxArray[selectedAttr].max > 20)
            divider = 10;
        
        if(distanceMinMaxArray[selectedAttr].max <=1 && 
                distanceMinMaxArray[selectedAttr].max > 0 && 
                distanceMinMaxArray[selectedAttr].min>=-1 && 
                distanceMinMaxArray[selectedAttr].min<0){
            tmpMaxDomain = (parseInt(distanceMinMaxArray[selectedAttr].max / divider) + 1) * divider;
            seperateDistance = 0.2;
            startPoint = (parseInt(distanceMinMaxArray[selectedAttr].min / 0.2)-1) * 0.2;
            emptyValueMapping = tmpMaxDomain +0.2;
        }else if(distanceMinMax > 1){
            seperateDistance = (parseInt(distanceMinMax / (numOfGroups * divider)) + 1) * divider;
            tmpMaxDomain = (parseInt(distanceMinMaxArray[selectedAttr].max / divider) + 1) * divider;
            startPoint = parseInt(distanceMinMaxArray[selectedAttr].min / divider) * divider;
            emptyValueMapping = tmpMaxDomain+seperateDistance;
        }else if(distanceMinMax < 1 && distanceMinMaxArray[selectedAttr].min >=0 ){
            seperateDistance = 0.1;
            startPoint = 0;
            emptyValueMapping = 1.1;
        }else{
            seperateDistance = 0.1;
            startPoint = -1;
            emptyValueMapping = tmpMaxDomain + 0.1;
        }
        
        for(var i=0; i<= numOfGroups ; i++){
            var tmpValue = i*seperateDistance + startPoint;
            tmpValue = Number(cbio.util.toPrecision(Number(tmpValue),5,0.1));
            monthDomain.push(tmpValue);
            if(tmpValue > distanceMinMaxArray[selectedAttr].max){
                if(distanceMinMax > 1000)
                    emptyValueMapping = (i+1)*seperateDistance + startPoint;
                break;
            }
        }   
        
        var logCheckBox = "";
        
        if(distanceMinMax > 1000)
            logCheckBox = "<span id='scale-span-"+chartID+
                "' style='float:right; font-size:10px; margin-right: 15px;"+
                "margin-top:3px;color: grey'>Log Scale X</span>"+
                "<input type='checkbox' value='"+ chartID +","+ 
                distanceMinMaxArray[selectedAttr].max +","+ 
                distanceMinMaxArray[selectedAttr].min + "," + selectedAttr+
                "' id='scale-input-"+chartID+
                "' class='study-view-bar-x-log'></input>";
        
        var contentHTML = "<div id=\"study-view-dc-chart-" + chartID + 
                "\" class='"+ className +"'  value='" + selectedAttr + "," + 
                selectedAttrDisplay + ",bar'><div style='width:100%; float:right'>"+
                "<span class='study-view-dc-chart-delete'>x</span>"+
                "<a href='javascript:varChart[" + chartID + "].filterAll();dc.redrawAll();'>"+
                "<span title='Reset Chart' class='study-view-dc-chart-change'>"+
                "RESET</span></a>"+logCheckBox +"</div></div>"+
                "<div style='width:100%; float:center;text-align:center;'>"+
                "<pieH4>" + selectedAttrDisplay + "</pieH4></div>";
        
        if($("#study-view-dc-chart-" + chartID+ "-main").length === 0){
            $("#study-view-charts")
                    .append("<div id=\"study-view-dc-chart-" + chartID+ 
                    "-main\" class='study-view-dc-chart study-view-bar-main'>" + 
                    contentHTML + "</div>");
            
            $("#scale-input-"+chartID).change(function(e) {
                var _para = {
                    chartID: chartID,
                    chartDivID: className,
                    attrId: selectedAttr,
                    displayName: selectedAttrDisplay,
                    distanceArray:distanceMinMaxArray
                };
                
                $(this).parent().parent().find('svg').remove();
                
                if(varChart[chartID].hasFilter()){
                    varChart[chartID].filterAll();
                    dc.redrawAll();
                }
                dc.deregisterChart(varChart[chartID]);                

                if($(this).attr('checked')){
                    initLogBarChart(_para);
                }else{
                    initBarChart(_para);
                }
                varChart[chartID].render();
            });
        }
            
        varChart[chartID] = dc.barChart("#study-view-dc-chart-" + chartID);
        var hasEmptyValue = false;
        
        varCluster[chartID] = ndx.dimension(function (d) {
            var returnValue = d[selectedAttr];
            if(returnValue === "NA" || returnValue === '' || returnValue === 'NaN'){
                hasEmptyValue = true;
                return emptyValueMapping;
            }else{
                if(d[selectedAttr] >= 0)
                    returnValue =  parseInt(d[selectedAttr] / seperateDistance) * seperateDistance + seperateDistance/2;
                else
                    returnValue =  (parseInt(d[selectedAttr] / seperateDistance)-1) * seperateDistance + seperateDistance/2;
                return returnValue;
            }
        });
        
        if(hasEmptyValue)
            monthDomain.push(Number(cbio.util.toPrecision(Number(emptyValueMapping),5,0.1)));
   
        varGroup[chartID] = varCluster[chartID].group();

        varChart[chartID]
            .width(370)
            .height(180)
            .margins({top: 10, right: 20, bottom: 30, left: 40})
            .dimension(varCluster[chartID])
            .group(varGroup[chartID])
            .centerBar(true)
            .elasticY(true)
            .elasticX(false)
            .turnOnControls(true)
            .mouseZoomable(false)
            .brushOn(true)
            .transitionDuration(600)
            .renderHorizontalGridLines(true)
            .renderVerticalGridLines(true);
    
        varChart[chartID].x(d3.scale.linear().domain([monthDomain[0]-seperateDistance,monthDomain[monthDomain.length-1]+seperateDistance]));
        varChart[chartID].yAxis().tickFormat(d3.format("d"));            
        varChart[chartID].xAxis().tickFormat(function(v) {
            if(v === emptyValueMapping) 
                return 'NA'; 
            else
                return v;
        });            
        varChart[chartID].xAxis().tickValues(monthDomain);
        
        var xunitsNum = monthDomain.length*1.3;
        
        if(xunitsNum <= 5)
            varChart[chartID].xUnits(function(){return 5;});
        else
            varChart[chartID].xUnits(function(){return xunitsNum;});
        
        varChart[chartID].on("filtered", function(chart,filter){
            var tmpCaseID = [],
                currentPieFilters = varChart[chartID].filters(),
                tmpDimention = varChart[attrNameMapUID["CASE_ID"]].dimension();
                
            var tmpResult = tmpDimention.top(Infinity);
            
            if(currentPieFilters.length === 0){
                $("#study-view-dc-chart-" + chartID + 
                        "-main .study-view-dc-chart-change")
                        .css('display','none');
                $("#study-view-dc-chart-" + chartID + "-main")
                        .css({'border-width':'1px', 'border-style':'solid'});
            }
            else{
                $("#study-view-dc-chart-" + chartID + 
                        "-main .study-view-dc-chart-change")
                        .css('display','block');
                $("#study-view-dc-chart-" + chartID + "-main")
                        .css({'border-width':'2px', 'border-style':'inset'});
            }
            
            if(typeof scatterStudyView !== 'undefined'){
                for(var i=0; i<tmpResult.length ; i++){
                    tmpCaseID.push(tmpResult[i].CASE_ID);
                }
                setScatterPlotStyle(tmpCaseID,currentPieFilters);   
            }
            
            changeHeader();
            removeMarker();
        });
        
    }
    
    function restyle(data) {
        var tmpA = [],
            tmpB = [];
        
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
                
                if(value1.sTitle === 'CNA'){
                    tmpValue = value['COPY_NUMBER_ALTERATIONS'];                
                }else if ( value1.sTitle === 'COMPLETE (ACGH, MRNA, SEQUENCING)'){
                    tmpValue = value[value1.sTitle];
                }else if ( value1.sTitle === 'CASE ID'){
                    tmpValue = "<a href='tumormap.do?case_id=" + 
                    value['CASE_ID'] + "&cancer_study_id=" +
                    parObject.studyId + "' target='_blank'>" + 
                    value['CASE_ID'] + "</a></strong>";
                }else{
                    tmpValue = value[value1.sTitle.replace(/[ ]/g,'_')];
                }
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
            "bScrollCollapse": true,
            "aoColumns": tmpA,
            "aaData":tmpB,
            "sDom": '<"dataTableTop"i<"dataTableReset">f>rt'
        });
        
        $(".dataTableReset").append("<a><span class='hidden' title='Reset Chart'>RESET</span></a>");
        $("#dataTable_filter label input").attr("value","");
        $('#study-view-dataTable-header').unbind('click');
        $('#study-view-dataTable-header').click(function(){
            var items=[];
            $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
               items.push( $(this).text() );       
            });
            var items = $.unique( items );

            filterChartsByGivingIDs(items);
        });
        $(".dataTableReset span").click(function(){
            updateTable(dataTable1,[]);
            resizeLeftColumn();            
            refreshSelectionInDataTable(dataTable1);
            dataTable1.fnAdjustColumnSizing();
            showDataTableReset(dataTable1);
        });
        $('#study-view-dataTable-updateTable').unbind('click');
        $('#study-view-dataTable-updateTable').click(function(){
            var filterString = "",
                filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
                
            for(var i=0 ; i<filteredResult.length ; i++){
                filterString += filteredResult[i].CASE_ID + '|';
            }
            filterString = filterString.substr(0,filterString.length-1);
            dataTable1.fnFilter(filterString,0,true);
            updateTable(dataTable1,[0]);
            resizeLeftColumn();            
            refreshSelectionInDataTable(dataTable1);
            dataTable1.fnAdjustColumnSizing();
            $("#dataTable_filter label input").attr("value","");
            showDataTableReset(dataTable1);
        });
        
        $("#dataTable_filter label input").keyup(function() {
            showDataTableReset(dataTable1);
            resizeLeftColumn();
        });
        $("#dataTable tbody").unbind('click');
        $("#dataTable tbody").mousedown(function(event){
            if(event.shiftKey){
                event.preventDefault();
                //TODO: Add table row shift click listener.
                console.log('here');
            }else{
                var returnValue, 
                    selectedRowCaseId = [],
                    oTable = $("#dataTable").dataTable();

               /*
                $(oTable.fnSettings().aoData).each(function (){
                    $(this.nTr).removeClass('row_selected');
                });
                */
                if($(event.target.parentNode).hasClass('row_selected')){
                    $(event.target.parentNode).removeClass('row_selected');
                    if($(event.target.parentNode).hasClass('odd')){
                       $(event.target.parentNode).css('background-color','#E2E4FF'); 
                    }else{
                        $(event.target.parentNode).css('background-color','white');
                    }
                }else{
                    $(event.target.parentNode).addClass('row_selected');
                    $(event.target.parentNode).css('background-color','lightgray');
                }
                returnValue = fnGetSelected(oTable);
                var returnValueLength = returnValue.length;
                for(var i=0 ; i< returnValueLength; i++){
                    selectedRowCaseId.push($(returnValue[i]).find('td').first().text());
                }

                if(selectedRowCaseId.length > 1){
                    removeMarker();
                    filterChartsByGivingIDs(selectedRowCaseId);
                }else if(selectedRowCaseId.length === 1){
                    removeMarker();
                    redrawChartsAfterDeletion();
                    getDataAndDrawMarker(selectedRowCaseId);
                }else{
                    removeMarker();
                    redrawChartsAfterDeletion();
                }
                setScatterPlotStyle(selectedRowCaseId,varChart[attrNameMapUID['CASE_ID']].filters());
            }
        });
    } 
    function showDataTableReset( _oTable ){
        var _showedColumnNumber = _oTable.fnSettings().fnRecordsDisplay();
        var _totalColumnNumber = _oTable.fnSettings().fnRecordsTotal();
        if(_showedColumnNumber !== _totalColumnNumber){
            $(".dataTableReset span").css('display','block');
        }else{
            $(".dataTableReset span").css('display','none');
        }
    }
    function fnGetSelected( oTableLocal ){
	var aReturn = new Array();
	var aTrs = oTableLocal.fnGetNodes();
	
	for ( var i=0 ; i<aTrs.length ; i++ ){
            if ( $(aTrs[i]).hasClass('row_selected') ){
                    aReturn.push( aTrs[i] );
            }
	}
	return aReturn;
    }

    function resizeLeftColumn(){
        var heightBody = $(".dataTables_scrollBody").css('height'),
            heightTable = $('.dataTables_scroll').css('height');
    
        $(".DTFC_LeftBodyLiner").css('height',heightBody);
        $(".DTFC_LeftBodyWrapper").css('height',heightBody); 
        $('.DTFC_ScrollWrapper').css('height',heightTable);            
    }
    
    function refreshSelectionInDataTable(_dataTable){
        $(".dataTables_scrollFoot tfoot th").each( function ( i ) {
            if(disableFiltId.indexOf(i) === -1){                
                $(this).css('z-index','1500');
                this.innerHTML = fnCreateSelect( _dataTable.fnGetColumnData(i));
                $('select', this).change( function () {
                    if($(this).val() === ''){
                        _dataTable.fnFilter($(this).val(), i);
                        disableFiltId.splice(disableFiltId.indexOf(i),1);
                    }else{
                        _dataTable.fnFilter("^"+$(this).val()+"$", i, true);
                        disableFiltId.push(i);
                    }
                    
                    refreshSelectionInDataTable(_dataTable);
                    resizeLeftColumn();
                });
            }
        });
    }
    
    function updateTable(_dataTable,_exceptionColumns){
        var oSettings = _dataTable.fnSettings();
        for(var iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
            if(_exceptionColumns.indexOf(iCol) === -1){
                oSettings.aoPreSearchCols[ iCol ].sSearch = '';
            }
        }
        oSettings.oPreviousSearch.sSearch = '';
        _dataTable.fnDraw();
    }

    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }
    
    function setScatterPlotStyle(_selectedCaseID,_filters){
        var style=[];
        for(var i=0 ; i< parObject.caseIds.length ; i++){
            var styleDatum ={};
            styleDatum.case_id = parObject.caseIds[i];
            if(_selectedCaseID.length !== parObject.caseIds.length){
                if(_selectedCaseID.indexOf(parObject.caseIds[i]) !== -1){
                    if(clickedCaseIds.indexOf(parObject.caseIds[i]) !== -1){
                        styleDatum.fill='red';
                        styleDatum.stroke='#3366cc';
                        styleDatum.strokeWidth='3px';
                        styleDatum.size='300';
                    }else{
                        styleDatum.fill='red';
                        styleDatum.stroke = 'red';
                        styleDatum.strokeWidth='0';
                        styleDatum.size='60';
                    }
                }else{
                    styleDatum.fill='#3366cc';
                    styleDatum.stroke = '#3366cc';
                    styleDatum.strokeWidth='0';
                    styleDatum.size='60';
                }
            }else if(_filters.length === 0){
                styleDatum.fill='#3366cc';
                styleDatum.stroke = '#3366cc';
                styleDatum.strokeWidth='0';
                styleDatum.size='60';
            }else{
                styleDatum.fill='red';
                styleDatum.stroke = 'red';
                styleDatum.strokeWidth='0';
                styleDatum.size='60';
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
        
        refreshSelectionInDataTable(oTable);
        
        oTable.fnAdjustColumnSizing();
        new FixedColumns( oTable);
        $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");
        $(".dataTables_scroll").css("overflow-x","scroll");
        $(".DTFC_LeftHeadWrapper").css("background-color","white");
    }
    
    function removePieMarker(){
        var _numOfCharts = varChart.length;
        for(var i=0; i< _numOfCharts; i++){
            if(attrNameMapUID['CASE_ID'] !== i){
                $("#study-view-dc-chart-" + i).find('svg g .mark').remove();
            }
        }
    }
    
    function redrawChartsAfterDeletion(){
        for(var i=0; i< varChart.length ; i++){
            if(removedChart.indexOf(i) === -1){
                if(varChart[i].filters().length > 0)
                    varChart[i].filterAll();
            }
        }
        dc.redrawAll();
    }
    
    function scatterPlotBrushCallBack(_brushedCaseIds) {
        brushedCaseIds = _brushedCaseIds;
        if(_brushedCaseIds.length === 0 || (shiftClickedCaseIds.length === 1 && _brushedCaseIds.indexOf(shiftClickedCaseIds[0]) === -1)){
            shiftClickedCaseIds = [];
            clickedCaseId = '';
        }
        scatterPlotCallBack(_brushedCaseIds);
        removeMarker();
    }
    
    function scatterPlotCallBack(_caseIDs){
        var _numOfCharts = varChart.length;
        if(_caseIDs.length > 0){
            varChart[attrNameMapUID['CASE_ID']].filterAll();
            varChart[attrNameMapUID['CASE_ID']].filter([_caseIDs]);
            dc.redrawAll();
        }else{
            for(var i=0; i< _numOfCharts ; i++){
                if(varChart[i].filters().length > 0)
                    varChart[i].filterAll();
            }
            dc.redrawAll();
            brushOn = false;
        }
        changeHeader();
    }
    
    function scatterPlotClickCallBack(_clickedCaseIds) {
        var _typeOfInputClickedCases = typeof _clickedCaseIds;
        if(_typeOfInputClickedCases === 'string'){
            clickedCaseId = _clickedCaseIds;
            scatterPlotClick(_clickedCaseIds);
        }else{
            shiftClickedCaseIds = _clickedCaseIds;
            scatterPlotShiftClick(_clickedCaseIds);
        }
    }
    
    function scatterPlotClick(_clickedCaseId){
        if(_clickedCaseId !== ''){
            removeMarker();
            getDataAndDrawMarker([_clickedCaseId]);
        }else{
            removeMarker();
        }
    }
    
    function scatterPlotShiftClick(_shiftClickedCaseIds){
        var _shiftClickedCasesLength = _shiftClickedCaseIds.length;
        
        removeMarker();
        shiftClickedCaseIds = _shiftClickedCaseIds;
        if(_shiftClickedCasesLength !== 0){
           /*if(clickedCaseId !== ''){
                if(shiftClickedCaseIds.indexOf(clickedCaseId) === -1){
                    shiftClickedCaseIds.push(clickedCaseId);
                }
                removeMarker();
                scatterPlotCallBack(shiftClickedCaseIds);
                getDataAndDrawMarker([clickedCaseId]);
            }else{*/
            clickedCaseId = '';
            scatterPlotCallBack(shiftClickedCaseIds);
            //}
        }else{
            redrawChartsAfterDeletion();
            if(clickedCaseId !== '')
                getDataAndDrawMarker([clickedCaseId]);
        }
    }
    
    function getDataAndDrawMarker(_clickedCaseIds) {
        var _numOfCharts = varChart.length;
        for(var i=0; i< _numOfCharts; i++){
            if(removedChart.indexOf(i) === -1){
                if(attrNameMapUID['CASE_ID'] !== i){
                    var _datum = $("#study-view-dc-chart-" + i);
                    var _value = _datum.attr('value');
                    var _valueArray = _value.split(",");
                    if(_valueArray[2] === 'pie'){
                        var _relativeValue = dataArr[_clickedCaseIds[0]][_valueArray[0]];
                        var gArray = _datum.find('svg g g');
                        $.each(gArray, function(key,value){
                            var _title = $(this).find('title').text();
                            var _titleArray = _title.split(":");
                            var _key = _titleArray[0];
                            if(_key === _relativeValue){
                                drawPieMarker($(this).find('path').attr('d'),"study-view-dc-chart-" + i,key,i);
                            }
                        });
                    }else if(_valueArray[2] === 'bar'){
                        drawBarMarker("study-view-dc-chart-" + i,dataArr[_clickedCaseIds[0]][_valueArray[0]]);
                    }
                }
            }
        }
    }
    
    function drawPieMarker(_d,_pieChartID,_childID,_fatherID) {
        var tmpPointsInfo = _d.split(/[\s,MLHVCSQTAZ]/),          
            tmpPointsInfo1 = _d.split(/[A]/);

        var x1 = Number(tmpPointsInfo[1]),
            y1 = Number(tmpPointsInfo[2]),
            largeArc = Number(tmpPointsInfo[6]),
            sweep = Number(tmpPointsInfo[7]),
            x2 = Number(tmpPointsInfo[8]),
            y2 = Number(tmpPointsInfo[9]),
            r = Number(tmpPointsInfo[3]);
    
        if((x1-x2!==0 ||y1-y2!==0) && tmpPointsInfo1.length === 2){
            var _xm = (x1 + x2) /2,
                _ym = (y1 + y2) /2;

            var m = Math.sqrt((Math.pow(_xm,2)+Math.pow(_ym,2)));
            
            var _tmpX = (r + 6) / m * _xm,
                _tmpY = (r + 6) / m * _ym;
            
            if(_xm === 0 && _ym === 0){
                _tmpY = 0;
                _tmpX = r + 6;
            }
            
            if(largeArc === 1 && Math.abs(x1 - x2) >0.1) {
                _tmpX = -_tmpX;
                _tmpY = -_tmpY;
            }
            
            var textID = "path-" + _fatherID+"-"+Number(_childID);
                    
            d3.select("#" + _pieChartID + " svg g").append("path")
                .attr("transform", function(d) { return "translate(" + _tmpX + "," + _tmpY + ")"; })
                .attr("d", d3.svg.symbol().size('25').type('triangle-up'))
                .attr('fill',"red")
                .attr('id',textID)
                .attr('class','mark');
        }
    }
    
    function removeMarker(){
        removePieMarker();
        removeBarMarker();
    }
    function removeBarMarker(){
        var _numOfCharts = varChart.length;
        for(var i=0; i< _numOfCharts; i++){
            if(attrNameMapUID['CASE_ID'] !== i){
                $("#study-view-dc-chart-" + i).find('svg .mark').remove();
            }
        }
    }
    
    function drawBarMarker(_barChartID,_value) {
        var transformChartBody = trimTransformString($('#' + _barChartID + " .chart-body").attr("transform"));
        var allBars = $('#' + _barChartID + " .chart-body").find('rect');
        var bar = [];
        var allAxisX = $('#' + _barChartID + " .axis.x").find("g");
        var transformAxiaX = trimTransformString($('#' + _barChartID + " .axis.x").attr("transform"));
        
        var xValue = [];
        var xTranslate = [];
        
        $.each(allBars,function(key,value){
            var barDatum = {}
            barDatum.x = Number($(this).attr('x')) + Number(transformChartBody[0]);
            barDatum.y = Number($(this).attr('y')) + Number(transformChartBody[1]) - 5;
            barDatum.width = Number($(this).attr('width'));
            bar.push(barDatum);
        });
        
        var numOfBar = bar.length;
        
        $.each(allAxisX,function(key,value){
            xValue[key] = Number($(this).select('text').text());
            xTranslate[key] = Number(trimTransformString($(this).attr('transform'))[0]) + Number(transformAxiaX[0]);
        });
        
        var x,y,
            numItemOfX = xTranslate.length;
        
        if(_value === 'NA'){
            x = bar[numOfBar-1].x + bar[numOfBar-1].width / 2;
            y= bar[numOfBar-1].y;
        }else {
            for(var i=0 ; i< numItemOfX ; i++){
                if(_value < xValue[i]){
                    for(var j=0 ; j< numOfBar ; j++){
                        if(bar[j].x < xTranslate[i] && bar[j].x > xTranslate[i-1]){
                            x = (xTranslate[i] + xTranslate[i-1])/2;
                            y= bar[j].y;
                            break;
                        }
                    }
                    break;
                }
            }
        }
        
        d3.select("#" + _barChartID + " svg").append("path")
            .attr("transform", function(d) { return "translate(" + x + "," + y + ")"; })
            .attr("d", d3.svg.symbol().size('25').type('triangle-down'))
            .attr('fill',"red")
            .attr('class','mark');
    }
    
    function trimTransformString(_string){
        var _tmpString = _string.split("(");
        
        _tmpString = _tmpString[1].split(")");
        _tmpString = _tmpString[0].split(",");
        
        return _tmpString;
    }
    
    function fnCreateSelect( aData ){
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
    
    function getRefererCaseId() {
        var idStr = /^#?case_ids=(.+)/.exec(location.hash);
        if (!idStr) return null;
        return idStr[1].split(/[ ,]+/);
    }
    function filterCharts(){
        var ids = getRefererCaseId();
        if(ids !== null){
            filterChartsByGivingIDs(ids);
        }
    }
    function filterChartsByGivingIDs(_ids){
        varChart[attrNameMapUID['CASE_ID']].filterAll();
        varChart[attrNameMapUID['CASE_ID']].filter([_ids]);
        dc.redrawAll();
        setScatterPlotStyle(_ids,varChart[attrNameMapUID['CASE_ID']].filters());
        changeHeader();
    }
    function removeAllNAValue(_data){
        var dataAttr = _data.attr, //Atrributes
            dataArr = _data.dataObjectM; //All data
    }
    function initPage(){
        $("#study-view-charts").html("");
        $("#study-view-charts").append(StudyViewBoilerplate.scatterPlotDiv);
        $("#data-table-chart").html("");
        $("#data-table-chart").append(StudyViewBoilerplate.dataTableDiv);
    }
    function initData(_data){
        var _dataArrLength = _data.dataObjectM.length;
        for(var i=0 ; i< _dataArrLength ; i++){
            dataArr[_data.dataObjectM[i].CASE_ID] = _data.dataObjectM[i];
        }
    }
    return {
        init: function(o,data){
            initData(data);
            initPage();
            initParameters(o);
            initCharts(data);
            restyle(data);
            resizeTable();
            filterCharts();
        }
    };
})();
