/**
 * Information view for the 3D Visualization panel.
 *
 * options: {el: [target container],
 *           model: {pdbId: String,
 *                   chainId: String,
 *                   pdbInfo: String}
 *          }
 *
 * @author Selcuk Onur Sumer
 */
var Mutation3dVisInfoView = Backbone.View.extend({
	render: function()
	{
		var self = this;

		// compile the template using underscore
		var template = _.template(
			$("#mutation_3d_vis_info_template").html(),
			self.model);

		// load the compiled HTML into the Backbone "el"
		self.$el.html(template);

		// format after rendering
		self.format();
	},
	format: function()
	{
		var self = this;
		var pdbInfo = self.model.pdbInfo;

		// if no info provided, then hide the corresponding span
		if (pdbInfo == null ||
		    pdbInfo.length == 0)
		{
			self.$el.find(".mutation-3d-pdb-info").hide();
		}
	}
});
