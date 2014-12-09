
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

  <script src="js/src/patient-view/clinical-timeline.js?<%=GlobalProperties.getAppVersion()%>"></script>

  <style type="text/css">
    .axis path,
    .axis line {
      fill: none;
      stroke: black;
      shape-rendering: crispEdges;
    }

    .axis text {
      font-family: sans-serif;
      font-size: 10px;
    }

    .timeline-label {
      font-family: sans-serif;
      font-size: 12px;
    }
    
    #timeline .axis {
      transform: translate(0px,20px);
      -ms-transform: translate(0px,20px); /* IE 9 */
      -webkit-transform: translate(0px,20px); /* Safari and Chrome */
      -o-transform: translate(0px,20px); /* Opera */
      -moz-transform: translate(0px,20px); /* Firefox */
    }

    .coloredDiv {
      height:20px; width:20px; float:left;
    }
  </style>
  
  <script type="text/javascript">


    function plotCaseLabelsInTimeline() {
        for (var i=0; i<caseIds.length; i++) {
            var caseId = caseIds[i];
            var clinicalData = clinicalDataMap[caseId];
            var su2cSampleId = guessClinicalData(clinicalData,["SU2C_SAMPLE_ID"]);
            if (!su2cSampleId) su2cSampleId = caseId;
            fillColorAndLabelForCase(d3.select('.timeline-'+su2cSampleId),caseId);
        }
    }

    $(document).ready(function(){
        
        var params = {
            //type:"diagnostic,treatment,lab_test",
            cancer_study_id:cancerStudyId,
            patient_id:patientId
        };
        
        $.post("clinical_timeline_data.json", 
            params,
            function(data){
                if (cbio.util.getObjectLength(data)===0) return;
                
                var timeData = parepareTimeLineData.prepare(data);
                if (timeData.length===0) return;

                var width = $("#td-content").width() - 75;
                var timeline = clinicalTimeline().itemHeight(12).width(width).colorProperty('color').opacityProperty('opacity').stack();
                var svg = d3.select("#timeline").append("svg").attr("width", width).datum(timeData).call(timeline);
                plotCaseLabelsInTimeline();
                $("#timeline-container").show();
            }
            ,"json"
        );
    });
  </script>

  <fieldset class="fieldset-border">
  <legend class="legend-border">Clinical Events</legend>
  <div id="timeline-container" style="display:hidden">
  <div id="timeline">
  
  </div>
  </div>
  </fieldset>
