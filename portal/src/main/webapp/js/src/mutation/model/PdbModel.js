/**
 * PDB data model.
 *
 * Contains PDB id and a chain list.
 *
 * @author Selcuk Onur Sumer
 */
var PdbModel = Backbone.Model.extend({
	initialize: function(attributes) {
		// pdb id (e.g: 1d5r)
		this.pdbId = attributes.pdbId;
		// collection of PdbChainModel instances
		this.chains = new PdbChainCollection(attributes.chains);
	}
});

/**
 * Collection of pdb data (PdbModel instances).
 */
var PdbCollection = Backbone.Collection.extend({
	model: PdbModel,
	initialize: function(options) {
		// TODO add & set attributes if required
	}
});
