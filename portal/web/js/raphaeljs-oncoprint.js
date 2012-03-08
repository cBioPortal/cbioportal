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

// stores the length of the longest
// label (gene & % alteration) in the OncoPrint
var LONGEST_LABEL_LENGTH = 0;

/*
 * Create default metrics.
 *
 * useDefaults - indicates if we want to use defaults
 *
 */
function CreateProperties(useDefaults) {

	if (useDefaults) {
		return {
			// general defaults
			'alteration_width'                  : DEFAULTS.get('ALTERATION_WIDTH'),
			'alteration_height'                 : DEFAULTS.get('ALTERATION_HEIGHT'),
			'alteration_vertical_padding'       : DEFAULTS.get('ALTERATION_VERTICAL_PADDING'),
			'alteration_horizontal_padding'     : DEFAULTS.get('ALTERATION_HORIZONTAL_PADDING'),
			// mrna
			'mrna_wireframe_width_scale_factor' : DEFAULTS.get('MRNA_WIREFRAME_WIDTH_SCALE_FACTOR'),
			// mutation	
			'mutation_height_scale_factor'      : DEFAULTS.get('MUTATION_HEIGHT_SCALE_FACTOR'),
			// use object for override coordinates flag
			'use_immediate_coordinates'         : false,
			// general sample properties
			'altered_samples_only'              : DEFAULTS.get('ALTERED_SAMPLES_ONLY')
		};
	}
}

/*
 * Computes the length of the given label
 * and sets global LONGEST_LABEL_LENGTH.
 *
 * longestLabel - longest label string
 *
 */
function SetLongestLabelLength(longestLabel) {

	// create scratch paper for drawing
	var scratchCanvas = Raphael(0,0, 1, 1);
	// draw text and set attributes
	var text = scratchCanvas.text(0,0, longestLabel);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	// get the bounding box
	var boundingBox = text.getBBox();
	// set LONGEST_LABEL_LENGTH -
	// include the space between gene and % altered and first genetic alteration box
	LONGEST_LABEL_LENGTH = boundingBox.width + DEFAULTS.get('LABEL_SPACING') + DEFAULTS.get('LABEL_PADDING');
	// remove the text element, scratch canvas we used to compute width
	text.remove();
	scratchCanvas.remove();
}

/*
 * Create a canvas for drawing.
 *
 * parentID - DOM element where we append canvas
 * numGenes - number of genes in the OncoPrint
 * numSamples - number of samples in the OncoPrint
 * properties - object which contains metric information
 *
 */
function CreateCanvas(parentID, numGenes, numSamples, properties) {

	// get dimensions for canvas
	var dimension = getOncoPrintCanvasDimension(numGenes, numSamples, properties);
	// create canvas
	var canvas = Raphael(parentID, dimension.width, dimension.height);
	// outta here
	return canvas;
}

/*
 * Draws the OncoPrint header.
 *
 * parentID - DOM element where we append canvas
 * headerVariables - various header strings
 * properties - object which contains properties information
 *
 * Note: headerVariables is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintHeaderVariables()
 *
 */
