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

  var div = d3.select("#mutation_diagram_" + mutationDiagram.id)
    .append("div");

  var svg = div.append("svg:svg")
    .attr("width", 700)
    .attr("height", 220);

  // label
  svg.append("svg:text")
    .attr("x", "10")
    .attr("y", "26")
    .attr("text-anchor", "start")
    .style("font-size", "16px")
    .style("font-family", "sans-serif")
    .text(mutationDiagram.label);

  // sequence
  svg.append("svg:rect")
   .attr("x", x)
   .attr("y", c - 6)
   .attr("width", scaleHoriz(Math.max(l, 100), w, l))
   .attr("height", 13)
   //.attr("class", "sequence")
   //.attr("title", mutationDiagram.label + " (1 - " + mutationDiagram.length + ")")
   .style("fill", sequenceColor)
   .style("stroke", "none")
   .append("svg:title")
     .text(mutationDiagram.label + " (1 - " + l + ")");

  // sequence scale
  sequenceScaleY = c + 20;

  svg.append("svg:line")
    .attr("x1", x)
    .attr("y1", sequenceScaleY + 6)
    .attr("x2", x)
    .attr("y2", sequenceScaleY)
    .style("stroke", scaleColors[0])
    .style("stroke-width", 1);

  svg.append("svg:line")
    .attr("x1", x)
    .attr("y1", sequenceScaleY)
    .attr("x2", x + scaleHoriz(l, w, l))
    .attr("y2", sequenceScaleY)
    .style("stroke", scaleColors[0])
    .style("stroke-width", 1);

  svg.append("svg:line")
    .attr("x1", x + scaleHoriz(l, w, l))
    .attr("y1", sequenceScaleY)
    .attr("x2", x + scaleHoriz(l, w, l))
    .attr("y2", sequenceScaleY + 6)
    .style("stroke", scaleColors[0])
    .style("stroke-width", 1);

  // sequence scale minor ticks
  for (i = 50; i < l; i += 100) {
    svg.append("svg:line")
      .attr("x1", x + scaleHoriz(i, w, l))
      .attr("y1", sequenceScaleY)
      .attr("x2", x + scaleHoriz(i, w, l))
      .attr("y2", sequenceScaleY + 2)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);
  }

  // sequence scale major ticks
  for (i = 0; i < l; i += 100) {
    svg.append("svg:line")
      .attr("x1", x + scaleHoriz(i, w, l))
      .attr("y1", sequenceScaleY)
      .attr("x2", x + scaleHoriz(i, w, l))
      .attr("y2", sequenceScaleY + 4)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);

    // sequence scale labels
    if (scaleHoriz(l - i, w, l) > 30) { 
      svg.append("svg:text")
        .attr("x", x + scaleHoriz(i, w, l))
        .attr("y", sequenceScaleY + 17)
        .attr("text-anchor", "middle")
        .style("fill", scaleColors[1])
        .style("font-size", "11px")
        .style("font-family", "sans-serif")
        .text(i);
    }
  }

  svg.append("svg:text")
    .attr("x", x + scaleHoriz(l, w, l))
    .attr("y", sequenceScaleY + 17)
    .attr("text-anchor", "middle")
    .style("fill", scaleColors[1])
    .style("font-size", "11px")
    .style("font-family", "sans-serif")
    .text(l + " aa");

  // domains
  for (i = 0, size = mutationDiagram.domains.length; i < size; i++)
  {
    domainX = x + scaleHoriz(mutationDiagram.domains[i].start, w, l);
    domainY = c - 10;
    domainW = scaleHoriz(mutationDiagram.domains[i].end - mutationDiagram.domains[i].start, w, l);
    domainH = 20;
    label = mutationDiagram.domains[i].label;

    svg.append("svg:rect")
      .attr("x", domainX)
      .attr("y", domainY)
      .attr("width", domainW)
      .attr("height", domainH)
      //.attr("class", "domain")
      //.attr("title", mutationDiagram.domains[i].label + " (" + mutationDiagram.domains[i].start + " - " + mutationDiagram.domains[i].end + ")")
      .style("fill", domainColors[i % 4])
      .style("stroke-width", "1")
      .style("stroke", domainStrokeColors[i % 4])
      .append("svg:title")
        .text(label + " domain (" + mutationDiagram.domains[i].start + " - " + mutationDiagram.domains[i].end + ")");


    if ((label.length * 5) < domainW) {
      svg.append("svg:text")
        .attr("x", domainX + (domainW / 2))
        //.attr("y", domainY + domainH + 14)
        .attr("y", domainY + domainH - 6)
        .attr("text-anchor", "middle")
        .style("font-size", "11px")
        .style("font-family", "sans-serif")
        .text(label)
        .append("svg:title")
          .text(label + " domain (" + mutationDiagram.domains[i].start + " - " + mutationDiagram.domains[i].end + ")");
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
    svg.append("svg:line")
      .attr("x1", scaleX)
      .attr("y1", scaleY)
      .attr("x2", scaleX + scaleW)
      .attr("y2", scaleY)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);

    svg.append("svg:line")
      .attr("x1", scaleX + scaleW)
      .attr("y1", scaleY)
      .attr("x2", scaleX + scaleW)
      .attr("y2", scaleY - scaleH)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);

    svg.append("svg:line")
      .attr("x1", scaleX + scaleW)
      .attr("y1", scaleY - scaleH)
      .attr("x2", scaleX)
      .attr("y2", scaleY - scaleH)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);

    // mutation scale major ticks
    svg.append("svg:line")
      .attr("x1", scaleX + scaleW - 4)
      .attr("y1", scaleY - (maxCount / 2) * per)
      .attr("x2", scaleX + scaleW)
      .attr("y2", scaleY - (maxCount / 2) * per)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);

    // mutation scale minor ticks
    svg.append("svg:line")
      .attr("x1", scaleX + scaleW - 2)
      .attr("y1", scaleY - (maxCount / 4) * per)
      .attr("x2", scaleX + scaleW)
      .attr("y2", scaleY - (maxCount / 4) * per)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);

    svg.append("svg:line")
      .attr("x1", scaleX + scaleW - 2)
      .attr("y1", scaleY - ((3 * maxCount) / 4) * per)
      .attr("x2", scaleX + scaleW)
      .attr("y2", scaleY - ((3 * maxCount) / 4) * per)
      .style("stroke", scaleColors[0])
      .style("stroke-width", 1);

    // mutation scale labels
    svg.append("svg:text")
      .attr("x", scaleX - 8)
      .attr("y", scaleY + 4)
      .attr("text-anchor", "middle")
      .style("fill", scaleColors[1])
      .style("font-size", "11px")
      .style("font-family", "sans-serif")
      .text("0");

    svg.append("svg:text")
      .attr("x", scaleX - 8)
      .attr("y", scaleY + 4 - (maxCount * per))
      .attr("text-anchor", "middle")
      .style("fill", scaleColors[1])
      .style("font-size", "11px")
      .style("font-family", "sans-serif")
      .text(maxCount);
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
      svg.append("svg:line")
        .attr("x1", x1)
        .attr("y1", y1)
        .attr("x2", x2)
        .attr("y2", y2)
        //.attr("class", "mutation")
        //.attr("title", mutationDiagram.mutations[i].label + " (" + mutationDiagram.mutations[i].count + ")")
        .style("stroke", mutationColors[i % 4])
        .style("stroke-width", 2)
        .append("svg:title")
          .text(mutationDiagram.mutations[i].label + " mutation (" + mutationDiagram.mutations[i].count + ")");
    }
    else
    {
      per = (h / 3) / 8;
      y2 = c - 12 - (per * mutationDiagram.mutations[i].count);
      for (j = 0, count = mutationDiagram.mutations[i].count; j < count; j++)
      {
        svg.append("svg:circle")
          .attr("cx", x2)
          .attr("cy", y1 - (j * per) - per/2)
          .attr("r", per/2)
          //.attr("class", "mutation")
          //.attr("title", mutationDiagram.mutations[i].label + " (" + mutationDiagram.mutations[i].count + ")")
          .style("fill", mutationColors[i % 4])
          .append("svg:title")
            .text(mutationDiagram.mutations[i].label + " mutation (" + mutationDiagram.mutations[i].count + ")");
      }
    }

    if (mutationDiagram.mutations[i].label && (maxCount == mutationDiagram.mutations[i].count))
    {
      svg.append("svg:text")
        .attr("x", x1)
        .attr("y", y2 - 4)
        .attr("text-anchor", "middle")
        .style("fill", mutationColors[i % 4])
        .style("font-size", "11px")
        .style("font-family", "sans-serif")
        .text(mutationDiagram.mutations[i].label);
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