// usage: log('inside coolFunc',this,arguments);
// http://paulirish.com/2009/log-a-lightweight-wrapper-for-consolelog/
window.log = function(){
  log.history = log.history || [];   // store logs to an array for reference
  log.history.push(arguments);
  if(this.console){
    console.log( Array.prototype.slice.call(arguments) );
  }
};

function drawMutationDiagram(sequences) {
  MAX_OFFSET = 4;
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
  id = mutationDiagram.metadata.hugoGeneSymbol;
  label = mutationDiagram.metadata.identifier;
  title = mutationDiagram.metadata.identifier + ", " + mutationDiagram.metadata.description + " (" + l + "aa)";

  var paper = Raphael("mutation_diagram_" + id, 700, 220);

  // label
  paper.text(10, 26, label).attr({"text-anchor": "start", "font-size": "16px", "font-family": "sans-serif"});

  // sequence
  paper.rect(x, c - 6, scaleHoriz(Math.max(l, 100), w, l), 13)
    .attr({"fill": sequenceColor, "stroke": "none", "title": title});

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

  // regions
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

	//  Region Rectangle
    regionRect = paper.rect(regionX, regionY, regionW, regionH)
      .attr({"fill": regionFillColor, "stroke-width": 1, "stroke": regionStrokeColor});
 	addMouseOver(regionRect.node, regionTitle, id);

	//  Region Label (only if it fits)
    if ((regionLabel != null) && ((regionLabel.length * 10) < regionW)) {
		currentText = paper.text(regionX + (regionW / 2), regionY + regionH - 10, regionLabel)
		.attr({"text-anchor": "center", "font-size": "12px", "font-family": "sans-serif", "fill": "white"});
		addMouseOver(currentText.node, regionTitle, id);
    } else {
		truncatedLabel = regionLabel.substring(0,3) + "..";
		if (truncatedLabel.length * 6 < regionW) {
			currentText = paper.text(regionX + (regionW / 2), regionY + regionH - 10, truncatedLabel)
			.attr({"text-anchor": "center", "font-size": "12px", "font-family": "sans-serif", "fill": "white"});
			addMouseOver(currentText.node, regionTitle, id);
		}
	}
  }

  // mutation scale
  maxCount = 0;
  for (i = 0, size = mutationDiagram.markups.length; i < size; i++)
  {
    if ((mutationDiagram.markups[i].type == "mutation") && (parseInt(mutationDiagram.markups[i].metadata.count) >= maxCount))
    {
      maxCount = mutationDiagram.markups[i].metadata.count;
    }
  }
  maxCount = maxCount + MAX_OFFSET;

  scaleX = x - 15;
  scaleY = c - 8;
  per = (h / 3) / maxCount;
  scaleH = maxCount * per;
  scaleW = scaleHoriz(8, w, l);

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

  // mutation histograms/pileups
  labelShown = false;
	for (i = 0, size = mutationDiagram.markups.length; i < size; i++) {
		if (mutationDiagram.markups[i].type == "mutation")	{
			x1 = x + scaleHoriz(mutationDiagram.markups[i].start, w, l);
			y1 = c - 12;
			x2 = x1;
			y2 = c - 12 - (per * mutationDiagram.markups[i].metadata.count);
			if (mutationDiagram.markups[i].metadata.count == 1) {
				countText = "(" + mutationDiagram.markups[i].metadata.count + " mutation)";
			} else {
				countText = "(" + mutationDiagram.markups[i].metadata.count + " mutations)";
			}
			mutationTitle = "Amino Acid Change:  " + mutationDiagram.markups[i].metadata.label + " " + countText;

			p = paper.path("M" + x1 + " " + y1 +
				"L" + x2 + " " + y2)
				.attr({"stroke": mutationDiagram.markups[i].lineColour, "stroke-width": 2});
				addMouseOver(p.node, mutationTitle, id);

			//  Draws the Label for the Max Count
			if (mutationDiagram.markups[i].metadata.label && (maxCount == mutationDiagram.markups[i].metadata.count + MAX_OFFSET) && !labelShown) {
				paper.text(x1, y2 - 12, mutationDiagram.markups[i].metadata.label)
				.attr({"fill": mutationDiagram.markups[i].lineColour, "font-size": "11px", "font-family": "sans-serif"});
				labelShown = true;
			}
		}
	}

  //  Add Lollipop Heads to make it easier to select specific mutations
  for (i = 0, size = mutationDiagram.markups.length; i < size; i++)
  {
    if (mutationDiagram.markups[i].type == "mutation")
    {
      x1 = x + scaleHoriz(mutationDiagram.markups[i].start, w, l);
      y1 = c - 12;
      x2 = x1;
      y2 = c - 12 - (per * mutationDiagram.markups[i].metadata.count);
	 	if (mutationDiagram.markups[i].metadata.count == 1) {
			countText = "(" + mutationDiagram.markups[i].metadata.count + " mutation)";
		} else {
			countText = "(" + mutationDiagram.markups[i].metadata.count + " mutations)";
		}
		mutationTitle = "Amino Acid Change:  " + mutationDiagram.markups[i].metadata.label + " " + countText;

		lollipop = paper.circle(x2, y2-1, 3, 3)
			.attr({"fill": regionFillColor, "stroke-width": 1, "stroke": "red", "fill": "red"});
			addMouseOver(lollipop.node, mutationTitle, id);
	}
	}
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

function addMouseOver(node, txt, id){
	node.style.cursor = "default"
	node.onmouseover = function () {
		$('#mutation_diagram_details_' + id).html(txt)
	};

	node.onmouseout = function () {
		$('#mutation_diagram_details_' + id).html("Roll-over in the diagram above to view details.");
	};
}