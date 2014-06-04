//TODO: Colors have conflicts when user save SELECTED_CASES/UNSELECTED_CALSES/ALL_CASES

/*
 * @author  Hongxin Zhang
 * @date    Apr. 2014
 */

/*
 * 
 * Save curve function has been disabled.
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
//    var uColor = ["#000000", "#dc3912", "#2986e2"];
//    var reserveName = ["ALL_CASES", "SELECTED_CASES", "UNSELECTED_CASES"];
//    var reserveDisplayName = ["All cases", "Selected cases", "Unselected cases"];

    /*Store data for unique curves: the color of these will be changed when user
     * saving them, in that case, the survival plot needs to redraw this curve*/
//    var uColorCurveData = {};

    //Saved curve information is identified based on the curve name,
    //in other words, the name of each curve is identical. 
    var savedCurveInfo = {};

    /**
     * @param {type} _id cureve id
     * @returns {Array} return name array of saved curves
     */
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
    
    /**
     * Containing all jQuery related functions
     * @param {type} _plotKey the plot key
     */
    function addEvents(_plotKey) {
        var _opts = opts[_plotKey],
            _title = $("#" + _opts.divs.main + " charttitleh4").text();

        if (!initStatus) {
            StudyViewUtil.showHideDivision(
                    '#' + _opts.divs.main,
                    '#' + _opts.divs.header
                    );
        }
//        $("#" + _opts.divs.pdf).unbind('submit');
//        $("#" + _opts.divs.pdf).submit(function() {
//            setSVGElementValue(_opts.divs.bodySvg,
//                    _opts.divs.pdfValue, _plotKey, _title);
//        });
//        $("#" + _opts.divs.svg).unbind('submit');
//        $("#" + _opts.divs.svg).submit(function() {
//            setSVGElementValue(_opts.divs.bodySvg,
//                    _opts.divs.svgValue, _plotKey, _title);
//        });

//        $("#" + _opts.divs.menu).unbind("click");
//        $("#" + _opts.divs.menu).click(function() {
//            var _svgWidth = 0,
//                    _label = $("#" + _opts.divs.bodyLabel),
//                    _display = _label.css('display');
//
//            if (_display === "none") {
//                StudyViewUtil.changePosition(
//                        '#' + _opts.divs.main,
//                        '#' + _opts.divs.bodyLabel,
//                        "#dc-plots");
//                $('#' + _opts.divs.bodyLabel).children('float', '');
//                _label.css('display', 'block');
//                _svgWidth = $("#" + _opts.divs.bodyLabel + " svg").width();
//                $("#" + _opts.divs.bodyLabel).width(_svgWidth + 15);
//            } else {
//                _label.css('display', 'none');
//            }
//        });

//        if ($("#" + _opts.divs.bodyLabel).css('display') === 'block') {
//            var _svgWidth = $("#" + _opts.divs.bodyLabel + " svg").width();
//            $("#" + _opts.divs.bodyLabel).width(_svgWidth + 15);
//        }

        $("#" + _opts.divs.body).css('opacity', '1');
        $("#" + _opts.divs.loader).css('display', 'none');
        
        $('#' + _opts.divs.downloadIcon).qtip('destroy', true);
        $('#' + _opts.divs.downloadIcon).qtip({
            id: "#" + _opts.divs.downloadIcon + "-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show: {event: "click", delay: 0},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'top center',at:'bottom center', viewport: $(window)},
            content: {
                text:   "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opts.divs.pdf + "'>" +
                        "<input type='hidden' name='svgelement' id='" + _opts.divs.pdfValue + "'>" +
                        "<input type='hidden' name='filetype' value='pdf'>" +
                        "<input type='hidden' id='" + _opts.divs.pdfName + "' name='filename' value=''>" +
                        "<input type='submit' style='font-size:10px' value='PDF'>" +
                        "</form>" +
                        "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opts.divs.svg + "'>" +
                        "<input type='hidden' name='svgelement' id='" + _opts.divs.svgValue + "'>" +
                        "<input type='hidden' name='filetype' value='svg'>" +
                        "<input type='hidden' id='" + _opts.divs.svgName + "' name='filename' value=''>" +
                        "<input type='submit' style='font-size:10px' value='SVG'>" +
                        "</form>"
            },
            events: {
                render: function(event, api) {

                    $("#" + _opts.divs.pdfName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".pdf");
                    $("#" + _opts.divs.svgName).val("Survival_Plot_result-" + StudyViewParams.params.studyId + ".svg");
    
                    $("#" + _opts.divs.pdf, api.elements.tooltip).submit(function() {
                        setSVGElementValue(_opts.divs.bodySvg,
                                _opts.divs.pdfValue, _plotKey, _title);
                    });
                    $("#" + _opts.divs.svg, api.elements.tooltip).submit(function() {
                        setSVGElementValue(_opts.divs.bodySvg,
                                _opts.divs.svgValue, _plotKey, _title);
                    });
//                    $("#study-view-scatter-plot-pdf", api.elements.tooltip).submit(function(){
//                        $("#study-view-scatter-plot-pdf-name").val("Scatter_Plot_result-"+ StudyViewParams.params.studyId +".pdf");
//                        setSVGElementValue("study-view-scatter-plot-body-svg",
//                            "study-view-scatter-plot-pdf-value",
//                            scatterPlotOptions,
//                            _title);
//                    });
//                    $("#study-view-scatter-plot-svg", api.elements.tooltip).submit(function(){
//                        $("#study-view-scatter-plot-svg-name").val("Scatter_Plot_result-"+ StudyViewParams.params.studyId +".svg");
//                        setSVGElementValue("study-view-scatter-plot-body-svg",
//                            "study-view-scatter-plot-svg-value",
//                            scatterPlotOptions,
//                            _title);
//                    });
                }
            }
        });
    }

    /**
     * Be used to create svg/pdf file
     * @param {type} _svgParentDivId    svg container
     * @param {type} _idNeedToSetValue  set the modified svg element value into
     *                                  this selected element
     * @param {type} _plotKey
     * @param {type} _title             the title appears above saved file 
     *                                  content, Exp. 'Scatter Plot'
     * @returns {undefined}
     */
    function setSVGElementValue(_svgParentDivId, _idNeedToSetValue, _plotKey, _title) {
        var _svgElement, _svgLabels, _svgTitle,
                _labelTextMaxLength = 0,
                _numOfLabels = 0,
                _svgWidth = 360,
                _svgheight = 360;

        _svgElement = $("#" + _svgParentDivId + " svg").html();
        _svgLabels = $("#" + opts[_plotKey].divs.bodyLabel + " svg");

        _svgLabels.find('image').remove();
        _svgLabels.find('text').each(function(i, obj) {
            var _value = $(obj).attr('oValue');

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
        $("#" + opts[_plotKey].divs.bodyLabel + " svg").remove();
        drawLabels(_plotKey);
        //The style has been reset because of the addEvents function, so we
        //need to change the related components manully 
        $("#" + opts[_plotKey].divs.header).css('display', 'block');
        $("#" + opts[_plotKey].divs.main + " .study-view-drag-icon").css('display', 'block');
    }

    function highlightCurve(_curveId) {
        var _hiddenDots = $("#" + _curveId + "-dots").find('path'),
            _hiddenDotsLength = _hiddenDots.length;
            
        for ( var i = 0; i < _hiddenDotsLength; i++) {
            $(_hiddenDots[i]).css('opacity', '.6');
        }
        $("#" + _curveId + "-line").css('stroke-width', '3px');
    }

    function resetCurve(_curveId) {
        var _hiddenDots = $("#" + _curveId + "-dots").find('path'),
            _hiddenDotsLength = _hiddenDots.length;
        
        for ( var i = 0; i < _hiddenDotsLength; i++) {
            $(_hiddenDots[i]).css('opacity', '0');
        }
        $("#" + _curveId + "-line").css('stroke-width', '');
    }

    //Save all related information with this curve. The saved curve(s) will be
    //showed again when redrawing survival plots
    function saveCurveInfoFunc(_this, _plotKey) {
        var _selectedIndex = $($(_this).parent()).index(),
                _selectedCurveInfo = curveInfo[_plotKey][_selectedIndex];
        if (!savedCurveInfo.hasOwnProperty(_plotKey)) {
            savedCurveInfo[_plotKey] = {};
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
        savedCurveInfo[_plotKey][_selectedCurveInfo.name] = _selectedCurveInfo;
        removeElement($(_this).parent());

        //After saving curve, the related curve info should be delete from 
        //curvesInfo
        StudyViewUtil.arrayDeleteByIndex(curveInfo[_plotKey], _selectedIndex);
        redrawLabel(_plotKey);
    }

    function removeElement(_this) {
        $(_this).remove();
    }

    //Move saved curve infomation back to curveInfo
    function undoSavedCurve(_curveName, _plotKey) {
        var _targetCurve = savedCurveInfo[_plotKey][_curveName];
        curveInfo[_plotKey].push(_targetCurve);
    }

    function removeSavedCurveFunc(_curveName, _plotKey) {
        delete savedCurveInfo[_plotKey][_curveName];
    }

    function removeCurveFunc(_index, _plotKey) {
        curveInfo[_plotKey].splice(_index, 1);
    }

    //When user click pin icon, this dialog will be popped up and remind user
    //input the curve name.
    function nameCurveDialog(_this, _callBackFunc, _plotKey) {
        var _parent = $(_this).parent(),
            _value = $(_parent).find("text:first").attr('oValue'),
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
                            curveInfo[_plotKey][$($(_this).parent()).index()].name = _curveName;
                            _callBackFunc(_this, _plotKey);
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
                "<chartTitleH4 oValue='" + _opt.title + "' id='" + _opt.divs.title +
                "' class='study-view-survival-plot-title'>" + _opt.title + "</chartTitleH4>" +
                "<div id='" + _opt.divs.header +
                "' class='study-view-survival-plot-header' style='float:right'>" +
//                "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opt.divs.pdf + "'>" +
//                "<input type='hidden' name='svgelement' id='" + _opt.divs.pdfValue + "'>" +
//                "<input type='hidden' name='filetype' value='pdf'>" +
//                "<input type='hidden' id='" + _opt.divs.pdfName + "' name='filename' value=''>" +
//                "<input type='submit' style='font-size:10px' value='PDF'>" +
//                "</form>" +
//                "<form style='display:inline-block; float:left; margin-right:5px' action='svgtopdf.do' method='post' id='" + _opt.divs.svg + "'>" +
//                "<input type='hidden' name='svgelement' id='" + _opt.divs.svgValue + "'>" +
//                "<input type='hidden' name='filetype' value='svg'>" +
//                "<input type='hidden' id='" + _opt.divs.svgName + "' name='filename' value=''>" +
//                "<input type='submit' style='font-size:10px' value='SVG'>" +
//                "</form>" +
//                "<img id='" + _opt.divs.menu + "' class='study-view-menu-icon' style='float:left; width:10px; height:10px;margin-top:4px; margin-right:4px;' class='study-view-menu-icon' src='images/menu.svg'/>" +
                "<img id='"+_opt.divs.downloadIcon+"' class='study-view-download-icon' src='images/in.svg'/>" +
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
                } else {
                    _plotData[_caseID].status = 'NA';
                }
                
                if (isNaN(_time)) {
                    _plotData[_caseID].months = 'NA';
                } else {
                    _plotData[_caseID].months = Number(_time);
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
        /*
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
        }*/
        return _trimedCasesInfo;
    }

    /*
     * Initilize all options for current survival plot
     * @param _index The survival plot identifier
     * @return _opts The initilized option object
     */
    function initOpts(_index, _key) {
        var _opts = {};

        _opts.index = _index;
        _opts.key = _key;
        _opts.title = plotsInfo[_key].name;
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
        _opts.divs.downloadIcon = "study-view-survival-download-icon-" + _index;

        //plot in _opts is for survival plot
        _opts.plot = jQuery.extend(true, {}, SurvivalCurveBroilerPlate);
        _opts.plot.text.xTitle = "Months Survival";
        _opts.plot.text.yTitle = "Surviving";
        _opts.plot.text.qTips.estimation = "Survival estimate";
        _opts.plot.text.qTips.censoredEvent = "Time of last observation";
        _opts.plot.text.qTips.failureEvent = "Time of death";
        _opts.plot.settings.canvas_width = 365;
        _opts.plot.settings.canvas_height = 310;
        _opts.plot.settings.chart_width = 290;
        _opts.plot.settings.chart_height = 250;
        _opts.plot.settings.chart_left = 70;
        _opts.plot.settings.chart_top = 5;
        _opts.plot.settings.include_legend = false;
        _opts.plot.settings.include_pvalue = false;
        _opts.plot.style.axisX_title_pos_x = 200;
        _opts.plot.style.axisX_title_pos_y = 295;
        _opts.plot.style.axisY_title_pos_x = -120;
        _opts.plot.style.axisY_title_pos_y = 20;
        _opts.plot.divs.curveDivId = "study-view-survival-plot-body-svg-" + _index;
        _opts.plot.divs.headerDivId = "";
        _opts.plot.divs.infoTableDivId = "study-view-survival-plot-table-" + _index;
        _opts.plot.text.infoTableTitles.total_cases = "#total cases";
        _opts.plot.text.infoTableTitles.num_of_events_cases = "#cases deceased";
        _opts.plot.text.infoTableTitles.median = "median months survival";

        return _opts;
    }

    function redrawView(_plotKey, _casesInfo) {
        var _color = "";
        
        inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        //confidenceIntervals = new ConfidenceIntervals();
        curveInfo[_plotKey] = [];

        for (var key in _casesInfo) {
            var instanceData = new SurvivalCurveProxy();
            instanceData.init(aData[_plotKey], _casesInfo[key].caseIds, kmEstimator, logRankTest);

            //If no data return, will no draw this curve
            if (instanceData.getData().length > 0) {
                var instanceSettings = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
                _color = _casesInfo[key].color;

                if (_color) {
                    instanceSettings.line_color = _color;
                    instanceSettings.mouseover_color = _color;
                    instanceSettings.curveId = _color.toString().substring(1) + "-" + _plotKey;
                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);

//                    if (StudyViewUtil.arrayFindByValue(reserveName, key)) {
//                        uColorCurveData[uColor[reserveName.indexOf(key)]] = instance;
//                    }

                    var _curveInfoDatum = {
                        name: key,
                        color: _color,
                        caseList: _casesInfo[key].color,
                        data: instance
                    };

                    curveInfo[_plotKey].push(_curveInfoDatum);
                } else {
                    //alert("Sorry, you can not create more than 30 curves.");
                    //break;
                }
            }
        }

        var inputArrLength = inputArr.length;
        for (var i = 0; i < inputArrLength; i++) {
            survivalPlot[_plotKey].addCurve(inputArr[i]);
        }
    }

    /*
     * Initialize survival plot by calling survivalCurve component
     * 
     * @param {object}  _casesInfo  Grouped cases information.
     * @param {object}  _data       The processed data by function dataprocess.
     * @param {object}  _plotIndex  The selected plot indentifier.
     */
    function initView(_casesInfo, _data, _plotKey) {
        var _color = "",
                inputArr = [];
        kmEstimator = new KmEstimator();
        logRankTest = new LogRankTest();
        //confidenceIntervals = new ConfidenceIntervals();   

        curveInfo[_plotKey] = [];

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
                    instanceSettings.curveId = _color.toString().substring(1) + "-" + _plotKey;
                    //Assemble the input
                    var instance = {};
                    instance.data = instanceData;
                    instance.settings = instanceSettings;
                    inputArr.push(instance);

//                    if (StudyViewUtil.arrayFindByValue(reserveName, key)) {
//                        uColorCurveData[uColor[reserveName.indexOf(key)]] = instance;
//                    }

                    var _curveInfoDatum = {
                        name: key,
                        color: _color,
                        caseList: _casesInfo[key].caseIds,
                        data: instance
                    };
                    curveInfo[_plotKey].push(_curveInfoDatum);
                } else {
                    alert("Sorry, you can not create more than 30 curves.");
                    break;
                }
            }
        }

        //We disabled pvalue calculation in here
        survivalPlot[_plotKey] = new SurvivalCurve();
        survivalPlot[_plotKey].init(inputArr, opts[_plotKey].plot);
    }

    
    /**
     * Redraw curves based on selected cases and unselected cases
     * 
     * @param {type} _casesInfo     the same as _casesInfo in initView
     * @param {type} _selectedAttr  the selected attribute which will be used to
     *                              seperate cases. Can be false or ''.
     */
    function redraw(_casesInfo, _selectedAttr) {
        for (var key in plotsInfo) {
            var _curveInfoLength = curveInfo[key].length;
            for (var i = 0; i < _curveInfoLength; i++) {
                survivalPlot[key].removeCurve(curveInfo[key][i].color.toString().substring(1) + "-" + key);
            }
            
            $("#" + opts[key].divs.main).qtip('destroy', true);
            
            kmEstimator = "";
            logRankTest = "";
            delete curveInfo[key];

            var _tmpCasesInfo = grouping(_casesInfo, _selectedAttr[0]);
            redrawView(key, _tmpCasesInfo);
            drawLabels(key);
            if (typeof _selectedAttr !== 'undefined') {
                StudyViewUtil.changeTitle("#" + opts[key].divs.main + " chartTitleH4", _selectedAttr[1], false);
            }
            addEvents(key);
        }
    }
    
    /**
     * The main function to draw survival plot labels.
     * 
     * @param {type} _plotKey the current selected plot indentifier.
     */
    function drawLabels(_plotKey) {
        var _svg = '',
            _curveInfo = curveInfo[_plotKey],
            _savedCurveInfo = savedCurveInfo[_plotKey],
            _newLabelsLength = _curveInfo.length,
            _savedLabelsLength = getSavedCurveName(_plotKey).length,
            _numOfLabels = _newLabelsLength + _savedLabelsLength,
            _width = 0,
            _height = _numOfLabels * 20 - 5;
        
        $("#" + opts[_plotKey].divs.main + " svg").qtip('destroy', true);
       
        if (_numOfLabels === 0) {
            $("#" + opts[_plotKey].divs.bodyLabel).css('display', 'none');
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
            
            $("#" + opts[_plotKey].divs.bodyLabel + " svg").remove();

            if (_savedLabelsLength > 0) {
                _height += 20;
            }

            _svg = d3.select("#" + opts[_plotKey].divs.bodyLabel)
                    .append("svg")
                    .attr('width', _width)
                    .attr("height", _height);

            drawNewLabels(_plotKey, _svg, 0, _width);

            if (_savedLabelsLength > 0) {
                //separator's height is 20px;
                drawSeparator(_svg, _newLabelsLength * 20, _width);
                drawSavedLabels(_plotKey, _svg, (_newLabelsLength + 1) * 20, _width);
            }
        }
        
        $("#" + opts[_plotKey].divs.main + " svg").qtip({
            id: opts[_plotKey].divs.bodyLabel + "-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow forceZindex'},
            show: {event: "mouseover", delay: 0},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'left top',at:'top right', viewport: $(window)},
            content: $("#" + opts[_plotKey].divs.bodyLabel).html(),
            events: {
                render: function(event, api) {
                    $('svg image', api.elements.tooltip).hover(function() {
                        $(this).css('cursor', 'pointer');
                    });

                    $('svg image', api.elements.tooltip).unbind('click');
                    $('svg image', api.elements.tooltip).click(function() {
                        if ($(this).attr('name') === 'pin') {

                            //The following functions will be excuted after user inputting
                            //the curve name, so we need to give it a call back function.
                            nameCurveDialog(this, saveCurveInfoFunc, _plotKey);

                        } else if ($(this).attr('name') === 'close') {
                            var _parent = $(this).parent(),
                                    _name = $(_parent).find('text').attr('oValue'),
                                _color = $(_parent).find('rect').attr('fill'),
                                _index = $(this).parent().index();

                            $(_parent).remove();
                            removeCurveFunc(_index, _plotKey);
                            redrawLabel(_plotKey);
                            survivalPlot[_plotKey].removeCurve(_color.toString().substring(1) + "-" + _plotKey);
                        } else if ($(this).attr('name') === 'saved-close') {
                            var _parent = $(this).parent(),
                                _name = $(_parent).find('text').attr('oValue');

                            $(_parent).remove();
                            undoSavedCurve(_name, _plotKey);
                            removeSavedCurveFunc(_name, _plotKey);
                            redrawLabel(_plotKey);
                        } else {
                            //TODO: Add more function
                        }
                    });

                    //$('#' + _opts.divs.main + ' svg rect').unbind('hover');
                    $('svg rect', api.elements.tooltip).hover(function() {
                        $(this).css('cursor', 'pointer');
                    });

                    $('svg rect', api.elements.tooltip).unbind('click');
                    $('svg rect', api.elements.tooltip).click(function() {
                        var _text = $($(this).parent()).find('text:first'),
                                _rgbRect = StudyViewUtil.rgbStringConvert($(this).css('fill')),
                                _rgbText = StudyViewUtil.rgbStringConvert($(_text).css('fill')),
                                _rectColor = StudyViewUtil.rgbToHex(_rgbRect[0], _rgbRect[1], _rgbRect[2]),
                                _textColor = StudyViewUtil.rgbToHex(_rgbText[0], _rgbText[1], _rgbText[2]);

                        if (_textColor === '#000000') {
                            $(_text).css('fill', 'red');
                            highlightCurve(_rectColor.substring(1) + "-" + _plotKey);
                        } else {
                            $(_text).css('fill', 'black');
                            resetCurve(_rectColor.substring(1) + "-" + _plotKey);
                        }

                    });
                }
            }
        });
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
     * @param _plotKey the plot identifier
     * @param _svg
     * @param _startedIndex
     * @param _svgWidth
     */
    function drawSavedLabels(_plotKey, _svg, _startedIndex, _svgWidth) {
        var _savedLabelsLength = getSavedCurveName(_plotKey).length,
                _savedCurveInfo = savedCurveInfo[_plotKey];

        if (_savedLabelsLength > 0) {
            var _index = 0;
            for (var key in _savedCurveInfo) {
                drawLabelBasicComponent(_plotKey, _svg, _index + _startedIndex, _savedCurveInfo[key].color, _savedCurveInfo[key].name, 'close', _svgWidth);
                _index++;
            }
        }
    }

    /**
     * Draw basic label componets:  one rect, one lable name, 
     *                              icons(pin or delete icons)
     * 
     * @param {type} _plotKey the current selected plot identifier.
     * @param {type} _svg       the svg where to draw labels.
     * @param {type} _index     the label index in current plot.
     * @param {type} _color     the label color.
     * @param {type} _textName  the label name.
     * @param {type} _iconType  'pin' or 'close', pin will draw pin icon and
     *                          delete icon, close will only draw delete icon.
     * @param {type} _svgWidth  the svg width.
     */
    function drawLabelBasicComponent(_plotKey, _svg, _index, _color, _textName, _iconType, _svgWidth) {
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
                .attr('id', 'survival_label_text_' + _plotKey + "_" + _index)
                .attr('oValue', _textName)
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
     * @param {type} _plotKey     the selected plot identifier.
     * @param {type} _svg           the svg where to draw labels.
     * @param {type} _startedIndex  
     * @param {type} _svgWidth      the svg width.
     */
    function drawNewLabels(_plotKey, _svg, _startedIndex, _svgWidth) {
        var _numOfLabels = curveInfo[_plotKey].length;
        for (var i = 0; i < _numOfLabels; i++) {
            drawLabelBasicComponent(
                    _plotKey, 
                    _svg, 
                    i + _startedIndex, 
                    curveInfo[_plotKey][i].color, 
                    curveInfo[_plotKey][i].name, 
                    'pin', 
                    _svgWidth);
        }
    }

    
    /**
     * Will be called when user pin/delete labeles
     * @param {type} _plotKey
     */
    function redrawLabel(_plotKey) {
        $("#" + opts[_plotKey].divs.bodyLabel + " svg").remove();
        drawLabels(_plotKey);
        addEvents(_plotKey);
    }
    
    /**
     * 
     * @param {type} _plotsInfo
     * @param {type} _data      all data before prcessing, and clone it to oData.
     */
    function createCurves(_plotsInfo, _data) {
        var _keys = Object.keys(_plotsInfo);
        numOfPlots = Object.keys(_plotsInfo).length;
        plotsInfo = _plotsInfo;
        oData = _data;
        oDataLength = _data.length;

        for (var i = 0; i < numOfPlots; i++) {
            plotBasicFuncs(i, _keys[i]);
        }

        //The initStatus will be used from other view
        initStatus = true;
    }

    function plotBasicFuncs(_index, _key) {
        var _casesInfo;

        aData[_key] = {};
        opts[_key] = {};
        aData[_key] = dataProcess(plotsInfo[_key]);

/*
        for(var _key in aData[_index]){
            console.log("-----");
            console.log(_key);
            console.log(aData[_index][_key].months);
            console.log(aData[_index][_key].status);
            console.log();
        }
        */
        //If no data returned, this survival plot should not be initialized.
        if (Object.keys(aData[_key]).length !== 0) {
            opts[_key] = initOpts(_index, _key);
            createDiv(opts[_key]);
            _casesInfo = grouping(plotsInfo[_key].caseLists, '');
            initView(_casesInfo, aData[_key], _key);
            drawLabels(_key);
            addEvents(_key);
        } else {
            console.log("No data for Survival Plot: " + _key);
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