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
        labelTable,
        
        //Remember customize order after user clicking header.
        labelTableOrder = [],
        
        //Remember previosu filters, if filters changed, dont refresh table
        //beause the user is modiftying the current chart. If do not changed,
        //and chart still got redraw, then refresh table.
        previousFilters = [],
        plotDataButtonFlag = false,
        chartColors;
    
    var label =[],
        labelSize = 10,
        maxLabelNameLength = 0,
        maxLabelValue = 0,
        fontSize = labelSize;
            
    var postFilterCallback,
        postRedrawCallback,
        pieLabelClickCallback,
        plotDataCallback;

    var titleLengthCutoff = 25;
    
    //Pie chart categories: regular, extendable
    var category = 'regular';
    //This function is designed to draw Pie Labels based on current color the
    //Pie Chart has. Pagging function will be added when the number of labels
    //bigger than 5.
    function addPieLabels() {
        var _filters =[];

        $('#' + DIV.mainDiv + ' .study-view-pie-label').html("");

        initLabelInfo();
        labelFunction();

        _filters = pieChart.filters();

        $('#' + DIV.labelTableID+'-0').find('tr').each(function(index, value) {
            if(_filters.indexOf($($($(value).find('td')[0])).find('span').text()) !== -1) {
                $(value).find('td').each(function (index1, value1) {
                    $(value1).addClass('highlightRow');
                });
            }
        });

        addPieLabelEvents();
    }

    function addPieLabelEvents() {
        $('#' + DIV.chartDiv + '-download-icon').qtip('destroy', true); 
        $('#'+  DIV.chartDiv + '-plot-data').qtip('destroy', true);
        $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip('destroy', true);
        
        //Add qtip for download icon when mouse over
        $('#' + DIV.chartDiv + '-download-icon-wrapper').qtip({
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show: {event: "mouseover", delay: 0},
            hide: {fixed:true, delay: 300, event: "mouseout"},
            position: {my:'bottom left',at:'top right', viewport: $(window)},
            content: {
                text:   "Download"
            }
        });
        
        //Add qtip for survival icon
        $('#'+  DIV.chartDiv+'-plot-data').qtip({
            style:  { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
            show:   {event: "mouseover"},
            hide:   {fixed:true, delay: 300, event: "mouseout"},
            position:   {my:'bottom left',at:'top right', viewport: $(window)},
            content:    "Survival analysis"
        });
        
        //Add qtip for download icon when mouse click
        $('#' + DIV.chartDiv + '-download-icon').qtip({
            id: '#' + DIV.chartDiv + "-download-icon-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
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
                        
                        for(var i = 0; i < label.length; i++){
                            content += '\r\n';
                            content += label[i].name + '\t';
                            content += label[i].value;
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
        
        $('#' + DIV.mainDiv).qtip('destroy', true);
        
        var _sDom = 'rt',
            _sScrollY = '200';
        if(category === 'regular') {
            $('#' + DIV.mainDiv).qtip({
            id: DIV.mainDiv,
            style: {
                classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow forceZindex qtip-max-width'
            },
            show: {event: "mouseover", solo: true, delay: 0},
            hide: {fixed:true, delay: 300, event: "mouseleave"},
            position: {my:'left center',at:'center right', viewport: $(window)},
            content: $('#' + DIV.mainDiv + ' .study-view-pie-label').html(),
            events: {
                render: function(event, api) {
                    $('#qtip-' + DIV.mainDiv + " table").attr('id', 'qtip-' + DIV.mainDiv + "-table");
                    labelTable = $('#qtip-' + DIV.mainDiv + "-table").dataTable({
                        "sDom": _sDom,
                        "sScrollY": _sScrollY,
                        "bPaginate": false,
                        "bScrollCollapse": true,
                        "autoWidth": false
                    });
                    
                    $('.pieLabel', api.elements.tooltip).mouseenter(function() {
                        pieLabelMouseEnter(this);
                    });
                    
                    $('.pieLabel', api.elements.tooltip).mouseleave(function(){
                        pieLabelMouseLeave(this);
                    });

                    $('.pieLabel', api.elements.tooltip).click(function(_event){
                        var _shiftClicked = StudyViewWindowEvents.getShiftKeyDown();
                        _event.preventDefault();
                        pieLabelClick(this, _shiftClicked);
                        api.show(_event);
                    });

//                    $('.pie-label-left-pagging', api.elements.tooltip).click(function(){
//                        var tmpValue = $(this).parent().parent().attr('id').split('-');
//                        var currentTableID = Number(tmpValue[tmpValue.length-1]);
//                        if(currentTableID !== 0){
//                            var nextTableID = currentTableID-1;
//                            $('#'+ DIV.labelTableID + '-'+currentTableID).css('display','none');            
//                            $('#'+DIV.labelTableID+'-'+nextTableID).css('display','block');
//                        }
//                    });
//                    
//                    $('.pie-label-right-pagging', api.elements.tooltip).click(function(){
//                        var fill = $(this).attr('fill');
//                        if(fill === 'blue'){
//                            var tmpValue = $(this).parent().parent().attr('id').split('-');
//                            var currentTableID = Number(tmpValue[tmpValue.length-1]);
//                            var nextTableID = currentTableID+1;
//
//                            $('#'+DIV.labelTableID+'-'+currentTableID).css('display','none');            
//                            $('#'+DIV.labelTableID+'-'+nextTableID).css('display','block');
//                        }
//                    });
                },
                visible: function(event, api) {
                    if(!$('#qtip-' + DIV.mainDiv + "-table").hasClass('clicked')){
                        labelTable.fnAdjustColumnSizing();
                        $('#qtip-' + DIV.mainDiv + "-table").addClass('clicked');
                    }
                }
            }
        });
        }else if(category === 'extendable'){
            $("#"+ DIV.chartDiv +"-extend").css('display', 'block');

            //Make sure datatable will be reinitialized when table view selected
            if ($("#"+DIV.chartDiv+"-pie-icon").css('display') === 'block' && !$.fn.DataTable.isDataTable( '#' + DIV.labelTableID+'-0' ) ) {
                initPieLabelDataTable();
            }
        }
    }
    
    //This function is designed to add functions like click, on, or other
    //other functions added after initializing this Pie Chart.
    function addFunctions() {
//        var _plotDataButtonDiv;
//        if(plotDataButtonFlag) {
////                _plotDataButtonDiv = "<input type='button' id='"+
////                                    DIV.chartDiv+"-plot-data' "+
////                                    "style='font-size:10px;clear:right;float:right;' value='Survival'>";
//            _plotDataButtonDiv = "<img id='"+
//                                DIV.chartDiv+"-plot-data' class='study-view-survival-icon' src='images/survival_icon.svg'/>";
//        }else {
//            _plotDataButtonDiv = "";
//        }
//        $("#"+DIV.chartDiv).append("<div id='"+DIV.chartDiv+"-side' class='study-view-pdf-svg-side'>"+
////                _plotDataButtonDiv + 
////                "<form style='display:inline-block;clear:right;float:right;' action='svgtopdf.do' method='post' id='"+DIV.chartDiv+"-pdf'>"+
////                "<input type='hidden' name='svgelement' id='"+DIV.chartDiv+"-pdf-value'>"+
////                "<input type='hidden' name='filetype' value='pdf'>"+
////                "<input type='hidden' id='"+DIV.chartDiv+"-pdf-name' name='filename' value='"+StudyViewParams.params.studyId + "_" +selectedAttr+".pdf'>"+
////                "<input type='submit' style='font-size:10px;' value='PDF'>"+          
////                "</form>"+
////                "<form style='display:inline-block;clear:right;float:right;' action='svgtopdf.do' method='post' id='"+DIV.chartDiv+"-svg'>"+
////                "<input type='hidden' name='svgelement' id='"+DIV.chartDiv+"-svg-value'>"+
////                "<input type='hidden' name='filetype' value='svg'>"+
////                "<input type='hidden' id='"+DIV.chartDiv+"-svg-name' name='filename' value='"+StudyViewParams.params.studyId + "_" +selectedAttr+".svg'>"+
////                "<input type='submit' style='font-size:10px;clear:right;float:right;' value='SVG'></form>"+
//                _plotDataButtonDiv + 
//                "<div id='"+ DIV.chartDiv+"-download-icon-wrapper'" +
//                "class='study-view-download-icon'><img id='"+ 
//                DIV.chartDiv+"-download-icon' style='float:left'"+
//                "src='images/in.svg'/></div>"+
////                "<img id='"+ DIV.chartDiv+"-transfer-icon'"+
////                " class='study-view-transfer-icon hover' "+
////                "src='images/transfer.svg'/>"+
////                "<img id='"+ DIV.chartDiv+"-expand-icon'"+
////                " class='study-view-expand-icon' "+
////                "src='images/expand.svg'/><img id='"+ 
////                DIV.chartDiv+"-contract-icon'"+
////                " class='study-view-contract-icon' "+
////                "src='images/contract.svg'/>"+
//                "</div>");
                
        
        
        
        if(selectedAttr !== 'CASE_ID'){
            pieChart.on("filtered", function(chart,filter){
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
                postFilterCallback();
            });
            pieChart.on("preRedraw",function(chart){
                var _filters = pieChart.filters();
                removeMarker();
                if(category !== "extendable") {
                    $('#qtip-' + DIV.mainDiv + "-table").find("tr").each(function(index, value) {
                        if(_filters.indexOf($(value).find('td').first().find('span').text()) !== -1) {
                            $(value).find('td').addClass("highlightRow");
                        }
                    });
                }
            });
            pieChart.on("postRedraw",function(chart){
                var _filters = pieChart.filters();
                
                if(
                        (!previousFilters.equals(_filters) 
                            && _filters.length === 0) 
                        || previousFilters.equals(_filters) ){
                    addPieLabels();
                }
                previousFilters = jQuery.extend(true, [], _filters);
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
//        
        if(plotDataButtonFlag) {
            $("#"+DIV.chartDiv+"-plot-data").click(function(){
                var _casesInfo = {},
                    _labelLength = label.length,
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
                    var _key = label[i].name.toString();
                    var _caseInfoDatum = {};
                    
                    _caseInfoDatum.caseIds = _caseIds[label[i].name];
                    if(typeof _caseIds[label[i].name] === 'undefined') {
                        console.log(label[i].name);
                        console.log(_caseIds[label[i].name]);
                    }
                    
                    _caseInfoDatum.color = label[i].color;
                    _casesInfo[_key] = _caseInfoDatum;
                }
                plotDataCallback(_casesInfo, [selectedAttr, selectedAttrDisplay]);

                setTimeout(function(){
                    StudyViewInitCharts.setPlotDataFlag(false);
                }, StudyViewParams.summaryParams.transitionDuration);
            });
        }
        
//        $("#"+DIV.chartDiv+"-transfer-icon").click(function() {
////            $("#"+DIV.mainDiv).css('z-index', 16000);
////            $("#"+DIV.mainDiv).animate({height: "340px", width: "375px", duration: 300, queue: false}, 300, function() {
////                StudyViewInitCharts.getLayout().layout();
////                $("#"+DIV.mainDiv).css('z-index', '');
////                $("#"+DIV.chartDiv+"-expand-icon").css('display', 'none');
////                $("#"+DIV.chartDiv +"-pieLabel-table").css('display', 'block');
////                $("#"+DIV.chartDiv+"-contract-icon").css('display', 'block');
////                $('#' + DIV.mainDiv).qtip('api').hide();
////            });
//            var _svg = $(pieChart.svg()[0]),
//                _svgHtml = _svg.html(),
//                _svgWidth = _svg.width(),
//                _svgHeight = _svg.height();
//                
//            $('#' + DIV.mainDiv).qtip('api').set('content.text', "<svg width="+_svgWidth+" height="+_svgHeight+">"+_svgHtml+"</svg>");
////            $('#' + DIV.chartDiv + ' svg').animate({'margin-left': '125px', duration: 300, queue: false});
//        });
//        
        $("#"+DIV.chartDiv+"-table-icon").click(function() {
            $("#"+DIV.mainDiv).css('z-index', 16000);
            $('#' + DIV.chartDiv ).css('display','none');
            $('#' + DIV.titleDiv ).css('display','none');
            if ( !$.fn.DataTable.isDataTable( '#' + DIV.labelTableID+'-0' ) ) {
                initPieLabelDataTable(animateTable);
            }else{
                animateTable();
            }

        });
        $("#"+DIV.chartDiv+"-pie-icon").click(function() {
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
        });
        
        $("#"+DIV.chartDiv+"-reload-icon").click(function() {
            $('#' + DIV.labelTableID+'-0').find('tr').each(function(index, value) {
                $(value).find('td').each(function (index1, value1) {
                    if($(value1).hasClass('highlightRow')) {
                        $(value1).removeClass('highlightRow');
                    }
                });
            });
            pieChart.filterAll();
            dc.redrawAll();
        });
    }

    function animateTable(){
        $("#"+DIV.mainDiv).animate({height: "340px", width: "375px", duration: 300, queue: false}, 300, function() {
            StudyViewInitCharts.getLayout().layout();
            $("#"+DIV.mainDiv).css('z-index', '');
            $("#"+DIV.chartDiv+"-pie-icon").css('display', 'block');
            $("#"+DIV.chartDiv+"-table-icon").css('display', 'none');
            $("#"+DIV.mainDiv + " .study-view-pie-label").css('display','block');
            if (labelTable)
                labelTable.fnAdjustColumnSizing();
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
            
            if(!_caseIds.hasOwnProperty(_key)){
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
        
        //Draw label header
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
                _number = Number($($(_value).parent().find('td.pieLabelValue')[0]).text()),
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
                        DIV.chartDiv+"-plot-data' class='study-view-survival-icon' src='images/survival_icon.svg'/>";
            }else {
                _plotDataButtonDiv = "";
            }
            
            
            $("#"+DIV.parentID).append("<div id=\"" + DIV.mainDiv +
                "\"" + _introDiv +
                "class='study-view-dc-chart study-view-pie-main'>"+
                "<div id='" + DIV.chartDiv +"-title-wrapper'" +
                " style='height: 16px; width:100%; float:left; text-align:center;'>"+
                "<div style='height:16px;float:right;' id='"+DIV.chartDiv+"-header'>"+
//                "<a href='javascript:StudyViewInitCharts.getChartsByID("+ 
//                chartID +").getChart().filterAll();" +
//                "dc.redrawAll();'><span title='Reset Chart'"+
//                "class='study-view-dc-chart-change' style='float:left; font-size:10px;'>"+
//                "RESET</span></a>" +
                "<img id='"+ DIV.chartDiv +"-reload-icon' class='study-view-title-icon hidden hover' src='images/reload-alt.svg'/>"+    
                _plotDataButtonDiv + 
                "<img id='"+ DIV.chartDiv +"-pie-icon' class='study-view-title-icon hover' src='images/pie.svg'/>"+
                "<img id='"+ DIV.chartDiv +"-table-icon' class='study-view-title-icon study-view-table-icon hover' src='images/table.svg'/>"+
                "<div id='"+ DIV.chartDiv+"-download-icon-wrapper'" +
                "class='study-view-download-icon'><img id='"+ 
                DIV.chartDiv+"-download-icon' style='float:left'"+
                "src='images/in.svg'/></div>"+
                "<img class='study-view-drag-icon' src='images/move.svg'/>"+
                "<span chartID="+chartID+" class='study-view-dc-chart-delete'>x</span>"+
                "</div><chartTitleH4 id='"+DIV.chartDiv +"-title'>" +
                _title + "</chartTitleH4></div>"+
                "<div id=\"" + DIV.chartDiv + "\" class='" + 
                className + "'  oValue='"+ selectedAttr + "," + 
                selectedAttrDisplay + ",pie'>"+
                
                "<div style='width:180px;float:left;text-align:center'></div></div>"+
//                "<div id='"+ DIV.chartDiv +"-extend' style='text-align:center;width: 100%;height:15px;float:left;display:none'>" + 
//                "<img id='"+ DIV.chartDiv +"-arrow-down-icon'"+
//                "class='study-view-arrow-icon show hover'" + 
//                "src='images/arrow-down.svg'/>"+
//                "<img id='"+ DIV.chartDiv +"-arrow-up-icon'"+
//                "class='study-view-arrow-icon hidden hover'" + 
//                "src='images/arrow-up.svg'/>"+
//                "</div>" + 
                "<div class='study-view-pie-label'></div></div>");
            //Title has been cut with 8 head character with ..., so the length
            //still longer than titleLengthCutoff
//            if(_title.length > titleLengthCutoff) {
//                addQtip(selectedAttrDisplay, DIV.chartDiv +"-title");
//            }
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
            
            //This comment function is designed to calculate the central point
            //between start and end points. Has been abandon since using arc
            //marker stead of red circle marker.
            /*
            var _xm = (_x1 + _x2) /2,
                _ym = (_y1 + _y2) /2;
            
            var m = Math.sqrt((Math.pow(_xm,2)+Math.pow(_ym,2)));

            
            var _tmpX = (_r + 3) / m * _xm,
                _tmpY = (_r + 3) / m * _ym;

            if(_xm === 0 && _ym === 0){
                _tmpY = 0;
                _tmpX = _r + 6;
            }
            if(_largeArc === 1 && Math.abs(_x1 - _x2) >0.1) {
                _tmpX = -_tmpX;
                _tmpY = -_tmpY;
            }*/
            
            //
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
        
            if(selectedAttrKeys.length > 10) {
                category = 'extendable';
            }else {
                category = 'regular';
            }
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

    //Initial Label Information stored in `label` array
    function initLabelInfo() {
        var _labelID = 0;

        label.length = 0;
        
        $('#' + DIV.chartDiv + '>svg>g>g').each(function(){
            var _labelDatum = {},
                _labelText = $(this).find('title').text(),
                _labelName = "",
                _labelValue = 0,
                _color = $(this).find('path').attr('fill'),            
                _pointsInfo = $(this).find('path').attr('d').split(/[\s,MLHVCSQTAZ]/);    
            
            _labelName = _labelText.substring(0, _labelText.lastIndexOf(":"));
            _labelValue = _labelText.substring(_labelText.lastIndexOf(":")+1);
            _labelValue = Number(_labelValue.trim());
            
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
                    _labelDatum.value = _labelValue;
                    label.push(_labelDatum);
                }
                _labelID++;
            }else{
                //StudyViewUtil.echoWarningMessg("Initial Lable Error");
            }
        });
        
        label.sort(function(a, b) {
            var _a = Number(a.value),
                _b = Number(b.value);
                
            if(_a < _b) {
                return 1;
            }else {
                return -1;
            }
        });
    }
    
    //Initial global parameters by using passed object .
    function initParam(_param) {
        var _baseID = _param.baseID;
        
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
    }

    //Remove drawed Pie Markder.
    function removeMarker() {
        $("#" + DIV.chartDiv).find('svg g .mark').remove();
    }
    
    //Called when the number of label biggen than 6, used by addPieLabels()
    function labelFunction() {
        var _tableDiv = '';

        _tableDiv +='<table id='+DIV.labelTableID+'-0><thead><th>'+selectedAttrDisplay+'</th><th>#</th></thead><tbody>';

        for(var i=0; i< label.length; i++){
            var _tmpName = label[i].name;
            
//            if(_tmpName.length > 9){
//                _tmpName = _tmpName.substring(0,5) + " ...";
//            }

            if(i % 1 === 0){
                _tableDiv += "<tr width='150px'>";
            }

            _tableDiv += '<td class="pieLabel" id="'+
                        DIV.labelTableTdID +label[i].id+'-'+i+
                        '"  style="font-size:'+fontSize+'px">'+
                        '<svg width="'+(labelSize+3)+'" height="'+
                        labelSize+'"><rect width="'+
                        labelSize+'" height="'+labelSize+'" style="fill:'+
                        label[i].color + ';" /></svg><span oValue="'+
                        label[i].name + '" style="vertical-align: top">'+
                        _tmpName+'</span></td><td class="pieLabelValue">'+label[i].value+'</td>';

            if(i % 1 === 0){
                _tableDiv += '</tr>';
            }

            if(maxLabelNameLength < _tmpName.length) {
                maxLabelNameLength = _tmpName.length;
            }

            if(maxLabelValue < label[i].value) {
                maxLabelValue = label[i].value;
            }
        }

        _tableDiv += '</tbody></table>';

        $('#' + DIV.mainDiv)
            .find('.study-view-pie-label')
            .append(_tableDiv);

        if(selectedAttrDisplay.length > maxLabelNameLength && selectedAttrDisplay.length > 20) {
            $('#' + DIV.mainDiv)
                .find('#' + DIV.labelTableID+'-0 thead th:nth-child(1)').text(selectedAttrDisplay.substring(0, maxLabelNameLength<20?18:maxLabelNameLength-3) + '...');
        }

        if(category === 'extendable' && ['CANCER_TYPE', 'CANCER_TYPE_DETAILED'].indexOf(selectedAttr) !== -1){
            $("#"+DIV.chartDiv+"-table-icon").click();
        }
    }

    function initPieLabelDataTable(callback) {
        var _aaSorting = [];

        if(labelTableOrder.length === 0) {
            _aaSorting = [[1, 'desc']];
        }else {
            _aaSorting = labelTableOrder;
        }
        labelTable = $('#' + DIV.labelTableID+'-0').dataTable({
            "sDom": "rt<f>",
            "sScrollY": "255",
            "bPaginate": false,
            "bScrollCollapse": true,
            "aaSorting": _aaSorting,
            "fnInitComplete": function(oSettings, json) {
                $('#'+ DIV.mainDiv + ' .dataTables_filter')
                        .find('label')
                        .contents()
                        .filter(function(){
                            return this.nodeType === 3;
                        }).remove();

                $('#'+ DIV.mainDiv + ' .dataTables_filter')
                        .find('input')
                        .attr('placeholder', 'Search...');

                labelTableOrder = oSettings.aaSorting;
            },
            "fnDrawCallback": function() {
                $('#'+ DIV.mainDiv).find('table tbody tr').hover(function(e, i) {
                    $(this).find('td').addClass('hoverRow');
                },function(e, i) {
                    $(this).find('td').removeClass('hoverRow');
                });
            }
        });

        $('#' + DIV.mainDiv+' .study-view-pie-label th').click(function() {
           labelTableOrder = labelTable.fnSettings().aaSorting;
        });

        $('#' + DIV.mainDiv+' .pieLabel').mouseenter(function() {
            pieLabelMouseEnter(this);
        });

        $('#' + DIV.mainDiv+' .pieLabel').mouseleave(function(){
            pieLabelMouseLeave(this);
        });

        $('#' + DIV.mainDiv+' .pieLabel').unbind('click');
        $('#' + DIV.mainDiv+' .pieLabel').click(function(_event){
            var _shiftClicked = StudyViewWindowEvents.getShiftKeyDown();
            _event.preventDefault();
            pieLabelClick(this, _shiftClicked);
        });

        if(callback && {}.toString.call(callback) === '[object Function]'){
            callback();
        }
    }

    function pieLabelMouseEnter(_this) {
        var idArray = $(_this).attr('id').split('-'),
            childID = Number(idArray[idArray.length-2])+1,
            fatherID = Number(idArray[idArray.length-3]);

        $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + childID+')').css({
            'fill-opacity': '.5',
            'stroke-width': '3'
        });

        drawMarker(childID,fatherID);
    }
    
    function pieLabelMouseLeave(_this) {
        var idArray = $(_this).attr('id').split('-'),
            childID = Number(idArray[idArray.length-2])+1,
            fatherID = Number(idArray[idArray.length-3]),
            arcID = fatherID+"-"+(Number(childID)-1);

        $("#" + DIV.chartDiv + " svg g #arc-" + arcID).remove();

        $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + childID+')').css({
            'fill-opacity': '1',
            'stroke-width': '1px'
        });
    }
    
    function pieLabelClick(_this, _shiftKeyDown) {
        var idArray = $(_this).attr('id').split('-');

        var childaLabelID = Number(idArray[idArray.length-1]),
            childID = Number(idArray[idArray.length-2])+1,
            chartID = Number(idArray[idArray.length-3]);

        var arcID = chartID+"-"+(Number(childID)-1);
        
        if(_shiftKeyDown) {
            $(_this).parent().find('td').each(function(index, value) {
                if($(value).hasClass('highlightRow')) {
                    $(value).removeClass('highlightRow');
                }else {
                    $(value).addClass('highlightRow');
                }
            });
            
            pieChart.onClick({
                key: label[childaLabelID].name, 
                value: label[childaLabelID].value
            });
        }else {
            var _trIndex = $(_this).parent().index(),
                _selfClicked = false;
            
            $(_this).parent().parent().find('tr').each(function(index, value) {
                $(value).find('td').each(function(index1, value1) {
                    if($(value1).hasClass('highlightRow')) {
                        $(value1).removeClass('highlightRow');
                        if(index === _trIndex) {
                            _selfClicked = true;
                        }
                    }
                });
            });
            
            pieChart.filterAll();
            
            if(!_selfClicked) {
                $(_this).parent().find('td').each(function(index, value) {
                    $(value).addClass('highlightRow');
                });
                
                pieChart.onClick({
                    key: label[childaLabelID].name, 
                    value: label[childaLabelID].value
                });
            }else {
                dc.redrawAll();
            }
        }

        $("#" + DIV.chartDiv + " svg g #" + arcID).remove();

        $('#' + DIV.chartDiv + ' svg>g>g:nth-child(' + childID+')').css({
            'fill-opacity': '1',
            'stroke-width': '1px'
        });
    }
    
    //Pass the qtip ID without #
    function addQtip(_text, _DivID){
        var _qtip = jQuery.extend(true,{},StudyViewBoilerplate.pieLabelQtip);
                
        _qtip.content.text = _text;
        $('#'+_DivID).qtip(_qtip);
    }
    
    //Display pie chart or dataTable
    function displayArrange() {
        if(category === 'extendable') {
            $("#"+DIV.chartDiv+"-table-icon").css('display', 'block');
        }else {
            $("#"+DIV.chartDiv+"-table-icon").css('display', 'none');
        }
        
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