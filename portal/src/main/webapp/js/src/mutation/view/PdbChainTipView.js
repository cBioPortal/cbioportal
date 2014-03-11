/**
 * Tooltip view for the PDB panel chain rectangles.
 *
 * options: {el: [target container],
 *           model: {pdbId, pdbInfo, chain}
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
		var summary = "";

		// TODO this can be implemented in a better way
		if (this.model.pdbInfo)
		{
			summary = "<b>Summary:</b> " + this.model.pdbInfo;
		}

		// pass variables in using Underscore.js template
		var variables = {pdbId: this.model.pdbId,
			pdbInfo: summary,
			chainId: this.model.chain.chainId};

		// compile the template using underscore
		return _.template(
				$("#mutation_details_pdb_chain_tip_template").html(),
				variables);
	}
});