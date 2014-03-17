var StudyViewInitSurvivalPlot = (function() {
    
    function createDiv() {
        $("#study-view-charts").append(StudyViewBoilerplate.survivalPlotDiv);
    }
    
    function initPlot(_caseIDs, _data) {
        survivalCurves.init(_caseIDs, _data);
    }
    
    return {
      init: function(_caseIDs, _data) {
          createDiv();
          initPlot(_caseIDs, _data);
      }  
    };
})();