function drawMutationDiagram(sequences)
{
  sequenceColor = "rgb(186, 189, 182)";
  scaleColors = [ "rgb(85, 87, 83)", "rgb(46, 52, 54)" ];
  mutationColors = [ "rgb(251, 154, 153)", "rgb(227, 26, 28)", "rgb(253, 191, 111)", "rgb(255, 127, 0)" ];

  x = 40;
  y = 0;
  w = 700 - (2 * x);
  h = 220;
  c = (2 * h) / 3;
  mutationDiagram = sequences[0];
  l = mutationDiagram.length;
  label = mutationDiagram.metadata.identifier;

  var paper = Raphael("diagram_" + label, 700, 220);

  // label
  paper.text(10, 26, label).attr({"text-anchor": "start", "font-size": "16px", "font-family": "sans-serif"});

  // sequence
  paper.rect(x, c - 6, scaleHoriz(Math.max(l, 100), w, l), 13).attr({"fill": sequenceColor, "stroke": "none"});
    // for native SVG tooltips:
    //    .append("svg:title")
    //      .text(mutationDiagram.label + " (1 - " + l + ")");

  // sequence scale
  sequenceScaleY = c + 20;

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

    // sequence scale labels
    if (scaleHoriz(l - i, w, l) > 30) { 
      paper.text(x + scaleHoriz(i, w, l), sequenceScaleY + 14, i)
        .attr({"text-anchor": "middle", "fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"});
    }
  }

  paper.text(x + scaleHoriz(l, w, l), sequenceScaleY + 14, l + " aa")
   .attr({"text-anchor": "middle", "fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"});

  // regions
  for (i = 0, size = mutationDiagram.regions.length; i < size; i++)
  {
    regionX = x + scaleHoriz(mutationDiagram.regions[i].start, w, l);
    regionY = c - 10;
    regionW = scaleHoriz(mutationDiagram.regions[i].end - mutationDiagram.regions[i].start, w, l);
    regionH = 20;
    regionFillColor = mutationDiagram.regions[i].colour;
    regionStrokeColor = darken(regionFillColor);
    regionLabel = mutationDiagram.regions[i].text;

    paper.rect(regionX, regionY, regionW, regionH)
      .attr({"fill": regionFillColor, "stroke-width": 1, "stroke": regionStrokeColor});
//      .append("svg:title")
//        .text(label + " domain (" + mutationDiagram.domains[i].start + " - " + mutationDiagram.domains[i].end + ")");


    if ((regionLabel.length * 5) < regionW) {
      paper.text(regionX + (regionW / 2), regionY + regionH - 9, regionLabel)
        .attr({"text-anchor": "middle", "font-size": "11px", "font-family": "sans-serif"});
//        .append("svg:title")
//          .text(label + " domain (" + mutationDiagram.domains[i].start + " - " + mutationDiagram.domains[i].end + ")");
    }
  }

  // mutation scale
  maxCount = 0;
  for (i = 0, size = mutationDiagram.markups.length; i < size; i++)
  {
    if ((mutationDiagram.markups[i].type == "mutation") && (parseInt(mutationDiagram.markups[i].metadata.count) > maxCount))
    {
      maxCount = mutationDiagram.markups[i].metadata.count;
    }
  }

  scaleX = x - 15;
  scaleY = c - 12;
  per = (h / 3) / maxCount;
  scaleH = maxCount * per;
  scaleW = scaleHoriz(8, w, l);

  if (maxCount > 4)
  {
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
      .attr({"fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"})

    paper.text(scaleX - 8, scaleY - (maxCount * per), maxCount)
      .attr({"fill": scaleColors[1], "font-size": "11px", "font-family": "sans-serif"})
  }

  // mutation histograms/pileups
  for (i = 0, size = mutationDiagram.markups.length; i < size; i++)
  {
    if (mutationDiagram.markups[i].type == "mutation")
    {
      x1 = x + scaleHoriz(mutationDiagram.markups[i].start, w, l);
      y1 = c - 12;
      x2 = x1;
      y2 = c - 12 - (per * mutationDiagram.markups[i].metadata.count);

      if (maxCount > 4)
      {
        paper.path("M" + x1 + " " + y1 +
                   "L" + x2 + " " + y2)
         .attr({"stroke": mutationDiagram.markups[i].lineColour, "stroke-width": 2});
//        .append("svg:title")
//          .text(mutationDiagram.mutations[i].label + " mutation (" + mutationDiagram.mutations[i].count + ")");
      }
      else
      {
        per = (h / 3) / 8;
        y2 = c - 12 - (per * mutationDiagram.markups[i].metadata.count);
        for (j = 0, count = mutationDiagram.markups[i].metadata.count; j < count; j++)
        {
            paper.circle(x2, y1 - (j * per) - (per / 2), per / 2)
                .attr({"fill": mutationDiagram.markups[i].colour, "stroke": "none"});
//          .append("svg:title")
//            .text(mutationDiagram.mutations[i].label + " mutation (" + mutationDiagram.mutations[i].count + ")");
        }
      }

      if (mutationDiagram.markups[i].metadata.type && (maxCount == mutationDiagram.markups[i].metadata.count))
      {
        paper.text(x1, y2 - 8, mutationDiagram.markups[i].metadata.type)
          .attr({"fill": mutationDiagram.markups[i].lineColour, "font-size": "11px", "font-family": "sans-serif"});
      }
    }
  }

  //$(".sequence").tipTip();
  //$(".domain").tipTip();
  //$(".mutation").tipTip();
}

function scaleHoriz(x, w, l)
{
  return x * (w/l);
}

function darken(color)
{
  rgb = Raphael.getRGB(color);
  hsb = Raphael.rgb2hsb(rgb.r, rgb.g, rgb.b);
  return Raphael.hsb(hsb.h, hsb.s, Math.max(0, hsb.b - (hsb.b * 0.20)));
}