function DrawOncoPrintHeader(parentID, headerVariables, properties) {

	// vars used below
	var x, y;
	var text;

	// create canvas 
 	var dimension = getOncoPrintHeaderCanvasSize(headerVariables);
	var canvas = Raphael(parentID, dimension.width, dimension.height);

	// render case list description
	x = 0;
	y = dimension.text_height / 2;
	text = canvas.text(x, y, headerVariables.get('CASE_SET_DESCRIPTION'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');

	// render altered stats
	y = y + dimension.text_height + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	text = canvas.text(x, y, headerVariables.get('ALTERED_STATS'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');

	// % altered column heading
	x = LONGEST_LABEL_LENGTH - DEFAULTS.get('LABEL_PADDING') * 2;
	y = y + dimension.text_height + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	text = canvas.text(x, y, headerVariables.get('PERCENT_ALTERED_COLUMN_HEADING'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');

	// samples column heading
	x = LONGEST_LABEL_LENGTH;
	if (properties.altered_samples_only) {
		text = canvas.text(x, y, headerVariables.get('ALTERED_SAMPLES_COLUMN_HEADING'));
	}
	else {
		text = canvas.text(x, y, headerVariables.get('ALL_SAMPLES_COLUMN_HEADING'));
	}
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');
}

/* 
 * Draw an OncoPrint for the entire set of genetic
 * alterations on the given canvas using the given style.
 *
 * canvas - canvas to draw on
 * geneticAlterations - the set of geneticAlterations to draw
 * properties - object which contains metric information
 *
 * Note: geneticAlterations is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintGeneticAlterationVariable()
 * 
 */
function DrawOncoPrint(canvas, geneticAlterations, properties) {

	// this is so row/col values are used in computation of x,y coords
	properties.use_immediate_coordinates = false;

	// iterate over all genetic alterations
	for (var lc = 0; lc < geneticAlterations.length; lc++) {
		var alteration = geneticAlterations[lc];
		// draw label first
		drawLabel(canvas, lc, alteration.hugoGeneSymbol, alteration.percentAltered, properties);
		// for this gene, interate over all samples
		for (var lc2 = 0; lc2 < alteration.alterations.length; lc2++) {
			var thisSampleAlteration = alteration.alterations[lc2];
			if (properties.altered_samples_only &&
				$.inArray(thisSampleAlteration.sample, alteration.alteredSamples) == -1) {
				continue;
			}
			// first draw MRNA "background"
			drawMRNA(canvas, lc, lc2, thisSampleAlteration.alteration, properties);
			// then draw CNA "within"
			drawCNA(canvas, lc, lc2, thisSampleAlteration.alteration, properties);
			// finally draw mutation square "on top"
			drawMutation(canvas, lc, lc2, thisSampleAlteration.alteration, properties);
		}
	}
}

/*
 * Draws a lengend for the given union of geneticAlterations.
 *
 * parentID - DOM element where we append canvas
 * geneticAlterations - the set of geneticAlterations to draw
 * legendFootnote - footnote to the legend
 *
 * Note: geneticAlterations is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintLegendGeneticAlterationVariable()
 *
 */
function DrawOncoPrintLegend(parentID, geneticAlterations, legendFootnote) {

	// create canvas 
 	var dimension = getOncoPrintLegendCanvasSize(geneticAlterations, legendFootnote);
	var canvas = Raphael(parentID, dimension.width, dimension.height);

	// this is so row/col values are used directly
	properties = CreateProperties(true);
	properties.use_immediate_coordinates = true;

	// interate over all genomic alterations in legend
	var x = LONGEST_LABEL_LENGTH;
	var y = properties.alteration_height / 2;
	var legendSpacing = DEFAULTS.get('LEGEND_SPACING');
	var legendPadding = DEFAULTS.get('LEGEND_PADDING');
	for (var lc = 0; lc < geneticAlterations.length; lc++) {
		var alteration = geneticAlterations[lc];
		// only one of the following will render
		drawMRNA(canvas, 0, x, alteration.alteration, properties);
		drawCNA(canvas, 0, x, alteration.alteration, properties);
		drawMutation(canvas, 0, x, alteration.alteration, properties);
		// render description
		x = x + properties.alteration_width + legendSpacing;
		var description = canvas.text(x, y, alteration.label);
		description.attr('font', DEFAULTS.get('LABEL_FONT'));
		description.attr('fill', DEFAULTS.get('LABEL_COLOR'));
		description.attr('text-anchor', 'start');
		x = x + description.getBBox().width + legendPadding;
	}

	// tack on legend footnote
	if (legendFootnote.length > 0) {
		x = LONGEST_LABEL_LENGTH;
		y = properties.alteration_height + DEFAULTS.get('LEGEND_FOOTNOTE_SPACING');
		var footnote = canvas.text(x, y, legendFootnote);
		footnote.attr('font', DEFAULTS.get('LABEL_FONT'));
		footnote.attr('fill', DEFAULTS.get('LABEL_COLOR'));
		footnote.attr('text-anchor', 'start');
	}
}

/*******************************************************************************
//
// The following functions are for internal use only.
//
*******************************************************************************/

/*
 * Computes dimensions of OncoPrint canvas with given properties.
 *
 * numGenes - number of genes in oncoprint
 * numSamples - number of samples in oncoprint
 * properties - object which contains metric information
 *
 */
function getOncoPrintCanvasDimension(numGenes, numSamples, properties) {

	// we want enough space for each alteration w/padding.  remove padding on the right/bottom
	var canvasHeight = (numGenes *
						(properties.alteration_height + properties.alteration_vertical_padding) -
						properties.alteration_vertical_padding);
	var canvasWidth = (LONGEST_LABEL_LENGTH +
					   numSamples *
					   (properties.alteration_width + properties.alteration_horizontal_padding) -
					   properties.alteration_horizontal_padding);

	// outta here
	return { 'width' : canvasWidth, 'height' : canvasHeight };
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
 * Computes dimensions of the OncoPrint Legend canvas.
 *
 * geneticAlterations - the genetic alterations in the legend
 * legendFootnote - the footnote in the legend
 *
 */
function getOncoPrintLegendCanvasSize(geneticAlterations, legendFootnote) {

	// setup some vars used below
	var text;
	var boundingBox;
	var canvasWidth = LONGEST_LABEL_LENGTH;
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
 * Draws a on the given canvas at row row.
 *
 * canvas - canvas to draw on
 * row - the vertical position to draw the text
 * geneSymbol - the gene symbol to render
 * percentAltered - the percent altered string
 * properties - object which contains property information
 *
 */
function drawLabel(canvas, row, geneSymbol, percentAltered, properties) {

	// compute starting coordinates
	var x = getXCoordinate(0, properties) - DEFAULTS.get('LABEL_PADDING');
	var y = getYCoordinate(row, properties) + properties.alteration_height / 2;
	// render % altered
	var text = canvas.text(x, y, percentAltered);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');
	// render gene symbol
	x = x - text.getBBox().width - DEFAULTS.get('LABEL_SPACING');
	text = canvas.text(x, y, geneSymbol);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');
}

/*
 * Draws an mRNA genomic alteration at given row & col.
 *
 * canvas - canvas to draw on
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * alterationSettings - the genomic alteration
 * properties - object which contains property information
 *
 */
function drawMRNA(canvas, row, column, alterationSettings, properties) {

	// compute starting coordinates
	var y = getYCoordinate(row, properties);
	var	x = getXCoordinate(column, properties);
	// create canvas rect
	var rect = canvas.rect(x, y, properties.alteration_width, properties.alteration_height);
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
}

/*
 * Draws a CNA genomic alteration at given row & col.
 *
 * canvas - canvas to draw on
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * alterationSettings - the genomic alteration
 * properties - object which contains property information
 *
 */
function drawCNA(canvas, row, column, alterationSettings, properties) {

	// compute starting coordinates
	var y = getYCoordinate(row, properties);
	var x = getXCoordinate(column, properties);
	// create canvas rect
	var mrnaWireframeWidth = getMRNAWireframeWidth(properties);
	var rect = canvas.rect(x + mrnaWireframeWidth,
						   y + mrnaWireframeWidth,
						   properties.alteration_width - mrnaWireframeWidth * 2,
						   properties.alteration_height - mrnaWireframeWidth * 2);
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
 * canvas - canvas to draw on
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * alterationSettings - the genomic alteration
 * properties - object which contains property information
 *
 */
function drawMutation(canvas, row, column, alterationSettings, properties) {

	// only render if we have a mutation
	if (alterationSettings & MUTATED) {
		// compute starting coordinates
		var y = getYCoordinate(row, properties);
		var x = getXCoordinate(column, properties);
		// create canvas rect -
		// center mutation square vertical & start drawing halfway into MRNA WIREFRAME
		var mrnaWireframeWidth = getMRNAWireframeWidth(properties);
		var mutationRectDimensions = getMutationRectDimensions(properties);
		var rect = canvas.rect(x + mrnaWireframeWidth / 2,
							   y + properties.alteration_height / 2 - mutationRectDimensions.height / 2,
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
 * properties - object which contains property information
 *
 */
function getXCoordinate(column, properties) {

	if (properties.use_immediate_coordinates) {
		return column;
	}
	else {
		return (column * (properties.alteration_width + properties.alteration_horizontal_padding)
				+ LONGEST_LABEL_LENGTH);
	}
}

/*
 * For the given row (gene) return the y coordinate
 *
 * row - gene we are processing
 * properties - object which contains property information
 *
 */
function getYCoordinate(row, properties) {

	if (properties.use_immediate_coordinates) {
		return row;
	}
	else {
		return row * (properties.alteration_height + properties.alteration_vertical_padding);
	}
}

/**
 * Computes the width of the mRNA wireframe.
 *
 * properties - object which contains property information
 *
 */
function getMRNAWireframeWidth(properties) {

	return properties.alteration_width * properties.mrna_wireframe_width_scale_factor;
}

/**
 * Computes the dimension of the mutation rect
 *
 * properties - object which contains property information
 *
 */
function getMutationRectDimensions(properties) {

	var width = properties.alteration_width - getMRNAWireframeWidth(properties);
	var height = properties.alteration_height * properties.mutation_height_scale_factor;

	return { 'width' : width, 'height' : height };
}
