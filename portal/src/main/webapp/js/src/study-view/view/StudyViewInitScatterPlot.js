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


var StudyViewInitScatterPlot = (function() { 
    var initStatus = false,
        arr = [],
        arrLength = 0,
        scatterPlot,
        clickedCaseId = '',
        shiftClickedCaseIds = '',
        brushedCaseIds = [],
        scatterPlotArr = [],
        scatterPlotDataAttr = {},
        scatterPlotOptions = {},
        dcCharts = [],
        clearFlag = false,
        boundaryVal = {
            max_x: '',
            max_y: '',
            min_x: '',
            min_y: ''
        };
    
    function initData(_arr) {
        getMinMax(_arr);

        arr = _arr;
        arrLength = arr.length;
        
        for ( var i = 0; i < arrLength; i++) {
            var _value = arr[i];
            
            if( !isNaN(_value['COPY_NUMBER_ALTERATIONS']) && 
                !isNaN(_value['MUTATION_COUNT']) && 
                _value['COPY_NUMBER_ALTERATIONS'] !== "" && 
                _value['MUTATION_COUNT'] !== "") {                  
                    var _scatterPlotDatumTmp= {};
                    _scatterPlotDatumTmp.x_val = _value['COPY_NUMBER_ALTERATIONS'];
                    _scatterPlotDatumTmp.y_val = _value['MUTATION_COUNT'];
                    _scatterPlotDatumTmp.case_id = _value['CASE_ID'];
                    _scatterPlotDatumTmp.qtip = "# of mutations:  <strong>"+
                        _value['MUTATION_COUNT'] + 
                        "</strong></br>Fraction of CNA: <strong>"+
                        cbio.util.toPrecision(_value['COPY_NUMBER_ALTERATIONS'],2,0.01)+
                        "<br><a href='case.do?sample_id=" +
                        _value['CASE_ID'] + "&cancer_study_id=" +
                        StudyViewParams.params.studyId + "' target='_blank'>" + 
                        _value['CASE_ID'] + "</a></strong>";
                    scatterPlotArr.push(_scatterPlotDatumTmp);
            }
        }
        
        dcCharts = StudyViewInitCharts.getCharts();
        
        scatterPlotDataAttr = jQuery.extend(true, {}, StudyViewBoilerplate.scatterPlotDataAttr);
        scatterPlotOptions = jQuery.extend(true, {}, StudyViewBoilerplate.scatterPlotOptions);    
        
        scatterPlotDataAttr.min_x = boundaryVal.min_x;
        scatterPlotDataAttr.max_x = boundaryVal.max_x;
        scatterPlotDataAttr.min_y = boundaryVal.min_y;
        scatterPlotDataAttr.max_y = boundaryVal.max_y;
    }
    
    function initComponent() {
        var _title = $("#study-view-scatter-plot chartTitleH4").text();
        
        if(scatterPlotArr.length !== 0){
            scatterPlot = new ScatterPlots();
            scatterPlot.init(scatterPlotOptions, scatterPlotArr, scatterPlotDataAttr,true);            
            scatterPlot.jointBrushCallback(scatterPlotBrushCallBack);
            scatterPlot.jointClickCallback(scatterPlotClickCallBack);
            
            if(scatterPlotDataAttr.max_x > 1000){
//                $("#" + scatterPlotOptions.names.log_scale_x).attr('checked',true);
                scatterPlot.setAxisXLogFlag(true);
                scatterPlot.updateScaleX(scatterPlotOptions.names.log_scale_x);
            }
            if(scatterPlotDataAttr.max_y > 1000){
//                $("#" + scatterPlotOptions.names.log_scale_y).attr('checked',true);
                scatterPlot.setAxisYLogFlag(true);
                scatterPlot.updateScaleY(scatterPlotOptions.names.log_scale_y);
            }
           
//            $("#" + scatterPlotOptions.names.log_scale_x).change(function() {
//                scatterPlot.updateScaleX(scatterPlotOptions.names.log_scale_x);
//            });
//            $("#" + scatterPlotOptions.names.log_scale_y).change(function() {
//                scatterPlot.updateScaleY(scatterPlotOptions.names.log_scale_y);
//            });
            
            StudyViewUtil
                    .showHideDivision("#study-view-scatter-plot", 
                                    "#study-view-scatter-plot-header", 0);
            StudyViewUtil
                    .showHideDivision("#study-view-scatter-plot", 
                                    "#plots-title-x-checkbox", 0);
            StudyViewUtil
                    .showHideDivision("#study-view-scatter-plot", 
                                    "#plots-title-y-checkbox", 0);
                                    
            $('#study-view-scatter-plot-download-icon').qtip('destroy', true);
            $('#study-view-scatter-plot-download-icon').qtip({
                id: "#study-view-scatter-plot-download-icon-qtip",
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'  },
                show: {event: "click"},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'top center',at:'bottom center', viewport: $(window)},
                content: {
                    text:
                            "<div style='display:inline-block;'>"+
                            "<button id='study-view-scatter-plot-pdf' style=\"width:50px\">PDF</button>"+
                            "</div>"+
                            "<br>"+
                            "<div style='display:inline-block;'>"+
                            "<button id='study-view-scatter-plot-svg' style=\"width:50px\">SVG</button>"+
                            "</div>"+
                            "<br>"+
                            "<div style='display:inline-block;'>"+
                            "<button id='study-view-scatter-plot-tsv' style=\"width:50px\">TXT</button>"+
                            "</div>"
                },
                events: {
                    render: function(event, api) {
                        $("#study-view-scatter-plot-pdf", api.elements.tooltip).click(function(){
                            setSVGElementValue("study-view-scatter-plot-body-svg",
                                "study-view-scatter-plot-pdf-value",
                                scatterPlotOptions,
                                _title,{
                                    filename: "Scatter_Plot_result-"+ StudyViewParams.params.studyId +".pdf",
                                    contentType: "application/pdf",
                                    servletName: "svgtopdf.do"
                                });
                        });
                        $("#study-view-scatter-plot-svg", api.elements.tooltip).click(function(){
                            setSVGElementValue("study-view-scatter-plot-body-svg",
                                "study-view-scatter-plot-svg-value",
                                scatterPlotOptions,
                                _title, {
                                    filename: "Scatter_Plot_result-"+ StudyViewParams.params.studyId +".svg"
                                });
                        });
                        $("#study-view-scatter-plot-tsv").click(function(){
                            var content = '';
                            
                            content = content + 'Sample ID' + '\t';
                            content = content + 'Fraction Genome Altered' + '\t';
                            content = content + 'Mutation Count';
                            
                            for(var i = 0; i < scatterPlotArr.length; i++){
                                content += '\r\n';
                                content += scatterPlotArr[i].case_id + '\t';
                                content += StudyViewUtil.restrictNumDigits(scatterPlotArr[i].x_val) + '\t';
                                content += scatterPlotArr[i].y_val;
                            }
//
                            var downloadOpts = {
//                                filename: cancerStudyName + "_" + _title + ".txt",
                                filename: StudyViewParams.params.studyId + "_" + _title + ".txt",
                                contentType: "text/plain;charset=utf-8",
                                preProcess: false
                            };

                            cbio.download.initDownload(content, downloadOpts);
                        });
                    }
                }
            });
            
            
//            $("#study-view-scatter-plot-menu-icon").unbind("click");
//            $("#study-view-scatter-plot-menu-icon").click(function() {
//                var _side = $("#study-view-scatter-plot-side");
//                var _display = _side.css('display');
//                if (_display === "none") {
//                    StudyViewUtil.changePosition("#study-view-scatter-plot", 
//                                    "#study-view-scatter-plot-side",
//                                    "#dc-plots");
//                    _side.css('display', 'block');
//                } else {
//                    _side.css('display', 'none');
//                }
//            });
            
//            $("#study-view-scatter-plot-pdf").submit(function(){
//                setSVGElementValue("study-view-scatter-plot-body-svg",
//                    "study-view-scatter-plot-pdf-value",
//                    scatterPlotOptions,
//                    _title);
//            });
//            $("#study-view-scatter-plot-svg").submit(function(){
//                setSVGElementValue("study-view-scatter-plot-body-svg",
//                    "study-view-scatter-plot-svg-value",
//                    scatterPlotOptions,
//                    _title);
//            });
        }else{
            $('#study-view-scatter-plot').css('display','none');
        }
    }
    
    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue,scatterPlotDataAttr, _title, downloadOptions){
        var svgElement;
        
        $("#" + _svgParentDivId + " .plots-title-x-help").remove();
        $("#" + _svgParentDivId + " .plots-title-y-help").remove();
        $("#" + _svgParentDivId + " #plots-title-x-checkbox").remove();
        $("#" + _svgParentDivId + " #plots-title-y-checkbox").remove();
        $("#" + _svgParentDivId + " .plots-title-x")
            .attr('x', 
                Number($("#" + _svgParentDivId + " .plots-title-x").attr('x')) + 15);
        //Remove x/y title help icon first.
        $("#" + _svgParentDivId + " svg>g").each(function(i, e){
            svgElement += cbio.download.serializeHtml(e);
        });
        
        svgElement = "<svg xmlns='http://www.w3.org/2000/svg' version='1.1'><g><text text-anchor='middle' x='220' y='30' " +
                "style='font-weight:bold'>" + _title + 
                "</text></g><g transform='translate(0,40)'>" + 
                svgElement + "</g></svg>";
                            
        cbio.download.initDownload(
            svgElement, downloadOptions);
    
        scatterPlot.updateTitleHelp(scatterPlotDataAttr.names.log_scale_x, scatterPlotDataAttr.names.log_scale_y);
    }
    
    function initPage(){
        $("#study-view-charts").append(StudyViewBoilerplate.scatterPlotDiv);
        $("#study-view-scatter-plot-header").css('display', 'none');
    }
    
    function redrawByAttribute(_casesInfo, _selctedAttr){
        var _style = [];
        
        if(_casesInfo.hasOwnProperty('NA')){
            var _casesLength = _casesInfo['NA'].caseIds.length;
            
            for(var i = 0; i < _casesLength; i++){
                var styleDatum = {};
                styleDatum.case_id = _casesInfo['NA'].caseIds[i];
                styleDatum.fill = _casesInfo['NA'].color;
                styleDatum.stroke = _casesInfo['NA'].color;
                styleDatum.strokeWidth = '0';
                styleDatum.size = '60';
                _style.push(styleDatum);
            }
        }
        for(var key in _casesInfo){
            if(key !== 'NA'){
                var _casesLength = _casesInfo[key].caseIds.length;

                for(var i = 0; i < _casesLength; i++){
                    var styleDatum = {};
                    styleDatum.case_id = _casesInfo[key].caseIds[i];
                    styleDatum.fill = _casesInfo[key].color;
                    styleDatum.stroke = _casesInfo[key].color;
                    styleDatum.strokeWidth = '0';
                    styleDatum.size = '60';
                    _style.push(styleDatum);
                }
            }
        }
        scatterPlot.updateStyle(_style);
    }
    
    function redraw(_selectedCaseIds, _hasFilterFlag) {
        if(initStatus){
            setStyle(_selectedCaseIds, _hasFilterFlag);
        }
    }
    
    /**
     * 
     * @param {type} _selectedCaseIds
     * @param {type} _hasFilterFlag     use to distinguish reset scatter or 
     *                                  brushed no case.
     * @returns {undefined}
     */
    function setStyle(_selectedCaseIds, _hasFilterFlag) {
        var _style = [];
        var numOfSamples = StudyViewParams.params.sampleIds.length;
        
        if(initStatus){
            for(var i=0 ; i< numOfSamples ; i++){
                var styleDatum = {};

                styleDatum.case_id = StudyViewParams.params.sampleIds[i];
                if(_selectedCaseIds.length !== numOfSamples){
                    if(_selectedCaseIds.indexOf(StudyViewParams.params.sampleIds[i]) !== -1){
                        if(clickedCaseId !== ''){
                            styleDatum.fill = '#2986e2';
                            styleDatum.stroke = 'red';
                            styleDatum.strokeWidth = '3';
                            styleDatum.size = '60';
                        }else{
                            styleDatum.fill = 'red';
                            styleDatum.stroke = 'red';
                            styleDatum.strokeWidth = '0';
                            styleDatum.size = '60';
                        }
//                        styleDatum.opacity = '1';
                    }else{
                        if(_hasFilterFlag){
                            styleDatum.fill = '#2986e2';
                            styleDatum.stroke = '#2986e2';
                            styleDatum.strokeWidth = '0';
                            styleDatum.size = '60';
//                            styleDatum.opacity = '0.6';
                        }else {
                            styleDatum.fill = '#2986e2';
                            styleDatum.stroke = '#2986e2';
                            styleDatum.strokeWidth = '0';
                            styleDatum.size = '60';
//                            styleDatum.opacity = '1';
                        }
                    }
                }else if(_selectedCaseIds.length === 0){
                    styleDatum.fill = '#2986e2';
                    styleDatum.stroke = '#2986e2';
                    styleDatum.strokeWidth = '0';
                    styleDatum.size = '60';
//                    styleDatum.opacity = '1';
                }else{
                    styleDatum.fill = 'red';
                    styleDatum.stroke = 'red';
                    styleDatum.strokeWidth = '0';
                    styleDatum.size = '60';
//                    styleDatum.opacity = '1';
                }
                _style.push(styleDatum);
            }

            scatterPlot.updateStyle(_style);
        }
    }
    
    function scatterPlotBrushCallBack(_brushedCaseIds) {
        var _numOfCharts = dcCharts.length;
        
        brushedCaseIds = _brushedCaseIds;
        if(_brushedCaseIds.length === 0 || (shiftClickedCaseIds.length === 1 && _brushedCaseIds.indexOf(shiftClickedCaseIds[0]) === -1)){
            shiftClickedCaseIds = [];
            clickedCaseId = '';
            
//            var oTable = $("#dataTable").dataTable();
//
//            $(oTable.fnSettings().aoData).each(function (){
//                if($(this.nTr).hasClass('row_selected')){
//                    $(this.nTr).removeClass('row_selected');
//                    if($(this.nTr).hasClass('odd')){
//                       $(this.nTr).css('background-color','#E2E4FF'); 
//                    }else{
//                        $(this.nTr).css('background-color','white');
//                    }
//                }
//            });
        }
        
        
        if(_brushedCaseIds.length > 0){
            for(var i=0; i< _numOfCharts ; i++){
                if(dcCharts[i] !== ''){
                    if(dcCharts[i].getChart().filters().length > 0)
                        dcCharts[i].getChart().filter(null);
                }
            }
        }
        
        scatterPlotCallBack(_brushedCaseIds);
        StudyViewInitCharts.removeMarker();
    }
    
    function scatterPlotCallBack(_caseIDs){
        var _numOfCharts = dcCharts.length,
            _caseIdChartIndex = StudyViewInitCharts.getCaseIdChartIndex();
        
        if(_caseIDs.length > 0){
            clearFlag = false;
            dcCharts[_caseIdChartIndex].getChart().filterAll();
            dcCharts[_caseIdChartIndex].getChart().filter([_caseIDs]);
            dc.redrawAll();
        
            StudyViewInitCharts.resetBars();
            StudyViewInitCharts.redrawWSCharts();
        }else{
            clearFlag = true;
            for(var i=0; i< _numOfCharts ; i++){
                if(dcCharts[i] !== ''){
                    if(dcCharts[i].getChart().filters().length > 0)
                        dcCharts[i].getChart().filterAll();
                }
            }
            dc.redrawAll();
            StudyViewInitCharts.resetBars();
            StudyViewInitCharts.redrawSpecialPlots();
        }
    }
    
    function scatterPlotClickCallBack(_clickedCaseIds) {
        var _typeOfInputClickedCases = typeof _clickedCaseIds;
        if(_typeOfInputClickedCases === 'string'){
            clickedCaseId = _clickedCaseIds;
            scatterPlotClick(_clickedCaseIds);
        }else{
            shiftClickedCaseIds = _clickedCaseIds;
            scatterPlotShiftClick(_clickedCaseIds);
        }
    }
    
    function scatterPlotClick(_clickedCaseId){
        if(_clickedCaseId !== ''){
            StudyViewInitCharts.removeMarker();
            StudyViewInitCharts.getDataAndDrawMarker([_clickedCaseId]);
        }else{
            StudyViewInitCharts.removeMarker();
        }
        StudyViewInitCharts.resetBars();
        StudyViewInitCharts.changeHeader();
    }
    
    function scatterPlotShiftClick(_shiftClickedCaseIds){
        var _shiftClickedCasesLength = _shiftClickedCaseIds.length,
            _numOfCharts = dcCharts.length;
            
        StudyViewInitCharts.removeMarker();
        shiftClickedCaseIds = _shiftClickedCaseIds;
        if(_shiftClickedCasesLength !== 0){
            clickedCaseId = '';
            scatterPlotCallBack(shiftClickedCaseIds);
        }else{
            for(var i=0; i< _numOfCharts ; i++){
                if(dcCharts[i] !== ''){
                    if(dcCharts[i].getChart().filters().length > 0)
                        dcCharts[i].getChart().filterAll();
                }
            }
            dc.redrawAll();
            StudyViewInitCharts.resetBars();
            StudyViewInitCharts.redrawWSCharts();
            
            if(clickedCaseId !== '')
                StudyViewInitCharts.getDataAndDrawMarker([clickedCaseId]);
        }
    }

    /*

    Calculate maximum and minimum value of mutation counts and copy number alteration

    Min Max value in StudyViewInitCharts are calculated based on single attribute.
    But the situation may exist which is the sample with max value of mutation count(CNA)
    may not have CNA(mutation count).

    In this case, we need to recalculate the max and min value in all samples.
    */
    function getMinMax(_arr){
        var initialed = false;
        _arr.forEach(function(sample, index){
            if(sample.hasOwnProperty('COPY_NUMBER_ALTERATIONS') && sample.hasOwnProperty('MUTATION_COUNT')){
                //Directly assign value of first sample to variable boundaryVal
                if(!isNaN(sample.COPY_NUMBER_ALTERATIONS) && !isNaN(sample.MUTATION_COUNT)){
                    if (!initialed) {
                        boundaryVal.max_x = boundaryVal.min_x = sample.COPY_NUMBER_ALTERATIONS;
                        boundaryVal.max_y = boundaryVal.min_y = sample.MUTATION_COUNT;
                        initialed = true;
                    } else {
                        if (sample.COPY_NUMBER_ALTERATIONS < boundaryVal.min_x) {
                            boundaryVal.min_x = sample.COPY_NUMBER_ALTERATIONS;
                        }
                        if (sample.COPY_NUMBER_ALTERATIONS > boundaryVal.max_x) {
                            boundaryVal.max_x = sample.COPY_NUMBER_ALTERATIONS;
                        }
                        if (sample.MUTATION_COUNT < boundaryVal.min_y) {
                            boundaryVal.min_y = sample.MUTATION_COUNT;
                        }
                        if (sample.MUTATION_COUNT > boundaryVal.max_y) {
                            boundaryVal.max_y = sample.MUTATION_COUNT;
                        }
                    }
                }
            }
        });
    }

    return {
        init: function(_arr) {
            initData(_arr);
            initPage();
            initComponent();
            initStatus = true;
        },

        getScatterPlot: function() {
            if(scatterPlot === undefined){
                return false;
            }else{
                return scatterPlot;
            }
        },
        
        getInitStatus: function() {
            return initStatus;
        },
        
        redraw: redraw,
        setStyle: setStyle,
        redrawByAttribute: redrawByAttribute,
        setClickedCasesId: function(_caseId) {
            clickedCaseId = _caseId;
        },
        setShiftClickedCasesId: function(_caseIds) {
            shiftClickedCaseIds = _caseIds;
        },
        setBrushedCaseId: function(_caseIds) {
            brushedCaseIds = _caseIds;
        },
        getclearFlag: function() {
            return clearFlag;
        },
        setclearFlag: function(_flag) {
            clearFlag = _flag;
        }
    };
})();