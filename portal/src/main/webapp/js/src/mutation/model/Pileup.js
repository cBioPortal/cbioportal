/**
 * Pileup Model.
 *
 * This model is designed to represent multiple mutations at the same
 * position. This is intended to be used for mutation diagram.
 *
 * @author Selcuk Onur Sumer
 */
var Pileup = Backbone.Model.extend({
	initialize: function(attributes) {
		this.pileupId = attributes.pileupId; // incremental id (client-side generated)
		this.mutations = attributes.mutations; // array of mutations at this data point
		this.count = attributes.count; // number of mutations at this data point
		this.location = attributes.location; // the location of the mutations
		this.label = attributes.label; // text label for this data point
		this.stats = attributes.stats;
	}
});