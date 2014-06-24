/**
 * PDB Chain Model.
 *
 * @author Selcuk Onur Sumer
 */
var PdbChainModel = Backbone.Model.extend({
	initialize: function(attributes) {
		// chain id (A, B, C, X, etc.)
		this.chainId = attributes.chainId;
		//  map of (mutation id, pdb position) pairs
		this.positionMap = attributes.positionMap;
		// collection of PdbAlignmentModel instances
		this.alignments = new PdbAlignmentCollection(attributes.alignments);
		// summary of all alignments (merged alignments)
		// TODO define a model for merged alignments (PdbMergedAlignment) ?
		this.mergedAlignment = PdbDataUtil.mergeAlignments(attributes.alignments);
	}
});

/**
 * Collection of pdb data (PdbModel instances).
 */
var PdbChainCollection = Backbone.Collection.extend({
	model: PdbChainModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});