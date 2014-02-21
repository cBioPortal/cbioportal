

var PdbAlignmentModel = Backbone.Model.extend({
	initialize: function(attributes) {
		this.alignmentId = attributes.alignmentId;
		this.pdbId = attributes.pdbId;
		this.chain = attributes.chain;
		this.uniprotId = attributes.uniprotId;
		this.pdbFrom = attributes.pdbFrom;
		this.pdbTo = attributes.pdbTo;
		this.uniprotFrom = attributes.uniprotFrom;
		this.uniprotTo = attributes.uniprotTo;
		this.alignmentString = attributes.alignmentString;
		this.eValue = attributes.eValue;
		this.identityPerc = attributes.identityPerc;
	}
});

/**
 * Collection of pdb alignment data (PdbAlignmentModel instances).
 */
var PdbAlignmentCollection = Backbone.Collection.extend({
	model: PdbAlignmentModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});