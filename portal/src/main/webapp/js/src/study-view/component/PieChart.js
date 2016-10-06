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



var PieChart = function(){
    var pieChart, cluster;

    //All DIV ID names are organized based on the structure rule, initialized 
    //in initParam function
    var DIV = {
        parentID : "",
        mainDiv : "",
        titleDiv: "",
        chartDiv : "",
        labelTableID : "",
        labelTableTdID : ""
    };

    var chartID,
        className,
        selectedAttr,
        selectedAttrKeys,
        selectedAttrDisplay,
        ndx,
        plotDataButtonFlag = false,
        tableStatus = 'unavailable', //unavailable, initialized, 
        pieLabelTableStatus = 'unavailable',
        chartColors;

    var labels =[],
        allSampleSize = 0,
        currentSampleSize = 0;
        maxLabelNameLength = 0,
        maxLabelValue = 0;

    var postFilterCallback,
        postRedrawCallback,
        pieLabelClickCallback,
        plotDataCallback;

    var titleLengthCutoff = 25;

    //Pie chart categories: w1h1, w1h2, w2h1, w2h2
    var category = ['w1','h1'];
    var currentView = 'pie'; //Default value: pie, table

    //The attributes will be converted to table view after initialization
    var tableAttrs = {'CANCER_TYPE':1, 'CANCER_TYPE_DETAILED':1};

    var reactTableData = {};
    var labelMetaData = [];
    
    //This function is designed to draw Pie Labels based on current color the
    //Pie Chart has. Pagging function will be added when the number of labels
    //bigger than 5.
    function addPieLabels() {
        initLabels();
        initReactData();
        labelFunction();
        addPieLabelEvents();

        //Make sure table will be reinitialized when table view selected
        if (currentView === 'table' && tableStatus === 'unavailable' ) {
            initReactTable(DIV.labelTableID, reactTableData);
            tableStatus = 'initialized';
        }
    }
    
    function updatePieLabels() {
        updateCurrentLabels();
        initReactData();
        updateTables();
    }
    
    function updateTables() {
        if(pieLabelTableStatus === 'initialized') {
            updateQtipReactTable();
        }
        if(tableStatus === 'initialized') {
            updateReactTable();
        }
    }

    function initReactData() {
        var result = {
            data: [],
            attributes: [
                {
                    "attr_id": "name",
                    "display_name": selectedAttrDisplay,
                    "datatype": "STRING",
                    "column_width": 215
                },
                {
                    "attr_id": "color",
                    "display_name": "Color",
                    "datatype": "STRING",
                    "show": false
                },
                {
                    "attr_id": "samples",
                    "display_name": "#",
                    "datatype": "NUMBER",
                    "column_width": 70
                },
                {
                    "attr_id": "sampleRate",
                    "display_name": "Freq",
                    "datatype": "PERCENTAGE",
                    "column_width": 90
                },
                {
                    "attr_id": "caseIds",
                    "display_name": "Cases",
                    "datatype": "STRING",
                    "show": false
                },
                {
                    "attr_id": "uniqueId",
                    "display_name": "uniqueId",
                    "datatype": "STRING",
                    "show": false
                }
            ]
        };

        _.each(labels, function(item, index) {
            for (var key in item) {
                var datum = {
                    'attr_id': key,
                    'uniqueId': item.id,
                    'attr_val': item[key]
                };
                result.data.push(datum);
            }
            result.data.push({
                'attr_id': 'sampleRate',
                'uniqueId': item.id,
                'attr_val': ( currentSampleSize <= 0 ? 0 : (Number(item.samples) * 100 / currentSampleSize).toFixed(1).toString()) + '%'
            });
        });

        reactTableData =  result;
    }

    function initReactTable(targetId, inputData, opts) {
        var _filters = pieChart.filters();
        var selectedRows = _.map(_.filter(labels, function(item) {
            return _.contains(_filters, item.name);
        }), function(item) {
            return item.id.toString();
        });
        
        var opts = $.extend({
            input: inputData,
            filter: "ALL",
            download: "NONE",
            downloadFileName: "data.txt",
            showHide: false,
            hideFilter: true,
            scroller: true,
            resultInfo: false,
            groupHeader: false,
            fixedChoose: false,
            uniqueId: 'uniqueId',
            rowHeight: 25,
            tableWidth: 375,
            maxHeight: 290,
            headerHeight: 26,
            groupHeaderHeight: 40,
            autoColumnWidth: false,
            columnMaxWidth: 300,
            columnSorting: false,
            tableType: 'pieLabel',
            selectedRow: selectedRows,
            rowClickFunc: pieLabelClick
        }, opts);
        
        var testElement = React.createElement(EnhancedFixedDataTableSpecial, opts);

        ReactDOM.render(testElement, document.getElementById(targetId));
    }
    
    function addPieLabelEvents() {
        $('#' + DIV.chartDiv + '-download-icon').qtip('destroy', true);
        $('#'+  DIV.chartDiv + '-plot-data').qtip('destroy', true);
        $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip('destroy', true);

        //Add qtip for download icon when mouse over
        $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip({
            style: { classes: 'qtip-light qtip-rounded qtip-shadow'  },
            show: {event: "mouseover", delay: 0},
            hide: {fixed:true, delay: 300, event: "mouseout"},
            position: {my:'bottom left',at:'top right', viewport: $(window)},
            content: {
                text:   "Download"
            }
        });

        //Add qtip for survival icon
        $('#'+  DIV.chartDiv+'-plot-data').qtip({
            style:  { classes: 'qtip-light qtip-rounded qtip-shadow'  },
            show:   {event: "mouseover"},
            hide:   {fixed:true, delay: 300, event: "mouseout"},
            position:   {my:'bottom left',at:'top right', viewport: $(window)},
            content:    "Survival analysis"
        });

        //Add qtip for download icon when mouse click
        $('#' + DIV.chartDiv + '-download-icon').qtip({
            id: '#' + DIV.chartDiv + "-download-icon-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow'  },
            show: {event: "click", delay: 0},
            hide: {fixed:true, delay: 300, event: "mouseout"},
            position: {my:'top center',at:'bottom center', viewport: $(window)},
            content: {
                text:
                        "<div style='display:inline-block;'>"+
                        "<button id='"+DIV.chartDiv+"-pdf' style=\"width:50px\">PDF</button>"+
                        "</div>"+
                        "<br>"+
                        "<div style='display:inline-block;'>"+
                        "<button id='"+DIV.chartDiv+"-svg' style=\"width:50px\">SVG</button>"+
                        "</div>"+
                        "<br>"+
                        "<div style='display:inline-block;'>"+
                        "<button id='"+DIV.chartDiv+"-tsv' style=\"width:50px\">TXT</button>"+
                        "</div>"
            },
            events: {
                show: function() {
                    $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip('api').hide();
                },
                render: function(event, api) {
                    $("#"+DIV.chartDiv+"-pdf", api.elements.tooltip).click(function(){
                        setSVGElementValue(DIV.chartDiv,
                            DIV.chartDiv+"-pdf-value", {
                                filename: StudyViewParams.params.studyId + "_" +selectedAttr+".pdf",
                                contentType: "application/pdf",
                                servletName: "svgtopdf.do"
                            });
                    });
                    $("#"+DIV.chartDiv+"-svg", api.elements.tooltip).click(function(){
                        setSVGElementValue(DIV.chartDiv,
                            DIV.chartDiv+"-svg-value", {
                                filename: StudyViewParams.params.studyId + "_" +selectedAttr+".svg"
                            });
                    });
                    $("#"+DIV.chartDiv+"-tsv").click(function(){
                        var content = '';

                        content = content + selectedAttrDisplay + '\t';
                        content = content + 'Count';

                        for(var i = 0; i < labelMetaData.length; i++){
                            content += '\r\n';
                            content += labelMetaData[i].name + '\t';
                            content += labelMetaData[i].samples;
                        }

                        var downloadOpts = {
//                            filename: cancerStudyName + "_" + selectedAttrDisplay + ".txt",
                            filename: StudyViewParams.params.studyId + "_" + selectedAttrDisplay + ".txt",
                            contentType: "text/plain;charset=utf-8",
                            preProcess: false
                        };

                        cbio.download.initDownload(content, downloadOpts);
                    });
                }
            }
        });

        if(currentView === 'pie') {
            $('#' + DIV.mainDiv).one('mouseover', function () {
                initMainDivQtip();
            });
        }
        
        $("#"+ DIV.chartDiv +"-extend").css('display', 'block');
    }

    function updateReactTable() {
        var data = $.extend(true, {}, reactTableData);
        initReactTable(DIV.labelTableID, data);
    }
    
    function updateQtipReactTable() {
        var data = $.extend(true, {}, reactTableData);
        data.attributes[0].column_width = 140;
        initReactTable('qtip-' + DIV.mainDiv + '-content-react', data, {
            tableWidth: 300,
            pieLabelMouseEnterFunc: pieLabelMouseEnter,
            pieLabelMouseLeaveFunc: pieLabelMouseLeave
        });
    }

    function initMainDivQtip () {
        $('#' + DIV.mainDiv).qtip({
            id: DIV.mainDiv,
            style: {
                classes: 'qtip-light qtip-rounded qtip-shadow forceZindex qtip-max-width study-view-qtip study-view-pie-label-qtip'
            },
            show: {event: "mouseover", solo: true, delay: 0, ready: true},
            hide: {fixed:true, delay: 300, event: "mouseleave"},
            //hide: false,
            position: {my:'left center',at:'center right', viewport: $(window)},
            content: '<div id="qtip-' + DIV.mainDiv + '-content-react">Loading....</div>',
            events: {
                render: function(event, api) {
                    updateCurrentLabels();
                    initReactData();
                    var data = $.extend(true, {}, reactTableData);
                    data.attributes[0].column_width = 140;
                    initReactTable('qtip-' + DIV.mainDiv + '-content-react',
                        data, {
                            tableWidth: 300,
                            pieLabelMouseEnterFunc: pieLabelMouseEnter,
                            pieLabelMouseLeaveFunc: pieLabelMouseLeave
                        });
                    pieLabelTableStatus = 'initialized';
                }
            }
        });
    }

    //This function is designed to add functions like click, on, or other
    //other functions added after initializing this Pie Chart.
    function addFunctions() {
        if(selectedAttr !== 'CASE_ID'){
            pieChart.on("filtered", function(chart,filter){
                console.log("filtered called for: "+chartID);

                var _currentFilters = pieChart.filters();

                if(_currentFilters.length === 0){
                    $("#"+DIV.chartDiv+"-reload-icon")
                                .css('display','none');
                    $("#" + DIV.mainDiv)
                            .css({'border-width':'1px', 'border-style':'solid'});
                }else{
                    $("#"+DIV.chartDiv+"-reload-icon")
                                .css('display','block');
                    $("#" + DIV.mainDiv)
                            .css({'border-width':'2px', 'border-style':'inset'});
                }

                removeMarker();
                
                // provide the postFilterCallback function with the chartID and the filter for the breadcrumbs
                postFilterCallback(chartID, filter);
            });
            pieChart.on("preRedraw",function(chart){
                removeMarker();
            });
            pieChart.on("postRedraw",function(chart){
                updatePieLabels();
                postRedrawCallback();
            });
            pieChart.on("postRender",function(chart){
                addPieLabels();
            });
        }
    }

    function showHideDivision(_listenedDiv, _targetDiv, _time){
        var _targetLength = _targetDiv.length;
        for ( var i = 0; i < _targetLength; i++) {
            $(_targetDiv[i]).css('display', 'none');
        }
        $(_listenedDiv).hover(function(){
            $(_listenedDiv).css('z-index', '1');
            for ( var i = 0; i < _targetLength; i++) {
                $(_targetDiv[i]).stop().fadeIn(_time, function(){
                    $(this).css('display', 'block');
                });
            }
//            $("#"+DIV.chartDiv +"-title-wrapper").width('130');
            $("#"+DIV.chartDiv +"-title-wrapper").css('text-align', 'left');
            if(selectedAttrDisplay.length > 14) {
                $("#"+DIV.chartDiv +"-title").text(selectedAttrDisplay.substring(0,12) + "...");
                addQtip(selectedAttrDisplay, DIV.chartDiv +"-title");
            }
        }, function(){
            $(_listenedDiv).css('z-index', '0');
            for ( var i = 0; i < _targetLength; i++) {
                $(_targetDiv[i]).stop().fadeOut(_time, function(){
                    $(this).css('display', 'none');
                });
            }
//            $("#"+DIV.chartDiv +"-title-wrapper").width('180');
            $("#"+DIV.chartDiv +"-title-wrapper").css('text-align', 'center');
            if(selectedAttrDisplay.length > titleLengthCutoff) {
                $("#"+DIV.chartDiv +"-title").text(selectedAttrDisplay.substring(0,(titleLengthCutoff-2)) + "...");
            }else {
                $("#"+DIV.chartDiv +"-title").text(selectedAttrDisplay);
            }
            $('#' + DIV.chartDiv +"-title").qtip('destroy', true);
        });
    }

    //Add all listener events
    function addEvents() {

        showHideDivision("#"+DIV.mainDiv,
                        ["#"+DIV.chartDiv+"-side"], 0);
        showHideDivision("#"+DIV.mainDiv,
                        ["#"+DIV.chartDiv+"-header"],0);

        if(plotDataButtonFlag) {
            $("#"+DIV.chartDiv+"-plot-data").click(function(){
                var _casesInfo = {},
                    _labelLength = labels.length,
                    _caseIds = [];

                StudyViewInitCharts.setPlotDataFlag(true);

                if(pieChart.hasFilter()){
                    $("#"+DIV.labelTableID+"-0").find('td').each(function(index, value) {
                        if($(value).hasClass('highlightRow')) {
                            $(value).removeClass('highlightRow');
                        }
                    });
                    pieChart.filterAll();
                    dc.redrawAll();
                }

                _caseIds = getCaseIds();

                for(var i = 0; i < _labelLength; i++){
                    var _key = labels[i].name.toString();
                    var _caseInfoDatum = {};

                    _caseInfoDatum.caseIds = _caseIds[labels[i].name];
                    if(typeof _caseIds[labels[i].name] === 'undefined') {
                        console.log(labels[i].name);
                        console.log(_caseIds[labels[i].name]);
                    }

                    _caseInfoDatum.color = labels[i].color;
                    _casesInfo[_key] = _caseInfoDatum;
                }
                plotDataCallback(_casesInfo, [selectedAttr, selectedAttrDisplay]);

                setTimeout(function(){
                    StudyViewInitCharts.setPlotDataFlag(false);
                }, StudyViewParams.summaryParams.transitionDuration);
            });
        }

        $("#"+DIV.chartDiv+"-table-icon").click(function() {
            currentView = 'table';
            $("#"+DIV.mainDiv).css('z-index', 16000);

            //qtip will not be needed in table view
            if($('#' + DIV.mainDiv).qtip('api')) {
                $('#' + DIV.mainDiv).qtip('api').hide();
                $('#' + DIV.mainDiv).qtip('api').disable(true);
            }

            $('#' + DIV.chartDiv ).css('display','none');
            $('#' + DIV.titleDiv ).css('display','none');
            if ( tableStatus === 'initialized' ) {
                animateTable();
            }else{
                initReactTable(DIV.labelTableID, reactTableData);
                tableStatus = 'initialized';
                animateTable();
            }

        });
        $("#"+DIV.chartDiv+"-pie-icon").click(function() {
            currentView = 'pie';
            $('#' + DIV.mainDiv).qtip('api').disable(false);
            $('#' + DIV.mainDiv).qtip('api').show();
            $("#"+DIV.mainDiv).css('z-index', 16000);
            $("#"+DIV.mainDiv + " .study-view-pie-label").css('display','none');
            $("#"+DIV.mainDiv).animate({height: "165px", width: "180px", duration: 300, queue: false}, 300, function() {
                StudyViewInitCharts.getLayout().layout();
                $("#"+DIV.mainDiv).css('z-index', '1');
                $("#"+DIV.chartDiv+"-pie-icon").css('display', 'none');
                $("#"+DIV.chartDiv+"-table-icon").css('display', 'block');
               });
            $('#' + DIV.chartDiv ).css('display','block');
            $('#' + DIV.titleDiv ).css('display','block');
            $('#' + DIV.mainDiv).one('mouseover', function () {
                initMainDivQtip();
            });
        });

        $("#"+DIV.chartDiv+"-reload-icon").click(function() {
            pieChart.filterAll();
            dc.redrawAll();
        });
    }

    function animateTable(){
        var height, width;

        if(category[0] === 'w1') {
            width = '180px';
        }else{
            width = '375px';
        }

        if(category[1] === 'h1') {
            height = '165px';
        }else{
            height = '340px';
        }

        $("#"+DIV.mainDiv).animate({height: height, width: width, duration: 300, queue: false}, 300, function() {
            StudyViewInitCharts.getLayout().layout();
            $("#"+DIV.mainDiv).css('z-index', '');
            $("#"+DIV.chartDiv+"-pie-icon").css('display', 'block');
            $("#"+DIV.chartDiv+"-table-icon").css('display', 'none');
            $("#"+DIV.mainDiv + " .study-view-pie-label").css('display','block');
        });
    }

    function getCaseIds(){
        var _cases = pieChart.dimension().top(Infinity),
            _caseIds = {},
            _casesLength = _cases.length;

        for(var i = 0; i < _casesLength; i++){
            var _key = _cases[i][selectedAttr];

            if(_key === '' || _key.toUpperCase() === 'UNKNOWN'){
                _key = 'NA';
            }

            if(_.isUndefined(_caseIds[_key])){
                _caseIds[_key] = [];
            }
            _caseIds[_key].push(_cases[i].CASE_ID);
        }

        return _caseIds;
    }

    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue, downloadOptions){
        var _svgElement;

        var _svgWidth = (maxLabelNameLength>selectedAttrDisplay.length?maxLabelNameLength:selectedAttrDisplay.length + maxLabelValue.toString().length) * 10 + 20,
            _valueXCo = 0,
            _pieLabelString = '',
            _pieLabelYCoord = 0,
            _svg = $("#" + _svgParentDivId + " svg"),
            _previousHidden = false;

        if($("#" + DIV.chartDiv).css('display') === 'none') {
            _previousHidden = true;
            $("#" + DIV.chartDiv).css('display', 'block');
        }


        var _svgHeight = _svg.height(),
            _text = _svg.find('text'),
            _textLength = _text.length,
            _slice = _svg.find('g .pie-slice'),
            _sliceLength = _slice.length,
            _pieLabel = $("#" + _svgParentDivId).parent().find('td.pieLabel'),
            _pieLabelLength = _pieLabel.length;

        if(_previousHidden) {
            $("#" + DIV.chartDiv).css('display', 'none');
        }
        //Change pie slice text styles
        for ( var i = 0; i < _textLength; i++) {
            $(_text[i]).css({
                'fill': 'white',
                'font-size': '14px',
                'stroke': 'white',
                'stroke-width': '1px'
            });
        }

        //Change pie slice styles
        for ( var i = 0; i < _sliceLength; i++) {
            $($(_slice[i]).find('path')[0]).css({
                'stroke': 'white',
                'stroke-width': '1px'
            });
        }


        if(_svgWidth < 180){
            _svgWidth = 180;
        }

        _valueXCo = _svgWidth - maxLabelValue.toString().length * 8 -30;

        //Draw sampleSize header
        _pieLabelString += "<g transform='translate(0, "+
                    _pieLabelYCoord+")'>"+ _labelColormarker+
                    "<text x='13' y='10' "+
                    "style='font-size:12px; font-weight:bold'>"+
                    selectedAttrDisplay + "</text>"+
                    "<text x='"+_valueXCo+"' y='10' "+
                    "style='font-size:12px; font-weight:bold'>#</text>"+
                    "<line x1='0' y1='14' x2='"+ (_valueXCo - 20) +"' y2='14' "+
                    "style='stroke:black;stroke-width:2'></line>" +
                    "<line x1='"+ (_valueXCo - 10) +"' y1='14' x2='"+ (_svgWidth-20) +"' y2='14' "+
                    "style='stroke:black;stroke-width:2'></line>" +
                    "</g>";

        _pieLabelYCoord += 18;

        //Draw pie label into output
        for ( var i = 0; i < _pieLabelLength; i++) {
            var _value = _pieLabel[i],
                _number = Number($($(_value).parent().find('td.pieLabelValue span')[0]).text()),
                _labelName = $($(_value).find('span')[0]).attr('oValue'),
                _labelColormarker = cbio.download.serializeHtml($(_value).find('svg>rect')[0]);

            _pieLabelString += "<g transform='translate(0, "+
                    _pieLabelYCoord+")'>"+ _labelColormarker+
                    "<text x='13' y='10' "+
                    "style='font-size:15px'>"+  _labelName + "</text>"+
                    "<text x='"+_valueXCo+"' y='10' "+
                    "style='font-size:15px'>"+  _number + "</text>"+
                    "</g>";

            _pieLabelYCoord += 15;
        }

        _svgElement = cbio.download.serializeHtml($("#" + _svgParentDivId + " svg>g")[0]);

        var svg = "<svg xmlns='http://www.w3.org/2000/svg' version='1.1' width='"+_svgWidth+"' height='"+(180+_pieLabelYCoord)+"'>"+
                    "<g><text x='"+(_svgWidth/2)+"' y='20' style='font-weight: bold;"+
                    "text-anchor: middle'>"+
                    selectedAttrDisplay+"</text></g>"+
                    "<g transform='translate("+(_svgWidth / 2 - 65)+", 20)'>"+_svgElement+ "</g>"+
                    "<g transform='translate(10, "+(_svgHeight+20)+")'>"+
                    _pieLabelString+"</g></svg>";

        cbio.download.initDownload(
            svg, downloadOptions);

        //Remove pie slice text styles
        for ( var i = 0; i < _textLength; i++) {
            $(_text[i]).css({
                'fill': '',
                'font-size': '',
                'stroke': '',
                'stroke-width': ''
            });
        }

        //Remove pie slice styles
        for ( var i = 0; i < _sliceLength; i++) {
            $($(_slice[i]).find('path')[0]).css({
                'stroke': '',
                'stroke-width': ''
            });
        }
    }

    //Initialize HTML tags which will be used for current Pie Chart.
    function createDiv() {
        var _introDiv = '',
            _introNumber = Number(chartID) +2;

        _introDiv = "data-step='" + _introNumber + "' data-intro='Pie chart will category\n\
                         attributes by different colors' data-step='3' data-intro='Pie chart will category\n\
                         attributes by different colors'";


        if(selectedAttr === 'CASE_ID'){
            $(DIV.parentID)
                    .append("<div id=\"" + DIV.mainDiv +
                    "\" class='study-view-dc-chart study-view-pie-main' "+
                    "style='display:none'><div id=\"" +
                    DIV.chartDiv + "\"></div></div>");
        }else{
            var _title = selectedAttrDisplay.toString(),
                _plotDataButtonDiv = "";

            if(_title.length > titleLengthCutoff) {
                _title = _title.substring(0,(titleLengthCutoff-2)) + "...";
            }

            if(plotDataButtonFlag) {
                _plotDataButtonDiv = "<img id='"+
                        DIV.chartDiv+"-plot-data' class='study-view-survival-icon' src='images/survival_icon.svg' alt='survival' />";
            }else {
                _plotDataButtonDiv = "";
            }


            $("#"+DIV.parentID).append("<div id=\"" + DIV.mainDiv +
                "\"" + _introDiv +
                "class='study-view-dc-chart study-view-pie-main'>"+
                "<div id='" + DIV.chartDiv +"-title-wrapper'" +
                " style='height: 16px; width:100%; float:left; text-align:center;'>"+
                "<div style='height:16px;float:right;' id='"+DIV.chartDiv+"-header'>"+
                "<img id='"+ DIV.chartDiv +"-reload-icon' class='study-view-title-icon study-view-hidden hover' src='images/reload-alt.svg' alt='reload' />"+
                _plotDataButtonDiv +
                "<img id='"+ DIV.chartDiv +"-pie-icon' class='study-view-title-icon hover' src='images/pie.svg' alt='pie chart' />"+
                "<img id='"+ DIV.chartDiv +"-table-icon' class='study-view-title-icon study-view-table-icon hover' src='images/table.svg' alt='table' />"+
                "<div id='"+ DIV.chartDiv+"-download-icon-wrapper'" +
                "class='study-view-download-icon'><img id='"+
                DIV.chartDiv+"-download-icon' style='float:left'"+
                "src='images/in.svg' alt='download' /></div>"+
                "<img class='study-view-drag-icon' src='images/move.svg' alt='move' />"+
                "<span chartID="+chartID+" class='study-view-dc-chart-delete'>x</span>"+
                "</div><chartTitleH4 id='"+DIV.chartDiv +"-title'>" +
                _title + "</chartTitleH4></div>"+
                "<div id=\"" + DIV.chartDiv + "\" class='" +
                className + "'  oValue='"+ selectedAttr + "," +
                selectedAttrDisplay + ",pie'>"+

                "<div style='width:180px;float:left;text-align:center'></div></div>"+
                "<div class='study-view-pie-label' id='" + DIV.labelTableID + "'></div></div>");
        }
    }



    //This function is designed to draw Pie Slice Marker(Arc) based on the
    //selected pie slice color.
    function drawMarker(_childID,_fatherID) {
        var _pointsInfo =
                $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + _childID+')')
                    .find('path')
                    .attr('d')
                    .split(/[\s,MLHVCSQTAZ]/);

        var _pointsInfo1 =
                $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + _childID+')')
                    .find('path')
                    .attr('d')
                    .split(/[A]/);

        var _fill =
                $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + _childID+')')
                    .find('path')
                    .attr('fill');

        var _x1 = Number(_pointsInfo[1]),
            _y1 = Number(_pointsInfo[2]),
            //_largeArc = Number(_pointsInfo[6]),
            _x2 = Number(_pointsInfo[8]),
            _y2 = Number(_pointsInfo[9]),
            _r = Number(_pointsInfo[3]);

        if((_x1 - _x2!==0 || _y1 - _y2!==0) && _pointsInfo1.length === 2){
            var _pointOne = Math.atan2(_y1,_x1);
            var _pointTwo = Math.atan2(_y2,_x2);

            if(_pointOne < -Math.PI/2){
                _pointOne = Math.PI/2 + Math.PI *2 +_pointOne;
            }else{
                _pointOne = Math.PI/2 +_pointOne;
            }

            if(_pointTwo < -Math.PI/2){
                _pointTwo = Math.PI/2 + Math.PI*2 +_pointTwo;
            }else{
                _pointTwo = Math.PI/2 +_pointTwo;
            }

            //The value of point two should always bigger than the value
            //of point one. If the point two close to 12 oclick, we should 
            //change it value close to 2PI instead of close to 0
            if(_pointTwo > 0 && _pointTwo < 0.0000001){
                _pointTwo = 2*Math.PI-_pointTwo;
            }

            if(_pointTwo < _pointOne){
                console.log('%cError: the end angle should always bigger' +
                        ' than start angle.', 'color: red');
            }

            var _arcID = "arc-" +_fatherID+"-"+(Number(_childID)-1);
            var _arc = d3.svg.arc()
                            .innerRadius(_r + 3)
                            .outerRadius(_r + 5)
                            .startAngle(_pointOne)
                            .endAngle(_pointTwo);

            d3.select("#" + DIV.chartDiv + " svg g").append("path")
                .attr("d", _arc)
                .attr('fill',_fill)
                .attr('id',_arcID)
                .attr('class','mark');
        }
    }

    //Initialize PieChart in DC.js
    function initDCPieChart() {
        var _pieWidth = 130,
            _pieRadius = (_pieWidth - 20) /2,
            _color = jQuery.extend(true, [], chartColors);
        var NAIndex = -1;

        pieChart = dc.pieChart("#" + DIV.chartDiv);

        cluster = ndx.dimension(function (d) {
            return d[selectedAttr];
        });

        if(selectedAttr !== 'CASE_ID') {
            selectedAttrKeys.sort(function(a, b) {
                if(a< b){
                    return -1;
                }else {
                    return 1;
                }
            });

            NAIndex = selectedAttrKeys.indexOf('NA');
            if(NAIndex !== -1) {
                _color.splice(NAIndex, 0, '#CCCCCC');
            }

            category[1] = 'h2';
        }

        pieChart
            .width(_pieWidth)
            .height(_pieWidth)
            .radius(_pieRadius)
            .dimension(cluster)
            .group(cluster.group())
            .transitionDuration(StudyViewParams.summaryParams.transitionDuration)
            .ordinalColors(_color)
            .label(function (d) {
                return d.value;
            })
            .ordering(function(d){ return d.key;});
    }

    function updateCurrentLabels() {
        labels = filterLabels();
    }
    
    function initLabels() {
        labelMetaData = initLabelInfo();
        labels = $.extend(true, [] , labelMetaData);
    }

    function findLabel(labelName) {
        for (var i = 0; i < labelMetaData.length; i++) {
            if (labelMetaData[i].name === labelName) {
                return labelMetaData[i];
            }
        }
        return '';
    }
    
    function filterLabels() {
        var _labels = [];
        currentSampleSize = 0;
        
        $('#' + DIV.chartDiv + '>svg>g>g').each(function(){
            var _labelText = $(this).find('title').text();
            var _pointsInfo = $(this).find('path').attr('d').split(/[\s,MLHVCSQTAZ]/);
            var _labelName = _labelText.substring(0, _labelText.lastIndexOf(":"));
            var _labelValue = Number(_labelText.substring(_labelText.lastIndexOf(":")+1).trim());

            if(_pointsInfo.length >= 10){

                var _x1 = Number( _pointsInfo[1] ),
                    _y1 = Number( _pointsInfo[2] ),
                    _x2 = Number( _pointsInfo[8] ),
                    _y2 = Number( _pointsInfo[9] );

                if(Math.abs(_x1 - _x2) > 0.01 || Math.abs(_y1 - _y2) > 0.01){
                    var _label = findLabel(_labelName);
                    if(_label) {
                        _label.samples = _labelValue;
                        currentSampleSize += _labelValue;
                        _labels.push(_label);
                    }

                    if(maxLabelValue < _labelValue) {
                        maxLabelValue = _labelValue;
                    }
                }
            }else{
                StudyViewUtil.echoWarningMessg("Initial Label Error");
            }
        });
        
        return _labels;
    }
    
    function initLabelInfo() {
        var _labelID = 0;
        var _labels = []
        currentSampleSize = 0;

        $('#' + DIV.chartDiv + '>svg>g>g').each(function(){
            var _labelDatum = {};
            var _labelText = $(this).find('title').text();
            var _color = $(this).find('path').attr('fill');
            var _pointsInfo = $(this).find('path').attr('d').split(/[\s,MLHVCSQTAZ]/);
            var _labelName = _labelText.substring(0, _labelText.lastIndexOf(":"));
            var _labelValue = Number(_labelText.substring(_labelText.lastIndexOf(":")+1).trim());

            if(_pointsInfo.length >= 10){

                var _x1 = Number( _pointsInfo[1] ),
                    _y1 = Number( _pointsInfo[2] ),
                    _x2 = Number( _pointsInfo[8] ),
                    _y2 = Number( _pointsInfo[9] );

                if(Math.abs(_x1 - _x2) > 0.01 || Math.abs(_y1 - _y2) > 0.01){
                    _labelDatum.id = _labelID;
                    _labelDatum.name = _labelName;
                    _labelDatum.color = _color;
                    _labelDatum.parentID = DIV.chartDiv;
                    _labelDatum.samples = _labelValue;
                    currentSampleSize += _labelValue;

                    if(maxLabelValue < _labelValue) {
                        maxLabelValue = _labelValue;
                    }
                    _labels.push(_labelDatum);
                }
                _labelID++;
            }else{
                StudyViewUtil.echoWarningMessg("Initial Label Error");
            }
        });
        
        return _labels;
    }

    //Initial global parameters by using passed object .
    function initParam(_param) {
        var _baseID = _param.baseID;
        var _maxAttrL = 0; //maximum attribute length

        className = _param.chartDivClass,
        chartID = _param.chartID;
        selectedAttr = _param.attrID;
        selectedAttrKeys = _param.attrKeys;
        selectedAttrDisplay = _param.displayName;
        ndx = _param.ndx;
        chartColors = _param.chartColors;
        plotDataButtonFlag = _param.plotDataButtonFlag;

        DIV.mainDiv = _baseID + "-dc-chart-main-" + chartID;
        DIV.chartDiv = _baseID + "-dc-chart-" + chartID;
        DIV.titleDiv = _baseID + "-dc-chart-" + chartID + '-title';
        DIV.labelTableID = "table-" + _baseID + "-dc-chart-" + chartID;
        DIV.labelTableTdID = "pieLabel-" + _baseID + "-dc-chart-" + chartID + "-";
        DIV.parentID = _baseID + "-charts";

        _.each(selectedAttrKeys, function (key) {
            if(key.length > _maxAttrL) {
                _maxAttrL = key.length;
            }
        })

        category[0]= 'w2';

        if(['CANCER_TYPE', 'CANCER_TYPE_DETAILED'].indexOf(selectedAttr) !== -1) {
            category[0] = 'w2';
        }
    }

    //Remove drawed Pie Markder.
    function removeMarker() {
        $("#" + DIV.chartDiv).find('svg g .mark').remove();
    }

    //Create pie label tables. One is for table view, the other one is for storing
    //table meta data.
    function labelFunction() {
        if (tableAttrs.hasOwnProperty(selectedAttr) && currentView !== 'table') {
            $("#" + DIV.chartDiv + "-table-icon").click();
        }
    }

    function copyPieLabelTable(id) {
        var html = $('#' + DIV.mainDiv + ' .study-view-pie-label-copy').html();
        if (id) {
            $(html).attr('id', id);
        }
        return html;
    }

    function qtip(el, tip) {
        $(el).qtip('destroy', true)
        $(el).qtip({
            content: {text: tip},
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 200, event: "mouseout"},
            style: { classes: 'qtip-light qtip-rounded' },
            position: {my:'top right',at:'bottom center',viewport: $(window)}
        });
    }

    function pieLabelMouseEnter(data) {
        var childID = Number(data.id) + 1,
            fatherID = chartID;

        $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + childID+')').css({
            'fill-opacity': '.5',
            'stroke-width': '3'
        });

        drawMarker(childID,fatherID);
    }

    function pieLabelMouseLeave(data) {
        var childID = Number(data.id) + 1,
            fatherID = chartID,
            arcID = fatherID+"-"+(Number(childID)-1);

        $("#" + DIV.chartDiv + " svg g #arc-" + arcID).remove();

        $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + childID+')').css({
            'fill-opacity': '1',
            'stroke-width': '1px'
        });
    }

    function pieLabelClick(selectedData, selected, allSelectedData) {
        var childaLabelID = Number(selectedData.id),
            childID = childaLabelID + 1;

        var arcID = chartID + "-" + (Number(childID) - 1);

        pieChart.onClick({
            key: labelMetaData[childaLabelID].name,
            value: labelMetaData[childaLabelID].value
        });
        $("#" + DIV.chartDiv + " svg g #" + arcID).remove();

        $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + childID + ')').css({
            'fill-opacity': '1',
            'stroke-width': '1px'
        });
    }

    //Pass the qtip ID without #
    function addQtip(_text, _DivID){
        var _qtip = jQuery.extend(true,{},StudyViewBoilerplate.pieLabelQtip);

        _qtip.content.text = _text;
        $('#'+_DivID).qtip('destroy', true);
        $('#'+_DivID).qtip(_qtip);
    }

    //Display pie chart or table
    function displayArrange() {
        $("#"+DIV.chartDiv+"-table-icon").css('display', 'block');
        $('#' + DIV.chartDiv ).css('display','block');
        $('#' + DIV.titleDiv).css('display','block');
        $("#"+DIV.mainDiv).css({height: "165px", width: "180px"});
        $("#"+DIV.mainDiv + " .study-view-pie-label").css('display','none');
        $("#"+DIV.chartDiv+"-pie-icon").css('display', 'none');
    }

    return {
        init: function(_param){
            initParam(_param);
            createDiv();
            initDCPieChart();
            displayArrange();
            addFunctions();
            addEvents();
        },

        destroy: function() {
            tableStatus = 'unavailable';
            pieLabelTableStatus = 'unavailable';
        },
        
        getChart : function(){
            return pieChart;
        },

        getCluster: function(){
            return cluster;
        },

        chartValue: function() {
            return {attr: selectedAttr, display: selectedAttrDisplay};
        },

        drawMarker: drawMarker,

        pieLabelClickCallbackFunction: function(_callback){
            pieLabelClickCallback = _callback;
        },

        removeMarker: removeMarker,

        postFilterCallbackFunc: function(_callback) {
            postFilterCallback = _callback;
        },
        postRedrawCallbackFunc: function(_callback) {
            postRedrawCallback = _callback;
        },
        plotDataCallbackFunc: function(_callback) {
            plotDataCallback = _callback;
        }
    };
};