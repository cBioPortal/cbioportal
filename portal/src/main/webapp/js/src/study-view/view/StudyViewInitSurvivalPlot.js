var StudyViewInitSurvivalPlot = (function() {
    var caseList = {},
        data = {},
        opts = {},
        inputArr = [],
        survivalCurve = "",
        kmEstimator = "",
        logRankTest = "";
        
    var curveInfo = [];
    
    var savedCurveInfo = [];
    
    function addEvents() {
        StudyViewOverallFunctions.showHideDivision(
                'study-view-survival-plot',
                'study-view-survival-plot-header'
        );

        $("#study-view-survival-plot svg image").hover(function(){
            $(this).css('cursor', 'pointer');
        });
        
        $("#study-view-survival-plot svg image").click(function(){
            if($(this).attr('name') === 'pin'){
                var _parent = $(this).parent();

                $(this).qtip({
                    content: {
                        text: $('<input type="text" style="float:left"/><button>OK</button>'), // Create an input (style it using CSS)
                        title: 'Please name your curve',
                        button: 'Close'
                    },
                    position: {
                        my: 'left bottom',
                        at: 'top right',
                        target: $(this),
                        viewport: $(window)
                    },
                    show: {
                        ready: true
                    },
                    hide: false,
                    style: {
                        tip: true,
                        classes: 'qtip-blue'
                    },
                    events: {
                        render: function(event, api) {
                            // Apply an event to the input element so we can close the tooltip when needed
                            $('button', api.elements.tooltip).bind('click', function() {
                                var _curveName = $('input', api.elements.tooltip).val();
                                var _modifiedName = _curveName;
                                if(_curveName !== ''){
                                    var _index = _parent.find('text').attr('id');
                                    //Destroy previous qtip first
                                    $('#'+_index).qtip('destroy', true);
                                    
                                    if(_curveName.length > 7){
                                        var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.pieLabelQtip);
                                        _qtip.content.text = _curveName;
                                        _qtip.position.my = "left bottom";
                                        _qtip.position.at = "top right";
                                        $('#'+_index).qtip(_qtip);
                                        _modifiedName = _curveName.substring(0, 5) + "...";
                                    }
                                    _parent.find('text')
                                            .text(_modifiedName)
                                            .attr('value', _curveName);
                                    _parent.find('image')
                                            .attr('href', 'images/close.png')
                                            .attr('name', 'close');
                                }
                                //Set to True: call .hide() before destroy
                                 api.destroy(true);
                            });
                        }
                    }
                });
            }
        });
    }
    
    function createDiv() {
        $("#study-view-charts").append(StudyViewBoilerplate.survivalPlotDiv);
    }
    
    function dataProcess(_data){
        var _dataLength = _data.length;
        
        //Get all of cases os information
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
    
    function grouping(_caseLists, seperateAttr){
        //If seperation attribute has been defined, the data will be put in
        //different group based on this attribute.
        if(seperateAttr !== '' && seperateAttr){
            for (var i = 0; i < _dataLength; i++) {  
                var _attr = _data[i][seperateAttr].toUpperCase(),
                    _caseID = _data[i].CASE_ID;
                if(!caseList.hasOwnProperty(_attr)){
                    caseList[_attr] = [];
                }
                caseList[_attr].push(_caseID);
            }
        }else{
            caseList = _caseLists;
        }
    }
    
    function initOpts(){
        opts = jQuery.extend(true, {}, SurvivalCurveBroilerPlate);

        //Customize settings
        opts.text.xTitle = "Months Survival";
        opts.text.yTitle = "Surviving";
        opts.text.qTips.estimation = "Survival estimate";
        opts.text.qTips.censoredEvent = "Time of last observation";
        opts.text.qTips.failureEvent = "Time of death";
        opts.settings.canvas_width = 450;
        opts.settings.canvas_height = 430;
        opts.settings.chart_width = 360;
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
            
            //Drawing survival curve needs at least two cases
            if(key !== 'NA'){
                var instanceData = new SurvivalCurveProxy();
                
                instanceData.init(data, caseList[key], kmEstimator, logRankTest);
                
                if(instanceData.getData().length > 0){
                    //Individual settings 
                    var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                    instanceSettings.line_color = color[index];
                    instanceSettings.mouseover_color = color[index];

                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);
                    
                    var _curveInfoDatum = {
                        name: key,
                        color: color[index],
                        caseList: caseList[key]
                    };

                    curveInfo.push(_curveInfoDatum);
                    index++;
                }
            }
        }
        
        //We disabled pvalue calculation in here
        survivalCurve = new SurvivalCurve();
        survivalCurve.init(inputArr, opts);
    }
    
    //Redraw curves based on selected cases and unselected cases
    function redraw(_caseIDs, _selectedAttr){
        removeContent();
        resetParams();
        grouping(_caseIDs, _selectedAttr);
        initView();
        drawLabels();
        addEvents();
    }
    
    function resetParams(){
        inputArr = [];
        survivalCurve = "";
        kmEstimator = "";
        logRankTest = "";
        curveInfo = [];
    }
    //Remove survival plot including all labels
    function removeContent(){
        $("#study-view-survival-plot-body-svg svg").remove();
        $("#study-view-survival-plot-body-label svg").remove();
    }
    //Draw curve labels including curve color, name and pin icon
    function drawLabels(){
        var _numOfLabels = curveInfo.length;
        var _svg = d3.select("#" + opts.divs.labelDivId)
            .append("svg")
            .attr("width", 100)
            .attr("height", _numOfLabels * 20);
        
        for(var i = 0; i < _numOfLabels; i++){
            var _showedName = curveInfo[i].name;
            
            if(_showedName.length > 7){
                _showedName = _showedName.substring(0, 5) + "...";
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
                    .attr('id', 'survival_label_text_'+i)
                    .attr('value', curveInfo[i].name)
                    .text(_showedName);
            
            _g.append("image")
                    .attr('xlink:href', 'images/pin.png')
                    .attr('x', '90')
                    .attr('y', '0')
                    .attr('height', '10px')
                    .attr('width', '10px')
                    .attr('name', 'pin');
            
            if(_showedName.length > 7){
                var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.pieLabelQtip);
                
                _qtip.content.text = curveInfo[i].name;
                _qtip.position.my = "left bottom";
                _qtip.position.at = "top right";
                $('#survival_label_text_'+i).qtip(_qtip);
            }
        }
    }
    
    return {
        init: function(_caseLists, _data) {
            createDiv();
            initOpts();
            dataProcess(_data);
            grouping(_caseLists, '');
            initView();
            drawLabels();
            addEvents();
        },
        
        redraw: redraw
    };
})();