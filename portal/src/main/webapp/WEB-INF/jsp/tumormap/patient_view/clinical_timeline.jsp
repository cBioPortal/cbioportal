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
            var circle = d3.selectAll(".timelineSeries_0").filter(function (x) {
                if (x.tooltip_tables.length === 1) {
                    return x.tooltip_tables[0].filter(function(x) {
                        return x[0] === "SpecimenReferenceNumber" || x[0] === "SPECIMEN_REFERENCE_NUMBER";
                    })[0][1] === su2cSampleId;
                }
                else {
                    return undefined;
                }
            });
            if (circle[0][0]) {
                var g = document.createElementNS("http://www.w3.org/2000/svg", "g");
                $(g).attr("transform","translate("+circle.attr("cx")+","+circle.attr("cy")+")");
                $(circle[0]).removeAttr("cx");
                $(circle[0]).removeAttr("cy");
                $(circle[0]).removeAttr("style");
                $(circle[0]).qtip('destroy');
                $(circle[0]).unbind('mouseover mouseout');
                $(circle[0]).wrap(g);
                g = $(circle[0]).parent();
                g.prop("__data__", $(circle[0]).prop("__data__"));
                fillColorAndLabelForCase(d3.select(g.get(0)), caseId);
                clinicalTimeline.addDataPointTooltip(g);
            }
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

                var timeData = clinicalTimelineParser(data);
                if (timeData.length===0) return;

                // add DECEASED point to STATUS track
                if ("CAISIS_OS_STATUS" in patientInfo &&
                    patientInfo["CAISIS_OS_STATUS"] === "DECEASED" &&
                    "CAISIS_OS_MONTHS" in patientInfo) {
                    var days = parseInt(parseInt(patientInfo["CAISIS_OS_MONTHS"])*30.4);
                    console.log(days);
                    var timePoint = {
                        "starting_time":days,
                        "ending_time":days,
                        "display":"circle",
                        "color": "#000",
                        "tooltip_tables":[
                            [
                                ["START_DATE", days],
                                ["STATUS", "DECEASED"]
                            ]
                        ]
                    }

                    var trackData = timeData.filter(function(x) {
                       return x.label === "STATUS";
                    })[0];

                    if (trackData) {
                        trackData.times = trackData.times.concat(timePoint);
                    } else {
                        timeData = timeData.concat({
                            "label":"STATUS",
                            "times":[timePoint]
                        });
                    }
                }

                var width = $("#td-content").width() - 75;
                var timeline = clinicalTimeline
                        .width(width)
                        .data(timeData)
                        .divId("#timeline")
                        .setTimepointsDisplay("IMAGING", "square")
                        .orderTracks(["SPECIMEN", "SURGERY", "STATUS", "DIAGNOSTICS", "DIAGNOSTIC", "IMAGING", "LAB_TEST", "TREATMENT"])
                        .splitByClinicalAttribute("LAB_TEST", "TEST")
                        .sizeByClinicalAttribute("PSA", "RESULT")
                        .sizeByClinicalAttribute("ALK", "RESULT")
                        .sizeByClinicalAttribute("TEST", "RESULT")
                        .sizeByClinicalAttribute("HGB", "RESULT")
                        .sizeByClinicalAttribute("PHOS", "RESULT")
                        .sizeByClinicalAttribute("LDH", "RESULT")
                        .splitByClinicalAttribute("TREATMENT", "AGENT")
                        .collapseAll()
                        .toggleTrackCollapse("SPECIMEN")
                        .enableTrackTooltips(false)
                        .addPostTimelineHook(plotCaseLabelsInTimeline);
                timeline();
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
