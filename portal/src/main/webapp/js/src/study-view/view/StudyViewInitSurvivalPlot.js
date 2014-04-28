//TODO: Colors have conflicts when user save SELECTED_CASES/UNSELECTED_CALSES/ALL_CASES

var StudyViewInitSurvivalPlot = (function() {
    var oData = [], //The data before processing, orginal data
            oDataLength = 0,
            aData = {}, //The data after processing
            inputArr = [],
            survivalPlot = {},
            kmEstimator = "",
            logRankTest = "",
            plotsInfo = {},
            numOfPlots = 0,
            opts = [],
            // if survival plot has been initialized, the status will be set to true.
            initStatus = false;

    var curveInfo = {};

    /*This color will be used for ALL_CASES, SELECTED_CASES AND UNSELECTED_CASES*/
    var uColor = ["#000000", "#dc3912", "#2986e2"];
    var reserveName = ["ALL_CASES", "SELECTED_CASES", "UNSELECTED_CASES"];

    /*Store data for unique curves: the color of these will be changed when user
     * saving them, in that case, the survival plot needs to redraw this curve*/
    var uColorCurveData = {};

    //Saved curve information is identified based on the curve name,
    //in other words, the name of each curve is identical. 
    var savedCurveInfo = {};

    function getSavedCurveName(_id) {
        if(savedCurveInfo.hasOwnProperty(_id)){
            return Object.keys(savedCurveInfo[_id]);
        }else{
            return [];
        }
    }

    function getInitStatus() {
        return initStatus;
    }
    
    function addEvents(_opts) {
        if (!initStatus) {
            StudyViewUtil.showHideDivision(
                    '#' + _opts.divs.main,
                    '#' + _opts.divs.header
                    );
        }

        $('#' + _opts.divs.main + ' svg image').unbind('hover');
        $('#' + _opts.divs.main + ' svg image').hover(function() {
            $(this).css('cursor', 'pointer');
        });

        $('#' + _opts.divs.main + ' svg image').unbind('click');
        $('#' + _opts.divs.main + ' svg image').click(function() {
            if ($(this).attr('name') === 'pin') {

                //The following functions will be excuted after user inputting
                //the curve name, so we need to give it a call back function.
                nameCurveDialog(this, saveCurveInfoFunc);

            } else if ($(this).attr('name') === 'close') {
                var _parent = $(this).parent(),
                        _name = $(_parent).find('text').attr('value'),
                        _color = $(_parent).find('rect').attr('fill'),
                        _index = $(this).parent().index();

                $(_parent).remove();
                removeCurveFunc(_index);
                redrawLabel();
                survivalPlot[opts.index].removeCurve(_color.toString().substring(1));
            } else if ($(this).attr('name') === 'saved-close') {
                var _parent = $(this).parent(),
                        _name = $(_parent).find('text').attr('value');

                $(_parent).remove();
                undoSavedCurve(_name);
                removeSavedCurveFunc(_name);
                redrawLabel();
            } else {
                //TODO: Add more function
            }
        });

        $('#' + _opts.divs.main + ' svg rect').unbind('hover');
        $('#' + _opts.divs.main + ' svg rect').hover(function() {
            $(this).css('cursor', 'pointer');
        });

        $('#' + _opts.divs.main + ' svg rect').unbind('click');
        $('#' + _opts.divs.main + ' svg rect').click(function() {
            var _text = $($(this).parent()).find('text:first'),
                    _rgbRect = StudyViewUtil.rgbStringConvert($(this).css('fill')),
                    _rgbText = StudyViewUtil.rgbStringConvert($(_text).css('fill')),
                    _rectColor = StudyViewUtil.rgbToHex(_rgbRect[0], _rgbRect[1], _rgbRect[2]),
                    _textColor = StudyViewUtil.rgbToHex(_rgbText[0], _rgbText[1], _rgbText[2]);

            if (_textColor === '#000000') {
                $(_text).css('fill', 'red');
                highlightCurve(_rectColor.substring(1));
            } else {
                $(_text).css('fill', 'black');
                resetCurve(_rectColor.substring(1));
            }

        });

        $("#" + _opts.divs.pdf).unbind('submit');
        $("#" + _opts.divs.pdf).submit(function() {
            setSVGElementValue(_opts.divs.bodySvg,
                    _opts.divs.pdfValue, _opts);
        });
        $("#" + _opts.divs.svg).unbind('submit');
        $("#" + _opts.divs.svg).submit(function() {
            setSVGElementValue(_opts.divs.bodySvg,
                    _opts.divs.svgValue, _opts);
        });

        $("#" + _opts.divs.menu).unbind("click");
        $("#" + _opts.divs.menu).click(function() {
            var _label = $("#" + _opts.divs.bodyLabel);
            var _display = _label.css('display');
            if (_display === "none") {
                StudyViewUtil.changePosition(
                        '#' + _opts.divs.main,
                        '#' + _opts.divs.bodyLabel,
                        "#dc-plots");
                $('#' + _opts.divs.bodyLabel).children('float', '');
                _label.css('display', 'block');
            } else {
                _label.css('display', 'none');
            }
        });

        $("#" + _opts.divs.body).css('display', 'block');
        $("#" + _opts.divs.loader).css('display', 'none');
    }

    function setSVGElementValue(_svgParentDivId, _idNeedToSetValue, _opts) {
        var _svgElement, _svgLabels, _svgTitle,
                _labelTextMaxLength = 0,
                _numOfLabels = 0,
                _svgWidth = 500,
                _svgheight = 500;

        _svgElement = $("#" + _svgParentDivId + " svg").html();
        _svgLabels = $("#" + _opts.divs.bodyLabel + " svg");
        
        _svgLabels.find('image').remove();
        _svgLabels.find('text').each(function(i, obj) {
            var _value = $(obj).attr('value');

            if (typeof _value === 'undefined') {
                _value = $(obj).text();
            }

            if (_value.length > _labelTextMaxLength) {
                _labelTextMaxLength = _value.length;
            }
            $(obj).text(_value);
            _numOfLabels++;
        });

        _svgWidth += _labelTextMaxLength * 10;

        if (_svgheight < _numOfLabels * 20) {
            _svgheight = _numOfLabels * 20 + 40;
        }

        _svgLabels = _svgLabels.html();

        _svgTitle = "<g><text text-anchor='middle' x='" + _svgWidth / 2 +
                "' y='30' style='font-weight:bold'>Survival Plot</text></g>";

        _svgElement = "<svg width='" + _svgWidth + "px' height='" + _svgheight + "px' style='font-size:14px'>" +
                _svgTitle + "<g transform='translate(0,40)'>" +
                _svgElement + "</g><g transform='translate(450,40)'>" +
                _svgLabels + "</g></svg>";
        $("#" + _idNeedToSetValue).val(_svgElement);

        redrawLabel(_opts);
        //The style has been reset because of the addEvents function, so we
        //need to change the related components manully 
        $("#" + _opts.divs.header).css('display', 'block');
        $("#" + _opts.divs.main + " .study-view-drag-icon").css('display', 'block');
    }

    function highlightCurve(_curveId) {
        var _hiddenDots = $("#" + _curveId + "-dots").find('path');

        $.each(_hiddenDots, function(index, obj) {
            $(obj).css('opacity', '.6');
        });
        $("#" + _curveId + "-line").css('stroke-width', '3px');
    }

    function resetCurve(_curveId) {
        var _hiddenDots = $("#" + _curveId + "-dots").find('path');

        $.each(_hiddenDots, function(index, obj) {
            $(obj).css('opacity', '0');
        });
        $("#" + _curveId + "-line").css('stroke-width', '');
    }

    //Save all related information with this curve. The saved curve(s) will be
    //showed again when redrawing survival plots
    function saveCurveInfoFunc(_this, _id) {
        var _selectedIndex = $($(_this).parent()).index(),
                _selectedCurveInfo = curveInfo[_id][_selectedIndex];

        if (StudyViewUtil.arrayFindByValue(uColor, _selectedCurveInfo.color)) {
            var _color = colorSelection('');

            //If no more color available, have to set the color here
            if (!_color) {
                _color = '#111111';
            }
            var _data = uColorCurveData[_selectedCurveInfo.color];

            survivalPlot[_id].removeCurve(_selectedCurveInfo.color.toString().substring(1));
            _data.settings.line_color = _color;
            _data.settings.mouseover_color = _color;
            _data.settings.curveId = _color.toString().substring(1);
            survivalPlot[_id].addCurve(_data);
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

    function removeElement(_this) {
        $(_this).remove();
    }

    //Move saved curve infomation back to curveInfo
    function undoSavedCurve(_curveName, _index) {
        var _targetCurve = savedCurveInfo[_curveName];
        curveInfo[_index].push(_targetCurve);
    }

    function removeSavedCurveFunc(_curveName, _plotIndex) {
        var _targetCurve = savedCurveInfo[_plotIndex][_curveName];
        delete savedCurveInfo[_plotIndex][_curveName];
        resetColor(_targetCurve.color, 'no');
    }

    function removeCurveFunc(_index, _plotIndex) {
        curveInfo[_plotIndex].splice(_index, 1);
    }
    
    //When user click pin icon, this dialog will be popped up and remind user
    //input the curve name.
    function nameCurveDialog(_this, _callBackFunc) {
        var _parent = $(_this).parent();
        var _value = $(_parent).find("text:first").attr('value');
        var _qtipContent = '<input type="text" style="float:left"/><button style="float:left">OK</button>';

        if (!StudyViewUtil.arrayFindByValue(reserveName, _value)) {
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

                        if (_curveName === '') {
                            var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.warningQtip);

                            _qtip.content.text = $("<span>No Name Inputed</span>");
                            _qtip.position.target = $(_this);
                            $(_parent).qtip(_qtip);
                        } else if (getSavedCurveName().indexOf(_curveName) !== -1) {
                            var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.warningQtip);

                            _qtip.content.text = $("<span>The curve name exists</span>");
                            _qtip.position.target = $(_this);
                            $(_parent).qtip(_qtip);
                        } else {
                            var _index = _parent.find('text').attr('id');
                            //Destroy previous qtip first
                            $('#' + _index).qtip('destroy', true);

                            if (_curveName.length > 7) {
                                var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.pieLabelQtip);
                                _qtip.content.text = _curveName;
                                _qtip.position.my = "left bottom";
                                _qtip.position.at = "top right";
                                $('#' + _index).qtip(_qtip);
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
                    $('input:text', api.elements.tooltip).keyup(function() {
                        var _currentText = $(this).attr('value');
                        var _parentText = $($(_this).parent()).find('text').attr('value');

                        if (_currentText === _parentText) {
                            $('input:checkbox', api.elements.tooltip).attr('checked', 'checked');
                        } else {
                            $('input:checkbox', api.elements.tooltip).attr('checked', false);
                        }
                    });
                    $('input:checkbox', api.elements.tooltip).change(function() {
                        var _checked = $(this).attr('checked');

                        if (_checked === 'checked') {
                            var _text = $($(_this).parent()).find('text').attr('value');
                            $('input:text', api.elements.tooltip).attr('value', _text);
                        } else {
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
        $("#study-view-charts").append(StudyViewBoilerplate.survivalPlotDiv);
    }

    function basicDiv(_opt) {
        var _div = "<div id='" + _opt.divs.main + 
                "' class='study-view-dc-chart w2 h1half study-view-survival-plot'>" +
                "<div id='" + _opt.divs.headerWrapper +
                "' class='study-view-survival-plot-header-wrapper'>" +
                "<chartTitleH4 id='" + _opt.divs.main +
                "' class='study-view-survival-plot-title'>" + _opt.title + "</chartTitleH4>" +
                "<div id='" + _opt.divs.header +
                "' class='study-view-survival-plot-header' style='float:right'>" +
                "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opt.divs.pdf + "'>" +
                "<input type='hidden' name='svgelement' id='" + _opt.divs.pdfValue + "'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' id='" + _opt.divs.pdfName + "' name='filename' value=''>" +
                "<input type='submit' style='font-size:10px' value='PDF'>" +
                "</form>" +
                "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opt.divs.svg + "'>" +
                "<input type='hidden' name='svgelement' id='" + _opt.divs.svgValue + "'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' id='" + _opt.divs.svgName + "' name='filename' value=''>" +
                "<input type='submit' style='font-size:10px' value='SVG'>" +
                "</form>" +
                "<img id='" + _opt.divs.menu + "' class='study-view-menu-icon' style='float:left; width:10px; height:10px;margin-top:4px; margin-right:4px;' class='study-view-menu-icon' src='images/menu.svg'/>" +
                "<img style='float:left; width:10px; height:10px;margin-top:4px; margin-right:4px;' class='study-view-drag-icon' src='images/move.svg'/>" +
                "<span class='study-view-chart-plot-delete study-view-survival-plot-delete'>x</span>" +
                "</div></div>" +
                "<div id='" + _opt.divs.loader + "' style='width: 100%; display:none'>" +
                "<img src='images/ajax-loader.gif'/></div>" +
                "<div id='" + _opt.divs.body + "' class='study-view-survival-plot-body'>" +
                "<div id='" + _opt.divs.bodySvg + "' style='float:left'></div>" +
                "<div id='" + _opt.divs.bodyLabel +
                "' class='study-view-survival-plot-body-label'></div>" +
                "</div></div>";

        $("#study-view-charts").append(_div);
    }

    function dataProcess(_plotInfo) {
        var _numOfValuedCase = 0;
        var _plotData = {};
        ///oData = _data;
        //Get all of cases os information
        for (var i = 0; i < oDataLength; i++) {
            if (oData[i].hasOwnProperty(_plotInfo.property[0]) && oData[i].hasOwnProperty(_plotInfo.property[1])) {
                var _time = oData[i][_plotInfo.property[0]],
                        _status = oData[i][_plotInfo.property[1]].toUpperCase(),
                        _caseID = oData[i].CASE_ID;
                _plotData[_caseID] = {};

                _plotData[_caseID].case_id = _caseID;

                if (_status === _plotInfo.status[0]) {
                    _plotData[_caseID].status = '0';
                } else if (_status === _plotInfo.status[1]) {
                    _plotData[_caseID].status = '1';
                } else if (_status === null || _status.length === 0 || _status === 'NA') {
                    _plotData[_caseID].status = 'NA';
                } else {
                    //Temporary put into NA group
                    _plotData[_caseID].status = 'NA';
                }

                if (_time === null || _time.length === 0 || _time === 'NA') {
                    _plotData[_caseID].months = 'NA';
                } else {
                    _plotData[_caseID].months = Number(oData[i].OS_MONTHS);
                }
            }
        }


        //Refind search data, if only one or no case has months information,
        //the survival plot should not be initialized.
        for (var key in _plotData) {
            if (_plotData[key].months !== 'NA') {
                _numOfValuedCase++;
            }
        }

        if (_numOfValuedCase < 2) {
            _plotData = {};
        }

        return _plotData;
    }

    function grouping(_casesInfo, _seperateAttr) {
        //If seperation attribute has been defined, the data will be put in
        //different group based on this attribute.
        if (_seperateAttr !== '' && _seperateAttr) {
            for (var i = 0; i < oDataLength; i++) {
                var _arr = oData[i][_seperateAttr],
                        _caseID = oData[i].CASE_ID;
                if (!_casesInfo.hasOwnProperty(_arr)) {
                    if (_casesInfo.hasOwnProperty('NA')) {
                        _casesInfo['NA'].caseIds.push(_caseID);
                    } else {
                        StudyViewUtil.echoWarningMessg("Unexpected attribute: " + _arr);
                    }
                } else {
                    _casesInfo[_arr].caseIds.push(_caseID);
                }
            }
        }

        return _casesInfo;
    }

    //Set local global params and only will be initialized once.
    function initParams() {
        
    }

    function initOpts(_index) {
        var _opts = {};

        //Customize settings
        _opts.index = _index;
        _opts.title = plotsInfo[_index].name;
        _opts.divs = {};
        _opts.divs.main = "study-view-survival-plot-" + _index;
        _opts.divs.title = "study-view-survival-pot-title-" + _index;
        _opts.divs.header = "study-view-survival-plot-header-" + _index;
        _opts.divs.headerWrapper = "study-view-survival-plot-header-wrapper" + _index;
        _opts.divs.body = "study-view-survival-plot-body-" + _index;
        _opts.divs.bodySvg = "study-view-survival-plot-body-svg-" + _index;
        _opts.divs.bodyLabel = "study-view-survival-plot-body-label-" + _index;
        _opts.divs.pdf = "study-view-survival-plot-pdf-" + _index;
        _opts.divs.pdfName = "study-view-survival-plot-pdf-name-" + _index;
        _opts.divs.pdfValue = "study-view-survival-plot-pdf-value-" + _index;
        _opts.divs.svg = "study-view-survival-plot-svg-" + _index;
        _opts.divs.svgName = "study-view-survival-plot-svg-name-" + _index;
        _opts.divs.svgValue = "study-view-survival-plot-svg-value-" + _index;
        _opts.divs.menu = "study-view-survival-plot-menu-" + _index;
        _opts.divs.loader = "study-view-survival-groupingplot-loader-" + _index;

        //plot in _opts is for survival plot
        _opts.plot = jQuery.extend(true, {}, SurvivalCurveBroilerPlate);
        _opts.plot.text.xTitle = "Months Survival";
        _opts.plot.text.yTitle = "Surviving";
        _opts.plot.text.qTips.estimation = "Survival estimate";
        _opts.plot.text.qTips.censoredEvent = "Time of last observation";
        _opts.plot.text.qTips.failureEvent = "Time of death";
        _opts.plot.settings.canvas_width = 370;
        _opts.plot.settings.canvas_height = 320;
        _opts.plot.settings.chart_width = 290;
        _opts.plot.settings.chart_height = 250;
        _opts.plot.settings.chart_left = 70;
        _opts.plot.settings.chart_top = 5;
        _opts.plot.settings.include_legend = false;
        _opts.plot.settings.include_pvalue = false;
        _opts.plot.style.axisX_title_pos_x = 170;
        _opts.plot.style.axisX_title_pos_y = 305;
        _opts.plot.style.axisY_title_pos_x = -100;
        _opts.plot.style.axisY_title_pos_y = 20;
        _opts.plot.divs.curveDivId = "study-view-survival-plot-body-svg-" + _index;
        _opts.plot.divs.headerDivId = "";
        _opts.plot.divs.infoTableDivId = "study-view-survival-plot-table-" + _index;
        _opts.plot.text.infoTableTitles.total_cases = "#total cases";
        _opts.plot.text.infoTableTitles.num_of_events_cases = "#cases deceased";
        _opts.plot.text.infoTableTitles.median = "median months survival";

        return _opts;
    }

    function redrawView(_opts, _casesInfo) {
        var _color = "";
        
        inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        //confidenceIntervals = new ConfidenceIntervals();
        curveInfo[_opts.index] = [];
        
        for (var key in _casesInfo) {

            //Drawing survival curve needs at least two cases
            if (!(key === 'NA')) {
                var instanceData = new SurvivalCurveProxy();

                instanceData.init(aData[_opts.index], _casesInfo[key].caseIds, kmEstimator, logRankTest);

                //If no data return, will no draw this curve
                if (instanceData.getData().length > 0) {
                    var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                    _color = _casesInfo[key].color;
                    
                    if (_color) {
                        instanceSettings.line_color = _color;
                        instanceSettings.mouseover_color = _color;
                        instanceSettings.curveId = _color.toString().substring(1) + "-" + _opts.index;
                        //Assemble the input
                        var instance = {};
                        instance.data = instanceData;
                        instance.settings = instanceSettings;
                        inputArr.push(instance);

                        if (StudyViewUtil.arrayFindByValue(reserveName, key)) {
                            uColorCurveData[uColor[reserveName.indexOf(key)]] = instance;
                        }

                        var _curveInfoDatum = {
                            name: key,
                            color: _color,
                            caseList: _casesInfo[key].color,
                            data: instance
                        };

                        curveInfo[_opts.index].push(_curveInfoDatum);
                    } else {
                        alert("Sorry, you can not create more than 30 curves.");
                        break;
                    }
                }
            }
        }

        var inputArrLength = inputArr.length;
        for (var i = 0; i < inputArrLength; i++) {
            survivalPlot[_opts.index].addCurve(inputArr[i]);
        }
    }

    function initView(_casesInfo, _data, _opts) {
        var _color = "";
        var inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        //confidenceIntervals = new ConfidenceIntervals();   
        
        curveInfo[_opts.index] = [];
        
        for (var key in _casesInfo) {

            //Drawing survival curve needs at least two cases
            if (key !== 'NA') {
                var instanceData = new SurvivalCurveProxy();

                instanceData.init(_data, _casesInfo[key].caseIds, kmEstimator, logRankTest);
                //If no data return, will no draw this curve
                if (instanceData.getData().length > 0) {
                    var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                    _color = _casesInfo[key].color;
                    if (_color) {
                        instanceSettings.line_color = _color;
                        instanceSettings.mouseover_color = _color;
                        instanceSettings.curveId = _color.toString().substring(1) + "-" + _opts.index;
                        //Assemble the input
                        var instance = {};
                        instance.data = instanceData;
                        instance.settings = instanceSettings;
                        inputArr.push(instance);

                        if (StudyViewUtil.arrayFindByValue(reserveName, key)) {
                            uColorCurveData[uColor[reserveName.indexOf(key)]] = instance;
                        }

                        var _curveInfoDatum = {
                            name: key,
                            color: _color,
                            caseList: _casesInfo[key].caseIds,
                            data: instance
                        };
                        curveInfo[_opts.index].push(_curveInfoDatum);
                    } else {
                        alert("Sorry, you can not create more than 30 curves.");
                        break;
                    }
                }
            }
        }
        /* There isn't any saved curve when intitialize survival plot
         if(getSavedCurveName().length > 0){
         initSavedCurves();
         }*/

        //We disabled pvalue calculation in here
        survivalPlot[_opts.index] = new SurvivalCurve();
        survivalPlot[_opts.index].init(inputArr, _opts.plot);


        $("#" + _opts.divs.pdfName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".pdf");
        $("#" + _opts.divs.svgName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".svg");
    }

    //Redraw curves based on selected cases and unselected cases
    function redraw(_casesInfo, _selectedAttr) {
        for (var j = 0; j < numOfPlots; j++){
            var _curveInfoLength = curveInfo[j].length;
            
            $("#" + opts[j].divs.loader).css('display', 'block');
            $("#" + opts[j].divs.body).css('display', 'none');

            for (var i = 0; i < _curveInfoLength; i++) {
                survivalPlot[j].removeCurve(curveInfo[j][i].color.toString().substring(1) + "-" + opts[j].index);
            }

            kmEstimator = "";
            logRankTest = "";
            delete curveInfo[j];
            
            _casesInfo = grouping(_casesInfo, _selectedAttr);
            redrawView(opts[j], _casesInfo);
            drawLabels(opts[j]);
            addEvents(opts[j]);
        }
    }

    function drawLabels(_opts) {
        var _curveInfo = curveInfo[_opts.index];
        var _savedCurveInfo = savedCurveInfo[_opts.index];
        var _newLabelsLength = _curveInfo.length;
        var _savedLabelsLength = getSavedCurveName(_opts.index).length;
        var _numOfLabels = _newLabelsLength + _savedLabelsLength;
        var _width = 0;
        var _height = _numOfLabels * 20;
        
        if (_numOfLabels === 0) {
            $("#" + _opts.divs.bodyLabel).css('display', 'none');
        } else {
            //TODO: this width also is calculated by maximum name length, need to 
            //change later
            for (var i = 0; i < _newLabelsLength; i++) {
                if (_curveInfo[i].name.length * 10 > _width) {
                    _width = _curveInfo[i].name.length * 10;
                }
            }

            for (var key in _savedCurveInfo) {
                if (_savedCurveInfo[key].name * 10 > _width) {
                    _width = _savedCurveInfo[key].name;
                }
            }

            _width += 45;

            $("#" + _opts.divs.bodyLabel + " svg").remove();

            if (_savedLabelsLength > 0) {
                _height += 20;
            }

            var _svg = d3.select("#" + _opts.divs.bodyLabel)
                    .append("svg")
                    .attr('width', _width)
                    .attr("height", _height);

            drawNewLabels(_opts, _svg, 0, _width);

            if (_savedLabelsLength > 0) {
                //separator's height is 60px;
                drawSeparator(_svg, _newLabelsLength, _width);
                drawSavedLabels(_opts, _svg, _newLabelsLength + 1, _width);
            }
        }
    }

    //Draw text and black line between new labels and saved labels
    function drawSeparator(_svg, _index, _width) {
        var _g = _svg.append("g").attr('transform', 'translate(0, ' + (_index * 20) + ')');

        _g.append("text")
                .attr('x', _width / 2)
                .attr('y', 12)
                .attr('fill', 'black')
                .attr('text-anchor', 'middle')
                .attr('class', 'study-view-survival-label-font-1')
                .text('Saved Curves');

        _g.append("line")
                .attr('x1', 0)
                .attr('y1', 16)
                .attr('x2', _width)
                .attr('y2', 16)
                .attr('stroke', 'black')
                .attr('stroke-width', '2px');
    }
    //Draw saved labels if have any
    function drawSavedLabels(_opts, _svg, _startedIndex, _svgWidth) {
        var _savedLabelsLength = getSavedCurveName(_opts.index).length;

        if (_savedLabelsLength > 0) {
            var _index = 0;
            for (var key in savedCurveInfo) {
                drawLabelBasicComponent(_opts, _svg, _index + _startedIndex, savedCurveInfo[key].color, savedCurveInfo[key].name, 'close', _svgWidth);
                _index++;
            }
        }
    }

    //Icon type has: pin or close
    function drawLabelBasicComponent(_opts, _svg, _index, _color, _textName, _iconType, _svgWidth) {
        var _showedName = _textName.substring(0, 1) + _textName.substring(1).toLowerCase();
        var _g = _svg.append("g").attr('transform', 'translate(0, ' + (_index * 20) + ')');
        var _textWidth = 0;

        _g.append("rect")
                .attr('width', 10)
                .attr('height', 10)
                .attr('fill', _color);

        _g.append("text")
                .attr('x', 15)
                .attr('y', 10)
                .attr('fill', 'black')
                .attr('font', '12px')
                .attr('id', 'survival_label_text_' + _opts.index + "_" + _index)
                .attr('value', _textName)
                .text(_textName);

        //TODO: Get the text length, ggbox does not work in here
        _textWidth = _textName.length * 9;

        if (_iconType === 'pin') {
            var _image = _g.append("image")
                    .attr('x', _svgWidth - 40)
                    .attr('y', '0')
                    .attr('height', '10px')
                    .attr('width', '10px');

            _image.attr('xlink:href', 'images/pushpin.svg');
            _image.attr('name', 'pin');

            _image = _g.append("image")
                    .attr('x', _svgWidth - 25)
                    .attr('y', '1')
                    .attr('height', '8px')
                    .attr('width', '8px');

            _image.attr('xlink:href', 'images/close.svg');
            _image.attr('name', 'close');

        } else if (_iconType === 'close') {
            var _image = _g.append("image")
                    .attr('x', _svgWidth - 25)
                    .attr('y', '1')
                    .attr('height', '8px')
                    .attr('width', '8px');

            _image.attr('xlink:href', 'images/close.svg');
            _image.attr('name', 'saved-close');
        } else {
            //TODO:
        }

        if (_showedName.length > 7) {
            var _qtip = jQuery.extend(true, {}, StudyViewBoilerplate.pieLabelQtip);

            _qtip.content.text = _textName;
            _qtip.position.my = "left bottom";
            _qtip.position.at = "top right";
            $('#survival_label_text_' + _opts.index + "_" + _index).qtip(_qtip);
        }
    }
    //Draw new curve labels including curve color, name and pin icon
    function drawNewLabels(_opts, _svg, _startedIndex, _svgWidth) {
        var _numOfLabels = curveInfo[_opts.index].length;
        for (var i = 0; i < _numOfLabels; i++) {
            drawLabelBasicComponent(_opts, _svg, i + _startedIndex, curveInfo[_opts.index][i].color, curveInfo[_opts.index][i].name, 'pin', _svgWidth);
        }
    }

    //Will be called when user pin/delete labeles
    function redrawLabel(_opts) {
        $("#" + _opts.divs.bodyLabel + " svg").remove();
        drawLabels(_opts);
        addEvents(_opts);
    }

    function createCurves(_plotsInfo, _data) {
        numOfPlots = Object.keys(_plotsInfo).length;
        plotsInfo = _plotsInfo;
        oData = _data;
        oDataLength = _data.length;
        
        for (var i = 0; i < numOfPlots; i++) {
            plotBasicFuncs(i);
        }
        initStatus = true;
    }

    function plotBasicFuncs(_index) {
        var _casesInfo;
        
        aData[_index] = {};
        opts[_index] = {};
        aData[_index] = dataProcess(plotsInfo[_index]);
        opts[_index] = initOpts(_index);
        basicDiv(opts[_index]);
        _casesInfo = grouping(plotsInfo[_index].caseLists, '');
        initView(_casesInfo, aData[_index], opts[_index]);
        drawLabels(opts[_index]);
        addEvents(opts[_index]);
    }

    return {
        init: function(_caseLists, _data) {
            var _plotsInfo = {
                '0': {
                    name: "Overall Survival Status",
                    property: ["OS_MONTHS", "OS_STATUS"],
                    status: ["LIVING", "DECEASED"],
                    caseLists: _caseLists
                },
                '1': {
                    name: "Disease Free Survival Status",
                    property: ["DFS_MONTHS", "DFS_STATUS"],
                    status: ["DISEASEFREE", "RECURRED/PROGRESSED"],
                    caseLists: _caseLists
                }
            };
            
            initParams();
            createCurves(_plotsInfo, _data);
        },
        getInitStatus: getInitStatus,
        redraw: redraw,
        redrawLabel: redrawLabel
    };
})();