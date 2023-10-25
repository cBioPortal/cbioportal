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

  <script src="js/src/patient-view/clinical-timeline.min.js?<%=GlobalProperties.getAppVersion()%>"></script>

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
            var compareAgainstIds = [caseId];
            var OtherSampleId = clinicalData["OTHER_SAMPLE_ID"];
            if (!cbio.util.checkNullOrUndefined(OtherSampleId)) {
                compareAgainstIds = compareAgainstIds.concat(OtherSampleId);
            }
            var circle = d3.selectAll(".timelineSeries_0").filter(function (x) {
                if (x.tooltip_tables.length === 1) {
                    var specRefNum = x.tooltip_tables[0].filter(function(x) {
                        return x[0] === "SpecimenReferenceNumber" || x[0] === "SPECIMEN_REFERENCE_NUMBER" || x[0] === "SAMPLE_ID";
                    })[0];
                    if (specRefNum) {
                        return compareAgainstIds.indexOf(specRefNum[1]) !== -1;
                    }
                }
                return undefined;
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
                window.pvTimeline.addDataPointTooltip(g);
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

                // order specimens by svg label number
                var specimen = _.findWhere(timeData, {label:"Specimen"});
                if (specimen) {
                    specimen.times = _.sortBy(specimen.times, function(x) {
                        var sortOrder = Infinity;

                        var specRefNum = _.filter(x.tooltip_tables[0], function(x) {
                            return x[0] === "SPECIMEN_REFERENCE_NUMBER" || x[0] === "SpecimenReferenceNumber" || x[0] === "SAMPLE_ID";
                        });
                        if (specRefNum) {
                            if (specRefNum.length > 1) {
                                console.warn("More than 1 specimen reference number found in tooltip table");
                            } else if (specRefNum.length === 1) {
                                sortOrder = caseIds.indexOf(specRefNum[0][1]);
                                if (sortOrder === -1) {
                                    sortOrder = Infinity;
                                }
                            }
                        }
                        return sortOrder;
                    });
                }

                // add DECEASED point to Status track using data from
                // .*OS_MONTHS$ if .*OS_STATUS$ is DECEASED
                var i;
                var prefixes;
                prefixes = Object.keys(patientInfo).filter(function (x) {
                    // find all keys postfixed with "OS_STATUS"
                    return /OS_STATUS$/.test(x);
                }).map(function(x) {
                    // get the prefixes
                    return x.substr(0, x.length-"OS_STATUS".length);
                });

                for (i=0; i < prefixes.length; i++) {
                    var prefix = prefixes[i];
                    if (patientInfo[prefix+"OS_STATUS"] === "1:DECEASED" &&
                        prefix + "OS_MONTHS" in patientInfo) {
                        var days = parseInt(parseInt(patientInfo[prefix+"OS_MONTHS"])*30.4);
                        var timePoint = {
                            "starting_time":days,
                            "ending_time":days,
                            "display":"circle",
                            "color": "#000",
                            "tooltip_tables":[
                                [
                                    ["START_DATE", days],
                                    ["STATUS", "1:DECEASED"]
                                ]
                            ]
                        }

                        var trackData = timeData.filter(function(x) {
                           return x.label === "Status";
                        })[0];

                        if (trackData) {
                            trackData.times = trackData.times.concat(timePoint);
                        } else {
                            timeData = timeData.concat({
                                "label":"Status",
                                "times":[timePoint]
                            });
                        }
                        // Add timepoint only once in case of multiple prefixes
                        break;
                    }
                }

                var width = $("#td-content").width() - 75;
                window.pvTimeline = clinicalTimeline()
                        .width(width)
                        .data(timeData)
                        .divId("#timeline")
                        .setTimepointsDisplay("Imaging", "square")
                        .orderTracks(["Specimen", "Surgery", "Status", "Diagnostics", "Diagnostic", "Imaging", "Lab_test", "Treatment"])
                        .splitByClinicalAttributes("Lab_test", "TEST")
                var splitData = window.pvTimeline.data();
                // Get TEST names that have a RESULT field in their clinical
                // tooltip table. We assume the RESULT field contains
                // integer/float values that can be used to size the dots on the
                // timeline by 
                var testsWithResults = splitData.filter(function(x) {
                    return x.parent_track === 'Lab_test' && 
                           _.all(x.times.map(
                                      function(t) {
                                          return t.tooltip_tables.length === 1 && 
                                                 t.tooltip_tables[0].filter(function(a) {return a[0] === 'RESULT'}).length > 0;
                                      })
                    )
                }).map(function(x) {
                    return x.label;
                });
                // Scale dot size on timepoint by RESULT field
                testsWithResults.forEach(function(test) {
                    window.pvTimeline =
                        window.pvTimeline
                        .sizeByClinicalAttribute(test, "RESULT")
                })
                window.pvTimeline =
                        window.pvTimeline
                        .splitByClinicalAttributes("Treatment", ["SUBTYPE", "AGENT"])
                        .collapseAll()
                        .toggleTrackCollapse("Specimen")
                        .enableTrackTooltips(false)
						.plugins([{obj: new trimClinicalTimeline("Trim Timeline"), enabled: true}])
                        .addPostTimelineHook(plotCaseLabelsInTimeline);
                window.pvTimeline();
                $("#timeline-container").show();
            }
            ,"json"
        );
    });
  </script>

  <fieldset id="clinical-timeline-fieldset" class="ui-widget-content">
  <legend class="legend-border">Clinical Events</legend>
  <div id="timeline-container" style="display:hidden">
  <div id="timeline">
  
  </div>
  </div>
  </fieldset>
