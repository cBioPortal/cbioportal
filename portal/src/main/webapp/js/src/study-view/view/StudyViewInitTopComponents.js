/*
 * View for Top Components 
 * 
 *                                       
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */

var StudyViewInitTopComponents = (function() {
    var parObject = {
            studyId: "",
            caseIds: "",
            cnaProfileId: "",
            mutationProfileId: "",
        };
        
    function liClickCallBack(_id, _text) {
        StudyViewInitCharts.createNewChart(_id, _text);
    };
    
        
    function addEvents() {
        
        $('#study-view-header-left-2').unbind('click');
        $('#study-view-header-left-2').click(function (){
            dc.filterAll();
            dc.redrawAll();
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
    }
    
    function changeHeader(_filteredResult, _numOfCases, _removedChart){
        var _caseID = [],
            _resultLength = _filteredResult.length,
            _charts = StudyViewInitCharts.getCharts();
    
        for(var i=0; i<_filteredResult.length ; i++){
            _caseID.push(_filteredResult[i].CASE_ID);
        }
        
        if(_resultLength === _numOfCases){
            var _hasFilter = false;
            
            for(var i=0; i<_charts.length; i++){
                if(_removedChart.indexOf(i) === -1){
                    if (_charts[i].getChart().filters().length > 0)
                        _hasFilter = true;
                }
            }
            if(_hasFilter){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','block');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-3").text(_resultLength + " cases are selected.");
                $("#study-view-header-left-case-ids").val(_caseID.join(" "));
            }else{
                $("#study-view-header-left-0").css('display','block');
                $("#study-view-header-left-1").css('display','none');
                $("#study-view-header-left-2").css('display','none');
                $("#study-view-header-left-3").css('display','none');
            }
        }else{
            if(_resultLength === 0){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','none');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-2").text('Reset');
                $("#study-view-header-left-3").text("No case is selected.");
                $("#study-view-header-left-case-ids").val(_caseID.join(" "));
            }else if(_resultLength === 1){
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','none');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-2").text('Clear selected case');
                $("#study-view-header-left-3").html("");
                $("#study-view-header-left-3")
                        .append("<a title='Go to patient-centric view' " + 
                        "href='case.do?cancer_study_id=" + parObject.studyId +
                        "&amp;case_id=" + _caseID[0] + "'>" + _caseID[0] + 
                        "</a>" + " is selected.");                
            }else{
                $("#study-view-header-left-0").css('display','none');
                $("#study-view-header-left-1").css('display','block');
                $("#study-view-header-left-2").css('display','block');
                $("#study-view-header-left-3").css('display','block');
                $("#study-view-header-left-2").text('Clear selected cases');
                $("#study-view-header-left-3").text(_resultLength + " cases are selected.");
                $("#study-view-header-left-case-ids").val(_caseID.join(" "));
            }
        }
    }
    
    function initComponents() {
        AddCharts.init();
        AddCharts.initAddChartsButton(StudyViewInitCharts.getShowedChartsInfo());
        AddCharts.liClickCallback(liClickCallBack);
    }
    
    function initParameters(o) {
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
    }
    
    function createDiv() {
        var _newElement = StudyViewBoilerplate.headerLeftDiv(),
            _headerLeftQtip = jQuery.extend(true, {}, StudyViewBoilerplate.headerCaseSelectCustomDialog);
        
        $("#study-view-header-function").append(_newElement);
        $("#study-view-header-function").append(StudyViewBoilerplate.customDialogDiv);
        $("#study-view-header-left-cancer_study-ids").val(parObject.studyId);
        $("#study-view-header-function").append(StudyViewBoilerplate.tutorialDiv);
        _headerLeftQtip.position.target = $(window);
        _headerLeftQtip.content.text = $('#study-view-case-select-custom-dialog');
        $('#study-view-header-left-0').qtip(_headerLeftQtip);;
    }
    
    return {
        init: function(_data) {
            initParameters(_data);
            createDiv();
            initComponents();
            addEvents();
        },
        
        changeHeader: changeHeader
    };
})();