var StudyViewInitSurvivalPlot = (function() {
    var caseList = {},
        data = {},
        opts = {},
        inputArr = [],
        survivalCurve = "",
        kmEstimator = "",
        logRankTest = "";
        
    var curveInfo = [];
    
    function createDiv() {
        $("#study-view-charts").append(StudyViewBoilerplate.survivalPlotDiv);
    }
    
    function dataProcess(_caseIDs, _data, seperateAttr){
        var _dataLength = _data.length;
            console.log(_data);
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
        
        //
        if(seperateAttr){
            for (var i = 0; i < _dataLength; i++) {  
                var _gender = _data[i][seperateAttr].toUpperCase(),
                    _caseID = _data[i].CASE_ID;
                if(!caseList.hasOwnProperty(_gender)){
                    caseList[_gender] = [];
                }
                caseList[_gender].push(_caseID);
            }
        }else{
            caseList.ALL_CASES = _caseIDs;
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
        opts.settings.canvas_width = 460;
        opts.settings.canvas_height = 430;
        opts.settings.chart_width = 370;
        opts.settings.chart_height = 380;
        opts.settings.chart_left = 80;
        opts.settings.chart_top = 0;
        opts.style.axisX_title_pos_x = 270;
	opts.style.axisX_title_pos_y = 420;
	opts.style.axisY_title_pos_x = -170;
	opts.style.axisY_title_pos_y = 20;  
        opts.divs.curveDivId = "study-view-survival-plot-body-svg";
        opts.divs.headerDivId = "";
        opts.divs.labelDivId = "study-view-survival-plot-body-label";
        opts.divs.infoTableDivId = "study-view-survival-plot-table";
        opts.text.infoTableTitles.total_cases = "#total cases";
        opts.text.infoTableTitles.num_of_events_cases = "#cases deceased";
        opts.text.infoTableTitles.median = "median months survival";
    }
    
    function initView(){
        kmEstimator = new KmEstimator(); 
        logRankTest = new LogRankTest();   
        //confidenceIntervals = new ConfidenceIntervals();   
        var color  = jQuery.extend(true, {}, StudyViewBoilerplate.chartColors);
        var index = 0;
        
        for(var key in caseList){
             //Init data instances for different groups
             
            if(key !== 'NA' && caseList[key].length > 2){
                var instanceData = new SurvivalCurveProxy();
                instanceData.init(data, caseList[key], kmEstimator, logRankTest);
                
                //Individual settings 
                var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                instanceSettings.line_color = color[index];
                instanceSettings.mouseover_color = color[index];
                //instanceSettings.legend = key;

                //Assemble the input
                var instance = {};
                instance.data = instanceData;
                instance.settings = instanceSettings;

                inputArr.push(instance);
                var _curveInfoDatum = {
                    name: key,
                    color: color[index]
                };
                
                curveInfo.push(_curveInfoDatum);
                index++;
            }
        }
        pValCallBackFunc(0);
        //logRankTest.calc(inputArr[0].data.getData(), pValCallBackFunc);
    }
    
    function pValCallBackFunc(_pVal){
        opts.vals.pVal = _pVal;
        survivalCurve = new SurvivalCurve();
        survivalCurve.init(inputArr, opts);
    }
    
    //Redraw curves based on selected cases and unselected cases
    function redraw(_caseIDs){
        
    }
    
    
    //Draw curve labels including curve color, name and pin icon
    function drawLabels(){
        var _numOfLabels = curveInfo.length;
        var _svg = d3.select("#" + opts.divs.labelDivId)
            .append("svg")
            .attr("width", 90)
            .attr("height", _numOfLabels * 20);
        
        for(var i = 0; i < _numOfLabels; i++){
            var _showedName = curveInfo[i].name;
            
            if(_showedName.length > 10){
                _showedName = _showedName.substring(0, 10);
            }
            
            var _g = _svg.append("g").attr('transform', 'translate(0, '+ (i*20) +')');
            
            _g.append("rect")
                    .attr('width', 10)
                    .attr('height', 10)
                    .attr('fill', curveInfo[i].color);
            
            _g.append("text")
                    .attr('x', 15)
                    .attr('y', 10)
                    .attr('fill', 'black')
                    .text(_showedName);
            
        }
    }
    
    return {
        init: function(_caseIDs, _data) {
            createDiv();
            //initParams(_caseIDs);
            initOpts();
            dataProcess(_caseIDs, _data, 'GENDER');
            initView();
            drawLabels();
        },
        
        redraw: redraw
    };
})();