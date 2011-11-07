<%
  String hugoGeneSymbol = request.getParameter("hugoGeneSymbol");
%>
<!DOCTYPE html>
<html>
<head>
  <title>mutation diagram :: <%= hugoGeneSymbol %></title>
  <script type="text/javascript" src="js/d3/d3.js"></script>
  <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
</head>
<body>
  <div id="container">
    <h1><%= hugoGeneSymbol %></h1>
    <script type="text/javascript">
$(document).ready(function () {
  $.ajax({ url: "mutation_diagram_data.json", dataType: "json", data: { hugoGeneSymbol: "<%= hugoGeneSymbol %>" }, success: drawMutationDiagram, type: "POST"});
});

function drawMutationDiagram(mutationDiagram)
{
  domainColors = [ "rgb(166, 206, 227)", "rgb(31, 120, 180)", "rgb(178, 223, 138)", "rgb(51, 160, 44)" ];
  domainStrokeColors = [ "rgb(145, 181, 199)", "rgb(27, 105, 158)", "rgb(156, 195, 121)", "rgb(44, 140, 38)" ];
  mutationColors = [ "rgb(251, 154, 153)", "rgb(227, 26, 28)", "rgb(253, 191, 111)", "rgb(255, 127, 0)" ];

  x = 40;
  y = 0;
  w = 540 - (2 * x);
  h = 220;
  c = (2 * h) / 3;
  l = mutationDiagram.length;

  var div = d3.select("#container")
    .append("div");

  var svg = div.append("svg:svg")
    .attr("width", 540)
    .attr("height", 220);

  // label
  svg.append("svg:text")
    .attr("x", "10")
    .attr("y", "26")
    .attr("text-anchor", "start")
    .style("font-size", "16px")
    .style("font-family", "sans-serif")
    .text("<%= hugoGeneSymbol %>");

  // sequence
  svg.append("svg:rect")
    .attr("x", x)
    .attr("y", c - 6)
    .attr("width", scaleHoriz(mutationDiagram.length, w, l))
    .attr("height", 13)
    .style("fill", "rgb(120, 120, 120)")
    .style("stroke", "none");

  // domains
  for (i = 0, size = mutationDiagram.domains.length; i < size; i++)
  {
    domainX = x + scaleHoriz(mutationDiagram.domains[i].start, w, l);
    domainY = c - 10;
    domainW = scaleHoriz(mutationDiagram.domains[i].end - mutationDiagram.domains[i].start, w, l);
    domainH = 20;

    svg.append("svg:rect")
      .attr("x", domainX)
      .attr("y", domainY)
      .attr("width", domainW)
      .attr("height", domainH)
      .style("fill", domainColors[i % 4])
      .style("stroke-width", "1")
      .style("stroke", domainStrokeColors[i % 4]);

    svg.append("svg:text")
      .attr("x", domainX + (domainW/2))
      .attr("y", domainY + domainH + 14)
      .attr("text-anchor", "middle")
      .style("font-size", "11px")
      .style("font-family", "sans-serif")
      .text(mutationDiagram.domains[i].label);
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
      .style("stroke", "rgb(80, 80, 80")
      .style("stroke-width", 1);

    svg.append("svg:line")
      .attr("x1", scaleX + scaleW)
      .attr("y1", scaleY)
      .attr("x2", scaleX + scaleW)
      .attr("y2", scaleY - scaleH)
      .style("stroke", "rgb(80, 80, 80")
      .style("stroke-width", 1);

    svg.append("svg:line")
      .attr("x1", scaleX + scaleW)
      .attr("y1", scaleY - scaleH)
      .attr("x2", scaleX)
      .attr("y2", scaleY - scaleH)
      .style("stroke", "rgb(80, 80, 80")
      .style("stroke-width", 1);

    for (i = 0; i < maxCount; i++)
    {
      // scale major ticks
      if ((i % (maxCount / 2)) == 0)
      {
        svg.append("svg:line")
          .attr("x1", scaleX + scaleW - 4)
          .attr("y1", scaleY - (i * per))
          .attr("x2", scaleX + scaleW)
          .attr("y2", scaleY - (i * per))
          .style("stroke", "rgb(80, 80, 80")
          .style("stroke-width", 1);
      }
      // scale minor ticks
      if ((i % (maxCount / 4)) == 0)
      {
        svg.append("svg:line")
          .attr("x1", scaleX + scaleW - 2)
          .attr("y1", scaleY - (i * per))
          .attr("x2", scaleX + scaleW)
          .attr("y2", scaleY - (i * per))
          .style("stroke", "rgb(80, 80, 80")
          .style("stroke-width", 1);
      }
    }

    // scale labels
    svg.append("svg:text")
      .attr("x", scaleX - 4)
      .attr("y", scaleY + 4)
      .attr("text-anchor", "middle")
      .style("fill", "rgb(80, 80, 80)")
      .style("font-size", "11px")
      .style("font-family", "sans-serif")
      .text("0");

    svg.append("svg:text")
      .attr("x", scaleX - 4)
      .attr("y", scaleY + 4 - (maxCount * per))
      .attr("text-anchor", "middle")
      .style("fill", "rgb(80, 80, 80)")
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
        .style("stroke", mutationColors[i % 4])
        .style("stroke-width", 1);
    }
    else
    {
      for (j = 0, count = mutationDiagram.mutations[i].count; j < count; j++)
      {
        svg.append("svg:circle")
          .attr("cx", x2)
          .attr("cy", y1 - (j * per) - per/2)
          .attr("r", per/2)
          .style("fill", mutationColors[i % 4]);
      }
    }

    if (mutationDiagram.mutations[i].label)
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
}

function scaleHoriz(x, w, l)
{
  return x * (w/l);
}
    </script>
  </div>
</body>
</html>
