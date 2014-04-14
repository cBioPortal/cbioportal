var StudyViewInitSurvivalPlot = (function() {
    var caseList,
        data = {},
        opts = {},
        inputArr = [],
        survivalCurve = "",
        kmEstimator = "",
        logRankTest = "";
    
    function createDiv() {
        $("#study-view-charts").append(StudyViewBoilerplate.survivalPlotDiv);
    }
    
    function dataProcess(_data){
        var _dataLength = _data.length;
        
        for (var i = 0; i < _dataLength; i++) {  
            var _os = _data[i].OS_MONTHS,
                _osStatus = _data[i].OS_STATUS.toUpperCase(),
                _caseID = _data[i].CASE_ID;
            
            data[_caseID] = {};
            
            data[_caseID].case_id = _caseID;
            
            if(_osStatus === null || _osStatus.length === 0 || _osStatus === 'NA') {
                data[_caseID].status = 'NA';
            } else if (_osStatus === "DECEASED") {
                data[_caseID].status =  '1';
            } else if(_osStatus === "LIVING") {
                data[_caseID].status =  '0';
            }
            
            if(_os === null || _os.length === 0 || _os === 'NA') {
                data[_caseID].months = 'NA';
            } else{
                data[_caseID].months =  Number(_data[i].OS_MONTHS);
            }
        }
    }
    
    function initParams(_caseIDs) {
        caseList = _caseIDs;
    }
    
    function initOpts(){
        opts = jQuery.extend(true, {}, SurvivalCurveBroilerPlate);

        //Customize settings
        opts.text.xTitle = "Months Survival";
        opts.text.yTitle = "Surviving";
        opts.text.qTips.estimation = "Survival estimate";
        opts.text.qTips.censoredEvent = "Time of last observation";
        opts.text.qTips.failureEvent = "Time of death";
        opts.settings.canvas_width = 940;
        opts.settings.canvas_height = 600;
        opts.divs.curveDivId = "study-view-survival-plot-body-svg";
        opts.divs.headerDivId = "study-view-survival-plot-header";
        opts.divs.infoTableDivId = "study-view-survival-plot-table";
        opts.text.infoTableTitles.total_cases = "#total cases";
        opts.text.infoTableTitles.num_of_events_cases = "#cases deceased";
        opts.text.infoTableTitles.median = "median months survival";
    }
    
    function initView(){
        kmEstimator = new KmEstimator(); 
        logRankTest = new LogRankTest();   
        //confidenceIntervals = new ConfidenceIntervals();   

        //Init data instances for different groups
        var unalteredDataInst = new SurvivalCurveProxy();
        unalteredDataInst.init(data, caseList, kmEstimator, logRankTest);

        //Individual settings 
        var unalteredSettingsInst = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
        unalteredSettingsInst.line_color = "blue";
        unalteredSettingsInst.mouseover_color = "#81BEF7";
        unalteredSettingsInst.legend = "All cases";

        //Assemble the input
        var unalteredInputInst = {};
        unalteredInputInst.data = unalteredDataInst;
        unalteredInputInst.settings = unalteredSettingsInst;

        //render the curve
        inputArr = [unalteredInputInst];
        pValCallBackFunc(0);
        //logRankTest.calc(inputArr[0].data.getData(), pValCallBackFunc);
    }
    
    function pValCallBackFunc(_pVal){
        opts.vals.pVal = _pVal;
        survivalCurve = new SurvivalCurve();
        survivalCurve.init(inputArr, opts);
    }
    return {
        init: function(_caseIDs, _data) {
            createDiv();
            initParams(_caseIDs);
            initOpts();
            dataProcess(_data);
            initView();
        }  
    };
})();