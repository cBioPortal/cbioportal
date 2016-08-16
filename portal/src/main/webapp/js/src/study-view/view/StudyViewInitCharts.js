/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
        //mutatedGenes = [],
        //cna = [],
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

        barOriginalColor = "#1F77B4",

        // Color scale from GOOGLE charts
        chartColors = jQuery.extend(true, [], StudyViewBoilerplate.chartColors),

        //Flag for plot data button using only. Pie/Bar chart all has postdraw
        //and postfiltered functions which will effect the survival/scatter
        //plot if click filtered chart's 'plot data' button which will clear
        //filter and redraw first then call the postredraw and postfiltered
        //functions
        plotDataFlag = false,

        tableCharts = ['CANCER_TYPE', 'CANCER_TYPE_DETAILED'],

        //table chart will always put ahead, and the higher prioirty, the bigger index(later will use array unshift for table charts)
        priorityAttrs = ['CANCER_TYPE_DETAILED', 'CANCER_TYPE', 'PATIENT_ID', 'CASE_ID', 'SEQUENCED', 'HAS_CNA_DATA', 'SAMPLE_COUNT_PATIENT'],

        //Study specific prioritise attributes
        studyPrioritiseAttrs = {
            'mskimpact': {
                high: ['DARWIN_PATIENT_AGE', 'DARWIN_VITAL_STATUS'], //High priority
                low: ['AGE','OS_STATUS'] //Low priority
            }
        };

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
        var _attr = dataObtained.attr,
            _arr = dataObtained.arr,
            _attrLength = _attr.length,
            _arrLength = _arr.length,
            _studyDesc = "",
            _highPriorityAttrs = [],
            _lowPriorityAttrs = [];

        _highPriorityAttrs = _highPriorityAttrs.concat(priorityAttrs);

        if(studyPrioritiseAttrs.hasOwnProperty(cancerStudyId)) {
            _highPriorityAttrs = studyPrioritiseAttrs[cancerStudyId].high.concat(_highPriorityAttrs);
            _lowPriorityAttrs = studyPrioritiseAttrs[cancerStudyId].low.concat(_lowPriorityAttrs);
        }
        //mutatedGenes = dataObtained.mutatedGenes;
        //cna = dataObtained.cna || '';
        numOfCases = _arr.length;
        ndx = crossfilter(_arr);

        //Save data based on CASE_ID
        for( var j = 0; j < _arrLength; j++ ){
            dataArr[_arr[j].CASE_ID] = _arr[j];
        }

        _attr.sort(function(a, b) {
            var aIndex = _highPriorityAttrs.indexOf(a.attr_id),
                bIndex = _highPriorityAttrs.indexOf(b.attr_id),
                laIndex = _lowPriorityAttrs.indexOf(a.attr_id),
                lbIndex = _lowPriorityAttrs.indexOf(b.attr_id);

            if (aIndex !== -1 && bIndex !== -1) {
                return aIndex < bIndex ? -1 : 1;
            } else if (aIndex !== -1) {
                return -1;
            } else if (bIndex !== -1) {
                return 1;
            } else if (laIndex !== -1) {
                return 1;
            } else if (lbIndex !== -1) {
                return -1;
            } else {
                if(a.numOfNoneEmpty < b.numOfNoneEmpty) {
                    return 1;
                }else {
                    return -1;
                }
            }
        });

        //Calculate the number of pie, bar charts
        //Initial varName, varType, distanceMinMaxArray, varDisplay
        for( var i = 0; i < _attrLength; i++ ){
            var _attr_id = _attr[i].attr_id;
            var _keys = _attr[i].keys;
            var _dataType = _attr[i].datatype.toUpperCase();
            var _createdChartsNum = pie.length + bar.length;

            if(_dataType === "NUMBER"){
                dataType[_attr_id] = 'allnumeric';
            }else{
                dataType[_attr_id] = 'string';
            }
            
            //hide the chart at first display if it only has one value
            if(_attr_id === "CASE_ID" && _keys.length > 1){
                pie.push(_attr[i]);
            }else if(_attr_id === "PATIENT_ID") {
                if(_keys.length !== Object.keys(dataArr).length) {
                    _studyDesc = "from " + _keys.length + " patients";
                }
            }else if(_dataType === "NUMBER"){
                if(selectedCol(_attr_id) && _createdChartsNum < 21  && _keys.length > 1){
                    bar.push(_attr[i]);
                }
                varType[_attr_id] = "bar";

                var _varValues = [];

                for(var j=0;j<_arr.length;j++){
                    if(!isNaN(_arr[j][_attr_id])){
                        _varValues.push(_arr[j][_attr_id]);
                    }
                }
                var findExtremeResult = cbio.util.findExtremes(_varValues, true);
                var calculatedMin = findExtremeResult[0];
                var calculatedMax = findExtremeResult[1];

                distanceMinMaxArray[_attr_id] = {
                    diff: calculatedMax - calculatedMin,
                    min: calculatedMin,
                    max: calculatedMax,
                    absoluteMin: Math.min.apply( Math, _varValues ),
                    absoluteMax: Math.max.apply( Math, _varValues )
                };
                
            }else if(_dataType === "STRING"){
                varType[_attr_id] = "pie";
                if(selectedCol(_attr_id) && _createdChartsNum < 21){
                    if (tableCharts.indexOf(_attr_id) !== -1) {
                        pie.unshift(_attr[i]);
                    } else if(_keys.length > 1){
                        pie.push(_attr[i]);
                    }
                }
            }else {
                StudyViewUtil.echoWarningMessg('Can not identify data type.');
                StudyViewUtil.echoWarningMessg('The data type is ' +_dataType);
            }
            if(_attr_id !== "PATIENT_ID") {
                varKeys[_attr_id] = [];
                varKeys[_attr_id] = _keys;
                varDisplay.push(_attr[i].display_name);
                varName.push(_attr[i].attr_id);
            }
        }

        $("#study-desc").append("&nbsp;&nbsp;<b>"+ Object.keys(dataArr).length +" samples " + _studyDesc+"</b>.");

        totalCharts = pie.length + bar.length;
    }

    function initSpecialCharts(_arr){
        if(cancerStudyId.indexOf("mskimpact") === -1 && cancerStudyId.indexOf("genie") === -1) {
            initSurvialPlotPrep(_arr);
        }

        if(
                StudyViewUtil.arrayFindByValue(varName, 'MUTATION_COUNT') &&
                StudyViewUtil.arrayFindByValue(varName, 'COPY_NUMBER_ALTERATIONS') &&
                varKeys.MUTATION_COUNT.length > 0 &&
                varKeys.COPY_NUMBER_ALTERATIONS.length > 0){
            initScatterPlot(_arr);

            //if(cancerStudyId.indexOf("mskimpact") !== -1){
            //    initSurvialPlotPrep(_arr);
            //}
        }

        initTables();
    }

    function initSurvialPlotPrep(_arr){
        if(     (StudyViewUtil.arrayFindByValue(varName, 'OS_MONTHS') &&
            StudyViewUtil.arrayFindByValue(varName, 'OS_STATUS') &&
            varKeys.OS_MONTHS.length > 0 &&
            varKeys.OS_STATUS.length > 0) ||
            (StudyViewUtil.arrayFindByValue(varName, 'DFS_MONTHS') &&
            StudyViewUtil.arrayFindByValue(varName, 'DFS_STATUS') &&
            varKeys.DFS_MONTHS.length > 0 &&
            varKeys.DFS_STATUS.length > 0)){

            initSurvivalPlot(_arr);
        }
    }

    function initTables() {
        var initParams = {data: {attr: [], arr: {}}, numberOfSamples: {}};

        if(hasMutation) {
            var numberOfSequencedSamples = StudyViewProxy.getSequencedSampleIds().length;
            initParams.data.attr.push({
                    name: 'mutatedGenes',
                    displayName: 'Mutated Genes (<span id="number-of-selected-sequenced-samples">'+numberOfSequencedSamples+'</span> profiled samples)'
                });
            initParams.numberOfSamples.numberOfSequencedSamples = numberOfSequencedSamples;
        }

        if(hasCNA) {
            var numberOfCnaSamples = StudyViewProxy.getCnaSampleIds().length;
            initParams.data.attr.push({
                    name: 'cna',
                    displayName: 'CNA Genes (<span id="number-of-selected-cna-samples">'+numberOfCnaSamples+'</span> profiled samples)'
                });
            initParams.numberOfSamples.numberOfCnaSamples = numberOfCnaSamples;
        }

        StudyViewInitTables.init(initParams);
    }

    function redrawSurvival() {
        var _unselectedCases= [],
            _selectedCases = getSelectedCasesID(),
            _allCases = StudyViewParams.params.sampleIds;

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
                            "#summary");
                }
            });

            // bind Draggabilly events to Packery
            msnry.bindDraggabillyEvents( draggie );
        }
        msnry.layout();
    }

    function initSurvivalPlot(_data) {
        var _plotsInfo = {};

        if (StudyViewUtil.arrayFindByValue(varName, 'OS_MONTHS') &&
                StudyViewUtil.arrayFindByValue(varName, 'OS_STATUS') &&
                varKeys.OS_MONTHS.length > 0 &&
                varKeys.OS_STATUS.length > 0) {
            _plotsInfo.OS=  {
                name: "Overall Survival",
                property: ["OS_MONTHS", "OS_STATUS"],
                status: [["LIVING"], ["DECEASED"]],
                caseLists: {
                    'All cases': {
                        caseIds: StudyViewParams.params.sampleIds,
                        color: '#2986e2'
                    }
                }
            };
        }

        if (StudyViewUtil.arrayFindByValue(varName, 'DFS_MONTHS') &&
                StudyViewUtil.arrayFindByValue(varName, 'DFS_STATUS') &&
                varKeys.DFS_MONTHS.length > 0 &&
                varKeys.DFS_STATUS.length > 0) {

            _plotsInfo.DFS=  {
                name: "Disease Free Survival",
                property: ["DFS_MONTHS", "DFS_STATUS"],
                status: [["DISEASEFREE"], ["RECURRED", "RECURRED/PROGRESSED", "PROGRESSED"]],
                caseLists: {
                    'All cases': {
                        caseIds: StudyViewParams.params.sampleIds,
                        color: '#2986e2'
                    }
                }
            };
        }

        StudyViewSurvivalPlotView.init(_plotsInfo, _data);

        if(cancerStudyId.indexOf("mskimpact") !== -1){
            var index = 0;
            for(var plot in _plotsInfo){
                $('#study-view-add-chart')
                    .append($('<option></option>')
                        .attr('id','survival-' + index)
                        .text(plot.name));
                ++index;
            }
            $('.study-view-survival-plot').css('display', 'none');
        }

        $(".study-view-survival-plot-delete").click(function (){
            var _plotDiv = $(this).parent().parent().parent(),
                _plotIdArray = _plotDiv.attr('id').split("-"),
                _plotId = _plotIdArray[_plotIdArray.length - 1],
                _title = $(this).parent().parent().find("charttitleh4").text();

            $($(this).parent().parent().parent()).css('display','none');
            $('#study_view_add_chart_chzn').css('display','inline-block');
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

        StudyViewInitScatterPlot.init(_arr);

        $(".study-view-scatter-plot-delete").unbind('click');
        $(".study-view-scatter-plot-delete").click(function (){
            // remove breadcrumbs for the chart
            BreadCrumbs.deleteBreadCrumbsByChartId("study-view-scatter-plot");

            $("#study-view-scatter-plot").css('display','none');
            $('#study_view_add_chart_chzn').css('display','block');
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
        if(cancerStudyId.indexOf("mskimpact") === -1 && cancerStudyId.indexOf("genie") === -1) {
            initSpecialCharts(_data.arr);
        }
        initDcCharts(_data);
    }

    function initDcCharts(_data) {
        var createdChartID = 0;

        tableCharts.forEach(function(e, i){
            for(var i=0; i< pie.length ; i++){
                if(pie[i].attr_id === e) {
                    makeNewPieChartInstance(createdChartID, pie[i]);
                    HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
                    attrNameMapUID[pie[i].attr_id] = createdChartID;
                    displayedID.push(pie[i].attr_id);
                    var tableIcon = $("#study-view-dc-chart-" + createdChartID + "-table-icon");
                    createdChartID++;
                    break;
                }else {
                    continue;
                }
            }
        });

        if(cancerStudyId.indexOf("mskimpact") !== -1 || cancerStudyId.indexOf("genie") !== -1) {
            initSpecialCharts(_data.arr);
        }


        for(var i=0; i< pie.length ; i++){
            if (tableCharts.indexOf(pie[i].attr_id) === -1) {
                makeNewPieChartInstance(createdChartID, pie[i]);
                HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
                attrNameMapUID[pie[i].attr_id] = createdChartID;
                displayedID.push(pie[i].attr_id);
                createdChartID++;
            }
        }

        for(var i=0; i< bar.length ; i++){
            makeNewBarChartInstance(createdChartID, bar[i], distanceMinMaxArray[bar[i].attr_id]);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[bar[i].attr_id] = createdChartID;
            displayedID.push(bar[i].attr_id);
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

                // delete histogram or pie chart breadcrumbs
                //BreadCrumbs.deleteBreadCrumbsByChartId(_id);
        });
    }

    function getSelectedCases() {
        return varChart[attrNameMapUID.CASE_ID]
                    .getChart()
                    .dimension()
                    .top(Infinity);
    }
    function getSelectedCasesID() {
        var _cases = varChart[attrNameMapUID.CASE_ID]
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

    function clearScatterPlot(){
        StudyViewInitScatterPlot.clearScatterPlot();
    }

    function makeNewPieChartInstance(_chartID, _pieInfo) {
        var _params = {
                baseID: "study-view",
                chartID: _chartID,
                chartDivClass: 'study-view-pie-chart',
                attrID: _pieInfo.attr_id,
                attrKeys: _pieInfo.keys,
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

    function redrawWSCharts(exceptionIds) {
        removeContentsAndStartLoading();
        changeHeader();

        if(StudyViewInitTables.getInitStatus()){
            var redrawService = getRedrawService(exceptionIds);
            // added showLoading functionality here to ensure it is shown on screen
            StudyViewInitTables.showLoadingIcon(redrawService);
            setTimeout(function() {
                StudyViewInitTables.redraw(redrawService);
            }, StudyViewParams.summaryParams.transitionDuration);
        }
        if(StudyViewSurvivalPlotView.getInitStatus()){
            //The timeout is set equal to the transition duration of dc charts.
            setTimeout(function() {
                redrawSurvival();
            }, StudyViewParams.summaryParams.transitionDuration);
        }
    }

    function getRedrawService(exceptionIds){
        var _selectedCases = getSelectedCases().map(function(e){
            return e.CASE_ID;
        }).sort();

        var redrawService = {
            selectedCases: _selectedCases,
            exceptionIds: _.isUndefined(exceptionIds)?[]:exceptionIds
        };
        return redrawService;
    }

    function redrawSpecialPlots(_casesInfo, _selectedAttr){
        var _scatterInit = StudyViewInitScatterPlot.getInitStatus();
        var _timeout = 0;

        if(StudyViewSurvivalPlotView.getInitStatus()) {
            var _length = StudyViewSurvivalPlotView.getNumOfPlots();

            for(var i = 0; i < _length; i++){
                if($("#study-view-survival-plot-" + i).css('display') === 'none'){
                    $("#study-view-survival-plot-" + i).css('display', 'block');
                    msnry.layout();
                }
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
    function postFilterCallbackFunc(chartID, chartFilter){
        if(!StudyViewInitScatterPlot.getclearFlag() && !plotDataFlag){
            removeMarker();
            resetBars();
            redrawSpecialPlots();
            // update the breadcrumbs
            updateBreadCrumbs(chartID, chartFilter);
        }
    }

    function updateBreadCrumbs(chartID, chartFilter) {
        var chartAttribute=displayedID[chartID];
        var chartType = varType[chartAttribute];

        if(chartType==="bar"){
            //var crumbTip = chartFilter==null?"":chartAttribute+": "+chartFilter[0]+" - "+chartFilter[1];
            //BreadCrumbs.updateBarChartBreadCrumb(chartID, chartAttribute, crumbTip, crumbTip, chartType);
            BreadCrumbs.updateBarChartBreadCrumb(chartID, chartFilter, chartAttribute, chartType);
        }
        else if(chartType==="pie"){
            //BreadCrumbs.updatePieChartBreadCrumb(chartID, chartFilter, chartAttribute+": "+chartFilter, chartFilter, chartType);
            BreadCrumbs.updatePieChartBreadCrumb(chartID, chartFilter, chartAttribute, chartType);
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
            _attrIdsLength = 0;

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
        
        if(varChart[_chartID].getChart().hasFilter()){
            varChart[_chartID].getChart().filterAll();
            dc.redrawAll();
            redrawSpecialPlots();
        }
        varChart[_chartID].destroy();
        dc.deregisterChart(varChart[_chartID].getChart());
//        $('#study-view-add-chart ul')
//                .append($('<li></li>').attr('id',_value[0]).text(_value[1]));
//
//        $('#study-view-add-chart ul').stop().hide();
//        $('#study-view-add-chart ul').css('height','100%');
        $("#study-view-dc-chart-main-" + _chartID).qtip('destroy', true);
        $("div").remove("#study-view-dc-chart-main-" + _chartID);
        
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
        $('#study_view_add_chart_chzn').css('display','inline-block');
        varChart[_chartID] = "";
        removedChart.push(Number(_chartID));
    }

    function changeHeader(){
        var _dimention = varChart[attrNameMapUID.CASE_ID].getChart().dimension();
        var _result = _dimention.top(Infinity);

        StudyViewInitTopComponents.changeHeader(_result, numOfCases, removedChart);
    }

    //This filter is the same one which used in previous Google Charts Version,
    //should be revised later.
    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(sex)|(darwin_vital_status)|(darwin_patient_age)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*type.*)|(.*site.*)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)|(sequenced)|(has_cna_data)|(sample_count_patient)/);
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
                    if(attrNameMapUID.CASE_ID !== i){
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

    /**
     * Pass in array of case Ids, return stable case Ids(fix case sensitive)
     * @param {type} caseIds
     * @returns {Array}
     */
    function getStableIds(caseIds) {
        var a = [],
            selectedIds = getSelectedCasesID();

        caseIds = caseIds.map(function(datum){ return datum.toString().toLowerCase();});

        if(caseIds instanceof Array && caseIds.length > 0) {
            selectedIds.forEach(function(e, i){
                if( caseIds.indexOf(e.toString().toLowerCase()) !== -1) {
                    a.push(e);
                }
            });
        }

        return a;
    }

    function filterChartsByGivingIDs(_ids){
        var _caseIDChart = varChart[attrNameMapUID.CASE_ID].getChart();

        if(_ids.length > 1){
            StudyViewInitScatterPlot.setClickedCasesId('');
        }
        // _ids = getStableIds(_ids);
        _caseIDChart.filterAll();
        _caseIDChart.filter([_ids]);
        dc.redrawAll();

        postFilterCallbackFunc(_ids);
    }

    function createNewChartFromOutside(_id, _text) {
        var _selectedChartType,
            _index,
            _chartType = [],
            _selectedAttrDisplay = _text,
            _chartID = -1,
            _createdFlag = true;

        if(_id === 'mutationCNA'){
            _chartType = ['scatter'];
        }else if(_id.indexOf('survival') !== -1){
            _chartType = [_id];
        }else if(varType.hasOwnProperty(_id)){
            _chartType = varType[_id].split(',');
        }else {
            _chartType = [_id];
        }

        _selectedChartType = _chartType[0];

        if(_id==='mutationCNA'){
            $("#study-view-scatter-plot").css('display','block');
        }else if(_id.indexOf('survival') !== -1){
            var tmp = _id.split("-"),
                _index = tmp[tmp.length - 1];

            $("#study-view-survival-plot-" + _index).css('display','block');
        }else if(varType.hasOwnProperty(_id)){
            if(totalCharts < 31) {
                if(Object.keys(attrNameMapUID).indexOf(_id) !== -1){
                    _chartID = attrNameMapUID[_id];
                }else{
                    _chartID = totalCharts;
                    HTMLtagsMapUID["study-view-dc-chart-" + totalCharts] = totalCharts;
                    attrNameMapUID[_id] = totalCharts;
                    totalCharts++;
                }

                if(_selectedChartType === 'pie'){
                    makeNewPieChartInstance(_chartID, {
                        attr_id:_id,
                        display_name:_selectedAttrDisplay,
                        keys: varKeys[_id]});
                }else{
                    makeNewBarChartInstance(_chartID,
                                            {attr_id:_id,
                                                display_name:_selectedAttrDisplay},
                                            distanceMinMaxArray[_id]);
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
        }else {
            // update the table's data and show it
            var tableIdMain = _id.replace('-option', '');
            var redrawService = getRedrawService();
            $('#' + tableIdMain).css('display','block');

            // call with minimal timeout to allow the visual update
            setTimeout(function() {
                var tableId = tableIdMain.replace("-main", "");
                StudyViewInitTables.showSingleLoadingIcon(redrawService, tableId);
                StudyViewInitTables.redrawSingleTable(redrawService, tableId);
            }, 1);
        }

        if(_createdFlag) {

            _index = removedChart.indexOf(_chartID);
            if (_index > -1) {
                removedChart.splice(_index, 1);
            }else {
                displayedID.push(_id)
                varDisplay.push(_selectedAttrDisplay);
            }

            bondDragForLayout();

//            $('#study-view-add-chart ul').find('li[id="' + _selectedAttr + '"]').remove();
            $('#study-view-add-chart').find('option[id="' + _id + '"]').remove();
//            if($('#study-view-add-chart ul').find('li').length === 0 ){
            if($('#study-view-add-chart').find('option').length === 1 &&
                    $('#study-view-add-chart').find('option').attr('id') === ''){
                $('#study_view_add_chart_chzn').css('display','none');
            }

            $("#study-view-add-chart").trigger("liszt:updated");
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
        bondDragForLayout: bondDragForLayout,
        getFilteredResults: function() {
            var _filteredResult = varChart[attrNameMapUID.CASE_ID].getCluster().top(Infinity);
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
            return attrNameMapUID.CASE_ID;
        },

        getPlotDataFlag: function() {
            return plotDataFlag;
        },

        setPlotDataFlag: function(_flag) {
            plotDataFlag = _flag;
        },

        clearScatterPlot: clearScatterPlot,
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
