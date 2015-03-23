<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


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
