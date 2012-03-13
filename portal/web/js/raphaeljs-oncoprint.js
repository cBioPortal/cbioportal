/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

/*
 * Javscript library based on RaphaelJS Library which renders OncoPrints. 
 */

// These bits are passed as "AlterationSettings" in DrawAlteration -
// they corresponded to names found in org.mskcc.portal.model.GeneticEventImpl
// CNA bits
var AMPLIFIED           = (1<<0);
var GAINED              = (1<<1);
var DIPLOID             = (1<<2);
var HEMIZYGOUSLYDELETED = (1<<3);
var HOMODELETED         = (1<<4);
var NONE                = (1<<5);
// MRNA bits (normal is in GeneticEventImpl, but never used)
var UPREGULATED         = (1<<6);
var DOWNREGULATED       = (1<<7);
var NOTSHOWN            = (1<<8);
// MUTATION bits
var MUTATED             = (1<<9);
var NORMAL              = (1<<10);

// store defaults in a module pattern
var DEFAULTS = (function() {
		var private = {
			// size of genetic alteration
			'ALTERATION_WIDTH'                  : 6,
			'ALTERATION_HEIGHT'                 : 18,
			// padding between genetic alteration boxes
			'ALTERATION_VERTICAL_PADDING'       : 1,
			'ALTERATION_HORIZONTAL_PADDING'     : 1,
			// cna styles
			'AMPLIFIED_COLOR'                   : "#FF0000",
			'GAINED_COLOR'                      : "#FFB6C1",
			'DIPLOID_COLOR'                     : "#D3D3D3",
			'HEMIZYGOUSLYDELETED_COLOR'         : "#8FD8D8",
			'HOMODELETED_COLOR'                 : "#0000FF",
			'NONE_COLOR'                        : "#D3D3D3",
			// mrna styles
			'MRNA_WIREFRAME_WIDTH_SCALE_FACTOR' : 1/6,
			'UPREGULATED_COLOR'                 : "#FF9999",
			'DOWNREGULATED_COLOR'               : "#6699CC",
			'NOTSHOWN_COLOR'                    : "#FFFFFF",
			// mutation styles
			'MUTATION_COLOR'                    : "#008000",
			'MUTATION_HEIGHT_SCALE_FACTOR'      : 1/3,
			// labels
			'LABEL_COLOR'                       : "#666666",
			'LABEL_FONT'                        : "normal 12px verdana",
			'LABEL_SPACING'                     : 15, // space between gene and percent altered 
			'LABEL_PADDING'                     : 3,  // padding (in pixels) between label and first genetic alteration
			// legend
			'LEGEND_SPACING'                    : 5,  // space between alteration and description
			'LEGEND_PADDING'                    : 15, // space between alteration / descriptions pairs
			'LEGEND_FOOTNOTE_SPACING'           : 10, // space between alteration / descriptions pairs and footnote
			// header
			'HEADER_VERTICAL_PADDING'           : 10, // space between header sentences
            // general sample properties
			'ALTERED_SAMPLES_ONLY'              : false
		};
		return {
		    get: function(name) { return private[name]; }
		};
})();

/*
 * Initializes the OncoPrintSystem
 *
 * headerElement - DOM element where we header canvas
 * bodyElement - DOM element where we append oncoprint canvas
 * legendElement - DOM element where we append legend canvas
 *
 */
function OncoPrintInit(headerElement, bodyElement, legendElement) {

	return {
		// setup canvases - these will be resized later
		'header_canvas'                     : Raphael(headerElement, 1, 1),
		'body_canvas'                       : Raphael(bodyElement, 1, 1),
		'legend_canvas'                     : Raphael(legendElement, 1, 1),
		// longest label length
		'longest_label_length'              : 0,
		// general styles
		'alteration_width'                  : DEFAULTS.get('ALTERATION_WIDTH'),
		'alteration_height'                 : DEFAULTS.get('ALTERATION_HEIGHT'),
		'alteration_vertical_padding'       : DEFAULTS.get('ALTERATION_VERTICAL_PADDING'),
		'alteration_horizontal_padding'     : DEFAULTS.get('ALTERATION_HORIZONTAL_PADDING'),
		// mrna styles
		'mrna_wireframe_width_scale_factor' : DEFAULTS.get('MRNA_WIREFRAME_WIDTH_SCALE_FACTOR'),
		// mutation	styles
		'mutation_height_scale_factor'      : DEFAULTS.get('MUTATION_HEIGHT_SCALE_FACTOR'),
		// use object for override coordinates flag
		'use_immediate_coordinates'         : false,
		// general sample properties
		'altered_samples_only'              : DEFAULTS.get('ALTERED_SAMPLES_ONLY')
	};
}

