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
 *
 * Benjamin Gross
 */

// These bits are passed as "AlterationSettings" in DrawAlteration -
// they corresponded to names found in org.mskcc.portal.model.GeneticEventImpl
// CNA bits
var CNA_AMPLIFIED           = (1<<0);
var CNA_GAINED              = (1<<1);
var CNA_DIPLOID             = (1<<2);
var CNA_HEMIZYGOUSLYDELETED = (1<<3);
var CNA_HOMODELETED         = (1<<4);
var CNA_NONE                = (1<<5);
// MRNA bits (normal is in GeneticEventImpl, but never used)
var MRNA_UPREGULATED        = (1<<6);
var MRNA_DOWNREGULATED      = (1<<7);
var MRNA_NOTSHOWN           = (1<<8);
// MUTATION bits
var MUTATED                 = (1<<9);
var NORMAL                  = (1<<10);

// store defaults in a module pattern
var DEFAULTS = (function() {
		var private = {
			// size of genetic alteration
			'ALTERATION_WIDTH'                  : 6,
			'ALTERATION_HEIGHT'                 : 18, // if this changes, MakeOncoPrint.CELL_HEIGHT needs to change
			// padding between genetic alteration boxes
			'ALTERATION_VERTICAL_PADDING'       : 1,
			'ALTERATION_HORIZONTAL_PADDING'     : 1,
			// cna styles
			'CNA_AMPLIFIED_COLOR'               : "#FF0000",
			'CNA_GAINED_COLOR'                  : "#FFB6C1",
			'CNA_DIPLOID_COLOR'                 : "#D3D3D3",
			'CNA_HEMIZYGOUSLYDELETED_COLOR'     : "#8FD8D8",
			'CNA_HOMODELETED_COLOR'             : "#0000FF",
			'CNA_NONE_COLOR'                    : "#D3D3D3",
			// mrna styles
			'MRNA_WIREFRAME_WIDTH_SCALE_FACTOR' : 1/6,
			'MRNA_WIREFRAME_WIDTH_SCALE_FACTOR2': 1/4,
			'MRNA_UPREGULATED_COLOR'            : "#FF9999",
			'MRNA_DOWNREGULATED_COLOR'          : "#6699CC",
			'MRNA_NOTSHOWN_COLOR'               : "#FFFFFF",
			// mutation styles
			'MUTATION_COLOR'                    : "#008000",
			'MUTATION_HEIGHT_SCALE_FACTOR'      : 1/3,
			// labels
			'LABEL_COLOR'                       : "#666666",
			'LABEL_FONT'                        : "normal 12px verdana",
			'LABEL_SPACING'                     : 15, // space between gene and percent altered 
			'LABEL_PADDING'                     : 3,  // padding (in pixels) between label and first genetic alteration
			'CASE_SET_DESCRIPTION_LABEL'        : "Case Set:i",
			// legend
			'LEGEND_SPACING'                    : 5,  // space between alteration and description
			'LEGEND_PADDING'                    : 15, // space between alteration / descriptions pairs
			'LEGEND_FOOTNOTE_SPACING'           : 10, // space between alteration / descriptions pairs and footnote
			// tooltip region
			'TOOLTIP_REGION_WIDTH'              : 500, // width of tooltip region
			'TOOLTIP_REGION_HEIGHT'             : 60, // height of tooltip region
			'TOOLTIP_HORIZONTAL_PADDING'        : 20, // space between header region and tooltip region
			'TOOLTIP_FONT'                      : "normal 12px arial",
			'TOOLTIP_COLOR'                     : "#000000",
			'TOOLTIP_MARGIN'                    : 10,
			'TOOLTIP_TEXT'                      : "Hover over a sample to view details.",
			'ALT_TOOLTIP_TEXT'                  : "Tooltips are disabled when white space is removed.",
			// header
			'HEADER_VERTICAL_SPACING'           : 15, // space between sentences that wrap
			'HEADER_VERTICAL_PADDING'           : 25, // space between header sentences
            // general sample properties
			'ALTERED_SAMPLES_ONLY'              : false,
			// scale factor
			'SCALE_FACTOR_X'                    : 1.0,
			// remove genomics alteration padding
			'REMOVE_GENOMIC_ALTERATION_HPADDING': false
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

	// compute case set description label length
	var scratchCanvas = Raphael(0,0, 1, 1);
	var text = scratchCanvas.text(0,0, DEFAULTS.get('CASE_SET_DESCRIPTION_LABEL'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	var caseSetDescriptionLabelLength = text.getBBox().width;
	text = scratchCanvas.text(0,0, "<");
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	var lessThanSignLength = text.getBBox().width;
	text = scratchCanvas.text(0,0, "1");
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	var digitLength = text.getBBox().width;


	return {
		// header element is used later to determine location of tooltip canvas
		'header_element'                    : headerElement,
		// setup canvases - these will be resized later
		'header_canvas'                     : Raphael(headerElement, 1, 1),
		'body_canvas'                       : Raphael(bodyElement, 1, 1),
		'legend_canvas'                     : Raphael(legendElement, 1, 1),
		'tooltip_canvas'                    : null,
		// longest label length
		'longest_label_length'              : 0,
		'case_set_description_label_length' : caseSetDescriptionLabelLength,
		'less_than_sign_length'             : lessThanSignLength,
		'digit_length'                      : digitLength,
		'longest_percent_altered_length'    : 0,
		// general styles
		'alteration_width'                  : DEFAULTS.get('ALTERATION_WIDTH'),
		'alteration_height'                 : DEFAULTS.get('ALTERATION_HEIGHT'),
		'alteration_vertical_padding'       : DEFAULTS.get('ALTERATION_VERTICAL_PADDING'),
		'alteration_horizontal_padding'     : DEFAULTS.get('ALTERATION_HORIZONTAL_PADDING'),
		// mrna styles
		'mrna_wireframe_width_scale_factor' : DEFAULTS.get('MRNA_WIREFRAME_WIDTH_SCALE_FACTOR'),
		// mutation	styles
		'mutation_height_scale_factor'      : DEFAULTS.get('MUTATION_HEIGHT_SCALE_FACTOR'),
		// this is used to change behavior of getXCoordinate() & getYCoordinate()
		'use_immediate_coordinates'         : false,
		// general sample properties
		'altered_samples_only'              : DEFAULTS.get('ALTERED_SAMPLES_ONLY'),
		// scale factor
		'scale_factor_x'                    : DEFAULTS.get('SCALE_FACTOR_X'),
		// remove padding between genomic alterations
		'remove_genomic_alteration_hpadding': DEFAULTS.get('REMOVE_GENOMIC_ALTERATION_HPADDING'),
		// keeps track of number of contigous samples (when remove genomic alteration hpadding is set
		'num_contiguous_samples'            : 0,
		// keep track of last y pos - used to know where to render next genomics alretarion
		'last_y_pos'                        : 0
	};
}

/*
 * Draws the OncoPrint header.
 *
 * oncoprint - opaque reference to oncoprint system
 * longestLabel - the longest label in the oncoprint (saves us some leg-work)
 * headerVariables - various header strings
 * forSummaryTab - flag indicating if we are rendering header for Summary Tab (if not, Cross Cancer Study)
 *
 * Note: headerVariables is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintHeaderVariables()
 *
 */
function DrawOncoPrintHeader(oncoprint, longestLabel, headerVariables, forSummaryTab) {

	if (forSummaryTab) {
		drawOncoPrintHeaderForSummaryTab(oncoprint, longestLabel, headerVariables);
	}
	else {
		drawOncoPrintHeaderForCrossCancerSummary(oncoprint, longestLabel, headerVariables);
	}
}

/* 
 * Draw an OncoPrint for the entire set of genetic
 * alterations on the given canvas using the given style.
 *
 * oncoprint - opaque reference to oncoprint system
 * longestLabel - the longest label in the oncoprint (saves us some leg-work)
 * geneticAlterations - the set of geneticAlterations to draw
 * wantToolTip - flag indicating if we want tool tips (yes for Summary Tab no for Cross Cancer Summary)
 *
 * Note: geneticAlterations is a JSON object literal that is
 * created by:
 * org.mskcc.portal.util.MakeOncoPrint.writeOncoPrintGeneticAlterationVariable()
 * 
 */
function DrawOncoPrintBody(oncoprint, longestLabel, geneticAlterations, wantTooltip) {

	// this is so row/col values are used in computation of x,y coords
	oncoprint.use_immediate_coordinates = false;

	// set longest label length
	oncoprint.longest_label_length = getLabelLength(longestLabel);

	// set longest % altered length
	oncoprint.longest_percent_altered_length = getLongestPercentAlteredLength(geneticAlterations);

	// resize canvas
	var dimension = getOncoPrintBodyCanvasSize(oncoprint,
											   geneticAlterations.length,
											   geneticAlterations[0].alterations.length);
	oncoprint.body_canvas.setSize(dimension.width, dimension.height);
	oncoprint.body_canvas.clear();

	// we need to change the default tooltip text when genomic alteration padding is set
	if (wantTooltip) {
		var text = (oncoprint.remove_genomic_alteration_hpadding) ?
			DEFAULTS.get('ALT_TOOLTIP_TEXT') : DEFAULTS.get('TOOLTIP_TEXT')
		addTooltipText(oncoprint, text);
	}

	// used to filter out unaltered samples in loop below
	var unalteredSample = (CNA_NONE | MRNA_NOTSHOWN | NORMAL);

	// iterate over all genetic alterations
	for (var lc = 0; lc < geneticAlterations.length; lc++) {
		oncoprint.last_y_pos = 0; // must come before any rendering
		var alteration = geneticAlterations[lc];
		// draw label first
		drawGeneLabel(oncoprint, lc, alteration.hugoGeneSymbol, alteration.percentAltered);
		oncoprint.num_contiguous_samples = 0;
		// for this gene, interate over all samples
		for (var lc2 = 0; lc2 < alteration.alterations.length; lc2++) {
			var thisSampleAlteration = alteration.alterations[lc2];
			// handle altered samples only bool - only render 
			if (oncoprint.altered_samples_only && (thisSampleAlteration.alteration == unalteredSample)) {
				continue;
			}
			// when remove genomic alteration padding is set, we only want to render rect for contiguous blocks
            else if (oncoprint.remove_genomic_alteration_hpadding) {
				++oncoprint.num_contiguous_samples;
                if (thisSampleAlteration.alteration == getNextAlteration(oncoprint, alteration.alterations, lc2+1)) {
                    continue;
                }
            }
			else {
				oncoprint.num_contiguous_samples = 1;
			}
			// first draw MRNA "background"
			var last_y_pos = drawMRNA(oncoprint, oncoprint.body_canvas, lc, null, thisSampleAlteration.alteration);
			// then draw CNA "within"
			drawCNA(oncoprint, oncoprint.body_canvas, lc, null, thisSampleAlteration.alteration);
			// finally draw mutation square "on top"
			drawMutation(oncoprint, oncoprint.body_canvas, lc, null, thisSampleAlteration.alteration);
			// tooltip
			if (wantTooltip & !oncoprint.remove_genomic_alteration_hpadding) {
				var tooltipText = "Sample: " + thisSampleAlteration.sample;
				if (thisSampleAlteration.mutation != null) {
					tooltipText = tooltipText + "\nAmino Acid Change: ";
					for (var lc3 = 0; lc3 < thisSampleAlteration.mutation.length; lc3++) {
						tooltipText = tooltipText + thisSampleAlteration.mutation[lc3] + ", ";
					}
					// zap off last ', '
					tooltipText = tooltipText.substring(0, tooltipText.length - 2);
				}
				createTooltip(oncoprint, lc, null, tooltipText);
			}
			// update some vars needed for next go-around
			oncoprint.last_y_pos = last_y_pos;
			oncoprint.num_contiguous_samples = 0;
		}
	}
	if (oncoprint.scale_factor_x != 1.0) {
		scaleBodyCanvas(oncoprint);
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
		drawMRNA(oncoprint, oncoprint.legend_canvas, 0, x, alteration.alteration);
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
 * Draws canvas used for "tooltips"
 *
 * oncoprint - opaque reference to oncoprint system
 * parentElement - element we live within (probably oncoprint_section div)
 * nearestControlElement - the nearest control to the tooltip canvas
 *
 */
function DrawOncoPrintTooltipRegion(oncoprint, parentElement, nearestControlElement) {

	// compute pos and dimension of tooltip canvas
	var parentElementPos = findPos(parentElement);
	var parentElementWidth = $(parentElement).width();
	var nearestControlElementPos = findPos(nearestControlElement);
	var nearestControlElementWidth = $(nearestControlElement).width();
	var headerPos = findPos(oncoprint.header_element);
	var x = headerPos[0] + oncoprint.header_canvas.width + DEFAULTS.get('TOOLTIP_HORIZONTAL_PADDING');
	if (x < nearestControlElementPos[0] + nearestControlElementWidth) {
		x = nearestControlElementPos[0] + nearestControlElementWidth + DEFAULTS.get('TOOLTIP_HORIZONTAL_PADDING');
	}
	var width = DEFAULTS.get('TOOLTIP_REGION_WIDTH');
	if (x + width > parentElementPos[0] + parentElementWidth) {
		width = parentElementPos[0] + parentElementWidth - x;
	}
	var y = headerPos[1];
	var height = DEFAULTS.get('TOOLTIP_REGION_HEIGHT');
	if (oncoprint.tooltip_canvas != null) {
		oncoprint.tooltip_canvas.remove();
	}
	oncoprint.tooltip_canvas = Raphael(x, y, width, height);

	// add place holder text
	var text = (oncoprint.remove_genomic_alteration_hpadding) ?
		DEFAULTS.get('ALT_TOOLTIP_TEXT') : DEFAULTS.get('TOOLTIP_TEXT')
	addTooltipText(oncoprint, text);
}

/*
 * Clears canvas used for "tooltips"
 *
 * oncoprint - opaque reference to oncoprint system
 *
 */
function ClearOncoPrintTooltipRegion(oncoprint) {

	if (oncoprint.tooltip_canvas != null) {
		oncoprint.tooltip_canvas.clear();
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
 * Toggles show altered samples only property.
 *
 * oncoprint - opaque reference to oncoprint system
 * showAlteredSamples - flag indicating if only altered samples should be displayed
 *
 */
function ShowAlteredSamples(oncoprint, showAlteredSamples) {

	oncoprint.altered_samples_only = showAlteredSamples;
}

/*
 * Sets scale factor X.  We expect values between 0-99,
 * and which get changed to values between 1.0 and 0.01
 *
 * oncoprint - opaque reference to oncoprint system
 * scaleFactorX - new X scale factor
 *
 */
function SetScaleFactor(oncoprint, scaleFactorX) {

	// sanity check
	if (scaleFactorX < 0) {
		scaleFactorX = 0;
	}
	else if (scaleFactorX > 99) {
		scaleFactorX = 99;
	}
	oncoprint.scale_factor_x = (100-scaleFactorX) / 100;

	// redraw
	scaleBodyCanvas(oncoprint);
}

/*
 * Toggles genomic alteration padding property.
 *
 * oncoprint - opaque reference to oncoprint system
 * removeGenomicAlterationPadding  - flag indicating if alteration padding should be removed
 *
 */
function RemoveGenomicAlterationPadding(oncoprint, removeGenomicAlterationPadding) {

	oncoprint.remove_genomic_alteration_hpadding = removeGenomicAlterationPadding;

	oncoprint.mrna_wireframe_width_scale_factor = (removeGenomicAlterationPadding) ?
		DEFAULTS.get('MRNA_WIREFRAME_WIDTH_SCALE_FACTOR2') :
		DEFAULTS.get('MRNA_WIREFRAME_WIDTH_SCALE_FACTOR');
}

/*******************************************************************************
//
// The following functions are not meant to be used outside of this library.
//
*******************************************************************************/

/*
 * Draws the OncoPrint header for use with the Summary Tab.
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
function drawOncoPrintHeaderForSummaryTab(oncoprint, longestLabel, headerVariables) {

	// vars used below
	var x, y;
	var text;
	var singleLineDescription = (headerVariables.get('CASE_SET_DESCRIPTION').indexOf("\n") == -1);

	// set longest label length
	oncoprint.longest_label_length = getLabelLength(longestLabel);

	// resize canvas 
 	var dimension = getOncoPrintHeaderCanvasSize(headerVariables, longestLabel, true);
	oncoprint.header_canvas.clear();

	// render case list description
	x = 0;
	y = DEFAULTS.get('HEADER_VERTICAL_SPACING');
	if (singleLineDescription) {
		text = oncoprint.header_canvas.text(x, y, headerVariables.get('CASE_SET_DESCRIPTION'));
		text.attr('font', DEFAULTS.get('LABEL_FONT'));
		text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
		text.attr('text-anchor', 'start');
	}
	else {
		var descriptionStrings = headerVariables.get('CASE_SET_DESCRIPTION').split("\n");
		// first line
		text = oncoprint.header_canvas.text(x, y, descriptionStrings[0]);
		text.attr('font', DEFAULTS.get('LABEL_FONT'));
		text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
		text.attr('text-anchor', 'start');
		// second line
		y = y + DEFAULTS.get('HEADER_VERTICAL_SPACING');
		text = oncoprint.header_canvas.text(oncoprint.case_set_description_label_length,
											y, descriptionStrings[1]);
		text.attr('font', DEFAULTS.get('LABEL_FONT'));
		text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
		text.attr('text-anchor', 'start');
	}

	// render altered stats
	y = y + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	text = oncoprint.header_canvas.text(x, y, headerVariables.get('ALTERED_STATS'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');

	// % altered column heading line one
	x = oncoprint.longest_label_length - DEFAULTS.get('LABEL_PADDING') * 2;
	y = y + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	var percentAlteredStrings = headerVariables.get('PERCENT_ALTERED_COLUMN_HEADING').split("\n");
	text = oncoprint.header_canvas.text(x, y, percentAlteredStrings[0]);
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

	// % altered column heading line two
	x = oncoprint.longest_label_length - DEFAULTS.get('LABEL_PADDING') * 2;
	y = y + DEFAULTS.get('HEADER_VERTICAL_SPACING');
	text = oncoprint.header_canvas.text(x, y, percentAlteredStrings[1]);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');

	// set the size of the canvas here - after we know the proper height
	oncoprint.header_canvas.setSize(dimension.width, y + DEFAULTS.get('HEADER_VERTICAL_SPACING'));
}

/*
 * Draws the OncoPrint header for use with Cross Cancer Summaries.
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
function drawOncoPrintHeaderForCrossCancerSummary(oncoprint, longestLabel, headerVariables) {

	// vars used below
	var x, y;
	var text;

	// set longest label length
	oncoprint.longest_label_length = getLabelLength(longestLabel);

	// resize canvas 
 	var dimension = getOncoPrintHeaderCanvasSize(headerVariables, longestLabel, false);
	// make a few minor adjustment for Cross Cancer Summary Page
	dimension.width = dimension.width + oncoprint.longest_label_length;
	//oncoprint.header_canvas.setSize(dimension.width, dimension.height);
	oncoprint.header_canvas.clear();

	// render altered stats
	x = 0;
	y = DEFAULTS.get('HEADER_VERTICAL_PADDING');
	text = oncoprint.header_canvas.text(x, y, headerVariables.get('ALTERED_STATS'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');

	// % altered column heading line one
	x = oncoprint.longest_label_length - DEFAULTS.get('LABEL_PADDING') * 2;
	y = y + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	var percentAlteredStrings = headerVariables.get('PERCENT_ALTERED_COLUMN_HEADING').split("\n");
	text = oncoprint.header_canvas.text(x, y, percentAlteredStrings[0]);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');

	// samples column heading
	x = oncoprint.longest_label_length;
	text = oncoprint.header_canvas.text(x, y, headerVariables.get('ALL_SAMPLES_COLUMN_HEADING'));
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'start');

	// % altered column heading line two
	x = oncoprint.longest_label_length - DEFAULTS.get('LABEL_PADDING') * 2;
	y = y + DEFAULTS.get('HEADER_VERTICAL_SPACING');
	text = oncoprint.header_canvas.text(x, y, percentAlteredStrings[1]);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');

	// set the size of the canvas here - after we know the proper height
	oncoprint.header_canvas.setSize(dimension.width, y + DEFAULTS.get('HEADER_VERTICAL_SPACING'));
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
	if (percentAltered.indexOf("<") != -1) {
		x = x + oncoprint.less_than_sign_length;
	}
	var justificationChars = (oncoprint.longest_percent_altered_length > percentAltered.length) ?
		oncoprint.longest_percent_altered_length - percentAltered.length : 0;
	x = x - (oncoprint.digit_length * justificationChars);
	text = oncoprint.body_canvas.text(x, y, geneSymbol);
	text.attr('font', DEFAULTS.get('LABEL_FONT'));
	text.attr('fill', DEFAULTS.get('LABEL_COLOR'));
	text.attr('text-anchor', 'end');
}

/*
 * Draws an mRNA genomic alteration at given row & col.
 *
 * Returns the last y pos of rect.
 *
 * oncoprint - opaque reference to oncoprint system
 * canvas - canvas to draw on
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * alterationSettings - the genomic alteration
 *
 */
function drawMRNA(oncoprint, canvas, row, column, alterationSettings) {

	// compute starting coordinates
	var y = getYCoordinate(oncoprint, row);
	var	x = getXCoordinate(oncoprint, column);
	// create canvas rect
	var alteration_width = (oncoprint.remove_genomic_alteration_hpadding) ?
		(oncoprint.alteration_width * oncoprint.num_contiguous_samples) : oncoprint.alteration_width;
	var rect = canvas.rect(x, y, alteration_width, oncoprint.alteration_height);
	// without this we get thin black border around rect
	rect.attr('stroke', 'none'); 
	// choose fill color based on alteration type
	if (alterationSettings & MRNA_UPREGULATED) {
		rect.attr('fill', DEFAULTS.get('MRNA_UPREGULATED_COLOR'));
	}
	else if (alterationSettings & MRNA_DOWNREGULATED) {
		rect.attr('fill', DEFAULTS.get('MRNA_DOWNREGULATED_COLOR'));
	}
	else if (alterationSettings & MRNA_NOTSHOWN) {
		rect.attr('fill', DEFAULTS.get('MRNA_NOTSHOWN_COLOR'));
	}

	// outta here
	return x + alteration_width;
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
	var alteration_width = (oncoprint.remove_genomic_alteration_hpadding) ?
		(oncoprint.alteration_width * oncoprint.num_contiguous_samples) : oncoprint.alteration_width;
	var rect = canvas.rect(x + mrnaWireframeWidth,
						   y + mrnaWireframeWidth,
						   alteration_width - mrnaWireframeWidth * 2,
						   oncoprint.alteration_height - mrnaWireframeWidth * 2);
	// without this we get thin black border around rect
	rect.attr('stroke', 'none'); 
	// choose fill color based on alteration type
	if (alterationSettings & CNA_AMPLIFIED) {
		rect.attr('fill', DEFAULTS.get('CNA_AMPLIFIED_COLOR'));
	}
	else if (alterationSettings & CNA_GAINED) {
		rect.attr('fill', DEFAULTS.get('CNA_GAINED_COLOR'));
	}
	else if (alterationSettings & CNA_DIPLOID) {
		rect.attr('fill', DEFAULTS.get('CNA_DIPLOID_COLOR'));
	}
	else if (alterationSettings & CNA_HEMIZYGOUSLYDELETED) {
		rect.attr('fill', DEFAULTS.get('CNA_HEMIZYGOUSLYDELETED_COLOR'));
	}
	else if (alterationSettings & CNA_HOMODELETED) {
		rect.attr('fill', DEFAULTS.get('CNA_HOMODELETED_COLOR'));
	}
	else if (alterationSettings & CNA_NONE) {
		rect.attr('fill', DEFAULTS.get('CNA_NONE_COLOR'));
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
 * Computes dimensions of the OncoPrint Header canvas.
 * Also returns height of a text string.
 *
 * headerVariables - various header strings
 * longestLabel - the longest label in the oncoprint (saves us some leg-work)
 * forSummaryTab - flag indicating if we are rendering header for Summary Tab (if not, Cross Cancer Study)
 *
 */
function getOncoPrintHeaderCanvasSize(headerVariables, longestLabel, forSummaryTab) {

	// vars used below
	var text;
	var boundingBox;
	var canvasWidth = 0;
	var canvasHeight = 0;
	var scratchCanvas = Raphael(0, 0, 1, 1);

	// case set description (only used on Summary Tab)
	var caseDescriptionIsLongest = caseDescriptionIsLongestString(headerVariables, longestLabel);
	if (forSummaryTab) {
		if (caseDescriptionIsLongest) {
			text = scratchCanvas.text(0,0, headerVariables.get('CASE_SET_DESCRIPTION'));
		}
		else {
			text = scratchCanvas.text(0,0, longestLabel + headerVariables.get('ALTERED_SAMPLES_COLUMN_HEADING'));
		}
		text.attr('font', DEFAULTS.get('LABEL_FONT'));
	}
	else {
		// assume altered samples column heading is greater than all samples column heading
		text = scratchCanvas.text(0,0, headerVariables.get('ALTERED_SAMPLES_COLUMN_HEADING'));
		text.attr('font', DEFAULTS.get('LABEL_FONT'));
	}
	boundingBox = text.getBBox();
	canvasWidth = boundingBox.width;
	// if case list description is not longest string,
	// the longest string is longest gene label + altered samples column heading
	// so we must include formatting in width
	if (!caseDescriptionIsLongest) {
		canvasWidth = canvasWidth + DEFAULTS.get('LABEL_SPACING') + DEFAULTS.get('LABEL_PADDING');
	}

	// only include this height if summary tab (for case set description)
	if (forSummaryTab) {
		canvasHeight = canvasHeight + boundingBox.height;
		// description may be two lines, in which case we want to add an extra space
		if (headerVariables.get('CASE_SET_DESCRIPTION').indexOf("\n") != -1) {
			canvasHeight = canvasHeight + boundingBox.height;
		}
	}

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
	// even though Cross Cancer does not have case set desc, we will use extra padding
	//canvasHeight = canvasHeight + DEFAULTS.get('HEADER_VERTICAL_PADDING');
	
	// clean up
	text.remove();
	scratchCanvas.remove();

	// outta here
	return { 'width' : canvasWidth, 'height' : canvasHeight };
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
 * Determines if case description is longer than 
 * than longest gene label & altered samples column heading
 *
 * headerVariables - various header strings
 * longestLabel - the longest label in the oncoprint (saves us some leg-work)
 *
 */
function caseDescriptionIsLongestString(headerVariables, longestLabel) {

	var caseSetDescription = headerVariables.get('CASE_SET_DESCRIPTION');
	// assume altered samples column heading is greater than all samples column heading
	var alteredSamplesHeading = headerVariables.get('ALTERED_SAMPLES_COLUMN_HEADING');
	return (caseSetDescription.length >= (longestLabel.length + alteredSamplesHeading.length));
}

/*
 * Computes the length (in pixels) of the given label.
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
 * Returns the longest percent altered string len (in chars).
 *
 * geneticAlterations - the genetic alterations
 *
 */
function getLongestPercentAlteredLength(geneticAlterations) {

	var maxPercentAlteredStringLen = 0;
	for (var lc = 0; lc < geneticAlterations.length; lc++) {
		if (geneticAlterations[lc].percentAltered.length > maxPercentAlteredStringLen) {
			maxPercentAlteredStringLen = geneticAlterations[lc].percentAltered.length;
		}
	}
	return maxPercentAlteredStringLen;
}

/*
 * For the given column (sample) return the x coordinate
 *
 * oncoprint - opaque reference to oncoprint system
 * column - sample we are processing
 *
 */
function getXCoordinate(oncoprint, column) {

	if (oncoprint.use_immediate_coordinates) {
		// used to get element coordinates for legend rendering
		return column;
	}
	else {
		if (oncoprint.last_y_pos == 0) {
			return oncoprint.longest_label_length;
		}
		else {
			var padding = (oncoprint.remove_genomic_alteration_hpadding) ?
				0 : oncoprint.alteration_horizontal_padding;
			return oncoprint.last_y_pos + padding;
		}
	}
}

/*
 * For the given row (gene) return the y coordinate
 *
 * oncoprint - opaque reference to oncoprint system
 * row - gene we are processing
 *
 */
function getYCoordinate(oncoprint, row) {

	if (oncoprint.use_immediate_coordinates) {
		// used to get element coordinates for legend rendering
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

	var width = (oncoprint.remove_genomic_alteration_hpadding) ?
		(oncoprint.alteration_width * oncoprint.num_contiguous_samples) : oncoprint.alteration_width;
	width = width - getMRNAWireframeWidth(oncoprint);

	var height = oncoprint.alteration_height * oncoprint.mutation_height_scale_factor;

	return { 'width' : width, 'height' : height };
}

/**
 * Routine called to setup tooltip text on mouse over of genetic alteration rect.
 *
 * oncoprint - opaque reference to oncoprint system
 * row - the vertical position to draw the alteration
 * column - the horizontal position to draw the alteration
 * tooltipText - the text to render in the tooltip
 *
 */
function createTooltip(oncoprint, row, column, tooltipText) {

	var rect = oncoprint.body_canvas.rect(getXCoordinate(oncoprint, column),
										  getYCoordinate(oncoprint, row),
										  oncoprint.alteration_width,
										  oncoprint.alteration_height);

	// without adding fill, mouseover will not work
	rect.attr('fill', '#000000');
	rect.attr('opacity', 0);

	rect.node.style.cursor = "default";
	rect.node.onmouseover = function () {
		addTooltipText(oncoprint, tooltipText);
	};

	// on mouse out, reset text
	rect.node.onmouseout = function () {
		addTooltipText(oncoprint, DEFAULTS.get('TOOLTIP_TEXT'));
	};
}

/**
 * Routine which adds tooltip text tooltip canvas
 *
 * oncoprint - opaque reference to oncoprint system
 * tooltipText - the text to render in the tooltip
 *
 */
function addTooltipText(oncoprint, tooltipText) {

	// sanity check
	if (oncoprint.tooltip_canvas == null) {
		return;
	}

	// clear off the canvas
	ClearOncoPrintTooltipRegion(oncoprint);

	// add back background
	var rect = oncoprint.tooltip_canvas.rect(0, 0,
											 DEFAULTS.get('TOOLTIP_REGION_WIDTH'),
											 DEFAULTS.get('TOOLTIP_REGION_HEIGHT'));
	rect.attr('stroke', 'none'); 
	rect.attr('fill', '#eeeeee');

	// create the text object
	var text = oncoprint.tooltip_canvas.text(DEFAULTS.get('TOOLTIP_MARGIN'),
											 oncoprint.tooltip_canvas.height / 2,
											 tooltipText);
	text.attr('font', DEFAULTS.get('TOOLTIP_FONT'));
	text.attr('fill', DEFAULTS.get('TOOLTIP_COLOR'));
	text.attr('text-anchor', 'start');
}

/**
 * Routine which scales body canvas.  Elements are then
 * translated based on the scaling dx. The scaling dx
 * is only computed for the first element.
 *
 * oncoprint - opaque reference to oncoprint system
 *
 */
function scaleBodyCanvas(oncoprint) {

	var dx = 0;
	var startingX = 0;
	var dxSet = false;
	var translationFactor = 0;
	var scaleFactorX = oncoprint.scale_factor_x;
	oncoprint.body_canvas.forEach(function(obj) {
		var node = obj.node;
		if (obj.node instanceof SVGRectElement) {
			var x = obj.attr('x');
			var y= obj.attr('y');
			obj.transform('S' + scaleFactorX + ',1.0,0,0');
			if (!dxSet) {
				startingX = x;
				dx = obj.matrix.x(x, y);
				translationFactor = (startingX-dx).toString();
				dxSet = true;
			}
			if (startingX != dx) {
				obj.transform('...T' + translationFactor + ",0");
			}
		}
	});
}

/**
 * Routine which returns next alteration, taking into account altered_samples_only property.
 *
 * oncoprint - opaque reference to oncoprint system
 * alterations - list of alterations
 * index - starting index
 *
 */
function getNextAlteration(oncoprint, alterations, index) {

        var unalteredSample = (CNA_NONE | MRNA_NOTSHOWN | NORMAL);      
        for (var lc = index; lc < alterations.length; lc++) {
                var thisSampleAlteration = alterations[lc].alteration;
                if (oncoprint.altered_samples_only && (thisSampleAlteration == unalteredSample)) {
                        continue;
                }
                else {
                        return thisSampleAlteration;
                }
        }

        // outta here
        return null;
}

/*******************************************************************************
//
// The following functions were obtained from:
//
// http://www.greywyvern.com/?post=331
//
// They are used to determine the position of the tooltip canvas.
//
*******************************************************************************/

function findPos(obj) {
  var curleft = curtop = 0, scr = obj, fixed = false;
  while ((scr = scr.parentNode) && scr != document.body) {
    curleft -= scr.scrollLeft || 0;
    curtop -= scr.scrollTop || 0;
    if (getStyle(scr, "position") == "fixed") fixed = true;
  }
  if (fixed && !window.opera) {
    var scrDist = scrollDist();
    curleft += scrDist[0];
    curtop += scrDist[1];
  }
  do {
    curleft += obj.offsetLeft;
    curtop += obj.offsetTop;
  } while (obj = obj.offsetParent);
  return [curleft, curtop];
}

function scrollDist() {
  var html = document.getElementsByTagName('html')[0];
  if (html.scrollTop && document.documentElement.scrollTop) {
    return [html.scrollLeft, html.scrollTop];
  } else if (html.scrollTop || document.documentElement.scrollTop) {
    return [
      html.scrollLeft + document.documentElement.scrollLeft,
      html.scrollTop + document.documentElement.scrollTop
    ];
  } else if (document.body.scrollTop)
    return [document.body.scrollLeft, document.body.scrollTop];
  return [0, 0];
}

function getStyle(obj, styleProp) {
  if (obj.currentStyle) {
    var y = obj.currentStyle[styleProp];
  } else if (window.getComputedStyle)
    var y = window.getComputedStyle(obj, null)[styleProp];
  return y;
}
