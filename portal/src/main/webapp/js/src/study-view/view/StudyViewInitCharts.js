/*
 * View for All Charts 
 * 
 *                                       
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */


var StudyViewInitCharts = (function(){
    

    var numOfCases,
        scatterStudyView,
        ndx,
        msnry,
        totalCharts,
        clickedCaseId = '',
        dataArr = {},
        createdChartID,
        removedChart = [],
        attrNameMapUID = [], //The relationshio between "The unique attribute name" and "The unique ID number in whole page"        
        brushedCaseIds = [],
        varName = [], //Store all attributes in all data
        HTMLtagsMapUID = [], //The relationshio between "The ID of div standing for each Chart in HTML" and "The unique ID number in whole page"
        varType = [], //Could be displayed Charts Type 'pie,bar'. Mix combination, seperate by comma
        distanceMinMaxArray = [], //Only for NUMBER dataType, store min, max and difference value
        dataType = {},
        displayedID = [], //Displayed Charts ID number
        varDisplay = [], //Displayed Charts Name -- the display_name in each attribute       
        shiftClickedCaseIds = [],
        chartColors = jQuery.extend(true, [], StudyViewBoilerplate.chartColors), // Color scale from GOOGLE charts
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
            scatterPlotArr = [],
            scatterPlotDataAttr = {},
            scatterPlotOptions = {};
            
        var dataA = dataObtained.attr, //Atrributes
            dataB = dataObtained.arr; //All data
            
        $("#study-view-scatter-plot-pdf-name").val("Scatter_Plot_result-"+ parObject.studyId +".pdf");
        $("#study-view-scatter-plot-svg-name").val("Scatter_Plot_result-"+ parObject.studyId +".svg");
        
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
            
            if(dataA[i]["datatype"] === "NUMBER"){
                dataType[dataA[i]["attr_id"]] = 'allnumeric';
            }else{
                dataType[dataA[i]["attr_id"]] = 'string';
            }
            
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

        //var totalCharts = pie.length,
        totalCharts = pie.length + bar.length,
            createdChartID = 0;
            
        for(var i=0; i< pie.length ; i++){
            makeNewPieChartInstance(createdChartID, pie[i]);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[pie[i]["attr_id"]] = createdChartID;
            displayedID.push(pie[i]["attr_id"]);
            createdChartID++;
        }
        
        for(var i=0; i< bar.length ; i++){
            makeNewBarChartInstance(createdChartID, bar[i], distanceMinMaxArray[bar[i]['attr_id']]);
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
                $('#study-view-add-chart').css('display','block');
                $('#study-view-add-chart ul')
                        .append($('<li></li>')
                            .attr('id','mutationCNA')
                            .text('Number of Mutation vs Fraction of copy number altered genome'));
                msnry.layout();
                clickedCaseId = '',
                brushedCaseIds = [];
                shiftClickedCaseIds = [];
                //deleteChartResetDataTable($("#dataTable").dataTable());
                AddCharts.bindliClickFunc();
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
        msnry = new Masonry( container, {
            columnWidth: 190,
            itemSelector: '.study-view-dc-chart',
            gutter:1
        });
        
        $('.study-view-dc-chart-delete').unbind('click');
        $('.study-view-dc-chart-delete').click(function(event){
                var _id = $(this).parent().parent().attr("id").split("-");
                var _valueA = $(this).parent().parent().attr('value').split(',');
                
                deleteChart(_id[_id.length-1],_valueA);
                msnry.layout();
                AddCharts.bindliClickFunc();
        });
        /*
        $('#study-view-header-left-4').click(function (){
            removeAllNAValue(dataObtained);
        });
        */
    }
    
    function makeNewPieChartInstance(_chartID, _pieInfo) {
        var _param = {
                baseID: "study-view",
                chartID: _chartID,
                chartDivClass: 'study-view-pie-chart',
                attrID: _pieInfo.attr_id,
                displayName: _pieInfo.display_name,
                transitionDuration: 800,
                ndx: ndx,
                chartColors: chartColors
            };

        var _piechartCallbackFunction = function(_currentPieFilters){
            var _tmpResult = varChart[attrNameMapUID["CASE_ID"]]
                    .getChart()
                    .dimension()
                    .top(Infinity),
            _tmpCaseID = [];

            clickedCaseId = '';

            if(typeof scatterStudyView !== 'undefined'){
                for(var i=0; i<_tmpResult.length ; i++){
                    _tmpCaseID.push(_tmpResult[i].CASE_ID);
                }
                setScatterPlotStyle(_tmpCaseID,_currentPieFilters);
            }
        };
        
        varChart[_chartID] = new PieChart();
        varChart[_chartID].init(_param);
        
        varChart[_chartID].scatterPlotCallbackFunction(_piechartCallbackFunction);
        varChart[_chartID].postFilterCallbackFunc(changeHeader);
    }
    
    function makeNewBarChartInstance(_chartID, _barInfo, _distanceArray) {
        var param = {
                baseID: "study-view",
                chartID: _chartID,
                chartDivClass: 'study-view-bar-chart',
                attrID: _barInfo.attr_id,
                displayName: _barInfo.display_name,
                transitionDuration: 800,
                ndx: ndx,
                needLogScale: false,
                distanceArray: _distanceArray
            };
            
        if(_distanceArray.distance > 1000){
            param.needLogScale = true;
        }else{
            param.needLogScale = false;
        }
        
        varChart[_chartID] = new BarChart();
        varChart[_chartID].init(param);
        var _barchartCallbackFunction = function(_currentPieFilters){
            var _tmpResult = varChart[attrNameMapUID["CASE_ID"]]
                    .getChart()
                    .dimension()
                    .top(Infinity),
            _tmpCaseID = [];

            clickedCaseId = '';

            if(typeof scatterStudyView !== 'undefined'){
                for(var i=0; i<_tmpResult.length ; i++){
                    _tmpCaseID.push(_tmpResult[i].CASE_ID);
                }
                setScatterPlotStyle(_tmpCaseID,_currentPieFilters);
            }
        };
        varChart[_chartID].scatterPlotCallbackFunction(_barchartCallbackFunction);
        varChart[_chartID].postFilterCallbackFunc(changeHeader);

        if(_distanceArray.distance > 1000){
            $("#scale-input-"+_chartID).change(function(e) {
                $(this).parent().parent().find('svg').remove();
                var _idArray = $(this).attr('id').split('-'),
                    _currentID = _idArray[2];

                if(varChart[_currentID].getChart().hasFilter()){
                    varChart[_currentID].getChart().filterAll();
                    dc.redrawAll();
                }
                dc.deregisterChart(varChart[_currentID].getChart());                

                var _param = {};

                if($(this).attr('checked')){
                    _param.needLogScale = true;
                }else{
                    _param.needLogScale = false;
                }
                varChart[_currentID].updateParam(_param);
                varChart[_currentID].reDrawChart();
                varChart[_currentID].getChart().render();
            });
        }
    }
    
    function deleteChart(_chartID,_value){
        $("div").remove("#study-view-dc-chart-main-" + _chartID); 
        if(varChart[_chartID].getChart().hasFilter()){
            varChart[_chartID].getChart().filterAll();
            dc.redrawAll();
        }
        dc.deregisterChart(varChart[_chartID].getChart());
        $('#study-view-add-chart ul')
                .append($('<li></li>').attr('id',_value[0]).text(_value[1]));
        
        $('#study-view-add-chart').css('display','block');
        varChart[_chartID] = "";
        //deleteChartResetDataTable($("#dataTable").dataTable());
        removedChart.push(Number(_chartID));
    }
    
    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue,scatterPlotDataAttr){
        $("#" + _svgParentDivId + " .plots-title-x-help").remove();
        $("#" + _svgParentDivId + " .plots-title-y-help").remove();
        
        var svgElement = $("#" + _svgParentDivId).html();
        $("#" + _idNeedToSetValue).val(svgElement);
        scatterStudyView.updateTitleHelp(scatterPlotDataAttr.names.log_scale_x, scatterPlotDataAttr.names.log_scale_y);
    }
        
    function changeHeader(){
        var tmpDimention = varChart[attrNameMapUID["CASE_ID"]].getChart().dimension();
        var tmpResult = tmpDimention.top(Infinity);
        
        StudyViewInitTopComponents.changeHeader(tmpResult, numOfCases, removedChart);
    }
    
    function updateDataTableCallbackFuncs() {
        
        var _dataTableRowClickCallback = function(_deSelect, _selectedRowCaseId) {
            clickedCaseId = _selectedRowCaseId;
            removeMarker();
            redrawChartsAfterDeletion();
            if(!_deSelect){
                getDataAndDrawMarker(_selectedRowCaseId);
            }
            setScatterPlotStyle(_selectedRowCaseId,varChart[attrNameMapUID['CASE_ID']].getChart().filters());
        };
        
        var _dataTableRowShiftClickCallback = function(_selectedRowCaseId) {
            shiftClickedCaseIds = _selectedRowCaseId;
            clickedCaseId = '';
            removeMarker();
            filterChartsByGivingIDs(_selectedRowCaseId);
            setScatterPlotStyle(_selectedRowCaseId,varChart[attrNameMapUID['CASE_ID']].getChart().filters());
        };
        
        DATATABLE.rowClickCallback(_dataTableRowClickCallback);
        DATATABLE.rowShiftClickCallback(_dataTableRowShiftClickCallback);
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
                    if(clickedCaseId !== ''){
                        styleDatum.fill='#3366cc';
                        styleDatum.stroke='red';
                        styleDatum.strokeWidth='3';
                        styleDatum.size='120';
                    }else{
                        styleDatum.fill='red';
                        styleDatum.stroke = 'red';
                        styleDatum.strokeWidth='0';
                        styleDatum.size='120';
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
                styleDatum.size='120';
            }
            style.push(styleDatum);
        }
        scatterStudyView.updateStyle(style);
    }
    
    function redrawChartsAfterDeletion(){
        for(var i=0; i< varChart.length ; i++){
            if(removedChart.indexOf(i) === -1){
                if(varChart[i].getChart().filters().length > 0)
                    varChart[i].getChart().filterAll();
            }
        }
        dc.redrawAll();
    }
    
    function scatterPlotBrushCallBack(_brushedCaseIds) {
        brushedCaseIds = _brushedCaseIds;
        if(_brushedCaseIds.length === 0 || (shiftClickedCaseIds.length === 1 && _brushedCaseIds.indexOf(shiftClickedCaseIds[0]) === -1)){
            shiftClickedCaseIds = [];
            clickedCaseId = '';
            
            var oTable = $("#dataTable").dataTable();

            $(oTable.fnSettings().aoData).each(function (){
                if($(this.nTr).hasClass('row_selected')){
                    $(this.nTr).removeClass('row_selected');
                    if($(this.nTr).hasClass('odd')){
                       $(this.nTr).css('background-color','#E2E4FF'); 
                    }else{
                        $(this.nTr).css('background-color','white');
                    }
                }
            });
        }
        scatterPlotCallBack(_brushedCaseIds);
        removeMarker();
    }
    
    function scatterPlotCallBack(_caseIDs){
        var _numOfCharts = varChart.length;
        if(_caseIDs.length > 0){
            varChart[attrNameMapUID['CASE_ID']].getChart().filterAll();
            varChart[attrNameMapUID['CASE_ID']].getChart().filter([_caseIDs]);
            dc.redrawAll();
        }else{
            for(var i=0; i< _numOfCharts ; i++){
                if(varChart[i].getChart().filters().length > 0)
                    varChart[i].getChart().filterAll();
            }
            dc.redrawAll();
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
        changeHeader();
    }
    
    function scatterPlotShiftClick(_shiftClickedCaseIds){
        var _shiftClickedCasesLength = _shiftClickedCaseIds.length;
        
        removeMarker();
        shiftClickedCaseIds = _shiftClickedCaseIds;
        if(_shiftClickedCasesLength !== 0){
            clickedCaseId = '';
            scatterPlotCallBack(shiftClickedCaseIds);
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
                                varChart[i].drawMarker(key+1,i);
                            }
                        });
                    }else if(_valueArray[2] === 'bar'){
                         varChart[i].drawMarker(dataArr[_clickedCaseIds[0]][_valueArray[0]]);
                    }
                }
            }
        }
    }
    
    function removeMarker(){
        var i, _chartLength = varChart.length;
        
        for( i = 0; i < _chartLength; i++ ){
            varChart[i].removeMarker();
        }
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
        varChart[attrNameMapUID['CASE_ID']].getChart().filterAll();
        varChart[attrNameMapUID['CASE_ID']].getChart().filter([_ids]);
        dc.redrawAll();
        setScatterPlotStyle(_ids,varChart[attrNameMapUID['CASE_ID']].getChart().filters());
        changeHeader();
    }
    
    function initPage(){
        $("#study-view-charts").html("");
        $("#study-view-charts").append(StudyViewBoilerplate.scatterPlotDiv);
        //$("#data-table-chart").html("");
        //$("#data-table-chart").append(StudyViewBoilerplate.dataTableDiv);
    }
    
    function initData(_data){
        var _dataArrLength = _data.arr.length;
        for(var i=0 ; i< _dataArrLength ; i++){
            dataArr[_data.arr[i].CASE_ID] = _data.arr[i];
        }
    }
    
    return {
        init: function(o,data) {
            initData(data);
            initPage();
            initParameters(o);
            initCharts(data);
            updateDataTableCallbackFuncs();
            filterCharts();
        },
        
        getFilteredResults: function() {
            var _filteredResult = varChart[attrNameMapUID['CASE_ID']].getCluster().top(Infinity);
            return _filteredResult;
        },
        
        getShowedChartsInfo: function() {
            var _param = {};
            
            _param.name = varName;
            _param.displayName = varDisplay;
            _param.displayedID = displayedID;
            
            return _param;
        },
        
        createNewChart: function(_id, _text) {
            var _chartType = [];
            
            if(_id === 'mutationCNA')
                _chartType = ['scatter'];
            else
                _chartType = varType[_id].split(',');

            var selectedAttr = _id;
            var selectedAttrDisplay = _text;
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

                if(selectedChartType === 'pie'){
                    makeNewPieChartInstance(chartTmpID, {attr_id:selectedAttr,display_name:selectedAttrDisplay});
                }else{
                    makeNewBarChartInstance(chartTmpID, {attr_id:selectedAttr,display_name:selectedAttrDisplay}, distanceMinMaxArray[selectedAttr]);
                }


                msnry.destroy();
                var container = document.querySelector('#study-view-charts');
                msnry = new Masonry( container, {
                  columnWidth: 190,
                  itemSelector: '.study-view-dc-chart',
                  gutter:1
                });

                varChart[chartTmpID].getChart().render();

                $('#study-view-dc-chart-'+ chartTmpID +' .study-view-dc-chart-delete').unbind('click');
                $('#study-view-dc-chart-'+ chartTmpID +' .study-view-dc-chart-delete').click(function(event){
                    var valueA = $(this).parent().parent().attr("value").split(',');
                    deleteChart(chartTmpID,valueA);
                    AddCharts.bindliClickFunc();
                    msnry.layout();
                });
            }

            var index = removedChart.indexOf(chartTmpID);
            if (index > -1) {
                removedChart.splice(index, 1);
            }
            msnry.layout();

            $('#study-view-add-chart ul').find('li[id="' + selectedAttr + '"]').remove();

            if($('#study-view-add-chart ul').find('li').length === 0 ){
                $('#study-view-add-chart').css('display','none');
            }
        },
        
        filterChartsByGivingIDs: filterChartsByGivingIDs,
        
        changeHeader: changeHeader
    };
})();
