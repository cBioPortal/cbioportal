/*
 * View for Top Components 
 * 
 *                                       
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */

var StudyViewInitTopComponents = (function() {
    function liClickCallBack(_id, _text) {
        StudyViewInitCharts.createNewChart(_id, _text);
    };
    
        
    function addEvents() {
        
        $('#study-view-header-left-2').unbind('click');
        $('#study-view-header-left-2').click(function (){
            var i,
                _charts = StudyViewInitCharts.getCharts(),
                _chartsLength = _charts.length;
            
            StudyViewInitScatterPlot.setclearFlag(true);
            
            //Previous using dc.filterAll(), but this will redraw word cloud
            //several times based on the number of charts. Right now, only
            //redraw word cloud if the chart has filter
            for( i = 0; i < _chartsLength; i++){
                if(_charts[i] !== "" && 
                        _charts[i].getChart().filter() !== null){
                    
                    _charts[i].getChart().filter(null);
                }
            }
            
            //If set the filter to null the update scatterplot in charts do
            //not work, so need to update scatter plot here
            //StudyViewInitCharts.redrawScatter();
            
            dc.redrawAll();
            StudyViewInitCharts.resetBars();
            StudyViewInitCharts.redrawSpecialPlots();
            setTimeout(function() {
                StudyViewInitScatterPlot.setclearFlag(false);
            }, StudyViewParams.summaryParams.transitionDuration);
            StudyViewInitCharts.changeHeader();
        });
        
        
        $("#study-view-case-select-custom-submit-btn").click(function() {
            var caseIds = $('#study-view-case-select-custom-input').val().trim().split(/\s+/);
            StudyViewInitCharts.filterChartsByGivingIDs(caseIds);
            $('#study-view-header-left-0').qtip('toggle');
        });
        
        $("#study-view-tutorial").click(function() {
            StudyViewInitIntroJS.init();
        });

        $("#study-view-header-left-4").click(function() {
            var _url;
            var _selectedCaseIds = StudyViewInitCharts.getSelectedCasesID();
            var _selectedPatientIds = StudyViewProxy.getPatientIdsBySampleIds(_selectedCaseIds);
            
            _selectedPatientIds = _selectedPatientIds.sort();
            _url =  "case.do?cancer_study_id="+
                    StudyViewParams.params.studyId+
                    "&case_id="+_selectedPatientIds[0]+
                    "#nav_case_ids="+_selectedPatientIds.join(",");
            
            window.open(_url);
        });
    }
    
    function changeHeader(_filteredResult, _numOfCases, _removedChart){
        var _caseID = [],
            _resultLength = _filteredResult.length,
            _charts = StudyViewInitCharts.getCharts(),
            
            //Check whether page has been scrolled or not, The position of 
            //left-3 will be different
            windowScolled = false; 
    
        for(var i=0; i<_filteredResult.length ; i++){
            _caseID.push(_filteredResult[i].CASE_ID);
        }
        

//        $("#study-view-header-left-2").css('left','210px');
        
        //StudyViewWindowEvents will return the page scrolling status, if the
        //header is on the top of page windowScorlled = false.
//        windowScolled = StudyViewWindowEvents.getScrollStatus();
     

        $("#study-view-header-left-1").css('display','block');
        $("#study-view-header-left-4").css('display','block');
        
        if(_resultLength === _numOfCases){
            var _hasFilter = false,
                _plotDataFlag = StudyViewInitCharts.getPlotDataFlag();
            
            for(var i=0; i<_charts.length; i++){
                if(_removedChart.indexOf(i) === -1){
                    if (_charts[i].getChart().filters().length > 0)
                        _hasFilter = true;
                }
            }
            if(_hasFilter){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").val('Query selected cases');
                $("#study-view-header-left-4").val('View selected cases');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-3").text(_resultLength + " cases are selected.");
            }else if(_plotDataFlag){
                $("#study-view-header-left-0").css('display','block');
                $("#study-view-header-left-1").val('Query all cases');
                $("#study-view-header-left-4").val('View all cases');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','none');
            }else {
                $("#study-view-header-left-0").css('display','block');
                $("#study-view-header-left-1").val('Query all cases');
                $("#study-view-header-left-4").val('View all cases');
                $("#study-view-header-left-2").css('display','none');
                $("#study-view-header-left-3").css('display','none');
            }
        }else{
            if(_resultLength === 0){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','none');
                $("#study-view-header-left-4").css('display','none');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-3").text("No case is selected.");
                $("#study-view-header-left-2").val('Reset all');
            }else if(_resultLength === 1){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").val('Query selected cases');
                $("#study-view-header-left-4").css('display','none');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-2").val('Reset all');
                $("#study-view-header-left-3").html("");
                $("#study-view-header-left-3")
                        .append("<a title='Go to sample view' href='"
                        + cbio.util.getLinkToSampleView(StudyViewParams.params.studyId, _caseID[0])
                        + "'><span style='color: red'>" + _caseID[0] + 
                        "</span></a>" + " is selected.");                
            }else{
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").val('Query selected cases');
                $("#study-view-header-left-4").val('View selected cases');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-2").val('Reset all');
                $("#study-view-header-left-3").text(_resultLength + " cases are selected.");
            }
        }
        $("#study-view-header-left-case-ids").val(StudyViewProxy.getPatientIdsBySampleIds(_caseID).join(" "));
    }
    
    function initAddCharts() {
        AddCharts.init();
        AddCharts.initAddChartsButton(StudyViewInitCharts.getShowedChartsInfo());
        AddCharts.liClickCallback(liClickCallBack);
    }
    
    function createDiv() {
        var _newElement = StudyViewBoilerplate.headerLeftDiv(),
            _headerLeftQtip = jQuery.extend(true, {}, StudyViewBoilerplate.headerCaseSelectCustomDialog);
        
        $("#study-view-header-function").append(_newElement);
        $("#study-view-header-function").append(StudyViewBoilerplate.customDialogDiv);
        $("#study-view-header-left-cancer_study-ids").val(StudyViewParams.params.studyId);
        $("#study-view-header-left-case-ids").val(StudyViewParams.params.patientIds.join(" "));
        //$("#study-view-header-function").append(StudyViewBoilerplate.tutorialDiv);
        _headerLeftQtip.position.target = $(window);
        _headerLeftQtip.content.text = $('#study-view-case-select-custom-dialog');
        $('#study-view-header-left-0').qtip(_headerLeftQtip);
        
        initAddCharts();
    }
    
    return {
        init: function() {
            createDiv();
            addEvents();
        },
        
        changeHeader: changeHeader
    };
})();