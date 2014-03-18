/**
 * Tooltip view for the PDB panel chain rectangles.
 *
 * options: {el: [target container],
 *           model: {pdbId, chain, pdbInfo, molInfo}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var PdbChainTipView = Backbone.View.extend({
	render: function()
	{
		// compile the template
		var template = this.compileTemplate();

		// load the compiled HTML into the Backbone "el"
		this.$el.html(template);
		this.format();
	},
	format: function()
	{
		// implement if necessary...
	},
	compileTemplate: function()
	{
		var self = this;
		var pdbInfo = self.model.pdbInfo;
		var molInfo = self.model.molInfo;

		// pass variables in using Underscore.js template
		var variables = {pdbId: self.model.pdbId,
			chainId: self.model.chain.chainId,
			pdbInfo: "",
			molInfo: ""};

		// TODO this can be implemented in a better way

		if (pdbInfo != null ||
		    pdbInfo.length > 0)
		{
			variables.pdbInfo = ": " + pdbInfo;
		}

		if (molInfo != null ||
		    molInfo.length > 0)
		{
			variables.molInfo = ": " + molInfo;
		}

		// compile the template using underscore
		return _.template(
				$("#mutation_details_pdb_chain_tip_template").html(),
				variables);
	}
});