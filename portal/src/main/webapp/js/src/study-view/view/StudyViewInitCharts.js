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
 * @interface: getScatterPlotInitValue -- return the max and min value of
 *                                        'COPY_NUMBER_ALTERATIONS' and
 *                                        'MUTATION_COUNT'.
 * @interface: getCharts -- return all DC charts
 * 
 * Following interfaces will call StudyViewInitCharts functions directly.
 * @interface: filterChartsByGivingIDs 
 * @interface: changeHeader 
 * @interface: scatterPlotBrushCallBack
 * @interface: scatterPlotClickCallBack
 * @interface: createNewChart
 * 
 * 
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */


var StudyViewInitCharts = (function(){
    

    var numOfCases,
        ndx, //Crossfilter dimension
        msnry,
        totalCharts,
        mutatedGenes = [],
        clickedCaseId = '',
        dataArr = {},
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
        
        WORDCLOUDTEXTSIZECONSTANT = 200,
        
        // Color scale from GOOGLE charts
        chartColors = jQuery.extend(true, [], StudyViewBoilerplate.chartColors), 
        parObject = {
            studyId: "",
            caseIds: "",
            cnaProfileId: "",
            mutationProfileId: "",
            caseSetId: ""
        };
    
    function initParameters(o) {
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
        parObject.caseSetId = o.caseSetId;
    }
    
    function initData(dataObtained) {
        var _keys = [],
            _attr = dataObtained.attr,
            _arr = dataObtained.arr,
            _attrLength = _attr.length,
            _arrLength = _arr.length;
        
        mutatedGenes = dataObtained.mutatedGenes;   
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
                console.log("%c Error: Can not identify data type.", "color:red");
            }

            varDisplay.push(_attr[i]["display_name"]);                
            varName.push(_attr[i]["attr_id"]);
        }
        
        totalCharts = pie.length + bar.length;
        initScatterPlot(_arr);
        var _trimedData = wordCloudDataProcess(mutatedGenes);
        initWordCloud(_trimedData);
    }
    
    function redrawWordCloud(){
        var _selectedCases = varChart[attrNameMapUID["CASE_ID"]]
                    .getChart()
                    .dimension()
                    .top(Infinity),
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
                mutation_profile: parObject.mutationProfileId
            };

            $.when($.ajax({type: "POST", url: "mutations.json", data: mutatedGenesObject}))
            .done(function(a1){
                var i, dataLength = a1.length;

                for( i = 0; i < dataLength; i++){
                    _selectedGeneMutatedInfo.push(a1[i]);
                }
                _filteredMutatedGenes = wordCloudDataProcess(_selectedGeneMutatedInfo);
                StudyViewInitWordCloud.redraw(_filteredMutatedGenes);
            });
        }else{
            _filteredMutatedGenes = wordCloudDataProcess([]);
            StudyViewInitWordCloud.redraw(_filteredMutatedGenes);
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
            rowHeight: 230,
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
            var draggie = new Draggabilly( elem );
            
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
                    $("#" + _itemElems[j].id).css('z-index','');
                }
            });
            
            // bind Draggabilly events to Packery
            msnry.bindDraggabillyEvents( draggie );
        }
        msnry.layout();
    }
    function initWordCloud(_data) {
        StudyViewInitWordCloud.init(parObject, _data);
        $(".study-view-word-cloud-delete").unbind('click');
        $(".study-view-word-cloud-delete").click(function (){
            $("#study-view-word-cloud").css('display','none');
            $('#study-view-add-chart').css('display','block');
            $('#study-view-add-chart ul')
                    .append($('<li></li>')
                        .attr('id','wordCloud')
                        .text('Word Cloud'));
            
            bondDragForLayout();
            AddCharts.bindliClickFunc();
        });
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
            
            bondDragForLayout();
            clickedCaseId = '',
            brushedCaseIds = [];
            shiftClickedCaseIds = [];
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
            console.log("$c Error: the number of created charts not equal to " +"\
                number of totalCharts. --1", "color:red");
            return false;
        }
        
        dc.renderAll();
        
        $('.study-view-dc-chart-delete').unbind('click');
        $('.study-view-dc-chart-delete').click(function(event){
                var _id = $(this).parent().parent().attr("id").split("-");
                var _valueA = $(this).parent().parent().attr('value').split(',');
                
                deleteChart(_id[_id.length-1],_valueA);
                bondDragForLayout();
                AddCharts.bindliClickFunc();
        });
    }
    
    function getSelectedCasesAndRedrawScatterPlot(_currentPieFilters) {
        var _tmpResult = varChart[attrNameMapUID["CASE_ID"]]
                    .getChart()
                    .dimension()
                    .top(Infinity),
        _tmpCaseID = [];

        clickedCaseId = '';

        if(StudyViewInitScatterPlot.getScatterPlot()){
            for(var i=0; i<_tmpResult.length ; i++){
                _tmpCaseID.push(_tmpResult[i].CASE_ID);
            }
            setScatterPlotStyle(_tmpCaseID,_currentPieFilters);
        }
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

        var _piechartCallbackFunction = getSelectedCasesAndRedrawScatterPlot;
        
        varChart[_chartID] = new PieChart();
        varChart[_chartID].init(_param);
        varChart[_chartID].scatterPlotCallbackFunction(_piechartCallbackFunction);
        varChart[_chartID].postFilterCallbackFunc(postFilterCallbackFunc);
    }
    
    function postFilterCallbackFunc(){
        changeHeader();
        redrawWordCloud();
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
            
        var _barchartCallbackFunction = getSelectedCasesAndRedrawScatterPlot;
        
        if(_distanceArray.diff > 1000){
            param.needLogScale = true;
        }else{
            param.needLogScale = false;
        }
        
        varChart[_chartID] = new BarChart();
        varChart[_chartID].init(param);
        varChart[_chartID].scatterPlotCallbackFunction(_barchartCallbackFunction);
        varChart[_chartID].postFilterCallbackFunc(postFilterCallbackFunc);

        if(_distanceArray.diff > 1000){
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
        getSelectedCasesAndRedrawScatterPlot([]);
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
        
        var _dataTable = StudyViewInitDataTable.getDataTable();
        _dataTable.rowClickCallback(_dataTableRowClickCallback);
        _dataTable.rowShiftClickCallback(_dataTableRowShiftClickCallback);
    }
    
    //This filter is the same one which used in previous Google Charts Version,
    //should be revised later.
    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }
    
    function setScatterPlotStyle(_selectedCaseID,_filters){
        var _style = [],
            _scatterPlot = StudyViewInitScatterPlot.getScatterPlot();
        
        if(_scatterPlot){
            for(var i=0 ; i< parObject.caseIds.length ; i++){
                var styleDatum = {};

                styleDatum.case_id = parObject.caseIds[i];
                if(_selectedCaseID.length !== parObject.caseIds.length){
                    if(_selectedCaseID.indexOf(parObject.caseIds[i]) !== -1){
                        if(clickedCaseId !== ''){
                            styleDatum.fill = '#2986e2';
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
                        styleDatum.fill = '#2986e2';
                        styleDatum.stroke = '#2986e2';
                        styleDatum.strokeWidth = '0';
                        styleDatum.size = '60';
                    }
                }else if(_filters === null || _filters.length === 0 ){
                    styleDatum.fill = '#2986e2';
                    styleDatum.stroke = '#2986e2';
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
        var _numOfCharts = varChart.length;
        
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
        
        
        if(_brushedCaseIds.length > 0){
            for(var i=0; i< _numOfCharts ; i++){
                if(varChart[i] !== ''){
                    if(varChart[i].getChart().filters().length > 0)
                        varChart[i].getChart().filter(null);
                }
            }
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
                if(varChart[i] !== ''){
                    if(varChart[i].getChart().filters().length > 0)
                        varChart[i].getChart().filterAll();
                }
            }
            dc.redrawAll();
        }
        changeHeader();
        redrawWordCloud();
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
                        var _datum = $("#study-view-dc-chart-" + i),
                            _value = _datum.attr('value'),
                            _valueArray = _value.split(",");
                    
                        if(_valueArray[2] === 'pie'){
                            var _relativeValue = dataArr[_clickedCaseIds[0]][_valueArray[0]],
                                _gArray = _datum.find('svg g g');
                            $.each(_gArray, function(key,value){
                                var _title = $(this).find('title').text(),
                                    _titleArray = _title.split(":"),
                                    _key = _titleArray[0];
                                
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
            clickedCaseId = '';
        }
        _caseIDChart.filterAll();
        _caseIDChart.filter([_ids]);
        dc.redrawAll();
        setScatterPlotStyle(_ids,_caseIDChart.filters());
        changeHeader();
        redrawWordCloud();
    }
    
    function createNewChartFromOutside(_id, _text) {
        var _selectedChartType,
            _index,
            _chartType = [],
            _selectedAttr = _id,
            _selectedAttrDisplay = _text,
            _chartID = -1;

        if(_id === 'mutationCNA'){
            _chartType = ['scatter'];
        }else if(_id === 'wordCloud'){
            _chartType = ['wordCloud'];
        }else{
            _chartType = varType[_id].split(',');
        }

        _selectedChartType = _chartType[0];

        if(_selectedAttr==='mutationCNA'){
            $("#study-view-scatter-plot").css('display','block');
        }else if(_selectedAttr==='wordCloud'){
            $("#study-view-word-cloud").css('display','block');
        }else{
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
                rowHeight: 230,
                itemSelector: '.study-view-dc-chart',
                gutter:5
            });

            varChart[_chartID].getChart().render();

            $('#study-view-dc-chart-'+ _chartID +' .study-view-dc-chart-delete').unbind('click');
            $('#study-view-dc-chart-'+ _chartID +' .study-view-dc-chart-delete').click(function(event){
                var valueA = $(this).parent().parent().attr("value").split(',');
                deleteChart(_chartID,valueA);
                AddCharts.bindliClickFunc();
                bondDragForLayout();
            });
        }

        _index = removedChart.indexOf(_chartID);
        if (_index > -1) {
            removedChart.splice(_index, 1);
        }
        
        bondDragForLayout();

        $('#study-view-add-chart ul').find('li[id="' + _selectedAttr + '"]').remove();

        if($('#study-view-add-chart ul').find('li').length === 0 ){
            $('#study-view-add-chart').css('display','none');
        }
    }
    return {
        init: function(_params,_data) {
            initParameters(_params);
            initData(_data);
            initCharts(_data);
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
        
        getSelectedCasesAndRedrawScatterPlot: getSelectedCasesAndRedrawScatterPlot,
        filterChartsByGivingIDs: filterChartsByGivingIDs,
        changeHeader: changeHeader,
        scatterPlotBrushCallBack: scatterPlotBrushCallBack,
        scatterPlotClickCallBack: scatterPlotClickCallBack,
        createNewChart: createNewChartFromOutside        
    };
})();
