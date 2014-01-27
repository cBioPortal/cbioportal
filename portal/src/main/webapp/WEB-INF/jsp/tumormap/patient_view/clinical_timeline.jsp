
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
        {label:"Diagnostics", display:"circle", times: [{"starting_time": 0, "tooltip":"First diagonosis"},{"starting_time": 5}, {"starting_time": 30}]},
        {label:"Lab Tests", display:"circle", times: [{"starting_time": 10}, ]},
        {label:"Therapy", display:"rect", times: [{"starting_time": 14, "ending_time": 20}]},
      ];

      var width = $("#td-content").width() - 50;

      function timelineCircle() {
        var timeline = clinicalTimeline()
          .tickFormat({
            format: function(d) {return "day "+d;}, 
            tickValues: [0,5,10,15,20,25,30], 
            tickSize: 8
          })
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
