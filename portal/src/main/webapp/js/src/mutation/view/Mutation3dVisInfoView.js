/**
 * Information view for the 3D Visualization panel.
 *
 * options: {el: [target container],
 *           model: {pdbId: String,
 *                   chainId: String,
 *                   pdbInfo: String,
 *                   molInfo: String}
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
		var molInfo = self.model.molInfo;

		// if no info provided, then hide the corresponding span
		if (pdbInfo == null ||
		    pdbInfo.length == 0)
		{
			self.$el.find(".mutation-3d-pdb-info").hide();
		}
		else
		{
			// make information text expandable/collapsible
			self._addExpander(".mutation-3d-pdb-info");
		}

		if (molInfo == null ||
		    molInfo.length == 0)
		{
			self.$el.find(".mutation-3d-mol-info").hide();
		}
		else
		{
			// make information text expandable/collapsible
			self._addExpander(".mutation-3d-mol-info");
		}
	},
	/**
	 * Applies expander plugin to the PDB info area. The options are
	 * optimized to have 1 line of description at init.
	 */
	_addExpander: function(selector)
	{
		var self = this;

		var expanderOpts = {slicePoint: 40, // default is 100
			widow: 2,
			expandPrefix: ' ',
			expandText: '[...]',
			//collapseTimer: 5000, // default is 0, so no re-collapsing
			userCollapseText: '[^]',
			moreClass: 'expander-read-more',
			lessClass: 'expander-read-less',
			detailClass: 'expander-details',
			// do not use default effects
			// (see https://github.com/kswedberg/jquery-expander/issues/46)
			expandEffect: 'fadeIn',
			collapseEffect: 'fadeOut'};

		//self.$el.find(".mutation-3d-info-main").expander(expanderOpts);
		self.$el.find(selector).expander(expanderOpts);
	}
});
