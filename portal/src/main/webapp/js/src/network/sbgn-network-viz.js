
// Shorthand for $( document ).ready()
$(function() {
  document.title="SBGN View";
  popUpSBGNView();
});

var sbgnStyleSheet = cytoscape.stylesheet()
.selector("node")
.css({
  'border-width': 1.5,
  'border-color': '#555',
  'background-color': '#f6f6f6',
  'font-size': 11,
  //          'shape': 'data(sbgnclass)',
  'background-opacity': '0.5'
})
.selector("node[sbgnclass]")
.css({
  'shape': 'data(sbgnclass)'
})
.selector("node[sbgnclass='complex']")
.css({
  'background-color': '#F4F3EE',
  'expanded-collapsed': 'expanded',
  'text-valign': 'bottom',
  'text-halign': 'center',
  'font-size': '16'
})
.selector("node[sbgnclass='compartment']")
.css({
  'background-opacity': '0',
  'background-color': '#FFFFFF',
  'content': 'data(sbgnlabel)',
  'text-valign': 'bottom',
  'text-halign': 'center',
  'font-size': '16',
  'expanded-collapsed': 'expanded'
})
.selector("node[sbgnclass='submap']")
.css({
  'expanded-collapsed': 'expanded'
})
.selector("node[sbgnclass][sbgnclass!='complex'][sbgnclass!='compartment'][sbgnclass!='submap']")
.css({
  'width': 'data(sbgnbbox.w)',
  'height': 'data(sbgnbbox.h)'
})
.selector("node:selected")
.css({
  'border-color': '#d67614',
  'target-arrow-color': '#000',
  'text-outline-color': '#000'})
  .selector("node:active")
  .css({
    'background-opacity': '0.7', 'overlay-color': '#d67614',
    'overlay-padding': '14'
  })
  .selector("edge")
  .css({
    'line-color': '#555',
    'target-arrow-fill': 'hollow',
    'source-arrow-fill': 'hollow',
    'width': 1.5,
    'target-arrow-color': '#555',
    'source-arrow-color': '#555',
    'target-arrow-shape': 'data(sbgnclass)'
  })
  .selector("edge[sbgnclass='inhibition']")
  .css({
    'target-arrow-fill': 'filled'
  })
  .selector("edge[sbgnclass='consumption']")
  .css({
    'target-arrow-shape': 'none',
    'source-arrow-shape': 'data(sbgnclass)',
    'line-style': 'consumption'
  })
  .selector("edge[sbgnclass='production']")
  .css({
    'target-arrow-fill': 'filled',
    'line-style': 'production'
  })
  .selector("edge:selected")
  .css({
    'line-color': '#d67614',
    'source-arrow-color': '#d67614',
    'target-arrow-color': '#d67614'
  })
  .selector("edge:active")
  .css({
    'background-opacity': '0.7', 'overlay-color': '#d67614',
    'overlay-padding': '8'
  })
  .selector("core")
  .css({
    'selection-box-color': '#d67614',
    'selection-box-opacity': '0.2', 'selection-box-border-color': '#d67614'
  })

  function popUpSBGNView()
  {
    //Get node ids from URL !!
    var container = $('#sbgnCanvas');
    var urlString = window.location.search.substring(1).split("&");
    var nodeA = urlString[0];
    var nodeB = urlString[1];

    //Construct PC2 url from pop up url !
    var pc2URL = "http://www.pathwaycommons.org/pc2/";
    var format = "graph?format=SBGN";
    var kind = "&kind=PATHSBETWEEN";
    var sourceA = "&source="+nodeA;
    var sourceB = "&source="+nodeB;
    var pc2URL = pc2URL + format + kind + sourceA + sourceB;

    //Add loading spinner
    container.append('<i class="fa fa-spinner fa-5x fa-spin"></i>');

    $.get(pc2URL, function(data)
    {
      //Remove loading spinner !
      container.empty();

      //Convert incoming SBGNML string to json
      var graphData = sbgnmlToJson.convert(data)
      var positionMap = {};

      //add position information to data for preset layout
      for (var i = 0; i < graphData.nodes.length; i++) {
        var xPos = graphData.nodes[i].data.sbgnbbox.x;
        var yPos = graphData.nodes[i].data.sbgnbbox.y;
        positionMap[graphData.nodes[i].data.id] = {'x': xPos, 'y': yPos};
      }

      var cy = window.cy = cytoscape(
        {
          container: document.getElementById('sbgnCanvas'),
          elements:graphData,
          style: sbgnStyleSheet,
          layout:
          {
            /*name: 'preset',
            positions: positionMap*/
            name: "cose-bilkent",
            animate: false,
            randomize: true,
            fit: true
          },
          showOverlay: false,
          minZoom: 0.125,
          maxZoom: 16,
          boxSelectionEnabled: true,
          motionBlur: true,
          wheelSensitivity: 0.1,
          ready: function ()
          {
            //refreshPaddings();

            var panProps = ({
              fitPadding: 10,
            });
            container.cytoscapePanzoom(panProps);

            cy.on('mouseover', 'node', function (event) {
            });

            cy.on('cxttap', 'node', function (event)
            {
            });

            cy.on('tap', 'node', function (event)
            {
            });

          }
        });
      });


    }
