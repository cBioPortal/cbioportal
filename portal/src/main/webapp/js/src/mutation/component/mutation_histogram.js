/**
 * Constructor for MutationHistogram class.
 *
 * @param geneSymbol    hugo gene symbol
 * @param options       visual options object
 * @param data          collection of Mutation models (MutationCollection)
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
function MutationHistogram(geneSymbol, options, data)
{
	// call super constructor
	MutationDiagram.call(this, geneSymbol, options, data);
}

// this is for inheritance (MutationHistogram extends MutationDiagram)
MutationHistogram.prototype = new MutationDiagram();
MutationHistogram.prototype.constructor = MutationHistogram;

/**
 * Draws histogram bars on the plot area.
 *
 * @param svg       svg container for the diagram
 * @param pileups   array of mutations (pileups)
 * @param options   options object
 * @param bounds    bounds of the plot area {width, height, x, y}
 *                  x, y is the actual position of the origin
 * @param xScale    scale function for the x-axis
 * @param yScale    scale function for the y-axis
 * @override
 */
MutationHistogram.prototype.drawPlot = function(svg, pileups, options, bounds, xScale, yScale)
{
	// TODO draw multi color animated histogram lines
};