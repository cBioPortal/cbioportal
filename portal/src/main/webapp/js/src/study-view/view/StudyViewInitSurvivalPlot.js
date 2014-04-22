//TODO: Colors have conflicts when user save SELECTED_CASES/UNSELECTED_CALSES/ALL_CASES

var StudyViewInitSurvivalPlot = (function() {
    var caseList = {},
        originalData = [],
        data = {},
        opts = {},
        inputArr = [],
        survivalCurve = "",
        kmEstimator = "",
        logRankTest = "",
        
        // if survival plot has been initialized, the status will be set to true.
        initStatus = false; 
        
    var curveInfo = [];
    var allCases = [];
    
    /*This color will be used for ALL_CASES, SELECTED_CASES AND UNSELECTED_CASES*/
    var uColor = ["#000000","#2986e2","#dc3912"];
    var reserveName = ["ALL_CASES","SELECTED_CASES","UNSELECTED_CASES"];
    /*Store data for unique curves: the color of these will be changed when user
     * saving them, in that case, the survival plot needs to redraw this curve*/
    var uColorCurveData = {};
    var color  = [];
        
    //Saved curve information is identified based on the curve name,
    //in other words, the name of each curve is identical. 
    var savedCurveInfo = {};
    
    function getSavedCurveName(){
        return Object.keys(savedCurveInfo);
    }
    
    function getInitStatus(){
        return initStatus;
    }
    
    function getSavedCurveLength(){
        return getSavedCurveName().length;
    }
    
    function initSelection(){
        var _attrsInfo = StudyViewInitCharts.getShowedChartsInfo();
        var _length = _attrsInfo['name'].length;
        var _newInfo = [];
        
        for(var i = 0; i < _length; i++){
            if(_attrsInfo['type'][_attrsInfo['name'][i]] === 'pie'){
                var _newInfoDatum = {};
                _newInfoDatum.name = _attrsInfo['name'][i];
                _newInfoDatum.displayName = _attrsInfo['displayName'][i];
                _newInfo.push(_newInfoDatum);
            }
        }
        
        _newInfo.sort(function(a, b){
            var _aValue = a.displayName.toUpperCase();
            var _bValue = b.displayName.toUpperCase();
            
            return _aValue.localeCompare(_bValue);
        });
        
        _length = _newInfo.length;
        
        for(var i = 0; i < _length; i++){
            $("#study-view-survival-plot-select").append('<option value="'+_newInfo[i].name+'">'+_newInfo[i].displayName+'</option>');
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
                '#study-view-survival-plot',
                '#study-view-survival-plot-header'
        );
        
        StudyViewUtil.showHideDivision(
                "#study-view-survival-plot", 
                "#study-view-survival-plot .study-view-drag-icon"
        );
        
        //If user choose one of attribute to draw survival plot, these
        //information should be shown.
        if($('#study-view-survival-plot-select>option:selected').attr('value') !== ''){
            $("#study-view-survival-plot-header").css('display', 'block');
            $("#study-view-survival-plot .study-view-drag-icon").css('display', 'block');
        }
        
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
                    _name = $(_parent).find('text').attr('value'),
                    _color = $(_parent).find('rect').attr('fill');
                    
                $(_parent).remove();
                removeSavedCurveFunc(_name);
                redrawLabel();
                survivalCurve.removeCurve(_color.toString().substring(1));
            }else if($(this).attr('name') === 'undo'){
                var _parent = $(this).parent(),
                    _name = $(_parent).find('text').attr('value');
                    
                $(_parent).remove();
                undoSavedCurve(_name);
                removeSavedCurveFunc(_name);
                redrawLabel();
            }else{
                //TODO: Add more function
            }
        });
        
        
        $("#study-view-survival-plot svg rect").hover(function(){
            $(this).css('cursor', 'pointer');
        });
        
        $("#study-view-survival-plot svg rect").click(function(){
            var _text = $($(this).parent()).find('text:first'),
                _rgbRect = StudyViewUtil.rgbStringConvert($(this).css('fill')),
                _rgbText = StudyViewUtil.rgbStringConvert($(_text).css('fill')),
                _rectColor = StudyViewUtil.rgbToHex(_rgbRect[0], _rgbRect[1], _rgbRect[2]),
                _textColor =  StudyViewUtil.rgbToHex(_rgbText[0], _rgbText[1], _rgbText[2]);
            
            if(_textColor === '#000000'){
                $(_text).css('fill', 'red');
                highlightCurve(_rectColor.substring(1));
            }else{
                $(_text).css('fill', 'black');
                resetCurve(_rectColor.substring(1));
            }
            
        });
        
        $("#study-view-survival-plot-pdf").submit(function(){
                setSVGElementValue("study-view-survival-plot-body-pdf",
                    "study-view-survival-plot-pdf-value");
        });
        $("#study-view-survival-plot-svg").submit(function(){
            setSVGElementValue("study-view-survival-plot-body-svg",
                "study-view-survival-plot-svg-value");
        });
        
        $("#study-view-survival-plot-body").css('display', 'block');
        $("#study-view-survival-plot-loader").css('display', 'none');
    }
    
    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue){
        var svgElement;
        
        //Remove x/y title help icon first.
        svgElement = $("#" + _svgParentDivId).html();
        $("#" + _idNeedToSetValue).val(svgElement);
    }
    
    function highlightCurve(_curveId){
        var _hiddenDots = $("#"+_curveId+"-dots").find('path');
        
        $.each(_hiddenDots, function(index, obj){
            $(obj).css('opacity','.6');
        });
        $("#"+_curveId+"-line").css('stroke-width', '3px');
    }
    
    function resetCurve(_curveId){
        var _hiddenDots = $("#"+_curveId+"-dots").find('path');
        
        $.each(_hiddenDots, function(index, obj){
            $(obj).css('opacity','0');
        });
        $("#"+_curveId+"-line").css('stroke-width', '');
    }
    
    //Save all related information with this curve. The saved curve(s) will be
    //showed again when redrawing survival plots
    function saveCurveInfoFunc(_this){
        var _selectedIndex = $($(_this).parent()).index(),
            _selectedCurveInfo = curveInfo[_selectedIndex];
    
        if(StudyViewUtil.arrayFindByValue(uColor, _selectedCurveInfo.color)){
            var _color = colorSelection('');
            var _data =  uColorCurveData[_selectedCurveInfo.color];
            
            survivalCurve.removeCurve(_selectedCurveInfo.color.toString().substring(1));
            _data.settings.line_color = _color;
            _data.settings.mouseover_color = _color;
            _data.settings.curveId = _color.toString().substring(1);
            survivalCurve.addCurve(_data);
            _selectedCurveInfo.color = _color;
        }
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
    
    //Move saved curve infomation back to curveInfo
    function undoSavedCurve(_curveName){
        var _targetCurve = savedCurveInfo[_curveName];
        curveInfo.push(_targetCurve);
    }
    
    function removeSavedCurveFunc(_curveName){
        var _targetCurve = savedCurveInfo[_curveName];
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
        var _value = $(_parent).find("text:first").attr('value');
        var _qtipContent = '<input type="text" style="float:left"/><button style="float:left">OK</button>';
        
        if(!StudyViewUtil.arrayFindByValue(reserveName, _value)){
            _qtipContent += '<input type="checkbox"  style="float:left; font-size:200%"/>';
        }
        
        $(_this).qtip({
            content: {
                text: $(_qtipContent), // Create an input (style it using CSS)
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
                            
                            //Update curve name with user inputted name
                            curveInfo[$($(_this).parent()).index()].name = _curveName;
                            _callBackFunc(_this);
                        }
                        //Set to True: call .hide() before destroy
                         api.destroy(true);
                    });
                    $('input:text', api.elements.tooltip).keyup(function(){
                        var _currentText = $(this).attr('value');
                        var _parentText = $($(_this).parent()).find('text').attr('value');
                        
                        if(_currentText === _parentText){
                            $('input:checkbox', api.elements.tooltip).attr('checked', 'checked');
                        }else{
                            $('input:checkbox', api.elements.tooltip).attr('checked', false);
                        }
                    });
                    $('input:checkbox', api.elements.tooltip).change(function(){
                        var _checked = $(this).attr('checked');
                        
                        if(_checked === 'checked'){
                            var _text = $($(_this).parent()).find('text').attr('value');
                            $('input:text', api.elements.tooltip).attr('value', _text);
                        }else{
                            $('input:text', api.elements.tooltip).attr('value', '');
                        }
                    });
                    $('input:checkbox', api.elements.tooltip).qtip({
                        content: {
                            text: 'Keep original name.'
                        },
                        position: {
                            my: 'left bottom',
                            at: 'top right',
                            target: $('input:checkbox', api.elements.tooltip),
                            viewport: $(window)
                        },
                        style: {
                            classes: 'qtip-blue'
                        }
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
            if(_data[i].hasOwnProperty('OS_MONTHS') && _data[i].hasOwnProperty('OS_STATUS')){
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
    }
    
    function grouping(_caseLists, _seperateAttr){
        //If seperation attribute has been defined, the data will be put in
        //different group based on this attribute.
        var _dataLength = originalData.length;
        if(_seperateAttr !== '' && _seperateAttr){
            for (var i = 0; i < _dataLength; i++) {  
                var _arr = originalData[i][_seperateAttr].toUpperCase(),
                    _caseID = originalData[i].CASE_ID;
                if(!caseList.hasOwnProperty(_arr)){
                    caseList[_arr] = [];
                }
                caseList[_arr].push(_caseID);
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
        opts.settings.include_legend = false;
        opts.settings.include_pvalue = false;
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
    
    function redrawView(_selectedAttr){
        var _color = "";
        
        kmEstimator = new KmEstimator(); 
        logRankTest = new LogRankTest();   
        //confidenceIntervals = new ConfidenceIntervals();   
        
        for(var key in caseList){
            
            //Drawing survival curve needs at least two cases
            if(!(key === 'NA' && _selectedAttr==='OS_MONTHS')){
                var instanceData = new SurvivalCurveProxy();
                
                instanceData.init(data, caseList[key], kmEstimator, logRankTest);
                
                //If no data return, will no draw this curve
                if(instanceData.getData().length > 0){
                    var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                    _color = colorSelection(key);
                    instanceSettings.line_color = _color;
                    instanceSettings.mouseover_color = _color;
                    instanceSettings.curveId = _color.toString().substring(1);
                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);
                    
                    if(StudyViewUtil.arrayFindByValue(reserveName, key)){
                        uColorCurveData[uColor[reserveName.indexOf(key)]] = instance;
                    }
                    
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
        
        var inputArrLength = inputArr.length;
        for(var i = 0; i < inputArrLength; i++){
            survivalCurve.addCurve(inputArr[i]);
        }
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
                    instanceSettings.curveId = _color.toString().substring(1);
                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);
                    
                    if(StudyViewUtil.arrayFindByValue(reserveName, key)){
                        uColorCurveData[uColor[reserveName.indexOf(key)]] = instance;
                    }
                    
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
        
        /* There isn't any saved curve when intitialize survival plot
        if(getSavedCurveName().length > 0){
            initSavedCurves();
        }*/
        
        //We disabled pvalue calculation in here
        survivalCurve = new SurvivalCurve();
        survivalCurve.init(inputArr, opts);
        
        
        $("#study-view-survival-plot-pdf-name").val("Survival_Plot_result-"+ StudyViewParams.params.studyId +".pdf");
        $("#study-view-survival-plot-svg-name").val("Survival_Plot_result-"+ StudyViewParams.params.studyId +".svg");
    }
    
    //Put all rule in here to select color for curves
    function colorSelection(_key){
        //We have unique colors and colors
        var _color;
        if(StudyViewUtil.arrayFindByValue(reserveName, _key)){
            _color = uColor[reserveName.indexOf(_key)];
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
        var _curveInfoLength = curveInfo.length;
        
        $("#study-view-survival-plot-loader").css('display', 'block');
        $("#study-view-survival-plot-body").css('display', 'none');
        
        for(var i = 0; i < _curveInfoLength; i++){
            survivalCurve.removeCurve(curveInfo[i].color.toString().substring(1));
        }
        
        //removeContentAndRunLoader();
        resetParams();
        grouping(_caseIDs, _selectedAttr);
        redrawView(_selectedAttr);
        drawLabels();
        addEvents();
        if(_selectedAttr === '' || !_selectedAttr){
            resetSelection();
        }
    }
    
    function resetParams(){
        inputArr = [];
        //survivalCurve = "";
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
        
        
        $("#study-view-survival-plot-body-label svg").remove();
        
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
        var _showedName = _textName.substring(0,1) + _textName.substring(1).toLowerCase();
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

        if(_iconType === 'pin'){
            var _image = _g.append("image")
                .attr('x', '90')
                .attr('y', '0')
                .attr('height', '10px')
                .attr('width', '10px');
        
            _image.attr('xlink:href', 'images/pushpin.svg');
            _image.attr('name', 'pin');
        }else if(_iconType === 'close'){
            var _image = _g.append("image")
                .attr('x', '75')
                .attr('y', '0')
                .attr('height', '10px')
                .attr('width', '10px');
            _image.attr('xlink:href', 'images/undo.svg');
            _image.attr('name', 'undo');
            
            _image = _g.append("image")
                .attr('x', '90')
                .attr('y', '1')
                .attr('height', '8px')
                .attr('width', '8px');
        
            _image.attr('xlink:href', 'images/close.svg');
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
            if(Object.keys(data).length > 0){
                initStatus = true;
                grouping(_caseLists, '');
                initView();
                drawLabels();
                initSelection();
                addEvents();
            }else{
                StudyViewUtil.echoWarningMessg("No Overall Data available, the survival plot should not be initialized.");
            }
        },
        getInitStatus: getInitStatus,
        redraw: redraw,
        redrawLabel: redrawLabel
    };
})();