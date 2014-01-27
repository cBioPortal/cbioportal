
  <script src="js/src/patient-view/clinical-timeline.js"></script>

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
    
    #timeline2 .axis {
      transform: translate(0px,30px);
      -ms-transform: translate(0px,30px); /* IE 9 */
      -webkit-transform: translate(0px,30px); /* Safari and Chrome */
      -o-transform: translate(0px,30px); /* Opera */
      -moz-transform: translate(0px,30px); /* Firefox */
    }

    .coloredDiv {
      height:20px; width:20px; float:left;
    }
  </style>
  
  <script type="text/javascript">

    $(document).ready(function(){

      var testData = [
        {label:"Diagnostics", display:"circle", times: [{"starting_time": 0, "tooltip":"First diagonosis"},{"starting_time": 200}, {"starting_time": 500}]},
        {label:"Lab Tests", display:"circle", times: [{"starting_time": -10}, ]},
        {label:"Therapy", display:"rect", times: [{"starting_time": 140, "ending_time": 360, "tooltip":"Chemo"}]},
      ];

      var width = $("#td-content").width() - 50;

      function timelineCircle() {
        var timeline = clinicalTimeline()
          .itemHeight(12)
          .stack(); // toggle between rectangles and circles

        var svg = d3.select("#timeline2").append("svg").attr("width", width)
          .datum(testData).call(timeline);
      }

      timelineCircle();
    });
  </script>

  <div>
    <div id="timeline2"></div>
  </div>
