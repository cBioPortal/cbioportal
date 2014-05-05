
var StudyViewInitScatterPlot = (function() { 
    var initStatus = false,
        arr = [],
        arrLength = 0,
        scatterPlot,
        clickedCaseId = '',
        shiftClickedCaseIds = '',
        brushedCaseIds = [],
        scatterPlotArr = [],
        scatterPlotDataAttr = {},
        scatterPlotOptions = {},
        dcCharts = [],
        clearFlag = false;
    
    function initData(_arr, _attr) {
        arr = jQuery.extend(true, [], _arr);
        arrLength = arr.length;
        
        $.each(_arr, function(i,value) {
            if( !isNaN(value['COPY_NUMBER_ALTERATIONS']) && 
                !isNaN(value['MUTATION_COUNT']) && 
                value['COPY_NUMBER_ALTERATIONS'] !== "" && 
                value['MUTATION_COUNT'] !== "") {                  
                    var _scatterPlotDatumTmp= {};
                    _scatterPlotDatumTmp.x_val = value['COPY_NUMBER_ALTERATIONS'];
                    _scatterPlotDatumTmp.y_val = value['MUTATION_COUNT'];
                    _scatterPlotDatumTmp.case_id = value['CASE_ID'];
                    _scatterPlotDatumTmp.qtip = "Case ID: <strong>" +
                        "<a href='tumormap.do?case_id=" +
                        value['CASE_ID'] + "&cancer_study_id=" +
                        StudyViewParams.params.studyId + "' target='_blank'>" + 
                        value['CASE_ID'] + "</a></strong>";
                    scatterPlotArr.push(_scatterPlotDatumTmp);
            }
        });
        
        dcCharts = StudyViewInitCharts.getCharts();
        
        scatterPlotDataAttr = jQuery.extend(true, {}, StudyViewBoilerplate.scatterPlotDataAttr);
        scatterPlotOptions = jQuery.extend(true, {}, StudyViewBoilerplate.scatterPlotOptions);    
        
        scatterPlotDataAttr.min_x = _attr.min_x;
        scatterPlotDataAttr.max_x = _attr.max_x;
        scatterPlotDataAttr.min_y = _attr.min_y;
        scatterPlotDataAttr.max_y = _attr.max_y;
    }
    
    function initComponent() {
        var _title = $("#study-view-scatter-plot chartTitleH4").text();
        
        if(scatterPlotArr.length !== 0){
            scatterPlot = new ScatterPlots();
            scatterPlot.init(scatterPlotOptions, scatterPlotArr, scatterPlotDataAttr,true);            
            scatterPlot.jointBrushCallback(scatterPlotBrushCallBack);
            scatterPlot.jointClickCallback(scatterPlotClickCallBack);
            
            if(scatterPlotDataAttr.max_x > 1000){
                $("#" + scatterPlotOptions.names.log_scale_x).attr('checked',true);
                scatterPlot.updateScaleX(scatterPlotOptions.names.log_scale_x);
            }
            if(scatterPlotDataAttr.max_y > 1000){
                $("#" + scatterPlotOptions.names.log_scale_y).attr('checked',true);
                scatterPlot.updateScaleY(scatterPlotOptions.names.log_scale_y);
            }
           
            $("#" + scatterPlotOptions.names.log_scale_x).change(function() {
                scatterPlot.updateScaleX(scatterPlotOptions.names.log_scale_x);
            });
            $("#" + scatterPlotOptions.names.log_scale_y).change(function() {
                scatterPlot.updateScaleY(scatterPlotOptions.names.log_scale_y);
            });
            
            StudyViewUtil
                    .showHideDivision("#study-view-scatter-plot", 
                                    "#study-view-scatter-plot-header");
            
            $("#study-view-scatter-plot-menu-icon").unbind("click");
            $("#study-view-scatter-plot-menu-icon").click(function() {
                var _side = $("#study-view-scatter-plot-side");
                var _display = _side.css('display');
                if (_display === "none") {
                    StudyViewUtil.changePosition("#study-view-scatter-plot", 
                                    "#study-view-scatter-plot-side",
                                    "#dc-plots");
                    _side.css('display', 'block');
                } else {
                    _side.css('display', 'none');
                }
            });
            
            $("#study-view-scatter-plot-pdf").submit(function(){
                setSVGElementValue("study-view-scatter-plot-body-svg",
                    "study-view-scatter-plot-pdf-value",
                    scatterPlotOptions,
                    _title);
            });
            $("#study-view-scatter-plot-svg").submit(function(){
                setSVGElementValue("study-view-scatter-plot-body-svg",
                    "study-view-scatter-plot-svg-value",
                    scatterPlotOptions,
                    _title);
            });
        }else{
            $('#study-view-scatter-plot').css('display','none');
        }
    }
    
    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue,scatterPlotDataAttr, _title){
        var svgElement;
        
        $("#" + _svgParentDivId + " .plots-title-x-help").remove();
        $("#" + _svgParentDivId + " .plots-title-y-help").remove();
        
        //Remove x/y title help icon first.
        svgElement = $("#" + _svgParentDivId + " svg").html();
        svgElement = "<svg><g><text text-anchor='middle' x='220' y='30' " +
                "style='font-weight:bold'>" + _title + 
                "</text></g><g transform='translate(0,40)'>" + 
                svgElement + "</g></svg>";
        $("#" + _idNeedToSetValue).val(svgElement);
        scatterPlot.updateTitleHelp(scatterPlotDataAttr.names.log_scale_x, scatterPlotDataAttr.names.log_scale_y);
    }
    
    function initPage(){
        $("#study-view-charts").append(StudyViewBoilerplate.scatterPlotDiv);
        $("#study-view-scatter-plot-pdf-name").val("Scatter_Plot_result-"+ StudyViewParams.params.studyId +".pdf");
        $("#study-view-scatter-plot-svg-name").val("Scatter_Plot_result-"+ StudyViewParams.params.studyId +".svg");
        $("#study-view-scatter-plot-header").css('display', 'none');
    }
    
    function redrawByAttribute(_casesInfo, _selctedAttr){
        var _style = [];
        /*
        for(var i = 0; i < arrLength; i++) {
            var _arr = arr[i][_selctedAttr],
                _caseId = arr[i]['CASE_ID'];
           
            if(!_casesInfo.hasOwnProperty(_arr)){
                if(_casesInfo.hasOwnProperty('NA')){
                    _casesInfo['NA'].caseIds.push(_caseId);
                }else{
                    StudyViewUtil.echoWarningMessg("Unexpected attribute: " + _arr);
                }
            }else{
                _casesInfo[_arr].caseIds.push(_caseId);
            }
        }
        */
        for(var key in _casesInfo){
            var _casesLength = _casesInfo[key].caseIds.length;
            
            for(var i = 0; i < _casesLength; i++){
                var styleDatum = {};
                styleDatum.case_id = _casesInfo[key].caseIds[i];
                styleDatum.fill = _casesInfo[key].color;
                styleDatum.stroke = _casesInfo[key].color;
                styleDatum.strokeWidth = '0';
                styleDatum.size = '60';
                _style.push(styleDatum);
            }
        }
        
        scatterPlot.updateStyle(_style);
    }
    
    function redraw(_selectedCaseIds) {
        if(initStatus){
            setStyle(_selectedCaseIds);
        }
    }
    
    function setStyle(_selectedCaseIds) {
        var _style = [];
        
        if(initStatus){
            for(var i=0 ; i< StudyViewParams.params.caseIds.length ; i++){
                var styleDatum = {};

                styleDatum.case_id = StudyViewParams.params.caseIds[i];
                if(_selectedCaseIds.length !== StudyViewParams.params.caseIds.length){
                    if(_selectedCaseIds.indexOf(StudyViewParams.params.caseIds[i]) !== -1){
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
                }else if(_selectedCaseIds.length === 0){
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

            scatterPlot.updateStyle(_style);
        }
    }
    
    function scatterPlotBrushCallBack(_brushedCaseIds) {
        var _numOfCharts = dcCharts.length;
        
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
                if(dcCharts[i] !== ''){
                    if(dcCharts[i].getChart().filters().length > 0)
                        dcCharts[i].getChart().filter(null);
                }
            }
        }
        
        scatterPlotCallBack(_brushedCaseIds);
        StudyViewInitCharts.removeMarker();
    }
    
    function scatterPlotCallBack(_caseIDs){
        var _numOfCharts = dcCharts.length,
            _caseIdChartIndex = StudyViewInitCharts.getCaseIdChartIndex();
        
        if(_caseIDs.length > 0){
            clearFlag = false;
            dcCharts[_caseIdChartIndex].getChart().filterAll();
            dcCharts[_caseIdChartIndex].getChart().filter([_caseIDs]);
            dc.redrawAll();
        
            StudyViewInitCharts.resetBars();
            StudyViewInitCharts.redrawWSCharts();
        }else{
            clearFlag = true;
            for(var i=0; i< _numOfCharts ; i++){
                if(dcCharts[i] !== ''){
                    if(dcCharts[i].getChart().filters().length > 0)
                        dcCharts[i].getChart().filterAll();
                }
            }
            dc.redrawAll();
            StudyViewInitCharts.resetBars();
            StudyViewInitCharts.redrawSpecialPlots();
        }
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
            StudyViewInitCharts.removeMarker();
            StudyViewInitCharts.getDataAndDrawMarker([_clickedCaseId]);
        }else{
            StudyViewInitCharts.removeMarker();
        }
        StudyViewInitCharts.resetBars();
        StudyViewInitCharts.changeHeader();
    }
    
    function scatterPlotShiftClick(_shiftClickedCaseIds){
        var _shiftClickedCasesLength = _shiftClickedCaseIds.length;
        
        StudyViewInitCharts.removeMarker();
        shiftClickedCaseIds = _shiftClickedCaseIds;
        if(_shiftClickedCasesLength !== 0){
            clickedCaseId = '';
            scatterPlotCallBack(shiftClickedCaseIds);
        }else{
            redrawChartsAfterDeletion();
            if(clickedCaseId !== '')
                StudyViewInitCharts.getDataAndDrawMarker([clickedCaseId]);
        }
    }
    
    return {
        init: function(_arr, _attr) {
            initData(_arr, _attr);
            initPage();
            initComponent();
            initStatus = true;
        },

        getScatterPlot: function() {
            if(scatterPlot === undefined){
                return false;
            }else{
                return scatterPlot;
            }
        },
        
        getInitStatus: function() {
            return initStatus;
        },
        
        redraw: redraw,
        setStyle: setStyle,
        redrawByAttribute: redrawByAttribute,
        setClickedCasesId: function(_caseId) {
            clickedCaseId = _caseId;
        },
        setShiftClickedCasesId: function(_caseIds) {
            shiftClickedCaseIds = _caseIds;
        },
        setBrushedCaseId: function(_caseIds) {
            brushedCaseIds = _caseIds;
        },
        getclearFlag: function() {
            return clearFlag;
        },
        setclearFlag: function(_flag) {
            clearFlag = _flag;
        }
    };
})();