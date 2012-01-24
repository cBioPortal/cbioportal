function drawMutationDiagram(mutationDiagram)
{
  sequenceColor = "rgb(186, 189, 182)";
  scaleColors = [ "rgb(85, 87, 83)", "rgb(46, 52, 54)" ];
  domainColors = [ "rgb(166, 206, 227)", "rgb(31, 120, 180)", "rgb(178, 223, 138)", "rgb(51, 160, 44)" ];
  domainStrokeColors = [ "rgb(145, 181, 199)", "rgb(27, 105, 158)", "rgb(156, 195, 121)", "rgb(44, 140, 38)" ];
  mutationColors = [ "rgb(251, 154, 153)", "rgb(227, 26, 28)", "rgb(253, 191, 111)", "rgb(255, 127, 0)" ];

  x = 40;
  y = 0;
  w = 700 - (2 * x);
  h = 220;
  c = (2 * h) / 3;
  l = mutationDiagram.length;

  var paper = Raphael("diagram_" + mutationDiagram.label, 700, 220);

  // label
  paper.text(10, 26, mutationDiagram.label).attr({"text-anchor": "start", "font-size": "16px", "font-family": "sans-serif"});

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

  // domains
  for (i = 0, size = mutationDiagram.domains.length; i < size; i++)
  {
    domainX = x + scaleHoriz(mutationDiagram.domains[i].start, w, l);
    domainY = c - 10;
    domainW = scaleHoriz(mutationDiagram.domains[i].end - mutationDiagram.domains[i].start, w, l);
    domainH = 20;
    label = mutationDiagram.domains[i].label;

    paper.rect(domainX, domainY, domainW, domainH)
      .attr({"fill": domainColors[i % 4], "stroke-width": 1, "stroke": domainStrokeColors[i % 4]});
//      .append("svg:title")
//        .text(label + " domain (" + mutationDiagram.domains[i].start + " - " + mutationDiagram.domains[i].end + ")");


    if ((label.length * 5) < domainW) {
      paper.text(domainX + (domainW / 2), domainY + domainH - 9, label)
        .attr({"text-anchor": "middle", "font-size": "11px", "font-family": "sans-serif"});
//        .append("svg:title")
//          .text(label + " domain (" + mutationDiagram.domains[i].start + " - " + mutationDiagram.domains[i].end + ")");
    }
  }

  // mutation scale
  maxCount = 0;
  for (i = 0, size = mutationDiagram.mutations.length; i < size; i++)
  {
    if (mutationDiagram.mutations[i].count > maxCount)
    {
      maxCount = mutationDiagram.mutations[i].count;
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
  for (i = 0, size = mutationDiagram.mutations.length; i < size; i++)
  {
    x1 = x + scaleHoriz(mutationDiagram.mutations[i].location, w, l);
    y1 = c - 12;
    x2 = x1;
    y2 = c - 12 - (per * mutationDiagram.mutations[i].count);

    if (maxCount > 4)
    {
      paper.path("M" + x1 + " " + y1 +
                 "L" + x2 + " " + y2)
       .attr({"stroke": mutationColors[i % 4], "stroke-width": 2});
//        .append("svg:title")
//          .text(mutationDiagram.mutations[i].label + " mutation (" + mutationDiagram.mutations[i].count + ")");
    }
    else
    {
      per = (h / 3) / 8;
      y2 = c - 12 - (per * mutationDiagram.mutations[i].count);
      for (j = 0, count = mutationDiagram.mutations[i].count; j < count; j++)
      {
          paper.circle(x2, y1 - (j * per) - (per / 2), per / 2)
              .attr({"fill": mutationColors[i % 4], "stroke": "none"});
//          .append("svg:title")
//            .text(mutationDiagram.mutations[i].label + " mutation (" + mutationDiagram.mutations[i].count + ")");
      }
    }

    if (mutationDiagram.mutations[i].label && (maxCount == mutationDiagram.mutations[i].count))
    {
      paper.text(x1, y2 - 8, mutationDiagram.mutations[i].label)
        .attr({"fill": mutationColors[i % 4], "font-size": "11px", "font-family": "sans-serif"});
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