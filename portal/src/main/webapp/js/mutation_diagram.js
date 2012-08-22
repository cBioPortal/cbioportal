// usage: log('inside coolFunc',this,arguments);
// http://paulirish.com/2009/log-a-lightweight-wrapper-for-consolelog/
window.log = function() {
  log.history = log.history || [];   // store logs to an array for reference
  log.history.push(arguments);
  if (this.console) {
    console.log(Array.prototype.slice.call(arguments));
  }
};

function drawMutationDiagram(sequences)
{
  var MAX_OFFSET = 4;
  var PAPER_WIDTH = 740; // width of the raphael box
  var PAPER_HEIGHT = 220; // height of the raphael box
  var sequenceColor = "rgb(186, 189, 182)";
  var scaleColors = [ "rgb(85, 87, 83)", "rgb(46, 52, 54)" ];
  var mutationColors = [ "rgb(251, 154, 153)", "rgb(227, 26, 28)", "rgb(253, 191, 111)", "rgb(255, 127, 0)" ];

  var x = 45; // starting x-coordinate (for the origin)
  var y = 0; // starting y-coordinate  (for the origin)
  var w = PAPER_WIDTH - (2 * x); // width of the diagram
  var h = PAPER_HEIGHT; // height of the diagram
  var c = (2 * h) / 3;
  var mutationDiagram = sequences[0];

  // if mutation diagram is available, then show the diagram tooltip box
  if (mutationDiagram != null)
  {
      $("#mutation_diagram_details_" + mutationDiagram.metadata.hugoGeneSymbol).show();
  }
  // else abort drawing
  else
  {
      return;
  }

  var l = mutationDiagram.length;
  var id = mutationDiagram.metadata.hugoGeneSymbol;
  var label = mutationDiagram.metadata.identifier; // main label text on top of the diagram
  var title = mutationDiagram.metadata.identifier + ", " +
              mutationDiagram.metadata.description + " (" + l + "aa)";

  var paper = Raphael("mutation_diagram_" + id, PAPER_WIDTH, PAPER_HEIGHT);

  // main label on the top
  paper.text(10, 26, label).attr({"text-anchor": "start", "font-size": "12px", "font-family": "sans-serif"});

  // label for y-axis
  var yaxis = paper.text(-27, 105, "# Mutations").attr({"text-anchor": "start", "font-size": "12px", "font-family": "sans-serif"});
  yaxis.rotate(270);

  // sequence (as a rectangle on x-axis)
  paper.rect(x, c - 6, scaleHoriz(Math.max(l, 100), w, l), 13)
    .attr({"fill": sequenceColor, "stroke": "none", "title": title});

  // sequence scale
  var sequenceScaleY = c + 20;

  paper.path("M" + x + " " + (sequenceScaleY + 6) +
             "L" + x + " " + sequenceScaleY +
             "L" + (x + scaleHoriz(l, w, l)) + " " + sequenceScaleY +
             "L" + (x + scaleHoriz(l, w, l)) + " " + (sequenceScaleY + 6))
    .attr({"stroke": scaleColors[0], "stroke-width": 1});

  // sequence scale minor ticks
  for (i = 50; i < l; i += 100) {
    paper.path("M" + (x + scaleHoriz(i, w, l)) + " " + sequenceScaleY +
               "L" + (x + scaleHoriz(i, w, l)) + " " + (sequenceScaleY + 2))
      .attr({"stroke": scaleColors[0], "stroke-width": 1});
  }

  // sequence scale major ticks
  for (i = 0; i < l; i += 100) {
    paper.path("M" + (x + scaleHoriz(i, w, l)) + " " + sequenceScaleY +
               "L" + (x + scaleHoriz(i, w, l)) + " " + (sequenceScaleY + 4))
      .attr({"stroke": scaleColors[0], "stroke-width": 1});
  }

  // sequence scale labels
  for (i = 0; i < l; i += 100) {
    if ((l < 1000) || ((i % 500) == 0)) {
      if (scaleHoriz(l - i, w, l) > 30) {
        paper.text(x + scaleHoriz(i, w, l), sequenceScaleY + 16, i)
          .attr({"text-anchor": "middle", "fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"});
      }
    }
  }

  paper.text(x + scaleHoriz(l, w, l), sequenceScaleY + 16, l + " aa")
   .attr({"text-anchor": "middle", "fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"});

  // regions (as rectangles on the sequence rectangle)
  for (i = 0, size = mutationDiagram.regions.length; i < size; i++) {
    regionX = x + scaleHoriz(mutationDiagram.regions[i].start, w, l);
    regionY = c - 10;
    regionW = scaleHoriz(mutationDiagram.regions[i].end - mutationDiagram.regions[i].start, w, l);
    regionH = 20;
    regionFillColor = mutationDiagram.regions[i].colour;
    regionStrokeColor = darken(regionFillColor);
    regionLabel = mutationDiagram.regions[i].text;
    regionMetadata = mutationDiagram.regions[i].metadata;
    regionTitle = regionMetadata.identifier + " " + regionMetadata.type.toLowerCase() + ", " + regionMetadata.description + " (" + mutationDiagram.regions[i].start + " - " + mutationDiagram.regions[i].end + ")";

    // region rectangle
    regionRect = paper.rect(regionX, regionY, regionW, regionH)
      .attr({"fill": regionFillColor, "stroke-width": 1, "stroke": regionStrokeColor});

    addMouseOver(regionRect.node, regionTitle, id);

    // region label (only if it fits)
    if (regionLabel != null) {
      if ((regionLabel.length * 10) < regionW) {
        currentText = paper.text(regionX + (regionW / 2), regionY + regionH - 10, regionLabel)
          .attr({"text-anchor": "center", "font-size": "12px", "font-family": "sans-serif", "fill": "white"});

        addMouseOver(currentText.node, regionTitle, id);
      }
      else {
        truncatedLabel = regionLabel.substring(0,3) + "..";

        if (truncatedLabel.length * 6 < regionW) {
          currentText = paper.text(regionX + (regionW / 2), regionY + regionH - 10, truncatedLabel)
            .attr({"text-anchor": "center", "font-size": "12px", "font-family": "sans-serif", "fill": "white"});

          addMouseOver(currentText.node, regionTitle, id);
        }
      }
    }
  }

  // mutation scale
  var maxCount = 0;

  for (i = 0, size = mutationDiagram.markups.length; i < size; i++) {
    if ((mutationDiagram.markups[i].type == "mutation") && (parseInt(mutationDiagram.markups[i].metadata.count) >= maxCount)) {
      maxCount = mutationDiagram.markups[i].metadata.count;
    }
  }
  maxCount = maxCount + MAX_OFFSET;

  var scaleX = x - 15;
  var scaleY = c - 8;
  var per = (h / 3) / maxCount;
  var scaleH = maxCount * per;
  //scaleW = scaleHoriz(8, w, l);
  var scaleW = 10; // no need to scale width, set a fixed value

  paper.path("M" + scaleX + " " + scaleY +
             "L" + (scaleX + scaleW) + " " + scaleY +
             "L" + (scaleX + scaleW) + " " + (scaleY - scaleH) +
             "L" + scaleX + " " + (scaleY - scaleH))
   .attr({"stroke": scaleColors[0], "stroke-width": 1});

  // mutation scale major ticks
  paper.path("M" + (scaleX + scaleW - 4) + " " + (scaleY - (maxCount / 2) * per) +
             "L" + (scaleX + scaleW) + " " + (scaleY - (maxCount / 2) * per))
   .attr({"stroke": scaleColors[0], "stroke-width": 1});

  // mutation scale minor ticks
  paper.path("M" + (scaleX + scaleW - 2) + " " + (scaleY - (3 * maxCount / 4) * per) +
             "L" + (scaleX + scaleW) + " " + (scaleY - (3 * maxCount / 4) * per))
   .attr({"stroke": scaleColors[0], "stroke-width": 1});

  paper.path("M" + (scaleX + scaleW - 2) + " " + (scaleY - (maxCount / 4) * per) +
             "L" + (scaleX + scaleW) + " " + (scaleY - (maxCount / 4) * per))
   .attr({"stroke": scaleColors[0], "stroke-width": 1});

  // mutation scale labels
  paper.text(scaleX - 8, scaleY, 0)
    .attr({"text-anchor": "middle", "fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"})

  paper.text(scaleX - 8, scaleY - (maxCount * per), maxCount)
    .attr({"text-anchor": "middle", "fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"})

  // mutation lollipops
  var labelShown = false;

  for (i = 0, size = mutationDiagram.markups.length; i < size; i++) {
    if (mutationDiagram.markups[i].type == "mutation") {
      var x1 = x + scaleHoriz(mutationDiagram.markups[i].start, w, l);
      var y1 = c - 12;
      var x2 = x1;
      var y2 = c - 12 - (per * mutationDiagram.markups[i].metadata.count);
      var lollipopFillColor = mutationDiagram.markups[i].colour[0];
      var lollipopStrokeColor = darken(lollipopFillColor);
      var markupLineColor = mutationDiagram.markups[i].lineColour;

      if (mutationDiagram.markups[i].metadata.count == 1) {
        countText = "(" + mutationDiagram.markups[i].metadata.count + " mutation)";
      }
      else {
        countText = "(" + mutationDiagram.markups[i].metadata.count + " mutations)";
      }

      var mutationTitle = "Amino Acid Change:  " +
        mutationDiagram.markups[i].metadata.label + " " +
        countText;

      var p = paper.path("M" + x1 + " " + (c - 7) + "L" + x2 + " " + y2)
        .toBack()
        .attr({"stroke": markupLineColor, "stroke-width": 1});

      addMouseOver(p.node, mutationTitle, id);

      var lollipop = paper.circle(x2, y2, 3, 3)
        .attr({"fill": lollipopFillColor, "stroke": lollipopStrokeColor, "stroke-width": 0.5});

      addMouseOver(lollipop.node, mutationTitle, id);

      // draws the label for the max count
      if (mutationDiagram.markups[i].metadata.label &&
          (maxCount == mutationDiagram.markups[i].metadata.count + MAX_OFFSET) &&
          !labelShown)
      {
        var maxCountLabel = paper.text(x1, y2 - 12, mutationDiagram.markups[i].metadata.label)
          .attr({"fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"});

        // adjust the label if it overlaps the y-axis
        adjustMaxCountLabel(maxCountLabel, x);

        labelShown = true;
      }
    }
  }
}

