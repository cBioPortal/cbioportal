/* 
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

/*
 * View for All Charts 
 * 
 * @param: _parmas -- include studyId, caseIds, cnaProfileId and mutationProfileId.
 * @param: _data -- TWO PROPERTIES
 *                 attr: data attributes, each attribute is an object which
 *                       include attr_id, datatype, description and dispaly_name
 *                 arr: data contents, each object of this array stand for one
 *                      case. Each case should include all attributes as key and
 *                      their relative value. If attribute of this case doesn't
 *                      exist, the NA value will still give to this attribute.
 *
 * @interface: getFilteredResults -- return the filtered result list of CASE_ID
 *                                   chart.
 * @interface: getShowedChartsInfo -- return charts infomation including
 *                                    ID names, display names and displayed ID.
 * @interface: getCharts -- return all DC charts
 * 
 * Following interfaces will call StudyViewInitCharts functions directly.
 * 
 * 
 * @author: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */


var StudyViewInitCharts = (function(){
    

    var numOfCases,
        ndx, //Crossfilter dimension
        msnry,
        totalCharts,
        mutatedGenes = [],
        dataArr = {},
        pie = [], //Displayed attributes info, dataType: STRING, NUMBER, OR BOOLEAN
        bar = [], //Displayed attributes info, dataType: NUMBER
        
        //Each attribute will have one unique ID. If the chart of one attribute
        //has been deleted, this ID will push into removedChart array. This
        //array will be used when the chart deleted and redraw charts.
        removedChart = [],
        
        //The relationshio between "The unique attribute name" and 
        //"The unique ID number in whole page"
        attrNameMapUID = {},
        varChart = [],
        varName = [], //Store all attributes in all data
        varKeys = {}, //Store all keys for each attribute
        
        //The relationshio between "The ID of div standing for each Chart
        //in HTML" and "The unique ID number in whole page".
        HTMLtagsMapUID = [], 
        
        //Could be displayed Charts Type 'pie,bar'.
        //Mix combination, seperate by comma
        varType = {},
        
        //Only for NUMBER dataType, store min, max and difference value
        distanceMinMaxArray = [], 
        dataType = {},
        displayedID = [], //Displayed Charts ID number
        varDisplay = [], //Displayed Charts Name -- the display_name in each attribute   
        
        WORDCLOUDTEXTSIZECONSTANT = 200,
        barOriginalColor = "#1F77B4",
       
        // Color scale from GOOGLE charts
        chartColors = jQuery.extend(true, [], StudyViewBoilerplate.chartColors),
        
        //Flag for plot data button using only. Pie/Bar chart all has postdraw 
        //and postfiltered functions which will effect the survival/scatter 
        //plot if click filtered chart's 'plot data' button which will clear 
        //filter and redraw first then call the postredraw and postfiltered 
        //functions
        plotDataFlag = false;
       
    
    function allNumberElements(_array){
        var _length = _array.length;
        var _flag = true;//all number flag;
        for(var i = 0; i < _length; i++){
            if(_array[i] !== 'NA' && isNaN(_array[i])){
                _flag = false;
                break;
            }
        }
        return _flag;
    }
    
    function initData(dataObtained) {
        var _keys = [], //number of keys for each attribute
            _attr = dataObtained.attr,
            _arr = dataObtained.arr,
            _attrLength = _attr.length,
            _arrLength = _arr.length,
            _studyDesc = "";
        
        mutatedGenes = dataObtained.mutatedGenes;   
        numOfCases = _arr.length;        
        ndx = crossfilter(_arr);
        
        //Calculate the number of pie, bar charts
        //Initial varName, varType, distanceMinMaxArray, varDisplay
        for( var i = 0; i < _attrLength; i++ ){
            var _varValuesNum = [];
            var _dataType = _attr[i]["datatype"].toUpperCase();
            var _allNumber = false;
            
            for( var j = 0; j < _arrLength; j++ ){
                if(_varValuesNum.hasOwnProperty(_arr[j][_attr[i]["attr_id"]])){
                    _varValuesNum[_arr[j][_attr[i]["attr_id"]]]++;
                }else{
                    _varValuesNum[_arr[j][_attr[i]["attr_id"]]]=0;
                }
                dataArr[_arr[j].CASE_ID] = _arr[j];   
            }
            
            _keys = Object.keys(_varValuesNum);
             //If chart only has one category and it is NA, do not show this chart
            if(_keys.length === 1 && _keys[0] === 'NA'){
                continue;
            }
            
            _allNumber = allNumberElements(_keys);
           
            if(_dataType === "NUMBER" || _allNumber){
                dataType[_attr[i]["attr_id"]] = 'allnumeric';
            }else{
                dataType[_attr[i]["attr_id"]] = 'string';
            }
            
            if(_attr[i]["attr_id"] === "CASE_ID" ){
                pie.push(_attr[i]);
            }else if(_attr[i]["attr_id"] === "PATIENT_ID") {
                if(_keys.length !== Object.keys(dataArr).length) {
                    _studyDesc = "from " + _keys.length + " patients";
                }
            }else if(_dataType === "NUMBER" || _dataType === "BOOLEAN" || _allNumber){                
                if(selectedCol(_attr[i]["attr_id"])){                    
                    if(_keys.length>10 || _attr[i]["attr_id"] === 'AGE' || _attr[i]["attr_id"] === 'MUTATION_COUNT' 
                            || _attr[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS')
                        bar.push(_attr[i]);
                    else
                        pie.push(_attr[i]);
                }

                if(_keys.length > 10 || _attr[i]["attr_id"] === 'AGE' || _attr[i]["attr_id"] === 'MUTATION_COUNT' 
                        || _attr[i]["attr_id"] === 'COPY_NUMBER_ALTERATIONS'){
                    varType[_attr[i]["attr_id"]] = "bar";
                }else{
                    varType[_attr[i]["attr_id"]] = "pie";
                }
                
                if(_dataType === "NUMBER" || _allNumber){
                    var _varValues = [];
                    
                    for(var j=0;j<_arr.length;j++){
                        if(_arr[j][_attr[i]["attr_id"]] && 
                                !isNaN(_arr[j][_attr[i]["attr_id"]])){
                            _varValues.push(_arr[j][_attr[i]["attr_id"]]);  
                        }
                    }

                    distanceMinMaxArray[_attr[i]["attr_id"]] = {
                        diff : Math.max.apply( Math, _varValues ) - Math.min.apply( Math, _varValues ),
                        min: Math.min.apply( Math, _varValues ),
                        max:Math.max.apply( Math, _varValues )
                    };
                }

            }else if(_dataType === "STRING"){
                if(selectedCol(_attr[i]["attr_id"])){
                    pie.push(_attr[i]);
                }
                varType[_attr[i]["attr_id"]] = "pie";
            }else {
                StudyViewUtil.echoWarningMessg('Can not identify data type.');
                StudyViewUtil.echoWarningMessg('The data type is ' +_dataType);
            }
            varKeys[_attr[i]["attr_id"]] = [];
            varKeys[_attr[i]["attr_id"]] = _keys;
            varDisplay.push(_attr[i]["display_name"]);                
            varName.push(_attr[i]["attr_id"]);
        }
        
        $("#study-desc").append("&nbsp;&nbsp;<b>"+ Object.keys(dataArr).length +" samples " + _studyDesc+"</b>.");
        
        totalCharts = pie.length + bar.length;
    }
    
    function initSpecialCharts(_arr){
        //var _trimedData = wordCloudDataProcess(mutatedGenes);
        
        if(     (StudyViewUtil.arrayFindByValue(varName, 'OS_MONTHS') && 
                StudyViewUtil.arrayFindByValue(varName, 'OS_STATUS') &&
                varKeys['OS_MONTHS'].length > 0 &&
                varKeys['OS_STATUS'].length > 0) || 
                (StudyViewUtil.arrayFindByValue(varName, 'DFS_MONTHS') && 
                StudyViewUtil.arrayFindByValue(varName, 'DFS_STATUS') &&
                varKeys['DFS_MONTHS'].length > 0 &&
                varKeys['DFS_STATUS'].length > 0)){
            
            initSurvivalPlot(_arr);
        }
        
        if(
                StudyViewUtil.arrayFindByValue(varName, 'MUTATION_COUNT') && 
                StudyViewUtil.arrayFindByValue(varName, 'COPY_NUMBER_ALTERATIONS') &&
                varKeys['MUTATION_COUNT'].length > 0 &&
                varKeys['COPY_NUMBER_ALTERATIONS'].length > 0){
            initScatterPlot(_arr);
        }
        
        /*
        if(!( 
                _trimedData.names.length === 1 && 
                _trimedData.names[0] === 'No Mutated Gene')){
        
            initWordCloud(_trimedData);
        }*/
    }
    function redrawSurvival() {
        var _unselectedCases= [],
            _selectedCases = getSelectedCasesID(),
            _allCases = StudyViewParams.params.caseIds;
        
        var _passedCases = [];
        var _selectedCasesLength = _selectedCases.length,
            _allCasesLength = _allCases.length;
        
        if(_allCasesLength > _selectedCasesLength){
            for(var i = 0; i < _allCasesLength; i++){
                if(_selectedCases.indexOf(_allCases[i]) === -1){
                    _unselectedCases.push(_allCases[i]);
                }
            }
            _passedCases = {
                'Selected cases': {
                    caseIds: _selectedCases,
                    color: "#dc3912"
                },
                'Unselected cases': {
                    caseIds: _unselectedCases,
                    color: "#2986e2"
                }
                //,ALL_CASES: _allCases
            };
        }else{
            _passedCases = {
                'All cases': {
                    caseIds: _allCases,
                    color: "#2986e2"
                }
            };
        }
        StudyViewSurvivalPlotView.redraw(_passedCases, false);
    }
    function redrawWordCloud(){
        var _selectedCases = getSelectedCases(),
        _selectedCasesLength = _selectedCases.length,
        _selectedGeneMutatedInfo = [],
        _filteredMutatedGenes = {},
        _selectedCasesIds = [];
        
        if(_selectedCasesLength !== 0){
            for( var i = 0; i < _selectedCasesLength; i++){
                _selectedCasesIds.push(_selectedCases[i].CASE_ID);
            }

            var mutatedGenesObject = {
                cmd: 'get_smg',
                case_list: _selectedCasesIds.join(' '),
                mutation_profile: StudyViewParams.params.mutationProfileId
            };

            $.when($.ajax({type: "POST", url: "mutations.json", data: mutatedGenesObject}))
            .done(function(a1){
                var i, dataLength = a1.length;

                for( i = 0; i < dataLength; i++){
                    _selectedGeneMutatedInfo.push(a1[i]);
                }
                _filteredMutatedGenes = wordCloudDataProcess(_selectedGeneMutatedInfo);
                StudyViewInitWordCloud.redraw(_filteredMutatedGenes);
                callBackFunctions();
                $("#study-view-word-cloud-loader").css('display', 'none');
            });
        }else{
            _filteredMutatedGenes = wordCloudDataProcess([]);
            StudyViewInitWordCloud.redraw(_filteredMutatedGenes);
            $("#study-view-word-cloud-loader").css('display', 'none');
        }     
    }
    
    //This function defined all of callback functions.
    function callBackFunctions(){
        if(StudyViewSurvivalPlotView.getInitStatus()){
            redrawSurvival();
        }
    }
    
    //Only return top 10 of maximum number of mutations gene
    function wordCloudDataProcess(_data) {
        /*This data format is:
        * cytoband: _value(string)
        * gene_symbol: _value(string)
        * length: _value(number)
        * num_muts: _value(number)
        */
        var i,
            //Use array instead of object, array has sorting function
            _mutatedGenes = [],
            _mutatedGenesLength,
            _topGenes = {},
            _dataLength = _data.length;
        for( i = 0; i < _dataLength; i++){
            if( _data[i].length > 0 ){
                var _numOfMutationsPerNucletide = 
                        Number(_data[i].num_muts) / Number(_data[i].length);
                
                _mutatedGenes.push([_data[i].gene_symbol, 
                                    _numOfMutationsPerNucletide]);
            }
        }
        if(_mutatedGenes.length !== 0){
            _mutatedGenes.sort(sortNumMuts);
        }
        
        if(_dataLength < 10){
            _mutatedGenesLength = _dataLength;
        }else{
            _mutatedGenesLength = 10;
        }
        
        if(_mutatedGenesLength === 0){
            _topGenes.names = ['No Mutated Gene'];
            _topGenes.size = [15];
        }else{
            _topGenes.names = [];
            _topGenes.size = [];

            for( i = 0; i< _mutatedGenesLength; i++){
                _topGenes.names.push(_mutatedGenes[i][0]);
                _topGenes.size.push(_mutatedGenes[i][1]);
            }
            _topGenes.size = calculateWordSize(_topGenes.size);
        }
        
        return _topGenes;
    }
    
    function calculateWordSize(_genes) {
        var i,
            _geneLength = _genes.length,
            _percental = [],
            _totalMuts = 0;
        
        for( i = 0; i < _geneLength; i++){
            _totalMuts += _genes[i];
        }
        
        for( i = 0; i < _geneLength; i++){
            var _size = (_genes[i] / _totalMuts) * WORDCLOUDTEXTSIZECONSTANT;
            if(_size > 40){
                _size = 40;
            }
            _percental.push(_size);
        }
        
        return _percental;
    }
    
    function sortNumMuts(a, b) {
        a = a[1];
        b = b[1];
        
        if (a < b)
            return 1;
        if (a > b)
            return -1;
        return 0;
    }
    
    function createLayout() {
        var container = document.querySelector('#study-view-charts');
        msnry = new Packery( container, {
            columnWidth: 190,
            rowHeight: 170,
            itemSelector: '.study-view-dc-chart',
            gutter:5
        });
        bondDragForLayout();
    }
    
    function bondDragForLayout(){
        var itemElems = msnry.getItemElements(),
            itemElemsLength = itemElems.length;
        // for each item...
        for ( var i=0, len = itemElemsLength; i < len; i++ ) {
            var elem = itemElems[i];
            // make element draggable with Draggabilly
            var draggie = new Draggabilly( elem, {
                handle: '.study-view-drag-icon'
            });
            
            //Set selected chart z-index bigger than others
            draggie.on( 'dragStart', function(instance, event, pointer){
                var _itemElems = msnry.getItemElements(),
                    _itemElemsLength = _itemElems.length;
                    
                for(var j=0; j< _itemElemsLength; j++){
                    if( instance.element.id === _itemElems[j].id){
                        $("#" + _itemElems[j].id).css('z-index','20');
                    }else{
                        $("#" + _itemElems[j].id).css('z-index','1');
                    }
                }
            });
            
            //Remove z-index of all charts
            draggie.on( 'dragEnd', function(instance, event, pointer){
                var _itemElems = msnry.getItemElements(),
                    _itemElemsLength = _itemElems.length;
                
                for(var j=0; j< _itemElemsLength; j++){
                    $("#" + _itemElems[j].id).css('z-index','1');
                }
                
//                //if label of survival opened, close it in here
//                StudyViewSurvivalPlotView.detectLabelPosition();
                
                //Detect Scatter Plot
                if($("#study-view-scatter-plot-side").css('display') === 'block'){
                    StudyViewUtil.changePosition(
                            '#study-view-scatter-plot',
                            '#study-view-scatter-plot-side',
                            "#dc-plots");
                }
            });
            
            // bind Draggabilly events to Packery
            msnry.bindDraggabillyEvents( draggie );
        }
        msnry.layout();
    }
    
    function initWordCloud(_data) {
        StudyViewInitWordCloud.init(_data);
        $(".study-view-word-cloud-delete").unbind('click');
        $(".study-view-word-cloud-delete").click(function (){
            $("#study-view-word-cloud").css('display','none');
            $('#study-view-add-chart').css('display','block');
//            $('#study-view-add-chart ul')
//                    .append($('<li></li>')
//                        .attr('id','wordCloud')
//                        .text('Word Cloud'));
//            $('#study-view-add-chart ul').stop().hide();
            $('#study-view-add-chart')
                    .append($('<option></option>')
                        .attr('id','wordCloud')
                        .text('Word Cloud'));
            bondDragForLayout();
            AddCharts.bindliClickFunc();
        });
    }
    
    function initSurvivalPlot(_data) {
        var _plotsInfo = {};
            
        if (StudyViewUtil.arrayFindByValue(varName, 'OS_MONTHS') && 
                StudyViewUtil.arrayFindByValue(varName, 'OS_STATUS') &&
                varKeys['OS_MONTHS'].length > 0 &&
                varKeys['OS_STATUS'].length > 0) {
            _plotsInfo.OS=  {
                name: "Overall Survival",
                property: ["OS_MONTHS", "OS_STATUS"],
                status: [["LIVING"], ["DECEASED"]],
                caseLists: {
                    'All cases': {
                        caseIds: StudyViewParams.params.caseIds, 
                        color: '#2986e2'
                    }
                }
            };
        }
        
        if (StudyViewUtil.arrayFindByValue(varName, 'DFS_MONTHS') && 
                StudyViewUtil.arrayFindByValue(varName, 'DFS_STATUS') &&
                varKeys['DFS_MONTHS'].length > 0 &&
                varKeys['DFS_STATUS'].length > 0) {
            
            _plotsInfo.DFS=  {
                name: "Disease Free Survival",
                property: ["DFS_MONTHS", "DFS_STATUS"],
                status: [["DISEASEFREE"], ["RECURRED", "RECURRED/PROGRESSED", "PROGRESSED"]],
                caseLists: {
                    'All cases': {
                        caseIds: StudyViewParams.params.caseIds, 
                        color: '#2986e2'
                    }
                }
            };
        }
        
        StudyViewSurvivalPlotView.init(_plotsInfo, _data);

        $(".study-view-survival-plot-delete").click(function (){
            var _plotDiv = $(this).parent().parent().parent(),
                _plotIdArray = _plotDiv.attr('id').split("-"),
                _plotId = _plotIdArray[_plotIdArray.length - 1],
                _title = $(this).parent().parent().find("charttitleh4").text();
           
            $($(this).parent().parent().parent()).css('display','none');
            $('#study-view-add-chart').css('display','block');
//            $('#study-view-add-chart ul')
//                    .append($('<li></li>')
//                        .attr('id','survival-' + _plotId)
//                        .text(_title));
//            
//            $('#study-view-add-chart ul').stop().hide();
//            $('#study-view-add-chart ul').css('height','100%');
            $('#study-view-add-chart')
                    .append($('<option></option>')
                        .attr('id','survival-' + _plotId)
                        .text(_title));
            bondDragForLayout();
            AddCharts.bindliClickFunc();
        });
    }
    
    function initScatterPlot(_arr) {
        var _attr = {};
            
        _attr.min_x = distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].min;
        _attr.max_x = distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].max;
        _attr.min_y = distanceMinMaxArray['MUTATION_COUNT'].min;
        _attr.max_y = distanceMinMaxArray['MUTATION_COUNT'].max;
        
        StudyViewInitScatterPlot.init(_arr, _attr);

        $(".study-view-scatter-plot-delete").unbind('click');
        $(".study-view-scatter-plot-delete").click(function (){
            $("#study-view-scatter-plot").css('display','none');
            $('#study-view-add-chart').css('display','block');
//            $('#study-view-add-chart ul')
//                    .append($('<li></li>')
//                        .attr('id','mutationCNA')
//                        .text('Number of Mutation vs Fraction of copy number altered genome'));
//                
//            $('#study-view-add-chart ul').stop().hide();
//            $('#study-view-add-chart ul').css('height','100%');
            $('#study-view-add-chart')
                    .append($('<option></option>')
                        .attr('id','mutationCNA')
                        .text('Number of Mutation vs Fraction of copy number altered genome'));
            bondDragForLayout();
            StudyViewInitScatterPlot.setClickedCasesId('');
            StudyViewInitScatterPlot.setBrushedCaseId([]);
            StudyViewInitScatterPlot.setShiftClickedCasesId([]);
            AddCharts.bindliClickFunc();
            removeMarker();
            redrawChartsAfterDeletion();
        });
    }
    
    function initCharts(_data) { 
        $("#study-view-charts").html("");
        initSpecialCharts(_data.arr);
        initDcCharts(_data);
    }
    
    function initDcCharts() {
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
            StudyViewUtil.echoWarningMessg('the number of created charts not equal to number of totalCharts. --1');
            return false;
        }
        
        dc.renderAll();
        
        $('.study-view-dc-chart-delete').unbind('click');
        $('.study-view-dc-chart-delete').click(function(event){
                var _id = $(this).attr("chartID");
                var _valueA = $("#study-view-dc-chart-" + _id).attr('oValue').split(',');
                
                deleteChart(_id,_valueA);
                bondDragForLayout();
                AddCharts.bindliClickFunc();
        });
    }
    
    function getSelectedCases() {
        return varChart[attrNameMapUID["CASE_ID"]]
                    .getChart()
                    .dimension()
                    .top(Infinity);
    }
    function getSelectedCasesID() {
        var _cases = varChart[attrNameMapUID["CASE_ID"]]
                    .getChart()
                    .dimension()
                    .top(Infinity);
        var _casesLength = _cases.length;
        var _casesID = [];
        
        for(var i = 0; i < _casesLength; i++){
            _casesID.push(_cases[i].CASE_ID);
        }
        
        return _casesID;
    }
    
    function redrawScatter(_casesInfo, _selectedAttr) {
        if(StudyViewInitScatterPlot.getInitStatus()) {
            if(typeof _casesInfo !== "undefined" && typeof _selectedAttr !== "undefined"){
                StudyViewInitScatterPlot.redrawByAttribute(_casesInfo, _selectedAttr);
            }else{
                if(dcHasFilters()){
                    StudyViewInitScatterPlot.redraw(getSelectedCasesID(), true);
                }else{
                    StudyViewInitScatterPlot.redraw([], false);
                }
            }
        }
    }
    
    function makeNewPieChartInstance(_chartID, _pieInfo) {
        var _params = {
                baseID: "study-view",
                chartID: _chartID,
                chartDivClass: 'study-view-pie-chart',
                attrID: _pieInfo.attr_id,
                displayName: _pieInfo.display_name,
                ndx: ndx,
                chartColors: chartColors,
                plotDataButtonFlag: false
            };
        
        if(StudyViewSurvivalPlotView.getInitStatus()) {
            
            _params.plotDataButtonFlag = true;
        }else{
            _params.plotDataButtonFlag = false;
        }
        
        varChart[_chartID] = new PieChart();
        varChart[_chartID].init(_params);
        varChart[_chartID].postFilterCallbackFunc(postFilterCallbackFunc);
        varChart[_chartID].postRedrawCallbackFunc(postRedrawCallbackFunc);
        varChart[_chartID].plotDataCallbackFunc(plotDataCallbackFunc);
    }
    
    function redrawWSCharts() {
        removeContentsAndStartLoading();
        changeHeader();
        
        if(StudyViewInitWordCloud.getInitStatus()){
            //redrawSurvival has been added in redrawWordCloud as callback func
            redrawWordCloud();
        }else if(StudyViewSurvivalPlotView.getInitStatus()){
            var _length = StudyViewSurvivalPlotView.getNumOfPlots();
            
            for(var i = 0; i < _length; i++){
                $("#study-view-survival-plot-body-" + i).css('opacity', '0.3');
                $("#study-view-survival-plot-loader-" + i).css('display', 'block');
            }
            
            //The timeout is set equal to the transition duration of dc charts.
            setTimeout(function() {
                redrawSurvival();
            }, StudyViewParams.summaryParams.transitionDuration);
        }
    }
    
    
    function redrawSpecialPlots(_casesInfo, _selectedAttr){
        var _scatterInit = StudyViewInitScatterPlot.getInitStatus();
        var _timeout = 0;
        
        if(StudyViewSurvivalPlotView.getInitStatus()) {
            var _length = StudyViewSurvivalPlotView.getNumOfPlots();
            
            for(var i = 0; i < _length; i++){
                $("#study-view-survival-plot-body-" + i).css('opacity', '0.3');
                $("#study-view-survival-plot-loader-" + i).css('display', 'block');
            }
            _timeout = StudyViewParams.summaryParams.transitionDuration;
        }else{
            _timeout = 0;
        }
        
        if(_scatterInit){
            $("#study-view-scatter-plot-loader").css('display', 'block');
            $("#study-view-scatter-plot-body").css('opacity', '0.3');
        }
        
        //When redraw plots, the page will be stuck before loader display, 
        //so we need to set timeout for displaying loader.
        //The timeout is set equal to the transition duration of dc charts.
        setTimeout(function() {
            StudyViewInitScatterPlot.setClickedCasesId('');
            StudyViewInitScatterPlot.setShiftClickedCasesId(getSelectedCases());
            
            if(typeof _casesInfo !== "undefined" && typeof _selectedAttr !== "undefined"){
                StudyViewSurvivalPlotView.redraw(_casesInfo, _selectedAttr);
                changeHeader();
                redrawScatter(_casesInfo, _selectedAttr[0]);
            }else{
                redrawWSCharts();
                redrawScatter();
            }
            
            if(_scatterInit){
                $("#study-view-scatter-plot-loader").css('display', 'none');
                $("#study-view-scatter-plot-body").css('opacity', '1');
            }
        }, _timeout);
    }
    
    
    /**
     * DC charts post redraw callback function
     */
    function postRedrawCallbackFunc(){
        //If no filter exist, should reset the clear flag of Scatter Plot.
        if(!dcHasFilters()){
            StudyViewInitScatterPlot.setclearFlag(false);
        }
    }
    
    /**
     * DC charts post filter callback function
     */
    function postFilterCallbackFunc(){
        if(!StudyViewInitScatterPlot.getclearFlag() && !plotDataFlag){
            removeMarker();
            resetBars();
            redrawSpecialPlots();
        }
    }
    
    /**
     * DC charts plot data button callback function
     * @param {type} _casesInfo
     * @param {type} _selectedAttr
     */
    function plotDataCallbackFunc(_casesInfo, _selectedAttr) {
        removeMarker();
        resetBars(_selectedAttr[0]);
        redrawSpecialPlots(_casesInfo, _selectedAttr);
    }
    
    /**
     * 
     * @returns {Boolean} whether current dc charts have filter
     */
    function dcHasFilters() {
        var _dcLength = varChart.length,
            _hasFilters = false;
        for(var i = 0; i< _dcLength; i++) {
            if(varChart[i] !== '' && varChart[i].getChart().hasFilter()){
                _hasFilters = true;
                break;
            }
        }
        return _hasFilters;
    }
    
    function resetBars(_exceptionAttr) {
        var _attrIds = [],
            _attrIdsLength = 0
    
        for( var _key in varType) {
            if(varType[_key] === 'bar'){
                if(typeof _exceptionAttr !== 'undefined'){
                    if(_key !== _exceptionAttr){
                        _attrIds.push(attrNameMapUID[_key]);
                    }
                }else {
                    _attrIds.push(attrNameMapUID[_key]);
                }
            }
        }
        
        _attrIdsLength = _attrIds.length;
        
        for( var i = 0; i < _attrIdsLength; i++) {
            for( var _key in HTMLtagsMapUID){
                if( HTMLtagsMapUID[_key] === _attrIds[i]){
                    var _bars = $("#" + _key + " g.chart-body").find("rect"),
                        _barsLength = _bars.length;
                
                    for ( var j = 0; j < _barsLength; j++) {
                        var _bar = $(_bars[j]);
                        if(!_bar.hasClass('deselected')){
                            _bar.attr('fill', barOriginalColor);
                        }
                    }
                    break;
                }
            }
        }
    }
    
    function removeContentsAndStartLoading(){
        $("#study-view-word-cloud svg").remove();
        $("#study-view-word-cloud-loader").css('display', 'block');
        if(StudyViewSurvivalPlotView.getInitStatus()) {
            var _length = StudyViewSurvivalPlotView.getNumOfPlots();
            
            for(var i = 0; i < _length; i++){
                $("#study-view-survival-plot-body-" + i).css('opacity', '0.3');
                $("#study-view-survival-plot-loader-" + i).css('display', 'block');
            }
        }
    }
    
    function makeNewBarChartInstance(_chartID, _barInfo, _distanceArray) {
        var _params = {
                baseID: "study-view",
                chartID: _chartID,
                chartDivClass: 'study-view-bar-chart',
                attrID: _barInfo.attr_id,
                displayName: _barInfo.display_name,
                ndx: ndx,
                needLogScale: false,
                distanceArray: _distanceArray,
                plotDataButtonFlag: true,
                timeoutFlag: true
            };
        
        if(_distanceArray.diff > 1000 && _distanceArray.min >= 1){
            _params.needLogScale = true;
        }else{
            _params.needLogScale = false;
        }
        
        if(StudyViewSurvivalPlotView.getInitStatus()) {
            
            _params.plotDataButtonFlag = true;
        }else{
            _params.plotDataButtonFlag = false;
        }
        
        varChart[_chartID] = new BarChart();
        varChart[_chartID].init(_params);
        varChart[_chartID].postFilterCallbackFunc(postFilterCallbackFunc);
        varChart[_chartID].postRedrawCallbackFunc(postRedrawCallbackFunc);
        varChart[_chartID].plotDataCallbackFunc(plotDataCallbackFunc);
        
        if(_distanceArray.diff > 1000 && _distanceArray.min >= 1){
            $("#scale-input-"+_chartID).change(function(e) {
                $(this).parent().parent().find('svg').remove();
                var _param = {},
                    _idArray = $(this).attr('id').split('-'),
                    _currentID = _idArray[2],
                    _currentChart = varChart[_currentID].getChart();

                if(_currentChart.hasFilter()){
                    _currentChart.filterAll();
                    dc.redrawAll();
                }
                dc.deregisterChart(_currentChart);

                if($(this).prop('checked')){
                    _param.needLogScale = true;
                }else{
                    _param.needLogScale = false;
                }
                varChart[_currentID].updateParam(_param);
                varChart[_currentID].reDrawChart();
                varChart[_currentID].getChart().render();
                redrawSpecialPlots();
            });
        }
    }
    
    function deleteChart(_chartID,_value){
        var _options;
        
        $("div").remove("#study-view-dc-chart-main-" + _chartID); 
        if(varChart[_chartID].getChart().hasFilter()){
            varChart[_chartID].getChart().filterAll();
            dc.redrawAll();
            redrawSpecialPlots();
        }
        dc.deregisterChart(varChart[_chartID].getChart());
//        $('#study-view-add-chart ul')
//                .append($('<li></li>').attr('id',_value[0]).text(_value[1]));
//        
//        $('#study-view-add-chart ul').stop().hide();
//        $('#study-view-add-chart ul').css('height','100%');
        $('#study-view-add-chart')
                .append($('<option></option>').attr('id',_value[0]).text(_value[1]));     
        
        _options = $('#study-view-add-chart').find('option:not(:first)');
        
        _options.sort(function(a, b) {
            var _aValue = a.text.toUpperCase();
            var _bValue = b.text.toUpperCase();
            
            return _aValue.localeCompare(_bValue);
        });
        $('#study-view-add-chart').find('option:not(:first)').remove();
        $('#study-view-add-chart').append(_options);
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
            StudyViewInitScatterPlot.setClickedCasesId(_selectedRowCaseId);
            removeMarker();
            //redrawChartsAfterDeletion();
            if(!_deSelect){
                getDataAndDrawMarker(_selectedRowCaseId);
            }
        };
        
        var _dataTableRowShiftClickCallback = function(_selectedRowCaseId) {
            StudyViewInitScatterPlot.setShiftClickedCasesId(_selectedRowCaseId);
            StudyViewInitScatterPlot.setClickedCasesId('');
            removeMarker();
            filterChartsByGivingIDs(_selectedRowCaseId);
        };
        
        var _dataTable = StudyViewInitDataTable.getDataTable();
        _dataTable.rowClickCallback(_dataTableRowClickCallback);
        _dataTable.rowShiftClickCallback(_dataTableRowShiftClickCallback);
    }
    
    //This filter is the same one which used in previous Google Charts Version,
    //should be revised later.
    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*class.*)|(.*type.*)|(.*site.*)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }
    
    function redrawChartsAfterDeletion(){
        for(var i = 0; i < varChart.length; i++){
            if(removedChart.indexOf(i) === -1){
                if(varChart[i].getChart().filters().length > 0)
                    varChart[i].getChart().filterAll();
            }
        }
        dc.redrawAll();
        redrawSpecialPlots();
    }
    
    function getDataAndDrawMarker(_clickedCaseIds) {
        if(_clickedCaseIds.length > 0) {
            var _numOfCharts = varChart.length;
            
            for(var i=0; i< _numOfCharts; i++){
                if(removedChart.indexOf(i) === -1){
                    if(attrNameMapUID['CASE_ID'] !== i){
                        var _datum = $("#study-view-dc-chart-" + i),
                            _value = _datum.attr('oValue'),
                            _valueArray = _value.split(",");
                    
                        if(_valueArray[2] === 'pie'){
                            var _relativeValue = dataArr[_clickedCaseIds[0]][_valueArray[0]],
                                _gArray = _datum.find('svg g g'),
                                _gArrayLength = _gArray.length;
                        
                            for ( var j = 0; j < _gArrayLength; j++) {
                                var _labelText = $(_gArray[j]).find('title').text(),
                                    _key = _labelText.substring(0, _labelText.lastIndexOf(":"));
                                if(_key === _relativeValue){
                                    varChart[i].drawMarker(j+1,i);
                                }
                            }
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
            if(varChart[i] !== ''){
                varChart[i].removeMarker();
            }
        }
    }
    
    function getRefererCaseId() {
        var _idStr = /^#?case_ids=(.+)/.exec(location.hash);
       
        if (!_idStr) return null;
        
        return _idStr[1].split(/[ ,]+/);
    }
    
    //This function is designed to analysis URL. If the user input
    //the case ID in url, this function will catch all case IDs 
    //and filter charts.
    function filterCharts(){
        var _ids = getRefererCaseId();
        
        if(_ids !== null){
            filterChartsByGivingIDs(_ids);
        }
    }
    
    function filterChartsByGivingIDs(_ids){
        var _caseIDChart = varChart[attrNameMapUID['CASE_ID']].getChart();
        
        if(_ids.length > 1){
            StudyViewInitScatterPlot.setClickedCasesId('');;
        }
        _caseIDChart.filterAll();
        _caseIDChart.filter([_ids]);
        dc.redrawAll();
        
        postFilterCallbackFunc(_ids);
    }
    
    function createNewChartFromOutside(_id, _text) {
        var _selectedChartType,
            _index,
            _chartType = [],
            _selectedAttr = _id,
            _selectedAttrDisplay = _text,
            _chartID = -1,
            _createdFlag = true;
    
        if(_id === 'mutationCNA'){
            _chartType = ['scatter'];
        }else if(_id === 'wordCloud'){
            _chartType = ['wordCloud'];
        }else if(_id.indexOf('survival') !== -1){
            _chartType = [_id];
        }else{
            _chartType = varType[_id].split(',');
        }

        _selectedChartType = _chartType[0];
        
        if(_selectedAttr==='mutationCNA'){
            $("#study-view-scatter-plot").css('display','block');
        }else if(_selectedAttr==='wordCloud'){
            $("#study-view-word-cloud").css('display','block');
        }else if(_selectedAttr.indexOf('survival') !== -1){
            var tmp = _selectedAttr.split("-"),
                _index = tmp[tmp.length - 1];
            
            $("#study-view-survival-plot-" + _index).css('display','block');
        }else{
            if(totalCharts < 31) {
                if(Object.keys(attrNameMapUID).indexOf(_selectedAttr) !== -1){
                    _chartID = attrNameMapUID[_selectedAttr];
                }else{
                    _chartID = totalCharts;
                    HTMLtagsMapUID["study-view-dc-chart-" + totalCharts] = totalCharts;
                    attrNameMapUID[_selectedAttr] = totalCharts;
                    totalCharts++;       
                }

                if(_selectedChartType === 'pie'){
                    makeNewPieChartInstance(_chartID, 
                                            {attr_id:_selectedAttr,
                                                display_name:_selectedAttrDisplay});
                }else{
                    makeNewBarChartInstance(_chartID,
                                            {attr_id:_selectedAttr,
                                                display_name:_selectedAttrDisplay},
                                            distanceMinMaxArray[_selectedAttr]);
                }


                msnry.destroy();
                msnry = new Packery( document.querySelector('#study-view-charts'), {
                    columnWidth: 190,
                    rowHeight: 170,
                    itemSelector: '.study-view-dc-chart',
                    gutter:5
                });

                varChart[_chartID].getChart().render();
                
                $('#study-view-dc-chart-'+ _chartID +'-header .study-view-dc-chart-delete').unbind('click');
                $('#study-view-dc-chart-'+ _chartID +'-header .study-view-dc-chart-delete').click(function(event){
                    var _ID = _chartID;
                    var valueA = $("#study-view-dc-chart-" + _ID).attr("oValue").split(',');
//                    var valueA = $(this).parent().parent().parent().find().attr("oValue").split(',');
                    deleteChart(_chartID,valueA);
                    AddCharts.bindliClickFunc();
                    bondDragForLayout();
                });
            }else{
                alert("Can not create more than 30 plots.");
                _createdFlag = false;
            }
        }
        
        if(_createdFlag) {
            _index = removedChart.indexOf(_chartID);
            if (_index > -1) {
                removedChart.splice(_index, 1);
            }

            bondDragForLayout();

//            $('#study-view-add-chart ul').find('li[id="' + _selectedAttr + '"]').remove();
            $('#study-view-add-chart').find('option[id="' + _selectedAttr + '"]').remove();
//            if($('#study-view-add-chart ul').find('li').length === 0 ){
            if($('#study-view-add-chart').find('option').length === 1 && 
                    $('#study-view-add-chart').find('option').attr('id') === ''){
                $('#study-view-add-chart').css('display','none');
            }
            
//            $('#study-view-add-chart ul').css('height','100%');
        }
    }
    
    return {
        init: function(_data) {
            initData(_data);
            initCharts(_data);
            createLayout();
//            updateDataTableCallbackFuncs();
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
            _param.type = varType;
            
            return _param;
        },
        
        getCharts: function() {
            return varChart;
        },
        
        getChartsByID: function(_index) {
            return varChart[_index];
        },
        
        getCaseIdChartIndex: function() {
            return attrNameMapUID['CASE_ID'];
        },
        
        getPlotDataFlag: function() {
            return plotDataFlag;
        },
        
        setPlotDataFlag: function(_flag) {
            plotDataFlag = _flag;
        },
        
        redrawScatter: redrawScatter,
        redrawSpecialPlots: redrawSpecialPlots,
        filterChartsByGivingIDs: filterChartsByGivingIDs,
        changeHeader: changeHeader,
        createNewChart: createNewChartFromOutside,
        getDataAndDrawMarker: getDataAndDrawMarker,
        removeMarker: removeMarker,
        redrawWSCharts: redrawWSCharts,
        resetBars: resetBars,
        getSelectedCasesID: getSelectedCasesID,
        getLayout: function() {
            return msnry;
        }
    };
})();
