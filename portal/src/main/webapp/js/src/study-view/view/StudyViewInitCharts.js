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
        ndx,
        msnry,
        totalCharts,
        clickedCaseId = '',
        dataArr = {},
        createdChartID,
        pie = [], //Displayed attributes info, dataType: STRING, NUMBER, OR BOOLEAN
        bar = [], //Displayed attributes info, dataType: NUMBER
        
        //Each attribute will have one unique ID. If the chart of one attribute
        //has been deleted, this ID will push into removedChart array. This
        //array will be used when the chart deleted and redraw charts.
        removedChart = [], 
        
        //The relationshio between "The unique attribute name" and 
        //"The unique ID number in whole page"
        attrNameMapUID = [],
        brushedCaseIds = [],
        varChart = [],
        removeKeyIndex = [],
        varName = [], //Store all attributes in all data
        
        //The relationshio between "The ID of div standing for each Chart
        //in HTML" and "The unique ID number in whole page".
        HTMLtagsMapUID = [], 
        
        //Could be displayed Charts Type 'pie,bar'.
        //Mix combination, seperate by comma
        varType = [],
        
        //Only for NUMBER dataType, store min, max and difference value
        distanceMinMaxArray = [], 
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
    
    function initData(dataObtained) {
        var _keys = [],
            _attr = dataObtained.attr,
            _arr = dataObtained.arr,
            _attrLength = _attr.length,
            _arrLength = _arr.length;
            
        numOfCases = _arr.length;        
        ndx = crossfilter(_arr);
        
        //Calculate the number of pie, bar charts
        //Initial varName, varType, distanceMinMaxArray, varDisplay
        for( var i = 0; i < _attrLength; i++ ){
            var _varValuesNum = [];
            
            for( var j = 0; j < _arrLength; j++ ){
                if(_varValuesNum.hasOwnProperty(_arr[j][_attr[i]["attr_id"]])){
                    _varValuesNum[_arr[j][_attr[i]["attr_id"]]]++;
                }else{
                    _varValuesNum[_arr[j][_attr[i]["attr_id"]]]=0;
                }
                dataArr[_arr[j].CASE_ID] = _arr[j];   
            }
            
            _keys = Object.keys(_varValuesNum);
            
            if(_attr[i]["datatype"] === "NUMBER"){
                dataType[_attr[i]["attr_id"]] = 'allnumeric';
            }else{
                dataType[_attr[i]["attr_id"]] = 'string';
            }
            
            if(_attr[i]["attr_id"] === "CASE_ID"){
                pie.push(_attr[i]);
            }else if(_attr[i]["datatype"] === "NUMBER" || _attr[i]["datatype"] === "BOOLEAN"){                
                if(selectedCol(_attr[i]["attr_id"])){                    
                    if(_keys.length>10 || _attr[i]["attr_id"] === 'MUTATION_COUNT' 
                            || _attr[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS')
                        bar.push(_attr[i]);
                    else
                        pie.push(_attr[i]);
                }

                if(_keys.length > 10 || _attr[i]["attr_id"] === 'MUTATION_COUNT' 
                        || _attr[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS'){
                    varType[_attr[i]["attr_id"]] = "bar";
                }else{
                    varType[_attr[i]["attr_id"]] = "pie";
                }
                
                if(_attr[i]["datatype"] === "NUMBER"){
                    var _varValues = [];
                    
                    for(var j=0;j<_arr.length;j++){
                        if(_arr[j][_attr[i]["attr_id"]] && 
                                _arr[j][_attr[i]["attr_id"]]!=="NA" && 
                                _arr[j][_attr[i]["attr_id"]]!==""){
                            _varValues.push(_arr[j][_attr[i]["attr_id"]]);  
                        }
                    }

                    distanceMinMaxArray[_attr[i]["attr_id"]] = {
                        diff : Math.max.apply( Math, _varValues ) - Math.min.apply( Math, _varValues ),
                        min: Math.min.apply( Math, _varValues ),
                        max:Math.max.apply( Math, _varValues )
                    };
                }

            }else if(_attr[i]["datatype"] === "STRING"){
                if(selectedCol(_attr[i]["attr_id"])){
                    pie.push(_attr[i]);
                }
                varType[_attr[i]["attr_id"]] = "pie";
            }else {
                console.log("Can not identify data type.");
            }

            varDisplay.push(_attr[i]["display_name"]);                
            varName.push(_attr[i]["attr_id"]);
        }

        //var totalCharts = pie.length,
        totalCharts = pie.length + bar.length;
        initScatterPlot(_arr);
    }
    
    function createLayout() {
        var container = document.querySelector('#study-view-charts');
        msnry = new Masonry( container, {
            columnWidth: 190,
            itemSelector: '.study-view-dc-chart',
            gutter:1
        });
        msnry.layout();
    }
    
    function initScatterPlot(_arr) {
        StudyViewInitScatterPlot.init(parObject, _arr);
        
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
    }
    
    function initCharts() {
        var createdChartID = 0;
            
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
        //dc.renderAll("group1");
        
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

            if(typeof StudyViewInitScatterPlot.getScatterPlot() !== 'undefined'){
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
            
        if(_distanceArray.diff > 1000){
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

            if(typeof StudyViewInitScatterPlot.getScatterPlot() !== 'undefined'){
                for(var i=0; i<_tmpResult.length ; i++){
                    _tmpCaseID.push(_tmpResult[i].CASE_ID);
                }
                setScatterPlotStyle(_tmpCaseID,_currentPieFilters);
            }
        };
        varChart[_chartID].scatterPlotCallbackFunction(_barchartCallbackFunction);
        varChart[_chartID].postFilterCallbackFunc(changeHeader);

        if(_distanceArray.diff > 1000){
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
        removedChart.push(Number(_chartID));
    }
        
    function changeHeader(){
        var _dimention = varChart[attrNameMapUID["CASE_ID"]].getChart().dimension();
        var _result = _dimention.top(Infinity);
        
        StudyViewInitTopComponents.changeHeader(_result, numOfCases, removedChart);
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
        
        StudyViewInitDataTable.getDataTable().rowClickCallback(_dataTableRowClickCallback);
        StudyViewInitDataTable.getDataTable().rowShiftClickCallback(_dataTableRowShiftClickCallback);
    }
    
    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }
    
    function setScatterPlotStyle(_selectedCaseID,_filters){
        var _style = [];
        
        for(var i=0 ; i< parObject.caseIds.length ; i++){
            var styleDatum = {};
            
            styleDatum.case_id = parObject.caseIds[i];
            if(_selectedCaseID.length !== parObject.caseIds.length){
                if(_selectedCaseID.indexOf(parObject.caseIds[i]) !== -1){
                    if(clickedCaseId !== ''){
                        styleDatum.fill = '#3366cc';
                        styleDatum.stroke = 'red';
                        styleDatum.strokeWidth = '3';
                        styleDatum.size = '120';
                    }else{
                        styleDatum.fill = 'red';
                        styleDatum.stroke = 'red';
                        styleDatum.strokeWidth = '0';
                        styleDatum.size = '120';
                    }
                }else{
                    styleDatum.fill = '#3366cc';
                    styleDatum.stroke = '#3366cc';
                    styleDatum.strokeWidth = '0';
                    styleDatum.size = '60';
                }
            }else if(_filters.length === 0){
                styleDatum.fill = '#3366cc';
                styleDatum.stroke = '#3366cc';
                styleDatum.strokeWidth = '0';
                styleDatum.size = '60';
            }else{
                styleDatum.fill = 'red';
                styleDatum.stroke = 'red';
                styleDatum.strokeWidth = '0';
                styleDatum.size = '120';
            }
            _style.push(styleDatum);
        }
        StudyViewInitScatterPlot.getScatterPlot().updateStyle(_style);
    }
    
    function redrawChartsAfterDeletion(){
        for(var i = 0; i < varChart.length; i++){
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
        if(_clickedCaseIds.length > 0) {
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
    
    function createNewChartFromOutside(_id, _text) {
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
    }
    return {
        init: function(o,data) {
            initParameters(o);
            initData(data);
            initCharts(data);
            createLayout();
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
        
        getScatterPlotInitValue: function() {
            var _datum = {};
            
            _datum.min_x = distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].min;
            _datum.max_x = distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].max;
            _datum.min_y = distanceMinMaxArray['MUTATION_COUNT'].min;
            _datum.max_y = distanceMinMaxArray['MUTATION_COUNT'].max;
            
            return _datum;
        },
        
        getCharts: function() {
            return varChart;
        },
        
        getChartsByID: function(_index) {
            return varChart[_index];
        },
        
        filterChartsByGivingIDs: filterChartsByGivingIDs,
        changeHeader: changeHeader,
        scatterPlotBrushCallBack: scatterPlotBrushCallBack,
        scatterPlotClickCallBack: scatterPlotClickCallBack,
        createNewChart: createNewChartFromOutside        
    };
})();