/**
 * Adjusts the label on the lollipop if it overlaps y-axis.
 *
 * @param maxCountLabel label (raphael text object) to be adjusted
 * @param axisX         x coordinate of the y-axis
 */
function adjustMaxCountLabel(maxCountLabel, axisX)
{
    var topLeftX = maxCountLabel.getBBox().x;

    // check if the label is overlapping the y-axis
    if (topLeftX < axisX)
    {
        var shiftX = axisX - topLeftX; // required shift for x-coordinate
        var shiftY = 0; // no shift required for y-coordinate

        maxCountLabel.transform("t" + shiftX + "," + shiftY);
    }
}

function scaleHoriz(x, w, l) {
  return x * (w/l);
}

function darken(color) {
  rgb = Raphael.getRGB(color);
  hsb = Raphael.rgb2hsb(rgb.r, rgb.g, rgb.b);
  return Raphael.hsb(hsb.h, hsb.s, Math.max(0, hsb.b - (hsb.b * 0.20)));
}

function addMouseOver(node, txt, id){
  node.style.cursor = "default"
  node.onmouseover = function () {
    $('#mutation_diagram_details_' + id).html(txt+"<BR>&nbsp;<BR>&nbsp;")
  };

  node.onmouseout = function () {
    $('#mutation_diagram_details_' + id).html("The height of the bars indicates the number of mutations at each position.<BR>Roll-over the dots and domains to view additional details.<br>&nbsp;");
  };
}
