//TODO: Colors have conflicts when user save SELECTED_CASES/UNSELECTED_CALSES/ALL_CASES

var StudyViewInitSurvivalPlot = (function() {
    var caseList = {},
        originalData = [],
        data = {},
        opts = {},
        inputArr = [],
        survivalCurve = "",
        kmEstimator = "",
        logRankTest = "";
        
    var curveInfo = [];
    var allCases = [];
    
    /*This color will be used for ALL_CASES, SELECTED_CASES AND UNSELECTED_CASES*/
    var uColor = ["#000000","#2986e2","#dc3912"];//
    var color  = [];
        
    //Saved curve information is identified based on the curve name,
    //in other words, the name of each curve is identical. 
    var savedCurveInfo = {};
    
    function getSavedCurveName(){
        return Object.keys(savedCurveInfo);
    }
    
    function initSelection(){
        var _attrsInfo = StudyViewInitCharts.getShowedChartsInfo();
        var _length = _attrsInfo['name'].length;
        
        for(var i = 0; i < _length; i++){
            if(_attrsInfo['type'][_attrsInfo['name'][i]] === 'pie'){
                $("#study-view-survival-plot-select").append('<option value="'+_attrsInfo['name'][i]+'">'+_attrsInfo['displayName'][i]+'</option>');
            }
        }
        
         $("#study-view-survival-plot-select").change(function(){
             var _value = $('#study-view-survival-plot-select>option:selected')
                     .attr('value');
             
             if(_value === ''){
                 redraw(allCases, false);
             }else{
                 redraw([], _value);
             }
         });
    }
    
    function addEvents() {
        StudyViewUtil.showHideDivision(
                'study-view-survival-plot',
                'study-view-survival-plot-header'
        );

        $("#study-view-survival-plot svg image").hover(function(){
            $(this).css('cursor', 'pointer');
        });
        
        $("#study-view-survival-plot svg image").click(function(){
            if($(this).attr('name') === 'pin'){
                
                //The following functions will be excuted after user inputting
                //the curve name, so we need to give it a call back function.
                nameCurveDialog(this, saveCurveInfoFunc);
                
            }else if($(this).attr('name') === 'close'){
                var _parent = $(this).parent(),
                    _name = $(_parent).find('text').attr('value');
                    
                $(_parent).remove();
                removeSavedCurveFunc(_name);
                redrawLabel();
            }else{
                //TODO: 
            }
        });
        
        $("#study-view-survival-plot-loader").css('display', 'none');
    }
    
    //Save all related information with this curve. The saved curve(s) will be
    //showed again when redrawing survival plots
    function saveCurveInfoFunc(_this){
        var _selectedIndex = $($(_this).parent()).index(),
            _selectedCurveInfo = curveInfo[_selectedIndex];
    
        savedCurveInfo[_selectedCurveInfo.name] = _selectedCurveInfo;
        //removeSaveCurveColor(_selectedCurveInfo.color);
        removeElement($(_this).parent());
        
        //After saving curve, the related curve info should be delete from 
        //curvesInfo
        StudyViewUtil.arrayDeleteByIndex(curveInfo, _selectedIndex);
        redrawLabel();
    }
    
    function removeElement(_this){
        $(_this).remove();
    }
    
    function removeSavedCurveFunc(_curveName){
        var _targetCurve = savedCurveInfo[_curveName];
        curveInfo.push(_targetCurve);
        delete savedCurveInfo[_curveName];
        resetColor(_targetCurve.color, 'no');
    }
    
    //Reset specific color element
    function resetColor(_colorName, _status){
        var _colorsLength = color.length;
        
        for(var i = 0; i < _colorsLength; i++){
            if(color[i].name === _colorName){
                color[i].status = _status;
                break;
            }
        }
    }
    
    //Reset all unselected color status to no
    function resetColorExceptSaved(){
        var _colorsLength = color.length,
            _savedColors = [];
    
        for(var key in savedCurveInfo){
            _savedColors.push(savedCurveInfo[key].color);
        }
        
        for(var i = 0; i < _colorsLength; i++){
            if(_savedColors.indexOf(color[i].name) === -1){
                color[i].status = 'no';
            }
        } 
    }
    //When user click pin icon, this dialog will be popped up and remind user
    //input the curve name.
    function nameCurveDialog(_this, _callBackFunc) {
        var _parent = $(_this).parent();

        $(_this).qtip({
            content: {
                text: $('<input type="text" style="float:left"/><button>OK</button>'), // Create an input (style it using CSS)
                title: 'Please name your curve',
                button: 'Close'
            },
            position: {
                my: 'left bottom',
                at: 'top right',
                target: $(_this),
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
                        
                        if(_curveName === ''){
                            var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.warningQtip);
                            
                            _qtip.content.text = $("<span>No Name Inputed</span>");
                            _qtip.position.target = $(_this);
                            $(_parent).qtip(_qtip);
                        }else if(getSavedCurveName().indexOf(_curveName) !== -1){
                            var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.warningQtip);
                            
                            _qtip.content.text = $("<span>The curve name exists</span>");
                            _qtip.position.target = $(_this);
                            $(_parent).qtip(_qtip);
                        }else {
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
                            
                            //Update curve name with user inputted name
                            curveInfo[$($(_this).parent()).index()].name = _curveName;
                            _callBackFunc(_this);
                        }
                        //Set to True: call .hide() before destroy
                         api.destroy(true);
                    });
                }
            }
        });
    }
    
    function createDiv() {
        $("#study-view-charts").append(StudyViewBoilerplate.survivalPlotDiv);
    }
    
    function dataProcess(_data){
        var _dataLength = _data.length;
        
        
        originalData = _data;
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
    
    function grouping(_caseLists, _seperateAttr){
        //If seperation attribute has been defined, the data will be put in
        //different group based on this attribute.
        var _dataLength = originalData.length;
        if(_seperateAttr !== '' && _seperateAttr){
            for (var i = 0; i < _dataLength; i++) {  
                var _attr = originalData[i][_seperateAttr].toUpperCase(),
                    _caseID = originalData[i].CASE_ID;
                if(!caseList.hasOwnProperty(_attr)){
                    caseList[_attr] = [];
                }
                caseList[_attr].push(_caseID);
            }
        }else{
            caseList = _caseLists;
        }
    }
    
    //Set local global params and only will be initialized once.
    function initParams(){
        var _colorNames = jQuery.extend(true, [], StudyViewBoilerplate.chartColors).slice(2);
        var _colorNamesLength = _colorNames.length;
        
        for(var i = 0; i < _colorNamesLength; i++){
            var _colorDatum = {
                name : _colorNames[i],
                status : "no"
                };
                
            color.push(_colorDatum);
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
        var _color = "";
        
        kmEstimator = new KmEstimator(); 
        logRankTest = new LogRankTest();   
        //confidenceIntervals = new ConfidenceIntervals();   
        
        for(var key in caseList){
            
            //Drawing survival curve needs at least two cases
            if(key !== 'NA'){
                var instanceData = new SurvivalCurveProxy();
                
                instanceData.init(data, caseList[key], kmEstimator, logRankTest);
                
                //If no data return, will no draw this curve
                if(instanceData.getData().length > 0){
                    var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                    _color = colorSelection(key);
                    instanceSettings.line_color = _color;
                    instanceSettings.mouseover_color = _color;

                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);

                    var _curveInfoDatum = {
                        name: key,
                        color: _color,
                        caseList: caseList[key],
                        data: instance
                    };

                    curveInfo.push(_curveInfoDatum);
                }
            }
        }
        
        if(getSavedCurveName().length > 0){
            initSavedCurves();
        }
        //We disabled pvalue calculation in here
        survivalCurve = new SurvivalCurve();
        survivalCurve.init(inputArr, opts);
    }
    
    //Put all rule in here to select color for curves
    function colorSelection(_key){
        //We have unique colors and colors
        var _color;
        if(_key === 'ALL_CASES'){
            _color = uColor[0];
        }else if(_key === 'SELECTED_CASES'){
            _color = uColor[1];
        }else if(_key === 'UNSELECTED_CASES'){
            _color = uColor[2];
        }else {
            var _colorsLength = color.length;
            
            for(var i = 0; i < _colorsLength; i++){
                if(color[i].status === 'no'){
                    _color = color[i].name;
                    color[i].status = "used";
                    break;
                }else{
                }
            }
        }
        return _color;
    }
    
    //Will be called in initView when has saved cureves
    function initSavedCurves(){
        for(var key in savedCurveInfo){
            inputArr.push(savedCurveInfo[key].data);
        }
    }
    
    //Redraw curves based on selected cases and unselected cases
    function redraw(_caseIDs, _selectedAttr){
        removeContentAndRunLoader();
        resetParams();
        grouping(_caseIDs, _selectedAttr);
        initView();
        drawLabels();
        addEvents();
        if(_selectedAttr === '' || !_selectedAttr){
            resetSelection();
        }
    }
    
    function resetParams(){
        inputArr = [];
        survivalCurve = "";
        kmEstimator = "";
        logRankTest = "";
        curveInfo = [];
        caseList = {};
        resetColorExceptSaved();
    }
    
    //If no separate attribute selected, the selction box should set to default
    function resetSelection(){
        $("#study-view-survival-plot-select").val('').prop('selected',true);
    }
    
    //Remove survival plot including all labels
    function removeContentAndRunLoader(){
        $("#study-view-survival-plot-loader").css('display', 'block');
        $("#study-view-survival-plot-body-svg svg").remove();
        $("#study-view-survival-plot-body-label svg").remove();
    }
    
    function drawLabels(){
        var _newLabelsLength = curveInfo.length;
        var _savedLabelsLength = getSavedCurveName().length;
        var _numOfLabels = _newLabelsLength + _savedLabelsLength;
        
        var _height = _numOfLabels * 20;
        if(_savedLabelsLength > 0){
            _height += 20;
        }
        
        var _svg = d3.select("#" + opts.divs.labelDivId)
            .append("svg")
            .attr("width", 100)
            .attr("height", _height);
        
        drawNewLabels(_svg, 0);
        
        if(_savedLabelsLength > 0){
            //separator's height is 60px;
            drawSeparator(_svg,_newLabelsLength);
            drawSavedLabels(_svg, _newLabelsLength + 1);
        }
    }
        
    //Draw text and black line between new labels and saved labels
    function drawSeparator(_svg, _index){
        var _g = _svg.append("g").attr('transform', 'translate(0, '+ (_index*20) +')');
        
         _g.append("text")
                .attr('x', 15)
                .attr('y', 12)
                .attr('fill', 'black')
                .attr('class', 'study-view-survival-label-font-1')
                .text('Saved Curves');
        
         _g.append("line")
                .attr('x1', 0)
                .attr('y1', 16)
                .attr('x2', 100)
                .attr('y2', 16)
                .attr('stroke', 'black')
                .attr('stroke-width', '2px');
    }
    //Draw saved labels if have any
    function drawSavedLabels(_svg, _startedIndex){
        var _savedLabelsLength = getSavedCurveName().length;
        
        if(_savedLabelsLength > 0){
            var _index = 0;
            for(var key in savedCurveInfo){
                drawLabelBasicComponent(_svg, _index + _startedIndex, savedCurveInfo[key].color, savedCurveInfo[key].name, 'close');
                _index++;
            }
        }
    }
    
    //Icon type has: pin or close
    function drawLabelBasicComponent(_svg, _index, _color, _textName, _iconType){
        var _showedName = _textName;
        var _g = _svg.append("g").attr('transform', 'translate(0, '+ (_index*20) +')');
        
        if(_showedName.length > 7){
            _showedName = _showedName.substring(0, 5) + "...";
        }
            
        _g.append("rect")
                .attr('width', 10)
                .attr('height', 10)
                .attr('fill', _color);

        _g.append("text")
                .attr('x', 15)
                .attr('y', 10)
                .attr('fill', 'black')
                .attr('id', 'survival_label_text_'+ _index)
                .attr('value', _textName)
                .text(_showedName);

        var _image = _g.append("image")
                .attr('x', '90')
                .attr('y', '0')
                .attr('height', '10px')
                .attr('width', '10px');
        if(_iconType === 'pin'){
            _image.attr('xlink:href', 'images/pin.png');
            _image.attr('name', 'pin');
        }else if(_iconType === 'close'){
             _image.attr('xlink:href', 'images/close.png');
            _image.attr('name', 'close');
        }else {
            //TODO:
        }
                
        if(_showedName.length > 7){
            var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.pieLabelQtip);

            _qtip.content.text = _textName;
            _qtip.position.my = "left bottom";
            _qtip.position.at = "top right";
            $('#survival_label_text_'+_index).qtip(_qtip);
        }
    }
    //Draw new curve labels including curve color, name and pin icon
    function drawNewLabels(_svg, _startedIndex){
        var _numOfLabels = curveInfo.length;
        for(var i = 0; i < _numOfLabels; i++){
            drawLabelBasicComponent(_svg, i + _startedIndex, curveInfo[i].color, curveInfo[i].name, 'pin');
        }
    }
    
    //Will be called when user pin/delete labeles
    function redrawLabel(){
        $("#study-view-survival-plot-body-label svg").remove();
        drawLabels();
        addEvents();
    }
    
    return {
        init: function(_caseLists, _data) {
            allCases = _caseLists;
            createDiv();
            initParams();
            initOpts();
            dataProcess(_data);
            grouping(_caseLists, '');
            initView();
            drawLabels();
            initSelection();
            addEvents();
        },
        
        redraw: redraw,
        redrawLabel: redrawLabel
    };
})();