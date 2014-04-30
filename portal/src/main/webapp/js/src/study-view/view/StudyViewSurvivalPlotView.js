//TODO: Colors have conflicts when user save SELECTED_CASES/UNSELECTED_CALSES/ALL_CASES

/*
 * @author  Hongxin Zhang
 * @date    Apr. 2014
 */

var StudyViewSurvivalPlotView = (function() {
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
        if (savedCurveInfo.hasOwnProperty(_id)) {
            return Object.keys(savedCurveInfo[_id]);
        } else {
            return [];
        }
    }

    function getInitStatus() {
        return initStatus;
    }

    function addEvents(_plotIndex) {
        var _opts = opts[_plotIndex],
            _title = $("#" + _opts.divs.main + " charttitleh4").text();

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
                nameCurveDialog(this, saveCurveInfoFunc, _plotIndex);

            } else if ($(this).attr('name') === 'close') {
                var _parent = $(this).parent(),
                        _name = $(_parent).find('text').attr('value'),
                        _color = $(_parent).find('rect').attr('fill'),
                        _index = $(this).parent().index();

                $(_parent).remove();
                removeCurveFunc(_index, _plotIndex);
                redrawLabel(_plotIndex);
                survivalPlot[_plotIndex].removeCurve(_color.toString().substring(1) + "-" + _plotIndex);
            } else if ($(this).attr('name') === 'saved-close') {
                var _parent = $(this).parent(),
                        _name = $(_parent).find('text').attr('value');

                $(_parent).remove();
                undoSavedCurve(_name, _plotIndex);
                removeSavedCurveFunc(_name, _plotIndex);
                redrawLabel(_plotIndex);
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
                highlightCurve(_rectColor.substring(1) + "-" + _plotIndex);
            } else {
                $(_text).css('fill', 'black');
                resetCurve(_rectColor.substring(1) + "-" + _plotIndex);
            }

        });

        $("#" + _opts.divs.pdf).unbind('submit');
        $("#" + _opts.divs.pdf).submit(function() {
            setSVGElementValue(_opts.divs.bodySvg,
                    _opts.divs.pdfValue, _plotIndex, _title);
        });
        $("#" + _opts.divs.svg).unbind('submit');
        $("#" + _opts.divs.svg).submit(function() {
            setSVGElementValue(_opts.divs.bodySvg,
                    _opts.divs.svgValue, _plotIndex, _title);
        });

        $("#" + _opts.divs.menu).unbind("click");
        $("#" + _opts.divs.menu).click(function() {
            var _svgWidth = 0,
                    _label = $("#" + _opts.divs.bodyLabel),
                    _display = _label.css('display');

            if (_display === "none") {
                StudyViewUtil.changePosition(
                        '#' + _opts.divs.main,
                        '#' + _opts.divs.bodyLabel,
                        "#dc-plots");
                $('#' + _opts.divs.bodyLabel).children('float', '');
                _label.css('display', 'block');
                _svgWidth = $("#" + _opts.divs.bodyLabel + " svg").width();
                $("#" + _opts.divs.bodyLabel).width(_svgWidth + 15);
            } else {
                _label.css('display', 'none');
            }
        });

        if ($("#" + _opts.divs.bodyLabel).css('display') === 'block') {
            var _svgWidth = $("#" + _opts.divs.bodyLabel + " svg").width();
            $("#" + _opts.divs.bodyLabel).width(_svgWidth + 15);
        }

        $("#" + _opts.divs.body).css('opacity', '1');
        $("#" + _opts.divs.loader).css('display', 'none');
    }

    function setSVGElementValue(_svgParentDivId, _idNeedToSetValue, _plotIndex, _title) {
        var _svgElement, _svgLabels, _svgTitle,
                _labelTextMaxLength = 0,
                _numOfLabels = 0,
                _svgWidth = 360,
                _svgheight = 360;

        _svgElement = $("#" + _svgParentDivId + " svg").html();
        _svgLabels = $("#" + opts[_plotIndex].divs.bodyLabel + " svg");

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

        _svgWidth += _labelTextMaxLength * 14;

        if (_svgheight < _numOfLabels * 20) {
            _svgheight = _numOfLabels * 20 + 40;
        }

        _svgLabels = _svgLabels.html();

        _svgTitle = "<g><text text-anchor='middle' x='210' y='30' " +
                "style='font-weight:bold'>" + _title + "</text></g>";

        _svgElement = "<svg width='" + _svgWidth + "px' height='" + _svgheight + "px' style='font-size:14px'>" +
                _svgTitle + "<g transform='translate(0,40)'>" +
                _svgElement + "</g><g transform='translate(370,50)'>" +
                _svgLabels + "</g></svg>";
        $("#" + _idNeedToSetValue).val(_svgElement);

        redrawLabel(_plotIndex);
        //The style has been reset because of the addEvents function, so we
        //need to change the related components manully 
        $("#" + opts[_plotIndex].divs.header).css('display', 'block');
        $("#" + opts[_plotIndex].divs.main + " .study-view-drag-icon").css('display', 'block');
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
    function saveCurveInfoFunc(_this, _plotIndex) {
        var _selectedIndex = $($(_this).parent()).index(),
                _selectedCurveInfo = curveInfo[_plotIndex][_selectedIndex];
        if (!savedCurveInfo.hasOwnProperty(_plotIndex)) {
            savedCurveInfo[_plotIndex] = {};
        }
        /*
         if (StudyViewUtil.arrayFindByValue(uColor, _selectedCurveInfo.color)) {
         var _color = colorSelection('');
         
         //If no more color available, have to set the color here
         if (!_color) {
         _color = '#111111';
         }
         var _data = uColorCurveData[_selectedCurveInfo.color];
         
         survivalPlot[_opts.index].removeCurve(_selectedCurveInfo.color.toString().substring(1));
         _data.settings.line_color = _color;
         _data.settings.mouseover_color = _color;
         _data.settings.curveId = _color.toString().substring(1);
         survivalPlot[_opts.index].addCurve(_data);
         _selectedCurveInfo.color = _color;
         }*/
        savedCurveInfo[_plotIndex][_selectedCurveInfo.name] = _selectedCurveInfo;
        removeElement($(_this).parent());

        //After saving curve, the related curve info should be delete from 
        //curvesInfo
        StudyViewUtil.arrayDeleteByIndex(curveInfo[_plotIndex], _selectedIndex);
        redrawLabel(_plotIndex);
    }

    function removeElement(_this) {
        $(_this).remove();
    }

    //Move saved curve infomation back to curveInfo
    function undoSavedCurve(_curveName, _plotIndex) {
        var _targetCurve = savedCurveInfo[_plotIndex][_curveName];
        curveInfo[_plotIndex].push(_targetCurve);
    }

    function removeSavedCurveFunc(_curveName, _plotIndex) {
        delete savedCurveInfo[_plotIndex][_curveName];
    }

    function removeCurveFunc(_index, _plotIndex) {
        curveInfo[_plotIndex].splice(_index, 1);
    }

    //When user click pin icon, this dialog will be popped up and remind user
    //input the curve name.
    function nameCurveDialog(_this, _callBackFunc, _plotIndex) {
        var _parent = $(_this).parent(),
            _value = $(_parent).find("text:first").attr('value'),
            _qtipContent = '<input type="text" style="float:left" value="'+
                _value+'"/><button style="float:left">OK</button>';
        
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
                            curveInfo[_plotIndex][$($(_this).parent()).index()].name = _curveName;
                            _callBackFunc(_this, _plotIndex);
                        }
                        //Set to True: call .hide() before destroy
                        api.destroy(true);
                    });
                }
            }
        });
    }

    /* 
     * Generate survival plot division 
     * @param {object} _opt
     */
    function createDiv(_opt) {
        var _div = "<div id='" + _opt.divs.main +
                "' class='study-view-dc-chart w2 h1half study-view-survival-plot'>" +
                "<div id='" + _opt.divs.headerWrapper +
                "' class='study-view-survival-plot-header-wrapper'>" +
                "<chartTitleH4 value='" + _opt.title + "' id='" + _opt.divs.title +
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
                "<div id='" + _opt.divs.loader + "' class='study-view-loader'>" +
                "<img src='images/ajax-loader.gif'/></div>" +
                "<div id='" + _opt.divs.body + "' class='study-view-survival-plot-body'>" +
                "<div id='" + _opt.divs.bodySvg + "' style='float:left'></div>" +
                "<div id='" + _opt.divs.bodyLabel +
                "' class='study-view-survival-plot-body-label'></div>" +
                "</div></div>";

        $("#study-view-charts").append(_div);
    }

    /*
     Convert input data into survivalProxy required format
     @param  _plotInfo   the plot basic information
     ---- format----
     { 
     identifier1: {
     name: '',
     property: [''],
     status: [['']],
     caseLists: {
     identifier1: {
     caseIds: [],
     color: ''
     },
     identifier1: 
     }
     },
     identifier1:
     }
     */
    function dataProcess(_plotInfo) {
        var _numOfValuedCase = 0;
        var _plotData = {};

        for (var i = 0; i < oDataLength; i++) {
            if (oData[i].hasOwnProperty(_plotInfo.property[0]) && oData[i].hasOwnProperty(_plotInfo.property[1])) {
                var _time = oData[i][_plotInfo.property[0]],
                        _status = oData[i][_plotInfo.property[1]].toUpperCase(),
                        _caseID = oData[i].CASE_ID;
                _plotData[_caseID] = {};

                _plotData[_caseID].case_id = _caseID;

                if (_plotInfo.status[0].indexOf(_status) !== -1) {
                    _plotData[_caseID].status = '0';
                } else if (_plotInfo.status[1].indexOf(_status) !== -1) {
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

    /*
     * Put all cases into groups based on keys in _seperateAttr, if no _seoerateAttr
     * inputted, the _casesInfo will not be changed.
     * 
     * @param {object}  _casesInfo cases information object, if _seperateAttr
     *                  initialized, the _casesInfo only include group name and
     *                  group color. If not initalzied, it includes all information.
     * @param {string} _seperateAttr    The unique attribute, like 'OS_MONTHS'
     */
    function grouping(_casesInfo, _seperateAttr) {
        //If seperation attribute has been defined, the data will be put in
        //different group based on this attribute.
        var _trimedCasesInfo =  jQuery.extend(true, {}, _casesInfo);
        
        if (_seperateAttr !== '' && _seperateAttr) {
            for (var i = 0; i < oDataLength; i++) {
                var _arr = oData[i][_seperateAttr],
                        _caseID = oData[i].CASE_ID;
                if (!_trimedCasesInfo.hasOwnProperty(_arr)) {
                    if (_trimedCasesInfo.hasOwnProperty('NA')) {
                        _trimedCasesInfo['NA'].caseIds.push(_caseID);
                    } else {
                        //TODO: User may only draw survial based on current groups.
                        //StudyViewUtil.echoWarningMessg("Unexpected attribute: " + _arr);
                    }
                } else {
                    _trimedCasesInfo[_arr].caseIds.push(_caseID);
                }
            }
        }
        return _trimedCasesInfo;
    }

    /*
     * Initilize all options for current survival plot
     * @param _index The survival plot identifier
     * @return _opts The initilized option object
     */
    function initOpts(_index) {
        var _opts = {};

        _opts.index = _index;
        _opts.title = plotsInfo[_index].name;
        _opts.divs = {};
        _opts.divs.main = "study-view-survival-plot-" + _index;
        _opts.divs.title = "study-view-survival-pot-title-" + _index;
        _opts.divs.header = "study-view-survival-plot-header-" + _index;
        _opts.divs.headerWrapper = "study-view-survival-plot-header-wrapper-" + _index;
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
        _opts.divs.loader = "study-view-survival-plot-loader-" + _index;

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
        _opts.plot.style.axisX_title_pos_x = 210;
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

    function redrawView(_plotIndex, _casesInfo) {
        var _color = "";

        inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        //confidenceIntervals = new ConfidenceIntervals();
        curveInfo[_plotIndex] = [];

        for (var key in _casesInfo) {
            var instanceData = new SurvivalCurveProxy();
            instanceData.init(aData[_plotIndex], _casesInfo[key].caseIds, kmEstimator, logRankTest);
            
            //If no data return, will no draw this curve
            if (instanceData.getData().length > 0) {
                var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                _color = _casesInfo[key].color;

                if (_color) {
                    instanceSettings.line_color = _color;
                    instanceSettings.mouseover_color = _color;
                    instanceSettings.curveId = _color.toString().substring(1) + "-" + _plotIndex;
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

                    curveInfo[_plotIndex].push(_curveInfoDatum);
                } else {
                    alert("Sorry, you can not create more than 30 curves.");
                    break;
                }
            }
        }

        var inputArrLength = inputArr.length;
        for (var i = 0; i < inputArrLength; i++) {
            survivalPlot[_plotIndex].addCurve(inputArr[i]);
        }
    }

    /*
     * Initialize survival plot by calling survivalCurve component
     * 
     * @param {object}  _casesInfo  Grouped cases information.
     * @param {object}  _data       The processed data by function dataprocess.
     * @param {object}  _plotIndex  The selected plot indentifier.
     */
    function initView(_casesInfo, _data, _plotIndex) {
        var _color = "",
                inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        //confidenceIntervals = new ConfidenceIntervals();   

        curveInfo[_plotIndex] = [];

        for (var key in _casesInfo) {
            var instanceData = new SurvivalCurveProxy();

            instanceData.init(_data, _casesInfo[key].caseIds, kmEstimator, logRankTest);
            //If no data return, will no draw this curve
            if (instanceData.getData().length > 0) {
                var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                _color = _casesInfo[key].color;
                if (_color) {
                    instanceSettings.line_color = _color;
                    instanceSettings.mouseover_color = _color;
                    instanceSettings.curveId = _color.toString().substring(1) + "-" + _plotIndex;
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
                    curveInfo[_plotIndex].push(_curveInfoDatum);
                } else {
                    alert("Sorry, you can not create more than 30 curves.");
                    break;
                }
            }
        }

        //We disabled pvalue calculation in here
        survivalPlot[_plotIndex] = new SurvivalCurve();
        survivalPlot[_plotIndex].init(inputArr, opts[_plotIndex].plot);


        $("#" + opts[_plotIndex].divs.pdfName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".pdf");
        $("#" + opts[_plotIndex].divs.svgName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".svg");
    }

    
    /**
     * Redraw curves based on selected cases and unselected cases
     * 
     * @param {type} _casesInfo     the same as _casesInfo in initView
     * @param {type} _selectedAttr  the selected attribute which will be used to
     *                              seperate cases. Can be false or ''.
     */
    function redraw(_casesInfo, _selectedAttr) {
        for (var j = 0; j < numOfPlots; j++) {
            var _curveInfoLength = curveInfo[j].length;
            for (var i = 0; i < _curveInfoLength; i++) {
                survivalPlot[j].removeCurve(curveInfo[j][i].color.toString().substring(1) + "-" + opts[j].index);
            }

            kmEstimator = "";
            logRankTest = "";
            delete curveInfo[j];

            var _tmpCasesInfo = grouping(_casesInfo, _selectedAttr);
            redrawView(opts[j].index, _tmpCasesInfo);
            drawLabels(opts[j].index);
            if (typeof _selectedAttr !== 'undefined') {
                StudyViewUtil.changeTitle("#" + opts[j].divs.main + " chartTitleH4", _selectedAttr, false);
            }
            addEvents(opts[j].index);
        }
    }
    
    /**
     * The main function to draw survival plot labels.
     * 
     * @param {type} _plotIndex the current selected plot indentifier.
     */
    function drawLabels(_plotIndex) {
        var _svg = '',
            _curveInfo = curveInfo[_plotIndex],
            _savedCurveInfo = savedCurveInfo[_plotIndex],
            _newLabelsLength = _curveInfo.length,
            _savedLabelsLength = getSavedCurveName(_plotIndex).length,
            _numOfLabels = _newLabelsLength + _savedLabelsLength,
            _width = 0,
            _height = _numOfLabels * 20 - 5;

        if (_numOfLabels === 0) {
            $("#" + opts[_plotIndex].divs.bodyLabel).css('display', 'none');
        } else {
            //TODO: this width is calculated by maximum name length multiply
            //a constant, need to be changed later
            for (var i = 0; i < _newLabelsLength; i++) {
                if (_curveInfo[i].name.length * 10 > _width) {
                    _width = _curveInfo[i].name.length * 10;
                }
            }

            for (var key in _savedCurveInfo) {
                if (_savedCurveInfo[key].name.length * 10 > _width) {
                    _width = _savedCurveInfo[key].name.length * 10;
                }
            }

            //_width += 45;
            _width += 30;
            
            $("#" + opts[_plotIndex].divs.bodyLabel + " svg").remove();

            if (_savedLabelsLength > 0) {
                _height += 20;
            }

            _svg = d3.select("#" + opts[_plotIndex].divs.bodyLabel)
                    .append("svg")
                    .attr('width', _width)
                    .attr("height", _height);

            drawNewLabels(_plotIndex, _svg, 0, _width);

            if (_savedLabelsLength > 0) {
                //separator's height is 20px;
                drawSeparator(_svg, _newLabelsLength * 20, _width);
                drawSavedLabels(_plotIndex, _svg, (_newLabelsLength + 1) * 20, _width);
            }
        }
    }
    
    /**
     * Draw 'Saved Curves' and black line between new labels and saved labels
     * 
     * @param _svg the svg container.
     * @param _yPosition the vertical position of this separator
     * @param _width the width of svg
     */
    function drawSeparator(_svg, _yPosition, _width) {
        var _g = _svg.append("g").attr('transform', 'translate(0, ' + _yPosition + ')');

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
    
    /**
     * Draw saved labels if have any
     * 
     * @param _plotIndex the plot identifier
     * @param _svg
     * @param _startedIndex
     * @param _svgWidth
     */
    function drawSavedLabels(_plotIndex, _svg, _startedIndex, _svgWidth) {
        var _savedLabelsLength = getSavedCurveName(_plotIndex).length,
                _savedCurveInfo = savedCurveInfo[_plotIndex];

        if (_savedLabelsLength > 0) {
            var _index = 0;
            for (var key in _savedCurveInfo) {
                drawLabelBasicComponent(_plotIndex, _svg, _index + _startedIndex, _savedCurveInfo[key].color, _savedCurveInfo[key].name, 'close', _svgWidth);
                _index++;
            }
        }
    }

    /**
     * Draw basic label componets:  one rect, one lable name, 
     *                              icons(pin or delete icons)
     * 
     * @param {type} _plotIndex the current selected plot identifier.
     * @param {type} _svg       the svg where to draw labels.
     * @param {type} _index     the label index in current plot.
     * @param {type} _color     the label color.
     * @param {type} _textName  the label name.
     * @param {type} _iconType  'pin' or 'close', pin will draw pin icon and
     *                          delete icon, close will only draw delete icon.
     * @param {type} _svgWidth  the svg width.
     */
    function drawLabelBasicComponent(_plotIndex, _svg, _index, _color, _textName, _iconType, _svgWidth) {
        var _g = _svg.append("g").attr('transform', 'translate(0, ' + (_index * 20) + ')');
       
        _g.append("rect")
                .attr('width', 10)
                .attr('height', 10)
                .attr('fill', _color);

        _g.append("text")
                .attr('x', 15)
                .attr('y', 10)
                .attr('fill', 'black')
                .attr('font', '12px')
                .attr('id', 'survival_label_text_' + _plotIndex + "_" + _index)
                .attr('value', _textName)
                .text(_textName);

        if (_iconType === 'pin') {
//            Temporary disable save curve function
//            var _image = _g.append("image")
//                    .attr('x', _svgWidth - 30)
//                    .attr('y', '0')
//                    .attr('height', '10px')
//                    .attr('width', '10px');
//
//            _image.attr('xlink:href', 'images/pushpin.svg');
//            _image.attr('name', 'pin');

            var _image = _g.append("image")
                    .attr('x', _svgWidth - 15)
                    .attr('y', '1')
                    .attr('height', '8px')
                    .attr('width', '8px');

            _image.attr('xlink:href', 'images/close.svg');
            _image.attr('name', 'close');

        } else if (_iconType === 'close') {
            var _image = _g.append("image")
                    .attr('x', _svgWidth - 15)
                    .attr('y', '1')
                    .attr('height', '8px')
                    .attr('width', '8px');

            _image.attr('xlink:href', 'images/close.svg');
            _image.attr('name', 'saved-close');
        } else {
            //TODO:
        }
    }
    
    /**
     * Calling drawLabelBasicComponent to draw all new labels including 
     * curve color, name and icontype = 'pin'.
     * 
     * @param {type} _plotIndex     the selected plot identifier.
     * @param {type} _svg           the svg where to draw labels.
     * @param {type} _startedIndex  
     * @param {type} _svgWidth      the svg width.
     */
    function drawNewLabels(_plotIndex, _svg, _startedIndex, _svgWidth) {
        var _numOfLabels = curveInfo[_plotIndex].length;
        for (var i = 0; i < _numOfLabels; i++) {
            drawLabelBasicComponent(
                    _plotIndex, 
                    _svg, 
                    i + _startedIndex, 
                    curveInfo[_plotIndex][i].color, 
                    curveInfo[_plotIndex][i].name, 
                    'pin', 
                    _svgWidth);
        }
    }

    
    /**
     * Will be called when user pin/delete labeles
     * @param {type} _plotIndex
     */
    function redrawLabel(_plotIndex) {
        $("#" + opts[_plotIndex].divs.bodyLabel + " svg").remove();
        drawLabels(_plotIndex);
        addEvents(_plotIndex);
    }
    
    /**
     * 
     * @param {type} _plotsInfo
     * @param {type} _data      all data before prcessing, and clone it to oData.
     */
    function createCurves(_plotsInfo, _data) {
        numOfPlots = Object.keys(_plotsInfo).length;
        plotsInfo = _plotsInfo;
        oData = _data;
        oDataLength = _data.length;

        for (var i = 0; i < numOfPlots; i++) {
            plotBasicFuncs(i);
        }

        //The initStatus will be used from other view
        initStatus = true;
    }

    function plotBasicFuncs(_index) {
        var _casesInfo;

        aData[_index] = {};
        opts[_index] = {};
        aData[_index] = dataProcess(plotsInfo[_index]);

        //If no data returned, this survival plot should not be initialized.
        if (Object.keys(aData[_index]).length !== 0) {
            opts[_index] = initOpts(_index);
            createDiv(opts[_index]);
            _casesInfo = grouping(plotsInfo[_index].caseLists, '');
            initView(_casesInfo, aData[_index], _index);
            drawLabels(_index);
            addEvents(_index);
        } else {
            console.log("No data for Survival Plot: " + _index);
        }
    }

    function getNumOfPlots() {
        return numOfPlots;
    }

    function detectLabelPosition() {
        for (var i = 0; i < numOfPlots; i++) {
            if ($("#" + opts[i].divs.bodyLabel).css('display') === 'block') {
                StudyViewUtil.changePosition(
                        '#' + opts[i].divs.main,
                        '#' + opts[i].divs.bodyLabel,
                        "#dc-plots");
            }
        }
    }

    return {
        init: function(_plotsInfo, _data) {
            createCurves(_plotsInfo, _data);
        },
        getInitStatus: getInitStatus,
        redraw: redraw,
        redrawLabel: redrawLabel,
        getNumOfPlots: getNumOfPlots,
        detectLabelPosition: detectLabelPosition
    };
})();