/*
 * Draws the OncoPrint header.
 *
 * oncoprint - opaque reference to oncoprint system
 * longestLabel - the longest label in the oncoprint (saves us some leg-work)
 * headerVariables - various header strings
 *
 * Note: headerVariables is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintHeaderVariables()
 *
 */
function DrawOncoPrintHeader(oncoprint, longestLabel, headerVariables) {

	// vars used below
	var x, y;
	var text;

	// set longest label length
	oncoprint.longest_label_length = getLabelLength(longestLabel);

	// resize canvas 
 	var dimension = getOncoPrintHeaderCanvasSize(headerVariables);
	oncoprint.header_canvas.setSize(dimension.width, dimension.height);
	oncoprint.header_canvas.clear();

	// render case list description
	x = 0;
	y = dimension.text_height / 2;
	text = oncoprint.header_canvas.text(x, y, headerVariables.get('CASE_SET_DESCRIPTION'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');

	// render altered stats
	y = y + dimension.text_height + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	text = oncoprint.header_canvas.text(x, y, headerVariables.get('ALTERED_STATS'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');

	// % altered column heading
	x = oncoprint.longest_label_length - DEFAULTS.get('LABEL_PADDING') * 2;
	y = y + dimension.text_height + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	text = oncoprint.header_canvas.text(x, y, headerVariables.get('PERCENT_ALTERED_COLUMN_HEADING'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');

	// samples column heading
	x = oncoprint.longest_label_length;
	if (oncoprint.altered_samples_only) {
		text = oncoprint.header_canvas.text(x, y, headerVariables.get('ALTERED_SAMPLES_COLUMN_HEADING'));
	}
	else {
		text = oncoprint.header_canvas.text(x, y, headerVariables.get('ALL_SAMPLES_COLUMN_HEADING'));
	}
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');
}

/* 
 * Draw an OncoPrint for the entire set of genetic
 * alterations on the given canvas using the given style.
 *
 * oncoprint - opaque reference to oncoprint system
 * longestLabel - the longest label in the oncoprint (saves us some leg-work)
 * geneticAlterations - the set of geneticAlterations to draw
 *
 * Note: geneticAlterations is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintGeneticAlterationVariable()
 * 
 */
function DrawOncoPrintBody(oncoprint, longestLabel, geneticAlterations) {

	// this is so row/col values are used in computation of x,y coords
	oncoprint.use_immediate_coordinates = false;

	// set longest label length
	oncoprint.longest_label_length = getLabelLength(longestLabel);

	// resize canvas
	var dimension = getOncoPrintBodyCanvasSize(oncoprint,
											   geneticAlterations.length,
											   geneticAlterations[0].alterations.length);
	oncoprint.body_canvas.setSize(dimension.width, dimension.height);
	oncoprint.body_canvas.clear();

	// used to filter out unaltered samples in loop belowe
	var unalteredSample = (NONE | NOTSHOWN | NORMAL);

	// iterate over all genetic alterations
	for (var lc = 0; lc < geneticAlterations.length; lc++) {
		var alteration = geneticAlterations[lc];
		// draw label first
		drawGeneLabel(oncoprint, lc, alteration.hugoGeneSymbol, alteration.percentAltered);
		// for this gene, interate over all samples
		var samplePos = -1;
		for (var lc2 = 0; lc2 < alteration.alterations.length; lc2++) {
			var thisSampleAlteration = alteration.alterations[lc2];
			if (oncoprint.altered_samples_only && (thisSampleAlteration.alteration == unalteredSample)) {
				continue;
			}
			++samplePos;
			// first draw MRNA "background"
			drawMRNA(oncoprint, oncoprint.body_canvas, lc, samplePos, thisSampleAlteration.alteration,
					 true, thisSampleAlteration.sample);
			// then draw CNA "within"
			drawCNA(oncoprint, oncoprint.body_canvas, lc, samplePos, thisSampleAlteration.alteration);
			// finally draw mutation square "on top"
			drawMutation(oncoprint, oncoprint.body_canvas, lc, samplePos, thisSampleAlteration.alteration);
		}
	}
}

/*
 * Draws a legend for the given union of geneticAlterations.
 *
 * oncoprint - opaque reference to oncoprint system
 * longestLabel - the longest label in the oncoprint (saves us some leg-work)
 * geneticAlterations - the set of geneticAlterations to draw
 * legendFootnote - footnote to the legend
 *
 * Note: geneticAlterations is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintLegendGeneticAlterationVariable()
 *
 */
function DrawOncoPrintLegend(oncoprint, longestLabel, geneticAlterations, legendFootnote) {

	// set longest label length
	oncoprint.longest_label_length = getLabelLength(longestLabel);

	// resize canvas 
 	var dimension = getOncoPrintLegendCanvasSize(oncoprint, geneticAlterations, legendFootnote);
	oncoprint.legend_canvas.setSize(dimension.width, dimension.height);
	oncoprint.legend_canvas.clear();

	// this is so row/col values are used directly
	oncoprint.use_immediate_coordinates = true;

	// interate over all genomic alterations in legend
	var x = oncoprint.longest_label_length;
	var y = oncoprint.alteration_height / 2;
	var legendSpacing = DEFAULTS.get('LEGEND_SPACING');
	var legendPadding = DEFAULTS.get('LEGEND_PADDING');
	for (var lc = 0; lc < geneticAlterations.length; lc++) {
		var alteration = geneticAlterations[lc];
		// only one of the following will render
		drawMRNA(oncoprint, oncoprint.legend_canvas, 0, x, alteration.alteration, false, "");
		drawCNA(oncoprint, oncoprint.legend_canvas, 0, x, alteration.alteration);
		drawMutation(oncoprint, oncoprint.legend_canvas, 0, x, alteration.alteration);
		// render description
		x = x + oncoprint.alteration_width + legendSpacing;
		var description = oncoprint.legend_canvas.text(x, y, alteration.label);
		description.attr('font', DEFAULTS.get('LABEL_FONT'));
		description.attr('fill', DEFAULTS.get('LABEL_COLOR'));
		description.attr('text-anchor', 'start');
		x = x + description.getBBox().width + legendPadding;
	}

	// tack on legend footnote
	if (legendFootnote.length > 0) {
		x = oncoprint.longest_label_length;
		y = oncoprint.alteration_height + DEFAULTS.get('LEGEND_FOOTNOTE_SPACING');
		var footnote = oncoprint.legend_canvas.text(x, y, legendFootnote);
		footnote.attr('font', DEFAULTS.get('LABEL_FONT'));
		footnote.attr('fill', DEFAULTS.get('LABEL_COLOR'));
		footnote.attr('text-anchor', 'start');
	}
}

/*
 * For the given oncoprint reference, returns the SVG Dom as string
 * for the body canvas.
 *
 * oncoprint - opaque reference to oncoprint system
 *
 */
function GetOncoPrintBodyXML(oncoprint) {

	// outta here
	return (new XMLSerializer()).serializeToString(oncoprint.body_canvas.canvas);
}

/*
 * For the given oncoprint reference, returns the longest label length.
 *
 * oncoprint - opaque reference to oncoprint system
 *
 */
function GetLongestLabelLength(oncoprint) {

	// outta here
	return oncoprint.longest_label_length;
}

/*
 * Toggles show altered samples only property.
 *
 * oncoprint - opaque reference to oncoprint system
 *
 */
function ShowAlteredSamples(oncoprint, showAlteredSamples) {

	oncoprint.altered_samples_only = showAlteredSamples;
}

/*******************************************************************************
//
// The following functions are for internal use only.
//
*******************************************************************************/

/*
 * Computes the length (pixels) of the given label.
 *
 * label - label string
 *
 */
function getLabelLength(label) {

	// create scratch paper for drawing
	var scratchCanvas = Raphael(0,0, 1, 1);
	// draw text and set attributes
	var text = scratchCanvas.text(0,0, label);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	// include the space between gene and % altered and first genetic alteration box
	var labelLength = text.getBBox().width + DEFAULTS.get('LABEL_SPACING') + DEFAULTS.get('LABEL_PADDING');
	// cleanup
	text.remove();
	scratchCanvas.remove();
	
	// outta here
	return labelLength;
}

/*
 * Computes dimensions of the OncoPrint Header canvas.
 * Also returns height of a text string.
 *
 * headerVariables - various header strings
 *
 */
function getOncoPrintHeaderCanvasSize(headerVariables) {

	// vars used below
	var text;
	var boundingBox;
	var canvasWidth = 0;
	var canvasHeight = 0;
	var textHeight = 0;
	var scratchCanvas = Raphael(0, 0, 1, 1);

	// case set description
	text = scratchCanvas.text(0,0, headerVariables.get('CASE_SET_DESCRIPTION'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	boundingBox = text.getBBox();
	// we assume case set decription is longest string
	canvasWidth = boundingBox.width;
	canvasHeight = canvasHeight + boundingBox.height;
	textHeight = boundingBox.height;

	// altered stats
	text = scratchCanvas.text(0,0, headerVariables.get('ALTERED_STATS'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	boundingBox = text.getBBox();
	canvasHeight = canvasHeight + boundingBox.height;

	// column headings - use % altered - spans 2 lines
	text = scratchCanvas.text(0,0, headerVariables.get('PERCENT_ALTERED_COLUMN_HEADING'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	boundingBox = text.getBBox();
	canvasHeight = canvasHeight + boundingBox.height;

	// add padding between lines: space betw case set desc & alter stats, alter stats & col headers
	canvasHeight = canvasHeight + DEFAULTS.get('HEADER_VERTICAL_PADDING') * 2;
	
	// clean up
	text.remove();
	scratchCanvas.remove();

	// outta here
	return { 'width' : canvasWidth, 'height' : canvasHeight, 'text_height' : textHeight };
}

/*
 * Computes dimensions of OncoPrint canvas with given oncoprint.
 *
 * oncoprint - opaque reference to oncoprint system
 * numGenes - number of genes in oncoprint
 * numSamples - number of samples in oncoprint
 *
 */
function getOncoPrintBodyCanvasSize(oncoprint, numGenes, numSamples) {

	// we want enough space for each alteration w/padding.  remove padding on the right/bottom
	var canvasHeight = (numGenes *
						(oncoprint.alteration_height + oncoprint.alteration_vertical_padding) -
						oncoprint.alteration_vertical_padding);
	var canvasWidth = (oncoprint.longest_label_length +
					   numSamples *
					   (oncoprint.alteration_width + oncoprint.alteration_horizontal_padding) -
					   oncoprint.alteration_horizontal_padding);

	// outta here
	return { 'width' : canvasWidth, 'height' : canvasHeight };
}

/*
 * Computes dimensions of the OncoPrint Legend canvas.
 *
 * oncoprint - opaque reference to oncoprint system
 * geneticAlterations - the genetic alterations in the legend
 * legendFootnote - the footnote in the legend
 *
 */
function getOncoPrintLegendCanvasSize(oncoprint, geneticAlterations, legendFootnote) {

	// setup some vars used below
	var text;
	var boundingBox;
	var canvasWidth = oncoprint.longest_label_length;
	var alterationWidth = DEFAULTS.get('ALTERATION_WIDTH');
	var canvasHeight = DEFAULTS.get('ALTERATION_HEIGHT');
	var legendSpacing = DEFAULTS.get('LEGEND_SPACING');
	var legendPadding = DEFAULTS.get('LEGEND_PADDING');
	var scratchCanvas = Raphael(0, 0, 1, 1);

	// interate over genetic alterations
	for (var lc = 0; lc < geneticAlterations.length; lc++) {
		// create a text object & set font attr
		text = scratchCanvas.text(0,0, geneticAlterations[lc].label);
		text.attr('font', DEFAULTS.get('LABEL_FONT'));
		// get the bounding box
		boundingBox = text.getBBox();
		// update canvasWidth
		canvasWidth = canvasWidth + alterationWidth + legendSpacing + boundingBox.width + legendPadding;
		// udate canvasHeight
		if (boundingBox.height > canvasHeight) {
			canvasHeight = boundingBox.height;
		}
		// get ready for next iteration
		text.remove();
	}

	if (legendFootnote.length > 0) {
		// create a text object & set font attr
		text = scratchCanvas.text(0,0, legendFootnote);
		text.attr('font', DEFAULTS.get('LABEL_FONT'));
		// get the bounding box
		boundingBox = text.getBBox();
		canvasHeight = canvasHeight + DEFAULTS.get('LEGEND_FOOTNOTE_SPACING') + boundingBox.height;
		text.remove();
	}

	// clean up
	scratchCanvas.remove();

	// outta here
	return { 'width' : canvasWidth, 'height' : canvasHeight };
}

/*
 * Draws a gene label on the body_canvas at row row.
 *
 * oncoprint - opaque reference to oncoprint system
 * row - the vertical position to draw the text
 * geneSymbol - the gene symbol to render
 * percentAltered - the percent altered string
 *
 */
function drawGeneLabel(oncoprint, row, geneSymbol, percentAltered) {

	// compute starting coordinates
	var x = getXCoordinate(oncoprint, 0) - DEFAULTS.get('LABEL_PADDING');
	var y = getYCoordinate(oncoprint, row) + oncoprint.alteration_height / 2;
	// render % altered
	var text = oncoprint.body_canvas.text(x, y, percentAltered);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');
	// render gene symbol
	x = x - text.getBBox().width - DEFAULTS.get('LABEL_SPACING');
	text = oncoprint.body_canvas.text(x, y, geneSymbol);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');
}

/*
 * Draws an mRNA genomic alteration at given row & col.
 *
 * oncoprint - opaque reference to oncoprint system
 * canvas - canvas to draw on
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * alterationSettings - the genomic alteration
 * createToolTip - if true, creates a tooltip with provide text
 * toolTipText - text for tooltip
 *
 */
function drawMRNA(oncoprint, canvas, row, column, alterationSettings, createToolTip, toolTipText) {

	// compute starting coordinates
	var y = getYCoordinate(oncoprint, row);
	var	x = getXCoordinate(oncoprint, column);
	// create canvas rect
	var rect = canvas.rect(x, y, oncoprint.alteration_width, oncoprint.alteration_height);
	// without this we get thin black border around rect
	rect.attr('stroke', 'none'); 
	// choose fill color based on alteration type
	if (alterationSettings & UPREGULATED) {
		rect.attr('fill', DEFAULTS.get('UPREGULATED_COLOR'));
	}
	else if (alterationSettings & DOWNREGULATED) {
		rect.attr('fill', DEFAULTS.get('DOWNREGULATED_COLOR'));
	}
	else if (alterationSettings & NOTSHOWN) {
		rect.attr('fill', DEFAULTS.get('NOTSHOWN_COLOR'));
	}
	if (createToolTip) {
	}
}

/*
 * Draws a CNA genomic alteration at given row & col.
 *
 * oncoprint - opaque reference to oncoprint system
 * canvas - canvas to draw on
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * alterationSettings - the genomic alteration
 *
 */
function drawCNA(oncoprint, canvas, row, column, alterationSettings) {

	// compute starting coordinates
	var y = getYCoordinate(oncoprint, row);
	var x = getXCoordinate(oncoprint, column);
	// create canvas rect
	var mrnaWireframeWidth = getMRNAWireframeWidth(oncoprint);
	var rect = canvas.rect(x + mrnaWireframeWidth,
						   y + mrnaWireframeWidth,
						   oncoprint.alteration_width - mrnaWireframeWidth * 2,
						   oncoprint.alteration_height - mrnaWireframeWidth * 2);
	// without this we get thin black border around rect
	rect.attr('stroke', 'none'); 
	// choose fill color based on alteration type
	if (alterationSettings & AMPLIFIED) {
		rect.attr('fill', DEFAULTS.get('AMPLIFIED_COLOR'));
	}
	else if (alterationSettings & GAINED) {
		rect.attr('fill', DEFAULTS.get('GAINED_COLOR'));
	}
	else if (alterationSettings & DIPLOID) {
		rect.attr('fill', DEFAULTS.get('DIPLOID_COLOR'));
	}
	else if (alterationSettings & HEMIZYGOUSLYDELETED) {
		rect.attr('fill', DEFAULTS.get('HEMIZYGOUSLYDELETED_COLOR'));
	}
	else if (alterationSettings & HOMODELETED) {
		rect.attr('fill', DEFAULTS.get('HOMODELETED_COLOR'));
	}
	else if (alterationSettings & NONE) {
		rect.attr('fill', DEFAULTS.get('NONE_COLOR'));
	}
}

/*
 * Draws a mutation genomic alteration at given row & col.
 *
 * oncoprint - opaque reference to oncoprint system
 * canvas - canvas to draw on
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * alterationSettings - the genomic alteration
 *
 */
function drawMutation(oncoprint, canvas, row, column, alterationSettings) {

	// only render if we have a mutation
	if (alterationSettings & MUTATED) {
		// compute starting coordinates
		var y = getYCoordinate(oncoprint, row);
		var x = getXCoordinate(oncoprint, column);
		// create canvas rect -
		// center mutation square vertical & start drawing halfway into MRNA WIREFRAME
		var mrnaWireframeWidth = getMRNAWireframeWidth(oncoprint);
		var mutationRectDimensions = getMutationRectDimensions(oncoprint);
		var rect = canvas.rect(x + mrnaWireframeWidth / 2,
							   y + oncoprint.alteration_height / 2 - mutationRectDimensions.height / 2,
							   mutationRectDimensions.width, mutationRectDimensions.height);
		// without this we get thin black border around rect
		rect.attr('stroke', 'none'); 
		// set color
		rect.attr('fill', DEFAULTS.get('MUTATION_COLOR'));
	}
}

/*
 * For the given column (sample) return the x coordinate
 *
 * column - sample we are processing
 *
 */
function getXCoordinate(oncoprint, column) {

	if (oncoprint.use_immediate_coordinates) {
		return column;
	}
	else {
		return (column * (oncoprint.alteration_width + oncoprint.alteration_horizontal_padding)
				+ oncoprint.longest_label_length);
	}
}

/*
 * For the given row (gene) return the y coordinate
 *
 * oncoPrint - opaque reference to oncoprint system
 * row - gene we are processing
 *
 */
function getYCoordinate(oncoprint, row) {

	if (oncoprint.use_immediate_coordinates) {
		return row;
	}
	else {
		return row * (oncoprint.alteration_height + oncoprint.alteration_vertical_padding);
	}
}

/**
 * Computes the width of the mRNA wireframe.
 *
 * oncoprint - opaque reference to oncoprint system
 *
 */
function getMRNAWireframeWidth(oncoprint) {

	return oncoprint.alteration_width * oncoprint.mrna_wireframe_width_scale_factor;
}

/**
 * Computes the dimension of the mutation rect
 *
 * oncoprint - opaque reference to oncoprint system
 *
 */
function getMutationRectDimensions(oncoprint) {

	var width = oncoprint.alteration_width - getMRNAWireframeWidth(oncoprint);
	var height = oncoprint.alteration_height * oncoprint.mutation_height_scale_factor;

	return { 'width' : width, 'height' : height };
}

/**
 * Converts an SVG DOM to a string.
 *
 * oncoprint - opaque reference to oncoprint system
 * canvas - canvas to draw on
 * removeGeneLabels - export oncoprint without gene/alteration label
 *
 */
function canvasToString(oncoprint, canvas, removeGeneLabels) {

	var toReturn = '';
	var xml = (new XMLSerializer()).serializeToString(canvas.canvas);

	if (removeGeneLabels) {
	}
	else {
		return xml;
	}
}

function SvgToString(elem, out, indent) {

   if (elem)
   {
      var attrs = elem.attributes;
      var attr;
      var i;
      var childs = elem.childNodes;

      for (i=0; i<indent; i++) out += "  ";
      out += "<" + elem.nodeName;
      for (i=attrs.length-1; i>=0; i--)
      {
         attr = attrs.item(i);
         out += " " + attr.nodeName + "=\"" + attr.nodeValue+ "\"";
      }

      if (elem.hasChildNodes())
      {
         out += ">\n";
         indent++;
         for (i=0; i<childs.length; i++)
         {
            if (childs.item(i).nodeType == 1) // element node ..
				SvgToString(childs.item(i), out, indent);
            else if (childs.item(i).nodeType == 3) // text node ..
            {
               for (j=0; j<indent; j++) out += "  ";
               out += childs.item(i).nodeValue + "\n";
            }
         }
         indent--;
         for (i=0; i<indent; i++) out += "  ";
         out += "</" + elem.nodeName + ">\n";
      }
      else
      {
         out += " />\n";
      }

   }
   return out;
}
